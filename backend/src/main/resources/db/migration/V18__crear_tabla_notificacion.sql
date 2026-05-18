-- =============================================================================
-- V18: Issue #214 - Sistema de notificaciones in-app (PR-A backend base)
-- =============================================================================
--
-- CONTEXTO
-- --------
-- Antes el frontend usaba `mockNotifications` hardcoded en `admin-shell.tsx`
-- y el `Bell` icon estaba importado pero nunca renderizado en `socio-shell.tsx`.
-- No había sistema real de notificaciones in-app.
--
-- Este PR (parte A de 3) introduce el almacenamiento + endpoints CRUD.
-- Los disparadores desde eventos de dominio (KYC, créditos, depósitos)
-- vienen en PR-C. El frontend (Bell + dropdown) viene en PR-B.
--
-- DISEÑO
-- ------
-- Tabla diseñada para alto volumen de lecturas (un socio puede tener cientos
-- de notificaciones a lo largo del tiempo). El índice parcial
-- `idx_notif_dest_unread` acelera dramáticamente el contador de no-leídas
-- (consulta más frecuente: badge del Bell cada 30s).
-- =============================================================================

CREATE TABLE IF NOT EXISTS notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Destinatario: usuario que recibe la notificación. NO usamos socio_id
    -- porque también queremos notificar a admins (no son socios).
    destinatario_id UUID NOT NULL,

    -- Tipo: usado para iconografía/clasificación en UI y filtros del backend.
    -- Validamos los valores conocidos vía CHECK (extensible con futuras
    -- migraciones; el enum del lado Java valida en runtime también).
    tipo VARCHAR(50) NOT NULL,

    -- Título corto (200 chars max) — aparece en bold en la lista.
    titulo VARCHAR(200) NOT NULL,

    -- Mensaje completo. TEXT porque pueden ser largos (ej. motivo de rechazo
    -- KYC, detalle de operación).
    mensaje TEXT NOT NULL,

    -- Link relativo donde llevar al usuario si hace click (ej. /dashboard/kyc).
    -- NULL = notificación sin acción asociada.
    link_accion VARCHAR(500),

    -- Estado de lectura.
    leida BOOLEAN NOT NULL DEFAULT false,
    fecha_lectura TIMESTAMPTZ,

    -- Prioridad para ordenamiento y estilos (URGENTE = rojo, NORMAL = azul, BAJA = gris).
    prioridad VARCHAR(20) NOT NULL DEFAULT 'NORMAL',

    -- Metadata libre en JSON para casos avanzados (ej. ID del crédito al que
    -- refiere la notificación). Por ahora opcional, ayuda a no añadir
    -- columnas para cada caso de uso.
    metadata JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT notificacion_tipo_check CHECK (tipo IN (
        'KYC_APROBADO', 'KYC_RECHAZADO', 'KYC_REQUIERE_INFO',
        'CREDITO_APROBADO', 'CREDITO_RECHAZADO', 'CREDITO_DESEMBOLSADO',
        'CUOTA_PROXIMA_VENCER', 'CUOTA_VENCIDA',
        'DEPOSITO_RECIBIDO', 'RETIRO_PROCESADO',
        'NUEVO_DISPOSITIVO_LOGIN',
        'ADMIN_NUEVA_SOLICITUD', 'ADMIN_NUEVO_KYC',
        'GENERAL'
    )),

    CONSTRAINT notificacion_prioridad_check CHECK (prioridad IN ('URGENTE', 'NORMAL', 'BAJA')),

    -- Garantía de consistencia: si leida=true, debe haber fecha de lectura.
    CONSTRAINT notificacion_fecha_lectura_check
        CHECK ((leida = false AND fecha_lectura IS NULL)
               OR (leida = true AND fecha_lectura IS NOT NULL))
);

-- Índice principal: las 2 queries más frecuentes son
--   (a) listar por destinatario ordenado por fecha desc
--   (b) contar no-leídas de un destinatario
-- Este índice cubre ambas eficientemente.
CREATE INDEX IF NOT EXISTS idx_notif_destinatario_fecha
    ON notificacion(destinatario_id, created_at DESC);

-- Índice parcial: solo no-leídas. Hace el COUNT del badge súper rápido
-- incluso con miles de notificaciones leídas viejas.
CREATE INDEX IF NOT EXISTS idx_notif_dest_unread
    ON notificacion(destinatario_id)
    WHERE leida = false;

-- Filtro por tipo para futuras vistas (ej. "solo mis notificaciones de crédito")
CREATE INDEX IF NOT EXISTS idx_notif_tipo
    ON notificacion(tipo, created_at DESC);
