'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog';
import { Loader2, ArrowLeft, CheckCircle, XCircle, AlertCircle, DollarSign, Calendar, User, CreditCard, FileText, TrendingUp, AlertTriangle } from 'lucide-react';
import { toast } from 'sonner';

interface Evaluacion {
  id: string;
  solicitudId: string;
  puntajeAntiguedad: number;
  puntajeHistorialAhorro: number;
  puntajeCapacidadPago: number;
  scoreInterno: number;
  elegible: boolean;
  nivelRiesgo: string;
  tasaInteresFinal: number;
  mensajeDecision: string;
}

interface Cuota {
  numeroCuota: number;
  fechaPago: string;
  capital: number;
  interes: number;
  seguro: number;
  montoCuota: number;
  saldoRestante: number;
  estado: string;
}

interface PlanAmortizacion {
  id: string;
  montoPrincipal: number;
  tasaInteres: number;
  plazoMeses: number;
  frecuenciaPago: string;
  fechaInicio: string;
  fechaFin: string;
  numeroCuotas: number;
  cuotaMensual: number;
  totalIntereses: number;
  totalPagado: number;
  saldoPendiente: number;
  estado: string;
  cuotas: Cuota[];
}

interface Solicitud {
  id: string;
  numeroSolicitud: string;
  socioId: string;
  tipoCreditoId: number;
  tipoCreditoNombre: string;
  productoFinanciableId?: number | null;
  productoNombreSnapshot?: string | null;
  productoPrecioSnapshot?: number | null;
  productoMonedaSnapshot?: string | null;
  productoColateralRequeridoSnapshot?: number | null;
  montoSolicitado: number;
  plazoMeses: number;
  tasaInteresAplicada: number;
  cuotaMensualEstimada: number;
  estado: string;
  colateralCuentaId: string;
  colateralMontoRetenido: number;
  destinoCredito: string;
  evaluacion: Evaluacion | null;
  createdAt: string;
  fechaAprobacion: string | null;
  fechaRechazo: string | null;
}

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: 'bg-yellow-100 text-yellow-800',
  EN_EVALUACION: 'bg-blue-100 text-blue-800',
  APROBADA: 'bg-green-100 text-green-800',
  RECHAZADA: 'bg-red-100 text-red-800',
  DESEMBOLSADO: 'bg-purple-100 text-purple-800',
  CANCELADA: 'bg-gray-100 text-gray-800',
};

const RIESGO_COLORS: Record<string, string> = {
  BAJO: 'bg-green-100 text-green-800',
  MEDIO: 'bg-yellow-100 text-yellow-800',
  ALTO: 'bg-orange-100 text-orange-800',
  MUY_ALTO: 'bg-red-100 text-red-800',
};

import { formatDate, formatCurrency } from '@/lib/utils/format';

function formatProductCurrency(value: number, currency?: string | null): string {
  return new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: currency === 'USD' ? 'USD' : 'VES',
    minimumFractionDigits: 2,
  }).format(value);
}

