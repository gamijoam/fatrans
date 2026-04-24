'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { cuentasApi } from '@/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { User, Mail, Calendar, Loader2 } from 'lucide-react';
import { ChangePasswordModal } from '@/components/shared/change-password-modal';

interface DashboardStats {
  totalCuentas: number;
  cuentasActivas: number;
  saldoTotal: number;
  movimientosMes: number;
}

export default function DashboardPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [stats, setStats] = useState<DashboardStats>({
    totalCuentas: 0,
    cuentasActivas: 0,
    saldoTotal: 0,
    movimientosMes: 0,
  });
  const [loadingStats, setLoadingStats] = useState(false);

  useEffect(() => {
    if (user?.socioId) {
      cargarStats();
    }
  }, [user?.socioId]);

  async function cargarStats() {
    if (!user?.socioId) return;
    setLoadingStats(true);
    try {
      const res = await cuentasApi.getCuentas(user.socioId);
      const data = res.data;
      const activas = data.cuentas.filter((c: { estado: string }) => c.estado === 'ACTIVA').length;
      const saldoTotal = data.cuentas.reduce((acc: number, c: { saldoActual: number }) => acc + Number(c.saldoActual), 0);
      setStats({
        totalCuentas: data.totalCuentas,
        cuentasActivas: activas,
        saldoTotal,
        movimientosMes: 0,
      });
    } catch (err) {
      console.error('Error cargando stats:', err);
    } finally {
      setLoadingStats(false);
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <ChangePasswordModal open={!!user?.debeCambiarPassword} />

      <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>

      <Card>
        <CardHeader>
          <CardTitle>Bienvenido, {user?.nombreCompleto || 'Usuario'}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-full bg-green-100">
              <User className="h-6 w-6 text-green-600" />
            </div>
            <div>
              <p className="font-medium">{user?.nombreUsuario}</p>
              <Badge variant="outline" className="mt-1">
                {user?.rol}
              </Badge>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t">
            <div className="flex items-center gap-3">
              <Mail className="h-4 w-4 text-gray-400" />
              <span className="text-sm text-gray-600">{user?.correoElectronico}</span>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-4 w-4 text-gray-400" />
              <span className="text-sm text-gray-600">ID: {user?.id}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Cuentas de Ahorro</CardTitle>
          </CardHeader>
          <CardContent>
            {loadingStats ? (
              <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
            ) : (
              <>
                <p className="text-2xl font-bold">{stats.totalCuentas}</p>
                <p className="text-xs text-gray-500 mt-1">Activas: {stats.cuentasActivas}</p>
                <p className="text-sm font-medium text-green-600 mt-2">
                  Bs {stats.saldoTotal.toLocaleString('es-VE', { minimumFractionDigits: 2 })}
                </p>
              </>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Créditos</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">0</p>
            <p className="text-xs text-gray-500 mt-1">Pendientes: 0</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Movimientos</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{stats.movimientosMes}</p>
            <p className="text-xs text-gray-500 mt-1">Último mes</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}