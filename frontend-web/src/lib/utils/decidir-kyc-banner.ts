/**
 * Decide qué banner KYC mostrar en el home del socio según el estado actual
 * (issue #215).
 *
 * El backend retorna {@code EstadoKYCResponse.estado} como uno de:
 *   PENDIENTE | EN_REVISION | APROBADO | RECHAZADO | REENVIADO | EXPIRADO | CANCELADO
 *
 * El BFF (`/api/kyc/estado`) además mapea el caso "socio no inició KYC"
 * (backend devuelve 500 + KYC_000) a `{estado: 'SIN_KYC', mensaje}`.
 *
 * Esta función traduce ese estado en una decisión de UI (tipo de banner,
 * título, mensaje, CTA). Si retorna `null`, NO mostramos banner —
 * típicamente porque el socio está APROBADO y no necesita ver nada.
 */

export type KycEstado =
  | 'SIN_KYC'        // BFF: socio no inició proceso
  | 'PENDIENTE'      // Documentos enviados, en cola
  | 'EN_REVISION'    // Analista revisando
  | 'APROBADO'       // OK — no mostrar banner
  | 'RECHAZADO'      // Necesita corregir
  | 'REENVIADO'      // Reenviado tras rechazo, esperando re-revisión
  | 'EXPIRADO'       // KYC expiró, debe renovar
  | 'CANCELADO';     // Socio canceló el proceso

export type KycBannerTipo = 'warning' | 'info' | 'error';

export interface KycBannerDecision {
  /** Estilo visual del banner. */
  tipo: KycBannerTipo;
  /** Título corto y accionable. */
  titulo: string;
  /** Texto descriptivo bajo el título. */
  mensaje: string;
  /** Texto del botón CTA (null = sin botón, solo informativo). */
  ctaTexto: string | null;
  /** URL relativa del CTA (típicamente `/dashboard/kyc`). */
  ctaHref: string | null;
}

/**
 * @param estado estado actual de la verificación KYC del socio
 * @param motivoRechazo opcional, solo aplica a estado RECHAZADO
 * @returns decisión de UI o `null` si NO debe mostrarse banner
 */
export function decidirKycBanner(
  estado: KycEstado | string | null | undefined,
  motivoRechazo?: string | null
): KycBannerDecision | null {
  if (!estado) {
    // Aún no cargó / error de red → no mostrar banner (UI no debe ruido cuando
    // simplemente no sabemos el estado todavía).
    return null;
  }

  switch (estado as KycEstado) {
    case 'APROBADO':
      // OK — el socio puede operar sin restricciones, sin banner.
      return null;

    case 'SIN_KYC':
      return {
        tipo: 'warning',
        titulo: 'Completa tu verificación de identidad',
        mensaje:
          'Necesitas verificar tu identidad (KYC) para acceder a todas las funciones del fondo (créditos, montos altos, etc.).',
        ctaTexto: 'Comenzar verificación',
        ctaHref: '/dashboard/kyc',
      };

    case 'PENDIENTE':
      return {
        tipo: 'info',
        titulo: 'Verificación KYC en cola',
        mensaje:
          'Recibimos tus documentos. Te notificaremos cuando el analista los revise (24–48h hábiles).',
        ctaTexto: 'Ver detalle',
        ctaHref: '/dashboard/kyc',
      };

    case 'EN_REVISION':
      return {
        tipo: 'info',
        titulo: 'Verificación KYC en revisión',
        mensaje:
          'Un analista está revisando tus documentos. Te notificaremos en breve.',
        ctaTexto: 'Ver detalle',
        ctaHref: '/dashboard/kyc',
      };

    case 'REENVIADO':
      return {
        tipo: 'info',
        titulo: 'Documentos reenviados',
        mensaje:
          'Tus correcciones fueron recibidas y están en cola para nueva revisión.',
        ctaTexto: 'Ver detalle',
        ctaHref: '/dashboard/kyc',
      };

    case 'RECHAZADO':
      return {
        tipo: 'error',
        titulo: 'Verificación KYC rechazada',
        mensaje: motivoRechazo
          ? `Motivo: ${motivoRechazo}. Corrige los documentos para continuar.`
          : 'Hubo un problema con tus documentos. Revisa el detalle para corregir.',
        ctaTexto: 'Corregir documentos',
        ctaHref: '/dashboard/kyc',
      };

    case 'EXPIRADO':
      return {
        tipo: 'warning',
        titulo: 'Tu verificación KYC expiró',
        mensaje:
          'Por seguridad y regulación bancaria, debes renovar tu verificación de identidad.',
        ctaTexto: 'Renovar KYC',
        ctaHref: '/dashboard/kyc',
      };

    case 'CANCELADO':
      return {
        tipo: 'warning',
        titulo: 'Verificación KYC cancelada',
        mensaje:
          'Cancelaste tu proceso de verificación. Puedes reiniciarlo cuando quieras.',
        ctaTexto: 'Reiniciar verificación',
        ctaHref: '/dashboard/kyc',
      };

    default:
      // Estado desconocido (futuro, typo, etc.) — no rompemos la UI, no
      // mostramos banner ruido. El log queda en consola para detectar.
      if (typeof window !== 'undefined') {
        // eslint-disable-next-line no-console
        console.warn(`[KycBanner] Estado KYC desconocido: ${estado}`);
      }
      return null;
  }
}
