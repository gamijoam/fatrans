'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { changePasswordSchema, ChangePasswordFormData } from '@/lib/utils/validators';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Loader2, Lock, Eye, EyeOff } from 'lucide-react';
import { toast } from 'sonner';

interface PasswordChangeDialogProps {
  onPasswordChange: (data: ChangePasswordFormData) => Promise<void>;
}

export function PasswordChangeDialog({ onPasswordChange }: PasswordChangeDialogProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [showActual, setShowActual] = useState(false);
  const [showNueva, setShowNueva] = useState(false);
  const [showConfirmar, setShowConfirmar] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<ChangePasswordFormData>({
    resolver: zodResolver(changePasswordSchema),
  });

  const onSubmit = async (data: ChangePasswordFormData) => {
    setIsLoading(true);
    try {
      await onPasswordChange(data);
      toast.success('Contraseña actualizada correctamente');
      setIsOpen(false);
      reset();
    } catch {
      toast.error('Error al cambiar la contraseña');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setIsOpen(false);
    reset();
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" className="w-full">
          <Lock className="mr-2 h-4 w-4" />
          Cambiar Contraseña
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Cambiar Contraseña</DialogTitle>
          <DialogDescription>
            Actualiza tu contraseña. Usa al menos 8 caracteres.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="passwordActual">Contraseña Actual</Label>
            <div className="relative">
              <Input
                id="passwordActual"
                type={showActual ? 'text' : 'password'}
                autoComplete="current-password"
                {...register('passwordActual')}
                aria-invalid={!!errors.passwordActual}
              />
              <button
                type="button"
                onClick={() => setShowActual(!showActual)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                aria-label={showActual ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {showActual ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {errors.passwordActual && (
              <p className="text-xs text-red-500">{errors.passwordActual.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="nuevoPassword">Nueva Contraseña</Label>
            <div className="relative">
              <Input
                id="nuevoPassword"
                type={showNueva ? 'text' : 'password'}
                autoComplete="new-password"
                {...register('nuevoPassword')}
                aria-invalid={!!errors.nuevoPassword}
              />
              <button
                type="button"
                onClick={() => setShowNueva(!showNueva)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                aria-label={showNueva ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {showNueva ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {errors.nuevoPassword && (
              <p className="text-xs text-red-500">{errors.nuevoPassword.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmarPassword">Confirmar Nueva Contraseña</Label>
            <div className="relative">
              <Input
                id="confirmarPassword"
                type={showConfirmar ? 'text' : 'password'}
                autoComplete="new-password"
                {...register('confirmarPassword')}
                aria-invalid={!!errors.confirmarPassword}
              />
              <button
                type="button"
                onClick={() => setShowConfirmar(!showConfirmar)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                aria-label={showConfirmar ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {showConfirmar ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {errors.confirmarPassword && (
              <p className="text-xs text-red-500">{errors.confirmarPassword.message}</p>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isLoading} className="bg-green-600 hover:bg-green-700">
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Guardando...
                </>
              ) : (
                'Actualizar'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}