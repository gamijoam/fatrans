/**
 * Parsea y formatea respuestas de notificaciones del backend (issue #214 PR-B).
 *
 * Wrap del backend: `{notificaciones, pagina, tamanio, totalElementos,
 * totalPaginas, noLeidas}`.
 *
 * Defensive: acepta también array directo o formas inesperadas (lección de
 * los parsers previos de cuentas/movimientos/beneficiarios).
 */

export type TipoNotificacion =
  | 'KYC_APROBADO' | 'KYC_RECHAZADO' | 'KYC_REQUIERE_INFO'
  | 'CREDITO_APROBADO' | 'CREDITO_RECHAZADO' | 'CREDITO_DESEMBOLSADO'
  | 'CUOTA_PROXIMA_VENCER' | 'CUOTA_VENCIDA'
  | 'DEPOSITO_RECIBIDO' | 'RETIRO_PROCESADO'
  | 'NUEVO_DISPOSITIVO_LOGIN'
  | 'ADMIN_NUEVA_SOLICITUD' | 'ADMIN_NUEVO_KYC'
  | 'GENERAL';

export type PrioridadNotificacion = 'URGENTE' | 'NORMAL' | 'BAJA';

export interface NotificacionApi {
  id: string;
  tipo: TipoNotificacion | string;
  titulo: string;
  mensaje: string;
  linkAccion?: string | null;
  leida: boolean;
  fechaLectura?: string | null;
  prioridad: PrioridadNotificacion | string;
  createdAt: string;
}

export interface NotificacionListPayload {
  notificaciones: NotificacionApi[];
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
  noLeidas: number;
}

const EMPTY: NotificacionListPayload = {
  notificaciones: [],
  pagina: 0,
  tamanio: 0,
  totalElementos: 0,
  totalPaginas: 0,
  noLeidas: 0,
};

/**
 * Parsea el response JSON del backend en una estructura tipada.
 * Cuando el formato no se reconoce, retorna el payload vacío para que la UI
 * pueda renderizar empty state sin romper.
 */
export function parseNotificacionesResponse(data: unknown): NotificacionListPayload {
  if (!data) return EMPTY;

  // Caso normal: objeto del backend
  if (typeof data === 'object' && 'notificaciones' in (data as object)) {
    const d = data as Partial<NotificacionListPayload>;
    const list = Array.isArray(d.notificaciones) ? d.notificaciones : [];
    return {
      notificaciones: list,
      pagina: typeof d.pagina === 'number' ? d.pagina : 0,
      tamanio: typeof d.tamanio === 'number' ? d.tamanio : list.length,
      totalElementos:
        typeof d.totalElementos === 'number' ? d.totalElementos : list.length,
      totalPaginas: typeof d.totalPaginas === 'number' ? d.totalPaginas : 1,
      noLeidas: typeof d.noLeidas === 'number' ? d.noLeidas : 0,
    };
  }

  // Caso defensive: array directo (futuro-proof)
  if (Array.isArray(data)) {
    return { ...EMPTY, notificaciones: data as NotificacionApi[], totalElementos: data.length };
  }

  return EMPTY;
}

/**
 * Formatea el contador del badge: si es 0 retorna `null` (no mostrar badge),
 * si es > 99 retorna `'99+'` (apto para visual).
 */
export function formatearBadgeCount(noLeidas: number): string | null {
  if (!Number.isFinite(noLeidas) || noLeidas <= 0) return null;
  if (noLeidas > 99) return '99+';
  return String(Math.floor(noLeidas));
}

/**
 * Devuelve el tipo CSS de prioridad (warning/info/error) para colorear
 * el item en la UI. Mismo patrón que `decidir-kyc-banner`.
 */
export function estiloPorPrioridad(
  prioridad: string
): 'urgent' | 'normal' | 'low' {
  if (prioridad === 'URGENTE') return 'urgent';
  if (prioridad === 'BAJA') return 'low';
  return 'normal';
}

/**
 * Formatea timestamp ISO a "hace X minutos / hace X horas / DD MMM".
 * Localizado a es-VE.
 */
export function formatearTiempoRelativo(isoString: string, ahora: Date = new Date()): string {
  if (!isoString) return '';
  try {
    const fecha = new Date(isoString);
    if (Number.isNaN(fecha.getTime())) return '';

    const diffMs = ahora.getTime() - fecha.getTime();
    const diffMin = Math.floor(diffMs / 60_000);
    const diffH = Math.floor(diffMs / 3_600_000);
    const diffDays = Math.floor(diffMs / 86_400_000);

    if (diffMin < 1) return 'ahora mismo';
    if (diffMin < 60) return `hace ${diffMin} ${diffMin === 1 ? 'minuto' : 'minutos'}`;
    if (diffH < 24) return `hace ${diffH} ${diffH === 1 ? 'hora' : 'horas'}`;
    if (diffDays < 7) return `hace ${diffDays} ${diffDays === 1 ? 'día' : 'días'}`;

    return fecha.toLocaleDateString('es-VE', { day: '2-digit', month: 'short' });
  } catch {
    return '';
  }
}
