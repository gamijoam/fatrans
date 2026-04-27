'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Plus, Pencil, Power, Users, AlertCircle, Shield } from 'lucide-react';
import { toast } from 'sonner';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

interface Usuario {
  id: string;
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  rol: string;
  cuentaActiva: boolean;
  fechaCreacion: string;
  debeCambiarPassword: boolean;
}

interface UsuarioForm {
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  password: string;
  rol: string;
  cuentaActiva: boolean;
}

const emptyForm: UsuarioForm = {
  nombreUsuario: '',
  correoElectronico: '',
  nombreCompleto: '',
  password: '',
  rol: 'ADMIN',
  cuentaActiva: true,
};

export default function AdminUsuariosPage() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);
  const [showDialog, setShowDialog] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<UsuarioForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [filterRol, setFilterRol] = useState<string>('todos');
  const [filterEstado, setFilterEstado] = useState<string>('todos');

  const cargarUsuarios = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/admin/usuarios', {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar usuarios');
      const data = await res.json();
      setUsuarios(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error cargando usuarios:', err);
      toast.error('Error al cargar usuarios');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarUsuarios();
  }, [cargarUsuarios]);

  const filteredUsuarios = usuarios.filter((usuario) => {
    if (filterRol !== 'todos' && usuario.rol !== filterRol) return false;
    if (filterEstado === 'activos' && !usuario.cuentaActiva) return false;
    if (filterEstado === 'inactivos' && usuario.cuentaActiva) return false;
    return true;
  });

  const handleOpenDialog = (usuario?: Usuario) => {
    if (usuario) {
      setEditingId(usuario.id);
      setForm({
        nombreUsuario: usuario.nombreUsuario,
        correoElectronico: usuario.correoElectronico,
        nombreCompleto: usuario.nombreCompleto,
        password: '',
        rol: usuario.rol,
        cuentaActiva: usuario.cuentaActiva,
      });
    } else {
      setEditingId(null);
      setForm(emptyForm);
    }
    setShowDialog(true);
  };

  const handleCloseDialog = () => {
    setShowDialog(false);
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);

    const payload = {
      nombreUsuario: form.nombreUsuario.trim(),
      correoElectronico: form.correoElectronico.trim(),
      nombreCompleto: form.nombreCompleto.trim(),
      password: form.password || undefined,
      rol: form.rol,
      cuentaActiva: form.cuentaActiva,
    };

    try {
      const url = editingId
        ? `/api/admin/usuarios/${editingId}`
        : '/api/admin/usuarios';
      const method = editingId ? 'PUT' : 'POST';

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const error = await res.json();
        throw new Error(error.message || error.error || 'Error al guardar');
      }

      toast.success(editingId ? 'Usuario actualizado' : 'Usuario creado');
      handleCloseDialog();
      cargarUsuarios();
    } catch (err) {
      console.error('Error guardando:', err);
      toast.error(err instanceof Error ? err.message : 'Error al guardar usuario');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleActivo = async (id: string, currentActivo: boolean) => {
    const action = currentActivo ? 'desactivar' : 'activar';

    try {
      const res = await fetch(`/api/admin/usuarios/${id}?action=${action}`, {
        method: 'POST',
        credentials: 'include',
      });

      if (!res.ok) {
        const error = await res.json();
        throw new Error(error.message || error.error || `Error al ${action}`);
      }

      toast.success(
        currentActivo ? 'Usuario desactivado' : 'Usuario activado'
      );
      cargarUsuarios();
    } catch (err) {
      console.error('Error toggling activo:', err);
      toast.error(err instanceof Error ? err.message : `Error al ${action}`);
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return new Date(dateStr).toLocaleString('es-VE');
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Gestión de Usuarios</h1>
        <div className="flex items-center gap-3">
          <select
            value={filterRol}
            onChange={(e) => setFilterRol(e.target.value)}
            className="px-3 py-2 border rounded-md text-sm"
          >
            <option value="todos">Todos los roles</option>
            <option value="ADMIN">Admin</option>
            <option value="SUPER_ADMIN">Super Admin</option>
          </select>
          <select
            value={filterEstado}
            onChange={(e) => setFilterEstado(e.target.value)}
            className="px-3 py-2 border rounded-md text-sm"
          >
            <option value="todos">Todos</option>
            <option value="activos">Activos</option>
            <option value="inactivos">Inactivos</option>
          </select>
          <Badge variant="outline" className="text-primary">
            {filteredUsuarios.length} usuarios
          </Badge>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5" />
              Usuarios del Sistema
            </CardTitle>
            <Button onClick={() => handleOpenDialog()}>
              <Plus className="h-4 w-4 mr-2" />
              Nuevo Usuario
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : filteredUsuarios.length === 0 ? (
            <div className="text-center py-12">
              <AlertCircle className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay usuarios registrados</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Usuario</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Rol</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Fecha Creación</TableHead>
                  <TableHead className="text-right">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredUsuarios.map((usuario) => (
                  <TableRow key={usuario.id}>
                    <TableCell>
                      <div>
                        <p className="font-medium">{usuario.nombreCompleto}</p>
                        <p className="text-xs text-gray-500">@{usuario.nombreUsuario}</p>
                      </div>
                    </TableCell>
                    <TableCell>{usuario.correoElectronico}</TableCell>
                    <TableCell>
                      <Badge
                        className={
                          usuario.rol === 'SUPER_ADMIN'
                            ? 'bg-purple-100 text-purple-800'
                            : 'bg-blue-100 text-blue-800'
                        }
                      >
                        <Shield className="h-3 w-3 mr-1" />
                        {usuario.rol}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {usuario.cuentaActiva ? (
                        <Badge className="bg-green-100 text-green-800">Activo</Badge>
                      ) : (
                        <Badge variant="outline" className="text-red-600">
                          Inactivo
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>{formatDate(usuario.fechaCreacion)}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleOpenDialog(usuario)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant={usuario.cuentaActiva ? 'destructive' : 'default'}
                          onClick={() => handleToggleActivo(usuario.id, usuario.cuentaActiva)}
                          disabled={usuario.rol === 'SUPER_ADMIN' && usuario.cuentaActiva}
                        >
                          <Power className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog open={showDialog} onOpenChange={setShowDialog}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>
              {editingId ? 'Editar Usuario' : 'Nuevo Usuario'}
            </DialogTitle>
            <DialogDescription>
              Complete todos los campos requeridos para{' '}
              {editingId ? 'actualizar' : 'crear'} un usuario.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="nombreUsuario">Nombre de Usuario *</Label>
              <Input
                id="nombreUsuario"
                value={form.nombreUsuario}
                onChange={(e) => setForm({ ...form, nombreUsuario: e.target.value })}
                placeholder="admin01"
                required
                disabled={!!editingId}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="correoElectronico">Correo Electrónico *</Label>
              <Input
                id="correoElectronico"
                type="email"
                value={form.correoElectronico}
                onChange={(e) => setForm({ ...form, correoElectronico: e.target.value })}
                placeholder="admin@tufondo.com"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="nombreCompleto">Nombre Completo *</Label>
              <Input
                id="nombreCompleto"
                value={form.nombreCompleto}
                onChange={(e) => setForm({ ...form, nombreCompleto: e.target.value })}
                placeholder="Administrador del Sistema"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">
                Contraseña {editingId ? '(dejar vacío para no cambiar)' : '*'}
              </Label>
              <Input
                id="password"
                type="password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                placeholder={editingId ? '••••••••' : 'Mínimo 8 caracteres'}
                required={!editingId}
                minLength={8}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="rol">Rol *</Label>
              <Select
                value={form.rol}
                onValueChange={(value) => setForm({ ...form, rol: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione un rol" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ADMIN">Administrador</SelectItem>
                  <SelectItem value="SUPER_ADMIN">Super Administrador</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={handleCloseDialog}
                disabled={saving}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={saving}>
                {saving && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                {editingId ? 'Actualizar' : 'Crear'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}