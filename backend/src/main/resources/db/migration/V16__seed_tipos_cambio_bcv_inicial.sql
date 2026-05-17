-- =============================================================================
-- V16: Issue #231 - Seed inicial de tipo de cambio BCV
-- =============================================================================
--
-- CONTEXTO
-- --------
-- La tabla `tipos_cambio` la crea Hibernate (`ddl-auto: update`) pero NO tiene
-- seed. El dashboard del socio depende de un valor inicial para mostrar el
-- saldo agregado VES+USD (issue #213 + #230). Sin seed:
--   1. En entornos limpios (QA reciente, dev), el dashboard cae al modo
--      fallback (saldos separados) — funciona pero no es ideal.
--   2. El job programado de scraping BCV (`BcvSyncService.ejecutarJobDiario`)
--      corre 1x al día a las 08:30 hora Venezuela. Si el deploy es a otra
--      hora, hay una ventana donde no hay tasa.
--
-- ACCIÓN
-- ------
-- Insertar una tasa BCV inicial usando el valor del día del deploy
-- (2026-05-19, capturado de bcv.org.ve: 1 USD = Bs 517,96190000).
--
-- IDEMPOTENTE: usamos `ON CONFLICT DO NOTHING` sobre la fecha — si ya hay
-- una tasa para esa fecha (ej. el job ya corrió antes que la migración),
-- no se duplica.
--
-- USO DE `to_regclass`: la tabla `tipos_cambio` es creada por Hibernate, no
-- por Flyway. En el primer arranque limpio, esta migración corre ANTES de
-- que Hibernate cree la tabla. Skip silencioso si no existe.
--
-- NOTA: el valor se actualiza luego automáticamente por el job BCV.
-- Esta migración solo asegura que haya UN valor inicial para evitar el
-- estado "vacío".
-- =============================================================================

DO $$
BEGIN
    IF to_regclass('public.tipos_cambio') IS NOT NULL THEN
        -- Crear índice único en `fecha` si no existe (necesario para ON CONFLICT)
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes
            WHERE schemaname = 'public'
              AND tablename = 'tipos_cambio'
              AND indexname = 'uk_tipos_cambio_fecha'
        ) THEN
            BEGIN
                CREATE UNIQUE INDEX uk_tipos_cambio_fecha
                    ON tipos_cambio(fecha);
            EXCEPTION WHEN unique_violation THEN
                RAISE NOTICE 'V16: índice uk_tipos_cambio_fecha ya existe, skip.';
            END;
        END IF;

        -- Insert idempotente del seed BCV inicial.
        -- Valor capturado de bcv.org.ve el 2026-05-19.
        INSERT INTO tipos_cambio (
            id, fecha, tasa_compra, tasa_venta, fuente, creado_por, created_at
        ) VALUES (
            gen_random_uuid(),
            DATE '2026-05-19',
            517.96190000,
            517.96190000,
            'BCV',
            NULL,
            now()
        )
        ON CONFLICT (fecha) DO NOTHING;

        RAISE NOTICE 'V16: seed BCV inicial procesado (insertado o ya existente).';
    ELSE
        RAISE NOTICE 'V16: tabla tipos_cambio no existe aún, skip (la creará Hibernate).';
    END IF;
END $$;
