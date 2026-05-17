import { describe, it, expect, beforeEach } from 'vitest';
import {
  leerCookieConsent,
  guardarCookieConsent,
  aceptarTodas,
  rechazarOpcionales,
  limpiarCookieConsent,
  puedeCargarCategoria,
} from '@/lib/utils/cookie-consent-storage';

describe('cookie-consent-storage', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  describe('leerCookieConsent', () => {
    it('devuelve null cuando no hay preferencia guardada (primera visita)', () => {
      expect(leerCookieConsent()).toBeNull();
    });

    it('devuelve null cuando el JSON está corrupto', () => {
      window.localStorage.setItem('fatrans.cookie.consent', 'no-es-json{');
      expect(leerCookieConsent()).toBeNull();
    });

    it('devuelve null cuando la versión del schema no coincide (re-solicitar consentimiento)', () => {
      window.localStorage.setItem(
        'fatrans.cookie.consent',
        JSON.stringify({
          version: '999',
          necesarias: true,
          preferencias: true,
          analiticas: true,
          marketing: true,
          fechaConsentimiento: '2026-05-17T22:00:00Z',
        }),
      );
      expect(leerCookieConsent()).toBeNull();
    });

    it('devuelve null si los flags no son booleanos (defensa)', () => {
      window.localStorage.setItem(
        'fatrans.cookie.consent',
        JSON.stringify({
          version: '1',
          necesarias: true,
          preferencias: 'yes',
          analiticas: 1,
          marketing: 'no',
          fechaConsentimiento: '2026-05-17T22:00:00Z',
        }),
      );
      expect(leerCookieConsent()).toBeNull();
    });

    it('devuelve el consent guardado tal cual', () => {
      const v = {
        version: '1',
        necesarias: true,
        preferencias: false,
        analiticas: true,
        marketing: false,
        fechaConsentimiento: '2026-05-17T22:00:00Z',
      };
      window.localStorage.setItem('fatrans.cookie.consent', JSON.stringify(v));
      expect(leerCookieConsent()).toEqual(v);
    });
  });

  describe('guardarCookieConsent', () => {
    it('persiste con necesarias=true forzado y fecha actual', () => {
      guardarCookieConsent({ preferencias: true, analiticas: false, marketing: true });
      const stored = leerCookieConsent();
      expect(stored).not.toBeNull();
      expect(stored!.necesarias).toBe(true);
      expect(stored!.preferencias).toBe(true);
      expect(stored!.analiticas).toBe(false);
      expect(stored!.marketing).toBe(true);
      expect(stored!.version).toBe('1');
      // Fecha ISO 8601 válida y reciente (último minuto)
      const fecha = new Date(stored!.fechaConsentimiento);
      const diff = Date.now() - fecha.getTime();
      expect(diff).toBeGreaterThanOrEqual(0);
      expect(diff).toBeLessThan(60_000);
    });
  });

  describe('aceptarTodas', () => {
    it('marca todas las categorías como true', () => {
      aceptarTodas();
      const stored = leerCookieConsent();
      expect(stored!.preferencias).toBe(true);
      expect(stored!.analiticas).toBe(true);
      expect(stored!.marketing).toBe(true);
    });
  });

  describe('rechazarOpcionales', () => {
    it('marca todas las opcionales como false pero necesarias true', () => {
      rechazarOpcionales();
      const stored = leerCookieConsent();
      expect(stored!.necesarias).toBe(true);
      expect(stored!.preferencias).toBe(false);
      expect(stored!.analiticas).toBe(false);
      expect(stored!.marketing).toBe(false);
    });
  });

  describe('limpiarCookieConsent', () => {
    it('borra la preferencia para que vuelva a aparecer el banner', () => {
      aceptarTodas();
      expect(leerCookieConsent()).not.toBeNull();
      limpiarCookieConsent();
      expect(leerCookieConsent()).toBeNull();
    });
  });

  describe('puedeCargarCategoria', () => {
    it('necesarias siempre devuelve true (incluso sin consentimiento)', () => {
      expect(puedeCargarCategoria('necesarias')).toBe(true);
    });

    it('sin consentimiento, opcionales devuelven false (conservador)', () => {
      expect(puedeCargarCategoria('analiticas')).toBe(false);
      expect(puedeCargarCategoria('marketing')).toBe(false);
      expect(puedeCargarCategoria('preferencias')).toBe(false);
    });

    it('respeta lo guardado por aceptarTodas', () => {
      aceptarTodas();
      expect(puedeCargarCategoria('analiticas')).toBe(true);
      expect(puedeCargarCategoria('marketing')).toBe(true);
    });

    it('respeta selección granular', () => {
      guardarCookieConsent({ preferencias: true, analiticas: false, marketing: false });
      expect(puedeCargarCategoria('preferencias')).toBe(true);
      expect(puedeCargarCategoria('analiticas')).toBe(false);
      expect(puedeCargarCategoria('marketing')).toBe(false);
    });
  });
});
