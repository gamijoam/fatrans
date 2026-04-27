import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import { VerifyPasswordDialog } from '@/components/features/profile/verify-password-dialog';

const mockOnVerified = vi.fn();
const mockOnOpenChange = vi.fn();

describe('VerifyPasswordDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe renderizar el dialog cuando está abierto', async () => {
    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    expect(screen.getByText('Verificación de Identidad')).toBeTruthy();
    expect(screen.getByLabelText(/contraseña actual/i)).toBeTruthy();
  });

  it('debe mostrar botón de verificar deshabilitado sin contraseña', async () => {
    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const button = screen.getByRole('button', { name: /verificar/i });
    expect(button).toBeDisabled();
  });

  it('debe habilitar botón cuando hay contraseña', async () => {
    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    fireEvent.change(input, { target: { value: 'password123' } });

    const button = screen.getByRole('button', { name: /verificar/i });
    expect(button).not.toBeDisabled();
  });

  it('debe llamar a la API al verificar contraseña correcta', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ valido: true, tokenVerificacion: 'test-token-123' }),
    } as any);

    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    fireEvent.change(input, { target: { value: 'password123' } });

    const button = screen.getByRole('button', { name: /verificar/i });
    fireEvent.click(button);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/perfil/verificar-password',
        expect.objectContaining({
          method: 'POST',
          credentials: 'include',
        })
      );
    });
  });

  it('debe llamar onVerified con token cuando contraseña es válida', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ valido: true, tokenVerificacion: 'valid-token-abc' }),
    } as any);

    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    fireEvent.change(input, { target: { value: 'correctPassword' } });

    const button = screen.getByRole('button', { name: /verificar/i });
    fireEvent.click(button);

    await waitFor(() => {
      expect(mockOnVerified).toHaveBeenCalledWith('valid-token-abc');
    });
  });

  it('debe mostrar error cuando contraseña es incorrecta', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ valido: false, message: 'Contraseña incorrecta' }),
    } as any);

    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    fireEvent.change(input, { target: { value: 'wrongPassword' } });

    const button = screen.getByRole('button', { name: /verificar/i });
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByText(/contraseña incorrecta/i)).toBeTruthy();
    });

    expect(mockOnVerified).not.toHaveBeenCalled();
  });

  it('debe limpiar error al cambiar contraseña', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ valido: false, message: 'Contraseña incorrecta' }),
    } as any);

    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    fireEvent.change(input, { target: { value: 'wrong' } });

    const button = screen.getByRole('button', { name: /verificar/i });
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByText(/contraseña incorrecta/i)).toBeTruthy();
    });

    fireEvent.change(input, { target: { value: 'newInput' } });

    await waitFor(() => {
      expect(screen.queryByText(/contraseña incorrecta/i)).toBeNull();
    });
  });

  it('debe manejar errores de red', async () => {
    global.fetch.mockImplementationOnce(() =>
      Promise.reject(new Error('Network error'))
    );

    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    fireEvent.change(input, { target: { value: 'password123' } });

    const button = screen.getByRole('button', { name: /verificar/i });
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByText('Network error')).toBeTruthy();
    });
  });

  it('debe resetear estado al cerrar', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ valido: true, tokenVerificacion: 'token' }),
    } as any);

    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    fireEvent.change(input, { target: { value: 'password123' } });

    const cancelButton = screen.getByRole('button', { name: /cancelar/i });
    fireEvent.click(cancelButton);

    expect(mockOnOpenChange).toHaveBeenCalledWith(false);
  });

  it('debe usar descripción personalizada cuando se provee', async () => {
    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
        description="Cambio de email requerido"
      />
    );

    expect(screen.getByText('Cambio de email requerido')).toBeTruthy();
  });

  it('debe toggle visibilidad de contraseña', async () => {
    render(
      <VerifyPasswordDialog
        isOpen={true}
        onOpenChange={mockOnOpenChange}
        onVerified={mockOnVerified}
      />
    );

    const input = screen.getByLabelText(/contraseña actual/i);
    expect(input).toHaveAttribute('type', 'password');

    const toggleButton = screen.getByRole('button', { name: /mostrar contraseña/i });
    fireEvent.click(toggleButton);

    expect(input).toHaveAttribute('type', 'text');
  });
});