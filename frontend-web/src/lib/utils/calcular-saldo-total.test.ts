import { describe, it, expect } from 'vitest';
import { calcularSaldoTotal, type CuentaParaSaldo, type TasaCambio } from './calcular-saldo-total';

/**
 * Tests para issue #213: agregación de saldos multimoneda.
 *
 * Antes del fix, el dashboard sumaba `c.saldoActual` sin importar moneda
 * (ej. `1.000 VES + 50 USD = 1.050`). Este utility lo reemplaza con
 * conversión correcta usando tasa BCV.
 */
describe('calcularSaldoTotal (issue #213)', () => {
  const tasa: TasaCambio = { tasaCompra: 44.5, tasaVenta: 45.0 };

  describe('Casos normales (con tasa válida)', () => {
    it('cuenta única VES → totalVES = saldo, totalUSD calculado con tasa de compra', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'VES', saldoActual: 100_000 },
      ];

      const result = calcularSaldoTotal(cuentas, tasa);

      expect(result).not.toBeNull();
      expect(result!.totalVES).toBe(100_000);
      expect(result!.totalUSD).toBeCloseTo(100_000 / 44.5, 2);
    });

    it('cuenta única USD → totalVES convertido, totalUSD = saldo', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'USD', saldoActual: 100 },
      ];

      const result = calcularSaldoTotal(cuentas, tasa);

      expect(result).not.toBeNull();
      expect(result!.totalVES).toBe(100 * 45.0); // = 4500
      // totalUSD = 4500 / 44.5 = ~101.12 (no es exactamente 100 por el spread compra/venta)
      expect(result!.totalUSD).toBeCloseTo(4500 / 44.5, 2);
    });

    it('Issue #213 - escenario crítico: cuenta 100.000 VES + 100 USD a tasa 45 = 104.500 VES (NO 100.100)', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'VES', saldoActual: 100_000 },
        { moneda: 'USD', saldoActual: 100 },
      ];

      const result = calcularSaldoTotal(cuentas, tasa);

      expect(result).not.toBeNull();
      // BUG: antes del fix esto era 100_100 (suma directa sin conversión)
      // FIX: 100_000 + (100 * 45) = 104_500
      expect(result!.totalVES).toBe(104_500);
      // Y NUNCA debe ser la suma directa sin conversión
      expect(result!.totalVES).not.toBe(100_100);
    });

    it('Issue #213 - escenario del backlog: 2M VES + 5000 USD a tasa 45 = 2.225.000 VES', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'VES', saldoActual: 2_000_000 },
        { moneda: 'USD', saldoActual: 5_000 },
      ];

      const result = calcularSaldoTotal(cuentas, tasa);

      expect(result).not.toBeNull();
      expect(result!.totalVES).toBe(2_225_000);
      expect(result!.totalUSD).toBeCloseTo(2_225_000 / 44.5, 2);
    });

    it('múltiples cuentas mixtas suman correctamente', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'VES', saldoActual: 50_000 },
        { moneda: 'VES', saldoActual: 30_000 },
        { moneda: 'USD', saldoActual: 200 },
      ];

      const result = calcularSaldoTotal(cuentas, tasa);

      expect(result).not.toBeNull();
      // 50_000 + 30_000 + (200 * 45) = 89_000
      expect(result!.totalVES).toBe(89_000);
    });

    it('array vacío → totales en cero', () => {
      const result = calcularSaldoTotal([], tasa);
      expect(result).not.toBeNull();
      expect(result!.totalVES).toBe(0);
      expect(result!.totalUSD).toBe(0);
    });
  });

  describe('Loading state (tasaCambio null)', () => {
    it('si tasaCambio es null y hay cuentas → retorna null (loading)', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'VES', saldoActual: 100_000 },
        { moneda: 'USD', saldoActual: 100 },
      ];

      const result = calcularSaldoTotal(cuentas, null);

      // CRÍTICO: NO retornar un número parcial — mostrar loading en UI
      expect(result).toBeNull();
    });

    it('si tasaCambio es null pero NO hay cuentas → totales en cero (no necesitamos tasa)', () => {
      const result = calcularSaldoTotal([], null);
      expect(result).not.toBeNull();
      expect(result!.totalVES).toBe(0);
      expect(result!.totalUSD).toBe(0);
    });
  });

  describe('Edge cases — tasa inválida (defensive)', () => {
    it('tasaVenta = 0 → null (evitar datos falsos)', () => {
      const tasaCero: TasaCambio = { tasaCompra: 44.5, tasaVenta: 0 };
      const result = calcularSaldoTotal([{ moneda: 'USD', saldoActual: 100 }], tasaCero);
      expect(result).toBeNull();
    });

    it('tasaCompra = 0 → null (evitar división por cero)', () => {
      const tasaCero: TasaCambio = { tasaCompra: 0, tasaVenta: 45 };
      const result = calcularSaldoTotal([{ moneda: 'VES', saldoActual: 100_000 }], tasaCero);
      expect(result).toBeNull();
    });

    it('saldoActual con NaN se ignora (no corrompe el total)', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'VES', saldoActual: 100_000 },
        { moneda: 'VES', saldoActual: NaN },
      ];

      const result = calcularSaldoTotal(cuentas, tasa);

      expect(result).not.toBeNull();
      expect(result!.totalVES).toBe(100_000);
    });

    it('moneda desconocida se ignora (no corrompe el total)', () => {
      const cuentas: CuentaParaSaldo[] = [
        { moneda: 'VES', saldoActual: 100_000 },
        { moneda: 'EUR', saldoActual: 999 }, // moneda no soportada
      ];

      const result = calcularSaldoTotal(cuentas, tasa);

      expect(result).not.toBeNull();
      expect(result!.totalVES).toBe(100_000);
    });
  });
});
