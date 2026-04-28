'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth-store';
import { creditosApi } from '@/lib/api/client';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
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

interface Solicitud {
  id: string;
  numeroSolicitud: string;
  tipoCreditoId: string;
  tipoCreditoNombre: string;
  montoSolicitado: number;
  plazoMeses: number;
  estado: string;
  fechaSolicitud: string;
  tasaInteres: number;
  cuotaMensual: number;
  montoAprobado?: number;
}

export default function CreditosPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const [solicitudes, setSolicitudes] = useState<Solicitud[]>([]);
  const [tiposCredito, setTiposCredito] = useState<TipoCredito[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user?.socioId || isLoading) return;

    const socioId = user.socioId;

    async function cargarDatos() {
      setLoading(true);
      setError(null);
      try {
        const [solicitudesRes, tiposRes] = await Promise.all([
          creditosApi.getSolicitudesPorSocio(socioId),
          creditosApi.getTiposCredito(),
        ]);
        setSolicitudes(solicitudesRes.data.solicitudes || []);
        setTiposCredito(tiposRes.data.tiposCredito || []);
      } catch (err: unknown) {
        console.error('Error al cargar créditos:', err);
        setError('Error al cargar los datos de créditos');
      } finally {
        setLoading(false);
      }
    }

    cargarDatos();
  }, [user?.socioId, isLoading]);

  const formatMonto = (monto: number | null | undefined) => {
    if (monto == null) return '-';
    return `$${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
  };

  const formatFecha = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-VE', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
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

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <h1 className="text-2xl font-bold text-gray-900">Mis Créditos</h1>
        <div className="flex flex-wrap gap-2">
          <Link href="/dashboard/creditos/simulador">
            <Button variant="outline" className="border-blue-500 text-blue-500">
              Simulardor
            </Button>
          </Link>
          <Link href="/dashboard/creditos/solicitar">
            <Button className="bg-green-600 hover:bg-green-700">
              Solicitar Crédito
            </Button>
          </Link>
        </div>
      </div>

      {error && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="py-4">
            <p className="text-red-600 text-center">{error}</p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Tipos de Crédito Disponibles</CardTitle>
        </CardHeader>
        <CardContent>
          {tiposCredito.length === 0 ? (
            <p className="text-gray-500">No hay tipos de crédito disponibles.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {tiposCredito.map((tipo) => (
                <div key={tipo.id} className="border rounded-lg p-4 hover:shadow-md transition-shadow">
                  <h3 className="font-semibold text-lg mb-2">{tipo.nombre}</h3>
                  <p className="text-sm text-gray-500 mb-3">{tipo.descripcion}</p>
                  <div className="space-y-1 text-sm">
                    <p><span className="text-gray-500">Tasa:</span> {(tipo.tasaInteresAnual * 100).toFixed(2)}%</p>
                    <p><span className="text-gray-500">Plazo:</span> {tipo.plazoMinimoMeses} - {tipo.plazoMaximoMeses} meses</p>
                    <p><span className="text-gray-500">Monto:</span> {formatMonto(tipo.montoMinimo)} - {formatMonto(tipo.montoMaximo)}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Mis Solicitudes de Crédito</CardTitle>
        </CardHeader>
        <CardContent>
          {solicitudes.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500 mb-4">No tienes solicitudes de crédito.</p>
              <Link href="/dashboard/creditos/solicitar">
                <Button className="bg-green-600 hover:bg-green-700">
                  Solicitar mi primer crédito
                </Button>
              </Link>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b bg-gray-50">
                    <th className="text-left py-3 px-3 text-sm font-medium text-gray-500">Número</th>
                    <th className="text-left py-3 px-3 text-sm font-medium text-gray-500">Tipo</th>
                    <th className="text-right py-3 px-3 text-sm font-medium text-gray-500">Monto</th>
                    <th className="text-right py-3 px-3 text-sm font-medium text-gray-500">Plazo</th>
                    <th className="text-center py-3 px-3 text-sm font-medium text-gray-500">Cuota</th>
                    <th className="text-center py-3 px-3 text-sm font-medium text-gray-500">Estado</th>
                    <th className="text-left py-3 px-3 text-sm font-medium text-gray-500">Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {solicitudes.map((solicitud) => (
                    <tr key={solicitud.id} className="border-b hover:bg-gray-50">
                      <td className="py-3 px-3">
                        <Link
                          href={`/dashboard/creditos/${solicitud.numeroSolicitud}`}
                          className="text-blue-600 hover:underline font-medium"
                        >
                          {solicitud.numeroSolicitud}
                        </Link>
                      </td>
                      <td className="py-3 px-3 text-sm">{solicitud.tipoCreditoNombre}</td>
                      <td className="py-3 px-3 text-right font-medium">
                        {formatMonto(solicitud.montoSolicitado)}
                      </td>
                      <td className="py-3 px-3 text-right">{solicitud.plazoMeses} meses</td>
                      <td className="py-3 px-3 text-right">
                        {solicitud.cuotaMensual ? formatMonto(solicitud.cuotaMensual) : '-'}
                      </td>
                      <td className="py-3 px-3 text-center">
                        <span className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${estadoColor(solicitud.estado)}`}>
                          {estadoLabel(solicitud.estado)}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-sm text-gray-500">
                        {formatFecha(solicitud.fechaSolicitud)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}