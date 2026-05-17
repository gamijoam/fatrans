/**
 * Enmascara una cédula/documento manteniendo solo el prefijo (V/E/J) y los
 * últimos N dígitos visibles (issue #219).
 *
 * Patrón estándar en banca venezolana (similar a Banesco, Mercantil, Nequi):
 * `V-20***456` permite al socio verificar que la app reconoce su documento
 * sin exponer todos los dígitos a alguien que esté mirando la pantalla.
 *
 * @param documento documento completo (ej. `V-20123456`, `J-123456789`)
 * @param visibleAlFinal cuántos dígitos del final dejar visibles (default 3)
 * @returns documento enmascarado, o el original si no se reconoce el formato
 *
 * @example
 *   enmascararCedula('V-20123456') === 'V-20***456'  // ojo: 2 inicio + 3 final
 *   enmascararCedula('E-1234567') === 'E-1***567'
 *   enmascararCedula('V-20123456', 4) === 'V-20**3456'
 */
export function enmascararCedula(
  documento: string | null | undefined,
  visibleAlFinal: number = 3
): string {
  if (!documento || typeof documento !== 'string') return '';

  const trimmed = documento.trim();
  if (trimmed.length === 0) return '';

  // Detectar prefijo `V-`, `E-`, `J-`, `G-`, `P-` (venezolano + jurídico)
  const matchPrefijo = trimmed.match(/^([VEJGP])[-]?(\d+)$/i);
  if (!matchPrefijo) {
    // Formato desconocido: enmascaramos manteniendo solo último N dígitos
    return enmascararGenerico(trimmed, visibleAlFinal);
  }

  const prefijo = matchPrefijo[1].toUpperCase();
  const digitos = matchPrefijo[2];

  if (digitos.length <= visibleAlFinal) {
    // Muy corto para enmascarar — mostrar todo (no oculta nada útil)
    return `${prefijo}-${digitos}`;
  }

  // Mostrar: prefijo + 2 primeros dígitos + asteriscos + últimos N dígitos
  const visibleAlInicio = Math.min(2, digitos.length - visibleAlFinal);
  const inicio = digitos.substring(0, visibleAlInicio);
  const final = digitos.substring(digitos.length - visibleAlFinal);
  const asteriscos = '*'.repeat(digitos.length - visibleAlInicio - visibleAlFinal);

  return `${prefijo}-${inicio}${asteriscos}${final}`;
}

function enmascararGenerico(texto: string, visibleAlFinal: number): string {
  if (texto.length <= visibleAlFinal) return texto;
  const visible = texto.substring(texto.length - visibleAlFinal);
  const asteriscos = '*'.repeat(texto.length - visibleAlFinal);
  return `${asteriscos}${visible}`;
}
