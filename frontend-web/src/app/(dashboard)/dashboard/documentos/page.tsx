'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/stores/auth-store';
import {
  FileText, Download, FileCheck, CreditCard, Users, DollarSign,
  Loader2, Calendar, Clock, ExternalLink, RefreshCw
} from 'lucide-react';
import { toast } from 'sonner';

interface Documento {
  documentoId: string;
  tipo: string;
  nombreArchivo: string;
  estado: string;
  clasificacion: string;
  tamanoBytes?: number;
  hashArchivo?: string;
  fechaGeneracion: string;
  fechaExpiracion?: string | null;
}

interface DocumentosResponse {
  documentos: Documento[];
  total: number;
  page: number;
  size: number;
}

interface DocumentoGenerado {
  documentoId: string;
  tipo: string;
  nombreArchivo: string;
  estado: string;
  tamanoBytes?: number;
  hashArchivo?: string;
  clasificacion: string;
  preSignedUrl?: string;
  urlExpiraEn?: number;
  fechaGeneracion: string;
  fechaExpiracion?: string | null;
}

const TIPO_DOCUMENTO_LABELS: Record<string, string> = {
  ESTADO_CUENTA: 'Estado de Cuenta',
  CONSTANCIA_AFILIACION: 'Constancia de Afiliación',
  CONTRATO_ADHESION: 'Contrato de Adhesión',
  PAGARE: 'Pagaré',
  TABLA_AMORTIZACION: 'Tabla de Amortización',
  CARTA_BENEFICIARIOS: 'Carta de Beneficiarios',
};

const TIPO_DOCUMENTO_ICONS: Record<string, React.ElementType> = {
  ESTADO_CUENTA: DollarSign,
  CONSTANCIA_AFILIACION: FileCheck,
  CONTRATO_ADHESION: FileText,
  PAGARE: CreditCard,
  TABLA_AMORTIZACION: DollarSign,
  CARTA_BENEFICIARIOS: Users,
};

const TIPO_DOCUMENTO_COLORS: Record<string, string> = {
  ESTADO_CUENTA: 'bg-blue-100 text-blue-600',
  CONSTANCIA_AFILIACION: 'bg-green-100 text-green-600',
  CONTRATO_ADHESION: 'bg-purple-100 text-purple-600',
  PAGARE: 'bg-orange-100 text-orange-600',
  TABLA_AMORTIZACION: 'bg-teal-100 text-teal-600',
  CARTA_BENEFICIARIOS: 'bg-pink-100 text-pink-600',
};

type TipoDocumentoKey = keyof typeof TIPO_DOCUMENTO_LABELS;

interface GenerarDocumentoRequest {
  tipo: TipoDocumentoKey;
  idRelacion: string;
}

