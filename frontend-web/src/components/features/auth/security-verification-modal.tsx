'use client';

import { useState } from 'react';
import { useVerification } from '@/hooks/use-verification';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AlertCircle, CheckCircle, Loader2, Shield, Mail, Lock, MessageSquare } from 'lucide-react';
import { toast } from 'sonner';

interface SecurityVerificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onVerified: (token: string) => void;
  verificationType?: 'password' | 'code';
  targetValue?: string;
}

export function SecurityVerificationModal({
  isOpen,
  onClose,
  onVerified,
  verificationType = 'password',
  targetValue,
}: SecurityVerificationModalProps) {
  const [password, setPassword] = useState('');
  const [codigo, setCodigo] = useState('');

  const { step, isLoading, error, verifyPassword, sendCode, confirmCode, reset } = useVerification({
    onSuccess: (token) => {
      toast.success('Verificación exitosa');
      onVerified(token);
    },
    onError: (error) => {
      toast.error(error);
    },
  });

  if (!isOpen) return null;

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await verifyPassword(password);
  };

  const handleCodeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (targetValue) {
      const token = await sendCode('SMS', targetValue);
      await confirmCode(codigo);
    }
  };

  const handleClose = () => {
    reset();
    setPassword('');
    setCodigo('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 bg-blue-100 rounded-full">
            <Shield className="h-6 w-6 text-blue-600" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-gray-900">Verificación de Seguridad</h2>
            <p className="text-sm text-gray-500">Para continuar, verifica tu identidad</p>
          </div>
        </div>

        {step === 'idle' || step === 'password' ? (
          <form onSubmit={handlePasswordSubmit} className="space-y-4">
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-start gap-2">
              <Lock className="h-4 w-4 text-amber-600 mt-0.5 flex-shrink-0" />
              <p className="text-sm text-amber-800">
                Ingresa tu contraseña actual para continuar con el cambio
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Contraseña Actual</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Ingresa tu contraseña"
                disabled={isLoading}
                autoComplete="current-password"
              />
            </div>

            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2">
                <AlertCircle className="h-4 w-4 text-red-600 flex-shrink-0" />
                <p className="text-sm text-red-700">{error}</p>
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <Button type="button" variant="outline" onClick={handleClose} className="flex-1">
                Cancelar
              </Button>
              <Button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700" disabled={isLoading || !password}>
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Verificando...
                  </>
                ) : (
                  'Verificar'
                )}
              </Button>
            </div>
          </form>
        ) : step === 'code_sent' ? (
          <form onSubmit={handleCodeSubmit} className="space-y-4">
            <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg flex items-start gap-2">
              <MessageSquare className="h-4 w-4 text-blue-600 mt-0.5 flex-shrink-0" />
              <p className="text-sm text-blue-800">
                Hemos enviado un código de verificación a tu teléfono
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="codigo">Código de Verificación</Label>
              <Input
                id="codigo"
                type="text"
                value={codigo}
                onChange={(e) => setCodigo(e.target.value)}
                placeholder="Ingresa el código de 6 dígitos"
                disabled={isLoading}
                maxLength={6}
                className="text-center text-xl tracking-widest"
              />
            </div>

            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2">
                <AlertCircle className="h-4 w-4 text-red-600 flex-shrink-0" />
                <p className="text-sm text-red-700">{error}</p>
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <Button type="button" variant="outline" onClick={() => setStep('password')} className="flex-1">
                Volver
              </Button>
              <Button type="submit" className="flex-1 bg-green-600 hover:bg-green-700" disabled={isLoading || codigo.length !== 6}>
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Confirmando...
                  </>
                ) : (
                  'Confirmar'
                )}
              </Button>
            </div>
          </form>
        ) : step === 'success' ? (
          <div className="text-center py-6">
            <div className="flex justify-center mb-4">
              <div className="p-3 bg-green-100 rounded-full">
                <CheckCircle className="h-8 w-8 text-green-600" />
              </div>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Verificación Exitosa</h3>
            <p className="text-sm text-gray-500 mb-4">Tu identidad ha sido verificada correctamente</p>
            <Button onClick={handleClose} className="w-full bg-green-600 hover:bg-green-700">
              Continuar
            </Button>
          </div>
        ) : step === 'error' ? (
          <div className="text-center py-6">
            <div className="flex justify-center mb-4">
              <div className="p-3 bg-red-100 rounded-full">
                <AlertCircle className="h-8 w-8 text-red-600" />
              </div>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Error de Verificación</h3>
            <p className="text-sm text-gray-500 mb-4">{error}</p>
            <div className="flex gap-3">
              <Button variant="outline" onClick={handleClose} className="flex-1">
                Cancelar
              </Button>
              <Button onClick={() => { setError(null); setStep('password'); }} className="flex-1">
                Intentar de Nuevo
              </Button>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}