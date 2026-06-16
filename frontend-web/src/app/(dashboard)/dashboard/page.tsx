'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';
import {
  Loader2, Wallet, CreditCard, Plus, ArrowUpRight, ArrowDownRight,
  Shield, FileText, Users, Calculator, Eye, EyeOff,
  AlertTriangle, Info, XOctagon, ChevronRight,
  type LucideIcon,
} from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { useTipoCambio } from '@/hooks/useTipoCambio';
import { calcularSaldoTotal, calcularSaldosPorMoneda } from '@/lib/utils/calcular-saldo-total';
import { parseCuentasResponse } from '@/lib/utils/parse-cuentas-response';
import {
  parseMovimientosResponse,
  type MovimientoApi,
} from '@/lib/utils/parse-movimientos-response';
import {
  parseBeneficiariosResponse,
  beneficiariosActivosOrdenados,
  sumaPorcentajes,
  type BeneficiarioApi,
} from '@/lib/utils/parse-beneficiarios-response';
import { decidirKycBanner, type KycBannerDecision } from '@/lib/utils/decidir-kyc-banner';
import {
  leerSaldoOculto,
  guardarSaldoOculto,
  aplicarOcultarSaldo,
} from '@/lib/utils/saldo-oculto-storage';

interface CuentaAhorro {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  moneda: string;
  saldoActual: number;
  estado: string;
}

function formatCurrency(amount: number, currency: string = 'VES') {
  return new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: currency === 'USD' ? 'USD' : 'VES',
    minimumFractionDigits: 2,
  }).format(amount);
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('es-VE', {
    day: 'numeric',
    month: 'short',
  });
}

function maskAccountNumber(numero: string) {
  return numero.replace(/.(?=.{4})/g, '*');
}

function AccountCard({ cuenta }: { cuenta: CuentaAhorro }) {
  const isUSD = cuenta.moneda === 'USD';
  const gradient = isUSD
    ? 'from-slate-800 to-slate-900'
    : 'from-[#0F2744] to-[#1a4a7a]';

  return (
    <div className="relative overflow-hidden rounded-2xl">
      <div className={`absolute inset-0 bg-gradient-to-br ${gradient}`} />
      <div className="relative p-6 text-white">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-2">
            <Wallet className="w-5 h-5 text-white/70" />
            <span className="text-sm font-medium text-white/70">
              {cuenta.tipoCuenta === 'AHORRO' ? 'Cuenta de Ahorro' : cuenta.tipoCuenta}
            </span>
          </div>
          <span className="text-xs font-medium px-2 py-1 bg-white/20 rounded-full">
            {isUSD ? 'USD' : 'VES'}
          </span>
        </div>

        <div className="mb-6">
          <p className="text-xs text-white/50 mb-1">Saldo Disponible</p>
          <p className="text-3xl font-bold tracking-tight">
            {formatCurrency(cuenta.saldoActual, cuenta.moneda)}
          </p>
        </div>

        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs text-white/50">Número de Cuenta</p>
            <p className="text-sm font-medium tracking-wider">
              {maskAccountNumber(cuenta.numeroCuenta)}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-400" />
            <span className="text-xs text-white/70">{cuenta.estado}</span>
          </div>
        </div>
      </div>
      {/* Decorative circles */}
      <div className="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-white/5" />
      <div className="absolute -bottom-5 -right-5 w-20 h-20 rounded-full bg-white/5" />
    </div>
  );
}

function QuickActionButton({
  icon: Icon,
  label,
  href,
  onClick,
  color,
}: {
  icon: typeof Plus;
  label: string;
  href?: string;
  onClick?: () => void;
  color: string;
}) {
  const content = (
    <>
      <div className={`w-12 h-12 rounded-xl ${color} flex items-center justify-center`}>
        <Icon className="w-6 h-6 text-white" />
      </div>
      <span className="text-xs font-medium text-slate-700 text-center">{label}</span>
    </>
  );

  const baseClass =
    'flex flex-col items-center gap-2 p-4 rounded-2xl bg-white border border-slate-200 ' +
    'hover:border-slate-300 hover:shadow-md transition-all active:scale-95 min-w-[100px] w-full';

  if (href) {
    return (
      <Link href={href} className={baseClass}>
        {content}
      </Link>
    );
  }

  return (
    <button onClick={onClick} className={baseClass}>
      {content}
    </button>
  );
}

