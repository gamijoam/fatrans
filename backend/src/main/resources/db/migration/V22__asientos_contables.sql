-- ═══════════════════════════════════════════════════════════════════════
--  V22 — Asientos contables (partida doble) + Partidas de asiento
-- ═══════════════════════════════════════════════════════════════════════
--
--  Issues #265 + #266 del EPIC Contabilidad #263.
--
--  Modelo:
--    asientos_contables  → cabecera del asiento (fecha, glosa, origen, estado)
--    partidas_asientos   → renglones del asiento (cuenta + debe/haber)
--
--  Invariantes de partida doble (la base del sistema contable):
--    1. Cada asiento tiene >= 2 partidas.
--    2. Suma de los DEBE = Suma de los HABER (asiento balanceado).
--    3. Cada partida tiene debe>0 XOR haber>0 (nunca ambos, nunca ninguno).
--    4. Las cuentas referenciadas deben aceptar_movimientos=TRUE y activa=TRUE.
--
--  Las invariantes 1 y 2 NO se pueden expresar como CHECK constraints
--  per-row en SQL estándar (requieren agregación). Se validan en el
--  servicio Java (AsientoContableService.registrar). La invariante 3 SÍ
--  se expresa como CHECK per-row.
--
--  ⚠️  Los asientos NUNCA se DELETEan — están protegidos por FK con
--      ON DELETE RESTRICT. Para "borrar" un asiento se crea un asiento de
--      reversión (sub-issue #273) que refiere al original vía
--      asiento_reversa_id. Esto preserva la auditoría histórica
--      requerida por SUDECA.
-- ═══════════════════════════════════════════════════════════════════════

-- ─── Cabecera: asientos_contables ──────────────────────────────────────
CREATE TABLE asientos_contables (
    id                      UUID PRIMARY KEY,

    -- Número correlativo del asiento. Generado por secuencia (no por la app)
    -- para garantizar unicidad y orden incluso bajo concurrencia.
    numero                  BIGINT NOT NULL UNIQUE,

    -- Fecha contable: la fecha que se reporta en los libros (puede ser
    -- distinta de created_at, ej. asientos de ajuste retroactivo).
    fecha_contable          DATE NOT NULL,

    -- Descripción del asiento (glosa general). Cada partida puede tener su
    -- propia glosa adicional en partidas_asientos.glosa.
    glosa                   VARCHAR(500) NOT NULL,

    -- Qué disparó el asiento. Permite filtrar reportes ("todos los depósitos
    -- del mes") y trazar el origen hacia el módulo de negocio.
    origen                  VARCHAR(30) NOT NULL
        CHECK (origen IN (
            'AHORRO_DEPOSITO', 'AHORRO_RETIRO', 'AHORRO_INTERES',
            'CREDITO_DESEMBOLSO', 'CREDITO_COBRO', 'CREDITO_INTERES',
            'MANUAL', 'CIERRE', 'REVERSION', 'AJUSTE'
        )),

    -- Referencia al evento que originó el asiento (ej. número de operación
    -- del movimiento de ahorros, número de cuota de crédito, etc.). Útil
    -- para reconciliar y para click-through desde reportes.
    referencia_externa      VARCHAR(100),

    -- Estado del asiento. ANULADO no significa "borrado" — sigue en BD
    -- para auditoría pero ya no afecta saldos. Anular requiere generar un
    -- asiento de reversión que cierra el balance (manejado en código).
    estado                  VARCHAR(15) NOT NULL DEFAULT 'REGISTRADO'
        CHECK (estado IN ('REGISTRADO', 'ANULADO')),

    -- Usuario que registró el asiento. Nullable porque los asientos
    -- generados por sistema (hooks contables automáticos de ahorros/créditos)
    -- no tienen usuario humano.
    creado_por_usuario_id   UUID,

    motivo_anulacion        VARCHAR(500),

    -- Si este es un asiento de reversión, apunta al asiento original que
    -- está reversando. Permite la traza completa de cambios contables.
    asiento_reversa_id      UUID,

    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                 BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_asiento_reversa
        FOREIGN KEY (asiento_reversa_id) REFERENCES asientos_contables(id)
        ON DELETE RESTRICT
);

COMMENT ON TABLE asientos_contables IS
'Cabecera de asientos contables (partida doble). Cada asiento tiene >=2 partidas en partidas_asientos. NUNCA se DELETEa — se anula con motivo y se genera asiento de reversión.';

COMMENT ON COLUMN asientos_contables.numero IS
'Correlativo único auto-generado por secuencia seq_asiento_numero. Se usa para el Libro Diario oficial.';

COMMENT ON COLUMN asientos_contables.fecha_contable IS
'Fecha que aparece en los libros legales. Puede diferir de created_at en ajustes retroactivos.';

CREATE INDEX idx_asientos_fecha       ON asientos_contables(fecha_contable);
CREATE INDEX idx_asientos_origen      ON asientos_contables(origen);
CREATE INDEX idx_asientos_estado      ON asientos_contables(estado);
CREATE INDEX idx_asientos_referencia  ON asientos_contables(referencia_externa);
CREATE INDEX idx_asientos_reversa     ON asientos_contables(asiento_reversa_id);

-- Secuencia para el correlativo. Definida en BD (no en código) para que
-- soporte concurrencia: dos requests simultáneos obtienen números
-- consecutivos sin race condition. INCREMENT 1, sin caché para evitar
-- huecos en el correlativo (los libros legales exigen continuidad).
CREATE SEQUENCE seq_asiento_numero
    START WITH 1
    INCREMENT BY 1
    NO CYCLE
    CACHE 1;

COMMENT ON SEQUENCE seq_asiento_numero IS
'Correlativo único de asientos contables. CACHE 1 para garantizar continuidad (sin huecos) — los libros legales exigen secuencia ininterrumpida.';

-- ─── Detalle: partidas_asientos ────────────────────────────────────────
CREATE TABLE partidas_asientos (
    id              UUID PRIMARY KEY,

    asiento_id      UUID NOT NULL,
    cuenta_id       UUID NOT NULL,

    -- Montos en NUMERIC(18,4): hasta 14 dígitos enteros + 4 decimales.
    -- Suficiente para el peor caso de hiperinflación venezolana sin
    -- pérdida de precisión.
    debe            NUMERIC(18,4) NOT NULL DEFAULT 0,
    haber           NUMERIC(18,4) NOT NULL DEFAULT 0,

    -- Posición de la partida en el asiento (para preservar el orden de
    -- presentación que el contador definió).
    orden           INTEGER NOT NULL,

    glosa           VARCHAR(300),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- INVARIANTE FUNDAMENTAL: una partida es del DEBE o del HABER, nunca
    -- ambas, nunca ninguna. Se valida en BD como defensa en profundidad
    -- (la app también lo valida).
    CONSTRAINT chk_partida_debe_xor_haber
        CHECK ((debe > 0 AND haber = 0) OR (debe = 0 AND haber > 0)),

    CONSTRAINT chk_partida_no_negativos
        CHECK (debe >= 0 AND haber >= 0),

    CONSTRAINT fk_partida_asiento
        FOREIGN KEY (asiento_id) REFERENCES asientos_contables(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_partida_cuenta
        FOREIGN KEY (cuenta_id) REFERENCES plan_cuentas(id)
        ON DELETE RESTRICT
);

COMMENT ON TABLE partidas_asientos IS
'Renglones (partidas) de cada asiento. La invariante de partida doble (Σdebe=Σhaber) se valida en el servicio, no en BD (requiere agregación cross-row).';

CREATE INDEX idx_partidas_asiento ON partidas_asientos(asiento_id);
CREATE INDEX idx_partidas_cuenta  ON partidas_asientos(cuenta_id);

-- Índice compuesto para el Libro Mayor (saldos por cuenta en un rango).
-- Cubrirá la query típica: "todas las partidas de la cuenta X entre fechas".
-- Lo hacemos cross-tabla con un índice en partidas + JOIN al asiento.
CREATE INDEX idx_partidas_cuenta_asiento ON partidas_asientos(cuenta_id, asiento_id);
