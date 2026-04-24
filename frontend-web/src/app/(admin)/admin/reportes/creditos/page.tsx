'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft, Download, FileText, Loader2, CreditCard, TrendingUp, DollarSign, Clock, CheckCircle, XCircle } from 'lucide-react';
import { toast } from 'sonner';

interface CreditoReporte {
  id: string;
  numeroSolicitud: string;
  socioNombre: string;
  socioCedula: string;
  tipoCredito: string;
  montoSolicitado: number;
  plazoMeses: number;
  tasaInteres: number;
  estado: string;
  fechaSolicitud: string;
  fechaAprobacion: string | null;
}

interface PageInfo {
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

interface CreditosStats {
  total: number;
  pendientes: number;
  aprobados: number;
  rechazados: number;
  montoTotalAprobado: number;
}

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: 'bg-yellow-100 text-yellow-800',
  EN_EVALUACION: 'bg-blue-100 text-blue-800',
  APROBADA: 'bg-green-100 text-green-800',
  RECHAZADA: 'bg-red-100 text-red-800',
  DESEMBOLSADO: 'bg-purple-100 text-purple-800',
};

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: 'VES',
    minimumFractionDigits: 0,
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

export default function AdminReporteCreditosPage() {
  const [loading, setLoading] = useState(false);
  const [creditos, setCreditos] = useState<CreditoReporte[]>([]);
  const [stats, setStats] = useState<CreditosStats>({ total: 0, pendientes: 0, aprobados: 0, rechazados: 0, montoTotalAprobado: 0 });
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(0);
  const [filtroEstado, setFiltroEstado] = useState('');

  const cargarCreditos = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: '20',
      });
      if (filtroEstado) params.set('estado', filtroEstado);

      const res = await fetch(`/api/admin/reportes/creditos?${params.toString()}`, {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar');

      const data = await res.json();
      setCreditos(data.content || []);

      const calculatedStats: CreditosStats = {
        total: data.totalElements || 0,
        pendientes: 0,
        aprobados: 0,
        rechazados: 0,
        montoTotalAprobado: 0,
      };
      (data.content || []).forEach((c: CreditoReporte) => {
        switch (c.estado) {
          case 'PENDIENTE': calculatedStats.pendientes++; break;
          case 'APROBADA': case 'DESEMBOLSADO': calculatedStats.aprobados++; break;
          case 'RECHAZADA': calculatedStats.rechazados++; break;
        }
        if (c.estado === 'APROBADA' || c.estado === 'DESEMBOLSADO') {
          calculatedStats.montoTotalAprobado += c.montoSolicitado;
        }
      });
      setStats(calculatedStats);

      setPageInfo({
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        size: data.size || 20,
        number: data.number || 0,
        first: data.first || true,
        last: data.last || true,
      });
    } catch (err) {
      toast.error('Error al cargar reporte de créditos');
    } finally {
      setLoading(false);
    }
  }, [page, filtroEstado]);

  useEffect(() => {
    cargarCreditos();
  }, [cargarCreditos]);

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/admin/reportes" className="p-2 hover:bg-gray-100 rounded-md">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Reporte de Créditos</h1>
          <p className="text-sm text-gray-500">Estado de solicitudes y créditos</p>
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card className="border-l-4 border-l-gray-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Total</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{stats.total}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-yellow-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Pendientes</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-yellow-600">{stats.pendientes}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-green-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Aprobados</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">{stats.aprobados}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-red-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Rechazados</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-red-600">{stats.rechazados}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-5 w-5" />
              Detalle de Créditos
            </CardTitle>
            <div className="flex gap-2">
              <select
                className="border rounded-md px-3 py-2 text-sm"
                value={filtroEstado}
                onChange={(e) => { setFiltroEstado(e.target.value); setPage(0); }}
              >
                <option value="">Todos</option>
                <option value="PENDIENTE">Pendiente</option>
                <option value="EN_EVALUACION">En Evaluación</option>
                <option value="APROBADA">Aprobada</option>
                <option value="RECHAZADA">Rechazada</option>
                <option value="DESEMBOLSADO">Desembolsado</option>
              </select>
              <Button variant="outline">
                <Download className="h-4 w-4 mr-2" />
                Exportar
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : creditos.length === 0 ? (
            <div className="text-center py-12">
              <CreditCard className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay créditos registrados</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th className="p-3 text-left">Nro. Solicitud</th>
                      <th className="p-3 text-left">Socio</th>
                      <th className="p-3 text-left">Tipo</th>
                      <th className="p-3 text-right">Monto</th>
                      <th className="p-3 text-center">Plazo</th>
                      <th className="p-3 text-center">Tasa</th>
                      <th className="p-3 text-center">Estado</th>
                      <th className="p-3 text-left">Fecha</th>
                    </tr>
                  </thead>
                  <tbody>
                    {creditos.map((credito) => (
                      <tr key={credito.id} className="border-b hover:bg-gray-50">
                        <td className="p-3 font-mono text-xs">{credito.numeroSolicitud}</td>
                        <td className="p-3">
                          <div className="font-medium">{credito.socioNombre}</div>
                          <div className="text-xs text-gray-500">{credito.socioCedula}</div>
                        </td>
                        <td className="p-3 text-xs">{credito.tipoCredito}</td>
                        <td className="p-3 text-right font-medium">{formatCurrency(credito.montoSolicitado)}</td>
                        <td className="p-3 text-center">{credito.plazoMeses} meses</td>
                        <td className="p-3 text-center">{(credito.tasaInteres * 100).toFixed(2)}%</td>
                        <td className="p-3 text-center">
                          <Badge className={ESTADO_COLORS[credito.estado] || 'bg-gray-100'}>
                            {credito.estado.replace('_', ' ')}
                          </Badge>
                        </td>
                        <td className="p-3 text-xs text-gray-500">{formatDate(credito.fechaSolicitud)}</td>
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
                    <Button variant="outline" size="sm" onClick={() => setPage(Math.max(0, page - 1))} disabled={pageInfo.first}>
                      Anterior
                    </Button>
                    <span className="px-3 py-2 text-sm">
                      Página {pageInfo.number + 1} de {pageInfo.totalPages}
                    </span>
                    <Button variant="outline" size="sm" onClick={() => setPage(page + 1)} disabled={pageInfo.last}>
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