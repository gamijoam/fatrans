import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DashboardKYCPagina from '@/app/(dashboard)/dashboard/kyc/page';
import { toast } from 'sonner';

/**
 * Tests del rediseño KYC (19-may-2026) — wizard de 3 pasos visible.
 *
 * El layout cambió por completo: ya no hay 4 documentos en una lista, no
 * hay stepper textual "PENDIENTE/EN REVISION/APROBADO", no hay "ID
 * truncado". La UI ahora se rige por el `pasoActivo` derivado del estado
 * (biometría + comprobante). Solo testeamos comportamiento visible al
 * socio — los detalles internos quedan cubiertos por el cálculo de
 * `pasoActivo` que es fácil de verificar por outputs.
 */

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

// Mockear BiometricCapture para no levantar todo el componente Didit en tests.
vi.mock('@/components/features/kyc/biometric-capture', () => ({
  BiometricCapture: ({ estadoBackend }: { estadoBackend?: string | null }) => (
    <div data-testid="biometric-capture" data-estado={estadoBackend ?? 'null'}>
      BiometricCapture mock
    </div>
  ),
}));

const baseEstadoKYC = {
  verificacionId: '550e8400-e29b-41d4-a716-446655440001',
  socioId: '123e4567-e89b-12d3-a456-426614174001',
  nivel: 'BASICO',
  estado: 'PENDIENTE',
  descripcionEstado: 'Pendiente',
  fechaInicio: '2026-05-01T00:00:00Z',
  fechaExpiracion: null as string | null,
  diasRestantes: 0,
  documentosRequeridos: 1,
  documentosValidos: 0,
  documentos: [] as Array<{
    id: string;
    tipo: string;
    descripcion: string;
    estado: string;
    nombreOriginal: string;
    fechaSubida: string;
    motivoRechazo: string | null;
  }>,
  comentarioRevision: null as string | null,
  motivoRechazo: null as string | null,
  estadoBiometria: 'NO_INICIADA' as
    | 'NO_INICIADA'
    | 'EN_PROGRESO'
    | 'APROBADA'
    | 'RECHAZADA'
    | 'EXPIRADA'
    | null,
};

function mockEstadoResponse(estado = baseEstadoKYC) {
  return {
    ok: true,
    status: 200,
    json: () => Promise.resolve(estado),
  } as unknown as Response;
}