function OperationLink({
  icon: Icon,
  title,
  href,
}: {
  icon: LucideIcon;
  title: string;
  href: string;
}) {
  return (
    <Link
      href={href}
      className="group flex min-h-[72px] items-center gap-4 rounded-2xl border border-slate-200 bg-white px-4 py-4 transition-all hover:border-[#16A34A]/40 hover:shadow-sm active:scale-[0.99]"
    >
      <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-[#0F2744] transition-colors group-hover:bg-emerald-50 group-hover:text-[#16A34A]">
        <Icon className="h-5 w-5" />
      </span>
      <span className="min-w-0 flex-1">
        <span className="block text-sm font-semibold text-[#0F2744]">{title}</span>
      </span>
      <ChevronRight className="h-4 w-4 shrink-0 text-slate-300 transition-colors group-hover:text-[#16A34A]" />
    </Link>
  );
}

// Issue #212: VehicleAlertBanner y VehicleCard ELIMINADOS — eran mocks con
// Toyota Hilux 2022 + SOAT ficticio que aparecían a CUALQUIER socio (incluso
// los que no tienen vehículo). El módulo de transporte aún no está implementado
// (EPIC #127). Cuando llegue, se reincorpora con datos reales.

/**
 * Issue #215: banner condicional sobre el estado KYC del socio.
 *
 * Antes el socio no se enteraba en su home si su KYC estaba pendiente,
 * en revisión, rechazado o nunca iniciado — descubría el problema solo
 * cuando intentaba operar y fallaba. Ahora le mostramos el estado
 * inmediato + CTA accionable.
 *
 * Si la decisión es `null` (APROBADO o estado desconocido), no renderiza
 * nada — la ausencia de banner es señal positiva.
 */
function KycBanner({ decision }: { decision: KycBannerDecision | null }) {
  if (!decision) return null;

  const styles = {
    warning: {
      container: 'bg-amber-50 border-amber-200',
      iconWrap: 'bg-amber-100',
      iconColor: 'text-amber-600',
      title: 'text-amber-900',
      text: 'text-amber-800',
      cta: 'bg-amber-500 hover:bg-amber-600 text-white',
      Icon: AlertTriangle,
    },
    info: {
      container: 'bg-blue-50 border-blue-200',
      iconWrap: 'bg-blue-100',
      iconColor: 'text-blue-600',
      title: 'text-blue-900',
      text: 'text-blue-800',
      cta: 'bg-blue-500 hover:bg-blue-600 text-white',
      Icon: Info,
    },
    error: {
      container: 'bg-red-50 border-red-200',
      iconWrap: 'bg-red-100',
      iconColor: 'text-red-600',
      title: 'text-red-900',
      text: 'text-red-800',
      cta: 'bg-red-500 hover:bg-red-600 text-white',
      Icon: XOctagon,
    },
  }[decision.tipo];

  const { Icon } = styles;

  return (
    <div
      role="alert"
      data-testid={`kyc-banner-${decision.tipo}`}
      className={`flex items-start gap-4 p-4 lg:p-5 rounded-2xl border ${styles.container}`}
    >
      <div className={`w-10 h-10 rounded-xl ${styles.iconWrap} flex items-center justify-center flex-shrink-0`}>
        <Icon className={`w-5 h-5 ${styles.iconColor}`} />
      </div>
      <div className="flex-1 min-w-0">
        <p className={`text-sm font-semibold ${styles.title} mb-1`}>{decision.titulo}</p>
        <p className={`text-xs ${styles.text}`}>{decision.mensaje}</p>
      </div>
      {decision.ctaTexto && decision.ctaHref && (
        <Link
          href={decision.ctaHref}
          className={`flex-shrink-0 inline-flex items-center gap-1 px-3 py-2 text-xs font-semibold rounded-lg transition-colors whitespace-nowrap ${styles.cta}`}
        >
          {decision.ctaTexto}
          <ChevronRight className="w-3 h-3" />
        </Link>
      )}
    </div>
  );
}

function TransactionItem({ actividad }: { actividad: MovimientoApi }) {
  const tipo = actividad.tipo?.toUpperCase() ?? '';
  const isDeposit = tipo === 'DEPOSITO' || tipo === 'INTERES' || tipo === 'TRANSFERENCIA_ENTRADA';
  const isInterest = tipo === 'INTERES';
  const monto = Number(actividad.monto) || 0;

  const iconBg = isInterest ? 'bg-emerald-100' : isDeposit ? 'bg-emerald-100' : 'bg-slate-100';
  const iconColor = isInterest ? 'text-emerald-600' : isDeposit ? 'text-emerald-600' : 'text-slate-600';
  const IconComponent = isDeposit ? ArrowDownRight : ArrowUpRight;

  return (
    <div className="flex items-center gap-4 py-4 border-b border-slate-100 last:border-0">
      <div className={`w-10 h-10 rounded-xl ${iconBg} flex items-center justify-center flex-shrink-0`}>
        <IconComponent className={`w-5 h-5 ${iconColor}`} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-slate-900 truncate">
          {actividad.descripcion || tipo.replace('_', ' ')}
        </p>
        <p className="text-xs text-slate-500">
          {actividad.fechaMovimiento ? formatDate(actividad.fechaMovimiento) : '—'}
        </p>
      </div>
      <div className={`text-sm font-semibold ${isDeposit ? 'text-emerald-600' : 'text-slate-700'}`}>
        {isDeposit ? '+' : '-'}{formatCurrency(monto)}
      </div>
    </div>
  );
}

function ProgressBar({ percentage, color }: { percentage: number; color: string }) {
  return (
    <div className="h-3 bg-slate-100 rounded-full overflow-hidden">
      <div
        className={`h-full ${color} rounded-full transition-all duration-500`}
        style={{ width: `${Math.min(percentage, 100)}%` }}
      />
    </div>
  );
}

export default function SocioDashboardPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [cuentas, setCuentas] = useState<CuentaAhorro[]>([]);
  const [loadingCuentas, setLoadingCuentas] = useState(true);

  // Issue #213: tasa BCV para agregar saldos multimoneda correctamente
  const { tasaActual, loading: loadingTasa } = useTipoCambio(1);
  // Toggle moneda de visualización (VES por defecto, mismo lado del país)
  const [monedaVista, setMonedaVista] = useState<'VES' | 'USD'>('VES');

  // Issue #219: toggle ocultar saldo (persistido en localStorage)
  // Empieza en `false` para que no haya mismatch SSR/CSR; en useEffect leemos
  // el valor real del storage.
  const [saldoOculto, setSaldoOculto] = useState<boolean>(false);
  useEffect(() => {
    setSaldoOculto(leerSaldoOculto());
  }, []);
  const toggleSaldoOculto = useCallback(() => {
    setSaldoOculto((prev) => {
      const nuevo = !prev;
      guardarSaldoOculto(nuevo);
      return nuevo;
    });
  }, []);

  // Issue #212: movimientos REALES (antes era `mockActividad` hardcoded)
  const [movimientos, setMovimientos] = useState<MovimientoApi[]>([]);
  const [loadingMovimientos, setLoadingMovimientos] = useState(true);

  // Issue #212: beneficiarios REALES (antes era `mockBeneficiarios` hardcoded
  // con "María García 60%, Juan Pérez 30%, Ana López 10%" — ficticios)
  const [beneficiarios, setBeneficiarios] = useState<BeneficiarioApi[]>([]);
  const [loadingBeneficiarios, setLoadingBeneficiarios] = useState(true);

  // Issue #215: estado KYC para decidir si mostrar banner de aviso.
  // Mientras no haya cargado (`null`), no mostramos nada — la decisión
  // del utility ya maneja `null` retornando `null`.
  const [kycEstado, setKycEstado] = useState<string | null>(null);
  const [kycMotivoRechazo, setKycMotivoRechazo] = useState<string | null>(null);

  const cargarCuentas = useCallback(async () => {
    if (!user?.socioId) return;
    setLoadingCuentas(true);
    try {
      const res = await fetch(`/api/cuentas/socio/${user.socioId}`);
      if (res.ok) {
        const data = await res.json();
        // Issue #213 (sub-bug descubierto en QA): el backend retorna
        // `{socioId, totalCuentas, cuentas: [...]}` (CuentasPorSocioResponse),
        // no un array directo. El check anterior `Array.isArray(data) ? data : []`
        // siempre dejaba la lista vacía aunque el socio tuviera cuentas.
        setCuentas(parseCuentasResponse(data));
      }
    } catch (err) {
      console.error('Error:', err);
    } finally {
      setLoadingCuentas(false);
    }
  }, [user?.socioId]);

  useEffect(() => {
    if (!isLoading && user?.socioId) {
      cargarCuentas();
    }
  }, [isLoading, user?.socioId, cargarCuentas]);

  // Issue #212: cargar beneficiarios REALES del socio
  const cargarBeneficiarios = useCallback(async () => {
    if (!user?.socioId) return;
    setLoadingBeneficiarios(true);
    try {
      const res = await fetch(`/api/beneficiarios?socioId=${user.socioId}`);
      if (res.ok) {
        const data = await res.json();
        setBeneficiarios(parseBeneficiariosResponse(data));
      } else {
        setBeneficiarios([]);
      }
    } catch (err) {
      console.error('Error cargando beneficiarios:', err);
      setBeneficiarios([]);
    } finally {
      setLoadingBeneficiarios(false);
    }
  }, [user?.socioId]);

  useEffect(() => {
    if (!isLoading && user?.socioId) {
      cargarBeneficiarios();
    }
  }, [isLoading, user?.socioId, cargarBeneficiarios]);

  // Issue #212: cargar movimientos REALES del socio (toma la primera cuenta
  // disponible — la mayoría de socios tiene 1-2 cuentas; los demás pueden ir
  // a "Ver todo" para ver los de otras cuentas).
  const cuentaParaMovimientos = cuentas[0];
  const movimientosHref = cuentaParaMovimientos
    ? `/dashboard/cuentas/${cuentaParaMovimientos.numeroCuenta}`
    : '/dashboard/cuentas';
  const cargarMovimientos = useCallback(async () => {
    if (!cuentaParaMovimientos) {
      // Sin cuentas → no hay movimientos posibles (ya no es loading)
      setMovimientos([]);
      setLoadingMovimientos(false);
      return;
    }
    setLoadingMovimientos(true);
    try {
      const res = await fetch(
        `/api/cuentas/${cuentaParaMovimientos.numeroCuenta}/movimientos?size=5`
      );
      if (res.ok) {
        const data = await res.json();
        setMovimientos(parseMovimientosResponse(data));
      } else {
        setMovimientos([]);
      }
    } catch (err) {
      console.error('Error cargando movimientos:', err);
      setMovimientos([]);
    } finally {
      setLoadingMovimientos(false);
    }
  }, [cuentaParaMovimientos]);

  useEffect(() => {
    // Esperar a que cuentas haya cargado antes de pedir movimientos
    if (!loadingCuentas) {
      cargarMovimientos();
    }
  }, [loadingCuentas, cargarMovimientos]);

  // Issue #215: cargar estado KYC (el BFF mapea KYC no iniciado a estado SIN_KYC)
  const cargarKycEstado = useCallback(async () => {
    try {
      const res = await fetch('/api/kyc/estado');
      if (res.ok) {
        const data = await res.json();
        setKycEstado(data?.estado ?? null);
        setKycMotivoRechazo(data?.motivoRechazo ?? null);
      } else {
        // 401/500 → no mostrar banner (no queremos ruido por fallo de red)
        setKycEstado(null);
      }
    } catch (err) {
      console.error('Error cargando estado KYC:', err);
      setKycEstado(null);
    }
  }, []);

  useEffect(() => {
    if (!isLoading && user?.socioId) {
      cargarKycEstado();
    }
  }, [isLoading, user?.socioId, cargarKycEstado]);

  // Decisión derivada — el utility maneja todos los casos (incluido null).
  const kycBannerDecision = decidirKycBanner(kycEstado, kycMotivoRechazo);

  // Issue #213: agregación correcta de saldos multimoneda.
  // `null` cuando la tasa todavía no se cargó (mostramos fallback, no número).
  const saldoAgregado = calcularSaldoTotal(cuentas, tasaActual);
  const saldoTotalListo = saldoAgregado !== null && !loadingTasa && !loadingCuentas;

  // Issue #230: fallback transparente cuando no hay tasa BCV.
  // Si la API de tipos-cambio no responde o devuelve vacío, agregamos a un
  // total agregado sería engañoso. Mostramos los saldos por moneda separados.
  const saldosPorMoneda = calcularSaldosPorMoneda(cuentas);
  // Mostramos el fallback cuando: ya terminaron de cargar las cuentas, hay
  // cuentas, pero la tasa no nos permite agregar (loading ya pasó pero saldoAgregado
  // sigue null).
  const debeMostrarFallback =
    !loadingCuentas &&
    !loadingTasa &&
    cuentas.length > 0 &&
    saldoAgregado === null;

  // Issue #212: mockActividad y mockBeneficiarios ELIMINADOS. Ahora vienen de
  // /api/cuentas/{n}/movimientos y /api/beneficiarios?socioId=...

  // Derivados de los beneficiarios reales para la UI
  const beneficiariosOrdenados = beneficiariosActivosOrdenados(beneficiarios);
  const totalPorcentajeAsignado = sumaPorcentajes(beneficiarios);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-[#16A34A]" />
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Welcome Section */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#0F2744]">
            Hola, {user?.nombreCompleto?.split(' ')[0] || ' Socio'}
          </h1>
          <p className="text-sm text-slate-500 mt-0.5">Aquí está el resumen de tu cuenta</p>
        </div>
      </div>

      {/* Issue #215: banner KYC condicional. NO renderiza nada si APROBADO o
          estado aún no cargado — la ausencia es señal positiva. */}
      <KycBanner decision={kycBannerDecision} />

      {/* Hero Balance Card */}
      <div className="bg-gradient-to-br from-[#0F2744] via-[#0F2744] to-[#1a4a7a] rounded-3xl p-6 lg:p-8 text-white relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 rounded-full bg-white/5 -translate-y-1/2 translate-x-1/2" />
        <div className="absolute bottom-0 left-0 w-40 h-40 rounded-full bg-white/5 translate-y-1/2 -translate-x-1/2" />

        <div className="relative">
          <div className="flex items-center justify-between mb-1 flex-wrap gap-3">
            <div className="flex items-center gap-2">
              <Shield className="w-4 h-4 text-emerald-400" />
              <span className="text-xs font-medium text-white/60 uppercase tracking-wider">Saldo Total Disponible</span>
              {/* Issue #219: toggle ocultar saldo (persistido) */}
              <button
                onClick={toggleSaldoOculto}
                aria-label={saldoOculto ? 'Mostrar saldo' : 'Ocultar saldo'}
                title={saldoOculto ? 'Mostrar saldo' : 'Ocultar saldo'}
                className="text-white/60 hover:text-white transition-colors p-1"
              >
                {saldoOculto ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {/* Issue #213: toggle moneda de visualización */}
            <div
              role="tablist"
              aria-label="Moneda de visualización del saldo total"
              className="inline-flex items-center bg-white/10 rounded-full p-1 text-xs font-semibold"
            >
              <button
                role="tab"
                aria-selected={monedaVista === 'VES'}
                onClick={() => setMonedaVista('VES')}
                className={`px-3 py-1 rounded-full transition-colors ${
                  monedaVista === 'VES' ? 'bg-white text-[#0F2744]' : 'text-white/70 hover:text-white'
                }`}
              >
                Bs
              </button>
              <button
                role="tab"
                aria-selected={monedaVista === 'USD'}
                onClick={() => setMonedaVista('USD')}
                className={`px-3 py-1 rounded-full transition-colors ${
                  monedaVista === 'USD' ? 'bg-white text-[#0F2744]' : 'text-white/70 hover:text-white'
                }`}
              >
                USD
              </button>
            </div>
          </div>

          {/* Issue #213/#230: tres estados posibles:
                1. Cargando (cuentas o tasa) → skeleton (no número incorrecto).
                2. Tasa disponible → saldo agregado con toggle Bs/USD.
                3. Tasa NO disponible pero hay cuentas → fallback con saldos
                   separados (estilo Wise/Revolut) en vez de skeleton infinito.
          */}
          {saldoTotalListo && saldoAgregado ? (
            // Caso 2: agregado correcto (con toggle ocultar #219)
            <p
              className="text-4xl lg:text-5xl font-bold tracking-tight mb-2"
              data-testid="saldo-total-agregado"
            >
              {aplicarOcultarSaldo(
                monedaVista === 'VES'
                  ? formatCurrency(saldoAgregado.totalVES, 'VES')
                  : formatCurrency(saldoAgregado.totalUSD, 'USD'),
                saldoOculto
              )}
            </p>
          ) : debeMostrarFallback ? (
            // Caso 3: fallback transparente sin tasa (con toggle ocultar #219)
            <div data-testid="saldo-total-fallback" className="mb-2">
              <div className="flex flex-wrap items-baseline gap-x-4 gap-y-1">
                {saldosPorMoneda.ves > 0 && (
                  <span className="text-3xl lg:text-4xl font-bold tracking-tight">
                    {aplicarOcultarSaldo(formatCurrency(saldosPorMoneda.ves, 'VES'), saldoOculto)}
                  </span>
                )}
                {saldosPorMoneda.ves > 0 && saldosPorMoneda.usd > 0 && (
                  <span className="text-2xl text-white/40">·</span>
                )}
                {saldosPorMoneda.usd > 0 && (
                  <span className="text-3xl lg:text-4xl font-bold tracking-tight">
                    {aplicarOcultarSaldo(formatCurrency(saldosPorMoneda.usd, 'USD'), saldoOculto)}
                  </span>
                )}
                {/* Edge: si ambos son 0 pero hay cuentas con saldo en otras monedas */}
                {saldosPorMoneda.ves === 0 && saldosPorMoneda.usd === 0 && (
                  <span className="text-3xl lg:text-4xl font-bold tracking-tight">
                    Sin saldo
                  </span>
                )}
              </div>
            </div>
          ) : (
            // Caso 1: cargando
            <div
              data-testid="saldo-total-loading"
              aria-label="Cargando saldo total"
              className="h-12 lg:h-14 w-64 bg-white/10 rounded-lg animate-pulse mb-2"
            />
          )}

          {/* Info debajo del saldo: tasa BCV cuando hay agregado, o aviso de fallback */}
          {tasaActual && saldoAgregado && (
            <p className="text-xs text-white/50 mb-6">
              Tasa BCV {tasaActual.fecha}: 1 USD = Bs {tasaActual.tasaVenta.toFixed(2)} ·
              {' '}
              {monedaVista === 'VES'
                ? <>Equivale a <span className="text-white/80">{formatCurrency(saldoAgregado.totalUSD, 'USD')}</span></>
                : <>Equivale a <span className="text-white/80">{formatCurrency(saldoAgregado.totalVES, 'VES')}</span></>}
            </p>
          )}
          {debeMostrarFallback && (
            <p className="text-xs text-white/50 mb-6">
              Tasa BCV no disponible — saldos mostrados por moneda
            </p>
          )}

          <Link
            href={movimientosHref}
            className="inline-flex items-center gap-2 text-sm font-semibold text-white/80 transition-colors hover:text-white"
          >
            Ver movimientos
            <ChevronRight className="h-4 w-4" />
          </Link>
        </div>
      </div>

      <section aria-labelledby="operaciones-frecuentes">
        <div className="mb-4">
          <h2 id="operaciones-frecuentes" className="text-lg font-bold text-[#0F2744]">
            Operaciones frecuentes
          </h2>
        </div>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
          <OperationLink
            icon={Wallet}
            title="Registrar aporte"
            href="/dashboard/cuentas"
          />
          <OperationLink
            icon={CreditCard}
            title="Ver opciones de crédito"
            href="/dashboard/creditos"
          />
          <OperationLink
            icon={FileText}
            title="Documentos y constancias"
            href="/dashboard/documentos"
          />
        </div>
      </section>

      {/* Account Cards */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-bold text-[#0F2744]">Mis Cuentas</h2>
          <Link href="/dashboard/cuentas" className="text-sm text-[#16A34A] font-medium hover:underline">
            Ver todas
          </Link>
        </div>

        {loadingCuentas ? (
          <div className="flex items-center justify-center py-8">
            <Loader2 className="h-6 w-6 animate-spin text-[#16A34A]" />
          </div>
        ) : cuentas.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {cuentas.map((cuenta) => (
              <Link key={cuenta.id} href={`/dashboard/cuentas/${cuenta.numeroCuenta}`}>
                <AccountCard cuenta={cuenta} />
              </Link>
            ))}
          </div>
        ) : (
          <Card className="p-8 text-center border-dashed border-2 border-slate-200 bg-slate-50">
            <Wallet className="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <p className="text-slate-500 mb-4">No tienes cuentas activas aún</p>
            <Link
              href="/dashboard/cuentas"
              className="inline-flex items-center gap-2 px-4 py-2 bg-[#16A34A] text-white text-sm font-medium rounded-lg hover:bg-[#15803D]"
            >
              <Plus className="w-4 h-4" />
              Abrir cuenta
            </Link>
          </Card>
        )}
      </div>

      {/* Issue #212: Two-Column Grid — Vehicle Card eliminado (mock Toyota Hilux).
          Ahora: Actividad real (left) + Beneficiarios reales (right). */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Recent Activity — datos reales del primer cuenta del socio */}
        <Card className="border-slate-200">
          <CardContent className="p-0">
            <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <h3 className="font-semibold text-[#0F2744]">Actividad Reciente</h3>
              {cuentaParaMovimientos && (
                <Link
                  href={`/dashboard/cuentas/${cuentaParaMovimientos.numeroCuenta}`}
                  className="text-xs text-[#16A34A] font-medium hover:underline"
                >
                  Ver todo
                </Link>
              )}
            </div>
            <div className="px-5 min-h-[200px]">
              {loadingMovimientos ? (
                <div className="flex items-center justify-center py-12">
                  <Loader2 className="h-6 w-6 animate-spin text-[#16A34A]" />
                </div>
              ) : movimientos.length > 0 ? (
                movimientos.map((mov) => (
                  <TransactionItem key={mov.id} actividad={mov} />
                ))
              ) : (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <Wallet className="w-10 h-10 text-slate-300 mb-2" />
                  <p className="text-sm text-slate-500 mb-1">Aún no tienes movimientos</p>
                  <p className="text-xs text-slate-400">
                    Tus depósitos y retiros aparecerán aquí
                  </p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Beneficiaries — datos reales del socio (issue #212) */}
        <Card className="border-slate-200">
          <CardContent className="p-5 min-h-[200px]">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-[#0F2744]">Beneficiarios</h3>
              <Link href="/dashboard/beneficiarios" className="text-xs text-[#16A34A] font-medium hover:underline">
                Gestionar
              </Link>
            </div>

            {loadingBeneficiarios ? (
              <div className="flex items-center justify-center py-12">
                <Loader2 className="h-6 w-6 animate-spin text-[#16A34A]" />
              </div>
            ) : beneficiariosOrdenados.length > 0 ? (
              <>
                <div className="space-y-3 mb-4">
                  {beneficiariosOrdenados.slice(0, 3).map((b) => {
                    const pct = Number(b.porcentaje) || 0;
                    return (
                      <div key={b.id}>
                        <div className="flex items-center justify-between text-sm mb-1">
                          <span className="text-slate-700 truncate pr-2">{b.nombreCompleto}</span>
                          <span className="font-medium text-[#0F2744]">{pct.toFixed(0)}%</span>
                        </div>
                        <ProgressBar percentage={pct} color="bg-[#16A34A]" />
                      </div>
                    );
                  })}
                </div>

                <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                  <span className="text-sm font-medium text-slate-700">Total asignado</span>
                  {/* Mostramos el porcentaje real, no un "100%" hardcoded */}
                  <span className={`text-sm font-bold ${
                    totalPorcentajeAsignado === 100
                      ? 'text-emerald-600'
                      : 'text-amber-600'
                  }`}>
                    {totalPorcentajeAsignado.toFixed(0)}%
                  </span>
                </div>
              </>
            ) : (
              <div className="flex flex-col items-center justify-center py-8 text-center">
                <Users className="w-10 h-10 text-slate-300 mb-2" />
                <p className="text-sm text-slate-500 mb-3">Aún no tienes beneficiarios</p>
                <Link
                  href="/dashboard/beneficiarios"
                  className="inline-flex items-center gap-2 px-4 py-2 bg-[#16A34A] text-white text-xs font-medium rounded-lg hover:bg-[#15803D]"
                >
                  <Plus className="w-3 h-3" />
                  Agregar primer beneficiario
                </Link>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Quick Links — issue #200: "Mi Unidad" eliminado (link `/dashboard/unidad`
          rompía con 404 porque el módulo Transporte aún no existe — EPIC #127). */}
      <div className="grid grid-cols-2 lg:grid-cols-3 gap-3">
        <QuickActionButton
          icon={FileText}
          label="Documentos"
          href="/dashboard/documentos"
          color="bg-slate-600"
        />
        <QuickActionButton
          icon={Calculator}
          label="Simulador"
          href="/dashboard/creditos/simular"
          color="bg-[#0F2744]"
        />
        <QuickActionButton
          icon={Shield}
          label="KYC"
          href="/dashboard/kyc"
          color="bg-emerald-600"
        />
      </div>
    </div>
  );
}
