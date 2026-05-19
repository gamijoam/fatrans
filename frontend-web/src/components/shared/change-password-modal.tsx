'use client';

import { useState, useEffect, useCallback } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Loader2, Lock, Eye, EyeOff } from 'lucide-react';
import { toast } from 'sonner';

interface ChangePasswordModalProps {
  open: boolean;
  onClose?: () => void;
}

const SESSION_TIMEOUT_MS = 30 * 60 * 1000;

export function ChangePasswordModal({ open }: ChangePasswordModalProps) {
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);
  const logout = useAuthStore((state) => state.logout);

  const [passwordActual, setPasswordActual] = useState('');
  const [nuevoPassword, setNuevoPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [timeRemaining, setTimeRemaining] = useState(SESSION_TIMEOUT_MS);

  const handleTimeout = useCallback(async () => {
    // Issue #219: reemplaza `alert()` nativo por toast.
    toast.error('Sesión expirada', {
      description: 'Por seguridad, debes cambiar tu contraseña al iniciar sesión nuevamente.',
      duration: 5000,
    });
    await logout();
    window.location.href = '/login';
  }, [logout]);

  useEffect(() => {
    if (!user?.debeCambiarPassword) return;

    const interval = setInterval(() => {
      setTimeRemaining((prev) => {
        if (prev <= 1000) {
          clearInterval(interval);
          handleTimeout();
          return 0;
        }
        return prev - 1000;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [user?.debeCambiarPassword, handleTimeout]);

  const formatTime = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (nuevoPassword !== confirmPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }

    if (nuevoPassword.length < 8) {
      setError('La contraseña debe tener al menos 8 caracteres');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch('/api/auth/cambiar-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          passwordActual,
          nuevoPassword,
        }),
      });

      let errorMessage = 'Error al cambiar contraseña';

      if (!response.ok) {
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
          try {
            const data = await response.json();
            errorMessage = data.message || errorMessage;
          } catch {
            errorMessage = `Error del servidor (${response.status})`;
          }
        } else {
          errorMessage = `Error del servidor (${response.status})`;
        }
        throw new Error(errorMessage);
      }

      // Re-sincronizar estado canónico del backend en lugar de hacer update
      // optimista. Antes (bug reportado mayo-2026): si por cualquier motivo
      // el backend no persistía `debe_cambiar_password=false` (rollback en
      // transacción, conflicto con la UPDATE @Modifying de `intentosFallidos`
      // dentro del mismo transactional context, etc), el frontend mostraba el
      // modal cerrado en memoria pero al refrescar la página volvía a aparecer
      // porque `/me` retornaba `true` desde DB. Pullear /me obliga a usar la
      // verdad del backend — si quedó `true` ahí, el modal NO se cierra y
      // mostramos el error real al socio en vez de fingir que pasó.
      const meRes = await fetch('/api/auth/me', { credentials: 'include' });
      if (meRes.ok) {
        const fresh = await meRes.json();
        if (fresh.debeCambiarPassword === true) {
          // Backend dice que sigue obligatorio cambiar password → algo falló
          // en la persistencia aunque el endpoint haya devuelto 200.
          throw new Error(
            'El sistema aceptó tu nueva contraseña pero no la guardó. ' +
            'Intentá de nuevo o contactá a soporte.'
          );
        }
        setUser({
          id: fresh.id,
          nombreUsuario: fresh.nombreUsuario,
          correoElectronico: fresh.correoElectronico,
          nombreCompleto: fresh.nombreCompleto,
          rol: fresh.rol,
          socioId: fresh.socioId,
          debeCambiarPassword: fresh.debeCambiarPassword ?? false,
        });
      } else {
        // Fallback: si /me falla (network, etc), aplicamos update optimista
        // para no dejar al socio atrapado con el modal.
        setUser({ ...user!, debeCambiarPassword: false });
      }

      toast.success('Contraseña actualizada', {
        description: 'Ya podés usar tu nueva contraseña.',
      });

      setPasswordActual('');
      setNuevoPassword('');
      setConfirmPassword('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cambiar contraseña');
    } finally {
      setIsLoading(false);
    }
  };

  if (!user?.debeCambiarPassword) {
    return null;
  }

  return (
    <Dialog open={open}>
      <DialogContent
        className="sm:max-w-[425px]"
        onInteractOutside={(e) => e.preventDefault()}
        // Bloquear cierre con Escape — el modal es obligatorio y dejarlo
        // cerrar dejaba al socio con `debeCambiarPassword=true` en backend
        // pero modal oculto en frontend hasta el próximo refresh (UX confusa).
        onEscapeKeyDown={(e) => e.preventDefault()}
      >
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Lock className="h-5 w-5" />
              Cambiar Contraseña
            </DialogTitle>
            <DialogDescription>
              Es la primera vez que inicias sesión. Debes cambiar tu contraseña temporal.
            </DialogDescription>
            <div className="mt-2 p-2 bg-amber-50 border border-amber-200 rounded-md">
              <p className="text-xs text-amber-800">
                Tiempo restante: <span className="font-mono font-bold">{formatTime(timeRemaining)}</span>
              </p>
            </div>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="password-actual">Contraseña Actual</Label>
              <Input
                id="password-actual"
                type="password"
                value={passwordActual}
                onChange={(e) => setPasswordActual(e.target.value)}
                required
                disabled={isLoading}
              />
            </div>

            <div className="grid gap-2">
              <Label htmlFor="nuevo-password">Nueva Contraseña</Label>
              <div className="relative">
                <Input
                  id="nuevo-password"
                  type={showPassword ? 'text' : 'password'}
                  value={nuevoPassword}
                  onChange={(e) => setNuevoPassword(e.target.value)}
                  required
                  disabled={isLoading}
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              <p className="text-xs text-gray-500">
                Mínimo 8 caracteres, una mayúscula, un número y un carácter especial (@$!%*?&)
              </p>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="confirm-password">Confirmar Nueva Contraseña</Label>
              <Input
                id="confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                disabled={isLoading}
              />
            </div>

            {error && (
              <p className="text-sm text-red-500">{error}</p>
            )}
          </div>

          <DialogFooter>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Cambiando...
                </>
              ) : (
                'Cambiar Contraseña'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}