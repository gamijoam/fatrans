'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { registroSchema } from '@/lib/utils/validators';
import type { RegistroFormData } from '@/lib/utils/validators';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Loader2, AlertCircle, CheckCircle } from 'lucide-react';
import { toast } from 'sonner';

export function RegistrationForm() {
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const router = useRouter();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegistroFormData>({
    resolver: zodResolver(registroSchema),
  });

  const onSubmit = async (data: RegistroFormData) => {
    setIsLoading(true);

    try {
      const response = await fetch('/api/auth/registro', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || 'Error al procesar solicitud');
      }

      setIsSuccess(true);
      toast.success('Solicitud enviada correctamente');

    } catch (error) {
      console.error('Registro error:', error);
      toast.error(error instanceof Error ? error.message : 'Error de conexión');
    } finally {
      setIsLoading(false);
    }
  };

  if (isSuccess) {
    return (
      <Card className="w-full max-w-md mx-auto">
        <CardContent className="pt-6">
          <div className="text-center space-y-4">
            <div className="flex justify-center">
              <CheckCircle className="h-16 w-16 text-green-500" />
            </div>
            <CardTitle className="text-xl">¡Solicitud Enviada!</CardTitle>
            <CardDescription>
              Tu solicitud de registro ha sido enviada exitosamente.
              Un administrador la revisará y recibirás un correo cuando sea aprobada.
            </CardDescription>
            <Button
              onClick={() => router.push('/login')}
              className="w-full bg-green-600 hover:bg-green-700 mt-4"
            >
              Volver al Login
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl text-center font-bold text-gray-900">
          Crear Cuenta
        </CardTitle>
        <CardDescription className="text-center">
          Completa el formulario para solicitar tu cuenta de socio
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="nombreCompleto">Nombre Completo</Label>
            <Input
              id="nombreCompleto"
              type="text"
              placeholder="Juan Pérez García"
              autoComplete="name"
              disabled={isLoading}
              {...register('nombreCompleto')}
              aria-invalid={!!errors.nombreCompleto}
              aria-describedby={errors.nombreCompleto ? 'nombreCompleto-error' : undefined}
            />
            {errors.nombreCompleto && (
              <p id="nombreCompleto-error" role="alert" className="text-xs text-red-500">
                {errors.nombreCompleto.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="cedula">Cédula de Identidad</Label>
            <Input
              id="cedula"
              type="text"
              placeholder="V-12345678 o 12345678"
              autoComplete="off"
              disabled={isLoading}
              {...register('cedula')}
              aria-invalid={!!errors.cedula}
              aria-describedby={errors.cedula ? 'cedula-error' : undefined}
            />
            {errors.cedula && (
              <p id="cedula-error" role="alert" className="text-xs text-red-500">
                {errors.cedula.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="correoElectronico">Correo Electrónico</Label>
            <Input
              id="correoElectronico"
              type="email"
              placeholder="juan@ejemplo.com"
              autoComplete="email"
              disabled={isLoading}
              {...register('correoElectronico')}
              aria-invalid={!!errors.correoElectronico}
              aria-describedby={errors.correoElectronico ? 'correoElectronico-error' : undefined}
            />
            {errors.correoElectronico && (
              <p id="correoElectronico-error" role="alert" className="text-xs text-red-500">
                {errors.correoElectronico.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="telefono">Teléfono</Label>
            <Input
              id="telefono"
              type="tel"
              placeholder="04121234567"
              autoComplete="tel"
              disabled={isLoading}
              {...register('telefono')}
              aria-invalid={!!errors.telefono}
              aria-describedby={errors.telefono ? 'telefono-error' : undefined}
            />
            {errors.telefono && (
              <p id="telefono-error" role="alert" className="text-xs text-red-500">
                {errors.telefono.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="empresa">Empresa</Label>
            <Input
              id="empresa"
              type="text"
              placeholder="Nombre de tu empresa"
              autoComplete="organization"
              disabled={isLoading}
              {...register('empresa')}
              aria-invalid={!!errors.empresa}
              aria-describedby={errors.empresa ? 'empresa-error' : undefined}
            />
            {errors.empresa && (
              <p id="empresa-error" role="alert" className="text-xs text-red-500">
                {errors.empresa.message}
              </p>
            )}
          </div>

          <div className="p-3 rounded-lg bg-yellow-50 border border-yellow-200 flex items-start gap-2">
            <AlertCircle className="h-4 w-4 text-yellow-600 mt-0.5 flex-shrink-0" />
            <p className="text-xs text-yellow-700">
              Tu solicitud estará pendiente de aprobación por un administrador antes de poder acceder.
            </p>
          </div>

          <Button
            type="submit"
            className="w-full bg-green-600 hover:bg-green-700"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Enviando...
              </>
            ) : (
              'Enviar Solicitud'
            )}
          </Button>
        </form>

        <div className="mt-6 text-center text-sm text-gray-500">
          ¿Ya tienes cuenta?{' '}
          <a href="/login" className="text-green-600 hover:underline">
            Inicia Sesión
          </a>
        </div>
      </CardContent>
    </Card>
  );
}
