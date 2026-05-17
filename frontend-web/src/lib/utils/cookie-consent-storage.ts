/**
 * Storage helpers para el consentimiento de cookies (issue #218 PR-A).
 *
 * Persiste en localStorage la decisión del usuario sobre qué categorías
 * de cookies acepta. Patrón estándar de cumplimiento LOPDP/GDPR-like:
 * el banner aparece la primera vez (sin preferencia guardada), y luego
 * se respeta la elección hasta que el usuario la cambie desde la
 * página de cookies.
 *
 * Utility puro: no usa hooks. Componentes lo invocan en cliente
 * (`'use client'`). Resiliente a SSR y modo privado.
 */

const STORAGE_KEY = 'fatrans.cookie.consent';
const STORAGE_VERSION = '1';

/**
 * Categorías de cookies. Hoy Fatrans solo usa las estrictamente
 * necesarias (sesión, CSRF, tema). Las otras dos quedan listas para
 * cuando se incorpore analítica/marketing — el plumbing ya existirá.
 */
export type CookieCategoria = 'necesarias' | 'preferencias' | 'analiticas' | 'marketing';

export interface CookieConsent {
  /** Versión del schema — incrementar si las categorías cambian. */
  version: string;
  /** Categorías aceptadas. `necesarias` siempre es true (no son opt-in). */
  necesarias: true;
  preferencias: boolean;
  analiticas: boolean;
  marketing: boolean;
  /** ISO 8601 con la fecha de aceptación. */
  fechaConsentimiento: string;
}

/**
 * Lee la preferencia guardada. Devuelve `null` si:
 * - El usuario nunca ha respondido (debe aparecer el banner)
 * - El schema es de una versión anterior (debe re-solicitarse)
 * - localStorage no está disponible (SSR / modo privado)
 */
export function leerCookieConsent(): CookieConsent | null {
  if (typeof window === 'undefined' || !window.localStorage) {
    return null;
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as CookieConsent;
    if (parsed.version !== STORAGE_VERSION) return null;
    // Validación defensiva: las flags deben ser booleanas
    if (
      typeof parsed.preferencias !== 'boolean' ||
      typeof parsed.analiticas !== 'boolean' ||
      typeof parsed.marketing !== 'boolean'
    ) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

/**
 * Guarda la preferencia. Las necesarias se fuerzan a `true` (no es
 * opt-in del usuario, son obligatorias para que la app funcione).
 */
export function guardarCookieConsent(consent: Omit<CookieConsent, 'version' | 'necesarias' | 'fechaConsentimiento'>): void {
  if (typeof window === 'undefined' || !window.localStorage) {
    return;
  }
  try {
    const full: CookieConsent = {
      version: STORAGE_VERSION,
      necesarias: true,
      preferencias: consent.preferencias,
      analiticas: consent.analiticas,
      marketing: consent.marketing,
      fechaConsentimiento: new Date().toISOString(),
    };
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(full));
  } catch {
    // Quota / modo privado / etc — silenciar (no es crítico)
  }
}

/** Atajos comunes: aceptar todas o solo necesarias. */
export function aceptarTodas(): void {
  guardarCookieConsent({ preferencias: true, analiticas: true, marketing: true });
}

export function rechazarOpcionales(): void {
  guardarCookieConsent({ preferencias: false, analiticas: false, marketing: false });
}

/**
 * Limpia el consentimiento — útil para "revocar" desde la página
 * de cookies. La próxima carga vuelve a mostrar el banner.
 */
export function limpiarCookieConsent(): void {
  if (typeof window === 'undefined' || !window.localStorage) return;
  try {
    window.localStorage.removeItem(STORAGE_KEY);
  } catch {
    /* noop */
  }
}

/**
 * Helper para que el código de la app pregunte "¿puedo cargar Google
 * Analytics ya?" sin meterse con el schema directamente.
 */
export function puedeCargarCategoria(categoria: CookieCategoria): boolean {
  if (categoria === 'necesarias') return true;
  const consent = leerCookieConsent();
  if (!consent) return false; // sin consentimiento → conservador (no cargar)
  return consent[categoria] === true;
}
