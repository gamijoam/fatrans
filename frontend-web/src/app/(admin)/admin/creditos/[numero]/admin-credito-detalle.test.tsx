import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AdminCreditoDetallePage from '@/app/(admin)/admin/creditos/[numero]/page';
import { useParams } from 'next/navigation';

vi.mock('next/navigation', () => ({
  useParams: vi.fn(),
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockSolicitudData = {
  id: 'solic-1',
  numeroSolicitud: 'SC-2024-00001',
  socioId: 'socio-uuid-1',
  tipoCreditoId: 1,
  tipoCreditoNombre: 'Crédito Personal',
  montoSolicitado: 5000000,
  plazoMeses: 12,
  tasaInteresAplicada: 0.024,
  cuotaMensualEstimada: 475000,
  estado: 'PENDIENTE',
  colateralCuentaId: 'cta-001',
  colateralMontoRetenido: 500000,
  destinoCredito: 'Compra de vehículo',
  evaluacion: null,
  createdAt: '2024-03-15T10:30:00Z',
  fechaAprobacion: null,
  fechaRechazo: null,
};

const mockSolicitudConEvaluacion = {
  ...mockSolicitudData,
  estado: 'EN_EVALUACION',
  evaluacion: {
    id: 'eval-1',
    solicitudId: 'solic-1',
    puntajeAntiguedad: 20,
    puntajeHistorialAhorro: 25,
    puntajeCapacidadPago: 35,
    scoreInterno: 80,
    elegible: true,
    nivelRiesgo: 'BAJO',
    tasaInteresFinal: 0.024,
    mensajeDecision: 'Solicitud aprobada para evaluación',
  },
};

const mockPlanData = {
  id: 'plan-1',
  solicitudId: 'solic-1',
  montoPrincipal: 5000000,
  tasaInteres: 0.024,
  plazoMeses: 12,
  frecuenciaPago: 'MENSUAL',
  fechaInicio: '2024-04-01',
  fechaFin: '2025-03-01',
  numeroCuotas: 12,
  cuotaMensual: 475000,
  totalIntereses: 700000,
  totalPagado: 5700000,
  saldoPendiente: 4750000,
  estado: 'ACTIVO',
  cuotas: [
    {
      numeroCuota: 1,
      fechaPago: '2024-05-01',
      capital: 400000,
      interes: 75000,
      seguro: 0,
      montoCuota: 475000,
      saldoRestante: 4600000,
      estado: 'PAGADA',
    },
    {
      numeroCuota: 2,
      fechaPago: '2024-06-01',
      capital: 408000,
      interes: 67000,
      seguro: 0,
      montoCuota: 475000,
      saldoRestante: 4192000,
      estado: 'PENDIENTE',
    },
  ],
};

describe('AdminCreditoDetallePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (useParams as ReturnType<typeof vi.fn>).mockReturnValue({ numero: 'SC-2024-00001' });
    global.fetch = vi.fn();
  });

  describe('Renderizado inicial', () => {
    it('debe mostrar loader mientras carga', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(() => new Promise(() => {}));
      const { container } = render(<AdminCreditoDetallePage />);
      const loader = container.querySelector('.animate-spin');
      expect(loader).toBeDefined();
    });
  });

  describe('Carga de datos', () => {
    it('debe cargar y mostrar datos de solicitud', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false, json: async () => ({}) });
        }
        return Promise.resolve({
          ok: true,
          json: async () => mockSolicitudData,
        });
      });

      const { container } = render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(container.textContent).toContain('SC-2024-00001');
      }, { timeout: 3000 });
    });

    it('debe mostrar mensaje de error cuando solicitud no existe', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: false,
        status: 404,
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText(/Solicitud no encontrada/i)).toBeDefined();
      }, { timeout: 3000 });
    });
  });

  describe('Badge de estado', () => {
    it('debe mostrar badge para estado PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudData),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText('PENDIENTE')).toBeDefined();
      });
    });
  });

  describe('Sección de evaluación', () => {
    it('debe mostrar sección de evaluación cuando existe', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({
            ok: true,
            json: () => Promise.resolve(mockPlanData),
          });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudConEvaluacion),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText('Evaluación Crediticia')).toBeDefined();
      });
    });

    it('debe mostrar puntajes de evaluación', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({
            ok: true,
            json: () => Promise.resolve(mockPlanData),
          });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudConEvaluacion),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText(/Antigüedad/)).toBeDefined();
        expect(screen.getByText('20/30')).toBeDefined();
      });
    });

    it('no debe mostrar sección de evaluación cuando es null', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudData),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.queryByText('Evaluación Crediticia')).toBeNull();
      });
    });
  });

  describe('Plan de amortización', () => {
    it('debe mostrar tabla de cuotas cuando existe plan', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({
            ok: true,
            json: () => Promise.resolve(mockPlanData),
          });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudConEvaluacion),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText('Plan de Amortización')).toBeDefined();
      });
    });
  });

  describe('Acciones - Estado PENDIENTE', () => {
    it('debe mostrar botón Evaluar cuando estado es PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudData),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText('Evaluar Solicitud')).toBeDefined();
      });
    });

    it('debe tener botón Evaluar que es clickeable', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudData),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        const evaluarBtn = screen.getByText('Evaluar Solicitud');
        expect(evaluarBtn).toBeEnabled();
      });
    });
  });

  describe('Acciones - Estado EN_EVALUACION', () => {
    it('debe mostrar botones Aprobar y Rechazar cuando estado es EN_EVALUACION', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudConEvaluacion),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText('Aprobar Crédito')).toBeDefined();
        expect(screen.getByText('Rechazar')).toBeDefined();
      });
    });
  });

  describe('Navegación', () => {
    it('debe tener enlace para volver a la lista', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudData),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        expect(screen.getByText('Volver a la lista')).toBeDefined();
      });
    });

    it('debe tener botón Volver a la lista que es un Link', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string) => {
        if (url.includes('/plan')) {
          return Promise.resolve({ ok: false });
        }
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockSolicitudData),
        });
      });

      render(<AdminCreditoDetallePage />);

      await waitFor(() => {
        const link = screen.getByRole('link', { name: /Volver a la lista/i });
        expect(link).toBeDefined();
      });
    });
  });
});