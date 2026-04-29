'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { loginSchema } from '@/lib/utils/validators';
import { useAuthStore } from '@/stores/auth-store';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Eye, EyeOff, Lock, ShieldCheck, Truck } from 'lucide-react';
import { toast } from 'sonner';
import { sanitizeHTML } from '@/lib/utils/cn';

interface LoginFormData {
  identificador: string;
  password: string;
}

const MAX_ATTEMPTS = 5;
const LOCKOUT_TIME = 60;

export function LoginFormSplit() {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [attempts, setAttempts] = useState(0);
  const [lockoutRemaining, setLockoutRemaining] = useState(0);
  const router = useRouter();
  const setUser = useAuthStore((state) => state.setUser);
  const setLoading = useAuthStore((state) => state.setLoading);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    if (lockoutRemaining > 0) return;

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

      if (result.success && result.user) {
        const userData = result.user;
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
        const appUrl = process.env.NEXT_PUBLIC_APP_URL || 'https://app.fatrans.com.ve';
        const adminUrl = process.env.NEXT_PUBLIC_ADMIN_URL || 'https://admin.fatrans.com.ve';

        if (rol === 'SOCIO') {
          window.location.href = `${appUrl}/dashboard`;
        } else {
          window.location.href = `${adminUrl}/admin`;
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
    <div className="flex min-h-screen">
      {/* Left Panel - Branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-[#0F2744] relative overflow-hidden flex-col justify-between p-12">
        {/* Subtle pattern overlay */}
        <div className="absolute inset-0 opacity-5">
          <svg className="w-full h-full" viewBox="0 0 100 100" preserveAspectRatio="none">
            <defs>
              <pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse">
                <path d="M 10 0 L 0 0 0 10" fill="none" stroke="white" strokeWidth="0.5"/>
              </pattern>
            </defs>
            <rect width="100" height="100" fill="url(#grid)"/>
          </svg>
        </div>

        {/* Content */}
        <div className="relative z-10">
          {/* Logo */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-[#16A34A] flex items-center justify-center">
              <Truck className="w-5 h-5 text-white" />
            </div>
            <span className="text-2xl font-bold text-white tracking-tight">Fatans</span>
          </div>
        </div>

        <div className="relative z-10 space-y-8">
          <div className="space-y-4">
            <h1 className="text-4xl font-bold text-white leading-tight">
              Tu fondo de ahorro en la palma de tu mano
            </h1>
            <p className="text-lg text-slate-400 leading-relaxed">
              Acceso seguro a tu cuenta, movimientos y créditos. Diseñado para el sector transporte venezolano.
            </p>
          </div>

          <div className="flex items-center gap-4 pt-4">
            <div className="flex items-center gap-2 text-slate-400">
              <ShieldCheck className="w-5 h-5 text-[#16A34A]" />
              <span className="text-sm">Cifrado de grado bancario</span>
            </div>
          </div>
        </div>

        <div className="relative z-10">
          <p className="text-sm text-slate-500">
            © {new Date().getFullYear()} Fatrans. Plataforma financiera para el transporte.
          </p>
        </div>
      </div>

      {/* Right Panel - Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-white">
        <div className="w-full max-w-md space-y-8">
          {/* Mobile Logo */}
          <div className="lg:hidden flex items-center justify-center gap-3 mb-8">
            <div className="w-10 h-10 rounded-lg bg-[#16A34A] flex items-center justify-center">
              <Truck className="w-5 h-5 text-white" />
            </div>
            <span className="text-2xl font-bold text-[#0F2744] tracking-tight">Fatans</span>
          </div>

          <div className="text-center lg:text-left">
            <h2 className="text-2xl font-bold text-[#0F2744]">Iniciar Sesión</h2>
            <p className="mt-2 text-sm text-slate-500">
              Ingresa tus credenciales para acceder a tu cuenta
            </p>
          </div>

          {/* Lockout Warning */}
          {lockoutRemaining > 0 && (
            <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-center gap-3">
              <Lock className="w-5 h-5 text-red-600 flex-shrink-0" />
              <p className="text-sm text-red-700">
                Demasiados intentos. Espera {lockoutRemaining}s
              </p>
            </div>
          )}

          {/* Attempts Warning */}
          {remainingAttempts < MAX_ATTEMPTS && lockoutRemaining === 0 && (
            <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 flex items-center gap-3">
              <ShieldCheck className="w-5 h-5 text-amber-600 flex-shrink-0" />
              <p className="text-sm text-amber-700">
                Intentos restantes: {remainingAttempts}
              </p>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="identificador" className="text-sm font-medium text-slate-700">
                Cédula o Correo Electrónico
              </Label>
              <Input
                id="identificador"
                type="text"
                placeholder="Ej: V-12345678 o correo@ejemplo.com"
                autoComplete="username"
                disabled={isLoading || lockoutRemaining > 0}
                className="h-14 px-4 rounded-xl border-slate-300 bg-slate-50 text-base placeholder:text-slate-400 focus:bg-white focus:border-[#16A34A] focus:ring-2 focus:ring-[#16A34A]/20 transition-all"
                {...register('identificador')}
                aria-invalid={!!errors.identificador}
                aria-describedby={errors.identificador ? 'identificador-error' : undefined}
              />
              {errors.identificador && (
                <p id="identificador-error" role="alert" className="text-sm text-red-500">
                  {errors.identificador.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password" className="text-sm font-medium text-slate-700">
                  Contraseña
                </Label>
                <a
                  href="/recuperar-password"
                  className="text-sm text-[#16A34A] hover:underline font-medium"
                >
                  ¿Olvidaste tu contraseña?
                </a>
              </div>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Ingresa tu contraseña"
                  autoComplete="current-password"
                  disabled={isLoading || lockoutRemaining > 0}
                  className="h-14 px-4 pr-12 rounded-xl border-slate-300 bg-slate-50 text-base placeholder:text-slate-400 focus:bg-white focus:border-[#16A34A] focus:ring-2 focus:ring-[#16A34A]/20 transition-all"
                  {...register('password')}
                  aria-invalid={!!errors.password}
                  aria-describedby={errors.password ? 'password-error' : undefined}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showPassword ? (
                    <EyeOff className="w-5 h-5" />
                  ) : (
                    <Eye className="w-5 h-5" />
                  )}
                </button>
              </div>
              {errors.password && (
                <p id="password-error" role="alert" className="text-sm text-red-500">
                  {errors.password.message}
                </p>
              )}
            </div>

            <div className="flex items-center gap-2">
              <input
                id="remember-me"
                type="checkbox"
                className="w-4 h-4 rounded border-slate-300 text-[#16A34A] focus:ring-[#16A34A]"
              />
              <label htmlFor="remember-me" className="text-sm text-slate-600 cursor-pointer">
                Recordarme en este dispositivo
              </label>
            </div>

            <Button
              type="submit"
              className="w-full h-14 bg-[#16A34A] hover:bg-[#15803D] text-white font-semibold text-base rounded-xl transition-colors shadow-lg shadow-[#16A34A]/25"
              disabled={isLoading || lockoutRemaining > 0}
            >
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  Verificando credenciales...
                </>
              ) : lockoutRemaining > 0 ? (
                `Espera ${lockoutRemaining}s`
              ) : (
                'Iniciar Sesión'
              )}
            </Button>
          </form>

          <p className="text-center text-sm text-slate-500">
            ¿No tienes cuenta?{' '}
            <a href="/registro" className="text-[#16A34A] font-semibold hover:underline">
              Afíliate aquí
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}
