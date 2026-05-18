import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import AdminSociosPage from '@/app/(admin)/admin/socios/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockSociosData = {
  content: [
    {
      id: '11111111-1111-1111-1111-111111111111',
      numeroSocio: 'SOC-2024-001',
      tipoDocumento: 'CEDULA',
      numeroDocumento: 'V-30123456',
      primerNombre: 'Juan',
      segundoNombre: 'Carlos',
      primerApellido: 'Pérez',
      segundoApellido: 'García',
      correoElectronico: 'juan.perez@test.com',
      telefonoPrincipal: '04121234567',
      empresa: 'Empresa ABC',
      estado: 'ACTIVO',
      fechaRegistro: '2024-01-15T10:30:00Z',
    },
    {
      id: '22222222-2222-2222-2222-222222222222',
      numeroSocio: 'SOC-2024-002',
      tipoDocumento: 'CEDULA',
      numeroDocumento: 'V-30234567',
      primerNombre: 'María',
      segundoNombre: 'Isabel',
      primerApellido: 'López',
      segundoApellido: 'Mendoza',
      correoElectronico: 'maria.lopez@test.com',
      telefonoPrincipal: '04141234567',
      empresa: 'Empresa XYZ',
      estado: 'PENDIENTE',
      fechaRegistro: '2024-02-20T14:00:00Z',
    },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 15,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

describe('AdminSociosPage', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe renderizar el título de la página', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockSociosData,
    });

    render(<AdminSociosPage />);

    await waitFor(() => {
      expect(screen.getByText('Gestión de Socios')).toBeTruthy();
    });
  });

  it('debe mostrar la tabla de socios después de cargar', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockSociosData,
    });

    render(<AdminSociosPage />);

    await waitFor(() => {
      expect(screen.getByText('Juan Carlos Pérez García')).toBeTruthy();
    });
  });

  it('debe mostrar mensaje cuando no hay socios', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        content: [],
        totalElements: 0,
        totalPages: 0,
      }),
    });

    render(<AdminSociosPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay socios registrados/i)).toBeTruthy();
    });
  });

  it('debe manejar errores al cargar socios', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    render(<AdminSociosPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay socios registrados/i)).toBeTruthy();
    });
  });

  it('debe tener input de búsqueda', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockSociosData,
    });

    render(<AdminSociosPage />);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/Buscar por cédula/i)).toBeTruthy();
    });
  });

  it('debe tener select de filtros de estado', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockSociosData,
    });

    render(<AdminSociosPage />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeTruthy();
    });
  });

  it('debe tener headers de tabla correctos', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockSociosData,
    });

    render(<AdminSociosPage />);

    await waitFor(() => {
      expect(screen.getByText('Nro. Socio')).toBeTruthy();
      expect(screen.getByText('Nombre')).toBeTruthy();
      expect(screen.getByText('Cédula')).toBeTruthy();
      expect(screen.getByText('Email')).toBeTruthy();
    });
  });
});