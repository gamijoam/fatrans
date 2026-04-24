'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { cuentasApi } from '@/lib/api/client';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import Link from 'next/link';

interface Movimiento {
  id: string;
  numeroOperacion: string;
  cuentaAhorroId: string;
  socioId: string;
  tipo: string;
  monto: number;
  saldoAnterior: number;
  saldoPosterior: number;
  descripcion: string | null;
  referencia: string | null;
  canalOrigen: string;
  estado: string;
  fechaMovimiento: string;
  fechaValor: string;
}

interface MovimientosResponse {
  numeroCuenta: string;
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
  movimientos: Movimiento[];
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

interface SaldoResponse {
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

export default function SaldoDetailPage() {
  const params = useParams();
  const numeroCuenta = params.numero as string;

  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [cuenta, setCuenta] = useState<{ moneda: string } | null>(null);
  const [saldo, setSaldo] = useState<SaldoResponse | null>(null);
  const [movimientos, setMovimientos] = useState<Movimiento[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMovimientos, setLoadingMovimientos] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

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
        console.error('Error al cargar datos:', err);
        setError('Error al cargar los datos');
      } finally {
        setLoading(false);
      }
    }

    cargarDatos();
  }, [numeroCuenta, isLoading]);

  useEffect(() => {
    if (!numeroCuenta || isLoading) return;

    async function cargarMovimientos() {
      setLoadingMovimientos(true);
      try {
        const res = await cuentasApi.getMovimientos(numeroCuenta, page, 20);
        const data: MovimientosResponse = res.data;
        setMovimientos(data.movimientos || []);
        setTotalPages(data.totalPaginas);
        setTotalElements(data.totalElementos);
      } catch (err: unknown) {
        console.error('Error al cargar movimientos:', err);
      } finally {
        setLoadingMovimientos(false);
      }
    }

    cargarMovimientos();
  }, [numeroCuenta, page, isLoading]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  if (error || !cuenta || !saldo) {
    return (
      <div className="space-y-4">
        <Link href={`/dashboard/cuentas/${numeroCuenta}`}>
          <Button variant="outline">← Volver a Detalle de Cuenta</Button>
        </Link>
        <Card>
          <CardContent className="py-8">
            <p className="text-center text-red-600">{error || 'Datos no encontrados'}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const formatMonto = (monto: number) => {
    const simbolo = cuenta?.moneda === 'VES' ? 'Bs' : '$';
    return `${simbolo} ${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
  };

  const formatFecha = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-VE', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatFechaCorta = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-VE', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Link href={`/dashboard/cuentas/${numeroCuenta}`}>
          <Button variant="outline">← Volver a Detalle</Button>
        </Link>
        <h1 className="text-2xl font-bold">Detalle de Saldo</h1>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <Card className="bg-gradient-to-br from-green-500 to-green-600 text-white">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium opacity-90">Saldo Disponible</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{formatMonto(saldo.saldoDisponible)}</p>
            <p className="text-sm opacity-80 mt-1">Para transferencias y retiros</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Saldo Actual</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-gray-900">{formatMonto(saldo.saldoActual)}</p>
            <p className="text-sm text-gray-500 mt-1">Total en cuenta</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Saldo Retenido</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-orange-600">{formatMonto(saldo.saldoRetenido)}</p>
            <p className="text-sm text-gray-500 mt-1">En procesos pendientes</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Límites de Operaciones</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">Límite de Depósito</p>
              <p className="text-lg font-bold text-green-600">{formatMonto(saldo.limiteDeposito)}</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">Límite Retiro Diario</p>
              <p className="text-lg font-bold text-blue-600">{formatMonto(saldo.limiteRetiroDiario)}</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">Retiros Realizados Hoy</p>
              <p className="text-lg font-bold text-orange-600">{saldo.retirosRealizadosHoy}</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">Retiros Restantes Hoy</p>
              <p className="text-lg font-bold text-purple-600">{saldo.retirosRestantesHoy}</p>
            </div>
          </div>
          <p className="text-sm text-gray-500 mt-4 text-center">
            Fecha de consulta: {formatFecha(saldo.fechaConsulta)}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Últimos Movimientos</CardTitle>
        </CardHeader>
        <CardContent>
          {loadingMovimientos ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-green-600" />
            </div>
          ) : movimientos.length === 0 ? (
            <p className="text-center text-gray-500 py-8">No hay movimientos registrados</p>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b">
                      <th className="text-left py-3 px-2 text-sm font-medium text-gray-500">Fecha</th>
                      <th className="text-left py-3 px-2 text-sm font-medium text-gray-500">Operación</th>
                      <th className="text-left py-3 px-2 text-sm font-medium text-gray-500">Tipo</th>
                      <th className="text-right py-3 px-2 text-sm font-medium text-gray-500">Monto</th>
                      <th className="text-right py-3 px-2 text-sm font-medium text-gray-500">Saldo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {movimientos.map((mov) => (
                      <tr key={mov.id} className="border-b hover:bg-gray-50">
                        <td className="py-3 px-2 text-sm">
                          <div>{formatFechaCorta(mov.fechaMovimiento)}</div>
                          <div className="text-gray-400 text-xs">{mov.numeroOperacion}</div>
                        </td>
                        <td className="py-3 px-2 text-sm">
                          <div>{mov.descripcion || mov.canalOrigen}</div>
                          {mov.referencia && (
                            <div className="text-gray-400 text-xs">Ref: {mov.referencia}</div>
                          )}
                        </td>
                        <td className="py-3 px-2">
                          <span
                            className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${
                              mov.tipo === 'DEPOSITO'
                                ? 'bg-green-100 text-green-800'
                                : 'bg-red-100 text-red-800'
                            }`}
                          >
                            {mov.tipo === 'DEPOSITO' ? 'Depósito' : 'Retiro'}
                          </span>
                        </td>
                        <td className={`py-3 px-2 text-right font-medium ${
                          mov.tipo === 'DEPOSITO' ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {mov.tipo === 'DEPOSITO' ? '+' : '-'}{formatMonto(mov.monto)}
                        </td>
                        <td className="py-3 px-2 text-right text-gray-600">
                          {formatMonto(mov.saldoPosterior)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {totalPages > 1 && (
                <div className="flex justify-center items-center gap-4 mt-6">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                  >
                    Anterior
                  </Button>
                  <span className="text-sm text-gray-500">
                    Página {page + 1} de {totalPages} ({totalElements} movimientos)
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                  >
                    Siguiente
                  </Button>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}