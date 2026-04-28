'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Users, Loader2, UserX, UserCheck, Eye, Search } from 'lucide-react';
import { toast } from 'sonner';

interface Socio {
  id: string;
  numeroSocio: string;
  tipoDocumento: string;
  numeroDocumento: string;
  primerNombre: string;
  segundoNombre: string;
  primerApellido: string;
  segundoApellido: string;
  correoElectronico: string;
  telefonoPrincipal: string;
  empresa: string;
  estado: string;
  fechaRegistro: string;
  fechaIngreso: string;
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

interface SociosStats {
  total: number;
  activos: number;
  inactivos: number;
  pendientes: number;
}

const ESTADO_COLORS: Record<string, string> = {
  ACTIVO: 'bg-green-100 text-green-800 border-green-300',
  INACTIVO: 'bg-red-100 text-red-800 border-red-300',
  PENDIENTE: 'bg-yellow-100 text-yellow-800 border-yellow-300',
  ELIMINADO: 'bg-gray-100 text-gray-800 border-gray-300',
};

const ESTADO_ICONS: Record<string, React.ReactNode> = {
  ACTIVO: <UserCheck className="h-4 w-4" />,
  INACTIVO: <UserX className="h-4 w-4" />,
  PENDIENTE: <Users className="h-4 w-4" />,
};

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-VE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  } catch {
    return dateStr;
  }
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: 'VES',
    minimumFractionDigits: 2,
  }).format(value);
}

const ESTADOS = ['ACTIVO', 'INACTIVO', 'PENDIENTE', 'ELIMINADO'];

export default function AdminSociosPage() {
  const [stats, setStats] = useState<SociosStats>({
    total: 0, activos: 0, inactivos: 0, pendientes: 0
  });
  const [socios, setSocios] = useState<Socio[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [page, setPage] = useState(0);
  const [debouncedBusqueda, setDebouncedBusqueda] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedBusqueda(busqueda);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [busqueda]);

  const cargarSocios = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: '15',
        sortBy: 'fechaRegistro',
        direction: 'DESC',
      });
      if (debouncedBusqueda) params.set('numeroDocumento', debouncedBusqueda);
      if (filtroEstado) params.set('estado', filtroEstado);

      const res = await fetch(`/api/admin/socios?${params.toString()}`, {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar socios');

      const data = await res.json();
      setSocios(data.content || []);
      setPageInfo({
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        size: data.size || 15,
        number: data.number || 0,
        first: data.first || true,
        last: data.last || true,
        empty: data.empty !== undefined ? data.empty : true,
      });

      const calculatedStats: SociosStats = {
        total: data.totalElements || 0,
        activos: 0,
        inactivos: 0,
        pendientes: 0,
      };
      (data.content || []).forEach((s: Socio) => {
        switch (s.estado) {
          case 'ACTIVO': calculatedStats.activos++; break;
          case 'INACTIVO': calculatedStats.inactivos++; break;
          case 'PENDIENTE': calculatedStats.pendientes++; break;
        }
      });
      setStats((prev) => ({ ...prev, ...calculatedStats }));

    } catch (err) {
      console.error('Error cargando socios:', err);
      toast.error('Error al cargar socios');
    } finally {
      setLoading(false);
    }
  }, [page, debouncedBusqueda, filtroEstado]);

  useEffect(() => {
    cargarSocios();
  }, [cargarSocios]);

  const getNombreCompleto = (socio: Socio) => {
    const parts = [
      socio.primerNombre,
      socio.segundoNombre,
      socio.primerApellido,
      socio.segundoApellido,
    ].filter(Boolean);
    return parts.join(' ');
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Gestión de Socios</h1>
        <Badge variant="outline" className="text-primary">
          {stats.total} socios
        </Badge>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <Card className="border-l-4 border-l-blue-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Total</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{stats.total}</p>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-green-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Activos</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">{stats.activos}</p>
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
        <Card className="border-l-4 border-l-red-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-medium text-gray-500">Inactivos</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-red-600">{stats.inactivos}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
            <CardTitle>Lista de Socios</CardTitle>
            <div className="flex flex-wrap gap-3">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  placeholder="Buscar por cédula..."
                  value={busqueda}
                  onChange={(e) => setBusqueda(e.target.value)}
                  className="pl-9 w-48"
                />
              </div>
              <select
                value={filtroEstado}
                onChange={(e) => { setFiltroEstado(e.target.value); setPage(0); }}
                className="px-3 py-2 border rounded-md text-sm"
              >
                <option value="">Todos los estados</option>
                {ESTADOS.map((estado) => (
                  <option key={estado} value={estado}>{estado}</option>
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
          ) : socios.length === 0 ? (
            <div className="text-center py-12">
              <Users className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay socios registrados</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th className="text-left p-3 font-medium">Nro. Socio</th>
                      <th className="text-left p-3 font-medium">Nombre</th>
                      <th className="text-left p-3 font-medium">Cédula</th>
                      <th className="text-left p-3 font-medium">Email</th>
                      <th className="text-left p-3 font-medium">Empresa</th>
                      <th className="text-center p-3 font-medium">Estado</th>
                      <th className="text-center p-3 font-medium">Fecha Registro</th>
                      <th className="text-center p-3 font-medium">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {socios.map((socio) => (
                      <tr key={socio.id} className="border-b hover:bg-gray-50">
                        <td className="p-3 font-mono text-xs">{socio.numeroSocio || '-'}</td>
                        <td className="p-3 font-medium">{getNombreCompleto(socio)}</td>
                        <td className="p-3">{socio.tipoDocumento} {socio.numeroDocumento}</td>
                        <td className="p-3 text-xs">{socio.correoElectronico || '-'}</td>
                        <td className="p-3 text-xs">{socio.empresa || '-'}</td>
                        <td className="p-3 text-center">
                          <Badge className={ESTADO_COLORS[socio.estado] || 'bg-gray-100'}>
                            <span className="flex items-center gap-1">
                              {ESTADO_ICONS[socio.estado]}
                              {socio.estado}
                            </span>
                          </Badge>
                        </td>
                        <td className="p-3 text-center text-xs text-gray-500">
                          {formatDate(socio.fechaRegistro)}
                        </td>
                        <td className="p-3 text-center">
                          <Link
                            href={`/admin/socios/${socio.id}`}
                            className="inline-flex items-center gap-1 text-green-600 hover:text-green-800 font-medium text-xs"
                          >
                            <Eye className="h-3 w-3" />
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