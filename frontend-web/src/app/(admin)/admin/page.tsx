'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Loader2,
  Users,
  Clock,
  CheckCircle,
  XCircle,
  Wallet,
  AlertTriangle,
  TrendingUp,
  CreditCard,
  Activity,
  ArrowUpRight,
  ArrowDownRight,
  DollarSign,
  FileText,
  UserPlus,
  LogIn,
  Settings,
  ChevronRight,
  Eye,
  MoreHorizontal,
  Shield,
  BarChart3,
} from 'lucide-react';

interface DashboardStats {
  totalSocios: number;
  sociosActivos: number;
  sociosPendientes: number;
  totalCuentasAhorro: number;
  cuentasActivas: number;
  depositosMes: number;
  retirosMes: number;
  prestamosActivos: number;
  solicitudesPendientes: number;
  solicitudesAprobadas: number;
  solicitudesRechazadas: number;
  capitalDesembolsado: number;
  carteraVencida: number;
  cuotasVencidas: number;
  tasaCumplimiento: number;
  tasaMora: number;
  actividadReciente: {
    nuevosSociosMes: number;
    depositosMes: number;
    retirosMes: number;
    prestamosAprobadosMes: number;
    montoDepositadoMes: number;
    montoRetiradoMes: number;
  };
}

interface ActivityLog {
  id: string;
  tipoEvento: string;
  usuarioId: string | null;
  nombreUsuario: string | null;
  ipAddress: string;
  timestamp: string;
  detalles: string | null;
  entityType: string | null;
  entityId: string | null;
  action: string | null;
}

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  trend?: { value: number; positive: boolean };
  icon: React.ElementType;
  iconBg: string;
  iconColor: string;
}

function Sparkline({ data, color }: { data: number[]; color: string }) {
  const max = Math.max(...data);
  const min = Math.min(...data);
  const range = max - min || 1;
  const points = data.map((v, i) => {
    const x = (i / (data.length - 1)) * 80;
    const y = 20 - ((v - min) / range) * 16;
    return `${x},${y}`;
  }).join(' ');

  return (
    <svg className="w-20 h-5" viewBox="0 0 80 20">
      <polyline
        fill="none"
        stroke={color}
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        points={points}
      />
    </svg>
  );
}

