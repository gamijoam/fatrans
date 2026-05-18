import { describe, it, expect } from 'vitest';
import { parseMovimientosResponse } from './parse-movimientos-response';

/**
 * Tests para issue #212: parsing de la respuesta de movimientos del backend.
 *
 * Bug histórico relacionado (issue #213 sub-bug): el código del dashboard
 * hacía `Array.isArray(data) ? data : []` esperando un array directo, pero
 * el backend retorna `{movimientos: [...]}`. Este parser blinda al frontend
 * contra ese mismo error.
 */
describe('parseMovimientosResponse', () => {
  const movimiento = {
    id: 'mov-1',
    numeroOperacion: 'OP-001',
    tipo: 'DEPOSITO',
    monto: 500.0,
    descripcion: 'Depósito en efectivo',
    fechaMovimiento: '2026-05-16T10:30:00',
  };

  it('Formato real del backend `{movimientos: [...]}` → extrae el array', () => {
    const respuesta = {
      numeroCuenta: '0134-001',
      pagina: 0,
      tamanio: 10,
      totalElementos: 1,
      totalPaginas: 1,
      movimientos: [movimiento],
    };

    const result = parseMovimientosResponse(respuesta);

    expect(result).toHaveLength(1);
    expect(result[0].id).toBe('mov-1');
  });

  it('Array directo (futuro-proof) → lo retorna tal cual', () => {
    const result = parseMovimientosResponse([movimiento]);
    expect(result).toEqual([movimiento]);
  });

  it('Sin movimientos: wrap con array vacío → []', () => {
    const respuesta = {
      numeroCuenta: '0134-001',
      pagina: 0,
      tamanio: 10,
      totalElementos: 0,
      totalPaginas: 0,
      movimientos: [],
    };
    expect(parseMovimientosResponse(respuesta)).toEqual([]);
  });

  describe('Defensive (formas inesperadas → array vacío, no crash)', () => {
    it('null → []', () => {
      expect(parseMovimientosResponse(null)).toEqual([]);
    });

    it('undefined → []', () => {
      expect(parseMovimientosResponse(undefined)).toEqual([]);
    });

    it('string → []', () => {
      expect(parseMovimientosResponse('Error 500')).toEqual([]);
    });

    it('objeto sin `movimientos` → []', () => {
      expect(parseMovimientosResponse({ message: 'algo' })).toEqual([]);
    });

    it('objeto con `movimientos` no-array → []', () => {
      expect(parseMovimientosResponse({ movimientos: 'no array' })).toEqual([]);
    });
  });
});
