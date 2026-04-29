'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { LogOut, AlertTriangle } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { toast } from 'sonner';
import { cn } from '@/lib/utils/cn';

interface LogoutButtonProps {
  variant?: 'default' | 'ghost' | 'destructive';
  className?: string;
}

export function LogoutButton({ variant = 'default', className }: LogoutButtonProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = async () => {
    setIsLoading(true);
    try {
      await logout();
      toast.success('Sesión cerrada correctamente');
      router.push('/login');
    } catch (error) {
      console.error('Logout error:', error);
      toast.error('Error al cerrar sesión');
    } finally {
      setIsLoading(false);
      setIsOpen(false);
    }
  };

  const triggerStyles = variant === 'ghost'
    ? 'flex items-center gap-3 w-full px-4 py-2 text-sm text-white/70 hover:text-white hover:bg-white/10 rounded-md transition-colors'
    : variant === 'destructive'
    ? 'flex items-center gap-3 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-md transition-colors'
    : 'flex items-center gap-3 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-md transition-colors';

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <button className={cn(triggerStyles, className)}>
          <LogOut className="h-4 w-4" />
          <span>Cerrar Sesión</span>
        </button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]" aria-labelledby="logout-dialog-title">
        <DialogHeader>
          <div className="flex items-center gap-2 text-red-600">
            <AlertTriangle className="h-5 w-5" />
            <DialogTitle id="logout-dialog-title">¿Cerrar Sesión?</DialogTitle>
          </div>
          <DialogDescription>
            Tu sesión será cerrada y tendrás que volver a iniciar sesión para acceder a tu cuenta.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={() => setIsOpen(false)}>
            Cancelar
          </Button>
          <Button
            variant="destructive"
            onClick={handleLogout}
            disabled={isLoading}
            aria-busy={isLoading}
          >
            {isLoading ? 'Cerrando...' : 'Cerrar Sesión'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
