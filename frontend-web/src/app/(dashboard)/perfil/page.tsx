'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { sociosApi } from '@/lib/api/client';
import { ProfileForm } from '@/components/features/profile/profile-form';
import { PasswordChangeDialog } from '@/components/features/profile/password-change-dialog';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import type { ProfileFormData } from '@/lib/utils/validators';

interface SocioData {
  id: string;
  numeroSocio: string;
  tipoDocumento: string;
  numeroDocumento: string;
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  fechaNacimiento?: string;
  genero?: string;
  estadoCivil?: string;
  correoElectronico: string;
  telefonoPrincipal: string;
  telefonoSecundario?: string;
  direccionResidencia?: {
    calle?: string;
    ciudad?: string;
    estado?: string;
    codigoPostal?: string;
    pais?: string;
  };
  direccionLaboral?: {
    calle?: string;
    ciudad?: string;
    estado?: string;
    codigoPostal?: string;
    pais?: string;
  };
  empresa?: string;
  departamento?: string;
  cargo?: string;
  tipoContrato?: string;
  numeroCuentaNomina?: string;
  bancoNomina?: string;
  contactoEmergencia?: {
    nombre: string;
    telefono: string;
    parentesco?: string;
  };
  estado: string;
  fechaIngreso: string;
  roles: string[];
}

export default function PerfilPage() {
  const [socio, setSocio] = useState<SocioData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const user = useAuthStore((state) => state.user);

  useEffect(() => {
    const fetchProfile = async () => {
      if (!user?.socioId) {
        toast.error('No se encontró ID de socio');
        setIsLoading(false);
        return;
      }

      try {
        const response = await sociosApi.getById(user.socioId);
        setSocio(response.data);
      } catch {
        toast.error('Error al cargar perfil');
      } finally {
        setIsLoading(false);
      }
    };

    fetchProfile();
  }, [user?.socioId]);

  const handleSubmit = async (data: ProfileFormData) => {
    if (!user?.socioId) return;

    setIsSaving(true);
    try {
      await sociosApi.updateProfile(user.socioId, data);
      toast.success('Perfil actualizado correctamente');
    } catch {
      toast.error('Error al guardar cambios');
    } finally {
      setIsSaving(false);
    }
  };

  const handlePasswordChange = async (data: { passwordActual: string; nuevoPassword: string; confirmarPassword: string }) => {
    console.log('Password change:', data);
    throw new Error('No implementado');
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  if (!socio) {
    return (
      <div className="p-6">
        <p className="text-center text-gray-500">No se pudo cargar el perfil</p>
      </div>
    );
  }

  const estadoBadgeVariant = socio.estado === 'ACTIVO' ? 'default' : 'secondary';
  const initials = `${socio.primerNombre[0] || ''}${socio.primerApellido[0] || ''}`.toUpperCase();

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Mi Perfil</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-1">
          <CardHeader className="text-center">
            <div className="flex justify-center mb-4">
              <Avatar className="h-24 w-24 bg-green-100">
                <AvatarFallback className="text-2xl text-green-600">{initials}</AvatarFallback>
              </Avatar>
            </div>
            <CardTitle>{socio.primerNombre} {socio.primerApellido}</CardTitle>
            <p className="text-sm text-gray-500">Socio {socio.numeroSocio}</p>
            <Badge variant={estadoBadgeVariant} className="mt-2 w-fit mx-auto">
              {socio.estado}
            </Badge>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-sm space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-500">Documento:</span>
                <span className="font-medium">{socio.tipoDocumento} {socio.numeroDocumento}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Fecha Ingreso:</span>
                <span className="font-medium">{new Date(socio.fechaIngreso).toLocaleDateString('es-VE')}</span>
              </div>
            </div>
            <Button variant="outline" className="w-full" disabled>
              Cambiar Foto
            </Button>
          </CardContent>
        </Card>

        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Información Personal</CardTitle>
                <PasswordChangeDialog onPasswordChange={handlePasswordChange} />
              </div>
            </CardHeader>
            <CardContent>
              <ProfileForm
                initialData={socio}
                onSubmit={handleSubmit}
                isLoading={isSaving}
              />
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}