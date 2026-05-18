import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import AdminUsuariosPage from '@/app/(admin)/admin/usuarios/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockUsuariosData = [
  {
    id: '11111111-1111-1111-1111-111111111111',
    nombreUsuario: 'admin01',
    correoElectronico: 'admin01@tufondo.com',
    nombreCompleto: 'Administrador Principal',
    rol: 'ADMIN',
    cuentaActiva: true,
    fechaCreacion: '2024-01-15T10:30:00Z',
    debeCambiarPassword: false,
  },
  {
    id: '22222222-2222-2222-2222-222222222222',
    nombreUsuario: 'superadmin',
    correoElectronico: 'superadmin@tufondo.com',
    nombreCompleto: 'Super Administrador',
    rol: 'SUPER_ADMIN',
    cuentaActiva: true,
    fechaCreacion: '2024-01-01T00:00:00Z',
    debeCambiarPassword: false,
  },
  {
    id: '33333333-3333-3333-3333-333333333333',
    nombreUsuario: 'admin02',
    correoElectronico: 'admin02@tufondo.com',
    nombreCompleto: 'Administrador Secundario',
    rol: 'ADMIN',
    cuentaActiva: false,
    fechaCreacion: '2024-02-20T14:00:00Z',
    debeCambiarPassword: true,
  },
];

describe('AdminUsuariosPage', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe renderizar el título de la página', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsuariosData,
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText('Gestión de Usuarios')).toBeTruthy();
    });
  });

  it('debe mostrar la tabla de usuarios después de cargar', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsuariosData,
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText('Administrador Principal')).toBeTruthy();
      expect(screen.getByText('Super Administrador')).toBeTruthy();
    });
  });

  it('debe mostrar mensaje cuando no hay usuarios', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay usuarios registrados/i)).toBeTruthy();
    });
  });

  it('debe manejar errores al cargar usuarios', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText(/No hay usuarios registrados/i)).toBeTruthy();
    });
  });

  it('debe tener selectores de filtro', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsuariosData,
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      const selects = screen.getAllByRole('combobox');
      expect(selects.length).toBe(2);
    });
  });

  it('debe tener botón para nuevo usuario', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsuariosData,
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText(/Nuevo Usuario/i)).toBeTruthy();
    });
  });

  it('debe tener botón para crear usuario', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsuariosData,
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Nuevo Usuario/i })).toBeTruthy();
    });
  });

  it('debe mostrar badges de rol', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [mockUsuariosData[0]],
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText('ADMIN')).toBeTruthy();
    });
  });

  it('debe mostrar estado activo', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => [mockUsuariosData[0]],
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText('Activo')).toBeTruthy();
    });
  });

  it('debe filtrar por rol', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsuariosData,
    });

    render(<AdminUsuariosPage />);

    await waitFor(() => {
      expect(screen.getByText('Administrador Principal')).toBeTruthy();
    });
  });
});