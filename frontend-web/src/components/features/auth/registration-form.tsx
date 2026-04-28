'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { registroSchema, ESTADOS_VENEZUELA, TIPOS_DOCUMENTO, GENEROS, ESTADOS_CIVILES } from '@/lib/utils/validators';
import type { RegistroFormData } from '@/lib/utils/validators';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Loader2, AlertCircle, CheckCircle, FileText, User, MapPin, Phone, Building } from 'lucide-react';
import { sanitizeHTML } from '@/lib/utils/cn';
import { toast } from 'sonner';

const getLabelText = (enumValue: string): string => {
  const labels: Record<string, string> = {
    CEDULA: 'Cédula de Identidad (V-)',
    CEDULA_EXTRANJERO: 'Cédula Extranjero (E-)',
    PASAPORTE: 'Pasaporte',
    RIF: 'RIF',
    MASCULINO: 'Masculino',
    FEMENINO: 'Femenino',
    OTRO: 'Otro',
    SOLTERO: 'Soltero/a',
    CASADO: 'Casado/a',
    DIVORCIADO: 'Divorciado/a',
    VIUDO: 'Viudo/a',
    UNION_LIBRE: 'Unión Libre',
  };
  return labels[enumValue] || enumValue;
};

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
      const payload = {
        ...data,
        salario: data.salario ? parseFloat(data.salario.replace(',', '.')) : null,
      };

      const response = await fetch('/api/auth/registro', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || 'Error al procesar solicitud');
      }

      setIsSuccess(true);
      toast.success('Solicitud enviada correctamente');

    } catch (error) {
      console.error('Registro error:', error);
      const message = error instanceof Error ? sanitizeHTML(error.message) : 'Error de conexión';
      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  if (isSuccess) {
    return (
      <Card className="w-full max-w-2xl mx-auto">
        <CardContent className="pt-6">
          <div className="text-center space-y-4">
            <div className="flex justify-center">
              <CheckCircle className="h-16 w-16 text-green-500" />
            </div>
            <CardTitle className="text-xl">¡Solicitud Enviada!</CardTitle>
            <CardDescription>
              Tu solicitud de registro ha sido enviada exitosamente.<br />
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
    <Card className="w-full max-w-2xl mx-auto">
      <CardHeader className="space-y-1">
        <div className="flex justify-center mb-2">
          <div className="p-3 rounded-full bg-blue-100">
            <FileText className="h-8 w-8 text-blue-600" />
          </div>
        </div>
        <CardTitle className="text-2xl text-center font-bold text-gray-900">
          Crear Cuenta de Socio
        </CardTitle>
        <CardDescription className="text-center">
          Completa todos los campos para solicitar tu cuenta de socio
        </CardDescription>
        <div className="flex justify-center">
          <Badge variant="outline" className="text-amber-600 bg-amber-50 border-amber-200">
            Pendiente de aprobación
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="bg-blue-50 p-3 rounded-lg flex items-center gap-2">
            <User className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-800">Datos Personales</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
              />
              {errors.nombreCompleto && (
                <p role="alert" className="text-xs text-red-500">{errors.nombreCompleto.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="tipoDocumento">Tipo de Documento</Label>
              <select
                id="tipoDocumento"
                disabled={isLoading}
                {...register('tipoDocumento')}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                aria-invalid={!!errors.tipoDocumento}
              >
                <option value="">Seleccione...</option>
                {TIPOS_DOCUMENTO.map((tipo) => (
                  <option key={tipo} value={tipo}>{getLabelText(tipo)}</option>
                ))}
              </select>
              {errors.tipoDocumento && (
                <p role="alert" className="text-xs text-red-500">{errors.tipoDocumento.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="cedula">Cédula de Identidad</Label>
              <Input
                id="cedula"
                type="text"
                placeholder="V-12345678"
                autoComplete="off"
                disabled={isLoading}
                {...register('cedula')}
                aria-invalid={!!errors.cedula}
              />
              {errors.cedula && (
                <p role="alert" className="text-xs text-red-500">{errors.cedula.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="fechaNacimiento">Fecha de Nacimiento</Label>
              <Input
                id="fechaNacimiento"
                type="date"
                disabled={isLoading}
                {...register('fechaNacimiento')}
                aria-invalid={!!errors.fechaNacimiento}
              />
              {errors.fechaNacimiento && (
                <p role="alert" className="text-xs text-red-500">{errors.fechaNacimiento.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="genero">Género</Label>
              <select
                id="genero"
                disabled={isLoading}
                {...register('genero')}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                aria-invalid={!!errors.genero}
              >
                <option value="">Seleccione...</option>
                {GENEROS.map((gen) => (
                  <option key={gen} value={gen}>{getLabelText(gen)}</option>
                ))}
              </select>
              {errors.genero && (
                <p role="alert" className="text-xs text-red-500">{errors.genero.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="estadoCivil">Estado Civil</Label>
              <select
                id="estadoCivil"
                disabled={isLoading}
                {...register('estadoCivil')}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                aria-invalid={!!errors.estadoCivil}
              >
                <option value="">Seleccione...</option>
                {ESTADOS_CIVILES.map((est) => (
                  <option key={est} value={est}>{getLabelText(est)}</option>
                ))}
              </select>
              {errors.estadoCivil && (
                <p role="alert" className="text-xs text-red-500">{errors.estadoCivil.message}</p>
              )}
            </div>
          </div>

          <div className="bg-blue-50 p-3 rounded-lg flex items-center gap-2">
            <Phone className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-800">Información de Contacto</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
              />
              {errors.correoElectronico && (
                <p role="alert" className="text-xs text-red-500">{errors.correoElectronico.message}</p>
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
              />
              {errors.telefono && (
                <p role="alert" className="text-xs text-red-500">{errors.telefono.message}</p>
              )}
            </div>
          </div>

          <div className="bg-blue-50 p-3 rounded-lg flex items-center gap-2">
            <Building className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-800">Información Laboral</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
              />
              {errors.empresa && (
                <p role="alert" className="text-xs text-red-500">{errors.empresa.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="rifEmpresa">RIF Empresa</Label>
              <Input
                id="rifEmpresa"
                type="text"
                placeholder="J-123456789-0"
                autoComplete="off"
                disabled={isLoading}
                {...register('rifEmpresa')}
                aria-invalid={!!errors.rifEmpresa}
              />
              {errors.rifEmpresa && (
                <p role="alert" className="text-xs text-red-500">{errors.rifEmpresa.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="departamento">Departamento</Label>
              <Input
                id="departamento"
                type="text"
                placeholder="Recursos Humanos"
                autoComplete="organization-title"
                disabled={isLoading}
                {...register('departamento')}
                aria-invalid={!!errors.departamento}
              />
              {errors.departamento && (
                <p role="alert" className="text-xs text-red-500">{errors.departamento.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="cargo">Cargo</Label>
              <Input
                id="cargo"
                type="text"
                placeholder="Analista de Sistemas"
                autoComplete="organization-title"
                disabled={isLoading}
                {...register('cargo')}
                aria-invalid={!!errors.cargo}
              />
              {errors.cargo && (
                <p role="alert" className="text-xs text-red-500">{errors.cargo.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="salario">Salario (Bs)</Label>
              <Input
                id="salario"
                type="text"
                placeholder="1,500.00"
                autoComplete="off"
                disabled={isLoading}
                {...register('salario')}
                aria-invalid={!!errors.salario}
              />
              {errors.salario && (
                <p role="alert" className="text-xs text-red-500">{errors.salario.message}</p>
              )}
            </div>
          </div>

          <div className="bg-blue-50 p-3 rounded-lg flex items-center gap-2">
            <MapPin className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-800">Dirección de Residencia</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="direccionEstado">Estado</Label>
              <select
                id="direccionEstado"
                disabled={isLoading}
                {...register('direccionEstado')}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="">Seleccione...</option>
                {ESTADOS_VENEZUELA.map((estado) => (
                  <option key={estado} value={estado}>{estado}</option>
                ))}
              </select>
              {errors.direccionEstado && (
                <p role="alert" className="text-xs text-red-500">{errors.direccionEstado.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="direccionCiudad">Ciudad</Label>
              <Input
                id="direccionCiudad"
                type="text"
                placeholder="Caracas"
                autoComplete="address-level2"
                disabled={isLoading}
                {...register('direccionCiudad')}
                aria-invalid={!!errors.direccionCiudad}
              />
              {errors.direccionCiudad && (
                <p role="alert" className="text-xs text-red-500">{errors.direccionCiudad.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="direccionMunicipio">Municipio</Label>
              <Input
                id="direccionMunicipio"
                type="text"
                placeholder="Libertador"
                autoComplete="address-level3"
                disabled={isLoading}
                {...register('direccionMunicipio')}
                aria-invalid={!!errors.direccionMunicipio}
              />
              {errors.direccionMunicipio && (
                <p role="alert" className="text-xs text-red-500">{errors.direccionMunicipio.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="direccionCalle">Calle / Avenida / Casa</Label>
              <Input
                id="direccionCalle"
                type="text"
                placeholder="Av. Principal, Casa #123"
                autoComplete="street-address"
                disabled={isLoading}
                {...register('direccionCalle')}
                aria-invalid={!!errors.direccionCalle}
              />
              {errors.direccionCalle && (
                <p role="alert" className="text-xs text-red-500">{errors.direccionCalle.message}</p>
              )}
            </div>
          </div>

          <div className="bg-blue-50 p-3 rounded-lg flex items-center gap-2">
            <Phone className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-800">Contacto de Emergencia</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="emergenciaNombre">Nombre Completo</Label>
              <Input
                id="emergenciaNombre"
                type="text"
                placeholder="María García"
                autoComplete="off"
                disabled={isLoading}
                {...register('emergenciaNombre')}
                aria-invalid={!!errors.emergenciaNombre}
              />
              {errors.emergenciaNombre && (
                <p role="alert" className="text-xs text-red-500">{errors.emergenciaNombre.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="emergenciaTelefono">Teléfono</Label>
              <Input
                id="emergenciaTelefono"
                type="tel"
                placeholder="04121234567"
                autoComplete="off"
                disabled={isLoading}
                {...register('emergenciaTelefono')}
                aria-invalid={!!errors.emergenciaTelefono}
              />
              {errors.emergenciaTelefono && (
                <p role="alert" className="text-xs text-red-500">{errors.emergenciaTelefono.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="emergenciaParentesco">Parentesco</Label>
              <Input
                id="emergenciaParentesco"
                type="text"
                placeholder="Cónyuge"
                autoComplete="off"
                disabled={isLoading}
                {...register('emergenciaParentesco')}
                aria-invalid={!!errors.emergenciaParentesco}
              />
              {errors.emergenciaParentesco && (
                <p role="alert" className="text-xs text-red-500">{errors.emergenciaParentesco.message}</p>
              )}
            </div>
          </div>

          <Separator className="my-4" />

          <div className="space-y-4">
            <div className="flex items-start gap-3 p-4 rounded-lg bg-yellow-50 border border-yellow-200">
              <AlertCircle className="h-5 w-5 text-yellow-600 mt-0.5 flex-shrink-0" />
              <div className="space-y-2">
                <p className="text-sm text-yellow-800 font-medium">Tu solicitud estará pendiente de aprobación</p>
                <p className="text-xs text-yellow-700">Un administrador la revisará y recibirás un correo cuando sea aprobada.</p>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="aceptaTerminos"
                  {...register('aceptaTerminos')}
                  className="h-4 w-4 rounded border-gray-300 text-green-600 focus:ring-green-500"
                />
                <Label htmlFor="aceptaTerminos" className="text-sm">
                  Acepto los <a href="/terminos" className="text-blue-600 hover:underline">términos y condiciones</a>
                </Label>
              </div>
              {errors.aceptaTerminos && (
                <p role="alert" className="text-xs text-red-500">{errors.aceptaTerminos.message}</p>
              )}

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="aceptaLopdp"
                  {...register('aceptaLopdp')}
                  className="h-4 w-4 rounded border-gray-300 text-green-600 focus:ring-green-500"
                />
                <Label htmlFor="aceptaLopdp" className="text-sm">
                  Acepto la <a href="/lopdp" className="text-blue-600 hover:underline">política de protección de datos personales</a> (LOPDP)
                </Label>
              </div>
              {errors.aceptaLopdp && (
                <p role="alert" className="text-xs text-red-500">{errors.aceptaLopdp.message}</p>
              )}
            </div>
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