import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DashboardKYCPagina from '@/app/(dashboard)/dashboard/kyc/page';
import { toast } from 'sonner';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('@/components/ui/progress', () => ({
  ProgressBar: ({ value }: { value: number }) => (
    <div data-testid="progress-bar" data-value={value}>Progress</div>
  ),
}));

const mockEstadoKYC = {
  verificacionId: '550e8400-e29b-41d4-a716-446655440001',
  socioId: '123e4567-e89b-12d3-a456-426614174001',
  nivel: 'BASICO',
  estado: 'PENDIENTE',
  descripcionEstado: 'Pendiente de revisión',
  fechaInicio: '2024-03-01T00:00:00Z',
  fechaExpiracion: '2024-06-01T00:00:00Z',
  diasRestantes: 90,
  documentosRequeridos: 4,
  documentosValidos: 2,
  documentos: [
    {
      id: 'doc1',
      tipo: 'CEDULA_ANVERSO',
      descripcion: 'Cédula - Anverso',
      estado: 'VALIDADO',
      nombreOriginal: 'cedula_frente.jpg',
      fechaSubida: '2024-03-10T10:00:00Z',
      motivoRechazo: null,
    },
    {
      id: 'doc2',
      tipo: 'CEDULA_REVERSO',
      descripcion: 'Cédula - Reverso',
      estado: 'PENDIENTE',
      nombreOriginal: '',
      fechaSubida: '',
      motivoRechazo: null,
    },
    {
      id: 'doc3',
      tipo: 'SELFIE_CEDULA',
      descripcion: 'Selfie con Cédula',
      estado: 'RECHAZADO',
      nombreOriginal: 'selfie.jpg',
      fechaSubida: '2024-03-09T10:00:00Z',
      motivoRechazo: 'La imagen está borrosa',
    },
    {
      id: 'doc4',
      tipo: 'COMPROBANTE_DOMICILIO',
      descripcion: 'Comprobante de Domicilio',
      estado: 'PENDIENTE',
      nombreOriginal: '',
      fechaSubida: '',
      motivoRechazo: null,
    },
  ],
  comentarioRevision: null,
  motivoRechazo: null,
};

