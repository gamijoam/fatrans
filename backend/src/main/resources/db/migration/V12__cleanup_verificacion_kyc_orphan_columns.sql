-- V12__cleanup_verificacion_kyc_orphan_columns.sql
--
-- Causa raíz: el entity `VerificacionKYCEntity` originalmente mapeaba el campo
-- como `@Column(name="nivel_verificacion")` (la migración V2 creó la columna así).
-- En algún refactor previo se renombró el @Column a `nivel`, pero NO se versionó
-- la migración correspondiente. Hibernate con `ddl-auto: update` agregó la nueva
-- columna `nivel NOT NULL` y dejó `nivel_verificacion NOT NULL` huérfana.
--
-- Resultado: cada INSERT (e.g. al aprobar una solicitud de registro y disparar
-- el auto-KYC) fallaba con
--
--   ERROR: null value in column "nivel_verificacion" of relation
--   "verificacion_kyc" violates not-null constraint
--
-- porque Hibernate llenaba `nivel` pero la columna huérfana `nivel_verificacion`
-- quedaba null y violaba la constraint.
--
-- Fix: dropear las columnas huérfanas que Hibernate ya reemplazó con nombres
-- nuevos (idempotente: IF EXISTS por si la tabla aún no tiene la columna en
-- algún entorno futuro).
--
-- Columnas huérfanas conocidas (V2 → entity actual):
--   nivel_verificacion  → reemplazada por `nivel`
--   fecha_finalizacion  → reemplazada por `fecha_completado`
--   score_confianza     → eliminada del modelo (no aparece en la entity actual)
--
-- Si en algún entorno legacy hay datos en `nivel_verificacion`, los volcamos a
-- `nivel` antes de dropear para no perder información.

DO $$
BEGIN
    -- 1. Sincronizar datos antes de dropear (si la columna vieja todavía existe)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'verificacion_kyc' AND column_name = 'nivel_verificacion'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'verificacion_kyc' AND column_name = 'nivel'
    ) THEN
        UPDATE verificacion_kyc
        SET nivel = nivel_verificacion
        WHERE nivel IS NULL AND nivel_verificacion IS NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'verificacion_kyc' AND column_name = 'fecha_finalizacion'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'verificacion_kyc' AND column_name = 'fecha_completado'
    ) THEN
        UPDATE verificacion_kyc
        SET fecha_completado = fecha_finalizacion
        WHERE fecha_completado IS NULL AND fecha_finalizacion IS NOT NULL;
    END IF;
END $$;

-- 2. Drop columnas huérfanas (idempotente).
ALTER TABLE verificacion_kyc DROP COLUMN IF EXISTS nivel_verificacion;
ALTER TABLE verificacion_kyc DROP COLUMN IF EXISTS fecha_finalizacion;
ALTER TABLE verificacion_kyc DROP COLUMN IF EXISTS score_confianza;
