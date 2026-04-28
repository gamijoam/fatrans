'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Mail, ArrowLeft, CheckCircle, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';

export default function RecuperarPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!email.trim()) {
      setError('Ingrese su correo electrónico');
      setLoading(false);
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError('Ingrese un correo electrónico válido');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch('/api/auth/recuperar-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identificador: email }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || 'Error al procesar solicitud');
      }

      setSubmitted(true);
      toast.success('Correo enviado', 'Si el email existe en nuestro sistema, recibirás un enlace de recuperación');
    } catch (err) {
      console.error('Error:', err);
      toast.error('Error', err instanceof Error ? err.message : 'No se pudo procesar la solicitud');
    } finally {
      setLoading(false);
    }
  };

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
              <Mail className="h-6 w-6 text-green-600" />
            </div>
            <CardTitle className="text-2xl font-bold">Recuperar Contraseña</CardTitle>
            <CardDescription>
              Ingrese su correo electrónico para recibir un enlace de recuperación
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            {submitted ? (
              <div className="text-center space-y-4 py-8">
                <div className="mx-auto w-16 h-16 rounded-full bg-green-100 flex items-center justify-center">
                  <CheckCircle className="h-8 w-8 text-green-600" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">Correo enviado</h3>
                  <p className="text-sm text-gray-500 mt-1">
                    Si el correo <strong>{email}</strong> existe en nuestro sistema,
                    recibirás un enlace para restablecer tu contraseña.
                  </p>
                </div>
                <p className="text-xs text-gray-400">
                  El enlace expirará en 24 horas.
                </p>
                <Link href="/login">
                  <Button variant="outline" className="mt-4">
                    Volver al login
                  </Button>
                </Link>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Correo electrónico</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="correo@ejemplo.com"
                    value={email}
                    onChange={(e) => {
                      setEmail(e.target.value);
                      setError('');
                    }}
                    disabled={loading}
                    className={error ? 'border-red-500' : ''}
                  />
                  {error && (
                    <p className="text-sm text-red-500 flex items-center gap-1">
                      <AlertCircle className="h-4 w-4" />
                      {error}
                    </p>
                  )}
                </div>

                <Button
                  type="submit"
                  className="w-full bg-green-600 hover:bg-green-700"
                  disabled={loading}
                >
                  {loading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                  Enviar enlace de recuperación
                </Button>

                <div className="text-center text-sm text-gray-500">
                  <p>
                    ¿Recordaste tu contraseña?{' '}
                    <Link href="/login" className="text-green-600 hover:underline">
                      Iniciar sesión
                    </Link>
                  </p>
                </div>
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