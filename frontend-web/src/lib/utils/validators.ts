import { z } from 'zod';

export const loginSchema = z.object({
  identificador: z
    .string()
    .min(3, 'El identificador debe tener al menos 3 caracteres')
    .max(100, 'El identificador no puede exceder 100 caracteres')
    .transform((val) => val.trim()),
  password: z
    .string()
    .min(8, 'La contraseña debe tener al menos 8 caracteres')
    .max(128, 'La contraseña no puede exceder 128 caracteres'),
});

export const loginErrorMessages = {
  identificador: {
    required: 'Por favor ingresa tu usuario o correo electrónico',
    minLength: 'El usuario debe tener al menos 3 caracteres',
    maxLength: 'El usuario no puede exceder 100 caracteres',
  },
  password: {
    required: 'Por favor ingresa tu contraseña',
    minLength: 'La contraseña debe tener al menos 8 caracteres',
  },
} as const;

const cedulaRegex = /^(V|E)-\d{7,8}$|^\d{7,8}$/;
const phoneRegex = /^\d{10,11}$/;
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const registroSchema = z.object({
  nombreCompleto: z
    .string()
    .min(3, 'El nombre debe tener al menos 3 caracteres')
    .max(200, 'El nombre no puede exceder 200 caracteres')
    .transform((val) => val.trim()),
  cedula: z
    .string()
    .regex(cedulaRegex, 'Cédula inválida. Formato: V-12345678 o 12345678'),
  correoElectronico: z
    .string()
    .regex(emailRegex, 'Correo electrónico inválido'),
  telefono: z
    .string()
    .regex(phoneRegex, 'Teléfono inválido. Debe tener 10-11 dígitos'),
  empresa: z
    .string()
    .min(2, 'El nombre de la empresa debe tener al menos 2 caracteres')
    .max(200, 'El nombre de la empresa no puede exceder 200 caracteres'),
});

export type RegistroFormData = z.infer<typeof registroSchema>;

export const moneySchema = z.object({
  monto: z.string().refine(
    (val) => {
      const num = parseFloat(val.replace(/[^\d.,]/g, '').replace(',', '.'));
      return !isNaN(num) && num >= 0.01 && num <= 500000;
    },
    { message: 'Monto inválido (0.01 - 500,000)' }
  ),
});

export const beneficiarioSchema = z.object({
  nombreCompleto: z.string().min(3, 'Mínimo 3 caracteres').max(200),
  tipoDocumento: z.enum(['CEDULA_IDENTIDAD', 'RIF', 'PASAPORTE', 'CEDULA_EXTRANJERO']),
  numeroDocumento: z.string().min(5, 'Mínimo 5 caracteres'),
  parentesco: z.enum(['CONYUGE', 'HIJO', 'PADRE', 'MADRE', 'HERMANO', 'ABUELO', 'NIETO', 'SOBRINO', 'TIO', 'OTRO']),
  porcentaje: z.string().refine(
    (val) => {
      const num = parseFloat(val);
      return !isNaN(num) && num >= 0.01 && num <= 100;
    },
    { message: 'Porcentaje inválido (0.01 - 100)' }
  ),
  telefono: z.string().optional(),
});

const direccionSchema = z.object({
  calle: z.string().max(200).optional(),
  ciudad: z.string().max(100).optional(),
  estado: z.string().max(100).optional(),
  codigoPostal: z.string().max(20).optional(),
  pais: z.string().max(100).optional(),
});

const contactoEmergenciaSchema = z.object({
  nombre: z.string().min(2, 'Mínimo 2 caracteres').max(200),
  telefono: z.string().regex(/^\+?[0-9]{7,15}$/, 'Teléfono inválido'),
  parentesco: z.string().max(100).optional(),
});

export const profileSchema = z.object({
  primerNombre: z.string().min(2, 'Mínimo 2 caracteres').max(50),
  segundoNombre: z.string().max(50).optional().or(z.literal('')),
  primerApellido: z.string().min(2, 'Mínimo 2 caracteres').max(50),
  segundoApellido: z.string().max(50).optional().or(z.literal('')),
  fechaNacimiento: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Fecha inválida').optional(),
  genero: z.enum(['MASCULINO', 'FEMENINO', 'OTRO']).optional(),
  estadoCivil: z.enum(['SOLTERO', 'CASADO', 'DIVORCIADO', 'VIUDO', 'UNION_LIBRE']).optional(),
  correoElectronico: z.string().email('Email inválido'),
  telefonoPrincipal: z.string().regex(/^\+?[0-9]{7,15}$/, 'Teléfono inválido'),
  telefonoSecundario: z.string().regex(/^\+?[0-9]{7,15}$/, 'Teléfono inválido').optional().or(z.literal('')),
  direccionResidencia: direccionSchema.optional(),
  direccionLaboral: direccionSchema.optional(),
  empresa: z.string().max(200).optional().or(z.literal('')),
  departamento: z.string().max(100).optional().or(z.literal('')),
  cargo: z.string().max(100).optional().or(z.literal('')),
  tipoContrato: z.enum(['CONTRATO_INDEFINIDO', 'CONTRATO_TEMPORAL', 'CONTRATO_POR_HORAS', 'SERVICIOS', 'APRENDIZ']).optional(),
  numeroCuentaNomina: z.string().regex(/^[0-9]{10,20}$/, 'Número de cuenta inválido').optional().or(z.literal('')),
  bancoNomina: z.string().max(100).optional().or(z.literal('')),
  contactoEmergencia: contactoEmergenciaSchema.optional(),
});

export type ProfileFormData = z.infer<typeof profileSchema>;

export const changePasswordSchema = z.object({
  passwordActual: z.string().min(1, 'Contraseña actual requerida'),
  nuevoPassword: z
    .string()
    .min(8, 'La nueva contraseña debe tener al menos 8 caracteres')
    .max(128, 'La contraseña no puede exceder 128 caracteres'),
  confirmarPassword: z.string(),
}).refine((data) => data.nuevoPassword === data.confirmarPassword, {
  message: 'Las contraseñas no coinciden',
  path: ['confirmarPassword'],
});

export type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;
