'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ProgressBar } from '@/components/ui/progress';
import {
  Shield, Upload, FileText, CheckCircle, XCircle, Clock, AlertTriangle,
  Loader2, Calendar, Send, Camera
} from 'lucide-react';
import { toast } from 'sonner';
import { BiometricCapture } from '@/components/features/kyc/biometric-capture';

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
  /** Estado biométrico (Didit) — usado para ocultar documentos ya capturados
      por la verificación facial y para reflejar el estado del widget en la UI. */
  estadoBiometria: 'NO_INICIADA' | 'EN_PROGRESO' | 'APROBADA' | 'RECHAZADA' | 'EXPIRADA' | null;
}

/** Todos los documentos que el KYC puede requerir. El subset visible depende
    del estado biométrico — ver `documentosVisibles` abajo. Cuando la biometría
    está APROBADA, los documentos con `cubrePorBiometria=true` se ocultan porque
    Didit ya capturó esa información (cédula anverso/reverso + selfie). */
const TIPOS_DOCUMENTO = [
  { tipo: 'CEDULA_ANVERSO', label: 'Cédula - Anverso', required: true, cubrePorBiometria: true },
  { tipo: 'CEDULA_REVERSO', label: 'Cédula - Reverso', required: true, cubrePorBiometria: true },
  { tipo: 'SELFIE_CEDULA', label: 'Selfie con Cédula', required: true, cubrePorBiometria: true },
  { tipo: 'COMPROBANTE_DOMICILIO', label: 'Comprobante de Domicilio', required: true, cubrePorBiometria: false },
];

