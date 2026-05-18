'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Loader2, Search, ShieldAlert } from 'lucide-react';
import { SessionManager } from '@/components/features/admin/session-manager';

export default function AdminSesionesPage() {
  const [usuarioId, setUsuarioId] = useState('');
  const [usuarioNombre, setUsuarioNombre] = useState('');
  const [showManager, setShowManager] = useState(false);

  const handleBuscar = () => {
    if (usuarioId.trim()) {
      setShowManager(true);
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Gestión de Sesiones</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ShieldAlert className="h-5 w-5" />
            Buscar Usuario
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-gray-500">
            Ingrese el ID del usuario para ver y gestionar sus sesiones activas.
            Esta función permite invalidar sesiones por seguridad.
          </p>
          <div className="flex gap-4">
            <Input
              placeholder="ID del usuario (UUID)"
              value={usuarioId}
              onChange={(e) => setUsuarioId(e.target.value)}
              className="max-w-md"
            />
            <Input
              placeholder="Nombre del usuario"
              value={usuarioNombre}
              onChange={(e) => setUsuarioNombre(e.target.value)}
              className="max-w-xs"
            />
            <Button onClick={handleBuscar} disabled={!usuarioId.trim()}>
              <Search className="h-4 w-4 mr-2" />
              Buscar
            </Button>
          </div>
        </CardContent>
      </Card>

      {showManager && usuarioId && (
        <SessionManager
          usuarioId={usuarioId}
          usuarioNombre={usuarioNombre || 'Usuario'}
        />
      )}
    </div>
  );
}