export default function AdminCreditoDetallePage() {
  const params = useParams();
  const router = useRouter();
  const numero = params.numero as string;

  const [solicitud, setSolicitud] = useState<Solicitud | null>(null);
  const [plan, setPlan] = useState<PlanAmortizacion | null>(null);
  const [loading, setLoading] = useState(true);

  const [evaluarOpen, setEvaluarOpen] = useState(false);
  const [evaluarData, setEvaluarData] = useState({ puntajeAntiguedad: 0, puntajeHistorialAhorro: 0, puntajeCapacidadPago: 0, salarioEstimado: 0 });

  const [aprobarOpen, setAprobarOpen] = useState(false);
  const [aprobarData, setAprobarData] = useState({ comentarios: '' });

  const [rechazarOpen, setRechazarOpen] = useState(false);
  const [rechazarData, setRechazarData] = useState({ motivo: '' });

  const [desembolsarOpen, setDesembolsarOpen] = useState(false);
  const [desembolsarData, setDesembolsarData] = useState({ cuentaDestino: '', referencia: '' });

  const [actionLoading, setActionLoading] = useState(false);

  const cargarDatos = useCallback(async () => {
    setLoading(true);
    try {
      const [solicitudRes, planRes] = await Promise.all([
        fetch(`/api/admin/creditos/solicitudes/${numero}`, { credentials: 'include' }),
        fetch(`/api/admin/creditos/solicitudes/${numero}/plan`, { credentials: 'include' }).catch(() => ({ ok: false, json: async () => ({}) })),
      ]);

      if (!solicitudRes.ok) throw new Error('Error al cargar solicitud');
      const solicitudData = await solicitudRes.json();
      setSolicitud(solicitudData);

      if (planRes.ok) {
        const planData = await planRes.json();
        setPlan(planData);
      }
    } catch (err) {
      console.error('Error cargando datos:', err);
      toast.error('Error al cargar datos de la solicitud');
    } finally {
      setLoading(false);
    }
  }, [numero]);

  useEffect(() => {
    if (numero) cargarDatos();
  }, [numero, cargarDatos]);

  const handleEvaluar = async () => {
    setActionLoading(true);
    try {
      const res = await fetch(`/api/admin/creditos/solicitudes/${numero}/evaluar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(evaluarData),
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al evaluar');
      toast.success('Solicitud evaluada exitosamente');
      setEvaluarOpen(false);
      cargarDatos();
    } catch (err) {
      toast.error('Error al evaluar solicitud');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAprobar = async () => {
    setActionLoading(true);
    try {
      const res = await fetch(`/api/admin/creditos/solicitudes/${numero}/aprobar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(aprobarData),
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al aprobar');
      toast.success('Solicitud aprobada exitosamente');
      setAprobarOpen(false);
      cargarDatos();
    } catch (err) {
      toast.error('Error al aprobar solicitud');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRechazar = async () => {
    setActionLoading(true);
    try {
      const res = await fetch(`/api/admin/creditos/solicitudes/${numero}/rechazar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivoRechazo: rechazarData.motivo }),
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al rechazar');
      toast.success('Solicitud rechazada');
      setRechazarOpen(false);
      cargarDatos();
    } catch (err) {
      toast.error('Error al rechazar solicitud');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDesembolsar = async () => {
    setActionLoading(true);
    try {
      const res = await fetch(`/api/admin/creditos/solicitudes/${numero}/desembolsar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cuentaDestino: desembolsarData.cuentaDestino, referencia: desembolsarData.referencia }),
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al desembolsar');
      toast.success('Crédito desembolsado exitosamente');
      setDesembolsarOpen(false);
      cargarDatos();
    } catch (err) {
      toast.error('Error al desembolsar crédito');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  if (!solicitud) {
    return (
      <div className="p-6">
        <p className="text-center text-gray-500">Solicitud no encontrada</p>
        <Link href="/admin/creditos" className="text-green-600 hover:underline mt-4 inline-block">
          Volver a la lista
        </Link>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/admin/creditos" className="p-2 hover:bg-gray-100 rounded-md">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-gray-900">Solicitud {solicitud.numeroSolicitud}</h1>
        </div>
        <Badge className={ESTADO_COLORS[solicitud.estado]}>{solicitud.estado.replace('_', ' ')}</Badge>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                Detalle de Solicitud
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-6">
                <div>
                  <p className="text-sm text-gray-500">Tipo de Crédito</p>
                  <p className="font-medium">{solicitud.tipoCreditoNombre}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Monto Solicitado</p>
                  <p className="font-medium text-lg">{formatCurrency(Number(solicitud.montoSolicitado))}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Plazo</p>
                  <p className="font-medium">{solicitud.plazoMeses} meses</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Cuota Estimada</p>
                  <p className="font-medium">{formatCurrency(Number(solicitud.cuotaMensualEstimada))}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Tasa Interés</p>
                  <p className="font-medium">{(Number(solicitud.tasaInteresAplicada) * 100).toFixed(2)}%</p>
                </div>
                {solicitud.productoNombreSnapshot && (
                  <div className="col-span-2 rounded-md border border-green-100 bg-green-50 p-4">
                    <p className="text-sm text-green-700">Producto financiado</p>
                    <p className="font-semibold text-[#0F2744]">{solicitud.productoNombreSnapshot}</p>
                    <div className="mt-2 grid grid-cols-1 gap-2 text-sm sm:grid-cols-2">
                      <div>
                        <span className="text-gray-500">Precio: </span>
                        <span className="font-medium">
                          {formatProductCurrency(Number(solicitud.productoPrecioSnapshot || 0), solicitud.productoMonedaSnapshot)}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-500">Colateral requerido: </span>
                        <span className="font-medium">
                          {formatProductCurrency(Number(solicitud.productoColateralRequeridoSnapshot || 0), solicitud.productoMonedaSnapshot)}
                        </span>
                      </div>
                    </div>
                  </div>
                )}
                <div>
                  <p className="text-sm text-gray-500">Destino</p>
                  <p className="font-medium">{solicitud.destinoCredito}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Fecha Solicitud</p>
                  <p className="font-medium">{formatDate(solicitud.createdAt)}</p>
                </div>
                {solicitud.colateralMontoRetenido > 0 && (
                  <div>
                    <p className="text-sm text-gray-500">Colateral Retenido</p>
                    <p className="font-medium text-yellow-600">{formatCurrency(Number(solicitud.colateralMontoRetenido))}</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {solicitud.evaluacion && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5" />
                  Evaluación Crediticia
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-xs text-gray-500">Antigüedad</p>
                    <p className="text-xl font-bold">{solicitud.evaluacion.puntajeAntiguedad}/30</p>
                  </div>
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-xs text-gray-500">Historial Ahorro</p>
                    <p className="text-xl font-bold">{solicitud.evaluacion.puntajeHistorialAhorro}/30</p>
                  </div>
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-xs text-gray-500">Capacidad Pago</p>
                    <p className="text-xl font-bold">{solicitud.evaluacion.puntajeCapacidadPago}/40</p>
                  </div>
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-xs text-gray-500">Score Total</p>
                    <p className="text-xl font-bold">{solicitud.evaluacion.scoreInterno}</p>
                  </div>
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div className={`p-3 rounded-lg ${solicitud.evaluacion.elegible ? 'bg-green-50' : 'bg-red-50'}`}>
                    <p className="text-xs text-gray-500">Elegible</p>
                    <p className="font-medium">{solicitud.evaluacion.elegible ? 'Sí' : 'No'}</p>
                  </div>
                  <div className={`p-3 rounded-lg ${RIESGO_COLORS[solicitud.evaluacion.nivelRiesgo] || 'bg-gray-50'}`}>
                    <p className="text-xs text-gray-500">Nivel Riesgo</p>
                    <p className="font-medium">{solicitud.evaluacion.nivelRiesgo}</p>
                  </div>
                  <div className="p-3 bg-blue-50 rounded-lg">
                    <p className="text-xs text-gray-500">Tasa Final</p>
                    <p className="font-medium">{(Number(solicitud.evaluacion.tasaInteresFinal) * 100).toFixed(2)}%</p>
                  </div>
                </div>
                <p className="mt-4 text-sm text-gray-600">{solicitud.evaluacion.mensajeDecision}</p>
              </CardContent>
            </Card>
          )}

          {plan && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Calendar className="h-5 w-5" />
                  Plan de Amortización
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-3 gap-4 mb-6">
                  <div>
                    <p className="text-sm text-gray-500">Monto Principal</p>
                    <p className="font-medium">{formatCurrency(Number(plan.montoPrincipal))}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Total Intereses</p>
                    <p className="font-medium">{formatCurrency(Number(plan.totalIntereses))}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Total a Pagar</p>
                    <p className="font-medium text-green-600">{formatCurrency(Number(plan.totalPagado))}</p>
                  </div>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full text-xs">
                    <thead>
                      <tr className="border-b bg-gray-50">
                        <th className="p-2 text-left">Nro</th>
                        <th className="p-2 text-left">Fecha</th>
                        <th className="p-2 text-right">Capital</th>
                        <th className="p-2 text-right">Interés</th>
                        <th className="p-2 text-right">Cuota</th>
                        <th className="p-2 text-right">Saldo</th>
                        <th className="p-2 text-center">Estado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {plan.cuotas.map((cuota) => (
                        <tr key={cuota.numeroCuota} className="border-b">
                          <td className="p-2">{cuota.numeroCuota}</td>
                          <td className="p-2">{new Date(cuota.fechaPago).toLocaleDateString('es-VE')}</td>
                          <td className="p-2 text-right">{formatCurrency(Number(cuota.capital))}</td>
                          <td className="p-2 text-right">{formatCurrency(Number(cuota.interes))}</td>
                          <td className="p-2 text-right font-medium">{formatCurrency(Number(cuota.montoCuota))}</td>
                          <td className="p-2 text-right">{formatCurrency(Number(cuota.saldoRestante))}</td>
                          <td className="p-2 text-center">
                            <Badge className={cuota.estado === 'PAGADA' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}>
                              {cuota.estado}
                            </Badge>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Acciones</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {solicitud.estado === 'PENDIENTE' && (
                <AlertDialog open={evaluarOpen} onOpenChange={setEvaluarOpen}>
                  <AlertDialogTrigger asChild>
                    <Button className="w-full bg-blue-600 hover:bg-blue-700">
                      <AlertCircle className="h-4 w-4 mr-2" />
                      Evaluar Solicitud
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Evaluar Solicitud</AlertDialogTitle>
                      <AlertDialogDescription>
                        Ingrese los datos de evaluación crediticia.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <div className="space-y-4 py-4">
                      <div>
                        <Label>Puntaje Antigüedad (0-30)</Label>
                        <Input type="number" min="0" max="30" value={evaluarData.puntajeAntiguedad}
                          onChange={(e) => setEvaluarData({ ...evaluarData, puntajeAntiguedad: Number(e.target.value) })} />
                      </div>
                      <div>
                        <Label>Puntaje Historial Ahorro (0-30)</Label>
                        <Input type="number" min="0" max="30" value={evaluarData.puntajeHistorialAhorro}
                          onChange={(e) => setEvaluarData({ ...evaluarData, puntajeHistorialAhorro: Number(e.target.value) })} />
                      </div>
                      <div>
                        <Label>Puntaje Capacidad Pago (0-40)</Label>
                        <Input type="number" min="0" max="40" value={evaluarData.puntajeCapacidadPago}
                          onChange={(e) => setEvaluarData({ ...evaluarData, puntajeCapacidadPago: Number(e.target.value) })} />
                      </div>
                      <div>
                        <Label>Salario Estimado (VES)</Label>
                        <Input type="number" min="0" value={evaluarData.salarioEstimado}
                          onChange={(e) => setEvaluarData({ ...evaluarData, salarioEstimado: Number(e.target.value) })} />
                      </div>
                    </div>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancelar</AlertDialogCancel>
                      <AlertDialogAction onClick={handleEvaluar} disabled={actionLoading}>
                        {actionLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Evaluar'}
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              )}

              {solicitud.estado === 'EN_EVALUACION' && (
                <>
                  <AlertDialog open={aprobarOpen} onOpenChange={setAprobarOpen}>
                    <AlertDialogTrigger asChild>
                      <Button className="w-full bg-green-600 hover:bg-green-700">
                        <CheckCircle className="h-4 w-4 mr-2" />
                        Aprobar Crédito
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Aprobar Solicitud</AlertDialogTitle>
                        <AlertDialogDescription>
                          Ingrese comentarios de aprobación (opcional).
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <div className="py-4">
                        <Label>Comentarios</Label>
                        <Textarea value={aprobarData.comentarios}
                          onChange={(e) => setAprobarData({ comentarios: e.target.value })}
                          placeholder="Comentarios adicionales..." />
                      </div>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancelar</AlertDialogCancel>
                        <AlertDialogAction onClick={handleAprobar} disabled={actionLoading}>
                          {actionLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Aprobar'}
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>

                  <AlertDialog open={rechazarOpen} onOpenChange={setRechazarOpen}>
                    <AlertDialogTrigger asChild>
                      <Button variant="outline" className="w-full border-red-300 text-red-600 hover:bg-red-50">
                        <XCircle className="h-4 w-4 mr-2" />
                        Rechazar
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Rechazar Solicitud</AlertDialogTitle>
                        <AlertDialogDescription>
                          Ingrese el motivo del rechazo.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <div className="py-4">
                        <Label>Motivo de Rechazo</Label>
                        <Textarea value={rechazarData.motivo}
                          onChange={(e) => setRechazarData({ motivo: e.target.value })}
                          placeholder="Motivo del rechazo..." />
                      </div>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancelar</AlertDialogCancel>
                        <AlertDialogAction onClick={handleRechazar} disabled={actionLoading || !rechazarData.motivo.trim()}>
                          {actionLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Rechazar'}
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </>
              )}

              {solicitud.estado === 'APROBADA' && (
                <AlertDialog open={desembolsarOpen} onOpenChange={setDesembolsarOpen}>
                  <AlertDialogTrigger asChild>
                    <Button className="w-full bg-purple-600 hover:bg-purple-700">
                      <DollarSign className="h-4 w-4 mr-2" />
                      Desembolsar
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Desembolsar Crédito</AlertDialogTitle>
                      <AlertDialogDescription>
                        Ingrese los datos del desembolso.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <div className="space-y-4 py-4">
                      <div>
                        <Label>Cuenta Destino</Label>
                        <Input value={desembolsarData.cuentaDestino}
                          onChange={(e) => setDesembolsarData({ ...desembolsarData, cuentaDestino: e.target.value })}
                          placeholder="Número de cuenta" />
                      </div>
                      <div>
                        <Label>Referencia</Label>
                        <Input value={desembolsarData.referencia}
                          onChange={(e) => setDesembolsarData({ ...desembolsarData, referencia: e.target.value })}
                          placeholder="Referencia del pago" />
                      </div>
                    </div>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancelar</AlertDialogCancel>
                      <AlertDialogAction onClick={handleDesembolsar} disabled={actionLoading || !desembolsarData.cuentaDestino.trim()}>
                        {actionLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Desembolsar'}
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              )}

              {(solicitud.estado === 'RECHAZADA' || solicitud.estado === 'DESEMBOLSADO' || solicitud.estado === 'CANCELADA') && (
                <p className="text-sm text-gray-500 text-center py-4">
                  Esta solicitud no requiere acciones.
                </p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2">
                <User className="h-4 w-4" />
                Información del Socio
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-500">ID Socio</p>
              <p className="font-medium mb-2">{solicitud.socioId}</p>
              <p className="text-sm text-gray-500">Tipo Crédito</p>
              <p className="font-medium">{solicitud.tipoCreditoNombre}</p>
            </CardContent>
          </Card>

          <Link href="/admin/creditos">
            <Button variant="outline" className="w-full">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Volver a la lista
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