export default function DashboardKYCPagina() {
  const [estadoKYC, setEstadoKYC] = useState<EstadoKYC | null>(null);
  const [loading, setLoading] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [subiendoDocumento, setSubiendoDocumento] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [targetTipo, setTargetTipo] = useState<string | null>(null);

  const cargarEstado = useCallback(async () => {
    try {
      const res = await fetch('/api/kyc/estado', { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        setEstadoKYC(data);
      } else if (res.status === 404) {
        setEstadoKYC(null);
      } else {
        toast.error('Error al cargar estado KYC');
      }
    } catch {
      toast.error('Error al cargar estado KYC');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarEstado();
  }, [cargarEstado]);

  const getEstadoBadge = (estado: string) => {
    switch (estado) {
      case 'APROBADO':
        return <Badge className="bg-green-100 text-green-800">Aprobado</Badge>;
      case 'RECHAZADO':
        return <Badge className="bg-red-100 text-red-800">Rechazado</Badge>;
      case 'EN_REVISION':
        return <Badge className="bg-blue-100 text-blue-800">En Revisión</Badge>;
      case 'PENDIENTE':
        return <Badge className="bg-yellow-100 text-yellow-800">Pendiente</Badge>;
      case 'EXPIRADO':
        return <Badge className="bg-gray-100 text-gray-800">Expirado</Badge>;
      default:
        return <Badge variant="secondary">{estado}</Badge>;
    }
  };

  const getDocumentoEstadoIcon = (estado: string) => {
    switch (estado) {
      case 'VALIDADO':
        return <CheckCircle className="h-5 w-5 text-green-600" />;
      case 'RECHAZADO':
        return <XCircle className="h-5 w-5 text-red-600" />;
      default:
        return <Clock className="h-5 w-5 text-yellow-600" />;
    }
  };

  const getDocumentoStatusBadge = (estado: string) => {
    switch (estado) {
      case 'VALIDADO':
        return <Badge className="bg-green-100 text-green-800 text-xs">Válido</Badge>;
      case 'RECHAZADO':
        return <Badge className="bg-red-100 text-red-800 text-xs">Rechazado</Badge>;
      case 'PENDIENTE':
        return <Badge className="bg-yellow-100 text-yellow-800 text-xs">Pendiente</Badge>;
      default:
        return <Badge variant="secondary" className="text-xs">{estado}</Badge>;
    }
  };

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
        toast.success('Documentos enviados para revisión');
        cargarEstado();
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al enviar');
      }
    } catch {
      toast.error('Error al enviar documentos');
    } finally {
      setEnviando(false);
    }
  };

  const handleSubirDocumento = async (tipo: string, file: File) => {
    if (!file) return;

    const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'application/pdf'];
    if (!validTypes.includes(file.type)) {
      toast.error('Formato no válido. Use: JPG, PNG, WEBP o PDF');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      toast.error('El archivo excede 10MB');
      return;
    }

    if (!estadoKYC?.verificacionId) {
      toast.error('No hay verificación activa');
      return;
    }

    setSubiendoDocumento(tipo);
    try {
      // Mandamos verificacionId al BFF porque el backend lo necesita en el
      // payload JSON que arma el BFF (el endpoint no acepta multipart).
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
        toast.success('Documento subido exitosamente');
        cargarEstado();
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al subir documento');
      }
    } catch {
      toast.error('Error al subir documento');
    } finally {
      setSubiendoDocumento(null);
    }
  };

  const puedeEnviar = estadoKYC && estadoKYC.estado === 'PENDIENTE';

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  /** Documentos: el socio puede subir si KYC está PENDIENTE o RECHAZADO (re-subida).
      No bloqueamos en EN_REVISION para no perder cambios si la sesión expiró. */
  const puedeSubirDocumentos = estadoKYC?.estado === 'PENDIENTE' || estadoKYC?.estado === 'RECHAZADO';
  /** Biométrica: la mostramos siempre que el KYC NO esté APROBADO. Para EN_REVISION
      la dejamos visible porque el componente ya gestiona internamente su estado
      (consentimiento → ready → in_progress → submitted). */
  const puedeBiometrica = estadoKYC && estadoKYC.estado !== 'APROBADO';

  /** Si la biometría ya pasó, Didit ya capturó cédula + selfie. Ocultamos los
      3 documentos correspondientes en "Paso 2" y dejamos solo los que NO cubre
      la biometría (comprobante de domicilio). Evita que el socio resuba algo
      que ya tenemos validado por el proveedor. */
  const biometriaAprobada = estadoKYC?.estadoBiometria === 'APROBADA';
  const documentosVisibles = biometriaAprobada
    ? TIPOS_DOCUMENTO.filter(d => !d.cubrePorBiometria)
    : TIPOS_DOCUMENTO;

  /** El backend siempre cuenta 4 documentos requeridos. Si la biometría
      aprobó, sumamos los 3 que cubre Didit como ya validados — la barra
      de progreso refleja el avance real considerando la biometría. */
  const docsCubiertosPorBiometria = biometriaAprobada
    ? TIPOS_DOCUMENTO.filter(d => d.cubrePorBiometria).length
    : 0;
  const docsValidosCalculados = (estadoKYC?.documentosValidos ?? 0) + docsCubiertosPorBiometria;

  return (
    <div className="p-4 lg:p-6 space-y-6 max-w-4xl mx-auto">
      {/* Header simplificado: el shell ya muestra "Verificación KYC" arriba,
          aquí solo aportamos contexto (estado actual + progreso). */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Verificación de identidad</h1>
          <p className="text-sm text-gray-500">Confirma tu identidad para habilitar todos los servicios.</p>
        </div>
        {estadoKYC && getEstadoBadge(estadoKYC.estado)}
      </div>

      {!estadoKYC ? (
        <Card>
          <CardContent className="py-12">
            <div className="text-center">
              <Shield className="h-12 w-12 mx-auto text-gray-300 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">KYC no iniciado</h3>
              <p className="text-gray-500">Aún no tienes un proceso de verificación activo. Contacta al administrador.</p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <>
          {/* Card unificada: badge + alertas (rechazo/comentario) + progreso de
              documentos + fecha de expiración cuando aplica. Reemplaza a las
              cards "Información" (ID/Fecha) y "Estado del Proceso" (stepper)
              que mostraban datos sin valor para el socio. */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center gap-2 text-base">
                <FileText className="h-5 w-5 text-gray-500" />
                Resumen
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-xs text-gray-500 uppercase tracking-wide">Nivel</p>
                  <p className="font-medium">{estadoKYC.nivel}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 uppercase tracking-wide">Documentos</p>
                  <p className="font-medium">
                    {docsValidosCalculados} / {estadoKYC.documentosRequeridos} válidos
                    {biometriaAprobada && (
                      <span className="text-xs text-gray-500 font-normal ml-1">
                        ({docsCubiertosPorBiometria} por biometría)
                      </span>
                    )}
                  </p>
                </div>
              </div>

              <ProgressBar
                value={estadoKYC.documentosRequeridos > 0
                  ? (docsValidosCalculados / estadoKYC.documentosRequeridos) * 100
                  : 0}
                className="h-2"
              />

              {estadoKYC.fechaExpiracion && estadoKYC.estado === 'APROBADO' && (
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Calendar className="h-4 w-4" />
                  <span>Vigente hasta el {new Date(estadoKYC.fechaExpiracion).toLocaleDateString('es-VE')}</span>
                  {estadoKYC.diasRestantes > 0 && (
                    <Badge variant="outline">{estadoKYC.diasRestantes} días</Badge>
                  )}
                </div>
              )}

              {estadoKYC.motivoRechazo && (
                <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                  <div className="flex items-center gap-2 text-red-800">
                    <AlertTriangle className="h-4 w-4" />
                    <span className="font-medium text-sm">Motivo del rechazo</span>
                  </div>
                  <p className="text-sm text-red-700 mt-1">{estadoKYC.motivoRechazo}</p>
                </div>
              )}

              {estadoKYC.comentarioRevision && (
                <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                  <p className="text-sm text-blue-700">{estadoKYC.comentarioRevision}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Captura biométrica vía Didit (passive liveness + face match + OCR cédula).
              Es el camino más rápido para verificar identidad — si pasa, el analista
              tiene mucho más contexto antes de revisar los documentos manuales. */}
          {puedeBiometrica && (
            <div className="space-y-2">
              <div className="flex items-center gap-2 px-1">
                <Camera className="h-4 w-4 text-blue-600" />
                <h2 className="text-sm font-semibold text-gray-700 uppercase tracking-wide">
                  Paso 1 · Verificación biométrica
                </h2>
                {!biometriaAprobada && (
                  <span className="text-xs text-gray-400">(recomendado)</span>
                )}
              </div>
              <BiometricCapture
                onCompleted={cargarEstado}
                estadoBackend={estadoKYC.estadoBiometria}
              />
            </div>
          )}

          {/* Documentos manuales: complemento del paso biométrico — si la
              biometría está APROBADA, solo pedimos los documentos que Didit
              NO captura (comprobante de domicilio). El resto se considera
              cubierto por la verificación biométrica. */}
          <div className="space-y-2">
            <div className="flex items-center gap-2 px-1 flex-wrap">
              <Upload className="h-4 w-4 text-green-600" />
              <h2 className="text-sm font-semibold text-gray-700 uppercase tracking-wide">
                Paso 2 · Documentos
              </h2>
              {biometriaAprobada && (
                <span className="text-xs text-gray-500">
                  · cédula y selfie ya cubiertos por la verificación facial
                </span>
              )}
            </div>
            <Card>
              <CardContent className="p-4 lg:p-6">
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
                <div className="space-y-3">
                  {documentosVisibles.map((doc) => {
                    const uploaded = estadoKYC.documentos?.find(d => d.tipo === doc.tipo);
                    return (
                      <div
                        key={doc.tipo}
                        className="flex items-center justify-between p-3 border rounded-lg"
                      >
                        <div className="flex items-center gap-3 min-w-0 flex-1">
                          {uploaded ? (
                            getDocumentoEstadoIcon(uploaded.estado)
                          ) : (
                            <div className="p-2 bg-gray-100 rounded-lg">
                              <FileText className="h-5 w-5 text-gray-400" />
                            </div>
                          )}
                          <div className="min-w-0 flex-1">
                            <p className="font-medium text-sm">{doc.label}</p>
                            {uploaded && (
                              <p className="text-xs text-gray-500 truncate">{uploaded.nombreOriginal}</p>
                            )}
                          </div>
                        </div>
                        <div className="flex items-center gap-2 shrink-0">
                          {uploaded ? (
                            <>
                              {getDocumentoStatusBadge(uploaded.estado)}
                              {uploaded.estado === 'RECHAZADO' && puedeSubirDocumentos && (
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => {
                                    setTargetTipo(doc.tipo);
                                    fileInputRef.current?.click();
                                  }}
                                >
                                  <Upload className="h-4 w-4" />
                                </Button>
                              )}
                            </>
                          ) : (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => {
                                setTargetTipo(doc.tipo);
                                fileInputRef.current?.click();
                              }}
                              disabled={subiendoDocumento !== null || !puedeSubirDocumentos}
                            >
                              {subiendoDocumento === doc.tipo ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                <Upload className="h-4 w-4 mr-1" />
                              )}
                              Subir
                            </Button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>

                {puedeEnviar && (
                  <div className="mt-6 pt-4 border-t">
                    <Button
                      className="w-full bg-green-600 hover:bg-green-700"
                      onClick={handleEnviarRevision}
                      disabled={enviando}
                    >
                      {enviando ? (
                        <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      ) : (
                        <Send className="h-4 w-4 mr-2" />
                      )}
                      Enviar a revisión
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}