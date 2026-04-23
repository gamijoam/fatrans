'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore, UserRol } from '@/stores/auth-store';
import { Spinner } from '@/components/ui/spinner';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles: UserRol[];
}

export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { user, isLoading } = useAuthStore();
  const router = useRouter();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    if (!isLoading) {
      setIsChecking(false);
    }
  }, [isLoading]);

  if (isChecking || isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!user || !isAuthenticated(user.rol, allowedRoles)) {
    return <Forbidden />;
  }

  return <>{children}</>;
}

function isAuthenticated(rol: UserRol, allowedRoles: UserRol[]): boolean {
  return allowedRoles.includes(rol);
}

function Forbidden() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="text-center space-y-4">
        <div className="text-6xl font-bold text-red-500">403</div>
        <h1 className="text-2xl font-bold text-gray-900">Acceso Denegado</h1>
        <p className="text-gray-500">
          No tienes permisos para acceder a esta sección.
        </p>
        <a
          href="/dashboard"
          className="inline-flex items-center justify-center rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
        >
          Volver al Dashboard
        </a>
      </div>
    </div>
  );
}
