-- =========================================================================
-- V19__add_consent_locdoft.sql
--
-- Issue #218 PR-B: añadir consentimiento LOCDOFT (Ley Orgánica contra la
-- Delincuencia Organizada y Financiamiento al Terrorismo) al flujo de
-- registro. Obligación legal — los sujetos obligados (Fatrans lo es como
-- fondo de ahorro) deben recolectar declaración jurada de origen lícito
-- de los fondos.
--
-- Patrón: idéntico a `acepta_lopdp` + `consent_lopdp_timestamp` que ya
-- vive en `solicitud_registro` (V11). Por consistencia, NO creamos tabla
-- aparte — mantenemos los 3 consentimientos del registro en la misma fila.
-- Si en el futuro se quiere unificar todos los consentimientos en una
-- sola tabla `consentimientos`, será un refactor independiente que
-- migrará TODOS los flags (términos, LOPDP, LOCDOFT) a la vez.
--
-- Idempotente (IF NOT EXISTS) para que se pueda re-aplicar en VPS prod
-- sin romper si por alguna razón ya existe.
-- =========================================================================

ALTER TABLE solicitud_registro
    ADD COLUMN IF NOT EXISTS acepta_locdoft BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE solicitud_registro
    ADD COLUMN IF NOT EXISTS consent_locdoft_timestamp TIMESTAMP NULL;

-- Quitar el default después del backfill: para nuevos registros queremos
-- que el campo sea obligatorio (la app siempre lo manda).
-- En filas existentes (registros previos al issue), queda FALSE — esos
-- usuarios no aceptaron LOCDOFT porque no existía la pantalla. Se les
-- puede solicitar re-aceptar en su próximo login si se considera
-- necesario (fuera de scope de este PR).
ALTER TABLE solicitud_registro
    ALTER COLUMN acepta_locdoft DROP DEFAULT;

COMMENT ON COLUMN solicitud_registro.acepta_locdoft IS
    'Declaración jurada de origen lícito de fondos (LOCDOFT). Obligatorio. Debe ser TRUE para que la solicitud sea válida.';

COMMENT ON COLUMN solicitud_registro.consent_locdoft_timestamp IS
    'Timestamp UTC sellado por el backend cuando se recibe acepta_locdoft=TRUE. Junto con ip_registro y user_agent_registro forma la traza legal para defensa en juicio.';
