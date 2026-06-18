"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { productosApi, resolveApiAssetUrl } from "@/lib/api/client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  ArrowLeft,
  CheckCircle2,
  Clock3,
  ImageIcon,
  PackageOpen,
  ShieldCheck,
  WalletCards,
} from "lucide-react";

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
  requiereAprobacionManual?: boolean;
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

export default function ProductoDetalleSocioPage() {
  const params = useParams<{ slug: string }>();
  const router = useRouter();
  const [producto, setProducto] = useState<Producto | null>(null);
  const [precalificacion, setPrecalificacion] =
    useState<Precalificacion | null>(null);
  const [imagenActiva, setImagenActiva] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [solicitando, setSolicitando] = useState(false);

  useEffect(() => {
    const cargarDetalle = async () => {
      setLoading(true);
      try {
        const productoRes = await productosApi.getProducto(params.slug);
        const item: Producto = productoRes.data;
        setProducto(item);
        const principal =
          item.imagenes?.find((imagen) => imagen.esPrincipal) ||
          item.imagenes?.[0];
        setImagenActiva(principal?.imagenUrl || item.imagenUrl || "");

        const precalificacionRes = await productosApi.precalificar(item.id);
        setPrecalificacion(precalificacionRes.data);
      } finally {
        setLoading(false);
      }
    };

    if (params.slug) {
      void cargarDetalle();
    }
  }, [params.slug]);

  const imagenes = useMemo(() => {
    if (!producto) return [];
    const gallery = producto.imagenes?.length
      ? producto.imagenes
      : producto.imagenUrl
        ? [
            {
              id: 0,
              imagenUrl: producto.imagenUrl,
              esPrincipal: true,
              orden: 0,
            },
          ]
        : [];
    return gallery.sort((a, b) => a.orden - b.orden);
  }, [producto]);

  const solicitar = async () => {
    if (!producto) return;
    setSolicitando(true);
    try {
      const res = await productosApi.solicitarFinanciamiento(producto.id);
      router.push(`/dashboard/creditos/${res.data.numeroSolicitud}`);
    } finally {
      setSolicitando(false);
    }
  };

  if (loading) {
    return (
      <div className="mx-auto max-w-7xl space-y-6">
        <div className="h-9 w-40 animate-pulse rounded bg-slate-200" />
        <div className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <div className="h-[420px] animate-pulse rounded-lg bg-slate-200" />
          <div className="space-y-4">
            <div className="h-12 animate-pulse rounded bg-slate-200" />
            <div className="h-36 animate-pulse rounded bg-slate-100" />
            <div className="h-48 animate-pulse rounded bg-slate-100" />
          </div>
        </div>
      </div>
    );
  }

  if (!producto) {
    return (
      <div className="mx-auto max-w-3xl rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center">
        <PackageOpen className="mx-auto h-10 w-10 text-slate-400" />
        <h1 className="mt-3 text-lg font-semibold text-slate-900">
          Producto no disponible
        </h1>
        <Button asChild className="mt-5">
          <Link href="/dashboard/productos">Volver a productos</Link>
        </Button>
      </div>
    );
  }

  const elegible = precalificacion?.elegible;
  const heroUrl = imagenActiva || producto.imagenUrl;

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <Button asChild variant="ghost" className="-ml-3 text-slate-600">
        <Link href="/dashboard/productos">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Volver
        </Link>
      </Button>

      <div className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
        <section className="space-y-3">
          <div className="relative min-h-[320px] overflow-hidden rounded-lg bg-[#0F2744] lg:min-h-[460px]">
            {heroUrl ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={resolveApiAssetUrl(heroUrl)}
                alt={producto.nombre}
                className="h-full min-h-[320px] w-full object-cover lg:min-h-[460px]"
              />
            ) : (
              <div className="flex min-h-[320px] items-center justify-center bg-[linear-gradient(135deg,#0F2744,#1D4B77)] lg:min-h-[460px]">
                <ImageIcon className="h-16 w-16 text-white/80" />
              </div>
            )}
            <Badge className="absolute left-4 top-4 bg-white text-[#0F2744] hover:bg-white">
              {producto.categoria}
            </Badge>
          </div>

          {imagenes.length > 1 && (
            <div className="flex gap-2 overflow-x-auto pb-1">
              {imagenes.map((imagen) => (
                <button
                  key={imagen.id}
                  type="button"
                  onClick={() => setImagenActiva(imagen.imagenUrl)}
                  className={`h-20 w-28 shrink-0 overflow-hidden rounded-md border bg-slate-100 ${
                    imagen.imagenUrl === imagenActiva
                      ? "border-[#16A34A] ring-2 ring-[#16A34A]/20"
                      : "border-slate-200"
                  }`}
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={resolveApiAssetUrl(imagen.imagenUrl)}
                    alt={`${producto.nombre} ${imagen.orden + 1}`}
                    className="h-full w-full object-cover"
                  />
                </button>
              ))}
            </div>
          )}
        </section>

        <aside className="space-y-4">
          <Card className="border-slate-200">
            <CardContent className="space-y-5 p-5">
              <div>
                <p className="text-sm font-medium text-[#16A34A]">
                  Producto financiable
                </p>
                <h1 className="mt-1 text-2xl font-bold text-[#0F2744]">
                  {producto.nombre}
                </h1>
                {producto.proveedor && (
                  <p className="mt-1 text-sm text-slate-500">
                    Proveedor: {producto.proveedor}
                  </p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-md bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Precio</p>
                  <p className="font-semibold text-[#0F2744]">
                    {formatMoney(producto.precio, producto.moneda)}
                  </p>
                </div>
                <div className="rounded-md bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Cuotas</p>
                  <p className="font-semibold text-[#0F2744]">
                    {producto.plazoMinimoMeses}-{producto.plazoMaximoMeses}{" "}
                    meses
                  </p>
                </div>
                <div className="rounded-md bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Colateral</p>
                  <p className="font-semibold text-[#0F2744]">
                    {producto.porcentajeColateral}%
                  </p>
                </div>
                <div className="rounded-md bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Revisión</p>
                  <p className="font-semibold text-[#0F2744]">
                    {producto.requiereAprobacionManual
                      ? "Manual"
                      : "Automática"}
                  </p>
                </div>
              </div>

              <div
                className={`rounded-lg border p-4 ${
                  elegible
                    ? "border-emerald-200 bg-emerald-50"
                    : "border-amber-200 bg-amber-50"
                }`}
              >
                <div className="flex items-start gap-3">
                  {elegible ? (
                    <ShieldCheck className="mt-0.5 h-5 w-5 text-[#16A34A]" />
                  ) : (
                    <WalletCards className="mt-0.5 h-5 w-5 text-amber-600" />
                  )}
                  <div>
                    <p className="font-medium text-slate-900">
                      Colateral requerido:{" "}
                      {formatMoney(
                        precalificacion?.colateralRequerido ??
                          producto.colateralRequerido,
                        producto.moneda,
                      )}
                    </p>
                    <p className="mt-1 text-sm text-slate-600">
                      {precalificacion?.mensaje ||
                        "Validando saldo disponible."}
                    </p>
                    {!elegible && precalificacion?.montoFaltante ? (
                      <p className="mt-2 text-sm font-medium text-amber-700">
                        Monto faltante:{" "}
                        {formatMoney(
                          precalificacion.montoFaltante,
                          producto.moneda,
                        )}
                      </p>
                    ) : null}
                  </div>
                </div>
              </div>

              <Button
                className="w-full bg-[#16A34A] hover:bg-[#15803D]"
                disabled={!elegible || solicitando}
                onClick={solicitar}
              >
                {solicitando
                  ? "Enviando solicitud..."
                  : "Solicitar financiamiento"}
              </Button>
            </CardContent>
          </Card>
        </aside>
      </div>

      <div className="grid gap-4 lg:grid-cols-[1fr_360px]">
        <Card className="border-slate-200">
          <CardContent className="p-5">
            <h2 className="font-semibold text-slate-950">Descripción</h2>
            <p className="mt-3 whitespace-pre-line text-sm leading-6 text-slate-600">
              {producto.descripcion || "Producto financiable para socios."}
            </p>
          </CardContent>
        </Card>

        <Card className="border-slate-200">
          <CardContent className="space-y-3 p-5">
            <h2 className="font-semibold text-slate-950">Condiciones</h2>
            <div className="flex gap-3 text-sm text-slate-600">
              <CheckCircle2 className="mt-0.5 h-4 w-4 text-[#16A34A]" />
              Solicitud sujeta al saldo colateral disponible.
            </div>
            <div className="flex gap-3 text-sm text-slate-600">
              <Clock3 className="mt-0.5 h-4 w-4 text-[#16A34A]" />
              Plazo permitido entre {producto.plazoMinimoMeses} y{" "}
              {producto.plazoMaximoMeses} meses.
            </div>
            <div className="flex gap-3 text-sm text-slate-600">
              <ShieldCheck className="mt-0.5 h-4 w-4 text-[#16A34A]" />
              La asociación puede revisar y aprobar antes del desembolso.
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
