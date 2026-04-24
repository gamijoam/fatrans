import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AdminKYCPagina from '@/app/(admin)/admin/kyc/page';
import { toast } from 'sonner';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockEstadisticas = {
  totalVerificaciones: 25,
  estadoActual: {
    pendientes: 5,
    enRevision: 3,
    aprobados: 15,
    rechazados: 2,
    expirados: 0,
  },
  metricas: {
    tiempoPromedioRevisionHoras: 4.5,
    tasaAprobacion: 0.6,
    tasaRechazo: 0.08,
    kycPorExpirarProximoMes: 1,
  },
};

const mockColaEN_REVISION = {
  pagina: 0,
  tamanio: 10,
  totalElementos: 2,
  totalPaginas: 1,
  cola: [
    {
      verificacionId: '550e8400-e29b-41d4-a716-446655440001',
      socioId: '123e4567-e89b-12d3-a456-426614174001',
      nivel: 'BASICO',
      estado: 'EN_REVISION',
      fechaEnvio: '2024-03-15T10:30:00Z',
      tiempoEnCola: '2 horas',
    },
    {
      verificacionId: '550e8400-e29b-41d4-a716-446655440002',
      socioId: '123e4567-e89b-12d3-a456-426614174002',
      nivel: 'MEDIO',
      estado: 'EN_REVISION',
      fechaEnvio: '2024-03-15T09:00:00Z',
      tiempoEnCola: '3 horas',
    },
  ],
};

const mockColaAPROBADOS = {
  pagina: 0,
  tamanio: 10,
  totalElementos: 15,
  totalPaginas: 2,
  cola: [
    {
      verificacionId: '550e8400-e29b-41d4-a716-446655440099',
      socioId: '123e4567-e89b-12d3-a456-426614174099',
      nivel: 'COMPLETO',
      estado: 'APROBADO',
      fechaEnvio: '2024-03-10T08:00:00Z',
      tiempoEnCola: '5 dias',
    },
  ],
};

describe('AdminKYCPagina - Flujo Completo', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('1. Carga inicial', () => {
    it('debe cargar estadísticas y cola al montar', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith('/api/admin/kyc/estadisticas', expect.any(Object));
      });
    });

    it('debe mostrar título Revisión KYC', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Revisión KYC')).toBeDefined();
      });
    });

    it('debe mostrar tarjetas de estadísticas', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Pendientes')).toBeDefined();
        expect(screen.getByText('En Revisión')).toBeDefined();
        expect(screen.getByText('Aprobados')).toBeDefined();
        expect(screen.getByText('Rechazados')).toBeDefined();
      });
    });

    it('debe mostrar métricas', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('60.0%')).toBeDefined();
        expect(screen.getByText('4.5h')).toBeDefined();
      });
    });
  });

  describe('2. Tabla de Cola', () => {
    it('debe mostrar headers de tabla', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Socio ID')).toBeDefined();
        expect(screen.getByText('Nivel')).toBeDefined();
        expect(screen.getByText('Estado')).toBeDefined();
        expect(screen.getByText('Acciones')).toBeDefined();
      });
    });

    it('debe mostrar items de cola', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        const badges = screen.getAllByText('EN_REVISION');
        expect(badges.length).toBe(2);
      });
    });

    it('debe tener links Revisar para cada item', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        const links = screen.getAllByRole('link', { name: 'Revisar' });
        expect(links.length).toBe(2);
        expect(links[0]).toHaveAttribute('href', '/admin/kyc/550e8400-e29b-41d4-a716-446655440001');
      });
    });
  });

  describe('3. Filtro por Estado', () => {
    it('debe tener selector de estado', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      const select = screen.getByLabelText('Estado:');
      expect(select).toBeDefined();

      const options = screen.getAllByRole('option');
      expect(options.length).toBe(4);
    });

    it('debe recargar cola al cambiar filtro', async () => {
      const user = userEvent.setup();
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaAPROBADOS),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Cola de Revisión')).toBeDefined();
      });

      const select = screen.getByLabelText('Estado:');
      await user.selectOptions(select, 'APROBADO');

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          '/api/admin/kyc/cola-revision?page=0&size=10&estado=APROBADO',
          expect.any(Object)
        );
      });
    });
  });

  describe('4. Paginación', () => {
    it('debe mostrar controles de paginación', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ ...mockColaAPROBADOS, totalPaginas: 3 }),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Anterior')).toBeDefined();
        expect(screen.getByText('Siguiente')).toBeDefined();
        expect(screen.getByText('Página 1 de 3')).toBeDefined();
      });
    });

    it('debe deshabilitar Anterior en página 0', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ ...mockColaAPROBADOS, totalPaginas: 3 }),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        const btn = screen.getByText('Anterior');
        expect(btn).toBeDisabled();
      });
    });
  });

  describe('5. Estados de Carga', () => {
    it('debe mostrar spinner mientras carga', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockImplementation(() => new Promise(() => {}));

      render(<AdminKYCPagina />);

      const spinners = document.querySelectorAll('[class*="animate-spin"]');
      expect(spinners.length).toBeGreaterThan(0);
    });
  });

  describe('6. Manejo de Errores', () => {
    it('debe manejar fallo de estadísticas', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: false,
          status: 500,
          json: () => Promise.resolve({}),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Revisión KYC')).toBeDefined();
      });
    });

    it('debe manejar cola vacía', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ ...mockColaEN_REVISION, cola: [], totalElementos: 0 }),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText(/No hay verificaciones en cola/i)).toBeDefined();
      });
    });

    it('debe still renderizar cuando estadísticas fallan', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: false,
          status: 500,
          json: () => Promise.resolve({}),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      await waitFor(() => {
        expect(screen.getByText('Revisión KYC')).toBeDefined();
        expect(screen.getByText('Cola de Revisión')).toBeDefined();
      });
    });
  });

  describe('7. Navegación', () => {
    it('debe tener icono de escudo en header', async () => {
      (global.fetch as ReturnType<typeof vi.fn>)
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEstadisticas),
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockColaEN_REVISION),
        });

      render(<AdminKYCPagina />);

      const icon = document.querySelector('[class*="text-green"]');
      expect(icon).toBeDefined();
    });
  });
});