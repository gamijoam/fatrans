/**
 * Calcula el saldo agregado de cuentas en distintas monedas (issue #213).
 *
 * Reemplaza la lógica buggy del dashboard que sumaba VES + USD directamente
 * (ej. `1.000 VES + 50 USD = 1.050`, matemáticamente sin sentido).
 *
 * Estrategia:
 *  - Sumamos cuentas VES como están (moneda base).
 *  - Convertimos cuentas USD a VES con la tasa de VENTA (la que pagaría
 *    el socio si quisiera comprar bolívares con esos dólares).
 *  - El total en USD se obtiene dividiendo el total VES entre la tasa de
 *    COMPRA (lo que recibiría si vendiera bolívares por dólares).
 *
 * Cuando {@code tasaCambio} es {@code null} (la tasa aún no se cargó), no
 * podemos calcular un total agregado correcto. Retornamos `null` para
 * que el caller muestre un loading state en vez de un número incorrecto.
 */

export interface CuentaParaSaldo {
  moneda: string;
  saldoActual: number;
}

export interface TasaCambio {
  tasaCompra: number;
  tasaVenta: number;
}

export interface SaldoAgregado {
  totalVES: number;
  totalUSD: number;
}

/**
 * @returns saldo agregado en VES y USD, o `null` si {@code tasaCambio} es null
 *          (en cuyo caso el caller debe mostrar loading).
 */
export function calcularSaldoTotal(
  cuentas: ReadonlyArray<CuentaParaSaldo>,
  tasaCambio: TasaCambio | null
): SaldoAgregado | null {
  if (!cuentas || cuentas.length === 0) {
    return { totalVES: 0, totalUSD: 0 };
  }

  // Solo VES → no necesitamos tasa para el total en VES, pero sí para mostrar USD.
  const tieneUSD = cuentas.some((c) => c.moneda === 'USD');
  const tieneVES = cuentas.some((c) => c.moneda === 'VES');

  // Si hay USD o queremos mostrar también el equivalente en USD, necesitamos tasa.
  if (tasaCambio === null) {
    return null;
  }

  if (tasaCambio.tasaVenta <= 0 || tasaCambio.tasaCompra <= 0) {
    // Tasa inválida (división por cero o negativos). Mejor null que datos falsos.
    return null;
  }

  let totalVES = 0;
  for (const cuenta of cuentas) {
    const saldo = Number(cuenta.saldoActual);
    if (!Number.isFinite(saldo)) continue;

    if (cuenta.moneda === 'VES') {
      totalVES += saldo;
    } else if (cuenta.moneda === 'USD') {
      totalVES += saldo * tasaCambio.tasaVenta;
    }
    // Cualquier otra moneda se ignora — sería un dato no soportado.
  }

  // El total en USD es el total VES convertido a la inversa (tasa de compra).
  const totalUSD = totalVES / tasaCambio.tasaCompra;

  // Marcas no-op solo para evitar warnings de TS de variables no usadas en
  // implementaciones futuras (defensive, sirve también de documentación).
  void tieneUSD;
  void tieneVES;

  return { totalVES, totalUSD };
}
