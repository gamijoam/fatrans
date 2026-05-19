'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Cookie, X } from 'lucide-react';
import {
  leerCookieConsent,
  aceptarTodas,
  rechazarOpcionales,
  guardarCookieConsent,
} from '@/lib/utils/cookie-consent-storage';
import { useAuthStore } from '@/stores/auth-store';

/**
 * Banner de consentimiento de cookies (issue #218 PR-A).
 *
 * Aparece solo si no hay preferencia guardada. Tres acciones:
 * - Aceptar todas (incluye analíticas/marketing si en el futuro las hay)
 * - Solo necesarias (rechaza opcionales)
 * - Personalizar (panel expandido con toggles granulares)
 *
 * Hoy Fatrans solo carga cookies necesarias (`/cookies`). Cuando se
 * agreguen analytics o marketing, esos toggles ya estarán en su lugar.
 *
 * Patrón LOPDP/GDPR-like: el banner debe ser claro, ofrecer la opción
 * de rechazo con la misma facilidad que la aceptación, y enlazar a la
 * política de cookies completa.
 */
export function CookieConsentBanner() {
  // null = aún no decidió React si mostrar (evita flicker en hidratación)
  // false = no mostrar (ya hay consentimiento guardado)
  // true = mostrar el banner
  const [visible, setVisible] = useState<boolean | null>(null);
  const [expandido, setExpandido] = useState(false);
  // Estado de los toggles del panel personalizado
  const [preferencias, setPreferencias] = useState(true);
  const [analiticas, setAnaliticas] = useState(false);
  const [marketing, setMarketing] = useState(false);

  /**
   * Si el socio tiene `debeCambiarPassword=true`, hay un modal obligatorio
   * de cambio de contraseña abierto sobre la pantalla. Bug reportado en PROD
   * (19-may-2026): el banner (`z-[100]`) quedaba encima del modal Radix
   * (`z-50`), capturando los clicks que deberían ir al modal — el socio no
   * podía ni cerrar el banner (no veía sus botones) ni completar el form
   * (los inputs no recibían focus). Solución: posponer el banner hasta que
   * el socio termine de cambiar la clave. Las cookies ya fueron aceptadas
   * implícitamente en el formulario de registro (LOPDP), así que el banner
   * es informativo y puede esperar.
   */
  const debeCambiarPassword = useAuthStore(
    (state) => state.user?.debeCambiarPassword ?? false,
  );

  useEffect(() => {
    // Solo en cliente — leerCookieConsent retorna null en SSR
    const consent = leerCookieConsent();
    setVisible(consent === null);
  }, []);

  if (visible !== true) return null;
  if (debeCambiarPassword) return null;

  const handleAceptarTodas = () => {
    aceptarTodas();
    setVisible(false);
  };

  const handleRechazar = () => {
    rechazarOpcionales();
    setVisible(false);
  };

  const handleGuardarPersonalizadas = () => {
    guardarCookieConsent({ preferencias, analiticas, marketing });
    setVisible(false);
  };

  return (
    <div
      role="dialog"
      aria-labelledby="cookie-banner-title"
      aria-describedby="cookie-banner-desc"
      // z-40 < z-50 de Radix Dialog para que cualquier modal crítico
      // (cambio de password obligatorio, etc.) quede siempre por encima
      // del banner aunque la guarda de `debeCambiarPassword` arriba falle.
      className="fixed inset-x-0 bottom-0 z-40 border-t border-slate-200 bg-white shadow-2xl"
    >
      <div className="mx-auto max-w-6xl px-4 py-4 lg:py-5">
        {!expandido ? (
          // === Vista compacta ===
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div className="flex items-start gap-3">
              <Cookie className="h-6 w-6 flex-shrink-0 text-[#16A34A] mt-0.5" aria-hidden="true" />
              <div>
                <h2 id="cookie-banner-title" className="font-semibold text-[#0F2744]">
                  Esta plataforma usa cookies
                </h2>
                <p id="cookie-banner-desc" className="mt-1 text-sm text-slate-600 leading-relaxed">
                  Las cookies <strong>estrictamente necesarias</strong> son requeridas para iniciar
                  sesión y operar de forma segura. Las opcionales nos ayudan a mejorar tu
                  experiencia. Puedes consultar el detalle en nuestra{' '}
                  <Link href="/cookies" className="text-[#16A34A] underline hover:no-underline">
                    Política de Cookies
                  </Link>
                  .
                </p>
              </div>
            </div>
            <div className="flex flex-col sm:flex-row gap-2 md:flex-shrink-0">
              <button
                type="button"
                onClick={() => setExpandido(true)}
                className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
              >
                Personalizar
              </button>
              <button
                type="button"
                onClick={handleRechazar}
                className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
              >
                Solo necesarias
              </button>
              <button
                type="button"
                onClick={handleAceptarTodas}
                className="rounded-md bg-[#16A34A] px-4 py-2 text-sm font-semibold text-white hover:bg-green-700 transition-colors"
              >
                Aceptar todas
              </button>
            </div>
          </div>
        ) : (
          // === Vista expandida (personalización granular) ===
          <div>
            <div className="flex items-start justify-between gap-3 mb-4">
              <div className="flex items-start gap-3">
                <Cookie className="h-6 w-6 flex-shrink-0 text-[#16A34A] mt-0.5" aria-hidden="true" />
                <div>
                  <h2 className="font-semibold text-[#0F2744]">Preferencias de cookies</h2>
                  <p className="mt-1 text-sm text-slate-600">
                    Elige qué categorías aceptas. Más detalle en la{' '}
                    <Link href="/cookies" className="text-[#16A34A] underline hover:no-underline">
                      Política de Cookies
                    </Link>
                    .
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setExpandido(false)}
                aria-label="Cerrar panel de personalización"
                className="rounded p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700 transition-colors"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-3 mb-4">
              <CategoriaRow
                titulo="Necesarias"
                descripcion="Sesión, autenticación y protección CSRF. Imprescindibles para usar la plataforma."
                checked
                disabled
                onChange={() => {
                  /* no-op — las necesarias no son opt-in */
                }}
              />
              <CategoriaRow
                titulo="Preferencias"
                descripcion="Recordar elecciones de interfaz (idioma, tema visual)."
                checked={preferencias}
                onChange={setPreferencias}
              />
              <CategoriaRow
                titulo="Analíticas"
                descripcion="Uso anónimo y agregado de la plataforma. Actualmente Fatrans no carga analytics — el toggle queda listo para el futuro."
                checked={analiticas}
                onChange={setAnaliticas}
              />
              <CategoriaRow
                titulo="Marketing"
                descripcion="Personalización publicitaria. Actualmente no se utilizan."
                checked={marketing}
                onChange={setMarketing}
              />
            </div>

            <div className="flex flex-col sm:flex-row gap-2 sm:justify-end">
              <button
                type="button"
                onClick={handleRechazar}
                className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
              >
                Solo necesarias
              </button>
              <button
                type="button"
                onClick={handleGuardarPersonalizadas}
                className="rounded-md bg-[#16A34A] px-4 py-2 text-sm font-semibold text-white hover:bg-green-700 transition-colors"
              >
                Guardar selección
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

interface CategoriaRowProps {
  titulo: string;
  descripcion: string;
  checked: boolean;
  disabled?: boolean;
  onChange: (v: boolean) => void;
}

function CategoriaRow({ titulo, descripcion, checked, disabled, onChange }: CategoriaRowProps) {
  return (
    <label
      className={`flex items-start gap-3 rounded-lg border p-3 ${
        disabled ? 'bg-slate-50 border-slate-200' : 'border-slate-200 hover:bg-slate-50 cursor-pointer'
      }`}
    >
      <input
        type="checkbox"
        checked={checked}
        disabled={disabled}
        onChange={(e) => onChange(e.target.checked)}
        aria-label={`Aceptar cookies ${titulo}`}
        className="mt-1 h-4 w-4 rounded border-slate-300 text-[#16A34A] focus:ring-[#16A34A]"
      />
      <div className="flex-1">
        <p className="font-medium text-sm text-[#0F2744]">
          {titulo}
          {disabled && <span className="ml-2 text-xs text-slate-500 font-normal">(siempre activas)</span>}
        </p>
        <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">{descripcion}</p>
      </div>
    </label>
  );
}
