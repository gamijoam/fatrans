import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AdminKYCDetallePagina from '@/app/(admin)/admin/kyc/[id]/page';
import { toast } from 'sonner';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('next/navigation', () => ({
  useParams: () => ({ id: '550e8400-e29b-41d4-a716-446655440001' }),
}));

const mockDetalleEN_REVISION = {
  verificacionId: '550e8400-e29b-41d4-a716-446655440001',
  socioId: '123e4567-e89b-12d3-a456-426614174001',
  nivel: 'BASICO',
  estado: 'EN_REVISION',
  fechaInicio: '2024-03-15T08:00:00Z',
  fechaEnvio: '2024-03-15T10:30:00Z',
  documentos: [
    {
      id: 'doc-1',
      tipo: 'CEDULA_ANVERSO',
      descripcion: 'Cédula de Identidad - Anverso',
      estado: 'PENDIENTE',
      urlVisualizacion: 'https://example.com/doc1.jpg',
      nombreOriginal: 'cedula_anverso.jpg',
      tamanoBytes: 1024000,
      fechaSubida: '2024-03-15T09:00:00Z',
    },
    {
      id: 'doc-2',
      tipo: 'CEDULA_REVERSO',
      descripcion: 'Cédula de Identidad - Reverso',
      estado: 'VALIDADO',
      urlVisualizacion: 'https://example.com/doc2.jpg',
      nombreOriginal: 'cedula_reverso.jpg',
      tamanoBytes: 980000,
      fechaSubida: '2024-03-15T09:05:00Z',
    },
    {
      id: 'doc-3',
      tipo: 'SELFIE_CEDULA',
      descripcion: 'Selfie con Cédula',
      estado: 'PENDIENTE',
      urlVisualizacion: 'https://example.com/selfie.jpg',
      nombreOriginal: 'selfie.jpg',
      tamanoBytes: 500000,
      fechaSubida: '2024-03-15T09:10:00Z',
    },
  ],
  consentimiento: {
    aceptado: true,
    fechaConsentimiento: '2024-03-15T08:00:00Z',
  },
};

const mockDetalleAPROBADO = {
  ...mockDetalleEN_REVISION,
  estado: 'APROBADO',
};

describe('AdminKYCDetallePagina - Flujo Completo', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('1. Carga inicial de detalle', () => {
    it('debe llamar API con el ID de la verificación', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          '/api/admin/kyc/revision/550e8400-e29b-41d4-a716-446655440001',
          expect.objectContaining({ credentials: 'include' })
        );
      });
    });

    it('debe mostrar título y descripción', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.getByText('Detalle KYC')).toBeDefined();
        expect(screen.getByText('Revisión de verificación de identidad')).toBeDefined();
      });
    });

    it('debe mostrar badge del estado correcto', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.getByText('En Revisión')).toBeDefined();
      });
    });
  });

  describe('2. Panel de Documentos', () => {
    it('debe mostrar documentos cargados', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.getByText('Documentos Cargados')).toBeDefined();
        expect(screen.getByText('Cédula de Identidad - Anverso')).toBeDefined();
        expect(screen.getByText('Cédula de Identidad - Reverso')).toBeDefined();
        expect(screen.getByText('Selfie con Cédula')).toBeDefined();
      });
    });

    it('debe mostrar botones Ver para cada documento', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        const verLinks = screen.getAllByRole('link', { name: 'Ver' });
        expect(verLinks.length).toBe(3);
      });
    });

    it('debe mostrar mensaje cuando no hay documentos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ ...mockDetalleEN_REVISION, documentos: [] }),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.getByText('No hay documentos cargados')).toBeDefined();
      });
    });
  });

  describe('3. Panel de Información', () => {
    it('debe mostrar datos del socio', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.getByText('Socio ID')).toBeDefined();
        expect(screen.getByText('Nivel KYC')).toBeDefined();
        expect(screen.getByText('Fecha Envío')).toBeDefined();
      });
    });
  });

  describe('4. Flujo de Aprobación', () => {
    it('debe mostrar botones Aprobar y Rechazar cuando estado es EN_REVISION', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.getByText('Aprobar')).toBeDefined();
        expect(screen.getByText('Rechazar')).toBeDefined();
      });
    });

    it('debe abrir diálogo al hacer click en Aprobar', async () => {
      const user = userEvent.setup();
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        user.click(screen.getByText('Aprobar'));
      });

      await waitFor(() => {
        expect(screen.getByText('¿Aprobar verificación?')).toBeDefined();
      });
    });
  });

  describe('5. Flujo de Rechazo', () => {
    it('debe abrir diálogo al hacer click en Rechazar', async () => {
      const user = userEvent.setup();
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        user.click(screen.getByText('Rechazar'));
      });

      await waitFor(() => {
        expect(screen.getByText('Rechazar verificación')).toBeDefined();
      });
    });
  });

  describe('6. Estados de Verificación', () => {
    it('debe ocultar acciones cuando estado es APROBADO', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleAPROBADO),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.queryByText('Acciones')).toBeNull();
      });
    });

    it('debe mostrar badge Aprobado cuando estado es APROBADO', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleAPROBADO),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(screen.getByText('Aprobado')).toBeDefined();
      });
    });
  });

  describe('7. Navegación', () => {
    it('debe tener enlace para volver a /admin/kyc', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockDetalleEN_REVISION),
      });

      const { container } = render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        const backLink = container.querySelector('a[href="/admin/kyc"]');
        expect(backLink).toBeDefined();
      });
    });
  });

  describe('8. Manejo de Errores', () => {
    it('debe mostrar error en toast cuando falla la carga', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: () => Promise.resolve({ message: 'Error interno del servidor' }),
      });

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith('Error al cargar detalles');
      });
    });

    it('debe manejar error de red', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('Network error'));

      render(<AdminKYCDetallePagina />);

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith('Error al cargar detalles');
      });
    });
  });

  describe('9. Estados de Carga', () => {
    it('debe mostrar spinner mientras carga', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(() => new Promise(() => {}));

      render(<AdminKYCDetallePagina />);

      const spinners = document.querySelectorAll('[class*="animate-spin"]');
      expect(spinners.length).toBeGreaterThan(0);
    });
  });
});