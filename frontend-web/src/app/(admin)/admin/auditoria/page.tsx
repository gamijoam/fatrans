'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Search, FileText, Filter, ChevronLeft, ChevronRight } from 'lucide-react';
import { toast } from 'sonner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

interface AuditLog {
  id: string;
  tipoEvento: string;
  usuarioId: string;
  nombreUsuario: string | null;
  ipAddress: string;
  timestamp: string;
  detalles: string | null;
  entityType: string | null;
  entityId: string | null;
  action: string | null;
}

const TIPO_EVENTO_COLORS: Record<string, string> = {
  'LOGIN_SUCCESS': 'bg-green-100 text-green-800',
  'LOGIN_FAILED': 'bg-red-100 text-red-800',
  'LOGOUT': 'bg-gray-100 text-gray-800',
  'TOKEN_REFRESH': 'bg-blue-100 text-blue-800',
  'ACCOUNT_LOCKED': 'bg-red-100 text-red-800',
  'DASHBOARD_ADMIN_ACCESS': 'bg-purple-100 text-purple-800',
  'SESSIONS_INVALIDATED': 'bg-orange-100 text-orange-800',
  'SESSION_INVALIDATED': 'bg-orange-100 text-orange-800',
  'TIPO_CREDITO_CREADO': 'bg-teal-100 text-teal-800',
  'TIPO_CREDITO_ACTUALIZADO': 'bg-cyan-100 text-cyan-800',
  'TIPO_CREDITO_ACTIVADO': 'bg-green-100 text-green-800',
  'TIPO_CREDITO_DESACTIVADO': 'bg-red-100 text-red-800',
  'ADMIN_CREADO': 'bg-indigo-100 text-indigo-800',
  'ADMIN_ACTUALIZADO': 'bg-cyan-100 text-cyan-800',
  'ADMIN_ACTIVADO': 'bg-green-100 text-green-800',
  'ADMIN_DESACTIVADO': 'bg-red-100 text-red-800',
};

const TIPOS_EVENTO = [
  'LOGIN_SUCCESS',
  'LOGIN_FAILED',
  'LOGOUT',
  'TOKEN_REFRESH',
  'ACCOUNT_LOCKED',
  'DASHBOARD_ADMIN_ACCESS',
  'SESSIONS_INVALIDATED',
  'SESSION_INVALIDATED',
  'TIPO_CREDITO_CREADO',
  'TIPO_CREDITO_ACTUALIZADO',
  'TIPO_CREDITO_ACTIVADO',
  'TIPO_CREDITO_DESACTIVADO',
  'ADMIN_CREADO',
  'ADMIN_ACTUALIZADO',
  'ADMIN_ACTIVADO',
  'ADMIN_DESACTIVADO',
];

