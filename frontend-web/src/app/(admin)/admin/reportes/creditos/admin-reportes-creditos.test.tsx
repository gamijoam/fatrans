import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import AdminReporteCreditosPage from '@/app/(admin)/admin/reportes/creditos/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
  },
}));

const mockCreditosData = {
  content: [
    {
      id: 'cred-1',
      numeroSolicitud: 'SC-2024-00001',
      socioNombre: 'Juan Pérez',
      socioCedula: 'V-30123456',
      tipoCredito: 'Crédito Personal',
      montoSolicitado: 5000000,
      plazoMeses: 12,
      tasaInteres: 0.024,
      estado: 'PENDIENTE',
      fechaSolicitud: '2024-03-15T10:30:00Z',
      fechaAprobacion: null,
    },
    {
      id: 'cred-2',
      numeroSolicitud: 'SC-2024-00002',
      socioNombre: 'María López',
      socioCedula: 'V-30234567',
      tipoCredito: 'Crédito Hipotecario',
      montoSolicitado: 50000000,
      plazoMeses: 60,
      tasaInteres: 0.018,
      estado: 'APROBADA',
      fechaSolicitud: '2024-03-10T14:20:00Z',
      fechaAprobacion: '2024-03-12T09:00:00Z',
    },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 20,
  number: 0,
  first: true,
  last: true,
};

describe('AdminReporteCreditosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('Renderizado', () => {
    it('debe mostrar título de la página', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Reporte de Créditos')).toBeDefined();
      });
    });

    it('debe mostrar descripción', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText(/Estado de solicitudes y créditos/i)).toBeDefined();
      });
    });
  });

  describe('Estadísticas', () => {
    it('debe mostrar stats de total', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Total')).toBeDefined();
      });
    });

    it('debe mostrar stats de pendientes', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Pendientes')).toBeDefined();
      });
    });

    it('debe mostrar stats de aprobados', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Aprobados')).toBeDefined();
      });
    });

    it('debe mostrar stats de rechazados', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Rechazados')).toBeDefined();
      });
    });
  });

  describe('Tabla de Créditos', () => {
    it('debe mostrar número de solicitud', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('SC-2024-00001')).toBeDefined();
      });
    });

    it('debe mostrar nombre del socio', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Juan Pérez')).toBeDefined();
      });
    });

    it('debe mostrar tipo de crédito', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Crédito Personal')).toBeDefined();
        expect(screen.getByText('Crédito Hipotecario')).toBeDefined();
      });
    });

    it('debe mostrar plazo en meses', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('12 meses')).toBeDefined();
        expect(screen.getByText('60 meses')).toBeDefined();
      });
    });

    it('debe mostrar tasa de interés', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('2.40%')).toBeDefined();
        expect(screen.getByText('1.80%')).toBeDefined();
      });
    });
  });

  describe('Badges de Estado', () => {
    it('debe mostrar badge PENDIENTE', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('PENDIENTE')).toBeDefined();
      });
    });

    it('debe mostrar badge APROBADA', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('APROBADA')).toBeDefined();
      });
    });
  });

  describe('Filtros', () => {
    it('debe tener selector de filtro por estado', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        const selects = screen.getAllByRole('combobox');
        expect(selects.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Botón Exportar', () => {
    it('debe tener botón Exportar', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText('Exportar')).toBeDefined();
      });
    });
  });

  describe('Navegación', () => {
    it('debe tener enlace para volver a reportes', () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCreditosData),
      });

      const { container } = render(<AdminReporteCreditosPage />);

      const backLink = container.querySelector('a[href="/admin/reportes"]');
      expect(backLink).toBeDefined();
    });
  });

  describe('Manejo de errores', () => {
    it('debe mostrar mensaje cuando no hay créditos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ content: [], totalElements: 0 }),
      });

      render(<AdminReporteCreditosPage />);

      await waitFor(() => {
        expect(screen.getByText(/No hay créditos registrados/i)).toBeDefined();
      });
    });
  });
});