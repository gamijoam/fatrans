/**
 * Parsea la respuesta del endpoint `/api/cuentas/socio/{socioId}` (issue #213
 * — bug pre-existente descubierto durante QA del fix de saldo multimoneda).
 *
 * El backend retorna {@code CuentasPorSocioResponse}:
 * ```json
 * { "socioId": "...", "totalCuentas": 2, "cuentas": [ {...}, {...} ] }
 * ```
 *
 * Pero la página `/dashboard` (home del socio) hacía `Array.isArray(data) ? data : []`,
 * esperando un array directo → siempre caía a `[]` y mostraba "No tienes cuentas
 * activas aún" aunque el socio tuviera cuentas. El bug se notaba poco antes
 * del fix de saldo (todo se veía como Bs 0,00 de todas formas), pero con el
 * fix de toggle y skeleton se volvió obvio.
 *
 * Este helper acepta ambos formatos (array directo y objeto envoltura) para
 * ser robusto ante cambios futuros del API.
 */

export interface CuentaParseada {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  moneda: string;
  saldoActual: number;
  estado: string;
}

/**
 * @param data respuesta cruda del fetch (ya parseada como JSON)
 * @returns array de cuentas (vacío si la forma no se reconoce)
 */
export function parseCuentasResponse(data: unknown): CuentaParseada[] {
  // Caso 1: array directo
  if (Array.isArray(data)) {
    return data as CuentaParseada[];
  }

  // Caso 2: objeto envoltura `{cuentas: [...]}` (forma del backend actual)
  if (data && typeof data === 'object' && 'cuentas' in data) {
    const cuentas = (data as { cuentas: unknown }).cuentas;
    if (Array.isArray(cuentas)) {
      return cuentas as CuentaParseada[];
    }
  }

  // Forma desconocida → array vacío (defensive)
  return [];
}
