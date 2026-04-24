import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import AdminReportesPage from '@/app/(admin)/admin/reportes/page';

describe('AdminReportesPage', () => {
  describe('Renderizado', () => {
    it('debe mostrar título principal', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Reportes y Estadísticas')).toBeDefined();
    });

    it('debe mostrar descripción de la página', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText(/Genere y descargue reportes del sistema/i)).toBeDefined();
    });
  });

  describe('Menú de Reportes', () => {
    it('debe mostrar 3 reportes en el menú', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Reporte de Socios')).toBeDefined();
      expect(screen.getByText('Reporte de Créditos')).toBeDefined();
      expect(screen.getByText('Estados de Cuenta')).toBeDefined();
    });

    it('debe mostrar descripción de cada reporte', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText(/Lista completa de socios activos e inactivos/i)).toBeDefined();
      expect(screen.getByText(/Estado de solicitudes y créditos/i)).toBeDefined();
      expect(screen.getByText(/Movimientos y saldos por cuenta/i)).toBeDefined();
    });

    it('debe tener enlaces a las páginas de reportes', () => {
      render(<AdminReportesPage />);
      const links = screen.getAllByRole('link');
      expect(links.length).toBeGreaterThanOrEqual(3);
    });

    it('debe tener enlace a /admin/reportes/socios', () => {
      render(<AdminReportesPage />);
      const link = screen.getByRole('link', { name: /Reporte de Socios/i });
      expect(link).toBeDefined();
      expect((link as HTMLAnchorElement).href).toContain('/admin/reportes/socios');
    });

    it('debe tener enlace a /admin/reportes/creditos', () => {
      render(<AdminReportesPage />);
      const link = screen.getByRole('link', { name: /Reporte de Créditos/i });
      expect(link).toBeDefined();
      expect((link as HTMLAnchorElement).href).toContain('/admin/reportes/creditos');
    });

    it('debe tener enlace a /admin/reportes/estado-cuenta', () => {
      render(<AdminReportesPage />);
      const link = screen.getByRole('link', { name: /Estados de Cuenta/i });
      expect(link).toBeDefined();
      expect((link as HTMLAnchorElement).href).toContain('/admin/reportes/estado-cuenta');
    });
  });

  describe('Formatos de Descarga', () => {
    it('debe mostrar sección de formatos', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Formatos de Descarga')).toBeDefined();
    });

    it('debe mostrar opciones PDF, Excel y CSV en sección de formatos', () => {
      render(<AdminReportesPage />);
      const formatosSection = screen.getByText('Formatos de Descarga').closest('div')?.parentElement;
      expect(formatosSection?.textContent).toContain('PDF');
      expect(formatosSection?.textContent).toContain('Excel (XLSX)');
      expect(formatosSection?.textContent).toContain('CSV');
    });
  });

  describe('Filtros Comunes', () => {
    it('debe mostrar sección de filtros', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Filtros Comunes')).toBeDefined();
    });

    it('debe tener selector de Rango de Fechas', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Rango de Fechas')).toBeDefined();
    });

    it('debe tener selector de Estado', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Estado')).toBeDefined();
    });

    it('debe tener selector de Formato', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Formato')).toBeDefined();
    });

    it('debe tener opciones de rango de fechas', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Último mes')).toBeDefined();
      expect(screen.getByText('Último trimestre')).toBeDefined();
      expect(screen.getByText('Último año')).toBeDefined();
    });

    it('debe tener opciones de estado', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Todos')).toBeDefined();
      expect(screen.getByText('Activos')).toBeDefined();
      expect(screen.getByText('Inactivos')).toBeDefined();
    });
  });

  describe('Estructura de cards', () => {
    it('debe tener sección Menú de Reportes', () => {
      render(<AdminReportesPage />);
      expect(screen.getByText('Menú de Reportes')).toBeDefined();
    });
  });
});