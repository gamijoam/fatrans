'use client';

import { useTipoCambio, formatCurrency } from '@/hooks/useTipoCambio';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, TrendingUp, TrendingDown, DollarSign } from 'lucide-react';

interface TipoCambioCardProps {
  className?: string;
}

export function TipoCambioCard({ className }: TipoCambioCardProps) {
  const { tasaActual, loading, error } = useTipoCambio(1);

  if (loading) {
    return (
      <Card className={className}>
        <CardContent className="flex items-center justify-center py-6">
          <Loader2 className="h-6 w-6 animate-spin text-green-600" />
        </CardContent>
      </Card>
    );
  }

  if (error || !tasaActual) {
    return (
      <Card className={className}>
        <CardContent className="py-4">
          <p className="text-sm text-gray-500">Tasa no disponible</p>
        </CardContent>
      </Card>
    );
  }

  const variacion = tasaActual.variacionPorcentual;
  const isPositive = variacion !== null && variacion >= 0;

  return (
    <Card className={className}>
      <CardHeader className="pb-2">
        <CardTitle className="flex items-center justify-between text-sm font-medium">
          <span className="flex items-center gap-2">
            <DollarSign className="h-4 w-4" />
            Tipo de Cambio
          </span>
          <Badge variant="outline" className="text-xs">
            {tasaActual.fecha}
          </Badge>
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-1">
          <div className="flex justify-between items-baseline">
            <span className="text-2xl font-bold">
              {formatCurrency(tasaActual.tasaVenta, 'VES')}
            </span>
            <span className="text-sm text-gray-500">/ USD</span>
          </div>

          <div className="flex items-center gap-2 text-xs text-gray-500">
            <span>Compra: {formatCurrency(tasaActual.tasaCompra, 'VES')}</span>
          </div>

          {variacion !== null && (
            <div className={`flex items-center gap-1 text-xs ${isPositive ? 'text-green-600' : 'text-red-600'}`}>
              {isPositive ? (
                <TrendingUp className="h-3 w-3" />
              ) : (
                <TrendingDown className="h-3 w-3" />
              )}
              <span>
                {isPositive ? '+' : ''}{variacion.toFixed(2)}% vs ayer
              </span>
            </div>
          )}

          {tasaActual.fuente && (
            <p className="text-xs text-gray-400 pt-1">Fuente: {tasaActual.fuente}</p>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

interface TipoCambioTableProps {
  className?: string;
}

export function TipoCambioTable({ className }: TipoCambioTableProps) {
  const { historial, loading, error } = useTipoCambio(30);

  if (loading) {
    return (
      <Card className={className}>
        <CardContent className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-green-600" />
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className={className}>
        <CardContent className="py-12 text-center text-gray-500">
          Error al cargar historial
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="text-lg">Historial Tipo de Cambio</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b">
                <th className="text-left py-2 px-2">Fecha</th>
                <th className="text-right py-2 px-2">Tasa Compra</th>
                <th className="text-right py-2 px-2">Tasa Venta</th>
                <th className="text-right py-2 px-2">Variación</th>
              </tr>
            </thead>
            <tbody>
              {historial.map((tc) => {
                const isPositive = tc.variacionPorcentual !== null && tc.variacionPorcentual >= 0;
                return (
                  <tr key={tc.id} className="border-b hover:bg-gray-50">
                    <td className="py-2 px-2">{tc.fecha}</td>
                    <td className="text-right py-2 px-2 font-mono">
                      {tc.tasaCompra.toFixed(2)}
                    </td>
                    <td className="text-right py-2 px-2 font-mono font-medium">
                      {tc.tasaVenta.toFixed(2)}
                    </td>
                    <td className={`text-right py-2 px-2 ${isPositive ? 'text-green-600' : 'text-red-600'}`}>
                      {tc.variacionPorcentual !== null
                        ? `${isPositive ? '+' : ''}${tc.variacionPorcentual.toFixed(2)}%`
                        : '-'}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
}