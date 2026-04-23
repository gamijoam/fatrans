'use client';

import { useAuthStore } from '@/stores/auth-store';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Loader2 } from 'lucide-react';

export default function DocumentosPage() {
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
      <h1 className="text-2xl font-bold text-gray-900">Mis Documentos</h1>
      
      <Card>
        <CardHeader>
          <CardTitle>Documentos PDF</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-gray-500">No hay documentos disponibles.</p>
        </CardContent>
      </Card>
    </div>
  );
}