export default function DashboardDocumentosPagina() {
  const user = useAuthStore((state) => state.user);
  const [documentos, setDocumentos] = useState<Documento[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState<string | null>(null);
  const [downloading, setDownloading] = useState<string | null>(null);
  const [filterTipo, setFilterTipo] = useState<string>('');

  const socioId = user?.socioId || '';

  const loadDocumentos = useCallback(async () => {
    if (!socioId) {
      setLoading(false);
      return;
    }
    try {
      const params = new URLSearchParams({ socioId, page: '0', size: '50' });
      if (filterTipo) params.append('tipo', filterTipo);

      const res = await fetch(`/api/documentos?${params.toString()}`, { credentials: 'include' });
      if (res.ok) {
        const data: DocumentosResponse = await res.json();
        setDocumentos(data.documentos || []);
      } else if (res.status !== 404) {
        toast.error('Error al cargar documentos');
      }
    } catch {
      toast.error('Error al cargar documentos');
    } finally {
      setLoading(false);
    }
  }, [socioId, filterTipo]);

  useEffect(() => {
    loadDocumentos();
  }, [loadDocumentos]);

  const handleGenerar = async (tipo: TipoDocumentoKey, idRelacion: string) => {
    setGenerating(tipo);
    try {
      let url = '';
      switch (tipo) {
        case 'ESTADO_CUENTA':
          url = `/api/documentos/estado-cuenta/${idRelacion}`;
          break;
        case 'CONSTANCIA_AFILIACION':
          url = `/api/documentos/constancia-afiliacion/${idRelacion}`;
          break;
        case 'CARTA_BENEFICIARIOS':
          url = `/api/documentos/carta-beneficiarios/${idRelacion}`;
          break;
        case 'TABLA_AMORTIZACION':
          url = `/api/documentos/tabla-amortizacion/${idRelacion}`;
          break;
        default:
          toast.error('Tipo de documento no soportado');
          return;
      }

      const res = await fetch(url, { credentials: 'include' });

      if (res.ok) {
        const data: DocumentoGenerado = await res.json();
        toast.success(`${TIPO_DOCUMENTO_LABELS[tipo]} generado exitosamente`);
        loadDocumentos();

        if (data.preSignedUrl) {
          setTimeout(() => {
            window.open(data.preSignedUrl, '_blank');
          }, 500);
        }
      } else {
        const error = await res.json();
        toast.error(error.message || `Error al generar ${TIPO_DOCUMENTO_LABELS[tipo]}`);
      }
    } catch {
      toast.error(`Error al generar ${TIPO_DOCUMENTO_LABELS[tipo]}`);
    } finally {
      setGenerating(null);
    }
  };

  const handleDescargar = async (documentoId: string) => {
    setDownloading(documentoId);
    try {
      const res = await fetch(`/api/documentos/${documentoId}/descargar`, { credentials: 'include' });

      if (res.ok) {
        const data = await res.json();
        if (data.preSignedUrl) {
          window.open(data.preSignedUrl, '_blank');
          toast.success('Descarga iniciada');
        } else {
          toast.error('URL de descarga no disponible');
        }
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al descargar');
      }
    } catch {
      toast.error('Error al descargar documento');
    } finally {
      setDownloading(null);
    }
  };

  const formatFileSize = (bytes?: number) => {
    if (!bytes) return '-';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('es-VE', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  const getEstadoBadge = (estado: string) => {
    switch (estado) {
      case 'ALMACENADO':
        return <Badge className="bg-green-100 text-green-800">Almacenado</Badge>;
      case 'GENERADO':
        return <Badge className="bg-yellow-100 text-yellow-800">Generando</Badge>;
      case 'EXPIRADO':
        return <Badge className="bg-gray-100 text-gray-800">Expirado</Badge>;
      case 'REVOCADO':
        return <Badge className="bg-red-100 text-red-800">Revocado</Badge>;
      default:
        return <Badge variant="secondary">{estado}</Badge>;
    }
  };

  const getClasificacionBadge = (clasificacion: string) => {
    switch (clasificacion) {
      case 'CONFIDENCIAL':
        return <Badge variant="outline" className="text-red-600 border-red-300">Confidencial</Badge>;
      case 'RESTRINGIDO':
        return <Badge variant="outline" className="text-orange-600 border-orange-300">Restringido</Badge>;
      case 'PUBLICO':
        return <Badge variant="outline" className="text-green-600 border-green-300">Público</Badge>;
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <div className="p-2 bg-blue-100 rounded-lg">
            <FileText className="h-6 w-6 text-blue-600" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Documentos PDF</h1>
            <p className="text-sm text-gray-500">Genere y descargue sus documentos oficiales</p>
          </div>
        </div>
        <Button
          variant="outline"
          onClick={loadDocumentos}
          className="flex items-center gap-2"
        >
          <RefreshCw className="h-4 w-4" />
          Actualizar
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {(['ESTADO_CUENTA', 'CONSTANCIA_AFILIACION', 'CARTA_BENEFICIARIOS', 'TABLA_AMORTIZACION'] as TipoDocumentoKey[]).map((tipo) => {
          const Icon = TIPO_DOCUMENTO_ICONS[tipo];
          const colorClass = TIPO_DOCUMENTO_COLORS[tipo];
          const isGenerating = generating === tipo;

          return (
            <Card key={tipo} className="cursor-pointer hover:shadow-md transition-shadow"
              onClick={() => socioId && handleGenerar(tipo, socioId)}
            >
              <CardContent className="p-4">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg ${colorClass}`}>
                    <Icon className="h-5 w-5" />
                  </div>
                  <div className="flex-1">
                    <p className="font-medium text-sm">{TIPO_DOCUMENTO_LABELS[tipo]}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      {isGenerating ? 'Generando...' : 'Click para generar'}
                    </p>
                  </div>
                  {isGenerating && <Loader2 className="h-4 w-4 animate-spin text-gray-400" />}
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              Documentos Generados
            </CardTitle>
            <div className="flex items-center gap-2">
              <select
                value={filterTipo}
                onChange={(e) => setFilterTipo(e.target.value)}
                className="px-3 py-1.5 border rounded-md text-sm"
              >
                <option value="">Todos los tipos</option>
                {Object.entries(TIPO_DOCUMENTO_LABELS).map(([key, label]) => (
                  <option key={key} value={key}>{label}</option>
                ))}
              </select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {documentos.length === 0 ? (
            <div className="text-center py-12">
              <FileText className="h-12 w-12 mx-auto text-gray-300 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">Sin documentos</h3>
              <p className="text-gray-500">Genere sus primeros documentos usando las opciones acima</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Documento</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Tipo</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Estado</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Clasificación</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Fecha</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Tamaño</th>
                    <th className="text-right py-3 px-4 text-sm font-medium text-gray-500">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {documentos.map((doc) => (
                    <tr key={doc.documentoId} className="border-b border-gray-100 hover:bg-gray-50">
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2">
                          <FileText className="h-4 w-4 text-gray-400" />
                          <span className="text-sm font-medium text-gray-900 truncate max-w-[200px]">
                            {doc.nombreArchivo}
                          </span>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span className="text-sm text-gray-600">
                          {TIPO_DOCUMENTO_LABELS[doc.tipo] || doc.tipo}
                        </span>
                      </td>
                      <td className="py-3 px-4">{getEstadoBadge(doc.estado)}</td>
                      <td className="py-3 px-4">{getClasificacionBadge(doc.clasificacion)}</td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-1 text-sm text-gray-500">
                          <Calendar className="h-3 w-3" />
                          {formatDate(doc.fechaGeneracion)}
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span className="text-sm text-gray-500">
                          {formatFileSize(doc.tamanoBytes)}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex justify-end gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleDescargar(doc.documentoId)}
                            disabled={downloading === doc.documentoId || doc.estado !== 'ALMACENADO'}
                          >
                            {downloading === doc.documentoId ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              <Download className="h-4 w-4" />
                            )}
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}