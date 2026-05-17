/**
 * Parsea la respuesta del endpoint `/api/beneficiarios?socioId=...` (issue #212).
 *
 * El BFF retorna `{beneficiarios: [], total: 0}` (estructura del backend
 * `BeneficiarioListResponseDTO`). Si el socio no tiene beneficiarios, el BFF
 * captura el 404 y retorna lista vacía.
 *
 * Defensive: acepta también array directo o cualquier forma desconocida →
 * array vacío (UI muestra empty state).
 */

export interface BeneficiarioApi {
  id: string;
  socioId: string;
  nombreCompleto: string;
  numeroDocumento?: string;
  tipoDocumento?: string;
  parentesco?: string;
  /** BigDecimal serializado puede venir como string o number. */
  porcentaje: number | string;
  telefono?: string;
  activo?: boolean;
}

export function parseBeneficiariosResponse(data: unknown): BeneficiarioApi[] {
  if (Array.isArray(data)) {
    return data as BeneficiarioApi[];
  }

  if (data && typeof data === 'object' && 'beneficiarios' in data) {
    const beneficiarios = (data as { beneficiarios: unknown }).beneficiarios;
    if (Array.isArray(beneficiarios)) {
      return beneficiarios as BeneficiarioApi[];
    }
  }

  return [];
}

/**
 * Filtra solo beneficiarios activos y los ordena por porcentaje descendente.
 * Útil para mostrar el resumen en el home (los más relevantes primero).
 */
export function beneficiariosActivosOrdenados(
  beneficiarios: BeneficiarioApi[]
): BeneficiarioApi[] {
  return beneficiarios
    .filter((b) => b.activo !== false)
    .sort((a, b) => Number(b.porcentaje ?? 0) - Number(a.porcentaje ?? 0));
}

/**
 * Suma los porcentajes asignados (útil para mostrar al socio si llegó al 100%).
 * Acepta strings y numbers porque BigDecimal serializa de ambas formas.
 */
export function sumaPorcentajes(beneficiarios: BeneficiarioApi[]): number {
  return beneficiarios
    .filter((b) => b.activo !== false)
    .reduce((total, b) => total + (Number(b.porcentaje) || 0), 0);
}
