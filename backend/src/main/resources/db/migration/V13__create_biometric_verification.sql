-- V13__create_biometric_verification.sql
--
-- Capa de persistencia para el módulo de verificación biométrica del KYC.
--
-- Decisiones de diseño:
--
-- 1. Tabla SEPARADA `biometric_verification`, no columnas en `verificacion_kyc`.
--    Una solicitud KYC puede generar varios intentos biométricos (usuario reintenta
--    tras un fallo de liveness). Mantenemos el histórico para auditoría LOPDP.
--
-- 2. NO almacenamos el template biométrico ni la imagen completa — solo:
--      • scores (numéricos, no reversibles a imagen)
--      • session_id del proveedor (referencia para auditoría / revocación)
--    Esto cumple el principio de minimización LOPDP (datos biométricos sensibles).
--
-- 3. La columna `estado_biometria` en `verificacion_kyc` es un cache de "¿pasó la
--    biometría?" para no tener que JOIN cada vez que listamos la cola del admin.
--    Se mantiene sincronizada por el use case que registra el resultado del webhook.
--
-- 4. `biometric_consent` es una tabla aparte porque el consentimiento de datos
--    biométricos en LOPDP venezolana exige separación del consentimiento general,
--    versionado, revocable y con trazabilidad (ip, user-agent, timestamp).

-- 1. Tabla principal de intentos biométricos.
CREATE TABLE IF NOT EXISTS biometric_verification (
    id                      UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    verificacion_kyc_id     UUID            NOT NULL REFERENCES verificacion_kyc(id) ON DELETE CASCADE,
    socio_id                UUID            NOT NULL REFERENCES socios(id) ON DELETE CASCADE,

    -- Identificación del proveedor (Didit, AWS Rekognition, etc.) — permite swap futuro.
    proveedor               VARCHAR(50)     NOT NULL,
    proveedor_session_id    VARCHAR(255)    NOT NULL,   -- referencia opaca del proveedor
    proveedor_workflow_id   VARCHAR(100),               -- workflow específico (Didit usa workflow_id)

    -- Estado del intento individual.
    estado                  VARCHAR(30)     NOT NULL,
    -- Scores 0.00–1.00 (NUMERIC(5,4) permite 4 decimales). Null = no medido aún.
    liveness_score          NUMERIC(5,4),
    face_match_score        NUMERIC(5,4),
    document_ocr_score      NUMERIC(5,4),

    -- Resultado textual del proveedor para diagnóstico (no PII).
    motivo_fallo            VARCHAR(500),
    tipo_ataque_detectado   VARCHAR(100),               -- e.g. "SPOOF_PRINT", "REPLAY"

    -- URLs presignadas / referencias a artefactos en MinIO (TTL corto, 90 días).
    selfie_storage_path     VARCHAR(500),
    documento_storage_path  VARCHAR(500),

    -- Auditoría temporal.
    fecha_inicio            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_completado        TIMESTAMP,
    fecha_expiracion_artefactos TIMESTAMP,              -- cuando se borra el selfie de MinIO

    -- IP y user-agent del usuario que inició el intento (LOPDP trazabilidad).
    ip_cliente              VARCHAR(45),
    user_agent              VARCHAR(500),

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT,

    CONSTRAINT chk_biometric_estado CHECK (estado IN (
        'PENDIENTE',
        'EN_PROGRESO',
        'APROBADA',
        'RECHAZADA',
        'EXPIRADA',
        'CANCELADA'
    )),
    CONSTRAINT chk_biometric_scores CHECK (
        (liveness_score      IS NULL OR (liveness_score      BETWEEN 0 AND 1)) AND
        (face_match_score    IS NULL OR (face_match_score    BETWEEN 0 AND 1)) AND
        (document_ocr_score  IS NULL OR (document_ocr_score  BETWEEN 0 AND 1))
    )
);

CREATE INDEX IF NOT EXISTS idx_biometric_verificacion_kyc
    ON biometric_verification (verificacion_kyc_id);

CREATE INDEX IF NOT EXISTS idx_biometric_socio
    ON biometric_verification (socio_id);

CREATE INDEX IF NOT EXISTS idx_biometric_estado_fecha
    ON biometric_verification (estado, fecha_inicio DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uq_biometric_proveedor_session
    ON biometric_verification (proveedor, proveedor_session_id);


-- 2. Cache de estado en verificacion_kyc para listados rápidos.
ALTER TABLE verificacion_kyc
    ADD COLUMN IF NOT EXISTS estado_biometria VARCHAR(30);

-- Default para registros previos: NO_INICIADA (no rompe nada, semánticamente correcto).
UPDATE verificacion_kyc
    SET estado_biometria = 'NO_INICIADA'
    WHERE estado_biometria IS NULL;

ALTER TABLE verificacion_kyc
    ALTER COLUMN estado_biometria SET NOT NULL,
    ALTER COLUMN estado_biometria SET DEFAULT 'NO_INICIADA';

ALTER TABLE verificacion_kyc
    DROP CONSTRAINT IF EXISTS chk_verificacion_kyc_estado_biometria;
ALTER TABLE verificacion_kyc
    ADD CONSTRAINT chk_verificacion_kyc_estado_biometria CHECK (estado_biometria IN (
        'NO_INICIADA',
        'EN_PROGRESO',
        'APROBADA',
        'RECHAZADA',
        'EXPIRADA'
    ));


-- 3. Consentimiento LOPDP biométrico (separado del consentimiento KYC general).
CREATE TABLE IF NOT EXISTS biometric_consent (
    id                          UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id                    UUID            NOT NULL REFERENCES socios(id) ON DELETE CASCADE,

    -- Versión del texto de consentimiento aceptado. Cambiamos versión cuando cambie el
    -- texto (cambio de proveedor, cambio de país de procesamiento, etc.).
    version_politica            VARCHAR(20)     NOT NULL,
    proveedor_destino           VARCHAR(50)     NOT NULL,    -- e.g. "DIDIT_ES"
    pais_procesamiento          VARCHAR(50)     NOT NULL,    -- e.g. "ES" (España)

    aceptado                    BOOLEAN         NOT NULL,
    fecha_consentimiento        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_revocacion            TIMESTAMP,                   -- LOPDP Art. 7: derecho a revocar

    ip_cliente                  VARCHAR(45),
    user_agent                  VARCHAR(500),

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_biometric_consent_revocacion CHECK (
        (fecha_revocacion IS NULL) OR (fecha_revocacion >= fecha_consentimiento)
    )
);

CREATE INDEX IF NOT EXISTS idx_biometric_consent_socio_activo
    ON biometric_consent (socio_id) WHERE fecha_revocacion IS NULL;