export default function AdminAuditoriaPage() {
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [size] = useState(20);

  const [filtroTipoEvento, setFiltroTipoEvento] = useState<string>('');
  const [filtroFechaInicio, setFiltroFechaInicio] = useState<string>('');
  const [filtroFechaFin, setFiltroFechaFin] = useState<string>('');

  const cargarAuditoria = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      if (filtroTipoEvento) params.append('tipoEvento', filtroTipoEvento);
      if (filtroFechaInicio) params.append('fechaInicio', filtroFechaInicio);
      if (filtroFechaFin) params.append('fechaFin', filtroFechaFin);

      const res = await fetch(`/api/admin/auditoria?${params.toString()}`, {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar auditoría');
      const data = await res.json();
      setAuditLogs(data.auditoria || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error('Error cargando auditoría:', err);
      toast.error('Error al cargar logs de auditoría');
    } finally {
      setLoading(false);
    }
  }, [page, size, filtroTipoEvento, filtroFechaInicio, filtroFechaFin]);

  useEffect(() => {
    cargarAuditoria();
  }, [cargarAuditoria]);

  const handleLimpiarFiltros = () => {
    setFiltroTipoEvento('');
    setFiltroFechaInicio('');
    setFiltroFechaFin('');
    setPage(0);
  };

  const formatDate = (dateStr: string) => {
    try {
      return new Date(dateStr).toLocaleString('es-VE');
    } catch {
      return dateStr;
    }
  };

  const getBadgeColor = (tipoEvento: string) => {
    return TIPO_EVENTO_COLORS[tipoEvento] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Auditoría del Sistema</h1>
        <Badge variant="outline" className="text-primary">
          {totalElements} registros
        </Badge>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Logs de Auditoría
          </CardTitle>
          <div className="flex flex-wrap gap-4 mt-4">
            <div className="space-y-1">
              <Label htmlFor="tipoEvento" className="text-xs">Tipo de Evento</Label>
              <select
                id="tipoEvento"
                value={filtroTipoEvento}
                onChange={(e) => {
                  setFiltroTipoEvento(e.target.value);
                  setPage(0);
                }}
                className="px-3 py-2 border rounded-md text-sm w-48"
              >
                <option value="">Todos</option>
                {TIPOS_EVENTO.map((tipo) => (
                  <option key={tipo} value={tipo}>{tipo}</option>
                ))}
              </select>
            </div>
            <div className="space-y-1">
              <Label htmlFor="fechaInicio" className="text-xs">Fecha Inicio</Label>
              <Input
                id="fechaInicio"
                type="date"
                value={filtroFechaInicio}
                onChange={(e) => {
                  setFiltroFechaInicio(e.target.value);
                  setPage(0);
                }}
                className="w-40"
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="fechaFin" className="text-xs">Fecha Fin</Label>
              <Input
                id="fechaFin"
                type="date"
                value={filtroFechaFin}
                onChange={(e) => {
                  setFiltroFechaFin(e.target.value);
                  setPage(0);
                }}
                className="w-40"
              />
            </div>
            <div className="flex items-end gap-2">
              <Button variant="outline" size="sm" onClick={handleLimpiarFiltros}>
                <Filter className="h-4 w-4 mr-1" />
                Limpiar
              </Button>
              <Button size="sm" onClick={() => cargarAuditoria()}>
                <Search className="h-4 w-4 mr-1" />
                Buscar
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : auditLogs.length === 0 ? (
            <div className="text-center py-12">
              <FileText className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay logs de auditoría</p>
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Fecha/Hora</TableHead>
                    <TableHead>Tipo Evento</TableHead>
                    <TableHead>Usuario</TableHead>
                    <TableHead>IP</TableHead>
                    <TableHead>Detalles</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {auditLogs.map((log) => (
                    <TableRow key={log.id}>
                      <TableCell className="text-sm whitespace-nowrap">
                        {formatDate(log.timestamp)}
                      </TableCell>
                      <TableCell>
                        <Badge className={getBadgeColor(log.tipoEvento)}>
                          {log.tipoEvento}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div>
                          <p className="font-medium text-sm">
                            {log.nombreUsuario || 'Sistema'}
                          </p>
                          {log.usuarioId && (
                            <p className="text-xs text-gray-500">
                              {log.usuarioId.substring(0, 8)}...
                            </p>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="font-mono text-sm">
                        {log.ipAddress}
                      </TableCell>
                      <TableCell className="text-sm max-w-xs truncate">
                        {log.detalles || '-'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {totalPages > 1 && (
                <div className="flex items-center justify-between mt-4">
                  <p className="text-sm text-gray-500">
                    Página {page + 1} de {totalPages}
                  </p>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage(page - 1)}
                      disabled={page === 0}
                    >
                      <ChevronLeft className="h-4 w-4 mr-1" />
                      Anterior
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage(page + 1)}
                      disabled={page >= totalPages - 1}
                    >
                      Siguiente
                      <ChevronRight className="h-4 w-4 ml-1" />
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