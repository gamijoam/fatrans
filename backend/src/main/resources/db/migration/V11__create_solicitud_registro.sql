-- V11__create_solicitud_registro.sql
--
-- Intent:
--   Documentar formalmente la tabla `solicitud_registro` en Flyway.
--   Esta tabla fue creada previamente en producción por Hibernate
--   (`spring.jpa.hibernate.ddl-auto: update`) a partir de la entity
--   `com.tufondo.socios.infrastructure.persistence.entity.SolicitudRegistroEntity`,
--   pero NO estaba versionada en `db/migration/`. Esto representa un GAP de
--   auditoría detectado y un riesgo para promociones futuras (especialmente
--   cuando se cambie `ddl-auto` a `validate`).
--
--   Esta migración:
--     1. Crea la tabla con `IF NOT EXISTS` para que sea idempotente en
--        ambientes donde la tabla YA existe (VPS producción).
--     2. Agrega tres columnas requeridas para defensa legal LOPDP que NO
--        estaban en la entity Hibernate original:
--          - ip_registro            (IP de origen del registro)
--          - user_agent_registro    (User-Agent del navegador)
--          - consent_lopdp_timestamp (timestamp preciso del consentimiento)
--        Se agregan con `ADD COLUMN IF NOT EXISTS` para idempotencia.
--     3. Añade constraints CHECK con valores válidos de los enums Java.
--     4. Añade índices con nombres explícitos.
--
-- Compatibilidad H2 / PostgreSQL:
--   - NO se usa `gen_random_uuid()` (no soportado en H2 sin extensión).
--     El ID lo genera Hibernate vía `@GeneratedValue(strategy = UUID)`.
--   - NO se usan ENUM nativos de Postgres: se usan VARCHAR + CHECK.
--   - `TIMESTAMP` es portable entre ambos motores.
--   - `NUMERIC(19,4)` para columnas monetarias (convención del proyecto;
--     compatible con `DECIMAL(19,4)` en Postgres y H2).
--   - `IF NOT EXISTS` en CREATE TABLE y ADD COLUMN es soportado en
--     PostgreSQL 9.6+ y H2 en modo PostgreSQL.

-- =========================================================================
-- 1. TABLA: solicitud_registro
-- =========================================================================
CREATE TABLE IF NOT EXISTS solicitud_registro (
    id                       UUID            NOT NULL,

    -- Datos personales
    nombre_completo          VARCHAR(100)    NOT NULL,
    tipo_documento           VARCHAR(20)     NOT NULL,
    cedula                   VARCHAR(20)     NOT NULL,
    fecha_nacimiento         DATE            NOT NULL,
    genero                   VARCHAR(20)     NOT NULL,
    estado_civil             VARCHAR(20)     NOT NULL,

    -- Contacto
    correo_electronico       VARCHAR(255)    NOT NULL,
    telefono                 VARCHAR(20)     NOT NULL,

    -- Datos laborales
    empresa                  VARCHAR(200)    NOT NULL,
    rif_empresa              VARCHAR(20)     NULL,
    departamento             VARCHAR(100)    NULL,
    cargo                    VARCHAR(100)    NULL,
    salario                  NUMERIC(19,4)   NULL,

    -- Dirección
    direccion_estado         VARCHAR(100)    NULL,
    direccion_ciudad         VARCHAR(100)    NULL,
    direccion_municipio      VARCHAR(100)    NULL,
    direccion_calle          VARCHAR(255)    NULL,

    -- Contacto de emergencia
    emergencia_nombre        VARCHAR(200)    NULL,
    emergencia_telefono      VARCHAR(20)     NULL,
    emergencia_parentesco    VARCHAR(50)     NULL,

    -- Workflow de revisión
    estado                   VARCHAR(20)     NOT NULL,
    fecha_solicitud          TIMESTAMP       NOT NULL,
    fecha_revision           TIMESTAMP       NULL,
    revisado_por             VARCHAR(100)    NULL,
    comentario               VARCHAR(500)    NULL,
    motivo_rechazo           TEXT            NULL,

    -- Consentimientos legales
    acepta_terminos          BOOLEAN         NOT NULL,
    acepta_lopdp             BOOLEAN         NOT NULL,

    -- Auditoría
    created_at               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_solicitud_registro PRIMARY KEY (id)
);

-- =========================================================================
-- 2. COLUMNAS LOPDP (no estaban en el schema Hibernate original)
--    Se agregan con IF NOT EXISTS para idempotencia en VPS producción.
-- =========================================================================
ALTER TABLE solicitud_registro
    ADD COLUMN IF NOT EXISTS ip_registro VARCHAR(45) NULL;

ALTER TABLE solicitud_registro
    ADD COLUMN IF NOT EXISTS user_agent_registro VARCHAR(500) NULL;

ALTER TABLE solicitud_registro
    ADD COLUMN IF NOT EXISTS consent_lopdp_timestamp TIMESTAMP NULL;

-- =========================================================================
-- 3. CONSTRAINTS UNIQUE  (idempotentes: solo se crean si no existen)
-- =========================================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_solicitud_registro_cedula
    ON solicitud_registro (cedula);

CREATE UNIQUE INDEX IF NOT EXISTS uq_solicitud_registro_correo
    ON solicitud_registro (correo_electronico);

-- =========================================================================
-- 4. CONSTRAINTS CHECK
--    Valores válidos extraídos de los enums Java:
--      - EstadoSolicitud:  PENDIENTE, APROBADA, RECHAZADA
--      - TipoDocumento:    CEDULA, CEDULA_IDENTIDAD, CEDULA_EXTRANJERO,
--                          PASAPORTE, RIF
--    NOTA: los enums usan la forma femenina (APROBADA/RECHAZADA) porque
--    el sustantivo "solicitud" es femenino en español. NO usar APROBADO
--    / RECHAZADO o Hibernate fallará al deserializar.
-- =========================================================================
ALTER TABLE solicitud_registro
    ADD CONSTRAINT chk_solicitud_registro_estado
    CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'));

ALTER TABLE solicitud_registro
    ADD CONSTRAINT chk_solicitud_registro_tipo_documento
    CHECK (tipo_documento IN ('CEDULA', 'CEDULA_IDENTIDAD',
                              'CEDULA_EXTRANJERO', 'PASAPORTE', 'RIF'));

-- =========================================================================
-- 5. ÍNDICES
--    idx_solicitud_registro_estado_fecha: usado para listar solicitudes
--    pendientes ordenadas (admin dashboard).
--    NOTA: la entity no tiene columna `creado_en`; el ordenamiento natural
--    para el listado del admin es por `fecha_solicitud DESC`.
-- =========================================================================
CREATE INDEX IF NOT EXISTS idx_solicitud_registro_estado_fecha
    ON solicitud_registro (estado, fecha_solicitud DESC);
