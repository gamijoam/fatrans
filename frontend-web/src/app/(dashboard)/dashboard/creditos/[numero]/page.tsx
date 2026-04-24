'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';
import { creditosApi } from '@/lib/api/client';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface Solicitud {
  id: string;
  numeroSolicitud: string;
  tipoCreditoId: string;
  tipoCreditoNombre: string;
  montoSolicitado: number;
  plazoMeses: number;
  estado: string;
  fechaSolicitud: string;
  fechaEvaluacion?: string;
  fechaAprobacion?: string;
  fechaDesembolso?: string;
  tasaInteres: number;
  tasaInteresAnual: number;
  cuotaMensual: number;
  montoAprobado?: number;
  observaciones?: string;
  motivoRechazo?: string;
  puntajeCrediticio?: number;
  nivelRiesgo?: string;
  planAmortizacion?: Array<{
    numeroCuota: number;
    fechaPago: string;
    capital: number;
    interes: number;
    cuota: number;
    saldo: number;
    estado: string;
  }>;
}

export default function CreditoDetailPage() {
  const params = useParams();
  const numero = params.numero as string;

  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [solicitud, setSolicitud] = useState<Solicitud | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!numero || isLoading) return;

    async function cargarSolicitud() {
      setLoading(true);
      setError(null);
      try {
        const res = await creditosApi.getSolicitud(numero);
        setSolicitud(res.data);
      } catch (err) {
        console.error('Error al cargar solicitud:', err);
        setError('Error al cargar los datos de la solicitud');
      } finally {
        setLoading(false);
      }
    }

    cargarSolicitud();
  }, [numero, isLoading]);

  const formatMonto = (monto: number) => {
    return `$${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
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

  const estadoColor = (estado: string) => {
    switch (estado) {
      case 'APROBADA':
      case 'DESEMBOLSADA':
        return 'bg-green-100 text-green-800';
      case 'RECHAZADA':
        return 'bg-red-100 text-red-800';
      case 'EN_EVALUACION':
        return 'bg-yellow-100 text-yellow-800';
      case 'PENDIENTE':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const estadoLabel = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE': return 'Pendiente';
      case 'EN_EVALUACION': return 'En Evaluación';
      case 'APROBADA': return 'Aprobada';
      case 'RECHAZADA': return 'Rechazada';
      case 'DESEMBOLSADA': return 'Desembolsada';
      case 'CANCELADA': return 'Cancelada';
      default: return estado;
    }
  };

  const nivelRiesgoColor = (nivel: string) => {
    switch (nivel) {
      case 'BAJO': return 'text-green-600';
      case 'MEDIO': return 'text-yellow-600';
      case 'ALTO': return 'text-red-600';
      default: return 'text-gray-600';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  if (error || !solicitud) {
    return (
      <div className="space-y-4">
        <Link href="/dashboard/creditos">
          <Button variant="outline">← Volver a Créditos</Button>
        </Link>
        <Card>
          <CardContent className="py-8">
            <p className="text-center text-red-600">{error || 'Solicitud no encontrada'}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/dashboard/creditos">
            <Button variant="outline">← Volver</Button>
          </Link>
          <div>
            <h1 className="text-2xl font-bold">Crédito {solicitud.numeroSolicitud}</h1>
            <p className="text-gray-500">{solicitud.tipoCreditoNombre}</p>
          </div>
        </div>
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${estadoColor(solicitud.estado)}`}>
          {estadoLabel(solicitud.estado)}
        </span>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Información del Crédito</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-500">Monto Solicitado</p>
                <p className="font-medium text-lg">{formatMonto(solicitud.montoSolicitado)}</p>
              </div>
              {solicitud.montoAprobado && (
                <div>
                  <p className="text-sm text-gray-500">Monto Aprobado</p>
                  <p className="font-medium text-lg text-green-600">{formatMonto(solicitud.montoAprobado)}</p>
                </div>
              )}
              <div>
                <p className="text-sm text-gray-500">Plazo</p>
                <p className="font-medium">{solicitud.plazoMeses} meses</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Cuota Mensual</p>
                <p className="font-medium text-lg text-blue-600">
                  {solicitud.cuotaMensual ? formatMonto(solicitud.cuotaMensual) : '-'}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Tasa de Interés</p>
                <p className="font-medium">{(solicitud.tasaInteresAnual * 100).toFixed(2)}% TEA</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Fecha de Solicitud</p>
                <p className="font-medium">{formatFecha(solicitud.fechaSolicitud)}</p>
              </div>
            </div>

            {solicitud.puntajeCrediticio !== undefined && solicitud.puntajeCrediticio !== null && (
              <div className="pt-4 border-t">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-500">Puntaje Crediticio</p>
                    <p className="font-medium text-xl">{solicitud.puntajeCrediticio.toFixed(0)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Nivel de Riesgo</p>
                    <p className={`font-medium text-xl ${nivelRiesgoColor(solicitud.nivelRiesgo || '')}`}>
                      {solicitud.nivelRiesgo || '-'}
                    </p>
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Historial de Estados</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="w-2 h-2 rounded-full bg-blue-500"></div>
                <div>
                  <p className="font-medium">Solicitud Creada</p>
                  <p className="text-sm text-gray-500">{formatFecha(solicitud.fechaSolicitud)}</p>
                </div>
              </div>

              {solicitud.fechaEvaluacion && (
                <div className="flex items-center gap-3">
                  <div className={`w-2 h-2 rounded-full ${solicitud.estado === 'RECHAZADA' ? 'bg-red-500' : 'bg-yellow-500'}`}></div>
                  <div>
                    <p className="font-medium">En Evaluación</p>
                    <p className="text-sm text-gray-500">{formatFecha(solicitud.fechaEvaluacion)}</p>
                  </div>
                </div>
              )}

              {solicitud.fechaAprobacion && (
                <div className="flex items-center gap-3">
                  <div className="w-2 h-2 rounded-full bg-green-500"></div>
                  <div>
                    <p className="font-medium">Aprobada</p>
                    <p className="text-sm text-gray-500">{formatFecha(solicitud.fechaAprobacion)}</p>
                  </div>
                </div>
              )}

              {solicitud.fechaDesembolso && (
                <div className="flex items-center gap-3">
                  <div className="w-2 h-2 rounded-full bg-green-700"></div>
                  <div>
                    <p className="font-medium">Desembolsada</p>
                    <p className="text-sm text-gray-500">{formatFecha(solicitud.fechaDesembolso)}</p>
                  </div>
                </div>
              )}
            </div>

            {solicitud.observaciones && (
              <div className="mt-4 pt-4 border-t">
                <p className="text-sm text-gray-500">Observaciones</p>
                <p className="text-sm mt-1">{solicitud.observaciones}</p>
              </div>
            )}

            {solicitud.motivoRechazo && (
              <div className="mt-4 pt-4 border-t">
                <p className="text-sm text-gray-500">Motivo de Rechazo</p>
                <p className="text-sm mt-1 text-red-600">{solicitud.motivoRechazo}</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {solicitud.planAmortizacion && solicitud.planAmortizacion.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Plan de Amortización</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b bg-gray-50">
                    <th className="text-left py-3 px-3">#</th>
                    <th className="text-left py-3 px-3">Fecha</th>
                    <th className="text-right py-3 px-3">Capital</th>
                    <th className="text-right py-3 px-3">Interés</th>
                    <th className="text-right py-3 px-3">Cuota</th>
                    <th className="text-right py-3 px-3">Saldo</th>
                    <th className="text-center py-3 px-3">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {solicitud.planAmortizacion.map((fila) => (
                    <tr key={fila.numeroCuota} className="border-b hover:bg-gray-50">
                      <td className="py-3 px-3">{fila.numeroCuota}</td>
                      <td className="py-3 px-3">{new Date(fila.fechaPago).toLocaleDateString('es-VE')}</td>
                      <td className="py-3 px-3 text-right">{formatMonto(fila.capital)}</td>
                      <td className="py-3 px-3 text-right">{formatMonto(fila.interes)}</td>
                      <td className="py-3 px-3 text-right font-medium">{formatMonto(fila.cuota)}</td>
                      <td className="py-3 px-3 text-right text-gray-500">{formatMonto(fila.saldo)}</td>
                      <td className="py-3 px-3 text-center">
                        <span className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${
                          fila.estado === 'PAGADA' ? 'bg-green-100 text-green-800' :
                          fila.estado === 'VENCIDA' ? 'bg-red-100 text-red-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {fila.estado}
                        </span>
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
  );
}