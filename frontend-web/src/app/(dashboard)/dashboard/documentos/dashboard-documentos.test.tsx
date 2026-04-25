import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DashboardDocumentosPagina from '@/app/(dashboard)/dashboard/documentos/page';
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

const mockDocumentosResponse = {
  documentos: [
    {
      documentoId: '660e8400-e29b-41d4-a716-446655440001',
      tipo: 'ESTADO_CUENTA',
      nombreArchivo: 'EstadoCuenta_2026-04_550e8400.pdf',
      estado: 'ALMACENADO',
      tamanoBytes: 245678,
      hashArchivo: 'SHA-256:abc123...',
      clasificacion: 'CONFIDENCIAL',
      fechaGeneracion: '2026-04-19T14:30:00Z',
      fechaExpiracion: '2026-04-26T14:30:00Z',
    },
    {
      documentoId: '660e8400-e29b-41d4-a716-446655440002',
      tipo: 'CONSTANCIA_AFILIACION',
      nombreArchivo: 'ConstanciaAfiliacion_550e8400.pdf',
      estado: 'ALMACENADO',
      tamanoBytes: 156234,
      hashArchivo: 'SHA-256:def456...',
      clasificacion: 'PUBLICO',
      fechaGeneracion: '2026-04-19T14:35:00Z',
      fechaExpiracion: null,
    },
  ],
  total: 2,
  page: 0,
  size: 20,
};

describe('DashboardDocumentosPagina', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  it('debe llamar API con socioId del usuario', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('socioId=550e8400-e29b-41d4-a716-446655440000'),
        expect.any(Object)
      );
    });
  });

  it('debe mostrar titulo Documentos PDF', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /Documentos PDF/i })).toBeDefined();
    });
  });

  it('debe mostrar 4 cards de generacion', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(screen.getByText('Estado de Cuenta')).toBeDefined();
      expect(screen.getByText('Constancia de Afiliación')).toBeDefined();
      expect(screen.getByText('Carta de Beneficiarios')).toBeDefined();
      expect(screen.getByText('Tabla de Amortización')).toBeDefined();
    });
  });

  it('debe mostrar tabla de documentos generados', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(screen.getByText('EstadoCuenta_2026-04_550e8400.pdf')).toBeDefined();
      expect(screen.getByText('ConstanciaAfiliacion_550e8400.pdf')).toBeDefined();
    });
  });

  it('debe mostrar botones de descarga', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      const downloadButtons = document.querySelectorAll('button');
      expect(downloadButtons.length).toBeGreaterThan(0);
    });
  });

  it('debe mostrar mensaje sin documentos cuando lista vacia', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ documentos: [], total: 0, page: 0, size: 20 }),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(screen.getByText('Sin documentos')).toBeDefined();
    });
  });

  it('debe mostrar toast error cuando falla carga', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: () => Promise.resolve({}),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Error al cargar documentos');
    });
  });

  it('debe mostrar spinner mientras carga', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(
      () => new Promise(() => {}) as any
    );

    render(<DashboardDocumentosPagina />);

    const spinners = document.querySelectorAll('[class*="animate-spin"]');
    expect(spinners.length).toBeGreaterThan(0);
  });

  it('debe mostrar filtro por tipo de documento', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      const select = screen.getByRole('combobox');
      expect(select).toBeDefined();
    });
  });

  it('debe tener boton actualizar', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Actualizar/i })).toBeDefined();
    });
  });
});