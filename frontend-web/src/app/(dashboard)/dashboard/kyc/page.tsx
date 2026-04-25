'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import {
  Shield, Upload, FileText, CheckCircle, XCircle, Clock, AlertTriangle,
  Check, X, Loader2, Calendar, Image as ImageIcon, Send
} from 'lucide-react';
import { toast } from 'sonner';

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
}

const TIPOS_DOCUMENTO = [
  { tipo: 'CEDULA_ANVERSO', label: 'Cédula - Anverso', required: true },
  { tipo: 'CEDULA_REVERSO', label: 'Cédula - Reverso', required: true },
  { tipo: 'SELFIE_CEDULA', label: 'Selfie con Cédula', required: true },
  { tipo: 'COMPROBANTE_DOMICILIO', label: 'Comprobante de Domicilio', required: true },
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
    setEnviando(true);
    try {
      const res = await fetch('/api/kyc/enviar', {
        method: 'POST',
        credentials: 'include',
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

    setSubiendoDocumento(tipo);
    try {
      const formData = new FormData();
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

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <div className="p-2 bg-blue-100 rounded-lg">
          <Shield className="h-6 w-6 text-blue-600" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Verificación de Identidad (KYC)</h1>
          <p className="text-sm text-gray-500">Complete su verificación para acceder a todos los servicios</p>
        </div>
      </div>

      {!estadoKYC ? (
        <Card>
          <CardContent className="py-12">
            <div className="text-center">
              <Shield className="h-12 w-12 mx-auto text-gray-300 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">KYC no iniciado</h3>
              <p className="text-gray-500 mb-6">No tienes un proceso de verificación activo. Contacta al administrador.</p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <FileText className="h-5 w-5" />
                      Estado de Verificación
                    </CardTitle>
                    {getEstadoBadge(estadoKYC.estado)}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">Nivel:</span>
                    <span className="font-medium">{estadoKYC.nivel}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">Documentos:</span>
                    <span className="font-medium">
                      {estadoKYC.documentosValidos} de {estadoKYC.documentosRequeridos} válidos
                    </span>
                  </div>
                  <Progress
                    value={estadoKYC.documentosRequeridos > 0 
                      ? (estadoKYC.documentosValidos / estadoKYC.documentosRequeridos) * 100 
                      : 0}
                    className="h-2"
                  />
                  {estadoKYC.fechaExpiracion && (
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                      <Calendar className="h-4 w-4" />
                      <span>Expira: {new Date(estadoKYC.fechaExpiracion).toLocaleDateString('es-VE')}</span>
                      {estadoKYC.diasRestantes > 0 && (
                        <Badge variant="outline">{estadoKYC.diasRestantes} días restantes</Badge>
                      )}
                    </div>
                  )}
                  {estadoKYC.motivoRechazo && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                      <div className="flex items-center gap-2 text-red-800">
                        <AlertTriangle className="h-4 w-4" />
                        <span className="font-medium">Motivo del rechazo:</span>
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

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Upload className="h-5 w-5" />
                    Documentos Requeridos
                  </CardTitle>
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
                  <div className="space-y-3">
                    {TIPOS_DOCUMENTO.map((doc) => {
                      const uploaded = estadoKYC.documentos?.find(d => d.tipo === doc.tipo);
                      return (
                        <div
                          key={doc.tipo}
                          className="flex items-center justify-between p-3 border rounded-lg"
                        >
                          <div className="flex items-center gap-3">
                            {uploaded ? (
                              getDocumentoEstadoIcon(uploaded.estado)
                            ) : (
                              <div className="p-2 bg-gray-100 rounded-lg">
                                <FileText className="h-5 w-5 text-gray-400" />
                              </div>
                            )}
                            <div>
                              <p className="font-medium text-sm">{doc.label}</p>
                              {uploaded && (
                                <p className="text-xs text-gray-500">{uploaded.nombreOriginal}</p>
                              )}
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            {uploaded ? (
                              <>
                                {getDocumentoStatusBadge(uploaded.estado)}
                                {uploaded.estado === 'RECHAZADO' && (
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => {
                                      setTargetTipo(doc.tipo);
                                      fileInputRef.current?.click();
                                    }}
                                  >
                                    <Upload className="h-4 w-4 mr-1" />
                                    Reemplazar
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
                                disabled={subiendoDocumento !== null}
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
                        Enviar a Revisión
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>

            <div className="space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle>Información</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex items-center gap-3">
                    <Shield className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-xs text-gray-500">ID Verificación</p>
                      <p className="font-mono text-sm">{estadoKYC.verificacionId?.slice(0, 8) ?? '-'}...</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <Calendar className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-xs text-gray-500">Fecha Inicio</p>
                      <p className="text-sm">
                        {estadoKYC.fechaInicio
                          ? new Date(estadoKYC.fechaInicio).toLocaleDateString('es-VE')
                          : '-'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-sm">Estado del Proceso</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {['PENDIENTE', 'EN_REVISION', 'APROBADO'].map((estado, i) => {
                      const currentIndex = ['PENDIENTE', 'EN_REVISION', 'APROBADO'].indexOf(estadoKYC.estado);
                      const isActive = i <= currentIndex;
                      const isCurrent = estadoKYC.estado === estado;
                      return (
                        <div key={estado} className="flex items-center gap-3">
                          <div className={`w-6 h-6 rounded-full flex items-center justify-center ${
                            isActive ? 'bg-green-600' : 'bg-gray-200'
                          }`}>
                            {isActive && <Check className="h-4 w-4 text-white" />}
                          </div>
                          <span className={`text-sm ${isCurrent ? 'font-medium' : 'text-gray-500'}`}>
                            {estado.replace('_', ' ')}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </>
      )}
    </div>
  );
}