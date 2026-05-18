-- =========================================================================
-- V20__locdoft_operacion_grande.sql
--
-- Issue #218 PR-C: declaración jurada LOCDOFT en operaciones grandes
-- (depósito o retiro > umbral configurable).
--
-- Cuando un socio realiza una operación cuyo monto supera el umbral
-- LOCDOFT configurado en `parametros_sistema`, el frontend muestra un
-- modal con la pregunta "¿Confirmas el origen lícito de los fondos?"
-- y un campo opcional "Origen de los fondos". La aceptación se registra
-- por operación en la tabla `consentimiento_locdoft_operacion` para
-- trazabilidad legal (Art. 9 LOCDOFT — los sujetos obligados deben
-- conservar declaraciones por al menos 5 años).
-- =========================================================================

-- 1. Seeds de los umbrales (idempotente — sólo inserta si la clave no existe)
--
-- NOTA HISTÓRICA: la tabla `parametros_sistema` tiene DOS columnas NOT NULL
-- duplicadas (`clave` de la migración V8 + `param_key` agregada por
-- Hibernate `ddl-auto: update` desde el campo `key` de la entidad JPA).
-- Cualquier INSERT directo desde SQL debe llenar AMBAS, sino viola el
-- NOT NULL constraint de `param_key`. (Pendiente: unificar esto en una
-- migración futura que dropee `clave` y deje solo `param_key`.)
INSERT INTO parametros_sistema (clave, param_key, valor, tipo, descripcion, categoria, editable)
SELECT 'LOCDOFT_UMBRAL_VES', 'LOCDOFT_UMBRAL_VES', '10000.00', 'CURRENCY',
       'Umbral en bolívares para exigir declaración jurada LOCDOFT en operaciones (depósito/retiro). Por defecto Bs 10.000 (aprox equivalente a USD 1.000 con tasa BCV media histórica).',
       'compliance', TRUE
WHERE NOT EXISTS (SELECT 1 FROM parametros_sistema WHERE clave = 'LOCDOFT_UMBRAL_VES');

INSERT INTO parametros_sistema (clave, param_key, valor, tipo, descripcion, categoria, editable)
SELECT 'LOCDOFT_UMBRAL_USD', 'LOCDOFT_UMBRAL_USD', '1000.00', 'CURRENCY',
       'Umbral en USD para exigir declaración jurada LOCDOFT en operaciones (depósito/retiro). Recomendación FATF: USD 1.000 para reporting de operaciones.',
       'compliance', TRUE
WHERE NOT EXISTS (SELECT 1 FROM parametros_sistema WHERE clave = 'LOCDOFT_UMBRAL_USD');

-- 2. Tabla de consentimientos por operación
--
-- Diseño: una fila por operación que requirió declaración. NO se sobreescribe
-- ni se borra (append-only conceptual, enforcement formal en EPIC #251 con
-- audit_log central). El movimiento_id queda nullable porque el consentimiento
-- se valida ANTES de crear el movimiento — si la operación falla por otra razón
-- (saldo insuficiente en retiro, validación de límite, etc.) el consentimiento
-- igual queda registrado como evidencia del intento + declaración del socio.
CREATE TABLE IF NOT EXISTS consentimiento_locdoft_operacion (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    socio_id UUID NOT NULL,
    cuenta_ahorro_id UUID NOT NULL,
    movimiento_id UUID NULL,                       -- FK soft a movimientos.id

    tipo_operacion VARCHAR(20) NOT NULL,           -- 'DEPOSITO' | 'RETIRO'
    monto NUMERIC(18, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL,                    -- 'VES' | 'USD'
    umbral_aplicado NUMERIC(18, 2) NOT NULL,       -- snapshot del umbral vigente al momento

    acepta_origen_licito BOOLEAN NOT NULL,         -- declaración jurada del socio
    origen_fondos TEXT NULL,                       -- texto libre opcional (PEP, etc.)

    -- Trazabilidad legal (defensa para auditorías SUDECA/UNIF)
    ip_origen VARCHAR(45) NULL,
    user_agent TEXT NULL,
    session_id VARCHAR(64) NULL,
    request_id VARCHAR(64) NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_locdoft_tipo CHECK (tipo_operacion IN ('DEPOSITO', 'RETIRO')),
    CONSTRAINT chk_locdoft_moneda CHECK (moneda IN ('VES', 'USD'))
);

-- Índices para consultas comunes (admin/cumplimiento)
CREATE INDEX IF NOT EXISTS idx_locdoft_op_socio_fecha
    ON consentimiento_locdoft_operacion (socio_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_locdoft_op_movimiento
    ON consentimiento_locdoft_operacion (movimiento_id)
    WHERE movimiento_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_locdoft_op_monto_fecha
    ON consentimiento_locdoft_operacion (created_at DESC, monto DESC);

COMMENT ON TABLE consentimiento_locdoft_operacion IS
    'Declaraciones juradas LOCDOFT por operación financiera grande (#218 PR-C). Append-only conceptual — la evidencia debe conservarse mínimo 5 años (Art. 9 LOCDOFT).';

COMMENT ON COLUMN consentimiento_locdoft_operacion.movimiento_id IS
    'NULL si el consentimiento se registró pero la operación falló después por otra razón. Esto es deseable para la auditoría: queda evidencia del intento y la declaración del socio.';

COMMENT ON COLUMN consentimiento_locdoft_operacion.umbral_aplicado IS
    'Snapshot del umbral vigente en parametros_sistema al momento de la operación. Si el umbral cambia después, no afecta el registro histórico.';
