'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { CreditCard, Loader2, CheckCircle, XCircle, Clock, DollarSign, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';

interface SolicitudCredito {
  id: string;
  numeroSolicitud: string;
  socioId: string;
  socioNombre: string;
  socioNumero: string;
  socioCedula: string;
  socioEmpresa: string;
  tipoCreditoId: number;
  tipoCreditoNombre: string;
  montoSolicitado: number;
  plazoMeses: number;
  tasaInteresAplicada: number;
  cuotaMensualEstimada: number;
  estado: string;
  destinoCredito: string;
  createdAt: string;
  fechaAprobacion: string | null;
  fechaRechazo: string | null;
  motivoRechazo: string | null;
}

interface PageInfo {
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

interface CreditosStats {
  total: number;
  aprobados: number;
  pendientes: number;
  enEvaluacion: number;
  rechazados: number;
  desembolsados: number;
}

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: 'bg-yellow-100 text-yellow-800 border-yellow-300',
  EN_EVALUACION: 'bg-blue-100 text-blue-800 border-blue-300',
  APROBADA: 'bg-green-100 text-green-800 border-green-300',
  RECHAZADA: 'bg-red-100 text-red-800 border-red-300',
  DESEMBOLSADO: 'bg-purple-100 text-purple-800 border-purple-300',
  CANCELADA: 'bg-gray-100 text-gray-800 border-gray-300',
};

const ESTADO_ICONS: Record<string, React.ReactNode> = {
  PENDIENTE: <Clock className="h-4 w-4" />,
  EN_EVALUACION: <AlertCircle className="h-4 w-4" />,
  APROBADA: <CheckCircle className="h-4 w-4" />,
  RECHAZADA: <XCircle className="h-4 w-4" />,
  DESEMBOLSADO: <DollarSign className="h-4 w-4" />,
};

