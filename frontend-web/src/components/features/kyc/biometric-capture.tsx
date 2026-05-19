'use client';

import { useEffect, useRef, useState } from 'react';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import {
  Loader2,
  Camera,
  ShieldCheck,
  CheckCircle2,
  ExternalLink,
  XCircle,
  RotateCw,
} from 'lucide-react';
import { toast } from 'sonner';

/** Estado biométrico del backend — coincide con el enum `EstadoBiometria` Java. */
type EstadoBiometricoBackend =
  | 'NO_INICIADA'
  | 'EN_PROGRESO'
  | 'APROBADA'
  | 'RECHAZADA'
  | 'EXPIRADA';

/**
 * Steps internos de UI. Antes había 6 (consent/ready/in_progress/submitted/
 * aprobado/rechazado) que confundían al socio — el rediseño los colapsa a 4
 * con flujo lineal: pendiente → en_progreso → (aprobada | rechazada).
 */
type Step = 'pendiente' | 'en_progreso' | 'aprobada' | 'rechazada';

function deriveStepFromBackend(
  estado: EstadoBiometricoBackend | null | undefined
): Step {
  switch (estado) {
    case 'APROBADA':
      return 'aprobada';
    case 'RECHAZADA':
    case 'EXPIRADA':
      return 'rechazada';
    case 'EN_PROGRESO':
      return 'en_progreso';
    case 'NO_INICIADA':
    case null:
    case undefined:
    default:
      return 'pendiente';
  }
}

/**
 * Captura biométrica del KYC — wrapper alrededor del widget de Didit
 * (passive liveness + face match + OCR de cédula).
 *
 * Rediseño (19-may-2026) — motivado por feedback de PROD: el socio veía
 * dos pasos (consent → ready → iniciar) y se confundía. Además, después de
 * pasar la verificación, el componente seguía pidiendo aceptar términos
 * porque el `useEffect` solo sincronizaba para APROBADA/RECHAZADA y se
 * comía la transición de `NO_INICIADA → EN_PROGRESO`.
 *
 * Cambios:
 *  1. Un solo botón "Iniciar verificación facial" — registra consent +
 *     crea sesión Didit + abre pestaña en una sola acción.
 *  2. `useEffect` sincroniza el step con TODOS los estados del backend
 *     (no solo terminales). El step deja de quedarse "atrás" tras un
 *     refetch.
 *  3. Auto-polling cada 5s cuando estamos en `en_progreso` — pide al
 *     padre que refetch /api/kyc/estado. Cuando llega el webhook de Didit,
 *     la UI pasa a `aprobada` sin que el socio haga nada.
 *  4. Mensaje terminal "aprobada" indica el siguiente paso explícitamente
 *     ("subí el comprobante de domicilio abajo") en vez de solo confirmar.
 *  5. "Rechazada" muestra checklist de qué revisar al reintentar.
 */
