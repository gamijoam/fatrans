'use client';

import { Logo } from './logo';

/**
 * Logo animado para estados de carga (login, splash, etc.).
 *
 * Animación: triple capa para que el logo "respire" y se sienta vivo
 * sin marear:
 *   1. Pulso de glow alrededor (anillo verde marca Fatrans #16A34A)
 *      con `animate-ping` de Tailwind.
 *   2. Pulso suave del logo (`animate-pulse-subtle` custom).
 *   3. Rotación lenta del anillo exterior (`animate-spin-slow` custom)
 *      para sugerir progreso sin ser molesto.
 *
 * Los keyframes nuevos viven en `globals.css` (definidos al final del
 * archivo). NO requiere instalar Framer Motion ni Lottie — CSS puro.
 *
 * Variantes:
 *   - `inline`: tamaño mediano, contenido inline (en cards, dialogs)
 *   - `overlay`: pantalla completa con fondo difuminado (al hacer login)
 */

export interface LoadingLogoProps {
  /** Tamaño del logo en px. Default 120. */
  size?: number;
  /** Mensaje opcional debajo del logo. */
  mensaje?: string;
  /** Variante visual. */
  variante?: 'inline' | 'overlay';
}

export function LoadingLogo({
  size = 120,
  mensaje = 'Iniciando sesión...',
  variante = 'inline',
}: LoadingLogoProps) {
  const contenido = (
    <div className="flex flex-col items-center gap-4">
      <div className="relative" style={{ width: size + 32, height: size + 32 }}>
        {/* Anillo exterior con rotación lenta y glow */}
        <div
          className="absolute inset-0 rounded-full border-2 border-[#16A34A]/30 animate-spin-slow"
          aria-hidden="true"
        />
        {/* Pulso de glow */}
        <div
          className="absolute inset-2 rounded-full bg-[#16A34A]/20 animate-ping"
          style={{ animationDuration: '2s' }}
          aria-hidden="true"
        />
        {/* Logo (con su propio pulso sutil) */}
        <div className="absolute inset-4 flex items-center justify-center animate-pulse-subtle">
          <Logo size={size} soloImagen priority />
        </div>
      </div>
      {mensaje && (
        <p className="text-sm text-slate-600 font-medium" role="status" aria-live="polite">
          {mensaje}
        </p>
      )}
    </div>
  );

  if (variante === 'overlay') {
    return (
      <div
        role="status"
        aria-label={mensaje}
        className="fixed inset-0 z-[200] flex items-center justify-center bg-white/80 backdrop-blur-sm"
      >
        {contenido}
      </div>
    );
  }

  return contenido;
}
