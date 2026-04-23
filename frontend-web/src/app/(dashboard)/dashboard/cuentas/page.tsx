'use client';

import { useAuthStore } from '@/stores/auth-store';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Loader2 } from 'lucide-react';

export default function CuentasPage() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Mis Cuentas</h1>
      
      <Card>
        <CardHeader>
          <CardTitle>Cuentas de Ahorro</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-gray-500">No tienes cuentas de ahorro activas.</p>
        </CardContent>
      </Card>
    </div>
  );
}