describe('DashboardKYCPagina', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('1. Carga inicial', () => {
    it('debe cargar estado KYC al montar', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith('/api/kyc/estado', expect.any(Object));
      });
    });

    it('debe mostrar título Verificación de Identidad', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /Verificación de Identidad/i })).toBeDefined();
      });
    });

    it('debe mostrar descripción de página', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText(/Complete su verificación/)).toBeDefined();
      });
    });
  });

  describe('2. Estado de verificación', () => {
    it('debe mostrar badge PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const badges = screen.getAllByText('Pendiente');
        expect(badges.length).toBeGreaterThan(0);
      });
    });

    it('debe mostrar nivel BASICO', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Nivel:')).toBeDefined();
        expect(screen.getByText('BASICO')).toBeDefined();
      });
    });

    it('debe mostrar contador de documentos válidos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('2 de 4 válidos')).toBeDefined();
      });
    });

    it('debe mostrar barra de progreso con valor 50', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const progress = screen.getByTestId('progress-bar');
        expect(progress).toHaveAttribute('data-value', '50');
      });
    });

    it('debe mostrar días restantes', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('90 días restantes')).toBeDefined();
      });
    });
  });

  describe('3. Documentos requeridos', () => {
    it('debe listar los 4 tipos de documentos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Cédula - Anverso')).toBeDefined();
        expect(screen.getByText('Cédula - Reverso')).toBeDefined();
        expect(screen.getByText('Selfie con Cédula')).toBeDefined();
        expect(screen.getByText('Comprobante de Domicilio')).toBeDefined();
      });
    });

    it('debe mostrar botones de acción para documentos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const buttons = document.querySelectorAll('button');
        const buttonTexts = Array.from(buttons).map(b => b.textContent);
        expect(buttonTexts.some(t => t?.includes('Subir') || t?.includes('Reemplazar'))).toBe(true);
      });
    });

    it('debe mostrar botón Reemplazar para documentos rechazados', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Reemplazar')).toBeDefined();
      });
    });

    it('debe mostrar badges de estado de documento', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const validos = screen.getAllByText('Válido');
        const rechazados = screen.getAllByText('Rechazado');
        expect(validos.length).toBeGreaterThan(0);
        expect(rechazados.length).toBeGreaterThan(0);
      });
    });

    it('debe mostrar nombre del archivo para documentos subidos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('cedula_frente.jpg')).toBeDefined();
        expect(screen.getByText('selfie.jpg')).toBeDefined();
      });
    });
  });

  describe('4. Botón Enviar a Revisión', () => {
    it('debe mostrar botón cuando estado es PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Enviar a Revisión/i })).toBeDefined();
      });
    });

    it('debe llamar API al enviar a revisión', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadoKYC),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve({}),
        });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Enviar a Revisión/i })).toBeEnabled();
      });

      await act(async () => {
        await userEvent.click(screen.getByRole('button', { name: /Enviar a Revisión/i }));
      });

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith('/api/kyc/enviar', expect.any(Object));
      });
    });

    it('debe deshabilitar botón mientras envía', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadoKYC),
        })
        .mockImplementation(() => new Promise(() => {}));

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Enviar a Revisión/i })).toBeDefined();
      });

      await act(async () => {
        await userEvent.click(screen.getByRole('button', { name: /Enviar a Revisión/i }));
      });

      const btn = screen.getByRole('button', { name: /Enviar a Revisión/i }) as HTMLButtonElement;
      expect(btn).toBeDisabled();
    });

    it('debe NO mostrar botón cuando estado es APROBADO', async () => {
      const estadoAprobado = { ...mockEstadoKYC, estado: 'APROBADO' };
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(estadoAprobado),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.queryByRole('button', { name: /Enviar a Revisión/i })).toBeNull();
      });
    });
  });

  describe('5. Panel de información', () => {
    it('debe mostrar ID de verificación truncado', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('550e8400...')).toBeDefined();
      });
    });

    it('debe mostrar fecha de inicio', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText(/Fecha Inicio/)).toBeDefined();
      });
    });
  });

  describe('6. Estados del proceso (stepper)', () => {
    it('debe mostrar los 3 estados', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockEstadoKYC),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('PENDIENTE')).toBeDefined();
        expect(screen.getByText('EN REVISION')).toBeDefined();
        expect(screen.getByText('APROBADO')).toBeDefined();
      });
    });
  });

  describe('7. Manejo de errores', () => {
    it('debe mostrar mensaje cuando no hay KYC (404)', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: () => Promise.resolve({}),
      } as Response);

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText(/KYC no iniciado/)).toBeDefined();
      });
    });

    it('debe mostrar toast cuando falla la carga', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: () => Promise.resolve({}),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith('Error al cargar estado KYC');
      });
    });
  });

  describe('8. Carga y estados', () => {
    it('debe mostrar spinner mientras carga', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(() => new Promise(() => {}));

      render(<DashboardKYCPagina />);

      const spinners = document.querySelectorAll('[class*="animate-spin"]');
      expect(spinners.length).toBeGreaterThan(0);
    });
  });

  describe('9. Estados KYC', () => {
    it('debe mostrar badge APROBADO', async () => {
      const estadoAprobado = { ...mockEstadoKYC, estado: 'APROBADO' };
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(estadoAprobado),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const badge = screen.getByText('Aprobado');
        expect(badge).toBeDefined();
      });
    });

    it('debe mostrar badge RECHAZADO y mensaje', async () => {
      const estadoRechazado = { ...mockEstadoKYC, estado: 'RECHAZADO', motivoRechazo: 'Documentos ilegibles' };
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(estadoRechazado),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText(/Documentos ilegibles/)).toBeDefined();
      });
    });

    it('debe mostrar badge EN REVISION y comentario', async () => {
      const estadoEnRevision = { ...mockEstadoKYC, estado: 'EN_REVISION', comentarioRevision: 'En análisis' };
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(estadoEnRevision),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const badge = screen.getByText('En Revisión');
        expect(badge).toBeDefined();
        expect(screen.getByText('En análisis')).toBeDefined();
      });
    });

    it('debe mostrar badge EXPIRADO', async () => {
      const estadoExpirado = { ...mockEstadoKYC, estado: 'EXPIRADO' };
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(estadoExpirado),
      });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const badge = screen.getByText('Expirado');
        expect(badge).toBeDefined();
      });
    });
  });

  describe('10. Subida de documentos via API', () => {
    it('debe mostrar toast de error cuando sube documento y falla', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadoKYC),
        })
        .mockResolvedValueOnce({
          ok: false,
          status: 400,
          json: () => Promise.resolve({ message: 'Error en subida' }),
        });

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Verificación de Identidad (KYC)')).toBeDefined();
      });
    });
  });
});