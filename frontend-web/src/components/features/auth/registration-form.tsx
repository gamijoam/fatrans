'use client';

import { useState } from 'react';
import { useForm, Controller, type Path } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import {
  registroSchema,
  ESTADOS_VENEZUELA,
  TIPOS_DOCUMENTO,
  GENEROS,
  ESTADOS_CIVILES,
} from '@/lib/utils/validators';
import type { RegistroFormData } from '@/lib/utils/validators';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { ProgressBar } from '@/components/ui/progress';
import {
  Loader2,
  AlertCircle,
  CheckCircle,
  User,
  MapPin,
  Phone,
  Building,
  ShieldCheck,
  ArrowLeft,
  ArrowRight,
} from 'lucide-react';
import { sanitizeHTML } from '@/lib/utils/cn';
import { toast } from 'sonner';

const ENUM_LABELS: Record<string, string> = {
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
const labelFor = (v: string) => ENUM_LABELS[v] ?? v;

type FieldName = Path<RegistroFormData>;

type StepDef = {
  id: 'personal' | 'contacto' | 'laboral' | 'emergencia' | 'confirmacion';
  title: string;
  description: string;
  icon: typeof User;
  /** Campos validados antes de avanzar (vacío = sin validación, ej. último paso). */
  fields: FieldName[];
};

const STEPS: StepDef[] = [
  {
    id: 'personal',
    title: 'Datos personales',
    description: 'Identificación y datos básicos',
    icon: User,
    fields: ['nombreCompleto', 'tipoDocumento', 'cedula', 'fechaNacimiento', 'genero', 'estadoCivil'],
  },
  {
    id: 'contacto',
    title: 'Contacto y dirección',
    description: 'Cómo te ubicamos',
    icon: MapPin,
    fields: [
      'correoElectronico',
      'telefono',
      'direccionEstado',
      'direccionCiudad',
      'direccionMunicipio',
      'direccionCalle',
    ],
  },
  {
    id: 'laboral',
    title: 'Información laboral',
    description: 'Empresa donde trabajas',
    icon: Building,
    fields: ['empresa', 'rifEmpresa', 'departamento', 'cargo', 'salario'],
  },
  {
    id: 'emergencia',
    title: 'Contacto de emergencia',
    description: 'A quién contactar si pasa algo',
    icon: Phone,
    fields: ['emergenciaNombre', 'emergenciaTelefono', 'emergenciaParentesco'],
  },
  {
    id: 'confirmacion',
    title: 'Confirmación',
    description: 'Revisa y acepta',
    icon: ShieldCheck,
    fields: ['aceptaTerminos', 'aceptaLopdp'],
  },
];

/** Pequeño helper visual: muestra "(Opcional)" después del label si aplica. */
function FieldLabel({ htmlFor, children, optional }: { htmlFor: string; children: React.ReactNode; optional?: boolean }) {
  return (
    <Label htmlFor={htmlFor} className="text-sm font-medium">
      {children}
      {optional && <span className="ml-1 text-xs font-normal text-muted-foreground">(opcional)</span>}
    </Label>
  );
}

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return (
    <p role="alert" className="text-xs text-destructive mt-1">
      {message}
    </p>
  );
}

