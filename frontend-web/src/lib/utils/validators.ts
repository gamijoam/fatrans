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

const cedulaRegex = /^(V|E)-\d{7,8}$/;
const rifRegex = /^(J|V|E|G)-\d{6,10}(-\d)?$/;
const phoneRegex = /^\d{10,11}$/;
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const ESTADOS_VENEZUELA = [
  'Amazonas', 'Anzoátegui', 'Apure', 'Aragua', 'Barinas', 'Bolívar',
  'Carabobo', 'Cojedes', 'Delta Amacuro', 'Distrito Capital', 'Falcón',
  'Guárico', 'Lara', 'Mérida', 'Miranda', 'Monagas', 'Nueva Esparta',
  'Portuguesa', 'Sucre', 'Táchira', 'Trujillo', 'La Guaira', 'Yaracuy', 'Zulia'
] as const;

export const BANCOS_VENEZUELA = [
  { codigo: '0102', nombre: 'Banco de Venezuela' },
  { codigo: '0104', nombre: 'Banco Provincial' },
  { codigo: '0105', nombre: 'Banco de Guyana' },
  { codigo: '0114', nombre: 'Banco del Caribe' },
  { codigo: '0128', nombre: 'Banco Caroní' },
  { codigo: '0134', nombre: 'Banco Bicentenario' },
  { codigo: '0137', nombre: 'Banco Sofitasa' },
  { codigo: '0138', nombre: 'Banco Plaza' },
  { codigo: '0151', nombre: 'Banco Fondo Común' },
  { codigo: '0156', nombre: 'Banco 100% Banco' },
  { codigo: '0157', nombre: 'Banco Digital' },
  { codigo: '0163', nombre: 'Banco del Tesoro' },
  { codigo: '0166', nombre: 'Banco Agro' },
  { codigo: '0171', nombre: 'Banco Activo' },
  { codigo: '0174', nombre: 'Banco Bancrecer' },
  { codigo: '0175', nombre: 'Banco Internacional de Desarrollo' },
  { codigo: '0177', nombre: 'Banco Banplus' },
  { codigo: '0181', nombre: 'Banco Venezuelan' },
  { codigo: '0186', nombre: 'Banco Mi Banco' },
  { codigo: '0190', nombre: 'Banco Nacional de Crédito' },
  { codigo: '0601', nombre: 'Banco de la Fuerza Armada' },
] as const;

const codigoPostalRegex = /^\d{4}$/;
const telefonoVenezuelRegex = /^(\+58)?[0-9]{10,11}$/;

export const TIPOS_DOCUMENTO = ['CEDULA', 'CEDULA_EXTRANJERO', 'PASAPORTE', 'RIF'] as const;
export const GENEROS = ['MASCULINO', 'FEMENINO', 'OTRO'] as const;
export const ESTADOS_CIVILES = ['SOLTERO', 'CASADO', 'DIVORCIADO', 'VIUDO', 'UNION_LIBRE'] as const;

export const registroSchema = z.object({
  nombreCompleto: z
    .string()
    .min(3, 'El nombre debe tener al menos 3 caracteres')
    .max(200, 'El nombre no puede exceder 200 caracteres')
    .transform((val) => val.trim()),
  tipoDocumento: z.enum(TIPOS_DOCUMENTO, { required_error: 'El tipo de documento es obligatorio' }),
  cedula: z
    .string()
    .regex(cedulaRegex, 'Cédula inválida. Formato: V-12345678 o E-12345678'),
  fechaNacimiento: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Fecha inválida. Formato: YYYY-MM-DD')
    .refine((val) => {
      const date = new Date(val);
      const now = new Date();
      return date < now;
    }, 'La fecha de nacimiento debe ser pasada'),
  genero: z.enum(GENEROS, { required_error: 'El género es obligatorio' }),
  estadoCivil: z.enum(ESTADOS_CIVILES, { required_error: 'El estado civil es obligatorio' }),
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
  rifEmpresa: z
    .string()
    .max(20, 'El RIF no puede exceder 20 caracteres')
    .optional()
    .or(z.literal('')),
  departamento: z
    .string()
    .max(100, 'El departamento no puede exceder 100 caracteres')
    .optional()
    .or(z.literal('')),
  cargo: z
    .string()
    .max(100, 'El cargo no puede exceder 100 caracteres')
    .optional()
    .or(z.literal('')),
  salario: z
    .string()
    .regex(/^\d+([.,]\d{1,2})?$/, 'Salario inválido')
    .optional()
    .or(z.literal('')),
  direccionEstado: z
    .string()
    .max(100, 'El estado no puede exceder 100 caracteres')
    .optional()
    .or(z.literal('')),
  direccionCiudad: z
    .string()
    .max(100, 'La ciudad no puede exceder 100 caracteres')
    .optional()
    .or(z.literal('')),
  direccionMunicipio: z
    .string()
    .max(100, 'El municipio no puede exceder 100 caracteres')
    .optional()
    .or(z.literal('')),
  direccionCalle: z
    .string()
    .max(255, 'La calle no puede exceder 255 caracteres')
    .optional()
    .or(z.literal('')),
  emergenciaNombre: z
    .string()
    .min(2, 'El nombre debe tener al menos 2 caracteres')
    .max(200, 'El nombre no puede exceder 200 caracteres')
    .optional()
    .or(z.literal('')),
  emergenciaTelefono: z
    .string()
    .regex(phoneRegex, 'Teléfono inválido. Debe tener 10-11 dígitos')
    .optional()
    .or(z.literal('')),
  emergenciaParentesco: z
    .string()
    .max(50, 'El parentesco no puede exceder 50 caracteres')
    .optional()
    .or(z.literal('')),
  aceptaTerminos: z.literal(true, {
    errorMap: () => ({ message: 'Debe aceptar los términos y condiciones' }),
  }),
  aceptaLopdp: z.literal(true, {
    errorMap: () => ({ message: 'Debe aceptar la política de protección de datos' }),
  }),
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
  estado: z.string().max(50).optional(),
  codigoPostal: z.string().regex(codigoPostalRegex, 'Código postal debe ser 4 dígitos').optional().or(z.literal('')),
  pais: z.string().max(100).optional(),
});

const contactoEmergenciaSchema = z.object({
  nombre: z.string().min(2, 'Mínimo 2 caracteres').max(200),
  telefono: z.string().regex(telefonoVenezuelRegex, 'Teléfono inválido'),
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
  telefonoPrincipal: z.string().regex(telefonoVenezuelRegex, 'Teléfono inválido (formato: 04121234567 o +584121234567)'),
  telefonoSecundario: z.string().regex(telefonoVenezuelRegex, 'Teléfono inválido').optional().or(z.literal('')),
  direccionResidencia: direccionSchema.optional(),
  direccionLaboral: direccionSchema.optional(),
  empresa: z.string().max(200).optional().or(z.literal('')),
  rifEmpresa: z.string().regex(/^(J|V|E|G)-\d{6,10}(-\d)?$/, 'RIF inválido').optional().or(z.literal('')),
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
    .max(128, 'La contraseña no puede exceder 128 caracteres')
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/,
      'Debe incluir mayúsculas, minúsculas, números y caracteres especiales'
    ),
  confirmarPassword: z.string(),
}).refine((data) => data.nuevoPassword === data.confirmarPassword, {
  message: 'Las contraseñas no coinciden',
  path: ['confirmarPassword'],
});

export type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;
