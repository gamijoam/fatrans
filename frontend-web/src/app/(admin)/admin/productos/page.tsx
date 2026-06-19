"use client";

import { useEffect, useState } from "react";
import {
  productosApi,
  creditosApi,
  resolveApiAssetUrl,
} from "@/lib/api/client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  History,
  ImageIcon,
  Pencil,
  PackageOpen,
  Pause,
  Plus,
  Send,
  Star,
  Trash2,
  Upload,
  X,
} from "lucide-react";

interface ProductoImagen {
  id: number;
  imagenUrl: string;
  esPrincipal: boolean;
  orden: number;
}

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
  imagenUrl?: string;
  imagenes?: ProductoImagen[];
  requiereAprobacionManual?: boolean;
  estado: string;
}

interface ProductoHistorialCambio {
  id: number;
  tipoEvento: string;
  campo?: string | null;
  valorAnterior?: string | null;
  valorNuevo?: string | null;
  estadoProducto?: string | null;
  actorId?: string | null;
  createdAt: string;
}

interface TipoCredito {
  id: number;
  nombre: string;
}

const initialForm = {
  codigo: "",
  nombre: "",
  descripcion: "",
  categoria: "REPUESTOS",
  proveedor: "",
  precio: "",
  moneda: "VES",
  tipoCreditoId: "",
  plazoMinimoMeses: "1",
  plazoMaximoMeses: "12",
  porcentajeColateral: "30",
  imagenUrl: "",
  requiereAprobacionManual: true,
};

