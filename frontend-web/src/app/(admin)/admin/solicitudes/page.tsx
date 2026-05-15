'use client';

import { useEffect, useState, useCallback } from 'react';
import { useAuthStore } from '@/stores/auth-store';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Check, X, Clock, User, Phone, Mail, Building, Eye, MapPin, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';

interface Solicitud {
  id: string;
  // --- Datos personales ---
  nombreCompleto: string;
  tipoDocumento?: 'CEDULA' | 'CEDULA_EXTRANJERO' | 'PASAPORTE' | 'RIF';
  cedula: string;
  fechaNacimiento?: string;
  genero?: 'MASCULINO' | 'FEMENINO' | 'OTRO';
  estadoCivil?: 'SOLTERO' | 'CASADO' | 'DIVORCIADO' | 'VIUDO' | 'UNION_LIBRE';
  // --- Contacto ---
  correoElectronico: string;
  telefono: string;
  // --- Laboral ---
  empresa: string;
  rifEmpresa?: string;
  departamento?: string;
  cargo?: string;
  salario?: number | string;
  // --- Dirección ---
  direccionEstado?: string;
  direccionCiudad?: string;
  direccionMunicipio?: string;
  direccionCalle?: string;
  // --- Emergencia ---
  emergenciaNombre?: string;
  emergenciaTelefono?: string;
  emergenciaParentesco?: string;
  // --- Consentimientos ---
  aceptaTerminos?: boolean;
  aceptaLopdp?: boolean;
  // --- Trazabilidad ---
  estado: 'PENDIENTE' | 'APROBADO' | 'APROBADA' | 'RECHAZADO' | 'RECHAZADA';
  fechaSolicitud: string;
  fechaRevision?: string;
  revisadoPor?: string;
  comentario?: string;
  motivoRechazo?: string;
}

