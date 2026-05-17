import { describe, it, expect } from 'vitest';
import { enmascararCedula } from './enmascarar-cedula';

/**
 * Tests para issue #219: enmascarar cédula al estilo banca (V-20***456).
 *
 * Patrón conocido: Banesco, Mercantil, BdV muestran solo prefijo + 2
 * primeros + asteriscos + últimos 3-4 dígitos. Protege contra shoulder
 * surfing y screen sharing en demos.
 */
describe('enmascararCedula (issue #219)', () => {

  describe('Cédulas venezolanas estándar', () => {
    it('V-20123456 (8 dígitos) → V-20***456', () => {
      expect(enmascararCedula('V-20123456')).toBe('V-20***456');
    });

    it('E-12345678 (extranjero, 8 dígitos) → E-12***678', () => {
      expect(enmascararCedula('E-12345678')).toBe('E-12***678');
    });

    it('E-1234567 (7 dígitos) → E-12**567', () => {
      expect(enmascararCedula('E-1234567')).toBe('E-12**567');
    });

    it('J-123456789 (RIF jurídico, 9 dígitos) → J-12****789', () => {
      expect(enmascararCedula('J-123456789')).toBe('J-12****789');
    });
  });

  describe('Configurable: visibleAlFinal personalizable', () => {
    it('V-20123456 con 4 dígitos visibles → V-20**3456', () => {
      expect(enmascararCedula('V-20123456', 4)).toBe('V-20**3456');
    });

    it('V-20123456 con 2 dígitos visibles → V-20****56', () => {
      expect(enmascararCedula('V-20123456', 2)).toBe('V-20****56');
    });
  });

  describe('Prefijo case-insensitive', () => {
    it('v-20123456 minúsculo → V-20***456 (normaliza prefijo)', () => {
      expect(enmascararCedula('v-20123456')).toBe('V-20***456');
    });
  });

  describe('Sin guion (formato compacto)', () => {
    it('V20123456 (sin guion) → V-20***456 (normaliza)', () => {
      expect(enmascararCedula('V20123456')).toBe('V-20***456');
    });
  });

  describe('Edge cases', () => {
    it('null → string vacío', () => {
      expect(enmascararCedula(null)).toBe('');
    });

    it('undefined → string vacío', () => {
      expect(enmascararCedula(undefined)).toBe('');
    });

    it('string vacío → string vacío', () => {
      expect(enmascararCedula('')).toBe('');
    });

    it('cédula muy corta (1-2 dígitos) → se muestra completa', () => {
      // No tiene sentido ocultar 1-2 dígitos cuando defaultVisibleAlFinal=3
      expect(enmascararCedula('V-12')).toBe('V-12');
    });

    it('formato no venezolano → enmascarado genérico (sin prefijo)', () => {
      // "A123456789" = 10 chars; oculta 7, muestra últimos 3
      expect(enmascararCedula('A123456789')).toBe('*******789');
    });
  });

  describe('Issue #219: seguridad — la cédula NUNCA debe aparecer completa cuando esperamos enmascararla', () => {
    it('V-20123456 enmascarada NO contiene "20123456" completo', () => {
      const result = enmascararCedula('V-20123456');
      expect(result).not.toContain('20123456');
      expect(result).toContain('***'); // debe haber asteriscos
    });

    it('cédula larga (10+ dígitos) sigue ocultando la mayoría', () => {
      // Hipotético — Venezuela no llega a 10 pero defensive futuro-proof
      const result = enmascararCedula('V-9999999999', 3);
      // Solo 2 primeros + 3 últimos visibles → ~5 dígitos asteriscos
      expect(result.match(/\*/g)?.length ?? 0).toBeGreaterThanOrEqual(4);
    });
  });
});
