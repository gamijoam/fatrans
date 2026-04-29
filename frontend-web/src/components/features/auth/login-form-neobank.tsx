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
import { Loader2, Eye, EyeOff, Lock, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import { sanitizeHTML } from '@/lib/utils/cn';

interface LoginFormData {
  identificador: string;
  password: string;
}

const MAX_ATTEMPTS = 5;
const LOCKOUT_TIME = 60;

function IconLock({ className = "" }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
    </svg>
  );
}

export function LoginFormNeobank() {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [attempts, setAttempts] = useState(0);
  const [lockoutRemaining, setLockoutRemaining] = useState(0);
  const [rememberMe, setRememberMe] = useState(false);
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
    <div className="min-h-screen relative flex items-center justify-center p-4">
      {/* Background with overlay */}
      <div className="absolute inset-0 z-0">
        <div className="absolute inset-0 bg-[#0a1628]" />
        <div
          className="absolute inset-0 opacity-10"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.15'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
          }}
        />
        <div className="absolute inset-0 bg-gradient-to-br from-blue-950/90 via-blue-950/85 to-slate-900/90" />
      </div>

      {/* Login Card */}
      <div className="relative z-10 w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-2xl shadow-blue-950/50 overflow-hidden">
          {/* Card Header */}
          <div className="px-8 pt-10 pb-8 text-center border-b border-slate-100">
            <h1 className="text-2xl font-bold text-slate-900 mb-2">
              Acceso Seguro al Fondo
            </h1>
            <p className="text-sm text-slate-500">
              Ingresa tus credenciales para continuar
            </p>
          </div>

          {/* Card Body */}
          <div className="px-8 py-8">
            {/* Lockout Warning */}
            {lockoutRemaining > 0 && (
              <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 flex items-center gap-3">
                <Lock className="w-5 h-5 text-red-600 flex-shrink-0" />
                <p className="text-sm text-red-700">
                  Demasiados intentos. Espera {lockoutRemaining}s
                </p>
              </div>
            )}

            {/* Attempts Warning */}
            {remainingAttempts < MAX_ATTEMPTS && lockoutRemaining === 0 && (
              <div className="mb-6 p-4 rounded-xl bg-amber-50 border border-amber-200 flex items-center gap-3">
                <ShieldCheck className="w-5 h-5 text-amber-600 flex-shrink-0" />
                <p className="text-sm text-amber-700">
                  Intentos restantes: {remainingAttempts}
                </p>
              </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Cedula/Email Input */}
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <IconLock className="w-4 h-4 text-[#16A34A]" />
                  <Label
                    htmlFor="identificador"
                    className="text-sm font-semibold text-slate-700"
                  >
                    Cédula o Correo Electrónico
                  </Label>
                </div>
                <Input
                  id="identificador"
                  type="text"
                  placeholder="Ej: V-12345678"
                  autoComplete="username"
                  disabled={isLoading || lockoutRemaining > 0}
                  className="h-14 px-4 rounded-xl border-slate-300 bg-slate-50 text-base placeholder:text-slate-400 focus:bg-white focus:border-[#16A34A] focus:ring-2 focus:ring-[#16A34A]/20 transition-all"
                  {...register('identificador')}
                  aria-invalid={!!errors.identificador}
                  aria-describedby={errors.identificador ? 'identificador-error' : undefined}
                />
                {errors.identificador && (
                  <p
                    id="identificador-error"
                    role="alert"
                    className="text-sm text-red-500"
                  >
                    {errors.identificador.message}
                  </p>
                )}
              </div>

              {/* Password Input */}
              <div className="space-y-2">
                <Label
                  htmlFor="password"
                  className="text-sm font-semibold text-slate-700"
                >
                  Contraseña
                </Label>
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
                  <p
                    id="password-error"
                    role="alert"
                    className="text-sm text-red-500"
                  >
                    {errors.password.message}
                  </p>
                )}
              </div>

              {/* Options Row */}
              <div className="flex items-center justify-between">
                <label className="flex items-center gap-2 cursor-pointer group">
                  <div className="relative">
                    <input
                      type="checkbox"
                      checked={rememberMe}
                      onChange={(e) => setRememberMe(e.target.checked)}
                      className="sr-only peer"
                    />
                    <div className="w-5 h-5 rounded border border-slate-300 bg-slate-50 peer-checked:bg-[#16A34A] peer-checked:border-[#16A34A] peer-focus:ring-2 peer-focus:ring-[#16A34A]/20 transition-all peer-checked:peer-disabled:bg-slate-400" />
                    <svg
                      className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-3 h-3 text-white opacity-0 peer-checked:opacity-100 transition-opacity"
                      viewBox="0 0 12 12"
                      fill="none"
                    >
                      <path
                        d="M2 6l3 3 5-5"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                  </div>
                  <span className="text-sm text-slate-600 group-hover:text-slate-900 transition-colors">
                    Recordarme
                  </span>
                </label>

                <a
                  href="/recuperar-password"
                  className="text-sm font-medium text-[#16A34A] hover:text-[#15803D] hover:underline transition-colors"
                >
                  ¿Olvidaste tu contraseña?
                </a>
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                className="w-full h-14 bg-[#16A34A] hover:bg-[#15803D] text-white font-semibold text-base rounded-xl transition-all shadow-lg shadow-[#16A34A]/25 hover:shadow-[#16A34A]/40 disabled:opacity-50 disabled:cursor-not-allowed"
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

            {/* Register Link */}
            <p className="mt-6 text-center text-sm text-slate-500">
              ¿No tienes cuenta?{' '}
              <a
                href="/registro"
                className="font-semibold text-[#16A34A] hover:underline"
              >
                Afíliate aquí
              </a>
            </p>
          </div>

          {/* Card Footer - Trust Badges */}
          <div className="px-8 py-5 bg-slate-50 border-t border-slate-100">
            <div className="flex items-center justify-center gap-1 text-xs text-slate-400">
              <Lock className="w-3 h-3" />
              <span>Cifrado de grado bancario.</span>
              <span className="mx-1.5 text-slate-300">•</span>
              <span>Auditoría en tiempo real.</span>
              <span className="mx-1.5 text-slate-300">•</span>
              <span className="font-medium text-slate-500">v2.1.0</span>
            </div>
          </div>
        </div>

        {/* Copyright */}
        <p className="mt-6 text-center text-xs text-white/40">
          © {new Date().getFullYear()} Fatrans. Todos los derechos reservados.
        </p>
      </div>
    </div>
  );
}
