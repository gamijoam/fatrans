'use client';

import { useState, useCallback } from 'react';

export type VerificationStep = 'idle' | 'password' | 'code_sent' | 'verifying' | 'success' | 'error';

interface UseVerificationOptions {
  onSuccess: (token: string) => void;
  onError?: (error: string) => void;
}

interface UseVerificationReturn {
  step: VerificationStep;
  setStep: (step: VerificationStep) => void;
  isLoading: boolean;
  error: string | null;
  setError: (error: string | null) => void;
  token: string | null;
  verifyPassword: (password: string) => Promise<boolean>;
  sendCode: (tipo: 'EMAIL' | 'SMS', valor: string) => Promise<string>;
  confirmCode: (codigo: string) => Promise<boolean>;
  reset: () => void;
}

export function useVerification({ onSuccess, onError }: UseVerificationOptions): UseVerificationReturn {
  const [step, setStep] = useState<VerificationStep>('idle');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [codigoToken, setCodigoToken] = useState<string | null>(null);

  const verifyPassword = useCallback(async (password: string): Promise<boolean> => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await fetch('/api/v1/perfil/verificar-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ password }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Contraseña incorrecta');
      }

      setToken(data.tokenVerificacion);
      setStep('success');
      onSuccess(data.tokenVerificacion);
      return true;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error al verificar contraseña';
      setError(message);
      setStep('error');
      onError?.(message);
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [onSuccess, onError]);

  const sendCode = useCallback(async (tipo: 'EMAIL' | 'SMS', valor: string): Promise<string> => {
    setIsLoading(true);
    setError(null);
    setStep('password');

    try {
      const response = await fetch('/api/v1/perfil/enviar-codigo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ tipo, valor }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Error al enviar código');
      }

      setCodigoToken(data.token);
      setStep('code_sent');
      return data.token;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error al enviar código';
      setError(message);
      setStep('error');
      onError?.(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [onError]);

  const confirmCode = useCallback(async (codigo: string): Promise<boolean> => {
    if (!codigoToken) {
      setError('No hay código pendiente');
      return false;
    }

    setIsLoading(true);
    setError(null);
    setStep('verifying');

    try {
      const response = await fetch('/api/v1/perfil/confirmar-codigo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ tipo: 'SMS', token: codigoToken, codigo }),
      });

      const data = await response.json();

      if (!response.ok || !data.valido) {
        throw new Error(data.message || 'Código inválido');
      }

      setToken(data.tokenVerificacion);
      setStep('success');
      onSuccess(data.tokenVerificacion);
      return true;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error al confirmar código';
      setError(message);
      setStep('error');
      onError?.(message);
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [codigoToken, onSuccess, onError]);

  const reset = useCallback(() => {
    setStep('idle');
    setIsLoading(false);
    setError(null);
    setToken(null);
    setCodigoToken(null);
  }, []);

  return {
    step,
    setStep,
    isLoading,
    error,
    setError,
    token,
    verifyPassword,
    sendCode,
    confirmCode,
    reset,
  };
}