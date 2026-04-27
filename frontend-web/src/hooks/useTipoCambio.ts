'use client';

import { useState, useEffect, useCallback } from 'react';

interface TipoCambio {
  id: string;
  fecha: string;
  tasaCompra: number;
  tasaVenta: number;
  fuente: string | null;
  variacionPorcentual: number | null;
}

interface UseTipoCambioReturn {
  tasaActual: TipoCambio | null;
  historial: TipoCambio[];
  loading: boolean;
  error: string | null;
  refetch: () => void;
}

export function useTipoCambio(limit = 30): UseTipoCambioReturn {
  const [tasaActual, setTasaActual] = useState<TipoCambio | null>(null);
  const [historial, setHistorial] = useState<TipoCambio[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTiposCambio = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(`/api/tipos-cambio?limit=${limit}`, {
        credentials: 'include',
      });

      if (!res.ok) {
        throw new Error('Error al cargar tipos de cambio');
      }

      const data = await res.json();

      if (Array.isArray(data) && data.length > 0) {
        setTasaActual(data[0]);
        setHistorial(data);
      }
    } catch (err) {
      console.error('Error cargando tipos de cambio:', err);
      setError(err instanceof Error ? err.message : 'Error desconocido');
    } finally {
      setLoading(false);
    }
  }, [limit]);

  useEffect(() => {
    fetchTiposCambio();
  }, [fetchTiposCambio]);

  return {
    tasaActual,
    historial,
    loading,
    error,
    refetch: fetchTiposCambio,
  };
}

export function formatCurrency(amount: number, currency: 'VES' | 'USD' = 'USD'): string {
  const formatter = new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: currency === 'USD' ? 'USD' : 'VES',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  return formatter.format(amount);
}

export function convertirABolivares(montoEnDolares: number, tasaVenta: number): number {
  return montoEnDolares * tasaVenta;
}

export function convertirADolares(montoEnBolivares: number, tasaCompra: number): number {
  if (tasaCompra === 0) return 0;
  return montoEnBolivares / tasaCompra;
}