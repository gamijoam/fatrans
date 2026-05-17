import { describe, it, expect } from 'vitest';
import {
  parseBeneficiariosResponse,
  beneficiariosActivosOrdenados,
  sumaPorcentajes,
  type BeneficiarioApi,
} from './parse-beneficiarios-response';

/**
 * Tests para issue #212: parsing de la respuesta de beneficiarios reales.
 *
 * Reemplaza al `mockBeneficiarios` hardcoded (María García, Juan Pérez, Ana
 * López) que aparecía en el home aunque el socio no tuviera ninguno.
 */
describe('parseBeneficiariosResponse', () => {
  const beneficiario: BeneficiarioApi = {
    id: 'b-1',
    socioId: 's-1',
    nombreCompleto: 'María García',
    porcentaje: 60,
    activo: true,
  };

  it('Formato del BFF `{beneficiarios: [...], total: N}` → extrae el array', () => {
    const respuesta = { beneficiarios: [beneficiario], total: 1 };
    const result = parseBeneficiariosResponse(respuesta);
    expect(result).toHaveLength(1);
    expect(result[0].nombreCompleto).toBe('María García');
  });

  it('BFF cuando socio NO tiene beneficiarios → `{beneficiarios: [], total: 0}` → []', () => {
    // El BFF (route.ts:34-36) convierte el 404 del backend en lista vacía.
    const result = parseBeneficiariosResponse({ beneficiarios: [], total: 0 });
    expect(result).toEqual([]);
  });

  it('Array directo (futuro-proof) → lo retorna tal cual', () => {
    expect(parseBeneficiariosResponse([beneficiario])).toEqual([beneficiario]);
  });

  describe('Defensive', () => {
    it('null → []', () => {
      expect(parseBeneficiariosResponse(null)).toEqual([]);
    });

    it('objeto sin `beneficiarios` → []', () => {
      expect(parseBeneficiariosResponse({ otroCampo: 1 })).toEqual([]);
    });

    it('objeto con `beneficiarios` no-array → []', () => {
      expect(parseBeneficiariosResponse({ beneficiarios: null })).toEqual([]);
    });
  });
});

describe('beneficiariosActivosOrdenados', () => {
  it('filtra inactivos y ordena por porcentaje descendente', () => {
    const lista: BeneficiarioApi[] = [
      { id: '1', socioId: 's', nombreCompleto: 'Ana', porcentaje: 10, activo: true },
      { id: '2', socioId: 's', nombreCompleto: 'Carlos (inactivo)', porcentaje: 100, activo: false },
      { id: '3', socioId: 's', nombreCompleto: 'Juan', porcentaje: 60, activo: true },
    ];

    const result = beneficiariosActivosOrdenados(lista);

    expect(result).toHaveLength(2); // Carlos filtrado por inactivo
    expect(result[0].nombreCompleto).toBe('Juan');   // 60% primero
    expect(result[1].nombreCompleto).toBe('Ana');    // 10% segundo
  });

  it('considera `activo` ausente como activo (defensive con responses parciales)', () => {
    const lista: BeneficiarioApi[] = [
      { id: '1', socioId: 's', nombreCompleto: 'X', porcentaje: 50 }, // sin activo
    ];
    expect(beneficiariosActivosOrdenados(lista)).toHaveLength(1);
  });

  it('porcentaje como string (BigDecimal serializado) ordena correctamente', () => {
    const lista: BeneficiarioApi[] = [
      { id: '1', socioId: 's', nombreCompleto: 'Bajo', porcentaje: '10', activo: true },
      { id: '2', socioId: 's', nombreCompleto: 'Alto', porcentaje: '70', activo: true },
    ];

    const result = beneficiariosActivosOrdenados(lista);
    expect(result[0].nombreCompleto).toBe('Alto');
  });
});

describe('sumaPorcentajes', () => {
  it('suma porcentajes solo de activos', () => {
    const lista: BeneficiarioApi[] = [
      { id: '1', socioId: 's', nombreCompleto: 'A', porcentaje: 60, activo: true },
      { id: '2', socioId: 's', nombreCompleto: 'B', porcentaje: 30, activo: true },
      { id: '3', socioId: 's', nombreCompleto: 'C (inactivo)', porcentaje: 999, activo: false },
    ];
    expect(sumaPorcentajes(lista)).toBe(90);
  });

  it('lista vacía → 0', () => {
    expect(sumaPorcentajes([])).toBe(0);
  });

  it('Issue #212: socio nuevo sin beneficiarios reales → 0 (vs el "100%" hardcoded del mock)', () => {
    // El mock anterior siempre mostraba "Total asignado: 100%" aunque no
    // hubiera beneficiarios reales. Esto verifica que ahora calculamos
    // honestamente.
    expect(sumaPorcentajes([])).toBe(0);
    expect(sumaPorcentajes([])).not.toBe(100);
  });

  it('porcentaje como string suma correctamente', () => {
    const lista: BeneficiarioApi[] = [
      { id: '1', socioId: 's', nombreCompleto: 'A', porcentaje: '50.5', activo: true },
      { id: '2', socioId: 's', nombreCompleto: 'B', porcentaje: '20.0', activo: true },
    ];
    expect(sumaPorcentajes(lista)).toBeCloseTo(70.5);
  });
});
