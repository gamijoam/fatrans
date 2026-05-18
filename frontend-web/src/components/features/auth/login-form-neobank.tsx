'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter, useSearchParams } from 'next/navigation';
import { loginSchema } from '@/lib/utils/validators';
import { useAuthStore } from '@/stores/auth-store';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Eye, EyeOff, Lock, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import { sanitizeHTML } from '@/lib/utils/cn';
import { Logo } from '@/components/branding/logo';
import { LoadingLogo } from '@/components/branding/loading-logo';

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
  const searchParams = useSearchParams();
  const setUser = useAuthStore((state) => state.setUser);
  const setLoading = useAuthStore((state) => state.setLoading);

  // Issue #221: si llegamos al login por idle timeout, avisar al usuario
  useEffect(() => {
    const reason = searchParams?.get('reason');
    if (reason === 'inactivity') {
      toast.info('Sesión cerrada por inactividad', {
        description: 'Por tu seguridad, cerramos tu sesión tras varios minutos sin actividad.',
        duration: 6000,
      });
    }
  }, [searchParams]);

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
    // Minimo visual de la animacion de "Iniciando sesion..." - si el backend
    // responde en <1500ms el overlay del logo animado apenas se ve y el
    // usuario percibe el login como "no hizo nada". Forzamos un piso de
    // 1.5s para que el feedback sea claro (patron estandar de banca digital
    // que prioriza confianza sobre microsegundos de latencia percibida).
    const ANIM_MIN_MS = 1500;
    const animStart = Date.now();
    const waitMinDelay = async () => {
      const elapsed = Date.now() - animStart;
      if (elapsed < ANIM_MIN_MS) {
        await new Promise((r) => setTimeout(r, ANIM_MIN_MS - elapsed));
      }
    };

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
        const appUrl = process.env.NEXT_PUBLIC_APP_URL || '';
        const adminUrl = process.env.NEXT_PUBLIC_ADMIN_URL || '';

        // Esperar minimo 1.5s para que la animacion se vea (UX bancario)
        await waitMinDelay();

        if (rol === 'SOCIO') {
          window.location.href = `${appUrl}/dashboard`;
        } else {
          window.location.href = `${adminUrl}/admin`;
        }
      }
    } catch (error) {
      setLoading(false);
      // Tambien respetar el minimo en errores: si el backend rebota en 200ms
      // con "credenciales invalidas", queremos que la animacion se vea al
      // menos un segundo antes de mostrar el toast (sino el usuario duda si
      // siquiera intento loguearse).
      await waitMinDelay();
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

      {/* Overlay de carga: visible mientras isLoading=true.
          El usuario ve el logo de Fatrans "respirando" con un anillo
          verde girando — feedback inmediato de que la app está
          procesando, no congelada. */}
      {isLoading && <LoadingLogo variante="overlay" mensaje="Iniciando sesión..." />}

      {/* Container responsivo: 2 columnas en desktop (logo institucional
          a la izquierda + card del form a la derecha), stack vertical en
          mobile (logo dentro del card como antes). */}
      <div className="relative z-10 w-full max-w-5xl">
        <div className="grid lg:grid-cols-[1.1fr_1fr] gap-8 lg:gap-12 items-center">
          {/* === COLUMNA IZQUIERDA: Branding institucional (solo desktop) === */}
          <aside className="hidden lg:flex flex-col items-center text-center text-white px-4">
            <Logo size={220} soloImagen priority />
            <h1 className="mt-6 text-5xl font-bold tracking-tight">Fatrans</h1>
            <p className="mt-2 text-base uppercase tracking-[0.2em] text-blue-200/90">
              Asociación de Ahorro y Crédito
            </p>
            <p className="mt-8 text-sm text-white/80 max-w-md leading-relaxed">
              El respaldo financiero del sector transporte venezolano.
              Ahorro, crédito y servicios diseñados para socios del transporte.
            </p>
            <div className="mt-8 flex flex-wrap gap-2 justify-center">
              <TrustChip icon={Lock} label="Cifrado de grado bancario" />
              <TrustChip icon={ShieldCheck} label="Auditoría LOCDOFT" />
            </div>
            <p className="mt-10 text-xs text-white/40">
              RIF J-50516835-5 · © {new Date().getFullYear()} Fatrans
            </p>
          </aside>

          {/* === COLUMNA DERECHA: Card del formulario === */}
          <div className="w-full max-w-md mx-auto lg:mx-0">
        <div className="bg-white rounded-2xl shadow-2xl shadow-blue-950/50 overflow-hidden">
          {/* Card Header — logo solo visible en mobile (en desktop ya
              está en la columna izquierda). En desktop, el header queda
              más compacto y limpio. */}
          <div className="px-7 pt-6 pb-5 text-center border-b border-slate-100">
            <div className="flex justify-center mb-3 lg:hidden">
              <Logo size={72} priority />
            </div>
            <h2 className="text-lg lg:text-xl font-bold text-slate-900">
              Acceso Seguro al Fondo
            </h2>
            <p className="text-xs lg:text-sm text-slate-500 mt-1">
              Ingresa tus credenciales para continuar
            </p>
          </div>

          {/* Card Body — padding compactado para que el card no se infle */}
          <div className="px-7 py-6">
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
                  className="h-12 px-4 rounded-xl border-slate-300 bg-slate-50 text-base placeholder:text-slate-400 focus:bg-white focus:border-[#16A34A] focus:ring-2 focus:ring-[#16A34A]/20 transition-all"
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
                    className="h-12 px-4 pr-12 rounded-xl border-slate-300 bg-slate-50 text-base placeholder:text-slate-400 focus:bg-white focus:border-[#16A34A] focus:ring-2 focus:ring-[#16A34A]/20 transition-all"
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
                className="w-full h-12 bg-[#16A34A] hover:bg-[#15803D] text-white font-semibold text-base rounded-xl transition-all shadow-lg shadow-[#16A34A]/25 hover:shadow-[#16A34A]/40 disabled:opacity-50 disabled:cursor-not-allowed"
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

          {/* Card Footer - Trust Badges (siempre visible) */}
          <div className="px-7 py-4 bg-slate-50 border-t border-slate-100">
            <div className="flex flex-wrap items-center justify-center gap-x-1 gap-y-1 text-xs text-slate-400">
              <Lock className="w-3 h-3" />
              <span>Cifrado de grado bancario.</span>
              <span className="mx-1 text-slate-300">•</span>
              <span className="font-medium text-slate-500">v2.1.0</span>
            </div>
          </div>
        </div>

        {/* Copyright — solo visible en mobile (en desktop ya está en la
            columna izquierda con el RIF). */}
        <p className="mt-6 text-center text-xs text-white/40 lg:hidden">
          © {new Date().getFullYear()} Fatrans · RIF J-50516835-5
        </p>
          </div>
          {/* === FIN columna derecha === */}
        </div>
        {/* === FIN grid 2-cols === */}
      </div>
    </div>
  );
}

/**
 * Chip de confianza institucional (solo visible en desktop).
 * Muestra una característica de seguridad/cumplimiento con ícono +
 * label sobre fondo glassmorphism.
 */
function TrustChip({ icon: Icon, label }: { icon: React.ComponentType<{ className?: string }>; label: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-white/10 backdrop-blur-sm border border-white/15 px-3 py-1.5 text-xs font-medium text-white/90">
      <Icon className="w-3.5 h-3.5 text-emerald-400" />
      {label}
    </span>
  );
}
