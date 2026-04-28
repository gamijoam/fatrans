'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Loader2, Plus, Pencil, Power, CreditCard, AlertCircle, CheckCircle } from 'lucide-react';
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

interface TipoCredito {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  tasaInteresAnual: number;
  plazoMinimoMeses: number;
  plazoMaximoMeses: number;
  montoMinimo: number;
  montoMaximo: number;
  porcentajeRequerimientoColateral: number | null;
  comisionApertura: number | null;
  penalidadMoraTasa: number | null;
  diasGracia: number | null;
  activo: boolean;
}

interface TipoCreditoForm {
  codigo: string;
  nombre: string;
  descripcion: string;
  tasaInteresAnual: string;
  plazoMinimoMeses: string;
  plazoMaximoMeses: string;
  montoMinimo: string;
  montoMaximo: string;
  porcentajeRequerimientoColateral: string;
  comisionApertura: string;
  penalidadMoraTasa: string;
  diasGracia: string;
}

const emptyForm: TipoCreditoForm = {
  codigo: '',
  nombre: '',
  descripcion: '',
  tasaInteresAnual: '',
  plazoMinimoMeses: '',
  plazoMaximoMeses: '',
  montoMinimo: '',
  montoMaximo: '',
  porcentajeRequerimientoColateral: '',
  comisionApertura: '',
  penalidadMoraTasa: '',
  diasGracia: '',
};

