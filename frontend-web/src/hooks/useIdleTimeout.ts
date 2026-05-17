'use client';

import { useEffect, useRef, useState, useCallback } from 'react';

/**
 * Hook puro de idle timeout para sesiones de banca (issue #221).
 *
 * Reproduce el patrón estándar de apps bancarias:
 *  - Tras `idleMs` de inactividad → muestra warning con countdown.
 *  - Si tras `warningCountdownMs` el usuario sigue sin responder → `onTimeout()`.
 *  - Cualquier actividad detectada en `events` resetea el contador a 0.
 *
 * Diseñado puro: sin importar react-idle-timer ni librerías externas. Usa
 * únicamente `setTimeout` y `addEventListener`. ~50 líneas testeables.
 *
 * @example
 *   const { showWarning, secondsRemaining, dismissWarning } = useIdleTimeout({
 *     idleMs: 9 * 60 * 1000,        // 9 min inactivo → warning
 *     warningCountdownMs: 60 * 1000, // 60s para responder
 *     onTimeout: () => logout(),
 *     enabled: !!user,
 *   });
 */

export interface UseIdleTimeoutOptions {
  /** Tiempo de inactividad antes de mostrar el warning (ms). */
  idleMs: number;
  /** Tiempo del countdown del warning antes del timeout (ms). */
  warningCountdownMs: number;
  /** Callback al expirar el countdown (típicamente: logout + redirect). */
  onTimeout: () => void;
  /** Si el watcher está activo. Útil para deshabilitar mientras no hay usuario. */
  enabled?: boolean;
  /** Eventos del DOM que resetean el timer (default: estándar de actividad). */
  events?: ReadonlyArray<keyof DocumentEventMap>;
}

export interface UseIdleTimeoutReturn {
  /** True cuando se está mostrando el warning. */
  showWarning: boolean;
  /** Segundos restantes en el countdown (0 cuando expira). */
  secondsRemaining: number;
  /** Cerrar el warning y reiniciar el idle timer (botón "Seguir conectado"). */
  dismissWarning: () => void;
}

const DEFAULT_EVENTS: ReadonlyArray<keyof DocumentEventMap> = [
  'mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart', 'click',
];

export function useIdleTimeout({
  idleMs,
  warningCountdownMs,
  onTimeout,
  enabled = true,
  events = DEFAULT_EVENTS,
}: UseIdleTimeoutOptions): UseIdleTimeoutReturn {
  const [showWarning, setShowWarning] = useState(false);
  const [secondsRemaining, setSecondsRemaining] = useState(
    Math.ceil(warningCountdownMs / 1000)
  );

  // Refs para los timers (no triggean re-renders)
  const idleTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const countdownTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const timeoutTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Refs para callbacks/valores volátiles: las funciones registradas en
  // setTimeout/listeners NO deben recrearse cuando cambia el state, sino
  // el useEffect de setup re-ejecuta y cancela los timers recién creados
  // (bug detectado en TDD).
  const onTimeoutRef = useRef(onTimeout);
  const showWarningRef = useRef(showWarning);
  const idleMsRef = useRef(idleMs);
  const warningCountdownMsRef = useRef(warningCountdownMs);

  useEffect(() => { onTimeoutRef.current = onTimeout; }, [onTimeout]);
  useEffect(() => { showWarningRef.current = showWarning; }, [showWarning]);
  useEffect(() => { idleMsRef.current = idleMs; }, [idleMs]);
  useEffect(() => { warningCountdownMsRef.current = warningCountdownMs; }, [warningCountdownMs]);

  const clearAllTimers = useCallback(() => {
    if (idleTimerRef.current) clearTimeout(idleTimerRef.current);
    if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);
    if (timeoutTimerRef.current) clearTimeout(timeoutTimerRef.current);
    idleTimerRef.current = null;
    countdownTimerRef.current = null;
    timeoutTimerRef.current = null;
  }, []);

  const dismissWarning = useCallback(() => {
    clearAllTimers();
    setShowWarning(false);
    setSecondsRemaining(Math.ceil(warningCountdownMsRef.current / 1000));
    // Reiniciar el ciclo idle
    idleTimerRef.current = setTimeout(() => startCountdownRef.current(),
                                       idleMsRef.current);
  }, [clearAllTimers]);

  // Ref para startCountdown (porque dismissWarning lo llama por ref para
  // evitar dep cycle: startCountdown necesita startCountdown vía setTimeout).
  const startCountdownRef = useRef<() => void>(() => {});

  // useEffect de setup: SIN deps que cambien con el state. Solo deps que
  // razonablemente requieren re-registrar listeners (enabled, events array).
  useEffect(() => {
    if (!enabled || typeof document === 'undefined') return;

    const startCountdown = () => {
      setShowWarning(true);
      const totalSeconds = Math.ceil(warningCountdownMsRef.current / 1000);
      setSecondsRemaining(totalSeconds);

      // Tick cada segundo para refrescar el display
      countdownTimerRef.current = setInterval(() => {
        setSecondsRemaining((prev) => (prev > 0 ? prev - 1 : 0));
      }, 1000);

      // Timeout duro que dispara onTimeout al expirar
      timeoutTimerRef.current = setTimeout(() => {
        if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);
        countdownTimerRef.current = null;
        timeoutTimerRef.current = null;
        setShowWarning(false);
        onTimeoutRef.current();
      }, warningCountdownMsRef.current);
    };

    // Exponer por ref para que dismissWarning pueda llamarlo
    startCountdownRef.current = startCountdown;

    const handleActivity = () => {
      // Si ya estamos mostrando warning, una actividad NO debe ocultarlo
      // automáticamente — el usuario debe presionar "Seguir conectado"
      // explícitamente. Esto previene que un mousemove accidental mantenga
      // viva una sesión que el usuario abandonó.
      if (showWarningRef.current) return;
      // Reset idle timer
      if (idleTimerRef.current) clearTimeout(idleTimerRef.current);
      idleTimerRef.current = setTimeout(startCountdown, idleMsRef.current);
    };

    events.forEach((ev) => document.addEventListener(ev, handleActivity, { passive: true }));
    idleTimerRef.current = setTimeout(startCountdown, idleMsRef.current);

    return () => {
      events.forEach((ev) => document.removeEventListener(ev, handleActivity));
      clearAllTimers();
    };
    // CRÍTICO: NO incluir state ni callbacks aquí, solo deps estables.
    // El comportamiento dinámico se logra vía refs.
  }, [enabled, events, clearAllTimers]);

  return { showWarning, secondsRemaining, dismissWarning };
}
