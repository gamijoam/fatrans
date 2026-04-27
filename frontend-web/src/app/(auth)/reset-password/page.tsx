'use client';

import { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Lock, ArrowLeft, CheckCircle, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';

export default function ResetPasswordPage() {
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  const validatePassword = (pwd: string): string | null => {
    if (pwd.length < 8) {
      return 'La contraseña debe tener al menos 8 caracteres';
    }
    if (!/[A-Z]/.test(pwd)) {
      return 'La contraseña debe tener al menos una mayúscula';
    }
    if (!/[a-z]/.test(pwd)) {
      return 'La contraseña debe tener al menos una minúscula';
    }
    if (!/[0-9]/.test(pwd)) {
      return 'La contraseña debe tener al menos un número';
    }
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setPasswordError('');

    if (!token) {
      setError('Token de recuperación inválido o expirado');
      return;
    }

    const pwdError = validatePassword(password);
    if (pwdError) {
      setPasswordError(pwdError);
      return;
    }

    if (password !== confirmPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }

    setLoading(true);

    try {
      const res = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, nuevaPassword: password }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || 'Error al restablecer contraseña');
      }

      setSubmitted(true);
      toast.success('Contraseña actualizada', 'Ahora puedes iniciar sesión con tu nueva contraseña');
    } catch (err) {
      console.error('Error:', err);
      toast.error('Error', err instanceof Error ? err.message : 'No se pudo restablecer la contraseña');
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center p-4 bg-gradient-to-br from-white via-green-50 to-blue-50">
        <div className="w-full max-w-md">
          <Card className="shadow-lg border-0">
            <CardContent className="text-center py-12">
              <div className="mx-auto w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mb-4">
                <AlertCircle className="h-8 w-8 text-red-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Token inválido</h3>
              <p className="text-sm text-gray-500 mb-6">
                El enlace de recuperación es inválido o ha expirado.
              </p>
              <Link href="/recuperar-password">
                <Button variant="outline">
                  Solicitar nuevo enlace
                </Button>
              </Link>
            </CardContent>
          </Card>
        </div>
      </main>
    );
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-4 bg-gradient-to-br from-white via-green-50 to-blue-50">
      <div className="w-full max-w-md">
        <Link
          href="/login"
          className="flex items-center gap-2 text-sm text-gray-600 hover:text-green-600 mb-6 transition-colors"
        >
          <ArrowLeft className="h-4 w-4" />
          Volver al login
        </Link>

        <Card className="shadow-lg border-0">
          <CardHeader className="space-y-1 text-center pb-2">
            <div className="mx-auto w-12 h-12 rounded-full bg-green-100 flex items-center justify-center mb-4">
              <Lock className="h-6 w-6 text-green-600" />
            </div>
            <CardTitle className="text-2xl font-bold">Nueva Contraseña</CardTitle>
            <CardDescription>
              Ingrese su nueva contraseña
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            {submitted ? (
              <div className="text-center space-y-4 py-8">
                <div className="mx-auto w-16 h-16 rounded-full bg-green-100 flex items-center justify-center">
                  <CheckCircle className="h-8 w-8 text-green-600" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">¡Contraseña actualizada!</h3>
                  <p className="text-sm text-gray-500 mt-1">
                    Su contraseña ha sido actualizada exitosamente.
                  </p>
                </div>
                <Link href="/login">
                  <Button className="mt-4 bg-green-600 hover:bg-green-700">
                    Ir al login
                  </Button>
                </Link>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="password">Nueva contraseña</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="********"
                    value={password}
                    onChange={(e) => {
                      setPassword(e.target.value);
                      setPasswordError('');
                    }}
                    disabled={loading}
                    className={passwordError ? 'border-red-500' : ''}
                  />
                  {passwordError && (
                    <p className="text-sm text-red-500 flex items-center gap-1">
                      <AlertCircle className="h-4 w-4" />
                      {passwordError}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">Confirmar contraseña</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    placeholder="********"
                    value={confirmPassword}
                    onChange={(e) => {
                      setConfirmPassword(e.target.value);
                      setError('');
                    }}
                    disabled={loading}
                    className={error ? 'border-red-500' : ''}
                  />
                </div>

                {error && (
                  <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-md">
                    <AlertCircle className="h-5 w-5 text-red-600 flex-shrink-0" />
                    <p className="text-sm text-red-600">{error}</p>
                  </div>
                )}

                <div className="bg-gray-50 p-3 rounded-md">
                  <p className="text-xs text-gray-500 mb-2">La contraseña debe:</p>
                  <ul className="text-xs text-gray-500 space-y-1">
                    <li className={password.length >= 8 ? 'text-green-600' : ''}>
                      • Al menos 8 caracteres
                    </li>
                    <li className={/[A-Z]/.test(password) ? 'text-green-600' : ''}>
                      • Una mayúscula
                    </li>
                    <li className={/[a-z]/.test(password) ? 'text-green-600' : ''}>
                      • Una minúscula
                    </li>
                    <li className={/[0-9]/.test(password) ? 'text-green-600' : ''}>
                      • Un número
                    </li>
                  </ul>
                </div>

                <Button
                  type="submit"
                  className="w-full bg-green-600 hover:bg-green-700"
                  disabled={loading}
                >
                  {loading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                  Actualizar contraseña
                </Button>
              </form>
            )}
          </CardContent>
        </Card>

        <p className="text-xs text-center text-gray-400 mt-6">
          FATRANS - Fondo de Ahorro | Todos los derechos reservados
        </p>
      </div>
    </main>
  );
}