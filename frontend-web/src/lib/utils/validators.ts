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
