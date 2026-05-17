import { describe, it, expect } from 'vitest';
import { registroSchema, profileSchema, ESTADOS_VENEZUELA, BANCOS_VENEZUELA, TIPOS_DOCUMENTO, GENEROS, ESTADOS_CIVILES } from '@/lib/utils/validators';

describe('registroSchema - Validaciones Issue #99', () => {

  describe('1. Datos Personales', () => {

    it('nombreCompleto válido pasa validación', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez García',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test C.A.',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('nombreCompleto con menos de 3 caracteres falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Ju',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test C.A.',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

  });

  describe('2. Tipo Documento', () => {

    it('tipoDocumento válido CEDULA pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('tipoDocumento CEDULA_EXTRANJERO pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA_EXTRANJERO',
        cedula: 'E-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('tipoDocumento inválido falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'INVALID',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

  });

  describe('3. Validación de Cédula', () => {

    it('cédula V-12345678 válida', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('cédula E-12345678 válida', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'E-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('cédula E-1234567 válida (7 dígitos)', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'E-1234567',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('cédula sin prefijo falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: '12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

    it('cédula con formato J- falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'J-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

  });

  describe('4. Fecha de Nacimiento', () => {

    it('fecha pasada válida pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('fecha futura falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '2099-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

    // ===== Issue #204: Validación de mayoría de edad (≥18) =====

    /**
     * Formatea una fecha de cumpleaños relativa a hoy en formato "YYYY-MM-DD".
     * Helper para tests independientes del año en que se ejecutan.
     */
    const fechaConOffset = (anios: number, dias: number = 0): string => {
      const hoy = new Date();
      const f = new Date(hoy.getFullYear() - anios, hoy.getMonth(), hoy.getDate() + dias);
      const yyyy = f.getFullYear();
      const mm = String(f.getMonth() + 1).padStart(2, '0');
      const dd = String(f.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    };

    const baseValido = {
      nombreCompleto: 'Juan Pérez',
      tipoDocumento: 'CEDULA' as const,
      cedula: 'V-12345678',
      genero: 'MASCULINO' as const,
      estadoCivil: 'SOLTERO' as const,
      correoElectronico: 'juan@test.com',
      telefono: '04121234567',
      empresa: 'Empresa Test',
      aceptaTerminos: true as const,
      aceptaLopdp: true as const,
    };

    it('Issue #204: persona con exactamente 18 años recién cumplidos pasa', () => {
      const result = registroSchema.safeParse({
        ...baseValido,
        fechaNacimiento: fechaConOffset(18, 0),
      });
      expect(result.success).toBe(true);
    });

    it('Issue #204: persona con 17 años falla con mensaje claro', () => {
      const result = registroSchema.safeParse({
        ...baseValido,
        fechaNacimiento: fechaConOffset(17, 0),
      });
      expect(result.success).toBe(false);
      if (!result.success) {
        const issueFecha = result.error.issues.find(i => i.path.includes('fechaNacimiento'));
        expect(issueFecha?.message).toBe('Debes tener al menos 18 años para registrarte');
      }
    });

    it('Issue #204: persona con 17 años y 364 días falla (1 día antes del 18)', () => {
      const result = registroSchema.safeParse({
        ...baseValido,
        // Nacimiento mañana hace 18 años → todavía tiene 17 hoy
        fechaNacimiento: fechaConOffset(18, 1),
      });
      expect(result.success).toBe(false);
    });

    it('Issue #204: persona con 30 años pasa (caso típico)', () => {
      const result = registroSchema.safeParse({
        ...baseValido,
        fechaNacimiento: fechaConOffset(30, 0),
      });
      expect(result.success).toBe(true);
    });

  });

  describe('5. Género', () => {

    it('género MASCULINO pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('género inválido falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'OTRO_GENERO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

  });

  describe('6. Estado Civil', () => {

    it('estadoCivil SOLTERO pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('estadoCivil UNION_LIBRE pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'UNION_LIBRE',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

  });

  describe('7. RIF Empresa', () => {

    it('rifEmpresa J-123456789-0 válido pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        rifEmpresa: 'J-123456789-0',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('rifEmpresa V-12345678 válido pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        rifEmpresa: 'V-12345678',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('rifEmpresa vacío pasa como optional', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        rifEmpresa: '',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

  });

  describe('8. Información de Contacto', () => {

    it('email válido pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com.ve',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('email sin @ falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

    it('teléfono 04121234567 válido', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('teléfono con 11 dígitos válido', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '02121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('teléfono corto falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '123',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

  });

  describe('9. Consentimientos', () => {

    it('aceptaTerminos true pasa', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('aceptaTerminos false falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: false,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(false);
    });

    it('aceptaLopdp false falla', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        aceptaTerminos: true,
        aceptaLopdp: false,
      });
      expect(result.success).toBe(false);
    });

  });

  describe('10. Campos Opcionales de Dirección', () => {

    it('campos de dirección opcionales pasan', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        direccionEstado: 'Caracas',
        direccionCiudad: 'Distrito Capital',
        direccionMunicipio: 'Libertador',
        direccionCalle: 'Av. Principal #123',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

    it('direccionEstado debe ser estado Venezuela válido', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        direccionEstado: 'Zulia',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

  });

  describe('11. Contacto de Emergencia', () => {

    it('campos de emergencia opcionales pasan', () => {
      const result = registroSchema.safeParse({
        nombreCompleto: 'Juan Pérez',
        tipoDocumento: 'CEDULA',
        cedula: 'V-12345678',
        fechaNacimiento: '1990-01-15',
        genero: 'MASCULINO',
        estadoCivil: 'SOLTERO',
        correoElectronico: 'juan@test.com',
        telefono: '04121234567',
        empresa: 'Empresa Test',
        emergenciaNombre: 'María García',
        emergenciaTelefono: '04121234567',
        emergenciaParentesco: 'Cónyuge',
        aceptaTerminos: true,
        aceptaLopdp: true,
      });
      expect(result.success).toBe(true);
    });

  });

  describe('12. Constantes', () => {

    it('ESTADOS_VENEZUELA tiene 24 estados', () => {
      expect(ESTADOS_VENEZUELA.length).toBe(24);
      expect(ESTADOS_VENEZUELA).toContain('Zulia');
      expect(ESTADOS_VENEZUELA).toContain('Distrito Capital');
    });

    it('TIPOS_DOCUMENTO tiene 4 tipos', () => {
      expect(TIPOS_DOCUMENTO.length).toBe(4);
      expect(TIPOS_DOCUMENTO).toContain('CEDULA');
      expect(TIPOS_DOCUMENTO).toContain('CEDULA_EXTRANJERO');
    });

    it('GENEROS tiene 3 géneros', () => {
      expect(GENEROS.length).toBe(3);
      expect(GENEROS).toContain('MASCULINO');
      expect(GENEROS).toContain('FEMENINO');
      expect(GENEROS).toContain('OTRO');
    });

    it('ESTADOS_CIVILES tiene 5 estados', () => {
      expect(ESTADOS_CIVILES.length).toBe(5);
      expect(ESTADOS_CIVILES).toContain('SOLTERO');
      expect(ESTADOS_CIVILES).toContain('CASADO');
    });

    it('BANCOS_VENEZUELA tiene bancos válidos', () => {
      expect(BANCOS_VENEZUELA.length).toBeGreaterThan(0);
      expect(BANCOS_VENEZUELA[0]).toHaveProperty('codigo');
      expect(BANCOS_VENEZUELA[0]).toHaveProperty('nombre');
      const bancoVenezuela = BANCOS_VENEZUELA.find(b => b.codigo === '0102');
      expect(bancoVenezuela?.nombre).toBe('Banco de Venezuela');
    });

  });

});

describe('profileSchema - Validaciones Issue #103', () => {

  describe('1. Teléfono Venezuela', () => {

    it('teléfono 04121234567 válido', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
      });
      expect(result.success).toBe(true);
    });

    it('teléfono +584121234567 válido', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '+584121234567',
      });
      expect(result.success).toBe(true);
    });

    it('teléfono corto falla', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '123',
      });
      expect(result.success).toBe(false);
    });

  });

  describe('2. Código Postal Venezuela', () => {

    it('código postal 4 dígitos válido', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        direccionResidencia: { codigoPostal: '1010' },
      });
      expect(result.success).toBe(true);
    });

    it('código postal con letras falla', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        direccionResidencia: { codigoPostal: '1010A' },
      });
      expect(result.success).toBe(false);
    });

    it('código postal con más de 4 dígitos falla', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        direccionResidencia: { codigoPostal: '10101' },
      });
      expect(result.success).toBe(false);
    });

  });

  describe('3. RIF Empresa', () => {

    it('RIF J-12345678-9 válido', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        rifEmpresa: 'J-12345678-9',
      });
      expect(result.success).toBe(true);
    });

    it('RIF V-12345678 válido', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        rifEmpresa: 'V-12345678',
      });
      expect(result.success).toBe(true);
    });

    it('RIF sin prefijo falla', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        rifEmpresa: '12345678',
      });
      expect(result.success).toBe(false);
    });

  });

  describe('4. Número de Cuenta', () => {

    it('cuenta 10 dígitos válida', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        numeroCuentaNomina: '0102123456',
      });
      expect(result.success).toBe(true);
    });

    it('cuenta 20 dígitos válida', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        numeroCuentaNomina: '01021234567890123456',
      });
      expect(result.success).toBe(true);
    });

    it('cuenta con letras falla', () => {
      const result = profileSchema.safeParse({
        primerNombre: 'Juan',
        primerApellido: 'Pérez',
        correoElectronico: 'juan@test.com',
        telefonoPrincipal: '04121234567',
        numeroCuentaNomina: '0102ABCD123456',
      });
      expect(result.success).toBe(false);
    });

  });

});