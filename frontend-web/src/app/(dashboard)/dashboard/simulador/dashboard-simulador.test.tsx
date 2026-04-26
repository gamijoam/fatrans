import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DashboardSimuladorPagina from '@/app/(dashboard)/dashboard/simulador/page';
import { toast } from 'sonner';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockSimulacionResponse = {
  monto: 50000,
  plazoMeses: 12,
  tasaAnual: 14.5,
  cuotaMensual: 4524.32,
  totalPagar: 54291.84,
  totalInteres: 4291.84,
  tablaAmortizacion: [
    {
      numero: 1,
      fechaPago: '2026-05-25T00:00:00Z',
      cuotaMensual: 4524.32,
      capital: 3924.32,
      interes: 600,
      saldoRestante: 46075.68,
    },
    {
      numero: 2,
      fechaPago: '2026-06-25T00:00:00Z',
      cuotaMensual: 4524.32,
      capital: 3971.56,
      interes: 552.76,
      saldoRestante: 42104.12,
    },
  ],
};

describe('DashboardSimuladorPagina', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  it('debe mostrar titulo Simulador de Créditos', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /Simulador de Créditos/i })).toBeDefined();
    });
  });

  it('debe mostrar 3 tipos de credito', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByText('Crédito Educación')).toBeDefined();
      expect(screen.getByText('Micro Crédito')).toBeDefined();
      expect(screen.getByText('Crédito Vehículo')).toBeDefined();
    });
  });

  it('debe tener boton Calcular', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Calcular/i })).toBeDefined();
    });
  });

  it('debe tener campo de monto', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/Ej: 50000/i)).toBeDefined();
    });
  });

  it('debe mostrar mensaje sin simulacion inicialmente', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByText('Sin simulación')).toBeDefined();
    });
  });

  it('debe hacer POST al api/simulador al calcular', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Calcular/i })).toBeDefined();
    });

    const montoInput = screen.getByPlaceholderText(/Ej: 50000/i);
    await act(async () => {
      await userEvent.type(montoInput, '50000');
    });

    await act(async () => {
      await userEvent.click(screen.getByRole('button', { name: /Calcular/i }));
    });

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/simulador',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
        })
      );
    });
  });

  it('debe mostrar resultados de simulacion', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Calcular/i })).toBeDefined();
    });

    const montoInput = screen.getByPlaceholderText(/Ej: 50000/i);
    await act(async () => {
      await userEvent.type(montoInput, '50000');
    });

    await act(async () => {
      await userEvent.click(screen.getByRole('button', { name: /Calcular/i }));
    });

    await waitFor(() => {
      expect(screen.getByText(/Tabla de Amortización/i)).toBeDefined();
    });
  });

  it('debe mostrar toast error si monto invalido', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Calcular/i })).toBeDefined();
    });

    await act(async () => {
      await userEvent.click(screen.getByRole('button', { name: /Calcular/i }));
    });

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Ingrese un monto válido');
    });
  });

  it('debe tener boton Reset', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockSimulacionResponse),
    } as Response);

    render(<DashboardSimuladorPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reset/i })).toBeDefined();
    });
  });
});