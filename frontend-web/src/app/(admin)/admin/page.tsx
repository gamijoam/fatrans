'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Shield, Users, FileText, CreditCard, Clock, CheckCircle, XCircle, Loader2, Wallet, TrendingUp, AlertTriangle } from 'lucide-react';
import { toast } from 'sonner';

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

export default function AdminDashboardPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loadingStats, setLoadingStats] = useState(true);

  const cargarStats = useCallback(async () => {
    setLoadingStats(true);
    try {
      const res = await fetch('/api/admin/dashboard/estadisticas', {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar estadísticas');
      const data = await res.json();
      setStats(data);
    } catch (err) {
      console.error('Error cargando stats:', err);
      toast.error('Error al cargar estadísticas del dashboard');
    } finally {
      setLoadingStats(false);
    }
  }, []);

  useEffect(() => {
    if (!isLoading) {
      cargarStats();
    }
  }, [isLoading, cargarStats]);

  const formatCurrency = (value: number) => {
    if (value >= 1000000) {
      return `$${(value / 1000000).toFixed(1)}M`;
    }
    if (value >= 1000) {
      return `$${(value / 1000).toFixed(1)}K`;
    }
    return `$${value.toFixed(2)}`;
  };

  if (isLoading || loadingStats) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard Admin</h1>
          <p className="text-sm text-gray-500">Panel de administración del sistema</p>
        </div>
        <Badge variant="outline" className="text-primary">
          {user?.rol}
        </Badge>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Bienvenido, {user?.nombreCompleto || 'Administrador'}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-full bg-green-100">
              <Shield className="h-6 w-6 text-green-600" />
            </div>
            <div>
              <p className="font-medium">{user?.nombreUsuario}</p>
              <p className="text-sm text-gray-500">{user?.correoElectronico}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="border-l-4 border-l-blue-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <Users className="h-4 w-4" />
              Total Socios
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{stats?.totalSocios || 0}</p>
            <p className="text-xs text-gray-500 mt-1">
              {stats?.sociosActivos || 0} activos · {stats?.sociosPendientes || 0} pendientes
            </p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-yellow-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <Clock className="h-4 w-4" />
              Solicitudes Pendientes
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{stats?.solicitudesPendientes || 0}</p>
            <p className="text-xs text-gray-500 mt-1">Requieren revisión</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-green-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <CheckCircle className="h-4 w-4" />
              Créditos Aprobados
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{stats?.solicitudesAprobadas || 0}</p>
            <p className="text-xs text-gray-500 mt-1">Total activos: {stats?.prestamosActivos || 0}</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-red-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <XCircle className="h-4 w-4" />
              Créditos Rechazados
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{stats?.solicitudesRechazadas || 0}</p>
            <p className="text-xs text-gray-500 mt-1">Total rechazados</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="border-l-4 border-l-purple-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <Wallet className="h-4 w-4" />
              Capital Desembolsado
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{formatCurrency(stats?.capitalDesembolsado || 0)}</p>
            <p className="text-xs text-gray-500 mt-1">En préstamos activos</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-orange-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <AlertTriangle className="h-4 w-4" />
              Cartera Vencida
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{formatCurrency(stats?.carteraVencida || 0)}</p>
            <p className="text-xs text-gray-500 mt-1">{stats?.cuotasVencidas || 0} cuotas vencidas</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-cyan-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <TrendingUp className="h-4 w-4" />
              Tasa Cumplimiento
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{(stats?.tasaCumplimiento || 0).toFixed(1)}%</p>
            <p className="text-xs text-gray-500 mt-1">Tasa mora: {(stats?.tasaMora || 0).toFixed(1)}%</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-teal-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <CreditCard className="h-4 w-4" />
              Cuentas Activas
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{stats?.cuentasActivas || 0}</p>
            <p className="text-xs text-gray-500 mt-1">De {stats?.totalCuentasAhorro || 0} cuentas</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              Actividad del Mes
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center justify-between py-2 border-b">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-full bg-blue-100">
                    <Users className="h-4 w-4 text-blue-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">Nuevos Socios</p>
                    <p className="text-xs text-gray-500">{stats?.actividadReciente?.nuevosSociosMes || 0} este mes</p>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between py-2 border-b">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-full bg-green-100">
                    <TrendingUp className="h-4 w-4 text-green-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">Depósitos del Mes</p>
                    <p className="text-xs text-gray-500">{formatCurrency(stats?.actividadReciente?.montoDepositadoMes || 0)}</p>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between py-2">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-full bg-orange-100">
                    <Wallet className="h-4 w-4 text-orange-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">Retiros del Mes</p>
                    <p className="text-xs text-gray-500">{formatCurrency(stats?.actividadReciente?.montoRetiradoMes || 0)}</p>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-5 w-5" />
              Accesos Rápidos
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-3">
              <Link
                href="/admin/socios"
                className="flex items-center gap-3 p-3 rounded-lg border hover:bg-gray-50 transition-colors"
              >
                <Users className="h-5 w-5 text-gray-400" />
                <span className="text-sm font-medium">Gestionar Socios</span>
              </Link>
              <Link
                href="/admin/solicitudes"
                className="flex items-center gap-3 p-3 rounded-lg border hover:bg-gray-50 transition-colors"
              >
                <FileText className="h-5 w-5 text-gray-400" />
                <span className="text-sm font-medium">Ver Solicitudes</span>
              </Link>
              <Link
                href="/admin/creditos"
                className="flex items-center gap-3 p-3 rounded-lg border hover:bg-gray-50 transition-colors"
              >
                <CreditCard className="h-5 w-5 text-gray-400" />
                <span className="text-sm font-medium">Gestionar Créditos</span>
              </Link>
              <Link
                href="/admin/reportes"
                className="flex items-center gap-3 p-3 rounded-lg border hover:bg-gray-50 transition-colors"
              >
                <FileText className="h-5 w-5 text-gray-400" />
                <span className="text-sm font-medium">Ver Reportes</span>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
