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

export function LogoutButton() {
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

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <button
          className="flex items-center gap-3 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-md transition-colors"
        >
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
