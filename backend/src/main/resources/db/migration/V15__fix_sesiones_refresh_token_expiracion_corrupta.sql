-- =============================================================================
-- V15: Issue #178 - Limpieza de sesiones con refresh_token_expiracion corrupta
-- =============================================================================
--
-- CONTEXTO
-- --------
-- Antes del fix de issue #178, `AuthUseCase.refreshTokenExpiracion(String)`
-- usaba `Instant.now().plusSeconds(jwtService.extraerExpiracionAccessToken(token)
-- .getEpochSecond())`. El método `getEpochSecond()` retorna un epoch ABSOLUTO
-- (segundos desde 1970), pero el código lo sumaba a `Instant.now()` como si
-- fuera un offset RELATIVO. Resultado: las filas insertadas durante ese período
-- tienen `refresh_token_expiracion` en el año ~3025+, lo cual:
--
--   1. Hace que la verificación `Sesion.estaExpirado()` nunca retorne true
--      por expiración (solo por `activo = false`).
--   2. Si se roba un refresh token, sigue siendo válido por años.
--
-- ACCIÓN
-- ------
-- Invalidar (`activo = false`) toda sesión cuya `refresh_token_expiracion`
-- esté más allá del rango razonable (> 1 año desde ahora). Esto obliga a
-- los usuarios afectados a re-autenticarse, generando una nueva sesión con
-- expiración correcta tras el fix.
--
-- SEGURIDAD: Usamos `to_regclass` porque la tabla `sesiones` es creada por
-- Hibernate `ddl-auto: update`, no por Flyway. En entornos limpios o
-- ambientes donde la tabla aún no exista al momento de aplicar esta
-- migración, evitamos el fallo del script.
-- =============================================================================

DO $$
BEGIN
    IF to_regclass('public.sesiones') IS NOT NULL THEN
        UPDATE sesiones
        SET activo = false
        WHERE activo = true
          AND refresh_token_expiracion > (now() + INTERVAL '1 year');

        -- Log informativo: cuántas filas fueron afectadas
        RAISE NOTICE 'V15: invalidadas % sesiones con refresh_token_expiracion corrupta',
            (SELECT count(*) FROM sesiones
             WHERE activo = false
               AND refresh_token_expiracion > (now() + INTERVAL '1 year'));
    ELSE
        RAISE NOTICE 'V15: tabla sesiones no existe aún, skip (será creada por Hibernate)';
    END IF;
END $$;
