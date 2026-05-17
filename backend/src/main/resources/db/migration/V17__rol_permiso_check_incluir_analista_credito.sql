-- =============================================================================
-- V17: HOTFIX - Actualizar CHECK constraint de `rol_permiso.rol` para incluir
--      ANALISTA_CREDITO (regresión introducida por PR #234, issue #207).
-- =============================================================================
--
-- PROBLEMA
-- --------
-- El PR #234 agregó el valor `ANALISTA_CREDITO` al enum `Rol.java` y al
-- `inicializarPermisosDefault()`. Asumió (erróneamente) que Hibernate
-- `ddl-auto: update` actualizaría el CHECK constraint generado para la
-- columna `rol_permiso.rol`. Hibernate NO actualiza CHECK constraints
-- existentes — solo agrega columnas/tablas nuevas.
--
-- Al arrancar el backend en QA tras el merge de #234:
--   PermisoInicializador.run() → inicializarPermisosDefault()
--     → inicializarRol(Rol.ANALISTA_CREDITO, ...)
--       → INSERT INTO rol_permiso (..., rol='ANALISTA_CREDITO')
--         → ERROR: rol_permiso_rol_check rejects the value
--
-- Resultado: backend muere al arrancar → todos los requests dan 500
-- (incluido /api/v1/auth/login que el usuario reportó).
--
-- ACCIÓN
-- ------
-- 1. Drop el constraint viejo (idempotente con IF EXISTS).
-- 2. Recrear con la lista completa actual del enum Rol (7 valores).
--
-- Usamos `to_regclass` para skip silencioso si la tabla aún no existe
-- (caso entorno limpio nuevo, donde Hibernate la creará después).
--
-- NOTA: este patrón se repetirá cada vez que se agregue un valor al enum
-- `Rol`. Considerar (otro issue) migrar a una tabla `roles` real para
-- desacoplar del enum, o usar `TEXT` + validación en aplicación.
-- =============================================================================

DO $$
BEGIN
    IF to_regclass('public.rol_permiso') IS NOT NULL THEN
        -- Drop si existe (Hibernate puede haber autogenerado un nombre
        -- estable o aleatorio; el más común es `rol_permiso_rol_check`).
        ALTER TABLE rol_permiso DROP CONSTRAINT IF EXISTS rol_permiso_rol_check;

        -- Recrear con todos los valores del enum actual.
        ALTER TABLE rol_permiso ADD CONSTRAINT rol_permiso_rol_check
            CHECK (rol IN (
                'SOCIO',
                'ADMIN',
                'SUPER_ADMIN',
                'CAJERO',
                'ANALISTA_KYC',
                'ANALISTA_CREDITO',   -- Issue #207 / hotfix
                'SISTEMA'
            ));

        RAISE NOTICE 'V17: rol_permiso_rol_check actualizado para incluir ANALISTA_CREDITO.';
    ELSE
        RAISE NOTICE 'V17: tabla rol_permiso no existe aún (entorno limpio), skip.';
    END IF;
END $$;
