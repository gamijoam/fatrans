'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ProgressBar } from '@/components/ui/progress';
import {
  Shield, Upload, FileText, CheckCircle, XCircle, Clock, AlertTriangle,
  Loader2, Calendar, Send, Camera, ChevronRight,
} from 'lucide-react';
import { toast } from 'sonner';
import { BiometricCapture } from '@/components/features/kyc/biometric-capture';

/**
 * Rediseño KYC (19-may-2026)
 * ============================
 *
 * Problema reportado en QA y PROD: socios completan verificación facial pero
 * la UI no se entera (queda pidiendo consent o "volver a verificar") y los
 * botones de comprobante quedan bloqueados sin explicación visible. Caso real:
 * Carlos en PROD subió selfie, vio "APROBADA" en BD vía SQL manual, pero el
 * frontend nunca actualizó porque dependía de webhook que no llegaba.
 *
 * Esta página ahora muestra:
 *  1. Un stepper visual arriba ("Paso 1 → Paso 2 → Listo") que el socio
 *     entiende sin leer texto. El paso activo destaca; los completados se
 *     marcan en verde con un check.
 *  2. Solo el paso activo se renderiza expandido (la verificación facial o
 *     el comprobante de domicilio). Antes ambos se mostraban siempre y el
 *     socio se perdía entre dos cards similares.
 *  3. Mensajes de estado en lenguaje plano, sin jerga técnica (KYC, OCR,
 *     liveness, etc).
 *  4. Cuando la biometría aprueba, la transición a "Paso 2" es automática
 *     gracias al polling de pull-sync del componente BiometricCapture
 *     (ver biometric-capture.tsx).
 *
 * El backend hace pull-sync a Didit cada 5s vía /api/kyc/biometric/refresh —
 * si por alguna razón el webhook no llegó (no configurado en dashboard de
 * Didit, network, etc), la UI sigue actualizándose sin intervención manual.
 */

interface DocumentoEstado {
  id: string;
  tipo: string;
  descripcion: string;
  estado: string;
  nombreOriginal: string;
  fechaSubida: string;
  motivoRechazo: string | null;
}

interface EstadoKYC {
  verificacionId: string;
  socioId: string;
  nivel: string;
  estado: string;
  descripcionEstado: string;
  fechaInicio: string;
  fechaExpiracion: string | null;
  diasRestantes: number;
  documentosRequeridos: number;
  documentosValidos: number;
  documentos: DocumentoEstado[];
  comentarioRevision: string | null;
  motivoRechazo: string | null;
  estadoBiometria: 'NO_INICIADA' | 'EN_PROGRESO' | 'APROBADA' | 'RECHAZADA' | 'EXPIRADA' | null;
}

/** Único documento manual: comprobante de domicilio (Didit no captura facturas). */
const TIPOS_DOCUMENTO = [
  { tipo: 'COMPROBANTE_DOMICILIO', label: 'Comprobante de Domicilio' },
];

/** Stepper visual con 3 pasos: verificación facial, comprobante, revisión. */
type StepKey = 'biometria' | 'comprobante' | 'revision';
type StepEstado = 'completado' | 'activo' | 'pendiente';

interface StepDef {
  key: StepKey;
  numero: number;
  titulo: string;
  descripcionCorta: string;
  icon: typeof Camera;
}

const STEPS: StepDef[] = [
  {
    key: 'biometria',
    numero: 1,
    titulo: 'Verificación facial',
    descripcionCorta: 'Selfie + foto de tu cédula',
    icon: Camera,
  },
  {
    key: 'comprobante',
    numero: 2,
    titulo: 'Comprobante de domicilio',
    descripcionCorta: 'Factura de servicios o contrato',
    icon: FileText,
  },
  {
    key: 'revision',
    numero: 3,
    titulo: 'Revisión final',
    descripcionCorta: 'Te avisamos por correo',
    icon: Shield,
  },
];

