'use client';

import { useState } from 'react';
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
import { Loader2, Camera, ShieldCheck, AlertCircle, ExternalLink } from 'lucide-react';
import { toast } from 'sonner';

/**
 * Captura biométrica del KYC — proxy hacia el widget de Didit (passive liveness +
 * face match + ID document OCR).
 *
 * Flujo:
 *  1. Usuario acepta consentimiento LOPDP biométrico (checkbox separado del KYC general).
 *  2. Hacemos POST a `/api/kyc/biometric/consentimiento`.
 *  3. Hacemos POST a `/api/kyc/biometric/iniciar` → recibimos `widgetUrl`.
 *  4. Abrimos el widget de Didit en una nueva pestaña (más seguro que iframe; Didit
 *     usa permisos de cámara que algunos navegadores bloquean en iframes cross-origin).
 *  5. El widget redirige al usuario de vuelta a la app; el backend recibe el resultado
 *     por webhook y actualiza el estado. La UI sondea (o se refresca al volver) y
 *     muestra "Verificación en revisión".
 */
export function BiometricCapture({
  onCompleted,
}: {
  onCompleted?: () => void;
}) {
  const [step, setStep] = useState<'consent' | 'ready' | 'in_progress' | 'submitted'>('consent');
  const [aceptado, setAceptado] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleAceptarConsentimiento = async () => {
    if (!aceptado) {
      toast.error('Debes aceptar la política biométrica');
      return;
    }
    setIsLoading(true);
    try {
      const res = await fetch('/api/kyc/biometric/consentimiento', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ aceptado: true, versionPolitica: '1.0' }),
      });
      if (!res.ok) {
        const e = await res.json().catch(() => ({}));
        throw new Error(e.message || 'Error registrando consentimiento');
      }
      setStep('ready');
      toast.success('Consentimiento registrado');
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleIniciar = async () => {
    setIsLoading(true);
    try {
      const res = await fetch('/api/kyc/biometric/iniciar', {
        method: 'POST',
        credentials: 'include',
      });
      if (!res.ok) {
        const e = await res.json().catch(() => ({}));
        throw new Error(e.message || 'Error iniciando verificación');
      }
      const data = await res.json();
      if (!data?.widgetUrl) {
        throw new Error('El proveedor no devolvió URL del widget');
      }
      // Abrir Didit en pestaña nueva — necesita permisos de cámara propios.
      window.open(data.widgetUrl, '_blank', 'noopener,noreferrer');
      setStep('in_progress');
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleMarcarTerminado = () => {
    setStep('submitted');
    onCompleted?.();
    toast.info('La verificación está siendo procesada por el proveedor');
  };

  return (
    <Card className="w-full max-w-xl mx-auto">
      <CardHeader>
        <div className="flex items-center gap-2">
          <Camera className="h-5 w-5 text-blue-600" />
          <CardTitle>Verificación biométrica</CardTitle>
        </div>
        <CardDescription>
          Necesitamos confirmar que eres tú con un selfie + tu cédula. La captura
          se procesa en Didit (proveedor externo) bajo encriptación TLS.
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-4">
        {step === 'consent' && (
          <>
            <Alert>
              <ShieldCheck className="h-4 w-4" />
              <AlertTitle>Consentimiento LOPDP biométrico</AlertTitle>
              <AlertDescription className="space-y-2 text-sm">
                <p>
                  Tu imagen facial es un <strong>dato biométrico sensible</strong>. Será
                  procesada por <strong>Didit</strong> (proveedor con sede en España) bajo
                  cifrado, solo para comparar tu selfie con tu cédula y detectar suplantación.
                </p>
                <p>
                  Las imágenes se eliminan a los <strong>90 días</strong>. Puedes revocar este
                  consentimiento en cualquier momento desde tu perfil — al revocar, también
                  se solicitará al proveedor que elimine tus datos.
                </p>
              </AlertDescription>
            </Alert>

            <div className="flex items-start gap-3 p-3 border rounded-lg">
              <Checkbox
                id="biometric-consent"
                checked={aceptado}
                onCheckedChange={(c) => setAceptado(c === true)}
                disabled={isLoading}
              />
              <Label htmlFor="biometric-consent" className="text-sm leading-snug cursor-pointer">
                Acepto que mi imagen facial sea procesada por Didit (España) para verificar
                mi identidad bajo la política biométrica de Fatrans. Entiendo que es un
                consentimiento separado del KYC documental y que puedo revocarlo.
              </Label>
            </div>

            <Button
              onClick={handleAceptarConsentimiento}
              disabled={!aceptado || isLoading}
              className="w-full bg-green-600 hover:bg-green-700"
            >
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
              Aceptar y continuar
            </Button>
          </>
        )}

        {step === 'ready' && (
          <>
            <Alert>
              <Camera className="h-4 w-4" />
              <AlertTitle>Lo que vas a hacer</AlertTitle>
              <AlertDescription className="text-sm">
                Vamos a abrir el widget de Didit en una pestaña nueva. Te pedirá permiso
                para usar la cámara, capturar tu cédula y un selfie corto. Toma menos
                de 1 minuto.
              </AlertDescription>
            </Alert>
            <Button
              onClick={handleIniciar}
              disabled={isLoading}
              className="w-full bg-blue-600 hover:bg-blue-700"
            >
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <ExternalLink className="mr-2 h-4 w-4" />}
              Abrir verificación
            </Button>
          </>
        )}

        {step === 'in_progress' && (
          <>
            <Alert>
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>Estamos esperando el resultado</AlertTitle>
              <AlertDescription className="text-sm">
                Completa el flujo en la pestaña que abrimos. Cuando termines, vuelve aquí
                y presiona <em>"Ya terminé"</em>. El proveedor nos enviará el resultado
                automáticamente en cuanto procese tu captura.
              </AlertDescription>
            </Alert>
            <Button onClick={handleMarcarTerminado} variant="outline" className="w-full">
              Ya terminé la verificación
            </Button>
          </>
        )}

        {step === 'submitted' && (
          <Alert className="border-green-200 bg-green-50">
            <ShieldCheck className="h-4 w-4 text-green-600" />
            <AlertTitle className="text-green-900">Verificación enviada</AlertTitle>
            <AlertDescription className="text-green-800 text-sm">
              Tu captura está siendo procesada. Recibirás el resultado en unos minutos.
              Puedes cerrar esta página — la decisión llegará por correo.
            </AlertDescription>
          </Alert>
        )}
      </CardContent>
    </Card>
  );
}
