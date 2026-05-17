import { redirect } from 'next/navigation';

/**
 * Issue #219: el simulador en esta ruta estaba duplicado y permitía editar
 * la tasa de interés (riesgo: usuario simula tasas inventadas, tipos de
 * crédito y tasas hardcoded en el frontend). Se eliminó en favor del
 * simulador real conectado al API en `/dashboard/creditos/simulador`.
 *
 * Mantenemos la ruta vieja redirigiendo para no romper bookmarks ni
 * enlaces externos. Cuando todos los clientes hayan migrado
 * (~3-6 meses), esta página se puede borrar por completo.
 */
export default function SimuladorRedirectPage() {
  redirect('/dashboard/creditos/simulador');
}
