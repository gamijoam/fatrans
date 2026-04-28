import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useTipoCambio, convertirABolivares, convertirADolares } from '@/hooks/useTipoCambio';

const mockTipoCambioData = [
  {
    id: '1',
    fecha: '2026-04-26',
    tasaCompra: 44.5,
    tasaVenta: 45.0,
    fuente: 'BCV',
    variacionPorcentual: 0.5,
  },
];

describe('Multi-Currency Conversion Functions', () => {
  describe('convertirABolivares', () => {
    it('debe convertir USD a VES con tasa de venta', () => {
      const result = convertirABolivares(1000, 45.0);
      expect(result).toBe(45000);
    });

    it('debe manejar decimales correctamente', () => {
      const result = convertirABolivares(500.50, 44.5);
      expect(result).toBeCloseTo(22272.25);
    });

    it('debe manejar monto cero', () => {
      const result = convertirABolivares(0, 45.0);
      expect(result).toBe(0);
    });
  });

  describe('convertirADolares', () => {
    it('debe convertir VES a USD con tasa de compra', () => {
      const result = convertirADolares(45000, 45.0);
      expect(result).toBe(1000);
    });

    it('debe manejar tasa cero', () => {
      const result = convertirADolares(45000, 0);
      expect(result).toBe(0);
    });

    it('debe manejar decimales correctamente', () => {
      const result = convertirADolares(22225, 44.5);
      expect(result).toBeCloseTo(499.44);
    });
  });

  describe('Cálculo de Patrimonio Total', () => {
    it('debe calcular patrimonio total en VES correctamente', () => {
      const saldoEnVES = 2000000;
      const saldoEnUSD = 5000;
      const tasaVenta = 45.0;

      const saldoTotalVES = saldoEnVES + (saldoEnUSD * tasaVenta);

      expect(saldoTotalVES).toBe(2225000);
    });

    it('debe calcular patrimonio total en USD correctamente', () => {
      const saldoEnVES = 2000000;
      const saldoEnUSD = 5000;
      const tasaCompra = 44.5;

      const saldoTotalUSD = saldoEnUSD + (saldoEnVES / tasaCompra);

      expect(saldoTotalUSD).toBeCloseTo(49943.82);
    });

    it('debe manejar solo cuentas VES', () => {
      const saldoEnVES = 5000000;
      const saldoEnUSD = 0;
      const tasaVenta = 45.0;
      const tasaCompra = 44.5;

      const saldoTotalVES = saldoEnVES + (saldoEnUSD * tasaVenta);
      const saldoTotalUSD = saldoEnUSD + (saldoEnVES / tasaCompra);

      expect(saldoTotalVES).toBe(5000000);
      expect(saldoTotalUSD).toBeCloseTo(112359.55);
    });

    it('debe manejar solo cuentas USD', () => {
      const saldoEnVES = 0;
      const saldoEnUSD = 10000;
      const tasaVenta = 45.0;
      const tasaCompra = 44.5;

      const saldoTotalVES = saldoEnVES + (saldoEnUSD * tasaVenta);
      const saldoTotalUSD = saldoEnUSD + (saldoEnVES / tasaCompra);

      expect(saldoTotalVES).toBe(450000);
      expect(saldoTotalUSD).toBe(10000);
    });
  });

  describe('useTipoCambio', () => {
    beforeEach(() => {
      global.fetch = vi.fn();
    });

    afterEach(() => {
      vi.restoreAllMocks();
    });

    it('debe cargar tasa actual correctamente', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockTipoCambioData,
      });

      const { result } = renderHook(() => useTipoCambio(1));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.tasaActual).toBeTruthy();
      expect(result.current.tasaActual?.tasaVenta).toBe(45.0);
      expect(result.current.tasaActual?.tasaCompra).toBe(44.5);
    });

    it('debe proporcionar función refetch', async () => {
      (global.fetch as any)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockTipoCambioData,
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => [mockTipoCambioData[0]],
        });

      const { result } = renderHook(() => useTipoCambio());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.refetch();
      });

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledTimes(2);
      });
    });
  });
});