const ESTADOS = ['PENDIENTE', 'EN_EVALUACION', 'APROBADA', 'RECHAZADA', 'DESEMBOLSADO', 'CANCELADA'];

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: 'VES',
    minimumFractionDigits: 2,
  }).format(value);
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('es-VE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

export default function AdminCreditosPage() {
  const [stats, setStats] = useState<CreditosStats>({
    total: 0, aprobados: 0, pendientes: 0, enEvaluacion: 0, rechazados: 0, desembolsados: 0
  });
  const [solicitudes, setSolicitudes] = useState<SolicitudCredito[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [filtroEstado, setFiltroEstado] = useState<string>('');
  const [page, setPage] = useState(0);

  const cargarSolicitudes = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: '15',
        sortBy: 'createdAt',
        sortDir: 'DESC',
      });
      if (filtroEstado) params.set('estado', filtroEstado);

      const res = await fetch(`/api/admin/creditos/solicitudes?${params.toString()}`, {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar solicitudes');

      const data = await res.json();
      setSolicitudes(data.content || []);
      setPageInfo({
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        size: data.size || 15,
        number: data.number || 0,
        first: data.first || true,
        last: data.last || true,
        empty: data.empty !== undefined ? data.empty : true,
      });

      const calculatedStats: CreditosStats = {
        total: data.totalElements || 0,
        aprobados: 0,
        pendientes: 0,
        enEvaluacion: 0,
        rechazados: 0,
        desembolsados: 0,
      };
      (data.content || []).forEach((s: SolicitudCredito) => {
        switch (s.estado) {
          case 'PENDIENTE': calculatedStats.pendientes++; break;
          case 'EN_EVALUACION': calculatedStats.enEvaluacion++; break;
          case 'APROBADA': calculatedStats.aprobados++; break;
          case 'RECHAZADA': calculatedStats.rechazados++; break;
          case 'DESEMBOLSADO': calculatedStats.desembolsados++; break;
        }
      });
      setStats((prev) => ({ ...prev, ...calculatedStats }));

    } catch (err) {
      console.error('Error cargando solicitudes:', err);
      toast.error('Error al cargar solicitudes de crédito');
    } finally {
      setLoading(false);
    }
  }, [page, filtroEstado]);

  useEffect(() => {
    cargarSolicitudes();
  }, [cargarSolicitudes]);

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Gestión de Créditos</h1>
        <Badge variant="outline" className="text-primary">
          {stats.total} solicitudes
        </Badge>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
        <Card className="border-l-4 border-l-yellow-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Pendientes</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-yellow-600">{stats.pendientes}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-blue-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">En Evaluación</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-blue-600">{stats.enEvaluacion}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-green-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Aprobadas</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">{stats.aprobados}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-red-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Rechazadas</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-red-600">{stats.rechazados}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-purple-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Desembolsados</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-purple-600">{stats.desembolsados}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-gray-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Total</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{stats.total}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Todas las Solicitudes</CardTitle>
            <div className="flex gap-2">
              <select
                value={filtroEstado}
                onChange={(e) => { setFiltroEstado(e.target.value); setPage(0); }}
                className="px-3 py-2 border rounded-md text-sm"
              >
                <option value="">Todos los estados</option>
                {ESTADOS.map((estado) => (
                  <option key={estado} value={estado}>{estado.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : solicitudes.length === 0 ? (
            <div className="text-center py-12">
              <CreditCard className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay solicitudes de crédito</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th className="text-left p-3 font-medium">Nro. Solicitud</th>
                      <th className="text-left p-3 font-medium">Socio</th>
                      <th className="text-left p-3 font-medium">Tipo</th>
                      <th className="text-right p-3 font-medium">Monto</th>
                      <th className="text-center p-3 font-medium">Plazo</th>
                      <th className="text-center p-3 font-medium">Estado</th>
                      <th className="text-center p-3 font-medium">Fecha</th>
                      <th className="text-center p-3 font-medium">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {solicitudes.map((solicitud) => (
                      <tr key={solicitud.id} className="border-b hover:bg-gray-50">
                        <td className="p-3 font-mono text-xs">{solicitud.numeroSolicitud}</td>
                        <td className="p-3">
                          <div className="font-medium">{solicitud.socioNombre}</div>
                          <div className="text-xs text-gray-500">{solicitud.socioCedula} · {solicitud.socioEmpresa}</div>
                        </td>
                        <td className="p-3">{solicitud.tipoCreditoNombre}</td>
                        <td className="p-3 text-right font-medium">{formatCurrency(solicitud.montoSolicitado)}</td>
                        <td className="p-3 text-center">{solicitud.plazoMeses} meses</td>
                        <td className="p-3 text-center">
                          <Badge className={ESTADO_COLORS[solicitud.estado] || 'bg-gray-100'}>
                            <span className="flex items-center gap-1">
                              {ESTADO_ICONS[solicitud.estado]}
                              {solicitud.estado.replace('_', ' ')}
                            </span>
                          </Badge>
                        </td>
                        <td className="p-3 text-center text-xs text-gray-500">
                          {formatDate(solicitud.createdAt)}
                        </td>
                        <td className="p-3 text-center">
                          <Link
                            href={`/admin/creditos/${solicitud.numeroSolicitud}`}
                            className="text-green-600 hover:text-green-800 font-medium text-xs"
                          >
                            Ver Detalle
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {pageInfo && pageInfo.totalPages > 1 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t">
                  <p className="text-sm text-gray-500">
                    Mostrando {pageInfo.number * pageInfo.size + 1} - {Math.min((pageInfo.number + 1) * pageInfo.size, pageInfo.totalElements)} de {pageInfo.totalElements}
                  </p>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage(Math.max(0, page - 1))}
                      disabled={pageInfo.first}
                    >
                      Anterior
                    </Button>
                    <span className="px-3 py-2 text-sm">
                      Página {pageInfo.number + 1} de {pageInfo.totalPages}
                    </span>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage(page + 1)}
                      disabled={pageInfo.last}
                    >
                      Siguiente
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}