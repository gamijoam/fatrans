'use client';

import { useEffect, useState } from 'react';
import { productosApi, creditosApi } from '@/lib/api/client';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { PackageOpen, Pause, Plus, Send } from 'lucide-react';

interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
  categoria: string;
  proveedor?: string;
  precio: number;
  moneda: string;
  tipoCreditoId: number;
  plazoMinimoMeses: number;
  plazoMaximoMeses: number;
  porcentajeColateral: number;
  colateralRequerido: number;
  estado: string;
}

interface TipoCredito {
  id: number;
  nombre: string;
}

const initialForm = {
  codigo: '',
  nombre: '',
  descripcion: '',
  categoria: 'REPUESTOS',
  proveedor: '',
  precio: '',
  moneda: 'VES',
  tipoCreditoId: '',
  plazoMinimoMeses: '1',
  plazoMaximoMeses: '12',
  porcentajeColateral: '30',
  imagenUrl: '',
  requiereAprobacionManual: true,
};

function formatMoney(value: number, currency: string) {
  const prefix = currency === 'USD' ? 'USD' : 'Bs.';
  return `${prefix} ${Number(value || 0).toLocaleString('es-VE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export default function AdminProductosPage() {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [tiposCredito, setTiposCredito] = useState<TipoCredito[]>([]);
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [productosRes, tiposRes] = await Promise.all([
        productosApi.getAdmin(),
        creditosApi.getTiposCredito(),
      ]);
      setProductos(productosRes.data.productos || []);
      setTiposCredito(tiposRes.data.tiposCredito || []);
      if (!form.tipoCreditoId && tiposRes.data.tiposCredito?.[0]?.id) {
        setForm((current) => ({ ...current, tipoCreditoId: String(tiposRes.data.tiposCredito[0].id) }));
      }
    } finally {
      setLoading(false);
    }
  };

  const submit = async () => {
    setSaving(true);
    try {
      await productosApi.crear({
        ...form,
        precio: Number(form.precio),
        tipoCreditoId: Number(form.tipoCreditoId),
        plazoMinimoMeses: Number(form.plazoMinimoMeses),
        plazoMaximoMeses: Number(form.plazoMaximoMeses),
        porcentajeColateral: Number(form.porcentajeColateral),
      });
      setForm(initialForm);
      await cargarDatos();
    } finally {
      setSaving(false);
    }
  };

  const cambiarEstado = async (producto: Producto, action: 'publicar' | 'pausar' | 'archivar') => {
    await productosApi[action](producto.id);
    await cargarDatos();
  };

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div>
        <p className="text-sm font-medium text-[#16A34A]">Catálogo financiero</p>
        <h1 className="text-2xl font-bold text-[#0F2744]">Productos financiables</h1>
        <p className="mt-1 text-sm text-slate-600">
          Publica beneficios para socios con precio, plazo, tipo de crédito y colateral requerido.
        </p>
      </div>

      <div className="grid gap-6 xl:grid-cols-[420px_1fr]">
        <Card className="border-slate-200">
          <CardContent className="p-5 space-y-4">
            <div className="flex items-center gap-2">
              <Plus className="h-5 w-5 text-[#16A34A]" />
              <h2 className="font-semibold text-slate-950">Nuevo producto</h2>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <Label>Código</Label>
                <Input value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} placeholder="CAUCHO-001" />
              </div>
              <div>
                <Label>Categoría</Label>
                <Input value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })} />
              </div>
            </div>

            <div>
              <Label>Nombre</Label>
              <Input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} placeholder="Kit de cauchos" />
            </div>

            <div>
              <Label>Descripción</Label>
              <Textarea value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} rows={3} />
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <Label>Precio</Label>
                <Input type="number" value={form.precio} onChange={(e) => setForm({ ...form, precio: e.target.value })} />
              </div>
              <div>
                <Label>Moneda</Label>
                <select className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm" value={form.moneda} onChange={(e) => setForm({ ...form, moneda: e.target.value })}>
                  <option value="VES">VES</option>
                  <option value="USD">USD</option>
                </select>
              </div>
            </div>

            <div>
              <Label>Tipo de crédito</Label>
              <select className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm" value={form.tipoCreditoId} onChange={(e) => setForm({ ...form, tipoCreditoId: e.target.value })}>
                {tiposCredito.map((tipo) => (
                  <option key={tipo.id} value={tipo.id}>{tipo.nombre}</option>
                ))}
              </select>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              <div>
                <Label>Plazo mín.</Label>
                <Input type="number" value={form.plazoMinimoMeses} onChange={(e) => setForm({ ...form, plazoMinimoMeses: e.target.value })} />
              </div>
              <div>
                <Label>Plazo máx.</Label>
                <Input type="number" value={form.plazoMaximoMeses} onChange={(e) => setForm({ ...form, plazoMaximoMeses: e.target.value })} />
              </div>
              <div>
                <Label>Colateral %</Label>
                <Input type="number" value={form.porcentajeColateral} onChange={(e) => setForm({ ...form, porcentajeColateral: e.target.value })} />
              </div>
            </div>

            <Button className="w-full bg-[#16A34A] hover:bg-[#15803D]" onClick={submit} disabled={saving || !form.codigo || !form.nombre || !form.precio || !form.tipoCreditoId}>
              {saving ? 'Guardando...' : 'Crear como borrador'}
            </Button>
          </CardContent>
        </Card>

        <div className="space-y-3">
          {loading ? (
            <div className="rounded-lg border border-slate-200 bg-white p-8 text-sm text-slate-500">Cargando productos...</div>
          ) : productos.length === 0 ? (
            <div className="rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center">
              <PackageOpen className="mx-auto h-10 w-10 text-slate-400" />
              <h2 className="mt-3 font-semibold text-slate-950">Aún no hay productos</h2>
            </div>
          ) : (
            productos.map((producto) => (
              <Card key={producto.id} className="border-slate-200">
                <CardContent className="p-5">
                  <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <h2 className="font-semibold text-slate-950">{producto.nombre}</h2>
                        <Badge variant={producto.estado === 'PUBLICADO' ? 'default' : 'secondary'}>{producto.estado}</Badge>
                        <Badge variant="outline">{producto.categoria}</Badge>
                      </div>
                      <p className="mt-1 line-clamp-2 text-sm text-slate-600">{producto.descripcion || 'Sin descripción'}</p>
                      <div className="mt-3 flex flex-wrap gap-4 text-sm text-slate-600">
                        <span>Precio: <strong className="text-[#0F2744]">{formatMoney(producto.precio, producto.moneda)}</strong></span>
                        <span>Colateral: <strong className="text-[#0F2744]">{producto.porcentajeColateral}%</strong></span>
                        <span>Plazo: <strong className="text-[#0F2744]">{producto.plazoMinimoMeses}-{producto.plazoMaximoMeses} meses</strong></span>
                      </div>
                    </div>
                    <div className="flex shrink-0 gap-2">
                      {producto.estado !== 'PUBLICADO' ? (
                        <Button size="sm" className="bg-[#16A34A] hover:bg-[#15803D]" onClick={() => cambiarEstado(producto, 'publicar')}>
                          <Send className="mr-2 h-4 w-4" />
                          Publicar
                        </Button>
                      ) : (
                        <Button size="sm" variant="outline" onClick={() => cambiarEstado(producto, 'pausar')}>
                          <Pause className="mr-2 h-4 w-4" />
                          Pausar
                        </Button>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
