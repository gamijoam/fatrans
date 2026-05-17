/**
 * Parsea la respuesta del endpoint `/api/cuentas/{n}/movimientos` (issue #212).
 *
 * El backend retorna `MovimientosListResponse`:
 * ```json
 * {
 *   "numeroCuenta": "...",
 *   "pagina": 0,
 *   "tamanio": 10,
 *   "totalElementos": 42,
 *   "totalPaginas": 5,
 *   "movimientos": [{...}, {...}]
 * }
 * ```
 *
 * Este helper acepta tanto el formato wrap como un array directo (defensive ante
 * cambios futuros del API). Devuelve `[]` para cualquier forma no reconocida —
 * UI muestra empty state en lugar de romper.
 */

export interface MovimientoApi {
  id: string;
  numeroOperacion?: string;
  cuentaAhorroId?: string;
  socioId?: string;
  tipo: string;            // 'DEPOSITO' | 'RETIRO' | 'INTERES' | 'TRANSFERENCIA' | ...
  monto: number | string;  // BigDecimal serializado puede venir como string
  descripcion?: string;
  fechaMovimiento?: string; // ISO 8601 LocalDateTime
  fechaValor?: string;
}

export function parseMovimientosResponse(data: unknown): MovimientoApi[] {
  if (Array.isArray(data)) {
    return data as MovimientoApi[];
  }

  if (data && typeof data === 'object' && 'movimientos' in data) {
    const movs = (data as { movimientos: unknown }).movimientos;
    if (Array.isArray(movs)) {
      return movs as MovimientoApi[];
    }
  }

  return [];
}
