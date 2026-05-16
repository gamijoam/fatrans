'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import {
  Loader2, Shield, ArrowLeft, CheckCircle, XCircle, FileText, Image,
  Calendar, User, Clock, AlertTriangle, CheckCheck, X
} from 'lucide-react';
import { toast } from 'sonner';

interface DocumentoRevision {
  id: string;
  tipo: string;
  descripcion: string;
  estado: string;
  urlVisualizacion: string;
  nombreOriginal: string;
  tamanoBytes: number;
  fechaSubida: string;
}

interface RevisionDetalle {
  verificacionId: string;
  socioId: string;
  nivel: string;
  estado: string;
  fechaInicio: string;
  fechaEnvio: string;
  documentos: DocumentoRevision[];
  consentimiento: {
    aceptado: boolean;
    fechaConsentimiento: string;
  };
}

export default function AdminKYCDetallePagina() {
  const params = useParams();
  const id = params.id as string;

  const [detalle, setDetalle] = useState<RevisionDetalle | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const [aprobarOpen, setAprobarOpen] = useState(false);
  const [rechazarOpen, setRechazarOpen] = useState(false);
  const [comentarioRechazo, setComentarioRechazo] = useState('');

  const cargarDetalle = useCallback(async () => {
    try {
      const res = await fetch(`/api/admin/kyc/revision/${id}`, { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        setDetalle(data);
      } else {
        toast.error('Error al cargar detalles');
      }
    } catch {
      toast.error('Error al cargar detalles');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    cargarDetalle();
  }, [cargarDetalle]);

  const handleAprobar = async () => {
    setActionLoading(true);
    try {
      const res = await fetch(`/api/admin/kyc/revision/${id}/aprobar`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({}),
      });

      if (res.ok) {
        toast.success('Verificación aprobada correctamente');
        setAprobarOpen(false);
        cargarDetalle();
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al aprobar');
      }
    } catch {
      toast.error('Error al aprobar verificación');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRechazar = async () => {
    if (!comentarioRechazo.trim()) {
      toast.error('Ingrese el motivo del rechazo');
      return;
    }

    setActionLoading(true);
    try {
      const res = await fetch(`/api/admin/kyc/revision/${id}/rechazar`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivo: comentarioRechazo }),
      });

      if (res.ok) {
        toast.success('Verificación rechazada');
        setRechazarOpen(false);
        setComentarioRechazo('');
        cargarDetalle();
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al rechazar');
      }
    } catch {
      toast.error('Error al rechazar verificación');
    } finally {
      setActionLoading(false);
    }
  };

  const getEstadoBadge = (estado: string) => {
    switch (estado) {
      case 'APROBADO':
        return <Badge className="bg-green-100 text-green-800">Aprobado</Badge>;
      case 'RECHAZADO':
        return <Badge className="bg-red-100 text-red-800">Rechazado</Badge>;
      case 'EN_REVISION':
        return <Badge className="bg-blue-100 text-blue-800">En Revisión</Badge>;
      default:
        return <Badge variant="secondary">{estado}</Badge>;
    }
  };

  const getDocumentoIcon = (tipo: string) => {
    if (tipo.includes('SELFIE')) return <Image className="h-5 w-5" />;
    return <FileText className="h-5 w-5" />;
  };

  const formatBytes = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  if (!detalle) {
    return (
      <div className="p-6">
        <p className="text-gray-500">No se encontró la verificación</p>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/admin/kyc" className="p-2 hover:bg-gray-100 rounded-md">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-gray-900">Detalle KYC</h1>
          <p className="text-sm text-gray-500">Revisión de verificación de identidad</p>
        </div>
        {getEstadoBadge(detalle.estado)}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                Documentos Cargados
              </CardTitle>
            </CardHeader>
            <CardContent>
              {!detalle.documentos || detalle.documentos.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <FileText className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                  <p>No hay documentos cargados</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {detalle.documentos.map((doc) => (
                    <div
                      key={doc.id}
                      className="flex items-center gap-4 p-3 border rounded-lg hover:bg-gray-50"
                    >
                      <div className="p-2 bg-gray-100 rounded-lg">
                        {getDocumentoIcon(doc.tipo)}
                      </div>
                      <div className="flex-1">
                        <p className="font-medium text-sm">{doc.descripcion}</p>
                        <p className="text-xs text-gray-500">
                          {doc.nombreOriginal} • {formatBytes(doc.tamanoBytes)}
                        </p>
                      </div>
                      <Badge
                        variant={doc.estado === 'VALIDADO' ? 'default' : 'secondary'}
                      >
                        {doc.estado}
                      </Badge>
                      {/* Usamos el endpoint BFF en lugar de la pre-signed URL
                          del backend (apunta a MinIO interno, no resuelve desde
                          el browser). El BFF stream el archivo con auth admin. */}
                      <Button size="sm" variant="outline" asChild>
                        <a
                          href={`/api/admin/kyc/documentos/${doc.id}/descargar`}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          Ver
                        </a>
                      </Button>
                    </div>
                  ))}
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
            <CardContent className="space-y-4">
              <div className="flex items-center gap-3">
                <User className="h-4 w-4 text-gray-400" />
                <div>
                  <p className="text-xs text-gray-500">Socio ID</p>
                  <p className="font-mono text-sm">{detalle.socioId}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Shield className="h-4 w-4 text-gray-400" />
                <div>
                  <p className="text-xs text-gray-500">Nivel KYC</p>
                  <p className="text-sm">{detalle.nivel}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Calendar className="h-4 w-4 text-gray-400" />
                <div>
                  <p className="text-xs text-gray-500">Fecha Envío</p>
                  <p className="text-sm">
                    {detalle.fechaEnvio
                      ? new Date(detalle.fechaEnvio).toLocaleDateString('es-VE', {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric',
                        })
                      : '-'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {detalle.estado === 'EN_REVISION' && (
            <Card>
              <CardHeader>
                <CardTitle>Acciones</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <AlertDialog open={aprobarOpen} onOpenChange={setAprobarOpen}>
                  <AlertDialogTrigger asChild>
                    <Button className="w-full bg-green-600 hover:bg-green-700">
                      <CheckCheck className="h-4 w-4 mr-2" />
                      Aprobar
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>¿Aprobar verificación?</AlertDialogTitle>
                      <AlertDialogDescription>
                        Esta acción aprobará la verificación KYC del socio. El usuario
                        podrá acceder a todas las funcionalidades del sistema.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancelar</AlertDialogCancel>
                      <AlertDialogAction onClick={handleAprobar} disabled={actionLoading}>
                        {actionLoading ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          'Aprobar'
                        )}
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>

                <AlertDialog open={rechazarOpen} onOpenChange={setRechazarOpen}>
                  <AlertDialogTrigger asChild>
                    <Button variant="outline" className="w-full text-red-600 border-red-200 hover:bg-red-50">
                      <X className="h-4 w-4 mr-2" />
                      Rechazar
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Rechazar verificación</AlertDialogTitle>
                      <AlertDialogDescription>
                        Ingrese el motivo del rechazo. El socio será notificado y podrá
                        reenviar sus documentos.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <div className="py-4">
                      <Label htmlFor="motivo">Motivo del rechazo</Label>
                      <Textarea
                        id="motivo"
                        value={comentarioRechazo}
                        onChange={(e) => setComentarioRechazo(e.target.value)}
                        placeholder="Ej: Documento ilegible, datos no coinciden..."
                        className="mt-2"
                        rows={3}
                      />
                    </div>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancelar</AlertDialogCancel>
                      <AlertDialogAction
                        onClick={handleRechazar}
                        disabled={actionLoading || !comentarioRechazo.trim()}
                        className="bg-red-600 hover:bg-red-700"
                      >
                        {actionLoading ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          'Rechazar'
                        )}
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}