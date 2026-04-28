'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';
import { creditosApi } from '@/lib/api/client';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface TipoCredito {
  id: string;
  nombre: string;
  descripcion: string;
  tasaInteresAnual: number;
  plazoMinimoMeses: number;
  plazoMaximoMeses: number;
  montoMinimo: number;
  montoMaximo: number;
  frecuenciaPago: string;
}

interface SimulacionResult {
  cuotaMensual: number;
  totalPagar: number;
  totalIntereses: number;
  tasaEfectivaMensual: number;
  planAmortizacion: Array<{
    numeroCuota: number;
    fechaPago: string;
    capital: number;
    interes: number;
    cuota: number;
    saldo: number;
  }>;
}

export default function SimuladorPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [tiposCredito, setTiposCredito] = useState<TipoCredito[]>([]);
  const [loading, setLoading] = useState(true);
  const [simulando, setSimulando] = useState(false);

  const [tipoCreditoId, setTipoCreditoId] = useState<string>('');
  const [monto, setMonto] = useState<string>('');
  const [plazo, setPlazo] = useState<string>('');
  const [resultado, setResultado] = useState<SimulacionResult | null>(null);

  const [tipoSeleccionado, setTipoSeleccionado] = useState<TipoCredito | null>(null);

  useEffect(() => {
    if (isLoading) return;

    async function cargarTipos() {
      setLoading(true);
      try {
        const res = await creditosApi.getTiposCredito();
        setTiposCredito(res.data || []);
        if (res.data?.length > 0) {
          setTipoCreditoId(res.data[0].id);
          setTipoSeleccionado(res.data[0]);
        }
      } catch (err) {
        console.error('Error al cargar tipos:', err);
        toast.error('Error al cargar tipos de crédito');
      } finally {
        setLoading(false);
      }
    }

    cargarTipos();
  }, [isLoading]);

  const handleTipoChange = (id: string) => {
    setTipoCreditoId(id);
    const tipo = tiposCredito.find(t => t.id === id);
    setTipoSeleccionado(tipo || null);
    if (tipo) {
      setMonto(String(tipo.montoMinimo));
      setPlazo(String(tipo.plazoMinimoMeses));
    }
  };

  const handleSimular = async () => {
    if (!tipoCreditoId || !monto || !plazo) {
      toast.error('Complete todos los campos');
      return;
    }

    const montoNum = parseFloat(monto);
    const plazoNum = parseInt(plazo);

    if (isNaN(montoNum) || isNaN(plazoNum)) {
      toast.error('Monto y plazo deben ser números válidos');
      return;
    }

    if (montoNum <= 0 || plazoNum <= 0) {
      toast.error('Monto y plazo deben ser mayores a cero');
      return;
    }

    if (montoNum > 10000000 || plazoNum > 360) {
      toast.error('Valores exceden límites permitidos');
      return;
    }

    if (tipoSeleccionado) {
      if (montoNum < tipoSeleccionado.montoMinimo || montoNum > tipoSeleccionado.montoMaximo) {
        toast.error(`Monto debe estar entre ${tipoSeleccionado.montoMinimo} y ${tipoSeleccionado.montoMaximo}`);
        return;
      }
      if (plazoNum < tipoSeleccionado.plazoMinimoMeses || plazoNum > tipoSeleccionado.plazoMaximoMeses) {
        toast.error(`Plazo debe estar entre ${tipoSeleccionado.plazoMinimoMeses} y ${tipoSeleccionado.plazoMaximoMeses} meses`);
        return;
      }
    }

    setSimulando(true);
    try {
      const res = await creditosApi.simular({
        tipoCreditoId,
        monto: montoNum,
        plazoMeses: plazoNum,
      });
      setResultado(res.data);
      toast.success('Simulación completada');
    } catch (err) {
      console.error('Error al simular:', err);
      toast.error('Error al realizar simulación');
    } finally {
      setSimulando(false);
    }
  };

  const formatMonto = (monto: number) => {
    return `$${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
  };

  if (loading || isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/dashboard/creditos">
          <Button variant="outline">← Volver</Button>
        </Link>
        <h1 className="text-2xl font-bold text-gray-900">Simulador de Crédito</h1>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Parámetros del Crédito</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="tipo">Tipo de Crédito</Label>
              <select
                id="tipo"
                aria-label="Seleccionar tipo de crédito"
                className="w-full h-10 px-3 border rounded-md bg-white"
                value={tipoCreditoId}
                onChange={(e) => handleTipoChange(e.target.value)}
              >
                {tiposCredito.map((tipo) => (
                  <option key={tipo.id} value={tipo.id}>
                    {tipo.nombre} - {(tipo.tasaInteresAnual * 100).toFixed(2)}% TEA
                  </option>
                ))}
              </select>
            </div>

            {tipoSeleccionado && (
              <div className="bg-blue-50 border border-blue-200 rounded p-3 text-sm">
                <p className="font-medium text-blue-800 mb-2">{tipoSeleccionado.nombre}</p>
                <p className="text-blue-600 mb-2">{tipoSeleccionado.descripcion}</p>
                <div className="grid grid-cols-2 gap-2 text-blue-700">
                  <p>Monto: {formatMonto(tipoSeleccionado.montoMinimo)} - {formatMonto(tipoSeleccionado.montoMaximo)}</p>
                  <p>Plazo: {tipoSeleccionado.plazoMinimoMeses} - {tipoSeleccionado.plazoMaximoMeses} meses</p>
                </div>
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="monto">Monto a Solicitar</Label>
              <Input
                id="monto"
                type="number"
                min={tipoSeleccionado?.montoMinimo || 0}
                max={tipoSeleccionado?.montoMaximo || 999999}
                step="0.01"
                placeholder="0.00"
                value={monto}
                onChange={(e) => setMonto(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="plazo">Plazo (meses)</Label>
              <Input
                id="plazo"
                type="number"
                min={tipoSeleccionado?.plazoMinimoMeses || 1}
                max={tipoSeleccionado?.plazoMaximoMeses || 360}
                placeholder="12"
                value={plazo}
                onChange={(e) => setPlazo(e.target.value)}
              />
            </div>

            <Button
              onClick={handleSimular}
              className="w-full bg-green-600 hover:bg-green-700"
              disabled={simulando || !tipoCreditoId || !monto || !plazo}
            >
              {simulando ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Simulando...
                </>
              ) : (
                'Calcular Cuota'
              )}
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Resultado de Simulación</CardTitle>
          </CardHeader>
          <CardContent>
            {!resultado ? (
              <div className="text-center py-8 text-gray-500">
                <p>Ingrese los parámetros y presione "Calcular Cuota"</p>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="grid grid-cols-3 gap-4">
                  <div className="text-center p-4 bg-green-50 rounded-lg">
                    <p className="text-sm text-gray-500">Cuota Mensual</p>
                    <p className="text-xl font-bold text-green-600">{formatMonto(resultado.cuotaMensual)}</p>
                  </div>
                  <div className="text-center p-4 bg-blue-50 rounded-lg">
                    <p className="text-sm text-gray-500">Total Intereses</p>
                    <p className="text-xl font-bold text-blue-600">{formatMonto(resultado.totalIntereses)}</p>
                  </div>
                  <div className="text-center p-4 bg-purple-50 rounded-lg">
                    <p className="text-sm text-gray-500">Total a Pagar</p>
                    <p className="text-xl font-bold text-purple-600">{formatMonto(resultado.totalPagar)}</p>
                  </div>
                </div>

                <div className="text-center text-sm text-gray-500">
                  <p>Tasa Efectiva Mensual: {(resultado.tasaEfectivaMensual * 100).toFixed(4)}%</p>
                </div>

                {resultado.planAmortizacion && resultado.planAmortizacion.length > 0 && (
                  <div className="border rounded-lg overflow-hidden">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="bg-gray-50">
                          <th className="text-left py-2 px-3">#</th>
                          <th className="text-right py-2 px-3">Capital</th>
                          <th className="text-right py-2 px-3">Interés</th>
                          <th className="text-right py-2 px-3">Cuota</th>
                          <th className="text-right py-2 px-3">Saldo</th>
                        </tr>
                      </thead>
                      <tbody>
                        {resultado.planAmortizacion.slice(0, 12).map((fila) => (
                          <tr key={fila.numeroCuota} className="border-t">
                            <td className="py-2 px-3">{fila.numeroCuota}</td>
                            <td className="py-2 px-3 text-right">{formatMonto(fila.capital)}</td>
                            <td className="py-2 px-3 text-right">{formatMonto(fila.interes)}</td>
                            <td className="py-2 px-3 text-right font-medium">{formatMonto(fila.cuota)}</td>
                            <td className="py-2 px-3 text-right text-gray-500">{formatMonto(fila.saldo)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {resultado.planAmortizacion.length > 12 && (
                      <div className="text-center py-2 text-sm text-gray-500 bg-gray-50">
                        ... y {resultado.planAmortizacion.length - 12} cuotas más
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}