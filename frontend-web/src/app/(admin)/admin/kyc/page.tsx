'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Loader2, Shield, CheckCircle, XCircle, Clock, AlertTriangle, Users, FileCheck } from 'lucide-react';
import { toast } from 'sonner';

interface EstadisticasKYC {
  totalVerificaciones: number;
  estadoActual: {
    pendientes: number;
    enRevision: number;
    aprobados: number;
    rechazados: number;
    expirados: number;
  };
  metricas: {
    tiempoPromedioRevisionHoras: number;
    tasaAprobacion: number;
    tasaRechazo: number;
    kycPorExpirarProximoMes: number;
  };
}

interface ColaItem {
  verificacionId: string;
  socioId: string;
  nivel: string;
  estado: string;
  fechaEnvio: string;
  tiempoEnCola: string;
}

interface ColaResponse {
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
  cola: ColaItem[];
}

export default function AdminKYCPagina() {
  const [stats, setStats] = useState<EstadisticasKYC | null>(null);
  const [cola, setCola] = useState<ColaItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filtroEstado, setFiltroEstado] = useState('EN_REVISION');

  useEffect(() => {
    cargarDatos();
  }, []);

  useEffect(() => {
    cargarCola();
  }, [page, filtroEstado]);

  const cargarDatos = async () => {
    try {
      const res = await fetch('/api/admin/kyc/estadisticas', { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        setStats(data);
      }
    } catch {
      toast.error('Error al cargar estadísticas');
    }
  };

  const cargarCola = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: '10',
        estado: filtroEstado,
      });
      const res = await fetch(`/api/admin/kyc/cola-revision?${params.toString()}`, { credentials: 'include' });
      if (res.ok) {
        const data: ColaResponse = await res.json();
        setCola(data.cola);
        setTotalPages(data.totalPaginas);
      }
    } catch {
      toast.error('Error al cargar cola de revisión');
    } finally {
      setLoading(false);
    }
  };

  const ESTADOS_KYC = [
    { value: 'EN_REVISION', label: 'En Revisión' },
    { value: 'PENDIENTE', label: 'Pendientes' },
    { value: 'APROBADO', label: 'Aprobados' },
    { value: 'RECHAZADO', label: 'Rechazados' },
  ];

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <div className="p-2 bg-green-100 rounded-lg">
          <Shield className="h-6 w-6 text-green-600" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Revisión KYC</h1>
          <p className="text-sm text-gray-500">Verificación de identidad de socios</p>
        </div>
      </div>

      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
                <Clock className="h-4 w-4" /> Pendientes
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{stats.estadoActual.pendientes}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
                <FileCheck className="h-4 w-4" /> En Revisión
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{stats.estadoActual.enRevision}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
                <CheckCircle className="h-4 w-4 text-green-600" /> Aprobados
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{stats.estadoActual.aprobados}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
                <XCircle className="h-4 w-4 text-red-600" /> Rechazados
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{stats.estadoActual.rechazados}</p>
            </CardContent>
          </Card>
        </div>
      )}

      {stats?.metricas && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Tasa de Aprobación</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-green-600">
                {(stats.metricas.tasaAprobacion * 100).toFixed(1)}%
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Tiempo Promedio Revisión</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">
                {stats.metricas.tiempoPromedioRevisionHoras.toFixed(1)}h
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle className="text-sm flex items-center gap-2">
                <AlertTriangle className="h-4 w-4 text-yellow-500" /> Por Expirar (30 días)
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-yellow-600">
                {stats.metricas.kycPorExpirarProximoMes}
              </p>
            </CardContent>
          </Card>
        </div>
      )}

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5" />
              Cola de Revisión
            </CardTitle>
            <div className="flex gap-2 items-center">
              <label htmlFor="filtro-estado-kyc" className="text-sm font-medium text-gray-700">Estado:</label>
              <select
                id="filtro-estado-kyc"
                value={filtroEstado}
                onChange={(e) => { setFiltroEstado(e.target.value); setPage(0); }}
                className="border rounded-md px-3 py-2 text-sm"
              >
                {ESTADOS_KYC.map((e) => (
                  <option key={e.value} value={e.value}>{e.label}</option>
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
          ) : cola.length === 0 ? (
            <div className="text-center py-12">
              <Shield className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay verificaciones en cola</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th className="p-3 text-left">Socio ID</th>
                      <th className="p-3 text-left">Nivel</th>
                      <th className="p-3 text-left">Estado</th>
                      <th className="p-3 text-left">Fecha</th>
                      <th className="p-3 text-left">Tiempo en Cola</th>
                      <th className="p-3 text-left">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cola.map((item) => (
                      <tr key={item.verificacionId} className="border-b hover:bg-gray-50">
                        <td className="p-3 font-mono text-xs">{item.socioId.slice(0, 8)}...</td>
                        <td className="p-3">
                          <Badge variant="outline">{item.nivel}</Badge>
                        </td>
                        <td className="p-3">
                          <Badge
                            variant={item.estado === 'APROBADO' ? 'default' : item.estado === 'RECHAZADO' ? 'destructive' : 'secondary'}
                          >
                            {item.estado}
                          </Badge>
                        </td>
                        <td className="p-3 text-gray-500">
                          {item.fechaEnvio ? new Date(item.fechaEnvio).toLocaleDateString('es-VE') : '-'}
                        </td>
                        <td className="p-3 text-gray-500">{item.tiempoEnCola}</td>
                        <td className="p-3">
                          <Link href={`/admin/kyc/${item.verificacionId}`}>
                            <Button size="sm" variant="outline">
                              Revisar
                            </Button>
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {totalPages > 1 && (
                <div className="flex items-center justify-between mt-4">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(Math.max(0, page - 1))}
                    disabled={page === 0}
                  >
                    Anterior
                  </Button>
                  <span className="text-sm text-gray-500">
                    Página {page + 1} de {totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(page + 1)}
                    disabled={page >= totalPages - 1}
                  >
                    Siguiente
                  </Button>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}