function formatMoney(value: number, currency: string) {
  const prefix = currency === "USD" ? "USD" : "Bs.";
  return `${prefix} ${Number(value || 0).toLocaleString("es-VE", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function formatHistorialLabel(item: ProductoHistorialCambio) {
  const labels: Record<string, string> = {
    ACTUALIZACION: "Actualizacion",
    CAMBIO_ESTADO: "Cambio de estado",
    CREACION: "Creacion",
    IMAGEN_AGREGADA: "Imagen agregada",
    IMAGEN_ELIMINADA: "Imagen eliminada",
    IMAGEN_PRINCIPAL: "Imagen principal",
  };
  return labels[item.tipoEvento] || item.tipoEvento;
}

export default function AdminProductosPage() {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [tiposCredito, setTiposCredito] = useState<TipoCredito[]>([]);
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploadingId, setUploadingId] = useState<number | null>(null);
  const [editingProduct, setEditingProduct] = useState<Producto | null>(null);
  const [historyProductId, setHistoryProductId] = useState<number | null>(null);
  const [historial, setHistorial] = useState<ProductoHistorialCambio[]>([]);
  const [loadingHistorial, setLoadingHistorial] = useState(false);

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
        setForm((current) => ({
          ...current,
          tipoCreditoId: String(tiposRes.data.tiposCredito[0].id),
        }));
      }
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm({
      ...initialForm,
      tipoCreditoId: tiposCredito[0]?.id ? String(tiposCredito[0].id) : "",
    });
    setEditingProduct(null);
  };

  const cargarProductoEnFormulario = (producto: Producto) => {
    setEditingProduct(producto);
    setForm({
      codigo: producto.codigo,
      nombre: producto.nombre || "",
      descripcion: producto.descripcion || "",
      categoria: producto.categoria || "REPUESTOS",
      proveedor: producto.proveedor || "",
      precio: String(producto.precio ?? ""),
      moneda: producto.moneda || "VES",
      tipoCreditoId: String(producto.tipoCreditoId || ""),
      plazoMinimoMeses: String(producto.plazoMinimoMeses || "1"),
      plazoMaximoMeses: String(producto.plazoMaximoMeses || "12"),
      porcentajeColateral: String(producto.porcentajeColateral ?? "30"),
      imagenUrl: producto.imagenUrl || "",
      requiereAprobacionManual:
        producto.requiereAprobacionManual === undefined
          ? true
          : Boolean(producto.requiereAprobacionManual),
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const buildPayload = () => ({
    ...form,
    imagenUrl: form.imagenUrl.startsWith("/api/v1/productos/imagenes/")
      ? form.imagenUrl
      : "",
    precio: Number(form.precio),
    tipoCreditoId: Number(form.tipoCreditoId),
    plazoMinimoMeses: Number(form.plazoMinimoMeses),
    plazoMaximoMeses: Number(form.plazoMaximoMeses),
    porcentajeColateral: Number(form.porcentajeColateral),
  });

  const submit = async () => {
    setSaving(true);
    try {
      if (editingProduct) {
        await productosApi.actualizar(editingProduct.id, buildPayload());
      } else {
        await productosApi.crear(buildPayload());
      }
      resetForm();
      await cargarDatos();
      if (editingProduct && historyProductId === editingProduct.id) {
        await cargarHistorial(editingProduct.id);
      }
    } finally {
      setSaving(false);
    }
  };

  const cambiarEstado = async (
    producto: Producto,
    action: "publicar" | "pausar" | "archivar",
  ) => {
    await productosApi[action](producto.id);
    await cargarDatos();
    if (historyProductId === producto.id) {
      await cargarHistorial(producto.id);
    }
  };

  const cargarHistorial = async (productoId: number) => {
    setHistoryProductId(productoId);
    setLoadingHistorial(true);
    try {
      const response = await productosApi.getHistorial(productoId);
      setHistorial(response.data.historial || []);
    } finally {
      setLoadingHistorial(false);
    }
  };

  const toggleHistorial = async (productoId: number) => {
    if (historyProductId === productoId) {
      setHistoryProductId(null);
      setHistorial([]);
      return;
    }
    await cargarHistorial(productoId);
  };

  const subirImagen = async (producto: Producto, file?: File) => {
    if (!file) return;
    setUploadingId(producto.id);
    try {
      const response = await productosApi.agregarImagen(producto.id, file);
      setProductos((current) =>
        current.map((item) =>
          item.id === producto.id ? { ...item, ...response.data } : item,
        ),
      );
      if (historyProductId === producto.id) {
        await cargarHistorial(producto.id);
      }
    } finally {
      setUploadingId(null);
    }
  };

  const marcarPrincipal = async (
    producto: Producto,
    imagen: ProductoImagen,
  ) => {
    const response = await productosApi.marcarImagenPrincipal(
      producto.id,
      imagen.id,
    );
    setProductos((current) =>
      current.map((item) =>
        item.id === producto.id ? { ...item, ...response.data } : item,
      ),
    );
    if (historyProductId === producto.id) {
      await cargarHistorial(producto.id);
    }
  };

  const eliminarImagen = async (producto: Producto, imagen: ProductoImagen) => {
    const response = await productosApi.eliminarImagen(producto.id, imagen.id);
    setProductos((current) =>
      current.map((item) =>
        item.id === producto.id ? { ...item, ...response.data } : item,
      ),
    );
    if (historyProductId === producto.id) {
      await cargarHistorial(producto.id);
    }
  };

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div>
        <p className="text-sm font-medium text-[#16A34A]">
          Catálogo financiero
        </p>
        <h1 className="text-2xl font-bold text-[#0F2744]">
          Productos financiables
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          Publica beneficios para socios con precio, plazo, tipo de crédito y
          colateral requerido.
        </p>
      </div>

      <div className="grid gap-6 xl:grid-cols-[420px_1fr]">
        <Card className="border-slate-200">
          <CardContent className="p-5 space-y-4">
            <div className="flex items-center gap-2">
              {editingProduct ? (
                <Pencil className="h-5 w-5 text-[#16A34A]" />
              ) : (
                <Plus className="h-5 w-5 text-[#16A34A]" />
              )}
              <h2 className="font-semibold text-slate-950">
                {editingProduct ? "Editar producto" : "Nuevo producto"}
              </h2>
            </div>
            {editingProduct?.estado === "PUBLICADO" && (
              <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-xs leading-5 text-amber-800">
                Los cambios aplican al catálogo y a nuevas solicitudes. Las
                solicitudes ya creadas conservan sus datos históricos.
              </div>
            )}

            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <Label>Código</Label>
                <Input
                  value={form.codigo}
                  onChange={(e) => setForm({ ...form, codigo: e.target.value })}
                  placeholder="CAUCHO-001"
                  disabled={Boolean(editingProduct)}
                />
              </div>
              <div>
                <Label>Categoría</Label>
                <Input
                  value={form.categoria}
                  onChange={(e) =>
                    setForm({ ...form, categoria: e.target.value })
                  }
                />
              </div>
            </div>

            <div>
              <Label>Nombre</Label>
              <Input
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                placeholder="Kit de cauchos"
              />
            </div>

            <div>
              <Label>Descripción</Label>
              <Textarea
                value={form.descripcion}
                onChange={(e) =>
                  setForm({ ...form, descripcion: e.target.value })
                }
                rows={3}
              />
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <Label>Precio</Label>
                <Input
                  type="number"
                  value={form.precio}
                  onChange={(e) => setForm({ ...form, precio: e.target.value })}
                />
              </div>
              <div>
                <Label>Moneda</Label>
                <select
                  className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                  value={form.moneda}
                  onChange={(e) => setForm({ ...form, moneda: e.target.value })}
                >
                  <option value="VES">VES</option>
                  <option value="USD">USD</option>
                </select>
              </div>
            </div>

            <div>
              <Label>Tipo de crédito</Label>
              <select
                className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                value={form.tipoCreditoId}
                onChange={(e) =>
                  setForm({ ...form, tipoCreditoId: e.target.value })
                }
              >
                {tiposCredito.map((tipo) => (
                  <option key={tipo.id} value={tipo.id}>
                    {tipo.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              <div>
                <Label>Plazo mín.</Label>
                <Input
                  type="number"
                  value={form.plazoMinimoMeses}
                  onChange={(e) =>
                    setForm({ ...form, plazoMinimoMeses: e.target.value })
                  }
                />
              </div>
              <div>
                <Label>Plazo máx.</Label>
                <Input
                  type="number"
                  value={form.plazoMaximoMeses}
                  onChange={(e) =>
                    setForm({ ...form, plazoMaximoMeses: e.target.value })
                  }
                />
              </div>
              <div>
                <Label>Colateral %</Label>
                <Input
                  type="number"
                  value={form.porcentajeColateral}
                  onChange={(e) =>
                    setForm({ ...form, porcentajeColateral: e.target.value })
                  }
                />
              </div>
            </div>

            <label className="flex items-start gap-3 rounded-md border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
              <input
                type="checkbox"
                checked={form.requiereAprobacionManual}
                onChange={(event) =>
                  setForm({
                    ...form,
                    requiereAprobacionManual: event.target.checked,
                  })
                }
                className="mt-1"
              />
              <span>
                Requiere aprobación manual
                <span className="block text-xs text-slate-500">
                  Mantiene revisión administrativa antes del desembolso.
                </span>
              </span>
            </label>

            <Button
              className="w-full bg-[#16A34A] hover:bg-[#15803D]"
              onClick={submit}
              disabled={
                saving ||
                !form.codigo ||
                !form.nombre ||
                !form.precio ||
                !form.tipoCreditoId
              }
            >
              {saving
                ? "Guardando..."
                : editingProduct
                  ? "Guardar cambios"
                  : "Crear como borrador"}
            </Button>
            {editingProduct && (
              <Button
                type="button"
                variant="outline"
                className="w-full"
                onClick={resetForm}
                disabled={saving}
              >
                <X className="mr-2 h-4 w-4" />
                Cancelar edición
              </Button>
            )}
          </CardContent>
        </Card>

        <div className="space-y-3">
          {loading ? (
            <div className="rounded-lg border border-slate-200 bg-white p-8 text-sm text-slate-500">
              Cargando productos...
            </div>
          ) : productos.length === 0 ? (
            <div className="rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center">
              <PackageOpen className="mx-auto h-10 w-10 text-slate-400" />
              <h2 className="mt-3 font-semibold text-slate-950">
                Aún no hay productos
              </h2>
            </div>
          ) : (
            productos.map((producto) => (
              <Card
                key={producto.id}
                className="overflow-hidden border-slate-200"
              >
                <CardContent className="p-5">
                  <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                    <div className="flex min-w-0 flex-1 gap-4">
                      <div className="relative h-24 w-32 shrink-0 overflow-hidden rounded-lg border border-slate-200 bg-slate-50">
                        {producto.imagenUrl ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={resolveApiAssetUrl(producto.imagenUrl)}
                            alt={producto.nombre}
                            className="h-full w-full object-cover"
                          />
                        ) : (
                          <div className="flex h-full w-full items-center justify-center bg-slate-100">
                            <ImageIcon className="h-8 w-8 text-slate-400" />
                          </div>
                        )}
                      </div>
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <h2 className="font-semibold text-slate-950">
                            {producto.nombre}
                          </h2>
                          <Badge
                            variant={
                              producto.estado === "PUBLICADO"
                                ? "default"
                                : "secondary"
                            }
                          >
                            {producto.estado}
                          </Badge>
                          <Badge variant="outline">{producto.categoria}</Badge>
                        </div>
                        <p className="mt-1 line-clamp-2 text-sm text-slate-600">
                          {producto.descripcion || "Sin descripción"}
                        </p>
                        <div className="mt-3 flex flex-wrap gap-4 text-sm text-slate-600">
                          <span>
                            Precio:{" "}
                            <strong className="text-[#0F2744]">
                              {formatMoney(producto.precio, producto.moneda)}
                            </strong>
                          </span>
                          <span>
                            Colateral:{" "}
                            <strong className="text-[#0F2744]">
                              {producto.porcentajeColateral}%
                            </strong>
                          </span>
                          <span>
                            Plazo:{" "}
                            <strong className="text-[#0F2744]">
                              {producto.plazoMinimoMeses}-
                              {producto.plazoMaximoMeses} meses
                            </strong>
                          </span>
                        </div>
                        <label className="mt-3 inline-flex h-9 cursor-pointer items-center gap-2 rounded-md border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50">
                          <Upload className="h-4 w-4" />
                          {uploadingId === producto.id
                            ? "Subiendo..."
                            : producto.imagenUrl
                              ? "Agregar foto"
                              : "Subir foto"}
                          <input
                            type="file"
                            accept="image/jpeg,image/png"
                            className="sr-only"
                            disabled={uploadingId === producto.id}
                            onChange={(event) => {
                              const file = event.target.files?.[0];
                              void subirImagen(producto, file);
                              event.target.value = "";
                            }}
                          />
                        </label>
                        <p className="mt-1 text-xs text-slate-500">
                          JPG o PNG, máximo 2 MB. Hasta 5 fotos por producto.
                        </p>
                        {producto.imagenes && producto.imagenes.length > 0 && (
                          <div className="mt-3 flex flex-wrap gap-2">
                            {producto.imagenes.map((imagen) => (
                              <div
                                key={imagen.id}
                                className="group relative h-16 w-20 overflow-hidden rounded-md border border-slate-200 bg-slate-50"
                              >
                                {/* eslint-disable-next-line @next/next/no-img-element */}
                                <img
                                  src={resolveApiAssetUrl(imagen.imagenUrl)}
                                  alt={`${producto.nombre} ${imagen.orden + 1}`}
                                  className="h-full w-full object-cover"
                                />
                                {imagen.esPrincipal && (
                                  <span className="absolute left-1 top-1 rounded bg-white px-1.5 py-0.5 text-[10px] font-semibold text-[#0F2744] shadow-sm">
                                    Principal
                                  </span>
                                )}
                                <div className="absolute inset-x-1 bottom-1 flex justify-end gap-1 opacity-0 transition-opacity group-hover:opacity-100 group-focus-within:opacity-100">
                                  {!imagen.esPrincipal && (
                                    <button
                                      type="button"
                                      aria-label="Marcar como principal"
                                      className="rounded bg-white p-1 text-[#0F2744] shadow-sm hover:bg-slate-100"
                                      onClick={() =>
                                        void marcarPrincipal(producto, imagen)
                                      }
                                    >
                                      <Star className="h-3.5 w-3.5" />
                                    </button>
                                  )}
                                  <button
                                    type="button"
                                    aria-label="Eliminar imagen"
                                    className="rounded bg-white p-1 text-red-600 shadow-sm hover:bg-red-50"
                                    onClick={() =>
                                      void eliminarImagen(producto, imagen)
                                    }
                                  >
                                    <Trash2 className="h-3.5 w-3.5" />
                                  </button>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="flex shrink-0 flex-wrap gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => cargarProductoEnFormulario(producto)}
                      >
                        <Pencil className="mr-2 h-4 w-4" />
                        Editar
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => void toggleHistorial(producto.id)}
                      >
                        <History className="mr-2 h-4 w-4" />
                        Historial
                      </Button>
                      {producto.estado !== "PUBLICADO" ? (
                        <Button
                          size="sm"
                          className="bg-[#16A34A] hover:bg-[#15803D]"
                          onClick={() => cambiarEstado(producto, "publicar")}
                        >
                          <Send className="mr-2 h-4 w-4" />
                          Publicar
                        </Button>
                      ) : (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => cambiarEstado(producto, "pausar")}
                        >
                          <Pause className="mr-2 h-4 w-4" />
                          Pausar
                        </Button>
                      )}
                    </div>
                  </div>
                  {historyProductId === producto.id && (
                    <div className="mt-5 rounded-md border border-slate-200 bg-slate-50 p-4">
                      <div className="mb-3 flex items-center justify-between gap-3">
                        <div>
                          <h3 className="text-sm font-semibold text-slate-950">
                            Historial de cambios
                          </h3>
                          <p className="text-xs text-slate-500">
                            Registro operativo del catalogo y sus imagenes.
                          </p>
                        </div>
                        <Button
                          type="button"
                          size="sm"
                          variant="ghost"
                          onClick={() => {
                            setHistoryProductId(null);
                            setHistorial([]);
                          }}
                        >
                          <X className="h-4 w-4" />
                        </Button>
                      </div>
                      {loadingHistorial ? (
                        <div className="py-6 text-sm text-slate-500">
                          Cargando historial...
                        </div>
                      ) : historial.length === 0 ? (
                        <div className="py-6 text-sm text-slate-500">
                          Este producto aun no tiene cambios registrados.
                        </div>
                      ) : (
                        <div className="max-h-72 space-y-2 overflow-y-auto pr-1">
                          {historial.map((item) => (
                            <div
                              key={item.id}
                              className="rounded-md border border-slate-200 bg-white p-3"
                            >
                              <div className="flex flex-wrap items-center gap-2">
                                <Badge variant="outline">
                                  {formatHistorialLabel(item)}
                                </Badge>
                                {item.campo && (
                                  <span className="text-xs font-medium text-slate-600">
                                    {item.campo}
                                  </span>
                                )}
                                {item.estadoProducto && (
                                  <span className="text-xs text-slate-500">
                                    Estado: {item.estadoProducto}
                                  </span>
                                )}
                                <span className="ml-auto text-xs text-slate-500">
                                  {new Date(item.createdAt).toLocaleString(
                                    "es-VE",
                                  )}
                                </span>
                              </div>
                              {(item.valorAnterior || item.valorNuevo) && (
                                <div className="mt-2 grid gap-2 text-xs text-slate-600 md:grid-cols-2">
                                  <div className="min-w-0 rounded bg-slate-100 px-2 py-1">
                                    <span className="font-medium">
                                      Anterior:
                                    </span>{" "}
                                    <span className="break-words">
                                      {item.valorAnterior || "Sin valor"}
                                    </span>
                                  </div>
                                  <div className="min-w-0 rounded bg-emerald-50 px-2 py-1 text-emerald-900">
                                    <span className="font-medium">Nuevo:</span>{" "}
                                    <span className="break-words">
                                      {item.valorNuevo || "Sin valor"}
                                    </span>
                                  </div>
                                </div>
                              )}
                              {item.actorId && (
                                <p className="mt-2 truncate text-xs text-slate-400">
                                  Admin: {item.actorId}
                                </p>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
