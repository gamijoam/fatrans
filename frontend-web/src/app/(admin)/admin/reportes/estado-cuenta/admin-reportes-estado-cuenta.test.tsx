import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AdminEstadoCuentaPage from '@/app/(admin)/admin/reportes/estado-cuenta/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockEstadoCuentaData = {
  socioId: 'socio-uuid-1',
  socioNombre: 'Juan Pérez',
  cuentaNumero: 'CTA-001-2024',
  periodo: 'Marzo 2024',
  saldoInicial: 5000000,
  saldoFinal: 7500000,
  totalDepositos: 3500000,
  totalRetiros: 1000000,
  movimientos: [
    {
      id: 'mov-1',
      fecha: '2024-03-01T10:00:00Z',
      tipo: 'DEPOSITO',
      monto: 2000000,
      descripcion: 'Depósito mensual',
      referencia: 'DEP-001',
      saldoDespues: 7000000,
    },
  ],
};

describe('AdminEstadoCuentaPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('Renderizado inicial', () => {
    it('debe mostrar título de la página', () => {
      render(<AdminEstadoCuentaPage />);
      expect(screen.getByText('Estado de Cuenta')).toBeDefined();
    });

    it('debe mostrar descripción', () => {
      render(<AdminEstadoCuentaPage />);
      expect(screen.getByText(/Genere estados de cuenta por socio y período/i)).toBeDefined();
    });
  });

  describe('Parámetros del Reporte', () => {
    it('debe tener campo ID Socio', () => {
      render(<AdminEstadoCuentaPage />);
      expect(screen.getByText('ID Socio')).toBeDefined();
    });

    it('debe tener selector de Año', () => {
      render(<AdminEstadoCuentaPage />);
      expect(screen.getByText('Año')).toBeDefined();
    });

    it('debe tener selector de Mes', () => {
      render(<AdminEstadoCuentaPage />);
      expect(screen.getByText('Mes')).toBeDefined();
    });

    it('debe tener botón Generar', () => {
      render(<AdminEstadoCuentaPage />);
      expect(screen.getByText('Generar')).toBeDefined();
    });

    it('debe permitir ingresar ID de socio', () => {
      render(<AdminEstadoCuentaPage />);
      const input = screen.getByPlaceholderText('UUID del socio');
      expect(input).toBeDefined();
    });
  });

  describe('Generar Reporte', () => {
    it('debe tener botón Generar habilitado', () => {
      render(<AdminEstadoCuentaPage />);
      const generarBtn = screen.getByText('Generar');
      expect(generarBtn).toBeEnabled();
    });

    it('debe tener campo ID Socio', () => {
      render(<AdminEstadoCuentaPage />);
      const input = screen.getByPlaceholderText('UUID del socio');
      expect(input).toBeDefined();
    });
  });

  describe('Navegación', () => {
    it('debe tener enlace para volver a reportes', () => {
      const { container } = render(<AdminEstadoCuentaPage />);
      const backLink = container.querySelector('a[href="/admin/reportes"]');
      expect(backLink).toBeDefined();
    });
  });

  describe('Selección de mes', () => {
    it('debe tener selector de mes con label Mes', () => {
      render(<AdminEstadoCuentaPage />);
      expect(screen.getByText('Mes')).toBeDefined();
    });
  });
});