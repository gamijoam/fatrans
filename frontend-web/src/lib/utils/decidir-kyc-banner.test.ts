import { describe, it, expect, vi } from 'vitest';
import { decidirKycBanner } from './decidir-kyc-banner';

/**
 * Tests para issue #215: decisión de UI del banner KYC según estado del backend.
 *
 * El backend tiene 7 estados de EstadoVerificacion + el "virtual" SIN_KYC
 * que mapea el BFF cuando el socio no inició el proceso. Cada uno debe
 * producir el banner correcto (o ninguno, para APROBADO).
 */
describe('decidirKycBanner (issue #215)', () => {

  describe('Estados que SÍ muestran banner', () => {

    it('SIN_KYC → banner warning con CTA para comenzar', () => {
      const decision = decidirKycBanner('SIN_KYC');

      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('warning');
      expect(decision!.titulo).toContain('Completa tu verificación');
      expect(decision!.ctaTexto).toBe('Comenzar verificación');
      expect(decision!.ctaHref).toBe('/dashboard/kyc');
    });

    it('PENDIENTE → banner info (24-48h)', () => {
      const decision = decidirKycBanner('PENDIENTE');
      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('info');
      expect(decision!.mensaje).toContain('24');
    });

    it('EN_REVISION → banner info', () => {
      const decision = decidirKycBanner('EN_REVISION');
      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('info');
      expect(decision!.titulo).toContain('revisión');
    });

    it('REENVIADO → banner info', () => {
      const decision = decidirKycBanner('REENVIADO');
      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('info');
    });

    it('RECHAZADO sin motivo → banner error genérico', () => {
      const decision = decidirKycBanner('RECHAZADO');
      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('error');
      expect(decision!.titulo).toContain('rechazada');
      expect(decision!.ctaTexto).toBe('Corregir documentos');
    });

    it('Issue #215: RECHAZADO CON motivo → muestra el motivo al socio', () => {
      const motivo = 'La foto de la cédula está borrosa';
      const decision = decidirKycBanner('RECHAZADO', motivo);

      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('error');
      // CRÍTICO: el motivo debe aparecer en el mensaje para que el socio
      // sepa qué corregir, no un mensaje genérico.
      expect(decision!.mensaje).toContain(motivo);
    });

    it('EXPIRADO → banner warning para renovar', () => {
      const decision = decidirKycBanner('EXPIRADO');
      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('warning');
      expect(decision!.ctaTexto).toBe('Renovar KYC');
    });

    it('CANCELADO → banner warning para reiniciar', () => {
      const decision = decidirKycBanner('CANCELADO');
      expect(decision).not.toBeNull();
      expect(decision!.tipo).toBe('warning');
      expect(decision!.ctaTexto).toBe('Reiniciar verificación');
    });
  });

  describe('Estados que NO muestran banner', () => {

    it('APROBADO → null (sin banner, todo OK)', () => {
      // CRÍTICO: socio aprobado NO debe ver banner — la ausencia es el
      // mensaje positivo. Banner permanente sería ruido visual.
      const decision = decidirKycBanner('APROBADO');
      expect(decision).toBeNull();
    });

    it('null (estado aún no cargado) → null (no ruido)', () => {
      expect(decidirKycBanner(null)).toBeNull();
    });

    it('undefined (estado nunca seteado) → null', () => {
      expect(decidirKycBanner(undefined)).toBeNull();
    });

    it('string vacío → null', () => {
      expect(decidirKycBanner('')).toBeNull();
    });
  });

  describe('Defensive (futuro-proof)', () => {

    it('estado desconocido → null + warning en consola', () => {
      const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      const decision = decidirKycBanner('FUTURO_NUEVO_ESTADO');

      expect(decision).toBeNull();
      expect(warnSpy).toHaveBeenCalledWith(
        expect.stringContaining('Estado KYC desconocido')
      );

      warnSpy.mockRestore();
    });
  });

  describe('Issue #215: todos los CTAs apuntan a /dashboard/kyc', () => {
    const estadosConBanner = [
      'SIN_KYC', 'PENDIENTE', 'EN_REVISION', 'REENVIADO',
      'RECHAZADO', 'EXPIRADO', 'CANCELADO',
    ];

    estadosConBanner.forEach((estado) => {
      it(`${estado} → ctaHref es /dashboard/kyc`, () => {
        const decision = decidirKycBanner(estado);
        expect(decision).not.toBeNull();
        expect(decision!.ctaHref).toBe('/dashboard/kyc');
      });
    });
  });
});
