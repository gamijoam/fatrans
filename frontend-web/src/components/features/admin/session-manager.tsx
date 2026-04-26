'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Badge } from '@/components/ui/badge';
import { Loader2, ShieldAlert, Trash2, Monitor, Globe } from 'lucide-react';
import { SesionInfo, sesionesApi } from '@/lib/api/sesiones';
import { toast } from 'sonner';

interface SessionManagerProps {
  usuarioId: string;
  usuarioNombre: string;
}

export function SessionManager({ usuarioId, usuarioNombre }: SessionManagerProps) {
  const [sesiones, setSesiones] = useState<SesionInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [invalidating, setInvalidating] = useState(false);
  const [showInvalidateAllDialog, setShowInvalidateAllDialog] = useState(false);
  const [selectedSesionId, setSelectedSesionId] = useState<string | null>(null);
  const [expanded, setExpanded] = useState(false);

  const cargarSesiones = async () => {
    setLoading(true);
    try {
      const response = await sesionesApi.listarPorUsuario(usuarioId);
      setSesiones(response.data);
    } catch {
      toast.error('Error al cargar sesiones');
    } finally {
      setLoading(false);
    }
  };

  const invalidarTodasLasSesiones = async () => {
    setInvalidating(true);
    try {
      await sesionesApi.invalidarTodas(usuarioId);
      toast.success('Todas las sesiones han sido invalidadas');
      setSesiones([]);
      setShowInvalidateAllDialog(false);
    } catch {
      toast.error('Error al invalidar sesiones');
    } finally {
      setInvalidating(false);
    }
  };

  const invalidarSesion = async (sesionId: string) => {
    try {
      await sesionesApi.invalidarSesion(sesionId);
      toast.success('Sesión invalidada');
      setSesiones(sesiones.filter(s => s.id !== sesionId));
    } catch {
      toast.error('Error al invalidar sesión');
    }
  };

  const toggleExpanded = () => {
    if (!expanded) {
      cargarSesiones();
    }
    setExpanded(!expanded);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('es-VE', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div className="flex items-center gap-2">
          <ShieldAlert className="h-5 w-5" />
          <CardTitle>Sesiones de {usuarioNombre}</CardTitle>
        </div>
        <Button variant="outline" size="sm" onClick={toggleExpanded}>
          {expanded ? 'Ocultar' : 'Ver sesiones'}
        </Button>
      </CardHeader>

      {expanded && (
        <CardContent className="space-y-4">
          {loading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-green-600" />
            </div>
          ) : sesiones.length === 0 ? (
            <p className="text-center text-gray-500 py-4">
              No hay sesiones activas para este usuario
            </p>
          ) : (
            <>
              <div className="flex justify-between items-center">
                <p className="text-sm text-gray-500">
                  {sesiones.length} sesión(es) activa(s)
                </p>
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => setShowInvalidateAllDialog(true)}
                >
                  <Trash2 className="h-4 w-4 mr-1" />
                  Invalidar todas
                </Button>
              </div>

              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Dispositivo</TableHead>
                    <TableHead>Ubicación</TableHead>
                    <TableHead>Último acceso</TableHead>
                    <TableHead>Expira</TableHead>
                    <TableHead>Estado</TableHead>
                    <TableHead className="text-right">Acciones</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sesiones.map((sesion) => (
                    <TableRow key={sesion.id}>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Monitor className="h-4 w-4 text-gray-400" />
                          <span className="text-sm">
                            {sesion.userAgent || 'Desconocido'}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Globe className="h-4 w-4 text-gray-400" />
                          <span className="text-sm">
                            {sesion.ipAddress || 'N/A'}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className="text-sm">
                        {formatDate(sesion.ultimoAcceso)}
                      </TableCell>
                      <TableCell className="text-sm">
                        {formatDate(sesion.expiraAt)}
                      </TableCell>
                      <TableCell>
                        <Badge variant={sesion.activa ? 'default' : 'secondary'}>
                          {sesion.activa ? 'Activa' : 'Inactiva'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setSelectedSesionId(sesion.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </>
          )}
        </CardContent>
      )}

      <AlertDialog open={showInvalidateAllDialog} onOpenChange={setShowInvalidateAllDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Invalidar todas las sesiones?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción invalidará todas las sesiones activas de {usuarioNombre}. 
              El usuario tendrá que iniciar sesión nuevamente en todos sus dispositivos.
              Se enviará una notificación por email al usuario.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={invalidarTodasLasSesiones}
              disabled={invalidating}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {invalidating && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Invalidar todas
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={!!selectedSesionId} onOpenChange={() => setSelectedSesionId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Invalidar esta sesión?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción invalidará la sesión seleccionada. El usuario tendrá que 
              iniciar sesión nuevamente en ese dispositivo.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => selectedSesionId && invalidarSesion(selectedSesionId)}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Invalidar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </Card>
  );
}