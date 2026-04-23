'use client';

import { useState } from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { profileSchema, ProfileFormData } from '@/lib/utils/validators';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Loader2, User, Briefcase, Phone, Shield } from 'lucide-react';

interface ProfileFormProps {
  initialData: ProfileData;
  onSubmit: (data: ProfileFormData) => Promise<void>;
  isLoading?: boolean;
}

interface ProfileData {
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
  direccionResidencia?: Direccion;
  direccionLaboral?: Direccion;
  empresa?: string;
  departamento?: string;
  cargo?: string;
  tipoContrato?: string;
  numeroCuentaNomina?: string;
  bancoNomina?: string;
  contactoEmergencia?: ContactoEmergencia;
  estado: string;
  fechaIngreso: string;
  roles: string[];
}

interface Direccion {
  calle?: string;
  ciudad?: string;
  estado?: string;
  codigoPostal?: string;
  pais?: string;
}

interface ContactoEmergencia {
  nombre: string;
  telefono: string;
  parentesco?: string;
}

export function ProfileForm({ initialData, onSubmit, isLoading }: ProfileFormProps) {
  const [activeTab, setActiveTab] = useState('personal');

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      primerNombre: initialData.primerNombre,
      segundoNombre: initialData.segundoNombre || '',
      primerApellido: initialData.primerApellido,
      segundoApellido: initialData.segundoApellido || '',
      fechaNacimiento: initialData.fechaNacimiento?.split('T')[0] || '',
      genero: (initialData.genero as 'MASCULINO' | 'FEMENINO' | 'OTRO') || undefined,
      estadoCivil: (initialData.estadoCivil as 'SOLTERO' | 'CASADO' | 'DIVORCIADO' | 'VIUDO' | 'UNION_LIBRE') || undefined,
      correoElectronico: initialData.correoElectronico,
      telefonoPrincipal: initialData.telefonoPrincipal,
      telefonoSecundario: initialData.telefonoSecundario || '',
      direccionResidencia: initialData.direccionResidencia,
      direccionLaboral: initialData.direccionLaboral,
      empresa: initialData.empresa || '',
      departamento: initialData.departamento || '',
      cargo: initialData.cargo || '',
      tipoContrato: (initialData.tipoContrato as 'CONTRATO_INDEFINIDO' | 'CONTRATO_TEMPORAL' | 'CONTRATO_POR_HORAS' | 'SERVICIOS' | 'APRENDIZ') || undefined,
      numeroCuentaNomina: initialData.numeroCuentaNomina || '',
      bancoNomina: initialData.bancoNomina || '',
      contactoEmergencia: initialData.contactoEmergencia,
    },
  });

  const direccionResidencia = watch('direccionResidencia');
  const direccionLaboral = watch('direccionLaboral');
  const generoValue = watch('genero');
  const estadoCivilValue = watch('estadoCivil');
  const tipoContratoValue = watch('tipoContrato');
  const contactoEmergencia = watch('contactoEmergencia');

  const onFormSubmit: SubmitHandler<ProfileFormData> = async (data) => {
    await onSubmit(data);
  };

  return (
    <form onSubmit={handleSubmit(onFormSubmit)}>
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="personal" className="flex items-center gap-2">
            <User className="h-4 w-4" />
            Personal
          </TabsTrigger>
          <TabsTrigger value="laboral" className="flex items-center gap-2">
            <Briefcase className="h-4 w-4" />
            Laboral
          </TabsTrigger>
          <TabsTrigger value="contacto" className="flex items-center gap-2">
            <Phone className="h-4 w-4" />
            Contacto
          </TabsTrigger>
          <TabsTrigger value="emergencia" className="flex items-center gap-2">
            <Shield className="h-4 w-4" />
            Emergencia
          </TabsTrigger>
        </TabsList>

        <TabsContent value="personal" className="space-y-4 mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Datos Personales</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="primerNombre">Primer Nombre</Label>
                  <Input id="primerNombre" {...register('primerNombre')} aria-invalid={!!errors.primerNombre} />
                  {errors.primerNombre && <p className="text-xs text-red-500">{errors.primerNombre.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="segundoNombre">Segundo Nombre</Label>
                  <Input id="segundoNombre" {...register('segundoNombre')} />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="primerApellido">Primer Apellido</Label>
                  <Input id="primerApellido" {...register('primerApellido')} aria-invalid={!!errors.primerApellido} />
                  {errors.primerApellido && <p className="text-xs text-red-500">{errors.primerApellido.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="segundoApellido">Segundo Apellido</Label>
                  <Input id="segundoApellido" {...register('segundoApellido')} />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fechaNacimiento">Fecha de Nacimiento</Label>
                  <Input id="fechaNacimiento" type="date" {...register('fechaNacimiento')} aria-invalid={!!errors.fechaNacimiento} />
                  {errors.fechaNacimiento && <p className="text-xs text-red-500">{errors.fechaNacimiento.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label>Género</Label>
                  <Select value={generoValue || ''} onValueChange={(val) => setValue('genero', val as 'MASCULINO' | 'FEMENINO' | 'OTRO')}>
                    <SelectTrigger>
                      <SelectValue placeholder="Seleccionar" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="MASCULINO">Masculino</SelectItem>
                      <SelectItem value="FEMENINO">Femenino</SelectItem>
                      <SelectItem value="OTRO">Otro</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="space-y-2">
                <Label>Estado Civil</Label>
                <Select value={estadoCivilValue || ''} onValueChange={(val) => setValue('estadoCivil', val as 'SOLTERO' | 'CASADO' | 'DIVORCIADO' | 'VIUDO' | 'UNION_LIBRE')}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="SOLTERO">Soltero</SelectItem>
                    <SelectItem value="CASADO">Casado</SelectItem>
                    <SelectItem value="DIVORCIADO">Divorciado</SelectItem>
                    <SelectItem value="VIUDO">Viudo</SelectItem>
                    <SelectItem value="UNION_LIBRE">Unión Libre</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Dirección de Residencia</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="calleResidencia">Calle / Avenida</Label>
                <Input
                  id="calleResidencia"
                  value={direccionResidencia?.calle || ''}
                  onChange={(e) => setValue('direccionResidencia', { ...direccionResidencia, calle: e.target.value } as ProfileFormData['direccionResidencia'])}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="ciudadResidencia">Ciudad</Label>
                  <Input
                    id="ciudadResidencia"
                    value={direccionResidencia?.ciudad || ''}
                    onChange={(e) => setValue('direccionResidencia', { ...direccionResidencia, ciudad: e.target.value } as ProfileFormData['direccionResidencia'])}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="estadoResidencia">Estado</Label>
                  <Input
                    id="estadoResidencia"
                    value={direccionResidencia?.estado || ''}
                    onChange={(e) => setValue('direccionResidencia', { ...direccionResidencia, estado: e.target.value } as ProfileFormData['direccionResidencia'])}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="laboral" className="space-y-4 mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Información Laboral</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="empresa">Empresa</Label>
                <Input id="empresa" {...register('empresa')} />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="departamento">Departamento</Label>
                  <Input id="departamento" {...register('departamento')} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cargo">Cargo</Label>
                  <Input id="cargo" {...register('cargo')} />
                </div>
              </div>

              <div className="space-y-2">
                <Label>Tipo de Contrato</Label>
                <Select value={tipoContratoValue || ''} onValueChange={(val) => setValue('tipoContrato', val as 'CONTRATO_INDEFINIDO' | 'CONTRATO_TEMPORAL' | 'CONTRATO_POR_HORAS' | 'SERVICIOS' | 'APRENDIZ')}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="CONTRATO_INDEFINIDO">Contrato Indefinido</SelectItem>
                    <SelectItem value="CONTRATO_TEMPORAL">Contrato Temporal</SelectItem>
                    <SelectItem value="CONTRATO_POR_HORAS">Contrato por Horas</SelectItem>
                    <SelectItem value="SERVICIOS">Servicios</SelectItem>
                    <SelectItem value="APRENDIZ">Aprendiz</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Dirección Laboral</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="calleLaboral">Calle / Avenida</Label>
                <Input
                  id="calleLaboral"
                  value={direccionLaboral?.calle || ''}
                  onChange={(e) => setValue('direccionLaboral', { ...direccionLaboral, calle: e.target.value } as ProfileFormData['direccionLaboral'])}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="ciudadLaboral">Ciudad</Label>
                  <Input
                    id="ciudadLaboral"
                    value={direccionLaboral?.ciudad || ''}
                    onChange={(e) => setValue('direccionLaboral', { ...direccionLaboral, ciudad: e.target.value } as ProfileFormData['direccionLaboral'])}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="estadoLaboral">Estado</Label>
                  <Input
                    id="estadoLaboral"
                    value={direccionLaboral?.estado || ''}
                    onChange={(e) => setValue('direccionLaboral', { ...direccionLaboral, estado: e.target.value } as ProfileFormData['direccionLaboral'])}
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Cuenta Nómina</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="bancoNomina">Banco</Label>
                  <Input id="bancoNomina" {...register('bancoNomina')} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="numeroCuentaNomina">Número de Cuenta</Label>
                  <Input id="numeroCuentaNomina" {...register('numeroCuentaNomina')} placeholder="10-20 dígitos" />
                  {errors.numeroCuentaNomina && <p className="text-xs text-red-500">{errors.numeroCuentaNomina.message}</p>}
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="contacto" className="space-y-4 mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Información de Contacto</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="correoElectronico">Correo Electrónico</Label>
                <Input id="correoElectronico" type="email" {...register('correoElectronico')} aria-invalid={!!errors.correoElectronico} />
                {errors.correoElectronico && <p className="text-xs text-red-500">{errors.correoElectronico.message}</p>}
              </div>

              <Separator />

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="telefonoPrincipal">Teléfono Principal</Label>
                  <Input id="telefonoPrincipal" {...register('telefonoPrincipal')} placeholder="Ej: 04141234567" />
                  {errors.telefonoPrincipal && <p className="text-xs text-red-500">{errors.telefonoPrincipal.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="telefonoSecundario">Teléfono Secundario</Label>
                  <Input id="telefonoSecundario" {...register('telefonoSecundario')} placeholder="Ej: 04161234567" />
                  {errors.telefonoSecundario && <p className="text-xs text-red-500">{errors.telefonoSecundario.message}</p>}
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="emergencia" className="space-y-4 mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Contacto de Emergencia</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="nombreEmergencia">Nombre Completo</Label>
                <Input
                  id="nombreEmergencia"
                  value={contactoEmergencia?.nombre || ''}
                  onChange={(e) => setValue('contactoEmergencia', { ...contactoEmergencia, nombre: e.target.value } as ProfileFormData['contactoEmergencia'])}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="telefonoEmergencia">Teléfono</Label>
                  <Input
                    id="telefonoEmergencia"
                    value={contactoEmergencia?.telefono || ''}
                    onChange={(e) => setValue('contactoEmergencia', { ...contactoEmergencia, telefono: e.target.value } as ProfileFormData['contactoEmergencia'])}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="parentescoEmergencia">Parentesco</Label>
                  <Input
                    id="parentescoEmergencia"
                    value={contactoEmergencia?.parentesco || ''}
                    onChange={(e) => setValue('contactoEmergencia', { ...contactoEmergencia, parentesco: e.target.value } as ProfileFormData['contactoEmergencia'])}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <div className="mt-6 flex justify-end gap-4">
        <Button type="submit" disabled={isLoading} className="bg-green-600 hover:bg-green-700">
          {isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Guardando...
            </>
          ) : (
            'Guardar Cambios'
          )}
        </Button>
      </div>
    </form>
  );
}