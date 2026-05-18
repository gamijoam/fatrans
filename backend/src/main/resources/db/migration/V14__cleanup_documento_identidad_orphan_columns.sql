-- V14: Limpieza de columnas huérfanas en documento_identidad
--
-- CONTEXTO: la tabla `documento_identidad` tiene dos columnas NOT NULL que la
-- entity actual NO mapea, dejadas por un schema anterior:
--   - `url_documento` (sustituida por `url_almacenamiento`)
--   - `estado_validacion` (sustituida por `estado`)
--
-- Sin esta migración, todo INSERT desde la entity falla con
-- "null value in column ... violates not-null constraint" porque Hibernate
-- nunca pasa valor para esas columnas (no existen en el modelo).
--
-- Misma motivación que V12 hizo para `verificacion_kyc.nivel_verificacion`.

-- 1. Backfill por si ya hay filas con esas columnas pobladas, para que el
--    DROP no rompa por dependencias. (No debería haber filas — el INSERT
--    fallaba siempre — pero somos defensivos.)
UPDATE documento_identidad
SET url_documento = COALESCE(url_documento, url_almacenamiento),
    estado_validacion = COALESCE(estado_validacion, estado)
WHERE url_documento IS NULL OR estado_validacion IS NULL;

-- 2. Drop columnas obsoletas.
ALTER TABLE documento_identidad DROP COLUMN IF EXISTS url_documento;
ALTER TABLE documento_identidad DROP COLUMN IF EXISTS estado_validacion;
