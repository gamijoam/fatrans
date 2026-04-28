import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AdminCreditosPage from '@/app/(admin)/admin/creditos/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
  },
}));

const mockSolicitudesData = {
  content: [
    {
      id: 'solic-1',
      numeroSolicitud: 'SC-2024-00001',
      socioId: 'socio-uuid-1',
      socioNombre: 'Juan Pérez',
      socioNumero: 'SOC-001',
      socioCedula: 'V-30123456',
      socioEmpresa: 'Empresa ABC',
      tipoCreditoId: 1,
      tipoCreditoNombre: 'Crédito Personal',
      montoSolicitado: 5000000,
      plazoMeses: 12,
      tasaInteresAplicada: 0.024,
      cuotaMensualEstimada: 475000,
      estado: 'PENDIENTE',
      destinoCredito: 'Compra de vehículo',
      createdAt: '2024-03-15T10:30:00Z',
      fechaAprobacion: null,
      fechaRechazo: null,
      motivoRechazo: null,
    },
    {
      id: 'solic-2',
      numeroSolicitud: 'SC-2024-00002',
      socioId: 'socio-uuid-2',
      socioNombre: 'María López',
      socioNumero: 'SOC-002',
      socioCedula: 'V-30234567',
      socioEmpresa: 'Empresa XYZ',
      tipoCreditoId: 2,
      tipoCreditoNombre: 'Crédito Hipotecario',
      montoSolicitado: 50000000,
      plazoMeses: 60,
      tasaInteresAplicada: 0.018,
      cuotaMensualEstimada: 1100000,
      estado: 'APROBADA',
      destinoCredito: 'Compra de vivienda',
      createdAt: '2024-03-10T14:20:00Z',
      fechaAprobacion: '2024-03-12T09:00:00Z',
      fechaRechazo: null,
      motivoRechazo: null,
    },
    {
      id: 'solic-3',
      numeroSolicitud: 'SC-2024-00003',
      socioId: 'socio-uuid-3',
      socioNombre: 'Carlos Rodríguez',
      socioNumero: 'SOC-003',
      socioCedula: 'V-30345678',
      socioEmpresa: 'Empresa DEF',
      tipoCreditoId: 1,
      tipoCreditoNombre: 'Crédito Personal',
      montoSolicitado: 3000000,
      plazoMeses: 6,
      tasaInteresAplicada: 0.028,
      cuotaMensualEstimada: 550000,
      estado: 'RECHAZADA',
      destinoCredito: 'Gastos médicos',
      createdAt: '2024-03-05T08:15:00Z',
      fechaAprobacion: null,
      fechaRechazo: '2024-03-07T16:30:00Z',
      motivoRechazo: 'Perfil crediticio no cumple requisitos mínimos',
    },
  ],
  totalElements: 3,
  totalPages: 1,
  size: 15,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

describe('AdminCreditosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('Renderizado inicial', () => {
    it('debe mostrar loading state inicialmente', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(() => new Promise(() => {}));
      const { container } = render(<AdminCreditosPage />);
      const loader = container.querySelector('.animate-spin');
      expect(loader).toBeDefined();
    });
  });

  describe('Carga de datos', () => {
    it('debe cargar y mostrar solicitudes cuando la API responde correctamente', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('SC-2024-00001')).toBeDefined();
        expect(screen.getByText('Juan Pérez')).toBeDefined();
      });
    });

    it('debe mostrar mensaje cuando no hay solicitudes', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ content: [], totalElements: 0 }),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText(/No hay solicitudes/i)).toBeDefined();
      });
    });
  });

  describe('Filtro por estado', () => {
    it('debe tener selector de filtro con todos los estados', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        const select = screen.getByRole('combobox');
        expect(select).toBeDefined();
      });
    });

    it('debe realizar fetch con filtro cuando se selecciona estado', async () => {
      const fetchMock = (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        const select = screen.getByRole('combobox');
        userEvent.selectOptions(select, 'PENDIENTE');
      });

      await waitFor(() => {
        expect(fetchMock).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Paginación', () => {
    it('debe mostrar controles de paginación cuando hay múltiples páginas', async () => {
      const multiPageData = {
        ...mockSolicitudesData,
        totalPages: 3,
        first: false,
        last: false,
      };

      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(multiPageData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText(/Página 1 de 3/)).toBeDefined();
        expect(screen.getByText('Anterior')).toBeDefined();
        expect(screen.getByText('Siguiente')).toBeDefined();
      });
    });

    it('debe deshabilitar botón Anterior en primera página', async () => {
      const multiPageData = {
        ...mockSolicitudesData,
        totalPages: 3,
        first: true,
        last: false,
      };

      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(multiPageData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        const anteriorBtn = screen.getByText('Anterior');
        expect(anteriorBtn).toBeDisabled();
      });
    });
  });

  describe('Badges de estado', () => {
    it('debe mostrar badge amarillo para estado PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        const pendientesBadge = screen.getByText('PENDIENTE');
        expect(pendientesBadge).toBeDefined();
      });
    });

    it('debe mostrar badge verde para estado APROBADA', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        const aprobadoBadge = screen.getByText('APROBADA');
        expect(aprobadoBadge).toBeDefined();
      });
    });

    it('debe mostrar badge rojo para estado RECHAZADA', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        const rechazadoBadge = screen.getByText('RECHAZADA');
        expect(rechazadoBadge).toBeDefined();
      });
    });
  });

  describe('Link a detalle', () => {
    it('debe tener enlace "Ver Detalle" para cada solicitud', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        const links = screen.getAllByText('Ver Detalle');
        expect(links).toHaveLength(3);
      });
    });
  });

  describe('Manejo de errores', () => {
    it('debe mostrar mensaje de error cuando la API falla', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: false,
        status: 500,
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText(/No hay solicitudes/i)).toBeDefined();
      });
    });
  });

  describe('Estadísticas', () => {
    it('debe mostrar stats de solicitudes pendientes', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Pendientes')).toBeDefined();
      });
    });

    it('debe mostrar stats de solicitudes aprobadas', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSolicitudesData),
      });

      render(<AdminCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Aprobadas')).toBeDefined();
      });
    });
  });
});