import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ChangePasswordModal } from '@/components/shared/change-password-modal';
import { useAuthStore } from '@/stores/auth-store';
import { toast } from 'sonner';

/**
 * Tests del fix del modal de cambio de password (19-may-2026).
 *
 * Bug reportado: socios cambiaban su password al primer login, el modal se
 * cerraba, pero al refrescar la página o volver de la verificación facial
 * el modal aparecía de nuevo. Causa raíz: el modal hacía update optimista
 * (Zustand local) sin verificar que el backend persistió el cambio. Si por
 * algún motivo el backend devolvía 200 pero no flushaba el flag, el
 * próximo `/me` devolvía `debeCambiarPassword=true` y el modal reaparecía.
 *
 * Fix: después del 200 del cambio, refetch `/api/auth/me` y validar que el
 * backend confirma el cambio. Si no, mostramos error en lugar de fingir éxito.
 */

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const baseUser = {
  id: 'user-1',
  nombreUsuario: 'socio_prueba',
  correoElectronico: 'socio@test.com',
  nombreCompleto: 'Socio Prueba',
  rol: 'SOCIO' as const,
  socioId: 'socio-1',
  debeCambiarPassword: true,
};

describe('ChangePasswordModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
    // Reset del store entre tests para evitar leak.
    useAuthStore.setState({
      user: baseUser,
      isAuthenticated: true,
      isLoading: false,
    });
  });

  it('NO renderiza si el user no tiene debeCambiarPassword', () => {
    useAuthStore.setState({
      user: { ...baseUser, debeCambiarPassword: false },
      isAuthenticated: true,
      isLoading: false,
    });

    render(<ChangePasswordModal open />);

    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('renderiza el modal cuando debeCambiarPassword=true', () => {
    render(<ChangePasswordModal open />);

    // Hay título + botón con el mismo texto — basta confirmar que el dialog está montado.
    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByLabelText(/Contraseña Actual/i)).toBeDefined();
    expect(screen.getByLabelText('Nueva Contraseña')).toBeDefined();
  });

  it('después de cambio exitoso refetch-ea /api/auth/me', async () => {
    (global.fetch as ReturnType<typeof vi.fn>)
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ message: 'OK' }),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () =>
          Promise.resolve({
            id: 'user-1',
            nombreUsuario: 'socio_prueba',
            correoElectronico: 'socio@test.com',
            nombreCompleto: 'Socio Prueba',
            rol: 'SOCIO',
            socioId: 'socio-1',
            debeCambiarPassword: false,
          }),
      } as Response);

    render(<ChangePasswordModal open />);

    await act(async () => {
      await userEvent.type(
        screen.getByLabelText(/Contraseña Actual/i),
        'OldPass123!'
      );
      await userEvent.type(
        screen.getByLabelText('Nueva Contraseña'),
        'NewPass123!'
      );
      await userEvent.type(
        screen.getByLabelText(/Confirmar Nueva Contraseña/i),
        'NewPass123!'
      );
      await userEvent.click(
        screen.getByRole('button', { name: 'Cambiar Contraseña' })
      );
    });

    await waitFor(() => {
      // Llamada 1: POST cambiar-password. Llamada 2: GET /me (refetch).
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/auth/cambiar-password',
        expect.objectContaining({ method: 'POST' })
      );
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/auth/me',
        expect.objectContaining({ credentials: 'include' })
      );
    });

    // El store debe quedar con debeCambiarPassword=false.
    await waitFor(() => {
      expect(useAuthStore.getState().user?.debeCambiarPassword).toBe(false);
    });

    expect(toast.success).toHaveBeenCalled();
  });

  it('si el backend devuelve 200 pero /me sigue diciendo debeCambiarPassword=true, muestra error', async () => {
    (global.fetch as ReturnType<typeof vi.fn>)
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ message: 'OK' }),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () =>
          Promise.resolve({
            ...baseUser,
            debeCambiarPassword: true, // backend no persistió
          }),
      } as Response);

    render(<ChangePasswordModal open />);

    await act(async () => {
      await userEvent.type(
        screen.getByLabelText(/Contraseña Actual/i),
        'OldPass123!'
      );
      await userEvent.type(
        screen.getByLabelText('Nueva Contraseña'),
        'NewPass123!'
      );
      await userEvent.type(
        screen.getByLabelText(/Confirmar Nueva Contraseña/i),
        'NewPass123!'
      );
      await userEvent.click(
        screen.getByRole('button', { name: 'Cambiar Contraseña' })
      );
    });

    // El store NO debe quedar con debeCambiarPassword=false porque backend no confirmó.
    await waitFor(() => {
      expect(useAuthStore.getState().user?.debeCambiarPassword).toBe(true);
    });

    // El modal debe seguir visible (sigue requiriendo cambio).
    // Hay título + botón con el mismo texto — basta confirmar que el dialog está montado.
    expect(screen.getByRole('dialog')).toBeDefined();
    // Y debe mostrar un mensaje de error.
    expect(
      screen.getByText(/no la guardó|Intentá de nuevo/i)
    ).toBeDefined();
  });

  it('si /me falla (network error), aplica update optimista para no atrapar al socio', async () => {
    (global.fetch as ReturnType<typeof vi.fn>)
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ message: 'OK' }),
      } as Response)
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: () => Promise.resolve({}),
      } as Response);

    render(<ChangePasswordModal open />);

    await act(async () => {
      await userEvent.type(
        screen.getByLabelText(/Contraseña Actual/i),
        'OldPass123!'
      );
      await userEvent.type(
        screen.getByLabelText('Nueva Contraseña'),
        'NewPass123!'
      );
      await userEvent.type(
        screen.getByLabelText(/Confirmar Nueva Contraseña/i),
        'NewPass123!'
      );
      await userEvent.click(
        screen.getByRole('button', { name: 'Cambiar Contraseña' })
      );
    });

    await waitFor(() => {
      // Update optimista: el store queda con debeCambiarPassword=false
      // aunque /me haya fallado — para no atrapar al socio con un modal
      // permanente si la red está flaky.
      expect(useAuthStore.getState().user?.debeCambiarPassword).toBe(false);
    });
  });

  it('valida que las contraseñas coincidan', async () => {
    render(<ChangePasswordModal open />);

    await act(async () => {
      await userEvent.type(
        screen.getByLabelText(/Contraseña Actual/i),
        'OldPass123!'
      );
      await userEvent.type(
        screen.getByLabelText('Nueva Contraseña'),
        'NewPass123!'
      );
      await userEvent.type(
        screen.getByLabelText(/Confirmar Nueva Contraseña/i),
        'OtraPass123!'
      );
      await userEvent.click(
        screen.getByRole('button', { name: 'Cambiar Contraseña' })
      );
    });

    expect(screen.getByText(/no coinciden/i)).toBeDefined();
    // No debe haber hecho fetch al backend.
    expect(global.fetch).not.toHaveBeenCalled();
  });

  it('muestra error cuando el backend rechaza el cambio (401)', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: false,
      status: 401,
      headers: { get: () => 'application/json' },
      json: () => Promise.resolve({ message: 'Contraseña actual incorrecta' }),
    } as unknown as Response);

    render(<ChangePasswordModal open />);

    await act(async () => {
      await userEvent.type(
        screen.getByLabelText(/Contraseña Actual/i),
        'WrongPass!'
      );
      await userEvent.type(
        screen.getByLabelText('Nueva Contraseña'),
        'NewPass123!'
      );
      await userEvent.type(
        screen.getByLabelText(/Confirmar Nueva Contraseña/i),
        'NewPass123!'
      );
      await userEvent.click(
        screen.getByRole('button', { name: 'Cambiar Contraseña' })
      );
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Contraseña actual incorrecta/i)
      ).toBeDefined();
    });

    // El store sigue con debeCambiarPassword=true porque el cambio falló.
    expect(useAuthStore.getState().user?.debeCambiarPassword).toBe(true);
  });
});