export function BiometricCapture({
  onCompleted,
  estadoBackend,
}: {
  /** Callback para que el padre re-fetchee el estado KYC. */
  onCompleted?: () => void;
  /** Estado biométrico actual según el backend (viene de /api/kyc/estado). */
  estadoBackend?: EstadoBiometricoBackend | null;
}) {
  const [step, setStep] = useState<Step>(() =>
    deriveStepFromBackend(estadoBackend)
  );
  const [aceptado, setAceptado] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  /** Cacheamos el URL del widget para que el socio pueda re-abrir la
      pestaña si la cerró sin completar (sin tener que crear sesión nueva
      en Didit). Se pierde si el componente se desmonta — en ese caso
      `handleReabrirWidget` inicia una sesión nueva. */
  const [widgetUrl, setWidgetUrl] = useState<string | null>(null);
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // Sincroniza step con TODOS los cambios de estadoBackend. Fix histórico:
  // antes este efecto solo manejaba APROBADA/RECHAZADA y dejaba al step
  // estancado en estados intermedios después de un refetch.
  useEffect(() => {
    setStep(deriveStepFromBackend(estadoBackend));
  }, [estadoBackend]);

  // Polling activo mientras estamos esperando el resultado de Didit.
  // En lugar de solo refetch /api/kyc/estado (que lee BD pasiva), llamamos
  // a /api/kyc/biometric/refresh que FUERZA al backend a consultar Didit
  // por pull. Eso suple al webhook cuando no está configurado o tarda —
  // sin esto, los socios quedaban atascados en EN_PROGRESO eternamente
  // y había que hacer UPDATE manual a la BD (caso real reportado en PROD
  // con carlos, alexander, gabriel).
  //
  // Cada 5s: POST /refresh → backend pulla Didit → si Approved/Rejected,
  // actualiza BD localmente + devuelve el nuevo estado → el padre hace
  // refetch para que el useEffect de arriba transicione el step.
  useEffect(() => {
    if (step !== 'en_progreso' || !onCompleted) return;
    const tick = async () => {
      try {
        await fetch('/api/kyc/biometric/refresh', {
          method: 'POST',
          credentials: 'include',
        });
      } catch {
        // Silencioso: el polling es best-effort. Si el endpoint falla,
        // el próximo tick lo reintenta.
      }
      // Independientemente del resultado del refresh, pedimos al padre que
      // refetch /api/kyc/estado para reflejar el cache local actualizado.
      onCompleted();
    };
    // Primer tick inmediato — útil cuando el socio vuelve a la pantalla
    // después de completar Didit en otra pestaña. Sin esto, esperaría
    // 5s para ver el cambio.
    tick();
    pollingRef.current = setInterval(tick, 5000);
    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    };
  }, [step, onCompleted]);

  /**
   * Inicia verificación: registra consent + crea sesión Didit + abre pestaña.
   * Todo en una acción para evitar el step intermedio donde el socio se perdía.
   */
  const handleIniciar = async () => {
    if (!aceptado) {
      toast.error('Aceptá el consentimiento para continuar');
      return;
    }
    setIsLoading(true);
    try {
      // 1) Consentimiento. El backend acepta repetir (idempotente vía upsert
      //    en biometric_consent), así que no hace falta saltarlo si ya existe.
      const consentRes = await fetch('/api/kyc/biometric/consentimiento', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ aceptado: true, versionPolitica: '1.0' }),
      });
      if (!consentRes.ok && consentRes.status !== 409) {
        const e = await consentRes.json().catch(() => ({}));
        throw new Error(e.message || 'No pudimos registrar el consentimiento');
      }

      // 2) Crear sesión Didit y abrir el widget en pestaña nueva.
      const iniciarRes = await fetch('/api/kyc/biometric/iniciar', {
        method: 'POST',
        credentials: 'include',
      });
      if (!iniciarRes.ok) {
        const e = await iniciarRes.json().catch(() => ({}));
        throw new Error(
          e.message || 'No pudimos iniciar la verificación, intentá de nuevo'
        );
      }
      const data = await iniciarRes.json();
      if (!data?.widgetUrl) {
        throw new Error('El proveedor no devolvió el enlace de verificación');
      }

      setWidgetUrl(data.widgetUrl);
      window.open(data.widgetUrl, '_blank', 'noopener,noreferrer');
      setStep('en_progreso');
      // Refetch del padre para persistir EN_PROGRESO en estadoBiometria
      // — si el socio refresca, no vuelve a pedirle consent.
      onCompleted?.();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Error inesperado');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Re-abrir la pestaña de Didit. Si tenemos URL en memoria la usamos;
   * si no (post-refresh), arrancamos una sesión nueva. Didit detecta
   * sesiones existentes por vendor_data y devuelve la cacheada cuando
   * aplica, así que no creamos duplicados.
   */
  const handleReabrirWidget = async () => {
    if (widgetUrl) {
      window.open(widgetUrl, '_blank', 'noopener,noreferrer');
      return;
    }
    setAceptado(true); // ya aceptó antes, no le pedimos de nuevo
    await handleIniciar();
  };

  /** Tras un rechazo, vuelve al estado inicial para que el socio reintente. */
  const handleReintentar = () => {
    setStep('pendiente');
    setAceptado(false);
    setWidgetUrl(null);
  };

  return (
    <Card className="w-full max-w-xl mx-auto">
      <CardHeader>
        <div className="flex items-center gap-2">
          <Camera className="h-5 w-5 text-blue-600" />
          <CardTitle>Verificación facial</CardTitle>
        </div>
        <CardDescription>
          Confirmá tu identidad con un selfie y tu cédula. Toma menos de 1 minuto.
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* ============================================================
             PASO 1 — Pendiente: acepta consent + arranca todo en una acción.
            ============================================================ */}
        {step === 'pendiente' && (
          <>
            <Alert>
              <ShieldCheck className="h-4 w-4" />
              <AlertTitle>Cómo funciona</AlertTitle>
              <AlertDescription className="text-sm space-y-2">
                <p>
                  <strong>1.</strong> Abrimos el widget de <strong>Didit</strong> (proveedor externo) en una pestaña nueva.
                </p>
                <p>
                  <strong>2.</strong> Te pide permiso para usar la cámara, sacar foto a tu cédula y un selfie corto.
                </p>
                <p>
                  <strong>3.</strong> Cuando termines, volvé acá. Te avisamos automáticamente cuando esté listo.
                </p>
              </AlertDescription>
            </Alert>

            <div className="flex items-start gap-3 p-3 border rounded-lg bg-slate-50">
              <Checkbox
                id="biometric-consent"
                checked={aceptado}
                onCheckedChange={(c) => setAceptado(c === true)}
                disabled={isLoading}
                className="mt-0.5"
              />
              <Label
                htmlFor="biometric-consent"
                className="text-xs leading-snug cursor-pointer text-slate-700"
              >
                Acepto que mi imagen facial se procese en Didit (España) para verificar mi identidad
                bajo la política LOPDP. Las imágenes se eliminan a los 90 días. Puedo revocar este
                consentimiento desde mi perfil.
              </Label>
            </div>

            <Button
              onClick={handleIniciar}
              disabled={!aceptado || isLoading}
              className="w-full h-12 text-base bg-blue-600 hover:bg-blue-700"
            >
              {isLoading ? (
                <Loader2 className="mr-2 h-5 w-5 animate-spin" />
              ) : (
                <Camera className="mr-2 h-5 w-5" />
              )}
              Iniciar verificación facial
            </Button>
          </>
        )}

        {/* ============================================================
             PASO 2 — En progreso: pestaña abierta, polling activo.
            ============================================================ */}
        {step === 'en_progreso' && (
          <>
            <div className="flex flex-col items-center gap-3 py-6">
              <Loader2 className="h-12 w-12 animate-spin text-blue-600" />
              <h3 className="font-semibold text-base text-center">
                Esperando el resultado
              </h3>
              <p className="text-sm text-center text-slate-600 max-w-sm">
                Completá la captura en la pestaña que se abrió. Cuando termines, esperá unos
                segundos.
              </p>
              <p className="text-xs text-slate-500 text-center">
                Verificamos cada 5 segundos — el resultado aparece solo.
              </p>
            </div>

            <div className="flex flex-col gap-2">
              <Button
                onClick={handleReabrirWidget}
                variant="outline"
                disabled={isLoading}
                className="w-full"
              >
                {isLoading ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <ExternalLink className="mr-2 h-4 w-4" />
                )}
                Volver a abrir la pestaña de verificación
              </Button>
              <Button
                onClick={() => onCompleted?.()}
                variant="ghost"
                size="sm"
                className="text-xs"
              >
                <RotateCw className="mr-2 h-3 w-3" />
                Actualizar manualmente
              </Button>
            </div>
          </>
        )}

        {/* ============================================================
             PASO 3a — Aprobada: terminal verde con next step explícito.
            ============================================================ */}
        {step === 'aprobada' && (
          <Alert className="border-green-300 bg-green-50">
            <CheckCircle2 className="h-5 w-5 text-green-600" />
            <AlertTitle className="text-green-900 font-semibold">
              Identidad verificada ✓
            </AlertTitle>
            <AlertDescription className="text-green-800 text-sm space-y-2">
              <p>Tu verificación facial fue aprobada. Ya tenemos tu cédula y tu selfie.</p>
              <p className="font-medium">
                👇 Ahora subí el comprobante de domicilio abajo para terminar.
              </p>
            </AlertDescription>
          </Alert>
        )}

        {/* ============================================================
             PASO 3b — Rechazada / Expirada: terminal rojo con tips.
            ============================================================ */}
        {step === 'rechazada' && (
          <>
            <Alert className="border-red-300 bg-red-50">
              <XCircle className="h-5 w-5 text-red-600" />
              <AlertTitle className="text-red-900 font-semibold">
                La verificación no pasó
              </AlertTitle>
              <AlertDescription className="text-red-800 text-sm space-y-2">
                <p>No pudimos confirmar tu identidad. Probá de nuevo asegurándote de:</p>
                <ul className="list-disc list-inside space-y-1 ml-1">
                  <li>Estar en un lugar con buena luz</li>
                  <li>Mostrar la cédula completa y sin reflejos</li>
                  <li>Hacer el selfie con la cara totalmente visible</li>
                </ul>
              </AlertDescription>
            </Alert>
            <Button
              onClick={handleReintentar}
              className="w-full bg-blue-600 hover:bg-blue-700"
            >
              <Camera className="mr-2 h-4 w-4" />
              Reintentar verificación
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  );
}
