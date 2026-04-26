import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
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
  ],
  total: 1,
  page: 0,
  size: 20,
};

describe('DashboardDocumentosPagina', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  it('debe renderizar y llamar API', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalled();
    });
  });

  it('debe mostrar boton Actualizar', async () => {
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

  it('debe mostrar mensaje sin documentos cuando no hay', async () => {
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

  it('debe mostrar spinner mientras carga', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(
      () => new Promise(() => {}) as any
    );

    render(<DashboardDocumentosPagina />);

    const spinners = document.querySelectorAll('[class*="animate-spin"]');
    expect(spinners.length).toBeGreaterThan(0);
  });

  it('debe tener selector de filtro', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockDocumentosResponse),
    } as Response);

    render(<DashboardDocumentosPagina />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeDefined();
    });
  });

  it('debe mostrar titulo de pagina', async () => {
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
});