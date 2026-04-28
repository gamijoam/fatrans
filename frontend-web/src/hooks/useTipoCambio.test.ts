import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useTipoCambio, formatCurrency, convertirABolivares, convertirADolares } from '@/hooks/useTipoCambio';

const mockTipoCambioData = [
  {
    id: '1',
    fecha: '2026-04-26',
    tasaCompra: 44.5,
    tasaVenta: 45.0,
    fuente: 'BCV',
    variacionPorcentual: 0.5,
  },
  {
    id: '2',
    fecha: '2026-04-25',
    tasaCompra: 44.0,
    tasaVenta: 44.5,
    fuente: 'BCV',
    variacionPorcentual: -0.3,
  },
];

describe('useTipoCambio', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe cargar tipos de cambio al montar', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTipoCambioData,
    });

    const { result } = renderHook(() => useTipoCambio());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.tasaActual).toEqual(mockTipoCambioData[0]);
    expect(result.current.historial).toEqual(mockTipoCambioData);
    expect(result.current.error).toBeNull();
  });

  it('debe manejar error al cargar tipos de cambio', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    const { result } = renderHook(() => useTipoCambio());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBeTruthy();
    expect(result.current.tasaActual).toBeNull();
  });

  it('debe cargar con límite personalizado', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTipoCambioData,
    });

    const { result } = renderHook(() => useTipoCambio(10));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(global.fetch).toHaveBeenCalledWith(
      '/api/tipos-cambio?limit=10',
      expect.objectContaining({ credentials: 'include' })
    );
  });

  it('debe permitir refetch manual', async () => {
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

    result.current.refetch();

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2);
    });
  });
});

describe('formatCurrency', () => {
  it('debe formatear USD correctamente', () => {
    const result = formatCurrency(100.50, 'USD');
    expect(result).toMatch(/\$?\s*100,?50/);
  });

  it('debe formatear VES correctamente', () => {
    const result = formatCurrency(50000.75, 'VES');
    expect(result).toMatch(/50\.?000/);
  });
});

describe('convertirABolivares', () => {
  it('debe convertir dólares a bolivares', () => {
    const result = convertirABolivares(100, 45.0);
    expect(result).toBe(4500);
  });

  it('debe manejar decimales correctamente', () => {
    const result = convertirABolivares(50.5, 44.5);
    expect(result).toBeCloseTo(2247.25);
  });
});

describe('convertirADolares', () => {
  it('debe convertir bolivares a dólares', () => {
    const result = convertirADolares(4500, 45.0);
    expect(result).toBe(100);
  });

  it('debe manejar tasa cero', () => {
    const result = convertirADolares(1000, 0);
    expect(result).toBe(0);
  });

  it('debe manejar decimales correctamente', () => {
    const result = convertirADolares(2225, 44.5);
    expect(result).toBeCloseTo(50);
  });
});