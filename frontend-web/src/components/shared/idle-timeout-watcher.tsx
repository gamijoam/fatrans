'use client';

import { useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { useIdleTimeout } from '@/hooks/useIdleTimeout';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Clock } from 'lucide-react';

/**
 * Vigilante de inactividad para sesiones de socio (issue #221).
 *
 * Tras `idleMinutes` minutos sin actividad → modal con countdown de
 * `warningSeconds` segundos. Si no responde → logout + redirect a
 * `/login?reason=inactivity`.
 *
 * Banking standard: si dejas el teléfono sin bloqueo, cualquiera operaría
 * con tu cuenta. Auto-logout previene esto.
 *
 * Defaults sensatos para socio (10 min idle + 60s countdown). Admin
 * podría usar valores más estrictos vía props si se reusa.
 */
interface IdleTimeoutWatcherProps {
  /** Minutos de inactividad antes del warning. Default 10. */
  idleMinutes?: number;
  /** Segundos del countdown del warning. Default 60. */
  warningSeconds?: number;
}

export function IdleTimeoutWatcher({
  idleMinutes = 10,
  warningSeconds = 60,
}: IdleTimeoutWatcherProps) {
  const router = useRouter();
  const logout = useAuthStore((state) => state.logout);
  const user = useAuthStore((state) => state.user);

  const handleTimeout = useCallback(async () => {
    try {
      await logout();
    } catch {
      // logout puede fallar si el token ya expiró; igual continuamos
    }
    router.push('/login?reason=inactivity');
  }, [logout, router]);

  const { showWarning, secondsRemaining, dismissWarning } = useIdleTimeout({
    idleMs: idleMinutes * 60 * 1000,
    warningCountdownMs: warningSeconds * 1000,
    onTimeout: handleTimeout,
    enabled: !!user, // solo activo si hay sesión
  });

  return (
    <AlertDialog open={showWarning} onOpenChange={(open) => !open && dismissWarning()}>
      <AlertDialogContent
        data-testid="idle-timeout-warning"
        className="border-amber-200"
      >
        <AlertDialogHeader>
          <div className="flex items-center gap-3 mb-1">
            <div className="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center">
              <Clock className="w-5 h-5 text-amber-600" />
            </div>
            <AlertDialogTitle className="text-[#0F2744]">
              ¿Sigues conectado?
            </AlertDialogTitle>
          </div>
          <AlertDialogDescription>
            Por tu seguridad, cerraremos tu sesión automáticamente en{' '}
            <span className="font-bold text-amber-700 tabular-nums">
              {secondsRemaining}s
            </span>{' '}
            debido a inactividad. Presiona el botón si deseas continuar.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogAction
            onClick={dismissWarning}
            className="bg-[#16A34A] hover:bg-[#15803D]"
          >
            Seguir conectado
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
