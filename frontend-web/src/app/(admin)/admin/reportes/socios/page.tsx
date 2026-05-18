'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft, Download, FileText, Loader2, Users, CheckCircle, XCircle } from 'lucide-react';
import { toast } from 'sonner';

interface SocioReporte {
  id: string;
  numeroSocio: string;
  nombreCompleto: string;
  cedula: string;
  correo: string;
  telefono: string;
  empresa: string;
  estado: string;
  fechaRegistro: string;
  ultimaActividad: string;
}

interface PageInfo {
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export default function AdminReporteSociosPage() {
  const [loading, setLoading] = useState(false);
  const [socios, setSocios] = useState<SocioReporte[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(0);
  const [filtroActivo, setFiltroActivo] = useState<string>('');

  const cargarSocios = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: '20',
      });
      if (filtroActivo) params.set('activo', filtroActivo);

      const res = await fetch(`/api/admin/reportes/socios?${params.toString()}`, {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar');

      const data = await res.json();
      setSocios(data.content || []);
      setPageInfo({
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        size: data.size || 20,
        number: data.number || 0,
        first: data.first || true,
        last: data.last || true,
      });
    } catch (err) {
      toast.error('Error al cargar reporte de socios');
    } finally {
      setLoading(false);
    }
  }, [page, filtroActivo]);

  useEffect(() => {
    cargarSocios();
  }, [cargarSocios]);

  const formatDate = (dateStr: string | null): string => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('es-VE', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/admin/reportes" className="p-2 hover:bg-gray-100 rounded-md">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Reporte de Socios</h1>
          <p className="text-sm text-gray-500">Lista completa de socios registrados</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5" />
              Socios Registrados
              {pageInfo && (
                <Badge variant="outline">{pageInfo.totalElements} total</Badge>
              )}
            </CardTitle>
            <div className="flex gap-2">
              <label htmlFor="filtro-activo" className="text-sm font-medium text-gray-700">Filtrar:</label>
              <select
                id="filtro-activo"
                className="border rounded-md px-3 py-2 text-sm"
                value={filtroActivo}
                onChange={(e) => { setFiltroActivo(e.target.value); setPage(0); }}
              >
                <option value="">Todos</option>
                <option value="true">Activos</option>
                <option value="false">Inactivos</option>
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
                      <th className="p-3 text-left">Nro. Socio</th>
                      <th className="p-3 text-left">Nombre</th>
                      <th className="p-3 text-left">Cédula</th>
                      <th className="p-3 text-left">Empresa</th>
                      <th className="p-3 text-left">Contacto</th>
                      <th className="p-3 text-center">Estado</th>
                      <th className="p-3 text-left">Registro</th>
                      <th className="p-3 text-left">Última Actividad</th>
                    </tr>
                  </thead>
                  <tbody>
                    {socios.map((socio) => (
                      <tr key={socio.id} className="border-b hover:bg-gray-50">
                        <td className="p-3 font-mono text-xs">{socio.numeroSocio}</td>
                        <td className="p-3 font-medium">{socio.nombreCompleto}</td>
                        <td className="p-3">{socio.cedula}</td>
                        <td className="p-3 text-xs">{socio.empresa}</td>
                        <td className="p-3">
                          <div className="text-xs">
                            <div>{socio.correo}</div>
                            <div className="text-gray-500">{socio.telefono}</div>
                          </div>
                        </td>
                        <td className="p-3 text-center">
                          <Badge className={socio.estado === 'ACTIVO' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}>
                            {socio.estado === 'ACTIVO' ? <CheckCircle className="h-3 w-3 mr-1" /> : <XCircle className="h-3 w-3 mr-1" />}
                            {socio.estado}
                          </Badge>
                        </td>
                        <td className="p-3 text-xs text-gray-500">{formatDate(socio.fechaRegistro)}</td>
                        <td className="p-3 text-xs text-gray-500">{formatDate(socio.ultimaActividad)}</td>
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