export default function AdminTiposCreditoPage() {
  const [tiposCredito, setTiposCredito] = useState<TipoCredito[]>([]);
  const [loading, setLoading] = useState(true);
  const [showDialog, setShowDialog] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<TipoCreditoForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [filterActivo, setFilterActivo] = useState<string>('todos');

  const cargarTiposCredito = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/admin/tipos-credito', {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar tipos de crédito');
      const data = await res.json();
      setTiposCredito(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error cargando tipos de crédito:', err);
      toast.error('Error al cargar tipos de crédito');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarTiposCredito();
  }, [cargarTiposCredito]);

  const filteredTipos = tiposCredito.filter((tipo) => {
    if (filterActivo === 'activos') return tipo.activo;
    if (filterActivo === 'inactivos') return !tipo.activo;
    return true;
  });

  const handleOpenDialog = (tipo?: TipoCredito) => {
    if (tipo) {
      setEditingId(tipo.id);
      setForm({
        codigo: tipo.codigo,
        nombre: tipo.nombre,
        descripcion: tipo.descripcion || '',
        tasaInteresAnual: (tipo.tasaInteresAnual * 100).toString(),
        plazoMinimoMeses: tipo.plazoMinimoMeses.toString(),
        plazoMaximoMeses: tipo.plazoMaximoMeses.toString(),
        montoMinimo: tipo.montoMinimo.toString(),
        montoMaximo: tipo.montoMaximo.toString(),
        porcentajeRequerimientoColateral: tipo.porcentajeRequerimientoColateral
          ? (tipo.porcentajeRequerimientoColateral * 100).toString()
          : '',
        comisionApertura: tipo.comisionApertura
          ? (tipo.comisionApertura * 100).toString()
          : '',
        penalidadMoraTasa: tipo.penalidadMoraTasa
          ? (tipo.penalidadMoraTasa * 100).toString()
          : '',
        diasGracia: tipo.diasGracia?.toString() || '',
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
      codigo: form.codigo.trim().toUpperCase(),
      nombre: form.nombre.trim(),
      descripcion: form.descripcion.trim() || null,
      tasaInteresAnual: parseFloat(form.tasaInteresAnual) / 100,
      plazoMinimoMeses: parseInt(form.plazoMinimoMeses),
      plazoMaximoMeses: parseInt(form.plazoMaximoMeses),
      montoMinimo: parseFloat(form.montoMinimo),
      montoMaximo: parseFloat(form.montoMaximo),
      porcentajeRequerimientoColateral: form.porcentajeRequerimientoColateral
        ? parseFloat(form.porcentajeRequerimientoColateral) / 100
        : null,
      comisionApertura: form.comisionApertura
        ? parseFloat(form.comisionApertura) / 100
        : null,
      penalidadMoraTasa: form.penalidadMoraTasa
        ? parseFloat(form.penalidadMoraTasa) / 100
        : null,
      diasGracia: form.diasGracia ? parseInt(form.diasGracia) : null,
    };

    try {
      const url = editingId
        ? `/api/admin/tipos-credito/${editingId}`
        : '/api/admin/tipos-credito';
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

      toast.success(editingId ? 'Tipo de crédito actualizado' : 'Tipo de crédito creado');
      handleCloseDialog();
      cargarTiposCredito();
    } catch (err) {
      console.error('Error guardando:', err);
      toast.error(err instanceof Error ? err.message : 'Error al guardar tipo de crédito');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleActivo = async (id: number, currentActivo: boolean) => {
    const action = currentActivo ? 'desactivar' : 'activar';

    try {
      const res = await fetch(`/api/admin/tipos-credito/${id}?action=${action}`, {
        method: 'POST',
        credentials: 'include',
      });

      if (!res.ok) {
        const error = await res.json();
        throw new Error(error.message || error.error || `Error al ${action}`);
      }

      toast.success(
        currentActivo ? 'Tipo de crédito desactivado' : 'Tipo de crédito activado'
      );
      cargarTiposCredito();
    } catch (err) {
      console.error('Error toggling activo:', err);
      toast.error(err instanceof Error ? err.message : `Error al ${action}`);
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('es-VE', {
      style: 'currency',
      currency: 'VES',
      minimumFractionDigits: 2,
    }).format(value);
  };

  const formatPercent = (value: number | null) => {
    if (value === null) return '-';
    return `${(value * 100).toFixed(2)}%`;
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Tipos de Crédito</h1>
        <div className="flex items-center gap-3">
          <select
            value={filterActivo}
            onChange={(e) => setFilterActivo(e.target.value)}
            className="px-3 py-2 border rounded-md text-sm"
          >
            <option value="todos">Todos</option>
            <option value="activos">Activos</option>
            <option value="inactivos">Inactivos</option>
          </select>
          <Badge variant="outline" className="text-primary">
            {filteredTipos.length} tipos
          </Badge>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-5 w-5" />
              Catálogo de Productos Crediticios
            </CardTitle>
            <Button onClick={() => handleOpenDialog()}>
              <Plus className="h-4 w-4 mr-2" />
              Nuevo Tipo
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : filteredTipos.length === 0 ? (
            <div className="text-center py-12">
              <AlertCircle className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay tipos de crédito registrados</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Código</TableHead>
                  <TableHead>Nombre</TableHead>
                  <TableHead>Tasa Anual</TableHead>
                  <TableHead>Plazo</TableHead>
                  <TableHead>Monto</TableHead>
                  <TableHead>Colateral</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead className="text-right">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredTipos.map((tipo) => (
                  <TableRow key={tipo.id}>
                    <TableCell className="font-mono text-sm">{tipo.codigo}</TableCell>
                    <TableCell>
                      <div>
                        <p className="font-medium">{tipo.nombre}</p>
                        {tipo.descripcion && (
                          <p className="text-xs text-gray-500 truncate max-w-xs">
                            {tipo.descripcion}
                          </p>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{formatPercent(tipo.tasaInteresAnual)}</TableCell>
                    <TableCell>
                      {tipo.plazoMinimoMeses}-{tipo.plazoMaximoMeses} meses
                    </TableCell>
                    <TableCell>
                      <div className="text-sm">
                        <p>{formatCurrency(tipo.montoMinimo)}</p>
                        <p className="text-gray-500">- {formatCurrency(tipo.montoMaximo)}</p>
                      </div>
                    </TableCell>
                    <TableCell>{formatPercent(tipo.porcentajeRequerimientoColateral)}</TableCell>
                    <TableCell>
                      {tipo.activo ? (
                        <Badge className="bg-green-100 text-green-800">Activo</Badge>
                      ) : (
                        <Badge variant="outline" className="text-red-600">
                          Inactivo
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleOpenDialog(tipo)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant={tipo.activo ? 'destructive' : 'default'}
                          onClick={() => handleToggleActivo(tipo.id, tipo.activo)}
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
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {editingId ? 'Editar Tipo de Crédito' : 'Nuevo Tipo de Crédito'}
            </DialogTitle>
            <DialogDescription>
              Complete todos los campos requeridos para{' '}
              {editingId ? 'actualizar' : 'crear'} un tipo de crédito.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="codigo">Código *</Label>
                <Input
                  id="codigo"
                  value={form.codigo}
                  onChange={(e) => setForm({ ...form, codigo: e.target.value })}
                  placeholder="MICRO_CREDITO"
                  required
                  disabled={!!editingId}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="nombre">Nombre *</Label>
                <Input
                  id="nombre"
                  value={form.nombre}
                  onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                  placeholder="Micro Crédito"
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="descripcion">Descripción</Label>
              <Textarea
                id="descripcion"
                value={form.descripcion}
                onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                placeholder="Descripción del tipo de crédito..."
                rows={2}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="tasaInteresAnual">Tasa Interés Anual (%) *</Label>
                <Input
                  id="tasaInteresAnual"
                  type="number"
                  step="0.01"
                  min="0.01"
                  max="100"
                  value={form.tasaInteresAnual}
                  onChange={(e) => setForm({ ...form, tasaInteresAnual: e.target.value })}
                  placeholder="24.00"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="diasGracia">Días de Gracia</Label>
                <Input
                  id="diasGracia"
                  type="number"
                  min="0"
                  value={form.diasGracia}
                  onChange={(e) => setForm({ ...form, diasGracia: e.target.value })}
                  placeholder="5"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="plazoMinimoMeses">Plazo Mínimo (meses) *</Label>
                <Input
                  id="plazoMinimoMeses"
                  type="number"
                  min="1"
                  value={form.plazoMinimoMeses}
                  onChange={(e) => setForm({ ...form, plazoMinimoMeses: e.target.value })}
                  placeholder="3"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="plazoMaximoMeses">Plazo Máximo (meses) *</Label>
                <Input
                  id="plazoMaximoMeses"
                  type="number"
                  min="1"
                  value={form.plazoMaximoMeses}
                  onChange={(e) => setForm({ ...form, plazoMaximoMeses: e.target.value })}
                  placeholder="12"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="montoMinimo">Monto Mínimo (VES) *</Label>
                <Input
                  id="montoMinimo"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={form.montoMinimo}
                  onChange={(e) => setForm({ ...form, montoMinimo: e.target.value })}
                  placeholder="100.00"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="montoMaximo">Monto Máximo (VES) *</Label>
                <Input
                  id="montoMaximo"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={form.montoMaximo}
                  onChange={(e) => setForm({ ...form, montoMaximo: e.target.value })}
                  placeholder="5000.00"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="porcentajeRequerimientoColateral">
                  Requerimiento Colateral (%)
                </Label>
                <Input
                  id="porcentajeRequerimientoColateral"
                  type="number"
                  step="0.01"
                  min="0"
                  max="100"
                  value={form.porcentajeRequerimientoColateral}
                  onChange={(e) =>
                    setForm({ ...form, porcentajeRequerimientoColateral: e.target.value })
                  }
                  placeholder="10.00"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="comisionApertura">Comisión Apertura (%)</Label>
                <Input
                  id="comisionApertura"
                  type="number"
                  step="0.01"
                  min="0"
                  max="100"
                  value={form.comisionApertura}
                  onChange={(e) => setForm({ ...form, comisionApertura: e.target.value })}
                  placeholder="1.00"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="penalidadMoraTasa">Penalidad Mora (%)</Label>
                <Input
                  id="penalidadMoraTasa"
                  type="number"
                  step="0.01"
                  min="0"
                  max="100"
                  value={form.penalidadMoraTasa}
                  onChange={(e) => setForm({ ...form, penalidadMoraTasa: e.target.value })}
                  placeholder="2.00"
                />
              </div>
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