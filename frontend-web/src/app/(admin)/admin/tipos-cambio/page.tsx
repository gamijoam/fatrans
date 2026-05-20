'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Plus, Pencil, Trash2, DollarSign, TrendingUp, TrendingDown, AlertCircle, RefreshCw } from 'lucide-react';
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

interface TipoCambio {
  id: string;
  fecha: string;
  tasaCompra: number;
  tasaVenta: number;
  fuente: string | null;
  variacionPorcentual: number | null;
}

interface TipoCambioForm {
  fecha: string;
  tasaCompra: string;
  tasaVenta: string;
  fuente: string;
}

const emptyForm: TipoCambioForm = {
  fecha: '',
  tasaCompra: '',
  tasaVenta: '',
  fuente: '',
};

export default function AdminTiposCambioPage() {
  const [tiposCambio, setTiposCambio] = useState<TipoCambio[]>([]);
  const [loading, setLoading] = useState(true);
  const [showDialog, setShowDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [form, setForm] = useState<TipoCambioForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [syncingBcv, setSyncingBcv] = useState(false);

  const cargarTiposCambio = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/admin/tipos-cambio', {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar tipos de cambio');
      const data = await res.json();
      setTiposCambio(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error cargando tipos de cambio:', err);
      toast.error('Error al cargar tipos de cambio');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarTiposCambio();
  }, [cargarTiposCambio]);

  /**
   * Dispara una sincronización inmediata del scraper BCV. Útil cuando el cron
   * automático no ha corrido todavía (deploy reciente, error de red previo) y
   * el admin necesita la tasa actualizada YA. El backend es idempotente: si la
   * tasa para la fecha valor ya existe, no la duplica.
   */
  const handleSyncBcv = async () => {
    setSyncingBcv(true);
    try {
      const res = await fetch('/api/admin/tipos-cambio/sync-bcv', {
        method: 'POST',
        credentials: 'include',
        cache: 'no-store',
      });
      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        toast.error(data.mensaje || data.message || 'No pudimos sincronizar con el BCV', {
          description: data.error || `HTTP ${res.status}`,
        });
        return;
      }

      if (data.insertada) {
        toast.success('Tasa BCV actualizada', {
          description: `Fecha ${data.fecha}: Bs ${data.tasa}`,
        });
      } else {
        toast.info('Sin cambios', {
          description: `La tasa de ${data.fecha} ya estaba en BD (Bs ${data.tasa}).`,
        });
      }
      await cargarTiposCambio();
    } catch (err) {
      console.error('Error syncing BCV:', err);
      toast.error('Error de red al sincronizar BCV');
    } finally {
      setSyncingBcv(false);
    }
  };

  const handleOpenDialog = (tc?: TipoCambio) => {
    if (tc) {
      setEditingId(tc.id);
      setForm({
        fecha: tc.fecha,
        tasaCompra: tc.tasaCompra.toString(),
        tasaVenta: tc.tasaVenta.toString(),
        fuente: tc.fuente || '',
      });
    } else {
      const today = new Date().toISOString().split('T')[0];
      setEditingId(null);
      setForm({ ...emptyForm, fecha: today });
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
      fecha: form.fecha,
      tasaCompra: parseFloat(form.tasaCompra),
      tasaVenta: parseFloat(form.tasaVenta),
      fuente: form.fuente.trim() || null,
    };

    try {
      const url = editingId
        ? `/api/admin/tipos-cambio?id=${editingId}`
        : '/api/admin/tipos-cambio';
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

      toast.success(editingId ? 'Tipo de cambio actualizado' : 'Tipo de cambio creado');
      handleCloseDialog();
      cargarTiposCambio();
    } catch (err) {
      console.error('Error guardando:', err);
      toast.error(err instanceof Error ? err.message : 'Error al guardar tipo de cambio');
    } finally {
      setSaving(false);
    }
  };

  const handleOpenDeleteDialog = (id: string) => {
    setDeletingId(id);
    setShowDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setShowDeleteDialog(false);
    setDeletingId(null);
  };

  const handleDelete = async () => {
    if (!deletingId) return;
    setDeleting(true);

    try {
      const res = await fetch(`/api/admin/tipos-cambio?id=${deletingId}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!res.ok) {
        const error = await res.json();
        throw new Error(error.message || error.error || 'Error al eliminar');
      }

      toast.success('Tipo de cambio eliminado');
      handleCloseDeleteDialog();
      cargarTiposCambio();
    } catch (err) {
      console.error('Error eliminando:', err);
      toast.error(err instanceof Error ? err.message : 'Error al eliminar tipo de cambio');
    } finally {
      setDeleting(false);
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return new Date(dateStr).toLocaleDateString('es-VE');
    } catch {
      return dateStr;
    }
  };

  const formatTasa = (value: number) => {
    return new Intl.NumberFormat('es-VE', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 6,
    }).format(value);
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Tipos de Cambio</h1>
        <Badge variant="outline" className="text-primary">
          {tiposCambio.length} registros
        </Badge>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <DollarSign className="h-5 w-5" />
              Tasas VES/USD
            </CardTitle>
            <div className="flex gap-2">
              <Button
                onClick={handleSyncBcv}
                disabled={syncingBcv}
                variant="outline"
                title="Forzar consulta inmediata al BCV (sin esperar al cron)"
              >
                {syncingBcv ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <RefreshCw className="h-4 w-4 mr-2" />
                )}
                Sincronizar BCV ahora
              </Button>
              <Button onClick={() => handleOpenDialog()}>
                <Plus className="h-4 w-4 mr-2" />
                Nueva Tasa
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : tiposCambio.length === 0 ? (
            <div className="text-center py-12">
              <AlertCircle className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay tipos de cambio registrados</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Fecha</TableHead>
                  <TableHead className="text-right">Tasa Compra</TableHead>
                  <TableHead className="text-right">Tasa Venta</TableHead>
                  <TableHead className="text-right">Variación</TableHead>
                  <TableHead>Fuente</TableHead>
                  <TableHead className="text-right">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tiposCambio.map((tc) => {
                  const isPositive = tc.variacionPorcentual !== null && tc.variacionPorcentual >= 0;
                  return (
                    <TableRow key={tc.id}>
                      <TableCell className="font-medium">
                        {formatDate(tc.fecha)}
                      </TableCell>
                      <TableCell className="text-right font-mono">
                        Bs {formatTasa(tc.tasaCompra)}
                      </TableCell>
                      <TableCell className="text-right font-mono font-bold">
                        Bs {formatTasa(tc.tasaVenta)}
                      </TableCell>
                      <TableCell className="text-right">
                        {tc.variacionPorcentual !== null ? (
                          <span className={`flex items-center justify-end gap-1 ${isPositive ? 'text-green-600' : 'text-red-600'}`}>
                            {isPositive ? (
                              <TrendingUp className="h-3 w-3" />
                            ) : (
                              <TrendingDown className="h-3 w-3" />
                            )}
                            {isPositive ? '+' : ''}{tc.variacionPorcentual.toFixed(2)}%
                          </span>
                        ) : (
                          '-'
                        )}
                      </TableCell>
                      <TableCell>{tc.fuente || '-'}</TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleOpenDialog(tc)}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            size="sm"
                            variant="destructive"
                            onClick={() => handleOpenDeleteDialog(tc.id)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog open={showDialog} onOpenChange={setShowDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingId ? 'Editar Tipo de Cambio' : 'Nuevo Tipo de Cambio'}
            </DialogTitle>
            <DialogDescription>
              Ingrese la fecha y las tasas de cambio.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="fecha">Fecha *</Label>
              <Input
                id="fecha"
                type="date"
                value={form.fecha}
                onChange={(e) => setForm({ ...form, fecha: e.target.value })}
                required
                disabled={!!editingId}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="tasaCompra">Tasa Compra (Bs por USD) *</Label>
                <Input
                  id="tasaCompra"
                  type="number"
                  step="0.000001"
                  min="0.000001"
                  value={form.tasaCompra}
                  onChange={(e) => setForm({ ...form, tasaCompra: e.target.value })}
                  placeholder="44.000000"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="tasaVenta">Tasa Venta (Bs por USD) *</Label>
                <Input
                  id="tasaVenta"
                  type="number"
                  step="0.000001"
                  min="0.000001"
                  value={form.tasaVenta}
                  onChange={(e) => setForm({ ...form, tasaVenta: e.target.value })}
                  placeholder="45.000000"
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="fuente">Fuente</Label>
              <Input
                id="fuente"
                value={form.fuente}
                onChange={(e) => setForm({ ...form, fuente: e.target.value })}
                placeholder="BCV, Mercado paralelo, etc."
              />
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

      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Eliminar Tipo de Cambio</DialogTitle>
            <DialogDescription>
              ¿Está seguro de que desea eliminar este registro de tipo de cambio? Esta acción no se puede deshacer.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={handleCloseDeleteDialog}
              disabled={deleting}
            >
              Cancelar
            </Button>
            <Button
              variant="destructive"
              onClick={handleDelete}
              disabled={deleting}
            >
              {deleting && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Eliminar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}