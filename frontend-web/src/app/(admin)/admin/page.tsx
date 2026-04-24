'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Shield, Users, FileText, CreditCard, Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react';

interface AdminStats {
  totalSocios: number;
  solicitudesPendientes: number;
  creditosAprobados: number;
  creditosRechazados: number;
}

export default function AdminDashboardPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [stats, setStats] = useState<AdminStats>({
    totalSocios: 0,
    solicitudesPendientes: 0,
    creditosAprobados: 0,
    creditosRechazados: 0,
  });
  const [loadingStats, setLoadingStats] = useState(true);

  useEffect(() => {
    if (!isLoading) {
      cargarStats();
    }
  }, [isLoading]);

  async function cargarStats() {
    setLoadingStats(true);
    try {
      setStats({
        totalSocios: 0,
        solicitudesPendientes: 0,
        creditosAprobados: 0,
        creditosRechazados: 0,
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
            {loadingStats ? (
              <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
            ) : (
              <>
                <p className="text-3xl font-bold">{stats.totalSocios}</p>
                <p className="text-xs text-gray-500 mt-1">Socios registrados</p>
              </>
            )}
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-yellow-500">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
              <Clock className="h-4 w-4" />
              Pendientes
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loadingStats ? (
              <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
            ) : (
              <>
                <p className="text-3xl font-bold">{stats.solicitudesPendientes}</p>
                <p className="text-xs text-gray-500 mt-1">Solicitudes en cola</p>
              </>
            )}
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
            {loadingStats ? (
              <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
            ) : (
              <>
                <p className="text-3xl font-bold">{stats.creditosAprobados}</p>
                <p className="text-xs text-gray-500 mt-1">Este mes</p>
              </>
            )}
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
            {loadingStats ? (
              <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
            ) : (
              <>
                <p className="text-3xl font-bold">{stats.creditosRechazados}</p>
                <p className="text-xs text-gray-500 mt-1">Este mes</p>
              </>
            )}
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              Actividad Reciente
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
                    <p className="text-sm font-medium">Nuevo socio registrado</p>
                    <p className="text-xs text-gray-500">Hace 5 minutos</p>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between py-2 border-b">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-full bg-yellow-100">
                    <Clock className="h-4 w-4 text-yellow-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">Solicitud de crédito pendiente</p>
                    <p className="text-xs text-gray-500">Hace 15 minutos</p>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between py-2">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-full bg-green-100">
                    <CheckCircle className="h-4 w-4 text-green-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">KYC aprobado</p>
                    <p className="text-xs text-gray-500">Hace 30 minutos</p>
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
