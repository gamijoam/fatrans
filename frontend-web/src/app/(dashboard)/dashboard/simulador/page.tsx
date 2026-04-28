'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Calculator, DollarSign, Calendar, Percent, RefreshCw,
  Clock, AlertTriangle, CheckCircle, Loader2, Download
} from 'lucide-react';
import { toast } from 'sonner';

interface Cuota {
  numero: number;
  fechaPago: string;
  cuotaMensual: number;
  capital: number;
  interes: number;
  saldoRestante: number;
}

interface SimulacionResponse {
  monto: number;
  plazoMeses: number;
  tasaAnual: number;
  cuotaMensual: number;
  totalPagar: number;
  totalInteres: number;
  tablaAmortizacion: Cuota[];
}

interface TipoCredito {
  id: string;
  nombre: string;
  tasaBase: number;
  colateralPorcentaje: number;
  plazoMin: number;
  plazoMax: number;
  montoMin: number;
  montoMax: number;
}

const TIPOS_CREDITO: TipoCredito[] = [
  { id: 'educacion', nombre: 'Crédito Educación', tasaBase: 14.5, colateralPorcentaje: 10, plazoMin: 6, plazoMax: 60, montoMin: 1000, montoMax: 500000 },
  { id: 'micro', nombre: 'Micro Crédito', tasaBase: 18, colateralPorcentaje: 15, plazoMin: 3, plazoMax: 36, montoMin: 500, montoMax: 100000 },
  { id: 'vehiculo', nombre: 'Crédito Vehículo', tasaBase: 12, colateralPorcentaje: 20, plazoMin: 12, plazoMax: 84, montoMin: 5000, montoMax: 2000000 },
];

const TIPOS_DOCUMENTO_LABELS: Record<string, string> = {
  ESTADO_CUENTA: 'Estado de Cuenta',
  CONSTANCIA_AFILIACION: 'Constancia de Afiliación',
  CONTRATO_ADHESION: 'Contrato de Adhesión',
  PAGARE: 'Pagaré',
  TABLA_AMORTIZACION: 'Tabla de Amortización',
  CARTA_BENEFICIARIOS: 'Carta de Beneficiarios',
};

