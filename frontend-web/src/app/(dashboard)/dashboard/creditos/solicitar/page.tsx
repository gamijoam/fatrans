'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { creditosApi, cuentasApi } from '@/lib/api/client';
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

interface Cuenta {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  moneda: string;
  saldo: number;
}

export default function SolicitarCreditoPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);
  const router = useRouter();

  const [tiposCredito, setTiposCredito] = useState<TipoCredito[]>([]);
  const [cuentas, setCuentas] = useState<Cuenta[]>([]);
  const [loading, setLoading] = useState(true);
  const [enviando, setEnviando] = useState(false);

  const [tipoCreditoId, setTipoCreditoId] = useState<string>('');
  const [monto, setMonto] = useState<string>('');
  const [plazo, setPlazo] = useState<string>('');
  const [cuentaGarantiaId, setCuentaGarantiaId] = useState<string>('');

  const [tipoSeleccionado, setTipoSeleccionado] = useState<TipoCredito | null>(null);

  useEffect(() => {
    if (isLoading) return;

    async function cargarDatos() {
      setLoading(true);
      try {
        const [tiposRes, cuentasRes] = await Promise.all([
          creditosApi.getTiposCredito(),
          user?.socioId ? cuentasApi.getCuentas(user.socioId) : Promise.resolve({ data: [] }),
        ]);

        const tipos = tiposRes.data.tiposCredito || [];
        setTiposCredito(tipos);

        const cuentasList = cuentasRes.data.cuentas || [];
        setCuentas(cuentasList);

        if (tipos.length > 0) {
          setTipoCreditoId(tipos[0].id);
          setTipoSeleccionado(tipos[0]);
          setMonto(String(tipos[0].montoMinimo));
          setPlazo(String(tipos[0].plazoMinimoMeses));
        }
      } catch (err) {
        console.error('Error al cargar datos:', err);
        toast.error('Error al cargar datos');
      } finally {
        setLoading(false);
      }
    }

    cargarDatos();
  }, [isLoading, user?.socioId]);

  const handleTipoChange = (id: string) => {
    setTipoCreditoId(id);
    const tipo = tiposCredito.find(t => t.id === id);
    setTipoSeleccionado(tipo || null);
    if (tipo) {
      setMonto(String(tipo.montoMinimo));
      setPlazo(String(tipo.plazoMinimoMeses));
    }
  };

  const handleSolicitar = async () => {
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

    setEnviando(true);
    try {
      const res = await creditosApi.crearSolicitud({
        tipoCreditoId,
        montoSolicitado: montoNum,
        plazoMeses: plazoNum,
        cuentaGarantiaId: cuentaGarantiaId || undefined,
        canalOrigen: 'WEB',
      });
      toast.success('Solicitud creada exitosamente');
      router.push(`/dashboard/creditos/${res.data.numeroSolicitud}`);
    } catch (err: unknown) {
      console.error('Error al crear solicitud:', err);
      const errorMessage = err instanceof Error ? err.message : 'Error al crear solicitud';
      toast.error(errorMessage);
    } finally {
      setEnviando(false);
    }
  };

  const formatMonto = (monto: number | null | undefined, moneda = 'USD') => {
    if (monto == null) return '-';
    if (moneda === 'VES') {
      return `Bs ${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
    }
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
        <h1 className="text-2xl font-bold text-gray-900">Solicitar Crédito</h1>
      </div>

      {tiposCredito.length === 0 ? (
        <Card>
          <CardContent className="py-8">
            <div className="text-center text-gray-500">
              <p className="mb-2">No hay tipos de crédito disponibles en este momento.</p>
              <p className="text-sm">Contacte al administrador para más información.</p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>Datos de la Solicitud</CardTitle>
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

            {cuentas.length > 0 && (
              <div className="space-y-2">
                <Label htmlFor="garantia">Cuenta de Garantía (opcional)</Label>
                <select
                  id="garantia"
                  aria-label="Seleccionar cuenta de garantía"
                  className="w-full h-10 px-3 border rounded-md bg-white"
                  value={cuentaGarantiaId}
                  onChange={(e) => setCuentaGarantiaId(e.target.value)}
                >
                  <option value="">Sin cuenta de garantía</option>
                  {cuentas.map((cuenta) => (
                    <option key={cuenta.id} value={cuenta.id}>
                      {cuenta.numeroCuenta} - {cuenta.moneda} - Saldo: {formatMonto(cuenta.saldo, cuenta.moneda)}
                    </option>
                  ))}
                </select>
                <p className="text-xs text-gray-500">
                  Seleccione una cuenta existente como garantía adicional para su crédito.
                </p>
              </div>
            )}

            <Button
              onClick={handleSolicitar}
              className="w-full bg-green-600 hover:bg-green-700 mt-4"
              disabled={enviando || !tipoCreditoId || !monto || !plazo}
            >
              {enviando ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Enviando Solicitud...
                </>
              ) : (
                'Enviar Solicitud de Crédito'
              )}
            </Button>
          </CardContent>
        </Card>
      )}

      <Card className="bg-gray-50">
        <CardContent className="py-4">
          <p className="text-sm text-gray-600 text-center">
            Su solicitud será revisada por un gestor. Recibirá una notificación cuando sea procesada.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
