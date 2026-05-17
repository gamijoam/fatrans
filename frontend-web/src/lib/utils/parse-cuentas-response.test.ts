import { describe, it, expect } from 'vitest';
import { parseCuentasResponse } from './parse-cuentas-response';

/**
 * Tests para issue #213 — sub-bug descubierto en QA:
 * `Array.isArray(data) ? data : []` siempre caía a `[]` porque el backend
 * retorna `{cuentas: [...]}`, no un array directo.
 */
describe('parseCuentasResponse', () => {
  const cuentaSample = {
    id: 'a1',
    numeroCuenta: '0134-001',
    tipoCuenta: 'CORRIENTE',
    moneda: 'VES',
    saldoActual: 12450.5,
    estado: 'ACTIVA',
  };

  describe('Caso real del backend (issue #213 sub-bug)', () => {
    it('formato `{socioId, totalCuentas, cuentas: [...]}` (CuentasPorSocioResponse) → extrae las cuentas', () => {
      const respuestaBackend = {
        socioId: '550e8400-e29b-41d4-a716-446655440000',
        totalCuentas: 2,
        cuentas: [cuentaSample, { ...cuentaSample, id: 'a2', moneda: 'USD', saldoActual: 500 }],
      };

      const result = parseCuentasResponse(respuestaBackend);

      // Caso crítico: NO debe retornar [] (que era el bug original)
      expect(result).toHaveLength(2);
      expect(result[0].moneda).toBe('VES');
      expect(result[1].moneda).toBe('USD');
    });

    it('regresión del bug exacto reportado: respuesta wrap con 2 cuentas reales', () => {
      // Réplica exacta del QA: 1 cuenta VES 12.450,50 + 1 cuenta USD 500
      const respuestaBackend = {
        socioId: '550e8400-e29b-41d4-a716-446655440000',
        totalCuentas: 2,
        cuentas: [
          { id: 'c1', numeroCuenta: '01340001000000005678', tipoCuenta: 'CORRIENTE',
            moneda: 'VES', saldoActual: 12450.5, estado: 'ACTIVA' },
          { id: 'c2', numeroCuenta: '01340001000000009999', tipoCuenta: 'CORRIENTE',
            moneda: 'USD', saldoActual: 500, estado: 'ACTIVA' },
        ],
      };

      const result = parseCuentasResponse(respuestaBackend);

      expect(result).toHaveLength(2);
      // Verificamos que el bug NO retorna []
      expect(result).not.toEqual([]);
    });
  });

  describe('Formato alternativo (array directo, por si el backend cambia)', () => {
    it('array directo → lo retorna tal cual', () => {
      const respuesta = [cuentaSample];
      const result = parseCuentasResponse(respuesta);
      expect(result).toEqual([cuentaSample]);
    });

    it('array vacío directo → array vacío', () => {
      const result = parseCuentasResponse([]);
      expect(result).toEqual([]);
    });
  });

  describe('Defensive (formas inesperadas no deben romper la UI)', () => {
    it('null → array vacío', () => {
      expect(parseCuentasResponse(null)).toEqual([]);
    });

    it('undefined → array vacío', () => {
      expect(parseCuentasResponse(undefined)).toEqual([]);
    });

    it('string → array vacío', () => {
      expect(parseCuentasResponse('error message')).toEqual([]);
    });

    it('objeto sin `cuentas` → array vacío', () => {
      expect(parseCuentasResponse({ message: 'Error', code: 500 })).toEqual([]);
    });

    it('objeto con `cuentas` que NO es array → array vacío', () => {
      expect(parseCuentasResponse({ cuentas: 'no es array' })).toEqual([]);
    });

    it('objeto con `cuentas` vacío → array vacío', () => {
      expect(parseCuentasResponse({ cuentas: [] })).toEqual([]);
    });
  });
});
