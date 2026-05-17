'use client';

import { useState, useEffect, useCallback, useMemo } from 'react';
import { useParams } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { cuentasApi } from '@/lib/api/client';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2 } from 'lucide-react';
import Link from 'next/link';
import { toast } from 'sonner';
import { MovimientoDetailModal } from '@/components/features/cuentas/movimiento-detail-modal';

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

interface CuentaInfo {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  moneda: string;
  saldoActual: number;
}

const formatMonto = (monto: number | null | undefined, moneda: string) => {
  if (monto == null) return '-';
  const simbolo = moneda === 'VES' ? 'Bs' : '$';
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
    month: '2-digit',
    day: '2-digit',
  });
};

export default function MovimientosPage() {
  const params = useParams();
  const numeroCuenta = params.numero as string;

  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [cuenta, setCuenta] = useState<CuentaInfo | null>(null);
  const [movimientos, setMovimientos] = useState<Movimiento[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMovimientos, setLoadingMovimientos] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [tipoFiltro, setTipoFiltro] = useState<string>('');
  const [fechaInicio, setFechaInicio] = useState<string>('');
  const [fechaFin, setFechaFin] = useState<string>('');
  // Filtros de monto: aplicados client-side sobre la página actual. Si en el
  // futuro se necesita filtrar sobre todo el histórico, mover a backend (PR-B
  // del issue #220).
  const [montoMinimo, setMontoMinimo] = useState<string>('');
  const [montoMaximo, setMontoMaximo] = useState<string>('');

  const [totales, setTotales] = useState({ depositos: 0, retiros: 0 });

  // Movimiento seleccionado para el modal de detalle (issue #220).
  const [selectedMov, setSelectedMov] = useState<Movimiento | null>(null);

  useEffect(() => {
    if (!numeroCuenta || isLoading) return;

    async function cargarCuenta() {
      try {
        const res = await cuentasApi.getCuenta(numeroCuenta);
        setCuenta(res.data);
      } catch (err) {
        console.error('Error al cargar cuenta:', err);
        setError('Error al cargar los datos de la cuenta');
      }
    }

    cargarCuenta();
  }, [numeroCuenta, isLoading]);

  const cargarMovimientos = useCallback(async () => {
    if (!numeroCuenta || isLoading) return;

    setLoadingMovimientos(true);
    try {
      const res = await cuentasApi.getMovimientos(
        numeroCuenta,
        page,
        10,
        fechaInicio || undefined,
        fechaFin || undefined,
        tipoFiltro || undefined
      );
      const data: MovimientosResponse = res.data;
      setMovimientos(data.movimientos || []);
      setTotalPages(data.totalPaginas);
      setTotalElements(data.totalElementos);

      const depositos = data.movimientos
        .filter((m: Movimiento) => m.tipo === 'DEPOSITO')
        .reduce((acc: number, m: Movimiento) => acc + Number(m.monto), 0);
      const retiros = data.movimientos
        .filter((m: Movimiento) => m.tipo === 'RETIRO')
        .reduce((acc: number, m: Movimiento) => acc + Number(m.monto), 0);
      setTotales({ depositos, retiros });
    } catch (err) {
      console.error('Error al cargar movimientos:', err);
    } finally {
      setLoadingMovimientos(false);
    }
  }, [numeroCuenta, page, fechaInicio, fechaFin, tipoFiltro, isLoading]);

  useEffect(() => {
    cargarMovimientos();
  }, [cargarMovimientos]);

  const aplicarFiltros = () => {
    if (fechaInicio && fechaFin && fechaFin < fechaInicio) {
      toast.error('La fecha fin debe ser mayor o igual a la fecha inicio');
      return;
    }
    setPage(0);
    cargarMovimientos();
  };

  const limpiarFiltros = () => {
    setTipoFiltro('');
    setFechaInicio('');
    setFechaFin('');
    setMontoMinimo('');
    setMontoMaximo('');
    setPage(0);
  };

  const moneda = cuenta?.moneda || 'VES';

  // Filtro de monto client-side sobre la página actual.
  // Justificación de scope: la página trae 10 movimientos por defecto,
  // filtrar 10 en cliente es razonable y evita un round-trip extra.
  // Si en el futuro se necesita filtrar sobre todo el histórico, se mueve
  // a backend (PR-B del issue #220) extendiendo el endpoint de movimientos.
  const movimientosFiltrados = useMemo(() => {
    const min = montoMinimo ? parseFloat(montoMinimo) : null;
    const max = montoMaximo ? parseFloat(montoMaximo) : null;
    // Si ningún filtro de monto es válido, devolver el array original
    if ((min === null || isNaN(min)) && (max === null || isNaN(max))) {
      return movimientos;
    }
    return movimientos.filter((m) => {
      const monto = Number(m.monto);
      if (min !== null && !isNaN(min) && monto < min) return false;
      if (max !== null && !isNaN(max) && monto > max) return false;
      return true;
    });
  }, [movimientos, montoMinimo, montoMaximo]);

  const hayFiltroMontoActivo =
    (montoMinimo !== '' && !isNaN(parseFloat(montoMinimo))) ||
    (montoMaximo !== '' && !isNaN(parseFloat(montoMaximo)));

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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href={`/dashboard/cuentas/${numeroCuenta}`}>
            <Button variant="outline">← Volver a Detalle</Button>
          </Link>
          <div>
            <h1 className="text-2xl font-bold">Movimientos</h1>
            <p className="text-gray-500">{numeroCuenta} - {cuenta.tipoCuenta}</p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-sm text-gray-500">Saldo Actual</p>
          <p className="text-xl font-bold text-green-600">{formatMonto(Number(cuenta.saldoActual), moneda)}</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Filtros</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="tipo">Tipo de Movimiento</Label>
              <select
                id="tipo"
                aria-label="Filtrar por tipo de movimiento"
                className="w-full h-10 px-3 border rounded-md bg-white"
                value={tipoFiltro}
                onChange={(e) => setTipoFiltro(e.target.value)}
              >
                <option value="">Todos</option>
                <option value="DEPOSITO">Depósitos</option>
                <option value="RETIRO">Retiros</option>
              </select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="fecha-inicio">Fecha Inicio</Label>
              <Input
                id="fecha-inicio"
                type="date"
                value={fechaInicio}
                onChange={(e) => setFechaInicio(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="fecha-fin">Fecha Fin</Label>
              <Input
                id="fecha-fin"
                type="date"
                value={fechaFin}
                onChange={(e) => setFechaFin(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="monto-minimo">Monto mínimo</Label>
              <Input
                id="monto-minimo"
                type="number"
                inputMode="decimal"
                step="0.01"
                min="0"
                placeholder="0.00"
                aria-describedby="monto-help"
                value={montoMinimo}
                onChange={(e) => setMontoMinimo(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="monto-maximo">Monto máximo</Label>
              <Input
                id="monto-maximo"
                type="number"
                inputMode="decimal"
                step="0.01"
                min="0"
                placeholder="Sin límite"
                aria-describedby="monto-help"
                value={montoMaximo}
                onChange={(e) => setMontoMaximo(e.target.value)}
              />
              <p id="monto-help" className="text-xs text-gray-500">
                Filtra solo la página actual
              </p>
            </div>
            <div className="flex items-end gap-2">
              <Button
                onClick={aplicarFiltros}
                className="bg-green-600 hover:bg-green-700"
                disabled={loadingMovimientos}
              >
                {loadingMovimientos ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Aplicando...
                  </>
                ) : (
                  'Aplicar'
                )}
              </Button>
              <Button variant="outline" onClick={limpiarFiltros}>
                Limpiar
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Totales del Período</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center p-4 bg-green-50 rounded-lg">
              <p className="text-sm text-gray-500">Total Depósitos</p>
              <p className="text-xl font-bold text-green-600">{formatMonto(totales.depositos, moneda)}</p>
            </div>
            <div className="text-center p-4 bg-red-50 rounded-lg">
              <p className="text-sm text-gray-500">Total Retiros</p>
              <p className="text-xl font-bold text-red-600">{formatMonto(totales.retiros, moneda)}</p>
            </div>
            <div className="text-center p-4 bg-blue-50 rounded-lg">
              <p className="text-sm text-gray-500">Neto</p>
              <p className={`text-xl font-bold ${totales.depositos - totales.retiros >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {formatMonto(totales.depositos - totales.retiros, moneda)}
              </p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">Movimientos</p>
              <p className="text-xl font-bold text-gray-600">{totalElements}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Lista de Movimientos</CardTitle>
        </CardHeader>
        <CardContent>
          {loadingMovimientos ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-green-600" />
            </div>
          ) : movimientosFiltrados.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-500 mb-4">
                {hayFiltroMontoActivo && movimientos.length > 0
                  ? 'Ningún movimiento de la página actual coincide con el filtro de monto. Intenta cambiar de página o ajustar el rango.'
                  : 'No hay movimientos que coincidan con los filtros'}
              </p>
              <Button variant="outline" onClick={limpiarFiltros}>
                Limpiar filtros
              </Button>
            </div>
          ) : (
            <>
              {hayFiltroMontoActivo && (
                <p className="mb-3 text-xs text-gray-500" role="status">
                  Mostrando {movimientosFiltrados.length} de {movimientos.length} movimientos
                  de la página actual (filtro de monto aplicado en cliente).
                </p>
              )}
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th scope="col" className="text-left py-3 px-3 text-sm font-medium text-gray-500">Fecha</th>
                      <th scope="col" className="text-left py-3 px-3 text-sm font-medium text-gray-500">Operación</th>
                      <th scope="col" className="text-left py-3 px-3 text-sm font-medium text-gray-500">Tipo</th>
                      <th scope="col" className="text-left py-3 px-3 text-sm font-medium text-gray-500">Descripción</th>
                      <th scope="col" className="text-right py-3 px-3 text-sm font-medium text-gray-500">Monto</th>
                      <th scope="col" className="text-right py-3 px-3 text-sm font-medium text-gray-500">Saldo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {movimientosFiltrados.map((mov) => (
                      <tr
                        key={mov.id}
                        className="border-b hover:bg-gray-50 cursor-pointer focus:outline-none focus:bg-gray-50"
                        role="button"
                        tabIndex={0}
                        aria-label={`Ver detalle del movimiento ${mov.numeroOperacion}`}
                        onClick={() => setSelectedMov(mov)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' || e.key === ' ') {
                            e.preventDefault();
                            setSelectedMov(mov);
                          }
                        }}
                      >
                        <td className="py-3 px-3 text-sm">
                          <div>{formatFechaCorta(mov.fechaMovimiento)}</div>
                          <div className="text-gray-400 text-xs">{mov.numeroOperacion}</div>
                        </td>
                        <td className="py-3 px-3 text-sm">
                          <span className="font-medium">{mov.canalOrigen}</span>
                        </td>
                        <td className="py-3 px-3">
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
                        <td className="py-3 px-3 text-sm text-gray-600">
                          {mov.descripcion || '-'}
                          {mov.referencia && (
                            <div className="text-gray-400 text-xs">Ref: {mov.referencia}</div>
                          )}
                        </td>
                        <td className={`py-3 px-3 text-right font-medium ${
                          mov.tipo === 'DEPOSITO' ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {mov.tipo === 'DEPOSITO' ? '+' : '-'}{formatMonto(mov.monto, moneda)}
                        </td>
                        <td className="py-3 px-3 text-right text-gray-600">
                          {formatMonto(mov.saldoPosterior, moneda)}
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

      <MovimientoDetailModal
        movimiento={selectedMov}
        moneda={moneda}
        onClose={() => setSelectedMov(null)}
      />
    </div>
  );
}