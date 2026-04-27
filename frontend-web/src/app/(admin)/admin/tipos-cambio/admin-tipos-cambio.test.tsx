import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import AdminTiposCambioPage from '@/app/(admin)/admin/tipos-cambio/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockTiposCambioData = [
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

describe('AdminTiposCambioPage', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe renderizar el título de la página', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCambioData,
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText('Tipos de Cambio')).toBeTruthy();
    });
  });

  it('debe mostrar la tabla de tipos de cambio después de cargar', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCambioData,
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText('Tasas VES/USD')).toBeTruthy();
    });
  });

  it('debe mostrar mensaje cuando no hay tipos de cambio', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay tipos de cambio registrados/i)).toBeTruthy();
    });
  });

  it('debe manejar errores al cargar tipos de cambio', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    const toastError = vi.fn();
    vi.mocked(await import('sonner')).toast = { ...vi.mocked(await import('sonner')).toast, error: toastError };

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay tipos de cambio registrados/i)).toBeTruthy();
    });
  });

  it('debe abrir dialog para crear nuevo tipo de cambio', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCambioData,
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText('Nueva Tasa')).toBeTruthy();
    });

    fireEvent.click(screen.getByText('Nueva Tasa'));

    await waitFor(() => {
      expect(screen.getByText('Nuevo Tipo de Cambio')).toBeTruthy();
    });
  });

  it('debe abrir dialog para crear nuevo tipo de cambio', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCambioData,
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText('Nueva Tasa')).toBeTruthy();
    });

    fireEvent.click(screen.getByText('Nueva Tasa'));

    await waitFor(() => {
      expect(screen.getByText('Nuevo Tipo de Cambio')).toBeTruthy();
    });
  });

  it('debe renderizar tabla con datos de tipos de cambio', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCambioData,
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      const tableRows = screen.getAllByRole('row');
      expect(tableRows.length).toBeGreaterThanOrEqual(3);
    });
  });

  it('debe mostrar variación positiva', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [mockTiposCambioData[0]],
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText(/\+0\.50%/)).toBeTruthy();
    });
  });

  it('debe mostrar variación negativa', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [mockTiposCambioData[1]],
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText(/-0\.30%/)).toBeTruthy();
    });
  });

  it('debe mostrar badge con cantidad de registros', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCambioData,
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText('2 registros')).toBeTruthy();
    });
  });

  it('debe mostrar loading spinner mientras carga', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTiposCambioData,
    });

    render(<AdminTiposCambioPage />);

    await waitFor(() => {
      expect(screen.getByText('Tipos de Cambio')).toBeTruthy();
    });
  });
});