"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { productosApi, resolveApiAssetUrl } from "@/lib/api/client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { PackageOpen, ShieldCheck, WalletCards } from "lucide-react";

interface ProductoImagen {
  id: number;
  imagenUrl: string;
  esPrincipal: boolean;
  orden: number;
}

interface Producto {
  id: number;
  slug: string;
  nombre: string;
  descripcion?: string;
  categoria: string;
  proveedor?: string;
  precio: number;
  moneda: string;
  imagenUrl?: string;
  imagenes?: ProductoImagen[];
  plazoMinimoMeses: number;
  plazoMaximoMeses: number;
  porcentajeColateral: number;
  colateralRequerido: number;
}

interface Precalificacion {
  productoId: number;
  elegible: boolean;
  saldoDisponible: number;
  colateralRequerido: number;
  montoFaltante: number;
  mensaje: string;
}

function formatMoney(value: number, currency: string) {
  const prefix = currency === "USD" ? "USD" : "Bs.";
  return `${prefix} ${Number(value || 0).toLocaleString("es-VE", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export default function ProductosSocioPage() {
  const router = useRouter();
  const [productos, setProductos] = useState<Producto[]>([]);
  const [precalificaciones, setPrecalificaciones] = useState<
    Record<number, Precalificacion>
  >({});
  const [loading, setLoading] = useState(true);
  const [solicitandoId, setSolicitandoId] = useState<number | null>(null);

  useEffect(() => {
    cargarProductos();
  }, []);

  const cargarProductos = async () => {
    setLoading(true);
    try {
      const res = await productosApi.getPublicados();
      const items = res.data.productos || [];
      setProductos(items);
      const resultados = await Promise.allSettled(
        items.map((producto: Producto) =>
          productosApi.precalificar(producto.id),
        ),
      );
      const mapa: Record<number, Precalificacion> = {};
      resultados.forEach((resultado, index) => {
        if (resultado.status === "fulfilled") {
          mapa[items[index].id] = resultado.value.data;
        }
      });
      setPrecalificaciones(mapa);
    } finally {
      setLoading(false);
    }
  };

  const solicitar = async (productoId: number) => {
    setSolicitandoId(productoId);
    try {
      const res = await productosApi.solicitarFinanciamiento(productoId);
      router.push(`/dashboard/creditos/${res.data.numeroSolicitud}`);
    } finally {
      setSolicitandoId(null);
    }
  };

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div className="flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="text-sm font-medium text-[#16A34A]">
            Beneficios financiables
          </p>
          <h1 className="text-2xl font-bold text-[#0F2744]">
            Productos para socios
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            Consulta productos disponibles y solicita financiamiento sujeto a
            colateral y aprobación.
          </p>
        </div>
        <Button variant="outline" onClick={cargarProductos} disabled={loading}>
          Actualizar
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {loading ? (
          Array.from({ length: 3 }).map((_, index) => (
            <Card key={index} className="border-slate-200">
              <CardContent className="p-5 space-y-4">
                <div className="h-32 rounded-md bg-slate-200 animate-pulse" />
                <div className="h-5 w-2/3 rounded bg-slate-200 animate-pulse" />
                <div className="h-4 w-full rounded bg-slate-100 animate-pulse" />
              </CardContent>
            </Card>
          ))
        ) : productos.length === 0 ? (
          <div className="md:col-span-2 xl:col-span-3 rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center">
            <PackageOpen className="mx-auto h-10 w-10 text-slate-400" />
            <h2 className="mt-3 text-lg font-semibold text-slate-900">
              No hay productos publicados
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Cuando la asociación publique beneficios aparecerán aquí.
            </p>
          </div>
        ) : (
          productos.map((producto) => {
            const precalificacion = precalificaciones[producto.id];
            const elegible = precalificacion?.elegible;
            const imagenPrincipal =
              producto.imagenes?.find((imagen) => imagen.esPrincipal) ||
              producto.imagenes?.[0];
            const heroUrl = imagenPrincipal?.imagenUrl || producto.imagenUrl;
            return (
              <Card
                key={producto.id}
                className="overflow-hidden border-slate-200"
              >
                <div className="relative h-36 bg-[#0F2744]">
                  {heroUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={resolveApiAssetUrl(heroUrl)}
                      alt={producto.nombre}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center bg-[linear-gradient(135deg,#0F2744,#1D4B77)]">
                      <PackageOpen className="h-12 w-12 text-white/80" />
                    </div>
                  )}
                  <Badge className="absolute left-3 top-3 bg-white text-[#0F2744] hover:bg-white">
                    {producto.categoria}
                  </Badge>
                  {producto.imagenes && producto.imagenes.length > 1 && (
                    <div className="absolute bottom-3 right-3 flex gap-1">
                      {producto.imagenes.slice(0, 4).map((imagen) => (
                        <span
                          key={imagen.id}
                          className={`h-1.5 w-5 rounded-full ${
                            imagen.esPrincipal ? "bg-white" : "bg-white/45"
                          }`}
                        />
                      ))}
                    </div>
                  )}
                </div>
                <CardContent className="p-5 space-y-4">
                  <div>
                    <h2 className="text-lg font-semibold text-slate-950">
                      {producto.nombre}
                    </h2>
                    <p className="mt-1 line-clamp-2 text-sm text-slate-600">
                      {producto.descripcion ||
                        "Producto financiable para socios."}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="rounded-md bg-slate-50 p-3">
                      <p className="text-slate-500">Precio</p>
                      <p className="font-semibold text-[#0F2744]">
                        {formatMoney(producto.precio, producto.moneda)}
                      </p>
                    </div>
                    <div className="rounded-md bg-slate-50 p-3">
                      <p className="text-slate-500">Cuotas</p>
                      <p className="font-semibold text-[#0F2744]">
                        {producto.plazoMinimoMeses}-{producto.plazoMaximoMeses}{" "}
                        meses
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 rounded-md border border-slate-200 p-3">
                    {elegible ? (
                      <ShieldCheck className="mt-0.5 h-5 w-5 text-[#16A34A]" />
                    ) : (
                      <WalletCards className="mt-0.5 h-5 w-5 text-amber-600" />
                    )}
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-slate-900">
                        Colateral requerido:{" "}
                        {formatMoney(
                          precalificacion?.colateralRequerido ??
                            producto.colateralRequerido,
                          producto.moneda,
                        )}
                      </p>
                      <p className="text-xs text-slate-500">
                        {precalificacion
                          ? precalificacion.mensaje
                          : "Validando saldo disponible."}
                      </p>
                    </div>
                  </div>

                  <Button
                    className="w-full bg-[#16A34A] hover:bg-[#15803D]"
                    disabled={!elegible || solicitandoId === producto.id}
                    onClick={() => solicitar(producto.id)}
                  >
                    {solicitandoId === producto.id
                      ? "Enviando solicitud..."
                      : "Solicitar financiamiento"}
                  </Button>
                </CardContent>
              </Card>
            );
          })
        )}
      </div>
    </div>
  );
}
