import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DashboardBeneficiariosPagina from '@/app/(dashboard)/dashboard/beneficiarios/page';
import { toast } from 'sonner';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('@/stores/auth-store', () => ({
  useAuthStore: () => ({
    user: {
      id: 'user-1',
      nombreUsuario: 'testuser',
      correoElectronico: 'test@example.com',
      nombreCompleto: 'Test User',
      rol: 'SOCIO' as const,
      socioId: '550e8400-e29b-41d4-a716-446655440000',
    },
  }),
}));

const mockBeneficiariosResponse = {
  beneficiarios: [
    {
      id: '660e8400-e29b-41d4-a716-446655440001',
      socioId: '550e8400-e29b-41d4-a716-446655440000',
      nombreCompleto: 'María Elena Pérez',
      numeroDocumento: 'V-87654321',
      tipoDocumento: 'CEDULA_IDENTIDAD',
      parentesco: 'CONYUGE',
      porcentaje: 50.00,
      telefono: '+58-414-5551234',
      activo: true,
      fechaRegistro: '2024-03-01T10:00:00Z',
      fechaActualizacion: '2024-03-01T10:00:00Z',
    },
    {
      id: '660e8400-e29b-41d4-a716-446655440002',
      socioId: '550e8400-e29b-41d4-a716-446655440000',
      nombreCompleto: 'Juan Carlos Pérez',
      numeroDocumento: 'V-11223344',
      tipoDocumento: 'CEDULA_IDENTIDAD',
      parentesco: 'HIJO',
      porcentaje: 50.00,
      telefono: '+58-412-1234567',
      activo: true,
      fechaRegistro: '2024-03-01T10:05:00Z',
      fechaActualizacion: '2024-03-01T10:05:00Z',
    },
  ],
  total: 2,
  sumaPorcentajes: 100.00,
};

describe('DashboardBeneficiariosPagina', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  it('debe llamar API con socioId del usuario', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockBeneficiariosResponse),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('socioId=550e8400-e29b-41d4-a716-446655440000'),
        expect.any(Object)
      );
    });
  });

  it('debe mostrar boton Agregar Beneficiario', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockBeneficiariosResponse),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Agregar Beneficiario/i })).toBeDefined();
    });
  });

  it('debe mostrar suma 100% cuando es valido', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockBeneficiariosResponse),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(screen.getByText('100.00%')).toBeDefined();
    });
  });

  it('debe abrir modal al hacer click en Agregar', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockBeneficiariosResponse),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Agregar Beneficiario/i })).toBeDefined();
    });

    await act(async () => {
      await userEvent.click(screen.getByRole('button', { name: /Agregar Beneficiario/i }));
    });

    expect(screen.getByText('Nuevo Beneficiario')).toBeDefined();
  });

  it('debe cerrar modal al Cancelar', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockBeneficiariosResponse),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Agregar Beneficiario/i })).toBeDefined();
    });

    await act(async () => {
      await userEvent.click(screen.getByRole('button', { name: /Agregar Beneficiario/i }));
    });

    expect(screen.getByText('Nuevo Beneficiario')).toBeDefined();

    await act(async () => {
      await userEvent.click(screen.getByRole('button', { name: /Cancelar/i }));
    });

    expect(screen.queryByText('Nuevo Beneficiario')).toBeNull();
  });

  it('debe mostrar mensaje sin beneficiarios cuando lista vacia', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ beneficiarios: [], total: 0, sumaPorcentajes: 0 }),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(screen.getByText('Sin beneficiarios')).toBeDefined();
    });
  });

  it('debe mostrar alerta cuando porcentaje incompleto', async () => {
    const responseIncompleto = {
      beneficiarios: [{
        ...mockBeneficiariosResponse.beneficiarios[0],
        porcentaje: 30.00,
      }],
      total: 1,
      sumaPorcentajes: 30.00,
    };

    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(responseIncompleto),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(screen.getByText(/Distribución incompleta/i)).toBeDefined();
    });
  });

  it('debe mostrar toast error cuando falla carga', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: () => Promise.resolve({}),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Error al cargar beneficiarios');
    });
  });

  it('debe mostrar spinner mientras carga', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(
      () => new Promise(() => {}) as any
    );

    render(<DashboardBeneficiariosPagina />);

    const spinners = document.querySelectorAll('[class*="animate-spin"]');
    expect(spinners.length).toBeGreaterThan(0);
  });

  it('debe deshabilitar boton con 5 beneficiarios', async () => {
    const cincoBeneficiarios = {
      beneficiarios: Array.from({ length: 5 }, (_, i) => ({
        id: `ben-${i}`,
        socioId: '550e8400-e29b-41d4-a716-446655440000',
        nombreCompleto: `Beneficiario ${i + 1}`,
        numeroDocumento: `V-${i}12345678`,
        tipoDocumento: 'CEDULA_IDENTIDAD',
        parentesco: 'CONYUGE',
        porcentaje: 20.00,
        telefono: null,
        activo: true,
        fechaRegistro: '2024-03-01T10:00:00Z',
        fechaActualizacion: '2024-03-01T10:00:00Z',
      })),
      total: 5,
      sumaPorcentajes: 100.00,
    };

    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(cincoBeneficiarios),
    } as Response);

    render(<DashboardBeneficiariosPagina />);

    await waitFor(() => {
      const btn = screen.getByRole('button', { name: /Agregar Beneficiario/i });
      expect(btn).toBeDisabled();
    });
  });
});