export default function SolicitudesPage() {
  const [solicitudes, setSolicitudes] = useState<Solicitud[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [rejectMotivo, setRejectMotivo] = useState('');
  /** Solicitud cuyo detalle completo se está viendo en el modal "Ver detalle". */
  const [detailSolicitud, setDetailSolicitud] = useState<Solicitud | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElementos, setTotalElementos] = useState(0);
  const user = useAuthStore((state) => state.user);

  const fetchSolicitudes = useCallback(async (page: number) => {
    setIsLoading(true);
    try {
      const res = await fetch("/api/admin/solicitudes", { credentials: "include" });
      const data = await res.json();
      const content = data.data?.solicitudes || data.content || data || [];
      setSolicitudes(content);
      setTotalPages(data.data?.totalPaginas || data.totalPages || 1);
      setTotalElementos(data.data?.totalElementos || data.totalElements || content.length);
    } catch (err) {
      console.error('Error fetching solicitudes:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSolicitudes(currentPage);
  }, [currentPage, fetchSolicitudes]);

  const handleAprobar = async (id: string) => {
    setIsProcessing(true);
    try {
      const resA = await fetch(`/api/admin/solicitudes/${id}/aprobar`, { method: 'POST', credentials: 'include' });
      if (!resA.ok) { const e = await resA.json().catch(() => ({})); throw new Error(e.message || 'Error al aprobar'); }
      toast.success('Solicitud aprobada');
      await fetchSolicitudes(currentPage);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Error al aprobar solicitud');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleOpenReject = (id: string) => {
    setSelectedId(id);
    setRejectMotivo('');
  };

  const handleReject = async () => {
    if (!selectedId || !rejectMotivo.trim()) {
      toast.error('El motivo es obligatorio');
      return;
    }

    setIsProcessing(true);
    try {
      const resR = await fetch(`/api/admin/solicitudes/${selectedId}/rechazar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivo: rejectMotivo }),
      });
      if (!resR.ok) { const e = await resR.json().catch(() => ({})); throw new Error(e.message || 'Error al rechazar'); }
      toast.success('Solicitud rechazada');
      setSelectedId(null);
      await fetchSolicitudes(currentPage);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Error al rechazar solicitud');
    } finally {
      setIsProcessing(false);
    }
  };

  const getEstadoBadge = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE':
        return <Badge variant="outline" className="text-yellow-600 border-yellow-600"><Clock className="h-3 w-3 mr-1" /> Pendiente</Badge>;
      case 'APROBADO':
        return <Badge variant="default" className="bg-green-600"><Check className="h-3 w-3 mr-1" /> Aprobado</Badge>;
      case 'RECHAZADO':
        return <Badge variant="destructive"><X className="h-3 w-3 mr-1" /> Rechazado</Badge>;
      default:
        return <Badge variant="secondary">{estado}</Badge>;
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString('es-VE', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      });
    } catch {
      return dateString;
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Solicitudes de Registro</h1>
        <Badge variant="outline" className="text-lg px-3 py-1">
          {totalElementos} pendientes
        </Badge>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Lista de Solicitudes</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : solicitudes.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <Clock className="h-12 w-12 mx-auto mb-4 text-gray-300" />
              <p>No hay solicitudes pendientes</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-3 px-4 font-semibold text-gray-600">Fecha</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-600">Solicitante</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-600">Cédula</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-600">Contacto</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-600">Empresa</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-600">Estado</th>
                      <th className="text-right py-3 px-4 font-semibold text-gray-600">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {solicitudes.map((solicitud) => (
                      <tr key={solicitud.id} className="border-b border-gray-100 hover:bg-gray-50">
                        <td className="py-3 px-4 text-sm text-gray-500">
                          {formatDate(solicitud.fechaSolicitud)}
                        </td>
                        <td className="py-3 px-4">
                          <div className="flex items-center gap-2">
                            <User className="h-4 w-4 text-gray-400" />
                            <span className="font-medium">{solicitud.nombreCompleto}</span>
                          </div>
                        </td>
                        <td className="py-3 px-4 text-sm font-mono">
                          {solicitud.cedula}
                        </td>
                        <td className="py-3 px-4">
                          <div className="space-y-1">
                            <div className="flex items-center gap-1 text-sm">
                              <Mail className="h-3 w-3 text-gray-400" />
                              {solicitud.correoElectronico}
                            </div>
                            <div className="flex items-center gap-1 text-sm">
                              <Phone className="h-3 w-3 text-gray-400" />
                              {solicitud.telefono}
                            </div>
                          </div>
                        </td>
                        <td className="py-3 px-4">
                          <div className="flex items-center gap-1 text-sm">
                            <Building className="h-3 w-3 text-gray-400" />
                            {solicitud.empresa}
                          </div>
                        </td>
                        <td className="py-3 px-4">
                          {getEstadoBadge(solicitud.estado)}
                        </td>
                        <td className="py-3 px-4">
                          <div className="flex items-center justify-end gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => setDetailSolicitud(solicitud)}
                              disabled={isProcessing}
                              aria-label="Ver detalle"
                            >
                              <Eye className="h-4 w-4 mr-1" />
                              Ver detalle
                            </Button>
                            <Button
                              size="sm"
                              variant="default"
                              className="bg-green-600 hover:bg-green-700"
                              onClick={() => handleAprobar(solicitud.id)}
                              disabled={isProcessing}
                            >
                              <Check className="h-4 w-4 mr-1" />
                              Aprobar
                            </Button>
                            <Button
                              size="sm"
                              variant="destructive"
                              onClick={() => handleOpenReject(solicitud.id)}
                              disabled={isProcessing}
                            >
                              <X className="h-4 w-4 mr-1" />
                              Rechazar
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 mt-4">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                    disabled={currentPage === 0}
                  >
                    Anterior
                  </Button>
                  <span className="text-sm text-gray-500">
                    Página {currentPage + 1} de {totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={currentPage >= totalPages - 1}
                  >
                    Siguiente
                  </Button>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      <Dialog open={!!selectedId} onOpenChange={() => setSelectedId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Rechazar Solicitud</DialogTitle>
            <DialogDescription>
              Indica el motivo del rechazo. Esta acción no se puede deshacer.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="motivo">Motivo de rechazo *</Label>
              <Input
                id="motivo"
                value={rejectMotivo}
                onChange={(e) => setRejectMotivo(e.target.value)}
                placeholder="Ej: Documentos incompletos"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedId(null)}>
              Cancelar
            </Button>
            <Button variant="destructive" onClick={handleReject} disabled={isProcessing}>
              {isProcessing ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Rechazando...
                </>
              ) : (
                'Rechazar'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Modal "Ver detalle": muestra TODOS los campos de la solicitud para que el
          admin pueda revisar con contexto antes de aprobar/rechazar. */}
      <Dialog open={!!detailSolicitud} onOpenChange={(open) => !open && setDetailSolicitud(null)}>
        <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Detalle de la solicitud</DialogTitle>
            <DialogDescription>
              {detailSolicitud?.nombreCompleto} — {detailSolicitud?.cedula}
            </DialogDescription>
          </DialogHeader>

          {detailSolicitud && (
            <div className="space-y-5 py-2 text-sm">
              <DetalleSeccion icon={<User className="h-4 w-4" />} titulo="Datos personales">
                <DetalleCampo label="Nombre completo" value={detailSolicitud.nombreCompleto} />
                <DetalleCampo label="Tipo de documento" value={detailSolicitud.tipoDocumento} />
                <DetalleCampo label="Cédula / documento" value={detailSolicitud.cedula} />
                <DetalleCampo label="Fecha de nacimiento" value={detailSolicitud.fechaNacimiento} />
                <DetalleCampo label="Género" value={detailSolicitud.genero} />
                <DetalleCampo label="Estado civil" value={detailSolicitud.estadoCivil} />
              </DetalleSeccion>

              <DetalleSeccion icon={<Mail className="h-4 w-4" />} titulo="Contacto">
                <DetalleCampo label="Correo electrónico" value={detailSolicitud.correoElectronico} />
                <DetalleCampo label="Teléfono" value={detailSolicitud.telefono} />
              </DetalleSeccion>

              <DetalleSeccion icon={<Building className="h-4 w-4" />} titulo="Información laboral">
                <DetalleCampo label="Empresa" value={detailSolicitud.empresa} />
                <DetalleCampo label="RIF empresa" value={detailSolicitud.rifEmpresa} />
                <DetalleCampo label="Departamento" value={detailSolicitud.departamento} />
                <DetalleCampo label="Cargo" value={detailSolicitud.cargo} />
                <DetalleCampo
                  label="Salario (Bs)"
                  value={
                    detailSolicitud.salario != null && detailSolicitud.salario !== ''
                      ? String(detailSolicitud.salario)
                      : undefined
                  }
                />
              </DetalleSeccion>

              <DetalleSeccion icon={<MapPin className="h-4 w-4" />} titulo="Dirección de residencia">
                <DetalleCampo label="Estado" value={detailSolicitud.direccionEstado} />
                <DetalleCampo label="Ciudad" value={detailSolicitud.direccionCiudad} />
                <DetalleCampo label="Municipio" value={detailSolicitud.direccionMunicipio} />
                <DetalleCampo label="Calle / dirección" value={detailSolicitud.direccionCalle} />
              </DetalleSeccion>

              <DetalleSeccion icon={<Phone className="h-4 w-4" />} titulo="Contacto de emergencia">
                <DetalleCampo label="Nombre" value={detailSolicitud.emergenciaNombre} />
                <DetalleCampo label="Teléfono" value={detailSolicitud.emergenciaTelefono} />
                <DetalleCampo label="Parentesco" value={detailSolicitud.emergenciaParentesco} />
              </DetalleSeccion>

              <DetalleSeccion icon={<ShieldCheck className="h-4 w-4" />} titulo="Consentimientos legales">
                <DetalleCampo
                  label="Acepta términos"
                  value={detailSolicitud.aceptaTerminos === true ? 'Sí' : detailSolicitud.aceptaTerminos === false ? 'No' : undefined}
                />
                <DetalleCampo
                  label="Acepta LOPDP"
                  value={detailSolicitud.aceptaLopdp === true ? 'Sí' : detailSolicitud.aceptaLopdp === false ? 'No' : undefined}
                />
              </DetalleSeccion>

              <DetalleSeccion icon={<Clock className="h-4 w-4" />} titulo="Trazabilidad de revisión">
                <DetalleCampo label="Estado" value={detailSolicitud.estado} />
                <DetalleCampo label="Fecha solicitud" value={detailSolicitud.fechaSolicitud} />
                <DetalleCampo label="Fecha revisión" value={detailSolicitud.fechaRevision} />
                <DetalleCampo label="Revisado por" value={detailSolicitud.revisadoPor} />
                <DetalleCampo label="Comentario" value={detailSolicitud.comentario} />
                <DetalleCampo label="Motivo de rechazo" value={detailSolicitud.motivoRechazo} />
              </DetalleSeccion>
            </div>
          )}

          <DialogFooter>
            <Button variant="outline" onClick={() => setDetailSolicitud(null)}>
              Cerrar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

/* --- Helpers locales para el modal de detalle --- */

function DetalleSeccion({
  icon,
  titulo,
  children,
}: {
  icon: React.ReactNode;
  titulo: string;
  children: React.ReactNode;
}) {
  return (
    <div className="space-y-2">
      <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-muted-foreground">
        <span className="text-blue-600">{icon}</span>
        <span className="font-semibold">{titulo}</span>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 pl-6">{children}</div>
    </div>
  );
}

function DetalleCampo({ label, value }: { label: string; value?: string | null }) {
  const display = value == null || value === '' ? '—' : value;
  return (
    <div className="flex flex-col">
      <span className="text-xs text-muted-foreground">{label}</span>
      <span className="text-sm break-words">{display}</span>
    </div>
  );
}