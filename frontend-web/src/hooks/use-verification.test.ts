import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useVerification } from '@/hooks/use-verification';
import { renderHook, act } from '@testing-library/react';

vi.mock('sonner', () => ({
  toast: { error: vi.fn(), success: vi.fn() },
}));

global.fetch = vi.fn();

describe('useVerification Hook', () => {
  const onSuccess = vi.fn();
  const onError = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('initial state', () => {
    it('tiene estado inicial idle', () => {
      const { result } = renderHook(() =>
        useVerification({ onSuccess, onError })
      );

      expect(result.current.step).toBe('idle');
      expect(result.current.isLoading).toBe(false);
      expect(result.current.error).toBeNull();
      expect(result.current.token).toBeNull();
    });
  });

  describe('reset', () => {
    it('reset limpia todos los estados', () => {
      const { result } = renderHook(() =>
        useVerification({ onSuccess, onError })
      );

      act(() => {
        result.current.reset();
      });

      expect(result.current.step).toBe('idle');
      expect(result.current.isLoading).toBe(false);
      expect(result.current.error).toBeNull();
      expect(result.current.token).toBeNull();
    });
  });

  describe('verifyPassword', () => {
    it('verifyPassword exitoso retorna true', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ tokenVerificacion: 'test-token-123' }),
      });

      const { result } = renderHook(() =>
        useVerification({ onSuccess, onError })
      );

      let verifyResult: boolean | undefined;
      await act(async () => {
        verifyResult = await result.current.verifyPassword('password123');
      });

      expect(verifyResult).toBe(true);
      expect(onSuccess).toHaveBeenCalledWith('test-token-123');
    });

    it('verifyPassword fallido retorna false', async () => {
      (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Contraseña incorrecta' }),
      });

      const { result } = renderHook(() =>
        useVerification({ onSuccess, onError })
      );

      let verifyResult: boolean | undefined;
      await act(async () => {
        verifyResult = await result.current.verifyPassword('wrongpassword');
      });

      expect(verifyResult).toBe(false);
      expect(result.current.step).toBe('error');
      expect(onError).toHaveBeenCalled();
    });
  });

  describe('confirmCode sin token', () => {
    it('confirmCode sin token retorna false', async () => {
      const { result } = renderHook(() =>
        useVerification({ onSuccess, onError })
      );

      let confirmResult: boolean | undefined;
      await act(async () => {
        confirmResult = await result.current.confirmCode('123456');
      });

      expect(confirmResult).toBe(false);
      expect(result.current.error).toBe('No hay código pendiente');
    });
  });
});