'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { cuentasApi } from '@/lib/api/client';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import Link from 'next/link';
import { useTipoCambio, convertirABolivares, convertirADolares } from '@/hooks/useTipoCambio';

interface CuentaDetail {
  id: string;
  numeroCuenta: string;
  socioId: string;
  saldoActual: number;
  saldoRetenido: number;
  saldoDisponible: number;
  tasaInteres: number;
  montoMinimoRequerido: number;
  estado: string;
  tipoCuenta: string;
  moneda: string;
  fechaApertura: string;
  fechaUltimaOperacion: string;
}

interface SaldoDetail {
  numeroCuenta: string;
  saldoActual: number;
  saldoRetenido: number;
  saldoDisponible: number;
  fechaConsulta: string;
  limiteDeposito: number;
  limiteRetiroDiario: number;
  retirosRealizadosHoy: number;
  retirosRestantesHoy: number;
}

export default function CuentaDetailPage() {
  const params = useParams();
  const numeroCuenta = params.numero as string;

  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);
  const { tasaActual } = useTipoCambio(1);

  const [cuenta, setCuenta] = useState<CuentaDetail | null>(null);
  const [saldo, setSaldo] = useState<SaldoDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!numeroCuenta || isLoading) return;

    async function cargarDatos() {
      setLoading(true);
      setError(null);
      try {
        const [cuentaRes, saldoRes] = await Promise.all([
          cuentasApi.getCuenta(numeroCuenta),
          cuentasApi.getSaldo(numeroCuenta),
        ]);
        setCuenta(cuentaRes.data);
        setSaldo(saldoRes.data);
      } catch (err: unknown) {
        console.error('Error al cargar cuenta:', err);
        setError('Error al cargar los datos de la cuenta');
      } finally {
        setLoading(false);
      }
    }

    cargarDatos();
  }, [numeroCuenta, isLoading]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  if (error || !cuenta) {
    return (
      <div className="space-y-4">
        <Link href="/dashboard/cuentas">
          <Button variant="outline">← Volver a Cuentas</Button>
        </Link>
        <Card>
          <CardContent className="py-8">
            <p className="text-center text-red-600">{error || 'Cuenta no encontrada'}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const formatMonto = (monto: number | null | undefined, moneda: string) => {
    if (monto == null) return '-';
    const simbolo = moneda === 'VES' ? 'Bs' : '$';
    return `${simbolo} ${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
  };

  const formatFecha = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-VE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const estadoColor = {
    ACTIVA: 'bg-green-100 text-green-800',
    INACTIVA: 'bg-gray-100 text-gray-800',
    BLOQUEADA: 'bg-red-100 text-red-800',
    CERRADA: 'bg-gray-100 text-gray-800',
  }[cuenta.estado] || 'bg-gray-100 text-gray-800';

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Link href="/dashboard/cuentas">
          <Button variant="outline">← Volver a Cuentas</Button>
        </Link>
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${estadoColor}`}>
          {cuenta.estado}
        </span>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-xl">{cuenta.numeroCuenta}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-500">Tipo de Cuenta</p>
                <p className="font-medium">{cuenta.tipoCuenta}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Moneda</p>
                <p className="font-medium">{cuenta.moneda === 'VES' ? 'Bolívar' : 'Dólar'}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Fecha de Apertura</p>
                <p className="font-medium">{formatFecha(cuenta.fechaApertura)}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Última Operación</p>
                <p className="font-medium">
                  {cuenta.fechaUltimaOperacion ? formatFecha(cuenta.fechaUltimaOperacion) : 'Sin operaciones'}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Saldo Disponible</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-center py-4">
              <p className="text-4xl font-bold text-green-600">
                {formatMonto(cuenta.saldoDisponible, cuenta.moneda)}
              </p>
              {tasaActual && (
                <p className="text-lg text-blue-600 mt-1">
                  {cuenta.moneda === 'VES'
                    ? `$ ${convertirADolares(cuenta.saldoDisponible, tasaActual.tasaCompra).toLocaleString('es-VE', { minimumFractionDigits: 2 })} USD`
                    : `Bs ${convertirABolivares(cuenta.saldoDisponible, tasaActual.tasaVenta).toLocaleString('es-VE', { minimumFractionDigits: 2 })}`
                  }
                </p>
              )}
            </div>
            {tasaActual && (
              <p className="text-xs text-gray-400 text-center">
                Tasa referencial: Bs {tasaActual.tasaVenta.toLocaleString('es-VE', { minimumFractionDigits: 2 })}/$
              </p>
            )}
            <div className="grid grid-cols-2 gap-4 pt-4 border-t">
              <div>
                <p className="text-sm text-gray-500">Saldo Actual</p>
                <p className="font-medium">{formatMonto(cuenta.saldoActual, cuenta.moneda)}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Saldo Retenido</p>
                <p className="font-medium">{formatMonto(cuenta.saldoRetenido, cuenta.moneda)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {saldo && (
        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Límites y Operaciones del Día</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              <div>
                <p className="text-sm text-gray-500">Límite de Depósito</p>
                <p className="font-medium">{formatMonto(saldo.limiteDeposito, cuenta.moneda)}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Límite Retiro Diario</p>
                <p className="font-medium">{formatMonto(saldo.limiteRetiroDiario, cuenta.moneda)}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Retiros Hoy</p>
                <p className="font-medium">{saldo.retirosRealizadosHoy.toLocaleString()}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Retiros Restantes</p>
                <p className="font-medium">{saldo.retirosRestantesHoy.toLocaleString()}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Información Adicional</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-gray-500">Tasa de Interés</p>
              <p className="font-medium">{(cuenta.tasaInteres * 100).toFixed(2)}%</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Monto Mínimo Requerido</p>
              <p className="font-medium">{formatMonto(cuenta.montoMinimoRequerido, cuenta.moneda)}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="flex gap-4">
        <Link href={`/dashboard/cuentas/${numeroCuenta}/saldo`} className="flex-1">
          <Button className="w-full bg-blue-500 hover:bg-blue-600 text-white">
            Ver Detalle de Saldo
          </Button>
        </Link>
      </div>
    </div>
  );
}