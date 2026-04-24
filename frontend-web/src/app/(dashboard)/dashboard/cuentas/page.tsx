'use client';

import { useState, useEffect } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { cuentasApi } from '@/lib/api/client';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Loader2 } from 'lucide-react';
import { toastSuccess, toastError } from '@/components/ui/toast-helpers';

interface Cuenta {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  saldo: number;
}

interface CuentasResponse {
  cuentas: Array<{
    id: string;
    numeroCuenta: string;
    tipoCuenta: string;
    saldoActual: number;
  }>;
}

interface MovimientoResponse {
  saldoPosterior: number;
}

export default function CuentasPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [cuentas, setCuentas] = useState<Cuenta[]>([]);
  const [loadingCuentas, setLoadingCuentas] = useState(false);
  const [loaded, setLoaded] = useState(false);

  const [monto, setMonto] = useState('');
  const [loadingOperacion, setLoadingOperacion] = useState(false);
  const [operacion, setOperacion] = useState<'deposito' | 'retiro' | null>(null);
  const [openConfirm, setOpenConfirm] = useState(false);
  const [cuentaSeleccionada, setCuentaSeleccionada] = useState<Cuenta | null>(null);

  const [errores, setErrores] = useState<{ monto?: string }>({});

  useEffect(() => {
    if (user?.socioId && !loaded && !loadingCuentas) {
      cargarCuentas();
    }
  }, [user?.socioId]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  async function cargarCuentas() {
    if (!user?.socioId || loaded) return;
    setLoadingCuentas(true);
    try {
      console.log('Cargando cuentas para socioId:', user.socioId);
      const res = await cuentasApi.getCuentas(user.socioId);
      console.log('Response cuentas:', res.data);
      const cuentasData: CuentasResponse = res.data;
      setCuentas(
        cuentasData.cuentas.map((c) => ({
          id: c.id,
          numeroCuenta: c.numeroCuenta,
          tipoCuenta: c.tipoCuenta,
          saldo: Number(c.saldoActual),
        }))
      );
      setLoaded(true);
    } catch (err) {
      console.error('Error al cargar cuentas:', err);
      toastError('Error al cargar cuentas');
    } finally {
      setLoadingCuentas(false);
    }
  }

  function validarMonto(value: string, tipo: 'deposito' | 'retiro'): string | undefined {
    const num = parseFloat(value);
    if (!value || isNaN(num)) return 'Ingrese un monto válido';
    if (num < 0.01) return 'El monto mínimo es $0.01';
    if (num > 500000) return 'El monto máximo es $500,000';
    if (tipo === 'retiro') {
      if (cuentaSeleccionada && num > cuentaSeleccionada.saldo) {
        return 'No puede retirar más del saldo disponible';
      }
    }
    return undefined;
  }

  function abrirFormulario(tipo: 'deposito' | 'retiro', cuenta: Cuenta) {
    setOperacion(tipo);
    setCuentaSeleccionada(cuenta);
    setMonto('');
    setErrores({});
    setOpenConfirm(true);
  }

  async function confirmarOperacion() {
    if (!cuentaSeleccionada || !operacion) return;

    const error = validarMonto(monto, operacion);
    if (error) {
      setErrores({ monto: error });
      return;
    }

    setLoadingOperacion(true);
    try {
      const numMonto = parseFloat(monto);
      let nuevoSaldo: number;
      if (operacion === 'deposito') {
        const res = await cuentasApi.deposito(cuentaSeleccionada.numeroCuenta, numMonto);
        nuevoSaldo = res.data.saldoPosterior;
        toastSuccess('Depósito realizado', `Se depositaron $${numMonto.toFixed(2)} a la cuenta ${cuentaSeleccionada.numeroCuenta}`);
      } else {
        const res = await cuentasApi.retiro(cuentaSeleccionada.numeroCuenta, numMonto);
        nuevoSaldo = res.data.saldoPosterior;
        toastSuccess('Retiro realizado', `Se retiraron $${numMonto.toFixed(2)} de la cuenta ${cuentaSeleccionada.numeroCuenta}`);
      }
      setCuentas((prev) =>
        prev.map((c) =>
          c.id === cuentaSeleccionada.id ? { ...c, saldo: nuevoSaldo } : c
        )
      );
      setOpenConfirm(false);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Error en la operación';
      toastError('Operación fallida', message);
    } finally {
      setLoadingOperacion(false);
    }
  }

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Mis Cuentas</h1>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Cuentas de Ahorro</CardTitle>
            <Button variant="outline" size="sm" onClick={cargarCuentas} disabled={loadingCuentas}>
              {loadingCuentas ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
              Actualizar
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {!loaded && !loadingCuentas && cuentas.length === 0 && (
            <p className="text-center text-gray-500 mb-4">Cargando cuentas...</p>
          )}
          {loadingCuentas && cuentas.length === 0 && (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-green-600" />
            </div>
          )}
          {cuentas.length === 0 && loaded && (
            <p className="text-gray-500 text-center py-8">No tienes cuentas de ahorro activas.</p>
          )}
          <div className="space-y-4">
            {cuentas.map((cuenta) => (
              <div key={cuenta.id} className="border rounded-lg p-4 bg-white">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <p className="font-semibold text-lg">{cuenta.numeroCuenta}</p>
                    <p className="text-sm text-gray-500">{cuenta.tipoCuenta}</p>
                  </div>
                  <p className="text-2xl font-bold text-green-600">
                    ${cuenta.saldo.toLocaleString('es-MX', { minimumFractionDigits: 2 })}
                  </p>
                </div>
                <div className="flex gap-2">
                  <Dialog open={openConfirm && cuentaSeleccionada?.id === cuenta.id} onOpenChange={(open) => {
                    if (!open) {
                      setOpenConfirm(false);
                      setCuentaSeleccionada(null);
                    }
                  }}>
                    <DialogTrigger asChild>
                      <Button
                        className="bg-green-500 hover:bg-green-600 text-white"
                        onClick={() => abrirFormulario('deposito', cuenta)}
                      >
                        Depósito
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Confirmar Depósito</DialogTitle>
                        <DialogDescription>
                          Ingrese el monto a depositar en la cuenta {cuenta.numeroCuenta}
                        </DialogDescription>
                      </DialogHeader>
                      {operacion === 'deposito' && (
                        <div className="space-y-4 py-4">
                          <div className="space-y-2">
                            <Label htmlFor="monto-deposito">Monto a depositar</Label>
                            <Input
                              id="monto-deposito"
                              type="number"
                              step="0.01"
                              min="0.01"
                              max="500000"
                              placeholder="0.00"
                              value={monto}
                              onChange={(e) => {
                                setMonto(e.target.value);
                                setErrores({});
                              }}
                              className={errores.monto ? 'border-red-500' : ''}
                            />
                            {errores.monto && (
                              <p className="text-sm text-red-500">{errores.monto}</p>
                            )}
                          </div>
                          <p className="text-sm text-gray-500">
                            Límite: $0.01 - $500,000.00
                          </p>
                        </div>
                      )}
                      <DialogFooter>
                        <Button variant="outline" onClick={() => { setOpenConfirm(false); setCuentaSeleccionada(null); }}>
                          Cancelar
                        </Button>
                        <Button
                          className="bg-green-500 hover:bg-green-600 text-white"
                          onClick={confirmarOperacion}
                          disabled={loadingOperacion || !monto}
                        >
                          {loadingOperacion ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                          Confirmar Depósito
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>

                  <Dialog open={openConfirm && operacion === 'retiro' && cuentaSeleccionada?.id === cuenta.id} onOpenChange={(open) => {
                    if (!open) {
                      setOpenConfirm(false);
                      setCuentaSeleccionada(null);
                    }
                  }}>
                    <DialogTrigger asChild>
                      <Button
                        className="bg-blue-500 hover:bg-blue-600 text-white"
                        onClick={() => abrirFormulario('retiro', cuenta)}
                      >
                        Retiro
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Confirmar Retiro</DialogTitle>
                        <DialogDescription>
                          Ingrese el monto a retirar de la cuenta {cuenta.numeroCuenta}
                        </DialogDescription>
                      </DialogHeader>
                      {operacion === 'retiro' && (
                        <div className="space-y-4 py-4">
                          <div className="space-y-2">
                            <Label htmlFor="monto-retiro">Monto a retirar</Label>
                            <Input
                              id="monto-retiro"
                              type="number"
                              step="0.01"
                              min="0.01"
                              max="500000"
                              placeholder="0.00"
                              value={monto}
                              onChange={(e) => {
                                setMonto(e.target.value);
                                setErrores({});
                              }}
                              className={errores.monto ? 'border-red-500' : ''}
                            />
                            {errores.monto && (
                              <p className="text-sm text-red-500">{errores.monto}</p>
                            )}
                          </div>
                          <div className="bg-yellow-50 border border-yellow-200 rounded p-3">
                            <p className="text-sm text-yellow-800">
                              <strong>Nota:</strong> Límite diario de retiro: $50,000.00
                            </p>
                          </div>
                          <p className="text-sm text-gray-500">
                            Saldo disponible: ${cuenta.saldo.toLocaleString('es-MX', { minimumFractionDigits: 2 })}
                          </p>
                        </div>
                      )}
                      <DialogFooter>
                        <Button variant="outline" onClick={() => { setOpenConfirm(false); setCuentaSeleccionada(null); }}>
                          Cancelar
                        </Button>
                        <Button
                          className="bg-blue-500 hover:bg-blue-600 text-white"
                          onClick={confirmarOperacion}
                          disabled={loadingOperacion || !monto}
                        >
                          {loadingOperacion ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                          Confirmar Retiro
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}