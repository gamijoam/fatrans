'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { loginSchema } from '@/lib/utils/validators';
import { useAuthStore } from '@/stores/auth-store';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Loader2, Eye, EyeOff, AlertCircle, Lock, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import { sanitizeHTML } from '@/lib/utils/cn';

interface LoginFormData {
  identificador: string;
  password: string;
}

const MAX_ATTEMPTS = 5;
const LOCKOUT_TIME = 60;

export function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [attempts, setAttempts] = useState(0);
  const [lockoutRemaining, setLockoutRemaining] = useState(0);
  const router = useRouter();
  const setUser = useAuthStore((state) => state.setUser);
  const setLoading = useAuthStore((state) => state.setLoading);

  useEffect(() => {
    if (lockoutRemaining > 0) {
      const interval = setInterval(() => {
        setLockoutRemaining((prev) => {
          if (prev <= 1) {
            clearInterval(interval);
            setAttempts(0);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [lockoutRemaining]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    if (lockoutRemaining > 0) {
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
        credentials: 'include',
      });

      const result = await response.json();

      if (!response.ok) {
        const newAttempts = attempts + 1;
        setAttempts(newAttempts);

        if (newAttempts >= MAX_ATTEMPTS) {
          setLockoutRemaining(LOCKOUT_TIME);
        }

        throw new Error(result.message || 'Credenciales inválidas');
      }

      const userResponse = await fetch('/api/auth/me', {
        credentials: 'include',
      });

      if (userResponse.ok) {
        const userData = await userResponse.json();
        setUser({
          id: userData.id,
          nombreUsuario: userData.nombreUsuario,
          correoElectronico: userData.correoElectronico,
          nombreCompleto: userData.nombreCompleto,
          rol: userData.rol,
          socioId: userData.socioId,
          debeCambiarPassword: userData.debeCambiarPassword ?? false,
        });
        setLoading(false);

        const rol = userData.rol;
        if (rol === 'SOCIO') {
          router.push('/dashboard');
        } else if (rol === 'ADMIN' || rol === 'SUPER_ADMIN' || rol === 'CAJERO' || rol === 'ANALISTA_KYC') {
          router.push('/admin');
        } else {
          router.push('/dashboard');
        }
      }

    } catch (error) {
      setLoading(false);
      console.error('Login error:', error);
      const message = error instanceof Error ? sanitizeHTML(error.message) : 'Error de conexión';
      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  const remainingAttempts = MAX_ATTEMPTS - attempts;

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="space-y-1">
        <div className="flex justify-center mb-2">
          <div className="p-3 rounded-full bg-green-100">
            <ShieldCheck className="h-8 w-8 text-green-600" />
          </div>
        </div>
        <CardTitle className="text-2xl text-center font-bold text-gray-900">
          Iniciar Sesión
        </CardTitle>
        <CardDescription className="text-center">
          Ingresa tus credenciales para acceder a tu cuenta
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {remainingAttempts < MAX_ATTEMPTS && lockoutRemaining === 0 && (
          <div className="mb-4 p-3 rounded-lg bg-yellow-50 border border-yellow-200 flex items-center gap-2">
            <AlertCircle className="h-4 w-4 text-yellow-600 flex-shrink-0" />
            <p className="text-sm text-yellow-700">
              Intentos restantes: {remainingAttempts}
            </p>
          </div>
        )}

        {lockoutRemaining > 0 && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 flex items-center gap-2">
            <Lock className="h-4 w-4 text-red-600 flex-shrink-0" />
            <p className="text-sm text-red-700">
              Demasiados intentos. Espera {lockoutRemaining}s
            </p>
          </div>
        )}

        <Separator />

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="identificador">Usuario, Email o Cédula</Label>
            <Input
              id="identificador"
              type="text"
              placeholder="Ingresa tu usuario, email o cédula"
              autoComplete="username"
              disabled={isLoading || lockoutRemaining > 0}
              {...register('identificador')}
              aria-invalid={!!errors.identificador}
              aria-describedby={errors.identificador ? 'identificador-error' : undefined}
            />
            {errors.identificador && (
              <p id="identificador-error" role="alert" className="text-xs text-red-500">
                {errors.identificador.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Contraseña</Label>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Ingresa tu contraseña"
                autoComplete="current-password"
                disabled={isLoading || lockoutRemaining > 0}
                {...register('password')}
                aria-invalid={!!errors.password}
                aria-describedby={errors.password ? 'password-error' : undefined}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                aria-pressed={showPassword}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 disabled:opacity-50"
                disabled={isLoading}
              >
                {showPassword ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </button>
            </div>
            {errors.password && (
              <p id="password-error" role="alert" className="text-xs text-red-500">
                {errors.password.message}
              </p>
            )}
          </div>

          <div className="flex items-center justify-between text-sm">
            <label htmlFor="remember-me" className="flex items-center gap-2 cursor-pointer">
              <input
                id="remember-me"
                type="checkbox"
                className="rounded border-gray-300 text-green-600 focus:ring-green-500"
              />
              <span>Recordarme</span>
            </label>
            <a
              href="/recuperar-password"
              className="text-green-600 hover:underline"
            >
              ¿Olvidaste tu contraseña?
            </a>
          </div>

          <Button
            type="submit"
            className="w-full bg-green-600 hover:bg-green-700"
            disabled={isLoading || lockoutRemaining > 0}
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Ingresando...
              </>
            ) : lockoutRemaining > 0 ? (
              `Espera ${lockoutRemaining}s`
            ) : (
              'Iniciar Sesión'
            )}
          </Button>
        </form>

        <div className="mt-6 text-center text-sm text-gray-500">
          ¿No tienes cuenta?{' '}
          <a href="/registro" className="text-green-600 hover:underline">
            Regístrate aquí
          </a>
        </div>
      </CardContent>
    </Card>
  );
}
