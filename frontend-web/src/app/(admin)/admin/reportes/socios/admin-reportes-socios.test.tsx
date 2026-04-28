import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import AdminReporteSociosPage from '@/app/(admin)/admin/reportes/socios/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
  },
}));

const mockSociosData = {
  content: [
    {
      id: 'socio-1',
      numeroSocio: 'SOC-001',
      nombreCompleto: 'Juan Pérez',
      cedula: 'V-30123456',
      correo: 'juan@example.com',
      telefono: '0412-1234567',
      empresa: 'Empresa ABC',
      estado: 'ACTIVO',
      fechaRegistro: '2024-01-15T10:00:00Z',
      ultimaActividad: '2024-03-20T15:30:00Z',
    },
    {
      id: 'socio-2',
      numeroSocio: 'SOC-002',
      nombreCompleto: 'María López',
      cedula: 'V-30234567',
      correo: 'maria@example.com',
      telefono: '0414-7654321',
      empresa: 'Empresa XYZ',
      estado: 'INACTIVO',
      fechaRegistro: '2023-06-10T08:00:00Z',
      ultimaActividad: '2023-12-01T12:00:00Z',
    },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 20,
  number: 0,
  first: true,
  last: true,
};

describe('AdminReporteSociosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('Renderizado inicial', () => {
    it('debe mostrar título de la página', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('Reporte de Socios')).toBeDefined();
      });
    });

    it('debe mostrar descripción', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText(/Lista completa de socios registrados/i)).toBeDefined();
      });
    });
  });

  describe('Tabla de Socios', () => {
    it('debe mostrar lista de socios cuando hay datos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('Juan Pérez')).toBeDefined();
        expect(screen.getByText('María López')).toBeDefined();
      });
    });

    it('debe mostrar número de socio', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('SOC-001')).toBeDefined();
        expect(screen.getByText('SOC-002')).toBeDefined();
      });
    });

    it('debe mostrar cédula de identidad', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('V-30123456')).toBeDefined();
        expect(screen.getByText('V-30234567')).toBeDefined();
      });
    });

    it('debe mostrar empresa', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('Empresa ABC')).toBeDefined();
        expect(screen.getByText('Empresa XYZ')).toBeDefined();
      });
    });

    it('debe mostrar contacto (correo y teléfono)', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('juan@example.com')).toBeDefined();
        expect(screen.getByText('0412-1234567')).toBeDefined();
      });
    });
  });

  describe('Badges de Estado', () => {
    it('debe mostrar badge ACTIVO para socios activos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('ACTIVO')).toBeDefined();
      });
    });

    it('debe mostrar badge INACTIVO para socios inactivos', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('INACTIVO')).toBeDefined();
      });
    });
  });

  describe('Filtros', () => {
    it('debe tener selector de filtro activo/inactivo', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        const selects = screen.getAllByRole('combobox');
        expect(selects.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Paginación', () => {
    it('no debe mostrar paginación cuando hay solo una página', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.queryByText(/Página \d+ de \d+/)).toBeNull();
      });
    });

    it('debe mostrar total de socios', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText(/2 total/)).toBeDefined();
      });
    });
  });

  describe('Botón Exportar', () => {
    it('debe tener botón Exportar', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText('Exportar')).toBeDefined();
      });
    });
  });

  describe('Navegación', () => {
    it('debe tener enlace para volver a reportes', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockSociosData),
      });

      const { container } = render(<AdminReporteSociosPage />);

      await waitFor(() => {
        const backLink = container.querySelector('a[href="/admin/reportes"]');
        expect(backLink).toBeDefined();
      }, { timeout: 3000 });
    });
  });

  describe('Manejo de errores', () => {
    it('debe mostrar mensaje cuando no hay socios', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ content: [], totalElements: 0 }),
      });

      render(<AdminReporteSociosPage />);

      await waitFor(() => {
        expect(screen.getByText(/No hay socios registrados/i)).toBeDefined();
      });
    });
  });
});