function MetricCard({ title, value, subtitle, trend, icon: Icon, iconBg, iconColor }: MetricCardProps) {
  return (
    <Card className="relative overflow-hidden hover:shadow-md transition-shadow duration-200">
      <CardContent className="p-5">
        <div className="flex items-start justify-between">
          <div className="space-y-2">
            <p className="text-xs font-medium text-slate-500 uppercase tracking-wide">{title}</p>
            <p className="text-2xl font-bold text-slate-900">{value}</p>
            {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
          </div>
          <div className={`p-2.5 rounded-xl ${iconBg}`}>
            <Icon className={`w-5 h-5 ${iconColor}`} />
          </div>
        </div>
        {trend && (
          <div className="flex items-center gap-1 mt-3">
            {trend.positive ? (
              <ArrowUpRight className="w-4 h-4 text-emerald-600" />
            ) : (
              <ArrowDownRight className="w-4 h-4 text-red-500" />
            )}
            <span className={`text-xs font-medium ${trend.positive ? 'text-emerald-600' : 'text-red-500'}`}>
              {trend.value}%
            </span>
            <span className="text-xs text-slate-400">vs mes anterior</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function StatusBadge({ status }: { status: string }) {
  const configs: Record<string, { bg: string; text: string; label: string }> = {
    ACTIVO: { bg: 'bg-emerald-100', text: 'text-emerald-700', label: 'Activo' },
    INACTIVO: { bg: 'bg-slate-100', text: 'text-slate-700', label: 'Inactivo' },
    PENDIENTE: { bg: 'bg-amber-100', text: 'text-amber-700', label: 'Pendiente' },
    APROBADO: { bg: 'bg-emerald-100', text: 'text-emerald-700', label: 'Aprobado' },
    RECHAZADO: { bg: 'bg-red-100', text: 'text-red-700', label: 'Rechazado' },
    EN_REVISION: { bg: 'bg-blue-100', text: 'text-blue-700', label: 'En Revisión' },
    DESEMBOLSADO: { bg: 'bg-purple-100', text: 'text-purple-700', label: 'Desembolsado' },
    VENCIDO: { bg: 'bg-red-100', text: 'text-red-700', label: 'Vencido' },
  };

  const config = configs[status] || { bg: 'bg-slate-100', text: 'text-slate-700', label: status };

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${config.bg} ${config.text}`}>
      {config.label}
    </span>
  );
}

function Avatar({ name, size = 'md' }: { name: string; size?: 'sm' | 'md' | 'lg' }) {
  const sizes = {
    sm: 'w-7 h-7 text-[10px]',
    md: 'w-9 h-9 text-xs',
    lg: 'w-11 h-11 text-sm',
  };

  const colors = [
    'from-blue-500 to-blue-600',
    'from-emerald-500 to-emerald-600',
    'from-purple-500 to-purple-600',
    'from-amber-500 to-amber-600',
    'from-rose-500 to-rose-600',
    'from-cyan-500 to-cyan-600',
  ];

  const colorIndex = name.charCodeAt(0) % colors.length;

  return (
    <div className={`${sizes[size]} rounded-full bg-gradient-to-br ${colors[colorIndex]} flex items-center justify-center text-white font-semibold`}>
      {name.charAt(0).toUpperCase()}
    </div>
  );
}

const TIPO_EVENTO_CONFIG: Record<string, { icon: typeof LogIn; color: string; bg: string }> = {
  'LOGIN_SUCCESS': { icon: LogIn, color: 'text-emerald-600', bg: 'bg-emerald-100' },
  'LOGIN_FAILED': { icon: XCircle, color: 'text-red-600', bg: 'bg-red-100' },
  'LOGOUT': { icon: LogIn, color: 'text-slate-600', bg: 'bg-slate-100' },
  'TOKEN_REFRESH': { icon: Activity, color: 'text-blue-600', bg: 'bg-blue-100' },
  'ACCOUNT_LOCKED': { icon: AlertTriangle, color: 'text-red-600', bg: 'bg-red-100' },
  'DASHBOARD_ADMIN_ACCESS': { icon: Activity, color: 'text-purple-600', bg: 'bg-purple-100' },
  'SOLICITUD_REGISTRO_APROBADA': { icon: CheckCircle, color: 'text-emerald-600', bg: 'bg-emerald-100' },
  'SOLICITUD_REGISTRO_RECHAZADA': { icon: XCircle, color: 'text-red-600', bg: 'bg-red-100' },
  'USER_CREATED': { icon: UserPlus, color: 'text-blue-600', bg: 'bg-blue-100' },
};

export default function AdminDashboardPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loadingStats, setLoadingStats] = useState(true);
  const [actividadReciente, setActividadReciente] = useState<ActivityLog[]>([]);
  const [loadingActividad, setLoadingActividad] = useState(true);

  const cargarStats = useCallback(async () => {
    setLoadingStats(true);
    try {
      const res = await fetch("/api/admin/dashboard/estadisticas", { credentials: "include" });
      if (res.ok) { const data = await res.json(); setStats(data); } else { throw new Error("Error stats"); }
    } catch (err) {
      console.error('Error cargando stats:', err);
    } finally {
      setLoadingStats(false);
    }
  }, []);

  const cargarActividad = useCallback(async () => {
    setLoadingActividad(true);
    try {
      const resA = await fetch("/api/admin/dashboard/actividad?limit=15", { credentials: "include" });
      const data = resA.ok ? await resA.json() : [];
      setActividadReciente(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error cargando actividad:', err);
      setActividadReciente([]);
    } finally {
      setLoadingActividad(false);
    }
  }, []);

  useEffect(() => {
    if (!isLoading) {
      cargarStats();
      cargarActividad();
    }
  }, [isLoading, cargarStats, cargarActividad]);

  const formatCurrency = (value: number) => {
    if (value >= 1000000) return `$${(value / 1000000).toFixed(1)}M`;
    if (value >= 1000) return `$${(value / 1000).toFixed(1)}K`;
    return `$${value.toFixed(2)}`;
  };

  const formatTimeAgo = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMs / 3600000);
      const diffDays = Math.floor(diffMs / 86400000);

      if (diffMins < 1) return 'Hace un momento';
      if (diffMins < 60) return `Hace ${diffMins} min`;
      if (diffHours < 24) return `Hace ${diffHours}h`;
      if (diffDays < 7) return `Hace ${diffDays}d`;
      return date.toLocaleDateString('es-VE', { day: 'numeric', month: 'short' });
    } catch {
      return dateStr;
    }
  };

  if (isLoading || loadingStats) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-[#16A34A]" />
      </div>
    );
  }

  const sparklineData = [12, 15, 14, 18, 22, 20, 25];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Tablero de Control</h1>
          <p className="text-sm text-slate-500 mt-0.5">Resumen de la actividad del sistema</p>
        </div>
        <div className="flex items-center gap-3">
          <Badge variant="outline" className="bg-white">
            <span className="w-2 h-2 rounded-full bg-emerald-500 mr-2 animate-pulse" />
            Sistema Activo
          </Badge>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Total Transportistas"
          value={stats?.totalSocios || 0}
          subtitle={`${stats?.sociosActivos || 0} activos · ${stats?.sociosPendientes || 0} pendientes`}
          trend={{ value: 12.5, positive: true }}
          icon={Users}
          iconBg="bg-blue-100"
          iconColor="text-blue-600"
        />
        <MetricCard
          title="Solicitudes Pendientes"
          value={stats?.solicitudesPendientes || 0}
          subtitle="Requieren revisión inmediata"
          trend={{ value: 8.2, positive: false }}
          icon={Clock}
          iconBg="bg-amber-100"
          iconColor="text-amber-600"
        />
        <MetricCard
          title="Créditos Activos"
          value={stats?.prestamosActivos || 0}
          subtitle={`${stats?.solicitudesAprobadas || 0} aprobados este mes`}
          trend={{ value: 5.1, positive: true }}
          icon={CreditCard}
          iconBg="bg-purple-100"
          iconColor="text-purple-600"
        />
        <MetricCard
          title="Capital Desembolsado"
          value={formatCurrency(stats?.capitalDesembolsado || 0)}
          subtitle={`Cartera vencida: ${formatCurrency(stats?.carteraVencida || 0)}`}
          trend={{ value: 3.7, positive: true }}
          icon={Wallet}
          iconBg="bg-emerald-100"
          iconColor="text-emerald-600"
        />
      </div>

      {/* Secondary Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="hover:shadow-md transition-shadow">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-medium text-slate-500 uppercase">Tasa Cumplimiento</p>
                <p className="text-2xl font-bold text-emerald-600 mt-1">{(stats?.tasaCumplimiento || 0).toFixed(1)}%</p>
              </div>
              <Sparkline data={sparklineData} color="#16A34A" />
            </div>
            <p className="text-xs text-slate-400 mt-2">Tasa mora: {(stats?.tasaMora || 0).toFixed(1)}%</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-medium text-slate-500 uppercase">Cuentas Activas</p>
                <p className="text-2xl font-bold text-slate-900 mt-1">{stats?.cuentasActivas || 0}</p>
              </div>
              <Sparkline data={[8, 12, 10, 15, 14, 18, 20]} color="#0F2744" />
            </div>
            <p className="text-xs text-slate-400 mt-2">De {stats?.totalCuentasAhorro || 0} totales</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-medium text-slate-500 uppercase">Depósitos del Mes</p>
                <p className="text-2xl font-bold text-slate-900 mt-1">{formatCurrency(stats?.actividadReciente?.montoDepositadoMes || 0)}</p>
              </div>
              <TrendingUp className="w-8 h-8 text-emerald-500" />
            </div>
            <p className="text-xs text-slate-400 mt-2">{stats?.actividadReciente?.depositosMes || 0} transacciones</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-medium text-slate-500 uppercase">Retiros del Mes</p>
                <p className="text-2xl font-bold text-slate-900 mt-1">{formatCurrency(stats?.actividadReciente?.montoRetiradoMes || 0)}</p>
              </div>
              <TrendingUp className="w-8 h-8 text-amber-500" />
            </div>
            <p className="text-xs text-slate-400 mt-2">{stats?.actividadReciente?.retirosMes || 0} transacciones</p>
          </CardContent>
        </Card>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Activity Feed */}
        <Card className="lg:col-span-2">
          <CardHeader className="pb-3 border-b border-slate-100">
            <div className="flex items-center justify-between">
              <CardTitle className="text-base font-semibold flex items-center gap-2">
                <Activity className="w-5 h-5 text-slate-400" />
                Actividad Reciente
              </CardTitle>
              <Link href="/admin/auditoria" className="text-xs text-[#16A34A] hover:underline font-medium flex items-center gap-1">
                Ver todo <ChevronRight className="w-3 h-3" />
              </Link>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            {loadingActividad ? (
              <div className="flex justify-center py-12">
                <Loader2 className="h-6 w-6 animate-spin text-[#16A34A]" />
              </div>
            ) : actividadReciente.length === 0 ? (
              <div className="text-center py-12 text-slate-500">
                <Activity className="h-10 w-10 mx-auto mb-3 text-slate-300" />
                <p className="text-sm">Sin actividad reciente</p>
              </div>
            ) : (
              <div className="divide-y divide-slate-100">
                {actividadReciente.slice(0, 8).map((log) => {
                  const config = TIPO_EVENTO_CONFIG[log.tipoEvento] || { icon: Activity, color: 'text-slate-600', bg: 'bg-slate-100' };
                  const IconComponent = config.icon;
                  return (
                    <div key={log.id} className="flex items-center gap-4 px-5 py-3.5 hover:bg-slate-50 transition-colors">
                      <div className={`p-2 rounded-xl ${config.bg}`}>
                        <IconComponent className={`w-4 h-4 ${config.color}`} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-slate-900 truncate">
                          {log.nombreUsuario || 'Sistema'}
                        </p>
                        <p className="text-xs text-slate-500 truncate">{log.detalles || log.tipoEvento}</p>
                      </div>
                      <div className="flex items-center gap-3">
                        <StatusBadge status={log.tipoEvento.includes('APROBADA') ? 'APROBADO' : log.tipoEvento.includes('RECHAZADA') ? 'RECHAZADO' : 'PENDIENTE'} />
                        <span className="text-xs text-slate-400 whitespace-nowrap">{formatTimeAgo(log.timestamp)}</span>
                        <button className="p-1 hover:bg-slate-200 rounded transition-colors">
                          <MoreHorizontal className="w-4 h-4 text-slate-400" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card>
          <CardHeader className="pb-3 border-b border-slate-100">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Settings className="w-5 h-5 text-slate-400" />
              Accesos Rápidos
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4">
            <div className="space-y-2">
              <Link
                href="/admin/socios"
                className="flex items-center justify-between p-3 rounded-xl hover:bg-slate-50 transition-colors group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-blue-100 rounded-lg">
                    <Users className="w-4 h-4 text-blue-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-900">Gestionar Transportistas</p>
                    <p className="text-xs text-slate-500">{stats?.totalSocios || 0} registrados</p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
              </Link>

              <Link
                href="/admin/solicitudes"
                className="flex items-center justify-between p-3 rounded-xl hover:bg-slate-50 transition-colors group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-amber-100 rounded-lg">
                    <FileText className="w-4 h-4 text-amber-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-900">Ver Solicitudes</p>
                    <p className="text-xs text-slate-500">{stats?.solicitudesPendientes || 0} pendientes</p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
              </Link>

              <Link
                href="/admin/creditos"
                className="flex items-center justify-between p-3 rounded-xl hover:bg-slate-50 transition-colors group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-purple-100 rounded-lg">
                    <CreditCard className="w-4 h-4 text-purple-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-900">Gestionar Créditos</p>
                    <p className="text-xs text-slate-500">{stats?.prestamosActivos || 0} activos</p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
              </Link>

              <Link
                href="/admin/kyc"
                className="flex items-center justify-between p-3 rounded-xl hover:bg-slate-50 transition-colors group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-emerald-100 rounded-lg">
                    <Shield className="w-4 h-4 text-emerald-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-900">Revisar KYC</p>
                    <p className="text-xs text-slate-500">Verificaciones pendientes</p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
              </Link>

              <Link
                href="/admin/reportes"
                className="flex items-center justify-between p-3 rounded-xl hover:bg-slate-50 transition-colors group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-slate-100 rounded-lg">
                    <BarChart3 className="w-4 h-4 text-slate-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-900">Ver Reportes</p>
                    <p className="text-xs text-slate-500">Análisis y estadísticas</p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Users Table */}
      <Card>
        <CardHeader className="pb-3 border-b border-slate-100">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Users className="w-5 h-5 text-slate-400" />
              Últimos Transportistas
            </CardTitle>
            <Link href="/admin/socios" className="text-xs text-[#16A34A] hover:underline font-medium flex items-center gap-1">
              Ver todos <ChevronRight className="w-3 h-3" />
            </Link>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th className="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">Transportista</th>
                  <th className="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">Cédula</th>
                  <th className="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">Estado</th>
                  <th className="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">Cuentas</th>
                  <th className="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">Fecha Ingreso</th>
                  <th className="px-5 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wide">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                <tr className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <Avatar name="Carlos Mendoza" size="md" />
                      <div>
                        <p className="text-sm font-medium text-slate-900">Carlos Mendoza</p>
                        <p className="text-xs text-slate-500">carlos.mendoza@mail.com</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4 text-sm text-slate-600">V-20.456.789</td>
                  <td className="px-5 py-4"><StatusBadge status="ACTIVO" /></td>
                  <td className="px-5 py-4 text-sm text-slate-600">2 cuentas</td>
                  <td className="px-5 py-4 text-sm text-slate-500">15 Mar 2026</td>
                  <td className="px-5 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button className="p-1.5 hover:bg-slate-200 rounded-lg transition-colors" title="Ver">
                        <Eye className="w-4 h-4 text-slate-500" />
                      </button>
                      <button className="p-1.5 hover:bg-slate-200 rounded-lg transition-colors" title="Más opciones">
                        <MoreHorizontal className="w-4 h-4 text-slate-500" />
                      </button>
                    </div>
                  </td>
                </tr>
                <tr className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <Avatar name="María García" size="md" />
                      <div>
                        <p className="text-sm font-medium text-slate-900">María García</p>
                        <p className="text-xs text-slate-500">maria.garcia@mail.com</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4 text-sm text-slate-600">V-18.234.567</td>
                  <td className="px-5 py-4"><StatusBadge status="ACTIVO" /></td>
                  <td className="px-5 py-4 text-sm text-slate-600">1 cuenta</td>
                  <td className="px-5 py-4 text-sm text-slate-500">22 Feb 2026</td>
                  <td className="px-5 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button className="p-1.5 hover:bg-slate-200 rounded-lg transition-colors" title="Ver">
                        <Eye className="w-4 h-4 text-slate-500" />
                      </button>
                      <button className="p-1.5 hover:bg-slate-200 rounded-lg transition-colors" title="Más opciones">
                        <MoreHorizontal className="w-4 h-4 text-slate-500" />
                      </button>
                    </div>
                  </td>
                </tr>
                <tr className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <Avatar name="Juan Pérez" size="md" />
                      <div>
                        <p className="text-sm font-medium text-slate-900">Juan Pérez</p>
                        <p className="text-xs text-slate-500">juan.perez@mail.com</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4 text-sm text-slate-600">V-25.678.901</td>
                  <td className="px-5 py-4"><StatusBadge status="PENDIENTE" /></td>
                  <td className="px-5 py-4 text-sm text-slate-600">0 cuentas</td>
                  <td className="px-5 py-4 text-sm text-slate-500">28 Abr 2026</td>
                  <td className="px-5 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button className="p-1.5 hover:bg-slate-200 rounded-lg transition-colors" title="Ver">
                        <Eye className="w-4 h-4 text-slate-500" />
                      </button>
                      <button className="p-1.5 hover:bg-slate-200 rounded-lg transition-colors" title="Más opciones">
                        <MoreHorizontal className="w-4 h-4 text-slate-500" />
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
