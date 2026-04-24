import { describe, it, expect } from 'vitest';
import {
  validateAdminAccess,
  validarNumeroSolicitud,
  sanitizarTexto,
  esNumeroValido,
} from '@/lib/auth/admin-validation';

function createMockToken(payload: object): string {
  const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64');
  const payloadEncoded = Buffer.from(JSON.stringify(payload)).toString('base64');
  const signature = 'mock-signature';
  return `${header}.${payloadEncoded}.${signature}`;
}

describe('admin-validation', () => {
  describe('validateAdminAccess', () => {
    it('debe retornar 401 cuando token es undefined', () => {
      const result = validateAdminAccess({ accessToken: undefined });
      expect(result.valid).toBe(false);
      expect(result.status).toBe(401);
      expect(result.message).toBe('No autenticado');
    });

    it('debe retornar 401 cuando token no tiene formato JWT', () => {
      const result = validateAdminAccess({ accessToken: 'not-a-jwt-token' });
      expect(result.valid).toBe(false);
      expect(result.status).toBe(401);
      expect(result.message).toBe('Token inválido');
    });

    it('debe retornar 403 cuando rol no es admin', () => {
      const token = createMockToken({ rol: 'SOCIO', sub: 'user-123' });
      const result = validateAdminAccess({ accessToken: token });
      expect(result.valid).toBe(false);
      expect(result.status).toBe(403);
      expect(result.message).toBe('No autorizado');
    });

    it('debe retornar 403 cuando rol es CAJERO', () => {
      const token = createMockToken({ rol: 'CAJERO', sub: 'user-123' });
      const result = validateAdminAccess({ accessToken: token });
      expect(result.valid).toBe(false);
      expect(result.status).toBe(403);
    });

    it('debe retornar 200 cuando rol es ADMIN', () => {
      const token = createMockToken({ rol: 'ADMIN', sub: 'admin-123' });
      const result = validateAdminAccess({ accessToken: token });
      expect(result.valid).toBe(true);
      expect(result.status).toBe(200);
      expect(result.message).toBe('OK');
    });

    it('debe retornar 200 cuando rol es ADMINISTRADOR', () => {
      const token = createMockToken({ rol: 'ADMINISTRADOR', sub: 'admin-123' });
      const result = validateAdminAccess({ accessToken: token });
      expect(result.valid).toBe(true);
      expect(result.status).toBe(200);
    });

    it('debe retornar 200 cuando rol es GESTOR', () => {
      const token = createMockToken({ rol: 'GESTOR', sub: 'gestor-123' });
      const result = validateAdminAccess({ accessToken: token });
      expect(result.valid).toBe(true);
      expect(result.status).toBe(200);
    });

    it('debe retornar 200 cuando rol es SUPER_ADMIN', () => {
      const token = createMockToken({ rol: 'SUPER_ADMIN', sub: 'super-123' });
      const result = validateAdminAccess({ accessToken: token });
      expect(result.valid).toBe(true);
      expect(result.status).toBe(200);
    });

    it('debe retornar 401 cuando token tiene payload inválido', () => {
      const token = 'invalid-token';
      const result = validateAdminAccess({ accessToken: token });
      expect(result.valid).toBe(false);
      expect(result.status).toBe(401);
    });
  });

  describe('validarNumeroSolicitud', () => {
    it('debe retornar true para formato válido SC-2024-00001', () => {
      expect(validarNumeroSolicitud('SC-2024-00001')).toBe(true);
    });

    it('debe retornar true para formato CRED-123456', () => {
      expect(validarNumeroSolicitud('CRED-123456')).toBe(true);
    });

    it('debe retornar true para número corto de 5 caracteres', () => {
      expect(validarNumeroSolicitud('ABC12')).toBe(true);
    });

    it('debe retornar false para cadena muy corta', () => {
      expect(validarNumeroSolicitud('ABC')).toBe(false);
    });

    it('debe retornar false para cadena vacía', () => {
      expect(validarNumeroSolicitud('')).toBe(false);
    });

    it('debe retornar false para cadena con caracteres inválidos', () => {
      expect(validarNumeroSolicitud('SC 2024 00001')).toBe(false);
      expect(validarNumeroSolicitud('SC_2024_00001')).toBe(false);
      expect(validarNumeroSolicitud('SC@2024')).toBe(false);
    });

    it('debe retornar false para cadena que excede 30 caracteres', () => {
      const longString = 'A'.repeat(31);
      expect(validarNumeroSolicitud(longString)).toBe(false);
    });

    it('debe retornar true para cadena de exactamente 30 caracteres', () => {
      const exactString = 'A'.repeat(30);
      expect(validarNumeroSolicitud(exactString)).toBe(true);
    });

    it('debe retornar false para números con puntos', () => {
      expect(validarNumeroSolicitud('SC.2024.00001')).toBe(false);
    });
  });

  describe('sanitizarTexto', () => {
    it('debe eliminar caracteres peligrosos', () => {
      expect(sanitizarTexto('<script>alert("xss")</script>')).toBe('scriptalert(xss)/script');
      expect(sanitizarTexto('"texto con comillas"')).toBe('texto con comillas');
      expect(sanitizarTexto("'texto con comillas simples'")).toBe('texto con comillas simples');
    });

    it('debe retornar string vacío para valores no-string', () => {
      expect(sanitizarTexto(123 as unknown as string)).toBe('');
      expect(sanitizarTexto(null as unknown as string)).toBe('');
      expect(sanitizarTexto(undefined as unknown as string)).toBe('');
      expect(sanitizarTexto({} as unknown as string)).toBe('');
    });

    it('debe mantener texto sin caracteres peligrosos', () => {
      expect(sanitizarTexto('Texto normal sin peligro')).toBe('Texto normal sin peligro');
    });

    it('debe retornar string vacío para strings vacíos', () => {
      expect(sanitizarTexto('')).toBe('');
    });
  });

  describe('esNumeroValido', () => {
    it('debe retornar true para números válidos', () => {
      expect(esNumeroValido(0)).toBe(true);
      expect(esNumeroValido(1)).toBe(true);
      expect(esNumeroValido(-1)).toBe(true);
      expect(esNumeroValido(1.5)).toBe(true);
      expect(esNumeroValido(0.001)).toBe(true);
    });

    it('debe retornar false para NaN', () => {
      expect(esNumeroValido(NaN)).toBe(false);
    });

    it('debe retornar false para Infinity', () => {
      expect(esNumeroValido(Infinity)).toBe(false);
      expect(esNumeroValido(-Infinity)).toBe(false);
    });

    it('debe retornar false para strings', () => {
      expect(esNumeroValido('1' as unknown as number)).toBe(false);
      expect(esNumeroValido('0' as unknown as number)).toBe(false);
    });

    it('debe retornar false para null y undefined', () => {
      expect(esNumeroValido(null as unknown as number)).toBe(false);
      expect(esNumeroValido(undefined as unknown as number)).toBe(false);
    });

    it('debe retornar false para objetos', () => {
      expect(esNumeroValido({} as unknown as number)).toBe(false);
      expect(esNumeroValido([] as unknown as number)).toBe(false);
    });
  });
});