describe('DashboardKYCPagina (rediseño wizard)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('Carga inicial', () => {
    it('llama a /api/kyc/estado al montar', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse()
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          expect.stringMatching(/^\/api\/kyc\/estado\?t=/),
          expect.objectContaining({
            credentials: 'include',
            cache: 'no-store',
          })
        );
      });
    });

    it('muestra el título principal', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse()
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: /Verificación de identidad/i })
        ).toBeDefined();
      });
    });

    it('muestra mensaje cuando no hay verificación activa (404)', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: () => Promise.resolve({}),
      } as Response);

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(
          screen.getByText(/Aún no tenés una verificación activa/i)
        ).toBeDefined();
      });
    });

    it('muestra toast cuando falla la carga (500)', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: () => Promise.resolve({}),
      } as Response);

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalled();
      });
    });
  });

  describe('Paso activo según estado', () => {
    it('Paso 1 (biometría) activo cuando estadoBiometria=NO_INICIADA', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({ ...baseEstadoKYC, estadoBiometria: 'NO_INICIADA' })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByTestId('biometric-capture')).toBeDefined();
      });
    });

    it('Paso 2 (comprobante) activo cuando biometría APROBADA y sin comprobante', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({ ...baseEstadoKYC, estadoBiometria: 'APROBADA' })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: /Comprobante de domicilio/i })
        ).toBeDefined();
        expect(
          screen.getByRole('button', { name: /Subir comprobante/i })
        ).toBeDefined();
      });

      // El componente de biometría NO se renderiza en este paso (paso 1 colapsado)
      expect(screen.queryByTestId('biometric-capture')).toBeNull();
    });

    it('Paso 3 (revisión) cuando estado=EN_REVISION', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({
          ...baseEstadoKYC,
          estado: 'EN_REVISION',
          estadoBiometria: 'APROBADA',
        })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        // El stepper también muestra "Revisión final" — uso un selector
        // específico para el alert global de estado.
        expect(
          screen.getByText(/Tu verificación está en revisión/i)
        ).toBeDefined();
      });
    });

    it('muestra mensaje de éxito cuando KYC APROBADO', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({
          ...baseEstadoKYC,
          estado: 'APROBADO',
          estadoBiometria: 'APROBADA',
          fechaExpiracion: '2027-05-01T00:00:00Z',
          diasRestantes: 365,
        })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(
          screen.getByText(/Tu identidad fue verificada/i)
        ).toBeDefined();
      });
    });
  });

  describe('Stepper visual', () => {
    it('muestra los 3 títulos de paso', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse()
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Verificación facial')).toBeDefined();
        // El título "Comprobante de domicilio" aparece en stepper y/o sección,
        // así que basta verificar que aparezca al menos una vez.
        expect(
          screen.getAllByText('Comprobante de domicilio').length
        ).toBeGreaterThan(0);
        expect(screen.getByText('Revisión final')).toBeDefined();
      });
    });

    it('progress bar refleja pasos completados', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({ ...baseEstadoKYC, estadoBiometria: 'APROBADA' })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        const progress = screen.getByTestId('progress-bar');
        // 1 de 3 pasos completados → ~33%
        expect(progress.getAttribute('data-value')).toBe(
          String((1 / 3) * 100)
        );
      });
    });
  });

  describe('Comprobante de domicilio', () => {
    it('botón "Enviar a revisión" aparece cuando comprobante PENDIENTE y estado PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({
          ...baseEstadoKYC,
          estadoBiometria: 'APROBADA',
          documentos: [
            {
              id: 'doc4',
              tipo: 'COMPROBANTE_DOMICILIO',
              descripcion: 'Comprobante de Domicilio',
              estado: 'PENDIENTE',
              nombreOriginal: 'factura.pdf',
              fechaSubida: '2026-05-01T00:00:00Z',
              motivoRechazo: null,
            },
          ],
        })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(
          screen.getByRole('button', { name: /Enviar a revisión/i })
        ).toBeDefined();
      });
    });

    it('muestra el nombre del archivo subido cuando comprobante PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({
          ...baseEstadoKYC,
          estadoBiometria: 'APROBADA',
          documentos: [
            {
              id: 'doc4',
              tipo: 'COMPROBANTE_DOMICILIO',
              descripcion: 'Comprobante de Domicilio',
              estado: 'PENDIENTE',
              nombreOriginal: 'factura_servicio.pdf',
              fechaSubida: '2026-05-01T00:00:00Z',
              motivoRechazo: null,
            },
          ],
        })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('factura_servicio.pdf')).toBeDefined();
      });
    });

    it('muestra motivo de rechazo cuando comprobante RECHAZADO', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({
          ...baseEstadoKYC,
          estadoBiometria: 'APROBADA',
          documentos: [
            {
              id: 'doc4',
              tipo: 'COMPROBANTE_DOMICILIO',
              descripcion: 'Comprobante de Domicilio',
              estado: 'RECHAZADO',
              nombreOriginal: 'factura.jpg',
              fechaSubida: '2026-05-01T00:00:00Z',
              motivoRechazo: 'La factura está borrosa',
            },
          ],
        })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText(/La factura está borrosa/i)).toBeDefined();
      });
    });

    it('llama a /api/kyc/enviar al hacer click en "Enviar a revisión"', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce(
          mockEstadoResponse({
            ...baseEstadoKYC,
            estadoBiometria: 'APROBADA',
            documentos: [
              {
                id: 'doc4',
                tipo: 'COMPROBANTE_DOMICILIO',
                descripcion: 'Comprobante de Domicilio',
                estado: 'PENDIENTE',
                nombreOriginal: 'factura.pdf',
                fechaSubida: '2026-05-01T00:00:00Z',
                motivoRechazo: null,
              },
            ],
          })
        )
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve({}),
        } as Response)
        .mockResolvedValueOnce(mockEstadoResponse());

      render(<DashboardKYCPagina />);

      const btn = await screen.findByRole('button', {
        name: /Enviar a revisión/i,
      });

      await act(async () => {
        await userEvent.click(btn);
      });

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          '/api/kyc/enviar',
          expect.any(Object)
        );
      });
    });
  });

  describe('Estados de rechazo', () => {
    it('muestra mensaje cuando KYC rechazado con motivo', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({
          ...baseEstadoKYC,
          estado: 'RECHAZADO',
          motivoRechazo: 'Documentos ilegibles',
        })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText(/Documentos ilegibles/i)).toBeDefined();
      });
    });

    it('muestra mensaje de reintento cuando biometría RECHAZADA', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce(
        mockEstadoResponse({ ...baseEstadoKYC, estadoBiometria: 'RECHAZADA' })
      );

      render(<DashboardKYCPagina />);

      await waitFor(() => {
        expect(
          screen.getByText(/La verificación anterior no pasó/i)
        ).toBeDefined();
      });
    });
  });
});
