/**
 * Helpers para persistir la preferencia "ocultar saldo" del socio en
 * localStorage (issue #219). Patrón estándar en banca móvil
 * (Nequi/Bancolombia/Plin): el socio puede esconder su saldo con un
 * tap del ícono ojo, y la preferencia persiste entre sesiones.
 *
 * Utility puro: no usa hooks de React. Los componentes son responsables
 * de cargar/guardar y mantener el estado. Esto lo hace testeable
 * sin renderer.
 */

const STORAGE_KEY = 'fatrans.saldo.oculto';

/**
 * Lee la preferencia guardada. `false` por defecto (saldo VISIBLE) si
 * nunca se ha guardado o si localStorage no está disponible (SSR/private
 * mode).
 */
export function leerSaldoOculto(): boolean {
  if (typeof window === 'undefined' || !window.localStorage) {
    return false;
  }
  try {
    const v = window.localStorage.getItem(STORAGE_KEY);
    return v === 'true';
  } catch {
    // Algunos browsers en modo privado lanzan al leer
    return false;
  }
}

/**
 * Guarda la preferencia. No-op si localStorage no está disponible
 * (no rompe, simplemente no persiste — es comportamiento aceptable).
 */
export function guardarSaldoOculto(oculto: boolean): void {
  if (typeof window === 'undefined' || !window.localStorage) {
    return;
  }
  try {
    window.localStorage.setItem(STORAGE_KEY, oculto ? 'true' : 'false');
  } catch {
    // Quota excedida, modo privado, etc. — silenciar (no es crítico)
  }
}

/**
 * Devuelve el texto a mostrar: el valor formateado original si está
 * visible, o el "censurado" si está oculto.
 *
 * Patrón visual: `••••••` (mismo ancho aproximado que un número grande).
 */
export function aplicarOcultarSaldo(
  textoFormateado: string,
  oculto: boolean
): string {
  if (!oculto) return textoFormateado;
  // Usamos 6 puntos gordos — suficiente para no revelar magnitud
  // (no mostramos "Bs 0,00" porque ahí sí se sabe que no tiene plata).
  return '••••••';
}