export default function DashboardSimuladorPagina() {
  const [monto, setMonto] = useState<string>('');
  const [plazoMeses, setPlazoMeses] = useState<string>('12');
  const [tasaAnual, setTasaAnual] = useState<string>('14.5');
  const [simulacion, setSimulacion] = useState<SimulacionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [tipoCreditoSeleccionado, setTipoCreditoSeleccionado] = useState<string>('educacion');

  const tipoCredito = TIPOS_CREDITO.find(t => t.id === tipoCreditoSeleccionado);

  const handleSelectTipo = (tipoId: string) => {
    setTipoCreditoSeleccionado(tipoId);
    const tipo = TIPOS_CREDITO.find(t => t.id === tipoId);
    if (tipo) {
      setTasaAnual(String(tipo.tasaBase));
      if (monto) {
        const numMonto = parseFloat(monto.replace(/[^0-9.]/g, ''));
        if (numMonto > tipo.montoMax) {
          setMonto(String(tipo.montoMax));
        }
        if (numMonto < tipo.montoMin) {
          setMonto(String(tipo.montoMin));
        }
      }
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('es-VE', {
      style: 'currency',
      currency: 'VES',
      minimumFractionDigits: 2,
    }).format(value);
  };

  const formatNumber = (value: number) => {
    return new Intl.NumberFormat('es-VE').format(value);
  };

  const parseNumber = (value: string) => {
    return parseFloat(value.replace(/[^0-9.]/g, '')) || 0;
  };

  const handleSimular = async () => {
    const numMonto = parseNumber(monto);

    if (!numMonto || numMonto <= 0) {
      toast.error('Ingrese un monto válido');
      return;
    }

    if (tipoCredito && (numMonto < tipoCredito.montoMin || numMonto > tipoCredito.montoMax)) {
      toast.error(`Monto debe estar entre ${formatNumber(tipoCredito.montoMin)} y ${formatNumber(tipoCredito.montoMax)}`);
      return;
    }

    const numPlazo = parseInt(plazoMeses);
    if (!numPlazo || numPlazo <= 0) {
      toast.error('Ingrese un plazo válido');
      return;
    }

    if (tipoCredito && (numPlazo < tipoCredito.plazoMin || numPlazo > tipoCredito.plazoMax)) {
      toast.error(`Plazo debe estar entre ${tipoCredito.plazoMin} y ${tipoCredito.plazoMax} meses`);
      return;
    }

    const numTasa = parseFloat(tasaAnual);
    if (!numTasa || numTasa <= 0 || numTasa > 100) {
      toast.error('Ingrese una tasa válida');
      return;
    }

    setLoading(true);
    try {
      const res = await fetch('/api/simulador', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          monto: numMonto,
          plazoMeses: numPlazo,
          tasaAnual: numTasa,
        }),
      });

      if (res.ok) {
        const data: SimulacionResponse = await res.json();
        setSimulacion(data);
        toast.success('Simulación completada');
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al simular');
      }
    } catch {
      toast.error('Error al simular');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setMonto('');
    setPlazoMeses('12');
    setTasaAnual('14.5');
    setSimulacion(null);
    setTipoCreditoSeleccionado('educacion');
  };

  const handleExportar = () => {
    if (!simulacion) return;

    const csvContent = [
      'Cuota,Fecha,Cuota Mensual,Capital,Interés,Saldo Restante',
      ...simulacion.tablaAmortizacion.map(c =>
        `${c.numero},${new Date(c.fechaPago).toLocaleDateString('es-VE')},${c.cuotaMensual},${c.capital},${c.interes},${c.saldoRestante}`
      )
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `simulacion_credito_${Date.now()}.csv`;
    link.click();
    URL.revokeObjectURL(url);

    toast.success('CSV exportado');
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('es-VE', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <div className="p-2 bg-green-100 rounded-lg">
          <Calculator className="h-6 w-6 text-green-600" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Simulador de Créditos</h1>
          <p className="text-sm text-gray-500">Calcule su cuota mensual y plan de pagos</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <DollarSign className="h-5 w-5" />
                Tipo de Crédito
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {TIPOS_CREDITO.map((tipo) => (
                <button
                  key={tipo.id}
                  onClick={() => handleSelectTipo(tipo.id)}
                  className={`w-full p-3 text-left rounded-lg border transition-colors ${
                    tipoCreditoSeleccionado === tipo.id
                      ? 'border-green-500 bg-green-50'
                      : 'border-gray-200 hover:bg-gray-50'
                  }`}
                >
                  <p className="font-medium">{tipo.nombre}</p>
                  <p className="text-sm text-gray-500">
                    Tasa: {tipo.tasaBase}% | Colateral: {tipo.colateralPorcentaje}%
                  </p>
                </button>
              ))}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calculator className="h-5 w-5" />
                Parameters
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <label className="text-sm font-medium text-gray-700 mb-1 block">
                  Monto (Bs)
                </label>
                <Input
                  type="text"
                  value={monto}
                  onChange={(e) => setMonto(e.target.value.replace(/[^0-9]/g, ''))}
                  placeholder={tipoCredito ? `${formatNumber(tipoCredito.montoMin)} - ${formatNumber(tipoCredito.montoMax)}` : 'Ej: 50000'}
                />
                {tipoCredito && (
                  <p className="text-xs text-gray-500 mt-1">
                    Rango: {formatNumber(tipoCredito.montoMin)} - {formatNumber(tipoCredito.montoMax)} Bs
                  </p>
                )}
              </div>

              <div>
                <label className="text-sm font-medium text-gray-700 mb-1 block">
                  Plazo (meses)
                </label>
                <Input
                  type="number"
                  value={plazoMeses}
                  onChange={(e) => setPlazoMeses(e.target.value)}
                  min={tipoCredito?.plazoMin}
                  max={tipoCredito?.plazoMax}
                  placeholder="Ej: 12"
                />
                {tipoCredito && (
                  <p className="text-xs text-gray-500 mt-1">
                    Rango: {tipoCredito.plazoMin} - {tipoCredito.plazoMax} meses
                  </p>
                )}
              </div>

              <div>
                <label className="text-sm font-medium text-gray-700 mb-1 block">
                  Tasa Anual (%)
                </label>
                <Input
                  type="number"
                  value={tasaAnual}
                  onChange={(e) => setTasaAnual(e.target.value)}
                  step="0.1"
                  min="0.1"
                  max="100"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <Button
                  onClick={handleSimular}
                  disabled={loading}
                  className="flex-1 bg-green-600 hover:bg-green-700"
                >
                  {loading ? (
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  ) : (
                    <Calculator className="h-4 w-4 mr-2" />
                  )}
                  Calcular
                </Button>
                <Button
                  variant="outline"
                  onClick={handleReset}
                >
                  <RefreshCw className="h-4 w-4" />
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="lg:col-span-2 space-y-6">
          {simulacion ? (
            <>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card className="bg-green-50">
                  <CardContent className="p-4 text-center">
                    <p className="text-sm text-green-600 font-medium">Cuota Mensual</p>
                    <p className="text-2xl font-bold text-green-700 mt-1">
                      {formatCurrency(simulacion.cuotaMensual)}
                    </p>
                  </CardContent>
                </Card>
                <Card className="bg-blue-50">
                  <CardContent className="p-4 text-center">
                    <p className="text-sm text-blue-600 font-medium">Total a Pagar</p>
                    <p className="text-2xl font-bold text-blue-700 mt-1">
                      {formatCurrency(simulacion.totalPagar)}
                    </p>
                  </CardContent>
                </Card>
                <Card className="bg-orange-50">
                  <CardContent className="p-4 text-center">
                    <p className="text-sm text-orange-600 font-medium">Total Interés</p>
                    <p className="text-2xl font-bold text-orange-700 mt-1">
                      {formatCurrency(simulacion.totalInteres)}
                    </p>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <Calendar className="h-5 w-5" />
                      Resumen del Crédito
                    </CardTitle>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={handleExportar}
                      >
                        <Download className="h-4 w-4 mr-1" />
                        Exportar CSV
                      </Button>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <p className="text-gray-500">Monto</p>
                      <p className="font-medium">{formatCurrency(simulacion.monto)}</p>
                    </div>
                    <div>
                      <p className="text-gray-500">Plazo</p>
                      <p className="font-medium">{simulacion.plazoMeses} meses</p>
                    </div>
                    <div>
                      <p className="text-gray-500">Tasa Anual</p>
                      <p className="font-medium">{simulacion.tasaAnual}%</p>
                    </div>
                    <div>
                      <p className="text-gray-500">Colateral Requerido</p>
                      <p className="font-medium">
                        {tipoCredito ? `${tipoCredito.colateralPorcentaje}% = ${formatCurrency(simulacion.monto * tipoCredito.colateralPorcentaje / 100)}` : '-'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Clock className="h-5 w-5" />
                    Tabla de Amortización
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-gray-200">
                          <th className="text-left py-2 px-3 font-medium text-gray-500">#</th>
                          <th className="text-left py-2 px-3 font-medium text-gray-500">Fecha</th>
                          <th className="text-right py-2 px-3 font-medium text-gray-500">Cuota</th>
                          <th className="text-right py-2 px-3 font-medium text-gray-500">Capital</th>
                          <th className="text-right py-2 px-3 font-medium text-gray-500">Interés</th>
                          <th className="text-right py-2 px-3 font-medium text-gray-500">Saldo</th>
                        </tr>
                      </thead>
                      <tbody>
                        {simulacion.tablaAmortizacion.map((cuota) => (
                          <tr key={cuota.numero} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="py-2 px-3">{cuota.numero}</td>
                            <td className="py-2 px-3">{formatDate(cuota.fechaPago)}</td>
                            <td className="py-2 px-3 text-right font-medium">{formatCurrency(cuota.cuotaMensual)}</td>
                            <td className="py-2 px-3 text-right">{formatCurrency(cuota.capital)}</td>
                            <td className="py-2 px-3 text-right text-red-600">{formatCurrency(cuota.interes)}</td>
                            <td className="py-2 px-3 text-right">{formatCurrency(cuota.saldoRestante)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </CardContent>
              </Card>
            </>
          ) : (
            <Card>
              <CardContent className="py-12">
                <div className="text-center">
                  <Calculator className="h-12 w-12 mx-auto text-gray-300 mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Sin simulación</h3>
                  <p className="text-gray-500">Ingrese los parámetros y haga click en Calcular</p>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}