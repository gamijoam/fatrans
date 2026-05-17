import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  leerSaldoOculto,
  guardarSaldoOculto,
  aplicarOcultarSaldo,
} from './saldo-oculto-storage';

/**
 * Tests para issue #219: persistencia de la preferencia "ocultar saldo"
 * en localStorage. Utility puro testeable sin React.
 */
describe('saldo-oculto-storage (issue #219)', () => {

  beforeEach(() => {
    // Limpiar localStorage antes de cada test
    if (typeof window !== 'undefined') {
      window.localStorage?.clear();
    }
  });

  describe('aplicarOcultarSaldo (función pura, sin localStorage)', () => {
    it('saldo visible → retorna texto original sin modificar', () => {
      expect(aplicarOcultarSaldo('Bs 12.450,50', false)).toBe('Bs 12.450,50');
    });

    it('saldo oculto → retorna placeholder de puntos', () => {
      expect(aplicarOcultarSaldo('Bs 12.450,50', true)).toBe('••••••');
    });

    it('Issue #219: el texto OCULTO no debe revelar la magnitud del saldo', () => {
      // Patrón crítico de seguridad: el placeholder debe ser CONSTANTE,
      // no proporcional al número de dígitos.
      const saldoChico = aplicarOcultarSaldo('Bs 1,00', true);
      const saldoGrande = aplicarOcultarSaldo('Bs 999.999.999,99', true);

      expect(saldoChico).toBe(saldoGrande);
      // Y NUNCA debe contener dígitos
      expect(saldoChico).not.toMatch(/[0-9]/);
    });
  });

  describe('Persistencia en localStorage', () => {
    it('valor por defecto (nunca guardado) → saldo VISIBLE (false)', () => {
      expect(leerSaldoOculto()).toBe(false);
    });

    it('guardar true → leer retorna true', () => {
      guardarSaldoOculto(true);
      expect(leerSaldoOculto()).toBe(true);
    });

    it('guardar false → leer retorna false', () => {
      guardarSaldoOculto(true);
      guardarSaldoOculto(false);
      expect(leerSaldoOculto()).toBe(false);
    });

    it('Issue #219: la preferencia persiste entre simulaciones de recarga', () => {
      guardarSaldoOculto(true);
      // Simulamos una "recarga" leyendo de nuevo del storage real
      const oculto = leerSaldoOculto();
      expect(oculto).toBe(true);
    });
  });

  describe('Defensive (SSR / private mode / quota)', () => {
    it('si localStorage lanza al leer → retorna false (no rompe la UI)', () => {
      const original = window.localStorage.getItem;
      window.localStorage.getItem = vi.fn(() => {
        throw new Error('Private mode');
      });

      expect(leerSaldoOculto()).toBe(false);

      window.localStorage.getItem = original;
    });

    it('si localStorage lanza al guardar → no rompe (silent)', () => {
      const original = window.localStorage.setItem;
      window.localStorage.setItem = vi.fn(() => {
        throw new Error('Quota exceeded');
      });

      // No debe lanzar
      expect(() => guardarSaldoOculto(true)).not.toThrow();

      window.localStorage.setItem = original;
    });
  });
});
