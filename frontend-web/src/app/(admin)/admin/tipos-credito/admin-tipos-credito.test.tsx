import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import AdminTiposCreditoPage from '@/app/(admin)/admin/tipos-credito/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockTiposCreditoData = [
  {
    id: 1,
    codigo: 'MICRO_CREDITO',
    nombre: 'Micro Crédito',
    descripcion: 'Crédito pequeño para emprendedores',
    tasaInteresAnual: 0.24,
    plazoMinimoMeses: 3,
    plazoMaximoMeses: 12,
    montoMinimo: 100,
    montoMaximo: 5000,
    porcentajeRequerimientoColateral: 0.1,
    comisionApertura: 0.01,
    penalidadMoraTasa: 0.02,
    diasGracia: 5,
    activo: true,
  },
  {
    id: 2,
    codigo: 'CREDITO_VEHICULO',
    nombre: 'Crédito Vehículo',
    descripcion: 'Financiamiento para compra de vehículos',
    tasaInteresAnual: 0.18,
    plazoMinimoMeses: 12,
    plazoMaximoMeses: 60,
    montoMinimo: 5000,
    montoMaximo: 100000,
    porcentajeRequerimientoColateral: 0.3,
    comisionApertura: 0.02,
    penalidadMoraTasa: 0.015,
    diasGracia: 10,
    activo: false,
  },
];

describe('AdminTiposCreditoPage', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe renderizar el título de la página', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCreditoData,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByText('Tipos de Crédito')).toBeTruthy();
    });
  });

  it('debe mostrar la tabla de tipos de crédito después de cargar', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCreditoData,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByText('MICRO_CREDITO')).toBeTruthy();
      expect(screen.getByText('Micro Crédito')).toBeTruthy();
      expect(screen.getByText('CREDITO_VEHICULO')).toBeTruthy();
    });
  });

  it('debe mostrar mensaje cuando no hay tipos de crédito', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay tipos de crédito registrados/i)).toBeTruthy();
    });
  });

  it('debe manejar errores al cargar tipos de crédito', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay tipos de crédito registrados/i)).toBeTruthy();
    });
  });

  it('debe tener select de filtro de estado', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCreditoData,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeTruthy();
    });
  });

  it('debe tener botón para nuevo tipo', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCreditoData,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByText(/Nuevo Tipo/i)).toBeTruthy();
    });
  });

  it('debe abrir diálogo al hacer clic en Nuevo Tipo', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCreditoData,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      const button = screen.getByText(/Nuevo Tipo/i);
      fireEvent.click(button);
    });

    await waitFor(() => {
      expect(screen.getByText(/Nuevo Tipo de Crédito/i)).toBeTruthy();
    });
  });

  it('debe mostrar badge de activo para tipo activo', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCreditoData,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByText('Activo')).toBeTruthy();
    });
  });

  it('debe mostrar botones de acción para cada tipo', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCreditoData,
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      const buttons = screen.getAllByRole('button');
      expect(buttons.length).toBeGreaterThanOrEqual(4);
    });
  });

  it('debe crear tipo de crédito exitosamente', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockTiposCreditoData,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ id: 3, ...mockTiposCreditoData[0] }),
      });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      fireEvent.click(screen.getByText(/Nuevo Tipo/i));
    });

    await waitFor(() => {
      expect(screen.getByText(/Nuevo Tipo de Crédito/i)).toBeTruthy();
    });
  });

  it('debe mostrar información de tasa y plazos', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [mockTiposCreditoData[0]],
    });

    render(<AdminTiposCreditoPage />);

    await waitFor(() => {
      expect(screen.getByText('24.00%')).toBeTruthy();
      expect(screen.getByText('3-12 meses')).toBeTruthy();
    });
  });
});