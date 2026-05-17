'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';
import { Loader2, Wallet, CreditCard, TrendingUp, Plus, ArrowUpRight, ArrowDownRight, Truck, AlertTriangle, Shield, ChevronRight, FileText, Calendar } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { useTipoCambio } from '@/hooks/useTipoCambio';
import { calcularSaldoTotal } from '@/lib/utils/calcular-saldo-total';
import { parseCuentasResponse } from '@/lib/utils/parse-cuentas-response';

interface CuentaAhorro {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  moneda: string;
  saldoActual: number;
  estado: string;
}

interface Actividad {
  id: string;
  tipo: 'DEPOSITO' | 'RETIRO' | 'INTERES';
  descripcion: string;
  monto: number;
  fecha: string;
  icono: string;
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

function QuickActionButton({ icon: Icon, label, onClick, color }: { icon: typeof Plus; label: string; onClick?: () => void; color: string }) {
  return (
    <button
      onClick={onClick}
      className="flex flex-col items-center gap-2 p-4 rounded-2xl bg-white border border-slate-200 hover:border-slate-300 hover:shadow-md transition-all active:scale-95 min-w-[100px]"
    >
      <div className={`w-12 h-12 rounded-xl ${color} flex items-center justify-center`}>
        <Icon className="w-6 h-6 text-white" />
      </div>
      <span className="text-xs font-medium text-slate-700 text-center">{label}</span>
    </button>
  );
}

function VehicleAlertBanner() {
  return (
    <div className="flex items-center gap-4 p-4 bg-amber-50 border border-amber-200 rounded-2xl">
      <div className="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center flex-shrink-0">
        <AlertTriangle className="w-6 h-6 text-amber-600" />
      </div>
      <div className="flex-1">
        <p className="text-sm font-semibold text-amber-900">SOAT vence en 15 días</p>
        <p className="text-xs text-amber-700">Renueva tu seguro antes del 15 May 2026</p>
      </div>
      <button className="px-4 py-2 bg-amber-500 text-white text-xs font-semibold rounded-lg hover:bg-amber-600 transition-colors">
        Renovarlo
      </button>
    </div>
  );
}

function VehicleCard() {
  return (
    <Card className="overflow-hidden border-slate-200">
      <div className="bg-gradient-to-r from-[#0F2744] to-[#1a4a7a] p-5 text-white">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-lg bg-white/20 flex items-center justify-center">
            <Truck className="w-5 h-5" />
          </div>
          <div>
            <p className="font-semibold">Toyota Hilux</p>
            <p className="text-xs text-white/70">Placa: ABC-1234</p>
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-xs text-white/50">Año</p>
            <p className="font-medium">2022</p>
          </div>
          <div>
            <p className="text-xs text-white/50">Color</p>
            <p className="font-medium">Gris Oscuro</p>
          </div>
        </div>
      </div>
      <CardContent className="p-4">
        <VehicleAlertBanner />
        <div className="mt-4 flex items-center justify-between">
          <Link href="/dashboard/unidad" className="text-xs text-[#16A34A] font-medium hover:underline flex items-center gap-1">
            Ver detalle <ChevronRight className="w-3 h-3" />
          </Link>
          <button className="text-xs text-slate-500 hover:text-slate-700">
            Editar
          </button>
        </div>
      </CardContent>
    </Card>
  );
}

function TransactionItem({ actividad }: { actividad: Actividad }) {
  const isDeposit = actividad.tipo === 'DEPOSITO' || actividad.tipo === 'INTERES';
  const isInterest = actividad.tipo === 'INTERES';

  const iconBg = isInterest ? 'bg-emerald-100' : isDeposit ? 'bg-emerald-100' : 'bg-slate-100';
  const iconColor = isInterest ? 'text-emerald-600' : isDeposit ? 'text-emerald-600' : 'text-slate-600';
  const IconComponent = isDeposit ? ArrowDownRight : ArrowUpRight;

  return (
    <div className="flex items-center gap-4 py-4 border-b border-slate-100 last:border-0">
      <div className={`w-10 h-10 rounded-xl ${iconBg} flex items-center justify-center flex-shrink-0`}>
        <IconComponent className={`w-5 h-5 ${iconColor}`} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-slate-900 truncate">{actividad.descripcion}</p>
        <p className="text-xs text-slate-500">{formatDate(actividad.fecha)}</p>
      </div>
      <div className={`text-sm font-semibold ${isDeposit ? 'text-emerald-600' : 'text-slate-700'}`}>
        {isDeposit ? '+' : '-'}{formatCurrency(actividad.monto)}
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

  // Issue #213: agregación correcta de saldos multimoneda.
  // `null` cuando la tasa todavía no se cargó (mostramos skeleton, no número).
  const saldoAgregado = calcularSaldoTotal(cuentas, tasaActual);
  const saldoTotalListo = saldoAgregado !== null && !loadingTasa && !loadingCuentas;

  const mockActividad: Actividad[] = [
    { id: '1', tipo: 'DEPOSITO', descripcion: 'Depósito en efectivo', monto: 500000, fecha: '2026-04-28', icono: 'down' },
    { id: '2', tipo: 'RETIRO', descripcion: 'Retiro en cajero', monto: 150000, fecha: '2026-04-27', icono: 'up' },
    { id: '3', tipo: 'INTERES', descripcion: 'Abono de intereses', monto: 12500, fecha: '2026-04-26', icono: 'down' },
    { id: '4', tipo: 'DEPOSITO', descripcion: 'Transferencia recibida', monto: 2000000, fecha: '2026-04-25', icono: 'down' },
  ];

  const mockBeneficiarios = [
    { nombre: 'María García', porcentaje: 60 },
    { nombre: 'Juan Pérez', porcentaje: 30 },
    { nombre: 'Ana López', porcentaje: 10 },
  ];

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

      {/* Hero Balance Card */}
      <div className="bg-gradient-to-br from-[#0F2744] via-[#0F2744] to-[#1a4a7a] rounded-3xl p-6 lg:p-8 text-white relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 rounded-full bg-white/5 -translate-y-1/2 translate-x-1/2" />
        <div className="absolute bottom-0 left-0 w-40 h-40 rounded-full bg-white/5 translate-y-1/2 -translate-x-1/2" />

        <div className="relative">
          <div className="flex items-center justify-between mb-1 flex-wrap gap-3">
            <div className="flex items-center gap-2">
              <Shield className="w-4 h-4 text-emerald-400" />
              <span className="text-xs font-medium text-white/60 uppercase tracking-wider">Saldo Total Disponible</span>
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

          {/* Issue #213: muestra saldo agregado en la moneda seleccionada.
              Mientras la tasa BCV no se haya cargado, mostramos skeleton (NO un
              número incorrecto). */}
          {saldoTotalListo && saldoAgregado ? (
            <p
              className="text-4xl lg:text-5xl font-bold tracking-tight mb-2"
              data-testid="saldo-total-agregado"
            >
              {monedaVista === 'VES'
                ? formatCurrency(saldoAgregado.totalVES, 'VES')
                : formatCurrency(saldoAgregado.totalUSD, 'USD')}
            </p>
          ) : (
            <div
              data-testid="saldo-total-loading"
              aria-label="Cargando saldo total"
              className="h-12 lg:h-14 w-64 bg-white/10 rounded-lg animate-pulse mb-2"
            />
          )}

          {/* Info de tasa usada para la conversión (transparencia) */}
          {tasaActual && (
            <p className="text-xs text-white/50 mb-6">
              Tasa BCV {tasaActual.fecha}: 1 USD = Bs {tasaActual.tasaVenta.toFixed(2)} ·
              {' '}
              {monedaVista === 'VES' && saldoAgregado
                ? <>Equivale a <span className="text-white/80">{formatCurrency(saldoAgregado.totalUSD, 'USD')}</span></>
                : monedaVista === 'USD' && saldoAgregado
                ? <>Equivale a <span className="text-white/80">{formatCurrency(saldoAgregado.totalVES, 'VES')}</span></>
                : null}
            </p>
          )}

          {/* Quick Actions */}
          <div className="flex flex-wrap gap-3">
            <Link
              href="/dashboard/cuentas"
              className="flex items-center gap-2 px-5 py-3 bg-white text-[#0F2744] font-semibold rounded-xl hover:bg-slate-100 transition-colors shadow-lg"
            >
              <Plus className="w-5 h-5" />
              Aportar
            </Link>
            <Link
              href="/dashboard/creditos/solicitar"
              className="flex items-center gap-2 px-5 py-3 bg-[#16A34A] text-white font-semibold rounded-xl hover:bg-[#15803D] transition-colors shadow-lg"
            >
              <CreditCard className="w-5 h-5" />
              Solicitar Crédito
            </Link>
          </div>
        </div>
      </div>

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

      {/* Two Column Grid: Activity & Vehicle */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Activity */}
        <Card className="border-slate-200">
          <CardContent className="p-0">
            <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <h3 className="font-semibold text-[#0F2744]">Actividad Reciente</h3>
              <Link href="/dashboard/cuentas" className="text-xs text-[#16A34A] font-medium hover:underline">
                Ver todo
              </Link>
            </div>
            <div className="px-5">
              {mockActividad.map((actividad) => (
                <TransactionItem key={actividad.id} actividad={actividad} />
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Vehicle Card */}
        <div className="space-y-4">
          <VehicleCard />

          {/* Beneficiaries Summary */}
          <Card className="border-slate-200">
            <CardContent className="p-5">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold text-[#0F2744]">Beneficiarios</h3>
                <Link href="/dashboard/beneficiarios" className="text-xs text-[#16A34A] font-medium hover:underline">
                  Gestionar
                </Link>
              </div>

              <div className="space-y-3 mb-4">
                {mockBeneficiarios.map((b) => (
                  <div key={b.nombre}>
                    <div className="flex items-center justify-between text-sm mb-1">
                      <span className="text-slate-700">{b.nombre}</span>
                      <span className="font-medium text-[#0F2744]">{b.porcentaje}%</span>
                    </div>
                    <ProgressBar percentage={b.porcentaje} color="bg-[#16A34A]" />
                  </div>
                ))}
              </div>

              <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                <span className="text-sm font-medium text-slate-700">Total asignado</span>
                <span className="text-sm font-bold text-emerald-600">100%</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Quick Links */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <QuickActionButton icon={FileText} label="Documentos" color="bg-slate-600" />
        <QuickActionButton icon={Calendar} label="Simulador" color="bg-[#0F2744]" />
        <QuickActionButton icon={Truck} label="Mi Unidad" color="bg-amber-500" />
        <QuickActionButton icon={Shield} label="KYC" color="bg-emerald-600" />
      </div>
    </div>
  );
}
