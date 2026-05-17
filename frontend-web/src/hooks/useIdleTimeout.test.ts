import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useIdleTimeout } from './useIdleTimeout';

/**
 * Tests para issue #221: idle timeout watcher.
 *
 * Usa fake timers para no esperar segundos/minutos reales. Simulamos
 * actividad disparando eventos del DOM en `document` (jsdom).
 */
describe('useIdleTimeout (issue #221)', () => {
  let onTimeout: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.useFakeTimers();
    onTimeout = vi.fn();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('Después de `idleMs` sin actividad → muestra warning', () => {
    const { result } = renderHook(() =>
      useIdleTimeout({
        idleMs: 9000,
        warningCountdownMs: 1000,
        onTimeout,
      })
    );

    expect(result.current.showWarning).toBe(false);

    act(() => {
      vi.advanceTimersByTime(9000);
    });

    expect(result.current.showWarning).toBe(true);
    expect(onTimeout).not.toHaveBeenCalled();
  });

  it('Issue #221: si nadie responde al warning → onTimeout se llama tras warningCountdownMs', () => {
    renderHook(() =>
      useIdleTimeout({
        idleMs: 9000,
        warningCountdownMs: 1000,
        onTimeout,
      })
    );

    act(() => {
      vi.advanceTimersByTime(9000); // entra en warning
    });
    act(() => {
      vi.advanceTimersByTime(1000); // expira el countdown
    });

    expect(onTimeout).toHaveBeenCalledTimes(1);
  });

  it('Actividad antes del warning → resetea el timer (NO triggea warning)', () => {
    const { result } = renderHook(() =>
      useIdleTimeout({
        idleMs: 9000,
        warningCountdownMs: 1000,
        onTimeout,
      })
    );

    // Pasamos 5s (menos que idleMs)
    act(() => {
      vi.advanceTimersByTime(5000);
    });

    // Actividad: reset
    act(() => {
      document.dispatchEvent(new Event('mousedown'));
    });

    // Otros 5s → todavía no debería haber warning (porque se reseteó)
    act(() => {
      vi.advanceTimersByTime(5000);
    });

    expect(result.current.showWarning).toBe(false);

    // Si pasan 4s más → ahora sí (total desde el reset: 9s)
    act(() => {
      vi.advanceTimersByTime(4000);
    });

    expect(result.current.showWarning).toBe(true);
  });

  it('Issue #221: actividad DURANTE el warning NO debe cancelarlo (anti-mousemove fantasma)', () => {
    const { result } = renderHook(() =>
      useIdleTimeout({
        idleMs: 1000,
        warningCountdownMs: 5000,
        onTimeout,
      })
    );

    // Entrar al warning
    act(() => {
      vi.advanceTimersByTime(1000);
    });
    expect(result.current.showWarning).toBe(true);

    // Simular actividad accidental durante el countdown
    act(() => {
      document.dispatchEvent(new Event('mousemove'));
    });

    // El warning DEBE seguir activo (usuario debe presionar "Seguir conectado")
    expect(result.current.showWarning).toBe(true);

    // Si nadie hace nada, expira normalmente
    act(() => {
      vi.advanceTimersByTime(5000);
    });
    expect(onTimeout).toHaveBeenCalledTimes(1);
  });

  it('dismissWarning() cierra el warning y reinicia el ciclo idle', () => {
    const { result } = renderHook(() =>
      useIdleTimeout({
        idleMs: 1000,
        warningCountdownMs: 5000,
        onTimeout,
      })
    );

    act(() => {
      vi.advanceTimersByTime(1000); // entra al warning
    });
    expect(result.current.showWarning).toBe(true);

    act(() => {
      result.current.dismissWarning();
    });
    expect(result.current.showWarning).toBe(false);
    expect(onTimeout).not.toHaveBeenCalled();

    // Tras dismiss, el ciclo reinicia: 1s más sin actividad debería volver al warning
    act(() => {
      vi.advanceTimersByTime(1000);
    });
    expect(result.current.showWarning).toBe(true);
  });

  it('secondsRemaining cuenta hacia abajo desde warningCountdownMs/1000', () => {
    const { result } = renderHook(() =>
      useIdleTimeout({
        idleMs: 1000,
        warningCountdownMs: 5000, // 5 seg
        onTimeout,
      })
    );

    act(() => {
      vi.advanceTimersByTime(1000); // entra al warning
    });
    expect(result.current.secondsRemaining).toBe(5);

    act(() => {
      vi.advanceTimersByTime(1000); // 1 seg después
    });
    expect(result.current.secondsRemaining).toBe(4);

    act(() => {
      vi.advanceTimersByTime(3000);
    });
    expect(result.current.secondsRemaining).toBe(1);
  });

  it('enabled=false → no se registran timers ni listeners', () => {
    renderHook(() =>
      useIdleTimeout({
        idleMs: 1000,
        warningCountdownMs: 1000,
        onTimeout,
        enabled: false,
      })
    );

    act(() => {
      vi.advanceTimersByTime(10000);
    });

    expect(onTimeout).not.toHaveBeenCalled();
  });

  it('Cleanup: desmontar el hook cancela todos los timers', () => {
    const { unmount } = renderHook(() =>
      useIdleTimeout({
        idleMs: 1000,
        warningCountdownMs: 1000,
        onTimeout,
      })
    );

    act(() => {
      vi.advanceTimersByTime(500);
    });
    unmount();

    // Aunque pasen 10s, onTimeout NO debe llamarse (timer cancelado)
    act(() => {
      vi.advanceTimersByTime(10000);
    });

    expect(onTimeout).not.toHaveBeenCalled();
  });
});