export default function DashboardKYCPagina() {
  const [estadoKYC, setEstadoKYC] = useState<EstadoKYC | null>(null);
  const [loading, setLoading] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [subiendoDocumento, setSubiendoDocumento] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [targetTipo, setTargetTipo] = useState<string | null>(null);

  const cargarEstado = useCallback(async () => {
    try {
      // `cache: 'no-store'` + cache-buster — sin esto, el navegador cachea la
      // primera respuesta (estadoBiometria=NO_INICIADA) y el polling del
      // BiometricCapture nunca observa la transición a APROBADA aunque el
      // backend la haya persistido. Bug reportado por Gabriel en QA el
      // 19-may-2026 (mismo patrón que el bug de /me en el modal de password).
      const res = await fetch(`/api/kyc/estado?t=${Date.now()}`, {
        credentials: 'include',
        cache: 'no-store',
        headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
      });
      if (res.ok) {
        const data = await res.json();
        setEstadoKYC(data);
      } else if (res.status === 404) {
        setEstadoKYC(null);
      } else {
        toast.error('No pudimos cargar el estado de tu verificación');
      }
    } catch {
      toast.error('No pudimos cargar el estado de tu verificación');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarEstado();
  }, [cargarEstado]);

  const handleEnviarRevision = async () => {
    if (!estadoKYC?.verificacionId) {
      toast.error('No hay verificación activa');
      return;
    }
    setEnviando(true);
    try {
      const res = await fetch('/api/kyc/enviar', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ verificacionId: estadoKYC.verificacionId }),
      });

      if (res.ok) {
        toast.success('Enviado a revisión', {
          description: 'Te avisaremos por correo cuando el equipo termine.',
        });
        cargarEstado();
      } else {
        const error = await res.json();
        toast.error(error.message || 'No pudimos enviar a revisión');
      }
    } catch {
      toast.error('No pudimos enviar a revisión');
    } finally {
      setEnviando(false);
    }
  };

  const handleSubirDocumento = async (tipo: string, file: File) => {
    if (!file) return;

    const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'application/pdf'];
    if (!validTypes.includes(file.type)) {
      toast.error('Formato no válido', { description: 'Usá JPG, PNG, WEBP o PDF.' });
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      toast.error('Archivo demasiado grande', { description: 'Máximo 10 MB.' });
      return;
    }

    if (!estadoKYC?.verificacionId) {
      toast.error('No hay verificación activa');
      return;
    }

    setSubiendoDocumento(tipo);
    try {
      const formData = new FormData();
      formData.append('verificacionId', estadoKYC.verificacionId);
      formData.append('tipoDocumento', tipo);
      formData.append('archivo', file);

      const res = await fetch('/api/kyc/documentos', {
        method: 'POST',
        credentials: 'include',
        body: formData,
      });

      if (res.ok) {
        toast.success('Comprobante subido', {
          description: 'Ya podés enviar tu verificación a revisión.',
        });
        cargarEstado();
      } else {
        const error = await res.json();
        toast.error(error.message || 'No pudimos subir el comprobante');
      }
    } catch {
      toast.error('No pudimos subir el comprobante');
    } finally {
      setSubiendoDocumento(null);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="h-10 w-10 animate-spin text-green-600" />
      </div>
    );
  }

  if (!estadoKYC) {
    return (
      <div className="p-4 lg:p-6 max-w-3xl mx-auto">
        <Card>
          <CardContent className="py-16">
            <div className="text-center space-y-3">
              <div className="inline-flex p-4 rounded-full bg-gray-100">
                <Shield className="h-10 w-10 text-gray-400" />
              </div>
              <h2 className="text-xl font-bold text-gray-900">
                Aún no tenés una verificación activa
              </h2>
              <p className="text-sm text-gray-500 max-w-md mx-auto">
                El administrador todavía no aprobó tu solicitud de registro.
                Cuando lo haga, vas a poder iniciar tu verificación de identidad
                desde acá.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // -----------------------------------------------------------------
  // Cálculo del paso activo y estado por paso.
  // -----------------------------------------------------------------
  const biometriaAprobada = estadoKYC.estadoBiometria === 'APROBADA';
  const biometriaRechazada =
    estadoKYC.estadoBiometria === 'RECHAZADA' ||
    estadoKYC.estadoBiometria === 'EXPIRADA';

  const comprobanteDoc = estadoKYC.documentos.find(
    (d) => d.tipo === 'COMPROBANTE_DOMICILIO'
  );
  const comprobanteSubido = !!comprobanteDoc;
  const comprobanteValido = comprobanteDoc?.estado === 'VALIDADO';
  const comprobanteRechazado = comprobanteDoc?.estado === 'RECHAZADO';

  const enviadoARevision = estadoKYC.estado === 'EN_REVISION';
  const kycAprobado = estadoKYC.estado === 'APROBADO';
  const kycRechazado = estadoKYC.estado === 'RECHAZADO';

  /**
   * Determina el paso activo según el estado actual del KYC.
   *
   * Importante: si el comprobante está subido pero el KYC todavía está en
   * PENDIENTE (el socio no apretó "Enviar a revisión" aún), nos quedamos en
   * el paso "comprobante" para que el botón de envío sea visible. Antes el
   * fallthrough mandaba al paso "revisión" y el socio no encontraba cómo
   * mandar su verificación.
   */
  const pasoActivo: StepKey = (() => {
    if (kycAprobado) return 'revision';
    if (enviadoARevision) return 'revision';
    if (!biometriaAprobada) return 'biometria';
    if (!comprobanteSubido || comprobanteRechazado) return 'comprobante';
    if (estadoKYC.estado === 'PENDIENTE') return 'comprobante';
    return 'revision';
  })();

  const estadoDePaso = (key: StepKey): StepEstado => {
    if (key === 'biometria') {
      if (biometriaAprobada) return 'completado';
      return pasoActivo === 'biometria' ? 'activo' : 'pendiente';
    }
    if (key === 'comprobante') {
      if (comprobanteValido) return 'completado';
      if (!biometriaAprobada) return 'pendiente';
      return pasoActivo === 'comprobante' ? 'activo' : 'pendiente';
    }
    if (key === 'revision') {
      if (kycAprobado) return 'completado';
      return pasoActivo === 'revision' ? 'activo' : 'pendiente';
    }
    return 'pendiente';
  };

  // Porcentaje de progreso global — usado en la barra superior.
  const totalPasos = STEPS.length;
  const pasosCompletados = STEPS.filter(
    (s) => estadoDePaso(s.key) === 'completado'
  ).length;
  const porcentaje = (pasosCompletados / totalPasos) * 100;

  return (
    <div className="p-4 lg:p-6 max-w-3xl mx-auto space-y-6">
      {/* ============================================================
            HEADER — título + estado global + progreso
          ============================================================ */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">
          Verificación de identidad
        </h1>
        <p className="text-sm text-gray-500 mt-1">
          Confirmá quién sos para habilitar todos los servicios de Fatrans.
          {' '}
          Toma 2 minutos.
        </p>
      </div>

      {/* Alertas de estado global */}
      {kycAprobado && (
        <div className="flex items-start gap-3 p-4 rounded-lg border border-green-200 bg-green-50">
          <CheckCircle className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="font-semibold text-green-900">¡Listo! Tu identidad fue verificada</p>
            <p className="text-sm text-green-800 mt-1">
              Ya tenés acceso completo a todos los servicios.
              {estadoKYC.fechaExpiracion && (
                <>
                  {' '}Vigente hasta el{' '}
                  <span className="font-medium">
                    {new Date(estadoKYC.fechaExpiracion).toLocaleDateString('es-VE')}
                  </span>
                  {estadoKYC.diasRestantes > 0 && ` (${estadoKYC.diasRestantes} días)`}
                  .
                </>
              )}
            </p>
          </div>
        </div>
      )}

      {kycRechazado && estadoKYC.motivoRechazo && (
        <div className="flex items-start gap-3 p-4 rounded-lg border border-red-200 bg-red-50">
          <AlertTriangle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="font-semibold text-red-900">Necesitamos que corrijas algo</p>
            <p className="text-sm text-red-800 mt-1">{estadoKYC.motivoRechazo}</p>
          </div>
        </div>
      )}

      {enviadoARevision && !kycAprobado && !kycRechazado && (
        <div className="flex items-start gap-3 p-4 rounded-lg border border-blue-200 bg-blue-50">
          <Clock className="h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="font-semibold text-blue-900">En revisión</p>
            <p className="text-sm text-blue-800 mt-1">
              Nuestro equipo está revisando tu información. Te avisamos por
              correo cuando esté lista — generalmente en menos de 24 horas.
            </p>
          </div>
        </div>
      )}

      {/* ============================================================
            STEPPER VISUAL — 3 pasos con número, ícono y estado
          ============================================================ */}
      <Card>
        <CardContent className="p-4 lg:p-6">
          <div className="flex items-center gap-2 mb-3 text-xs text-gray-500 uppercase tracking-wide font-semibold">
            <span>Progreso</span>
            <span className="text-gray-300">·</span>
            <span>
              {pasosCompletados} de {totalPasos} pasos completados
            </span>
          </div>
          <ProgressBar value={porcentaje} className="h-2 mb-5" />

          <ol className="flex flex-col sm:flex-row sm:items-stretch gap-3 sm:gap-0">
            {STEPS.map((step, idx) => {
              const estado = estadoDePaso(step.key);
              return (
                <li key={step.key} className="flex sm:flex-1 items-center">
                  <button
                    type="button"
                    aria-current={estado === 'activo' ? 'step' : undefined}
                    className={`
                      flex-1 flex items-start gap-3 p-3 rounded-lg border-2 text-left transition-colors
                      ${estado === 'activo'
                        ? 'border-blue-400 bg-blue-50'
                        : estado === 'completado'
                        ? 'border-green-300 bg-green-50/60'
                        : 'border-gray-200 bg-white opacity-60'}
                    `}
                  >
                    <div
                      className={`
                        flex items-center justify-center h-9 w-9 rounded-full flex-shrink-0 font-bold text-sm
                        ${estado === 'completado'
                          ? 'bg-green-600 text-white'
                          : estado === 'activo'
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-200 text-gray-500'}
                      `}
                    >
                      {estado === 'completado' ? (
                        <CheckCircle className="h-5 w-5" />
                      ) : (
                        step.numero
                      )}
                    </div>
                    <div className="min-w-0">
                      <p
                        className={`text-sm font-semibold ${
                          estado === 'completado'
                            ? 'text-green-900'
                            : estado === 'activo'
                            ? 'text-blue-900'
                            : 'text-gray-700'
                        }`}
                      >
                        {step.titulo}
                      </p>
                      <p className="text-xs text-gray-500 mt-0.5 truncate">
                        {step.descripcionCorta}
                      </p>
                    </div>
                  </button>
                  {idx < STEPS.length - 1 && (
                    <ChevronRight className="hidden sm:block h-5 w-5 text-gray-300 mx-1 flex-shrink-0" />
                  )}
                </li>
              );
            })}
          </ol>
        </CardContent>
      </Card>

      {/* ============================================================
            CONTENIDO DEL PASO ACTIVO
          ============================================================ */}

      {/* --- PASO 1: BIOMETRÍA --- */}
      {pasoActivo === 'biometria' && (
        <>
          {biometriaRechazada && (
            <div className="flex items-start gap-3 p-4 rounded-lg border border-amber-200 bg-amber-50">
              <AlertTriangle className="h-5 w-5 text-amber-600 flex-shrink-0 mt-0.5" />
              <div className="flex-1">
                <p className="font-semibold text-amber-900">
                  La verificación anterior no pasó
                </p>
                <p className="text-sm text-amber-800 mt-1">
                  No te preocupes — intentalo de nuevo. Asegurate de estar en un
                  lugar con buena luz y que la cédula se vea completa.
                </p>
              </div>
            </div>
          )}

          <BiometricCapture
            onCompleted={cargarEstado}
            estadoBackend={estadoKYC.estadoBiometria}
          />

          <div className="text-xs text-gray-500 text-center px-4">
            Tus imágenes se procesan en{' '}
            <span className="font-medium text-gray-700">Didit</span> (proveedor
            europeo certificado) bajo política LOPDP. Se eliminan a los 90 días.
          </div>
        </>
      )}

      {/* --- PASO 2: COMPROBANTE --- */}
      {pasoActivo === 'comprobante' && (
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-green-600" />
              <CardTitle>Comprobante de domicilio</CardTitle>
            </div>
            <p className="text-sm text-gray-500 mt-1">
              Subí una factura de servicios (luz, agua, internet) o un contrato
              de alquiler con tu nombre y dirección. Debe ser de los{' '}
              <span className="font-medium">últimos 3 meses</span>.
            </p>
          </CardHeader>
          <CardContent>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp,application/pdf"
              className="hidden"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file && targetTipo) {
                  handleSubirDocumento(targetTipo, file);
                  e.target.value = '';
                }
              }}
            />

            {TIPOS_DOCUMENTO.map((doc) => {
              const uploaded = estadoKYC.documentos?.find((d) => d.tipo === doc.tipo);
              const rechazado = uploaded?.estado === 'RECHAZADO';
              const validado = uploaded?.estado === 'VALIDADO';
              const pendiente = uploaded && !validado && !rechazado;

              return (
                <div key={doc.tipo} className="space-y-3">
                  {uploaded && (
                    <div
                      className={`
                        flex items-start gap-3 p-3 rounded-lg border
                        ${validado
                          ? 'border-green-200 bg-green-50'
                          : rechazado
                          ? 'border-red-200 bg-red-50'
                          : 'border-blue-200 bg-blue-50'}
                      `}
                    >
                      {validado ? (
                        <CheckCircle className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
                      ) : rechazado ? (
                        <XCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
                      ) : (
                        <Clock className="h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5" />
                      )}
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {uploaded.nombreOriginal}
                        </p>
                        <p className="text-xs text-gray-500 mt-0.5">
                          {validado && 'Validado por el equipo'}
                          {rechazado && uploaded.motivoRechazo}
                          {pendiente && 'Esperando revisión'}
                        </p>
                      </div>
                    </div>
                  )}

                  <Button
                    type="button"
                    onClick={() => {
                      setTargetTipo(doc.tipo);
                      fileInputRef.current?.click();
                    }}
                    disabled={subiendoDocumento !== null || validado}
                    className="w-full h-12 text-base bg-green-600 hover:bg-green-700"
                    variant={uploaded && !rechazado ? 'outline' : 'default'}
                  >
                    {subiendoDocumento === doc.tipo ? (
                      <>
                        <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                        Subiendo...
                      </>
                    ) : validado ? (
                      <>
                        <CheckCircle className="mr-2 h-5 w-5" />
                        Comprobante validado
                      </>
                    ) : uploaded ? (
                      <>
                        <Upload className="mr-2 h-5 w-5" />
                        Subir otro archivo
                      </>
                    ) : (
                      <>
                        <Upload className="mr-2 h-5 w-5" />
                        Subir comprobante
                      </>
                    )}
                  </Button>
                </div>
              );
            })}

            {/* Botón "Enviar a revisión" — solo cuando todo está listo. */}
            {comprobanteSubido && !comprobanteRechazado && estadoKYC.estado === 'PENDIENTE' && (
              <>
                <div className="mt-6 pt-4 border-t">
                  <p className="text-sm text-gray-600 mb-3">
                    Ya tenemos todo lo que necesitamos. Cuando estés listo,
                    enviá tu información para que el equipo la revise.
                  </p>
                  <Button
                    onClick={handleEnviarRevision}
                    disabled={enviando}
                    className="w-full h-12 bg-blue-600 hover:bg-blue-700"
                  >
                    {enviando ? (
                      <>
                        <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                        Enviando...
                      </>
                    ) : (
                      <>
                        <Send className="mr-2 h-5 w-5" />
                        Enviar a revisión
                      </>
                    )}
                  </Button>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      )}

      {/* --- PASO 3: REVISIÓN / RESULTADO --- */}
      {pasoActivo === 'revision' && !kycAprobado && (
        <Card>
          <CardContent className="py-10 text-center space-y-4">
            <div className="inline-flex p-4 rounded-full bg-blue-100">
              <Clock className="h-10 w-10 text-blue-600" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-900">
                Tu verificación está en revisión
              </h2>
              <p className="text-sm text-gray-600 mt-2 max-w-md mx-auto">
                Nuestro equipo está revisando tu información. Te enviaremos un
                correo apenas terminemos — generalmente en menos de 24 horas
                hábiles.
              </p>
            </div>

            {estadoKYC.comentarioRevision && (
              <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-left max-w-md mx-auto">
                <p className="text-xs font-semibold text-blue-900 mb-1">
                  Comentario del revisor
                </p>
                <p className="text-sm text-blue-700">
                  {estadoKYC.comentarioRevision}
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* --- Resumen permanente (datos de la verificación) --- */}
      <details className="text-sm">
        <summary className="cursor-pointer text-gray-500 hover:text-gray-700">
          Ver detalles de mi verificación
        </summary>
        <div className="mt-2 p-4 bg-gray-50 rounded-lg space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-gray-500">Nivel</span>
            <span className="font-medium">{estadoKYC.nivel}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-500">Estado</span>
            <Badge
              className={
                kycAprobado
                  ? 'bg-green-100 text-green-800'
                  : kycRechazado
                  ? 'bg-red-100 text-red-800'
                  : enviadoARevision
                  ? 'bg-blue-100 text-blue-800'
                  : 'bg-yellow-100 text-yellow-800'
              }
            >
              {estadoKYC.descripcionEstado}
            </Badge>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-500">Iniciado</span>
            <span className="font-medium">
              {new Date(estadoKYC.fechaInicio).toLocaleDateString('es-VE')}
            </span>
          </div>
          {estadoKYC.fechaExpiracion && (
            <div className="flex items-center justify-between">
              <span className="text-gray-500">Vence</span>
              <span className="font-medium">
                {new Date(estadoKYC.fechaExpiracion).toLocaleDateString('es-VE')}
              </span>
            </div>
          )}
        </div>
      </details>
    </div>
  );
}