export function RegistrationForm() {
  const [stepIndex, setStepIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const router = useRouter();

  const {
    register,
    handleSubmit,
    formState: { errors },
    trigger,
    control,
    getValues,
  } = useForm<RegistroFormData>({
    resolver: zodResolver(registroSchema),
    mode: 'onBlur',
    reValidateMode: 'onChange',
  });

  const currentStep = STEPS[stepIndex];
  const isLastStep = stepIndex === STEPS.length - 1;
  const progressValue = ((stepIndex + 1) / STEPS.length) * 100;

  const goNext = async () => {
    const valid = await trigger(currentStep.fields, { shouldFocus: true });
    if (!valid) return;
    setStepIndex((i) => Math.min(i + 1, STEPS.length - 1));
  };

  const goBack = () => setStepIndex((i) => Math.max(i - 1, 0));

  const onSubmit = async (data: RegistroFormData) => {
    setIsLoading(true);
    try {
      // El BFF revalida con registroSchema y normaliza salario (coma → punto).
      const response = await fetch('/api/auth/registro', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.message || 'Error al procesar solicitud');
      setIsSuccess(true);
      toast.success('Solicitud enviada correctamente');
    } catch (error) {
      const message =
        error instanceof Error ? sanitizeHTML(error.message) : 'Error de conexión';
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
            <CardTitle className="text-xl">¡Solicitud enviada!</CardTitle>
            <CardDescription>
              Tu solicitud de registro fue enviada correctamente.
              <br />
              Un administrador la revisará y recibirás un correo cuando sea aprobada.
            </CardDescription>
            <Button
              onClick={() => router.push('/login')}
              className="w-full bg-green-600 hover:bg-green-700 mt-4"
            >
              Volver al login
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  const Icon = currentStep.icon;

  return (
    <Card className="w-full max-w-2xl mx-auto">
      <CardHeader className="space-y-3">
        <div className="flex justify-center mb-1">
          <div className="p-3 rounded-full bg-blue-100">
            <Icon className="h-7 w-7 text-blue-600" />
          </div>
        </div>
        <CardTitle className="text-2xl text-center font-bold text-gray-900">
          Crear cuenta de socio
        </CardTitle>
        <CardDescription className="text-center">
          Paso {stepIndex + 1} de {STEPS.length}: {currentStep.title}
        </CardDescription>

        <div className="flex justify-center pt-1">
          <Badge variant="outline" className="text-amber-600 bg-amber-50 border-amber-200">
            Pendiente de aprobación
          </Badge>
        </div>

        {/* Progress bar */}
        <div className="pt-2 space-y-2">
          <ProgressBar value={progressValue} />
          <div className="hidden sm:flex justify-between gap-1 text-[10px] uppercase tracking-wide text-muted-foreground">
            {STEPS.map((s, i) => (
              <span
                key={s.id}
                className={i === stepIndex ? 'font-semibold text-foreground' : ''}
              >
                {s.title.split(' ')[0]}
              </span>
            ))}
          </div>
        </div>
      </CardHeader>

      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* ============ PASO 1: PERSONAL ============ */}
          {currentStep.id === 'personal' && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="md:col-span-2 space-y-2">
                  <FieldLabel htmlFor="nombreCompleto">Nombre completo</FieldLabel>
                  <Input
                    id="nombreCompleto"
                    type="text"
                    placeholder="Juan Pérez García"
                    autoComplete="name"
                    disabled={isLoading}
                    aria-invalid={!!errors.nombreCompleto}
                    {...register('nombreCompleto')}
                  />
                  <FieldError message={errors.nombreCompleto?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="tipoDocumento">Tipo de documento</FieldLabel>
                  <Controller
                    control={control}
                    name="tipoDocumento"
                    render={({ field }) => (
                      <Select
                        onValueChange={field.onChange}
                        value={field.value ?? ''}
                        disabled={isLoading}
                      >
                        <SelectTrigger id="tipoDocumento" aria-invalid={!!errors.tipoDocumento}>
                          <SelectValue placeholder="Selecciona..." />
                        </SelectTrigger>
                        <SelectContent>
                          {TIPOS_DOCUMENTO.map((t) => (
                            <SelectItem key={t} value={t}>
                              {labelFor(t)}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                  <FieldError message={errors.tipoDocumento?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="cedula">Cédula</FieldLabel>
                  <Input
                    id="cedula"
                    type="text"
                    placeholder="V-12345678"
                    autoComplete="off"
                    disabled={isLoading}
                    aria-invalid={!!errors.cedula}
                    {...register('cedula')}
                  />
                  <FieldError message={errors.cedula?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="fechaNacimiento">Fecha de nacimiento</FieldLabel>
                  <Input
                    id="fechaNacimiento"
                    type="date"
                    disabled={isLoading}
                    aria-invalid={!!errors.fechaNacimiento}
                    {...register('fechaNacimiento')}
                  />
                  <FieldError message={errors.fechaNacimiento?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="genero">Género</FieldLabel>
                  <Controller
                    control={control}
                    name="genero"
                    render={({ field }) => (
                      <Select
                        onValueChange={field.onChange}
                        value={field.value ?? ''}
                        disabled={isLoading}
                      >
                        <SelectTrigger id="genero" aria-invalid={!!errors.genero}>
                          <SelectValue placeholder="Selecciona..." />
                        </SelectTrigger>
                        <SelectContent>
                          {GENEROS.map((g) => (
                            <SelectItem key={g} value={g}>
                              {labelFor(g)}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                  <FieldError message={errors.genero?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="estadoCivil">Estado civil</FieldLabel>
                  <Controller
                    control={control}
                    name="estadoCivil"
                    render={({ field }) => (
                      <Select
                        onValueChange={field.onChange}
                        value={field.value ?? ''}
                        disabled={isLoading}
                      >
                        <SelectTrigger id="estadoCivil" aria-invalid={!!errors.estadoCivil}>
                          <SelectValue placeholder="Selecciona..." />
                        </SelectTrigger>
                        <SelectContent>
                          {ESTADOS_CIVILES.map((e) => (
                            <SelectItem key={e} value={e}>
                              {labelFor(e)}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                  <FieldError message={errors.estadoCivil?.message} />
                </div>
              </div>
            </div>
          )}

          {/* ============ PASO 2: CONTACTO + DIRECCIÓN ============ */}
          {currentStep.id === 'contacto' && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <FieldLabel htmlFor="correoElectronico">Correo electrónico</FieldLabel>
                  <Input
                    id="correoElectronico"
                    type="email"
                    placeholder="juan@ejemplo.com"
                    autoComplete="email"
                    disabled={isLoading}
                    aria-invalid={!!errors.correoElectronico}
                    {...register('correoElectronico')}
                  />
                  <FieldError message={errors.correoElectronico?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="telefono">Teléfono</FieldLabel>
                  <Input
                    id="telefono"
                    type="tel"
                    placeholder="04121234567"
                    autoComplete="tel"
                    disabled={isLoading}
                    aria-invalid={!!errors.telefono}
                    {...register('telefono')}
                  />
                  <FieldError message={errors.telefono?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="direccionEstado" optional>
                    Estado
                  </FieldLabel>
                  <Controller
                    control={control}
                    name="direccionEstado"
                    render={({ field }) => (
                      <Select
                        onValueChange={field.onChange}
                        value={field.value ?? ''}
                        disabled={isLoading}
                      >
                        <SelectTrigger id="direccionEstado">
                          <SelectValue placeholder="Selecciona estado..." />
                        </SelectTrigger>
                        <SelectContent>
                          {ESTADOS_VENEZUELA.map((e) => (
                            <SelectItem key={e} value={e}>
                              {e}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                  <FieldError message={errors.direccionEstado?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="direccionCiudad" optional>
                    Ciudad
                  </FieldLabel>
                  <Input
                    id="direccionCiudad"
                    type="text"
                    placeholder="Caracas"
                    autoComplete="address-level2"
                    disabled={isLoading}
                    {...register('direccionCiudad')}
                  />
                  <FieldError message={errors.direccionCiudad?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="direccionMunicipio" optional>
                    Municipio
                  </FieldLabel>
                  <Input
                    id="direccionMunicipio"
                    type="text"
                    placeholder="Libertador"
                    autoComplete="address-level3"
                    disabled={isLoading}
                    {...register('direccionMunicipio')}
                  />
                  <FieldError message={errors.direccionMunicipio?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="direccionCalle" optional>
                    Calle / Avenida / Casa
                  </FieldLabel>
                  <Input
                    id="direccionCalle"
                    type="text"
                    placeholder="Av. Principal, Casa #123"
                    autoComplete="street-address"
                    disabled={isLoading}
                    {...register('direccionCalle')}
                  />
                  <FieldError message={errors.direccionCalle?.message} />
                </div>
              </div>
            </div>
          )}

          {/* ============ PASO 3: LABORAL ============ */}
          {currentStep.id === 'laboral' && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="md:col-span-2 space-y-2">
                  <FieldLabel htmlFor="empresa">Empresa</FieldLabel>
                  <Input
                    id="empresa"
                    type="text"
                    placeholder="Nombre de tu empresa"
                    autoComplete="organization"
                    disabled={isLoading}
                    aria-invalid={!!errors.empresa}
                    {...register('empresa')}
                  />
                  <FieldError message={errors.empresa?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="rifEmpresa" optional>
                    RIF Empresa
                  </FieldLabel>
                  <Input
                    id="rifEmpresa"
                    type="text"
                    placeholder="J-123456789-0"
                    autoComplete="off"
                    disabled={isLoading}
                    {...register('rifEmpresa')}
                  />
                  <FieldError message={errors.rifEmpresa?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="departamento" optional>
                    Departamento
                  </FieldLabel>
                  <Input
                    id="departamento"
                    type="text"
                    placeholder="Recursos Humanos"
                    autoComplete="organization-title"
                    disabled={isLoading}
                    {...register('departamento')}
                  />
                  <FieldError message={errors.departamento?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="cargo" optional>
                    Cargo
                  </FieldLabel>
                  <Input
                    id="cargo"
                    type="text"
                    placeholder="Analista de sistemas"
                    autoComplete="organization-title"
                    disabled={isLoading}
                    {...register('cargo')}
                  />
                  <FieldError message={errors.cargo?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="salario" optional>
                    Salario mensual (Bs)
                  </FieldLabel>
                  <Input
                    id="salario"
                    type="text"
                    inputMode="decimal"
                    placeholder="1500.00"
                    autoComplete="off"
                    disabled={isLoading}
                    {...register('salario')}
                  />
                  <p className="text-xs text-muted-foreground">
                    Usa punto o coma para los decimales (ej. 1500.00 o 1500,00).
                  </p>
                  <FieldError message={errors.salario?.message} />
                </div>
              </div>
            </div>
          )}

          {/* ============ PASO 4: EMERGENCIA ============ */}
          {currentStep.id === 'emergencia' && (
            <div className="space-y-4">
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>¿Por qué te pedimos esto?</AlertTitle>
                <AlertDescription>
                  En caso de una emergencia que te impida operar tu cuenta, contactaremos
                  a esta persona. Es opcional pero recomendado.
                </AlertDescription>
              </Alert>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="md:col-span-2 space-y-2">
                  <FieldLabel htmlFor="emergenciaNombre" optional>
                    Nombre completo del contacto
                  </FieldLabel>
                  <Input
                    id="emergenciaNombre"
                    type="text"
                    placeholder="María García"
                    autoComplete="off"
                    disabled={isLoading}
                    {...register('emergenciaNombre')}
                  />
                  <FieldError message={errors.emergenciaNombre?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="emergenciaTelefono" optional>
                    Teléfono
                  </FieldLabel>
                  <Input
                    id="emergenciaTelefono"
                    type="tel"
                    placeholder="04121234567"
                    autoComplete="off"
                    disabled={isLoading}
                    {...register('emergenciaTelefono')}
                  />
                  <FieldError message={errors.emergenciaTelefono?.message} />
                </div>

                <div className="space-y-2">
                  <FieldLabel htmlFor="emergenciaParentesco" optional>
                    Parentesco
                  </FieldLabel>
                  <Input
                    id="emergenciaParentesco"
                    type="text"
                    placeholder="Cónyuge, hijo/a, padre/madre…"
                    autoComplete="off"
                    disabled={isLoading}
                    {...register('emergenciaParentesco')}
                  />
                  <FieldError message={errors.emergenciaParentesco?.message} />
                </div>
              </div>
            </div>
          )}

          {/* ============ PASO 5: CONFIRMACIÓN ============ */}
          {currentStep.id === 'confirmacion' && (
            <div className="space-y-4">
              <Alert className="border-yellow-200 bg-yellow-50">
                <AlertCircle className="h-4 w-4 text-yellow-600" />
                <AlertTitle className="text-yellow-900">Tu solicitud quedará pendiente de aprobación</AlertTitle>
                <AlertDescription className="text-yellow-800">
                  Un administrador la revisará y recibirás un correo cuando sea aprobada
                  o si necesitamos información adicional.
                </AlertDescription>
              </Alert>

              {/* Resumen rápido */}
              <div className="rounded-lg border bg-muted/30 p-4 space-y-1 text-sm">
                <p className="font-medium">Resumen</p>
                <p>
                  <span className="text-muted-foreground">Nombre:</span>{' '}
                  {getValues('nombreCompleto') || '—'}
                </p>
                <p>
                  <span className="text-muted-foreground">Cédula:</span>{' '}
                  {getValues('cedula') || '—'}
                </p>
                <p>
                  <span className="text-muted-foreground">Correo:</span>{' '}
                  {getValues('correoElectronico') || '—'}
                </p>
                <p>
                  <span className="text-muted-foreground">Empresa:</span>{' '}
                  {getValues('empresa') || '—'}
                </p>
              </div>

              <div className="space-y-3 pt-2">
                <Controller
                  control={control}
                  name="aceptaTerminos"
                  render={({ field }) => (
                    <div className="flex items-start gap-3">
                      <Checkbox
                        id="aceptaTerminos"
                        checked={field.value === true}
                        onCheckedChange={(c) => field.onChange(c === true)}
                        disabled={isLoading}
                        aria-invalid={!!errors.aceptaTerminos}
                      />
                      <Label htmlFor="aceptaTerminos" className="text-sm leading-tight">
                        Acepto los{' '}
                        <a href="/terminos" className="text-blue-600 hover:underline">
                          términos y condiciones
                        </a>
                        .
                      </Label>
                    </div>
                  )}
                />
                <FieldError message={errors.aceptaTerminos?.message} />

                <Controller
                  control={control}
                  name="aceptaLopdp"
                  render={({ field }) => (
                    <div className="flex items-start gap-3">
                      <Checkbox
                        id="aceptaLopdp"
                        checked={field.value === true}
                        onCheckedChange={(c) => field.onChange(c === true)}
                        disabled={isLoading}
                        aria-invalid={!!errors.aceptaLopdp}
                      />
                      <Label htmlFor="aceptaLopdp" className="text-sm leading-tight">
                        Acepto la{' '}
                        <a href="/lopdp" className="text-blue-600 hover:underline">
                          política de protección de datos personales
                        </a>{' '}
                        (LOPDP).
                      </Label>
                    </div>
                  )}
                />
                <FieldError message={errors.aceptaLopdp?.message} />
              </div>
            </div>
          )}

          {/* ============ NAVEGACIÓN ============ */}
          <div className="flex flex-col-reverse sm:flex-row gap-3 pt-4">
            {stepIndex > 0 && (
              <Button
                type="button"
                variant="outline"
                onClick={goBack}
                disabled={isLoading}
                className="sm:w-32"
              >
                <ArrowLeft className="mr-2 h-4 w-4" />
                Anterior
              </Button>
            )}

            {!isLastStep ? (
              <Button
                type="button"
                onClick={goNext}
                disabled={isLoading}
                className="flex-1 bg-green-600 hover:bg-green-700"
              >
                Continuar
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            ) : (
              <Button
                type="submit"
                disabled={isLoading}
                className="flex-1 bg-green-600 hover:bg-green-700"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Enviando…
                  </>
                ) : (
                  'Enviar solicitud'
                )}
              </Button>
            )}
          </div>
        </form>

        <div className="mt-6 text-center text-sm text-muted-foreground">
          ¿Ya tienes cuenta?{' '}
          <a href="/login" className="text-green-600 hover:underline">
            Inicia sesión
          </a>
        </div>
      </CardContent>
    </Card>
  );
}
