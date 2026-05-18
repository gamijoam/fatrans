# REAUDITORIA - Flujo de Registro (Fase 1 cerrada)

**Fecha:** 2026-05-15
**Auditor:** Claude Code agent (general-purpose)
**Alcance:** Verificacion de cierre de los 6 criticos + 5 altos del audit original.
**Auditoria base:** [[auditoria_20260515_REGISTRO_FLUJO]] (referenciada por usuario; archivo no presente en repo, pero los 17 hallazgos vienen enumerados en el brief).
**Branch:** `fix/registration-flow-phase-1` (13 ficheros modificados / 5 ficheros nuevos vs `develop`).

## VEREDICTO

Mergeable a `develop` con reservas. Los hallazgos puramente Fase 1 (mapping del repository, password en logs, rate-limit, BFF reenviando todos los campos, captura LOPDP server-side, migracion Flyway) estan cerrados con tests de regresion solidos. **Quedan abiertos #3 (captcha), #5 (validacion RIF) y #6 (DTO/handler de admin sin enmascarar PII) y #4 (enumeracion via mensajes de exception).** #3 y #5 son Fase 2/3 segun el brief. #4 y #6 son hallazgos Fase 1 que el fix declaro abordar pero NO se tocaron en este branch: `CedulaDuplicadaException`/`CorreoDuplicadoException` siguen devolviendo el valor en el mensaje y los handlers (`SociosExceptionHandler.java:30,35`, `GlobalExceptionHandler.java:83,88`) lo propagan al cliente; el `SolicitudRegistroResponseDTO` sigue exponiendo cedula/correo/telefono sin mask. Recomendacion: cerrar #4 y #6 antes del merge — son cambios chicos y siguen siendo criticos.

## ESTADO POR HALLAZGO

| # | Hallazgo original | Estado | Evidencia |
|---|---|---|---|
| 🔴 #1 | Repository mapping incompleto (15+ campos perdidos, incluidos consentimientos LOPDP) | **CERRADO** | `SolicitudRegistroRepositoryImpl.java:71-147` mapea los 35 campos en ambos sentidos (toEntity/toDomain), incluyendo `aceptaTerminos`, `aceptaLopdp`, `ipRegistro`, `userAgentRegistro`, `consentLopdpTimestamp`. Test: `SolicitudRegistroRepositoryImplTest.persisteCamposLopdp` + `preservaConsentimientosLegales`. |
| 🔴 #2 | Password temporal logueada en plain text | **CERRADO** | `AprobarSolicitudUseCase.java:157-160`: el log final solo registra `solicitudId`, `socioId`, `nombreUsuario` — nunca `passwordTemporal`. `EmailNotificationServiceImpl.java:17-21`: comentario explicito "NUNCA loguear la contraseña" y solo loguea `nombreUsuario`. Ademas se reemplazo `ThreadLocalRandom` por `SecureRandom` (l.215-243). Test: `AprobarSolicitudUseCaseTest.passwordTemporalNoAparereEnLogs` + `passwordsConsecutivosDistintos`. |
| 🔴 #3 | Sin captcha / anti-bot en endpoint publico | **AUN ABIERTO (Fase 2/3)** | Grep `captcha|recaptcha|hcaptcha|turnstile` en todo el repo: 0 matches. No es regresion — el brief lo marca como diferido. Mitigacion parcial via rate-limit (3/min, #7). |
| 🔴 #4 | Enumeracion de cedulas/correos via mensajes especificos | **ABIERTO** (fix NO aplicado) | `CedulaDuplicadaException.java:7` aun construye `"Ya existe una solicitud o socio con la cédula: " + cedula`; idem `CorreoDuplicadoException.java:7`. Los dos handlers (`SociosExceptionHandler.java:28-36` y `GlobalExceptionHandler.java:81-89`) reenvian `ex.getMessage()` con codigos `CEDULA_DUPLICADA`/`EMAIL_DUPLICADO`. Atacante puede enumerar usuarios. Riesgo alto en endpoint publico sin captcha. |
| 🔴 #5 | Sin validacion de RIF en backend | **AUN ABIERTO (Fase 2/3)** | `SolicitudRegistroRequestDTO.java:58` sigue siendo `private String rifEmpresa;` sin `@Pattern`. Brief lo califica como diferible. |
| 🔴 #6 | PII completa en respuesta admin sin mascara/permisos finos | **ABIERTO** (fix NO aplicado) | `SolicitudRegistroResponseDTO.java:20-34` expone `cedula`, `correoElectronico`, `telefono`, `nombreCompleto`, `empresa`, `revisadoPor` sin mask y sin distincion entre rol ADMIN listando vs detalle individual. No hay DTO separado para listado vs detalle. |
| 🟠 #7 | Rate-limit roto (apuntaba a endpoint inexistente) | **CERRADO** | `GlobalRateLimitFilter.java:38` ya apunta a `/api/v1/socios/solicitud` con `3 req/min`. `findMatchingConfig` (l.100-120) implementa correctamente prioridad exact > prefijo mas largo (wildcard). Tests: `registroBloqueaCuartaRequest`, `reglaEspecificaPrevaleceSobreWildcard`, `otrosEndpointsSociosNoUsanLimiteDeRegistro` — 5 tests en total. |
| 🟠 #11 | BFF `/api/auth/registro/route.ts` reenviaba solo 5 de 22 campos | **CERRADO** | `frontend-web/src/app/api/auth/registro/route.ts:91-136` arma `backendPayload` con los 22 campos del schema + `aceptaTerminos`, `aceptaLopdp`, `ipRegistro`, `userAgentRegistro` (24 props). Helper `getClientIp` extrae primer hop de `x-forwarded-for`. Test cubierto en `route.test.ts` (13 casos, incluyen "reenvía TODOS los campos", "ipRegistro primer hop", null fallback, 5xx mask, 4xx propagate). |
| GAP | Migracion Flyway faltante (tabla creada por Hibernate) | **CERRADO** | `V11__create_solicitud_registro.sql` nuevo con `CREATE TABLE IF NOT EXISTS` + las 3 columnas LOPDP via `ADD COLUMN IF NOT EXISTS`. Header documenta intent (idempotencia + portabilidad H2/Postgres). Constraints CHECK y UNIQUE incluidos. |
| Tests rotos | ¿se elimino algun test existente? | **NO** | `git diff develop -- backend/src/test` solo muestra modificacion (no eliminacion) en `AprobarSolicitudUseCaseTest.java`. 3 tests nuevos en backend + 1 nuevo en frontend. |

> Los demas hallazgos del audit original (🔴 #1-#6, 🟠 #7-#11, 🟡 #12-#15, ⚪ #16-#17) no estaban enumerados en el brief y por tanto fuera del alcance verificable de esta reauditoria. Los hallazgos altos que SI estaban en el brief (#7 y #11) figuran arriba.

## NUEVOS HALLAZGOS DETECTADOS DURANTE LA REAUDITORIA

- **🟡 N-1 — Inconsistencia de schema entre Flyway y JPA entity.** `V11__create_solicitud_registro.sql:57` declara `salario NUMERIC(19,4)`, pero `SolicitudRegistroEntity.java:72` lo define como `precision = 18, scale = 2`. Con `ddl-auto: validate` en el futuro, Hibernate fallara al validar. Recomendar alinear (sugerencia: `NUMERIC(19,2)` en SQL o `precision=19, scale=4` en entity). Riesgo bajo hoy porque sigue en `update`, pero la migracion lo introduce como deuda nueva.
- **🟡 N-2 — Indices duplicados.** `V11__...sql:137` crea `idx_solicitud_registro_estado_fecha (estado, fecha_solicitud DESC)`, pero la entity (`SolicitudRegistroEntity.java:23-26`) declara dos indices separados (`idx_solicitud_estado`, `idx_solicitud_fecha`). En primera promocion a Postgres limpio existiran los tres con espacio duplicado.
- **🟡 N-3 — `SocioControllerTest.java` deshabilitado / `@Disabled`?** No verificado; pero el modulo `socios` ahora tiene 3 tests nuevos que requieren Spring context (`@SpringBootTest`, `@DataJpaTest`). Si CI todavia esta en perfil sin H2, podrian quedar verdes solo por silencio. Verificar que el job `mvn test` los recoge.
- **⚪ N-4 — Logging informativo de IP redundante.** El controller ya guarda IP en la entidad; el `GlobalRateLimitFilter.java:77` ademas la loguea con WARN al exceder. No es problema, pero conviene confirmar que esa IP no sale a un log con retencion mayor a la del campo `ip_registro` (consistencia LOPDP).

## TESTS

**Nuevos (4 archivos):**
- `SolicitudRegistroRepositoryImplTest.java` — 7 tests. Cubre `persisteCamposLopdp`, `preservaConsentimientosLegales`, `camposLopdpSonNullables`, existencia y autogeneracion de id/fecha. **Cierra regresion de #1.**
- `AprobarSolicitudUseCaseTest.java` — extendido con 4 tests nuevos en `SeguridadPasswordTests` (Nested): `passwordTemporalNoAparereEnLogs` (typo en nombre: "Apareere"), `passwordTemporalTieneFortalezaMinima`, `passwordSeEntregaAUsuarioCreator`, `passwordsConsecutivosDistintos`. **Cierra regresion de #2.**
- `GlobalRateLimitFilterTest.java` — 5 tests. **Cierra regresion de #7.**
- `SolicitudRegistroControllerTest.java` — 3 tests sobre captura LOPDP (prefiere BFF, fallback servlet, fallback remoteAddr). **Cubre el nuevo `resolveClientIp`.**
- `frontend-web/src/app/api/auth/registro/route.test.ts` — 13 tests Vitest (matches el "13/13" del brief). Cubre payload completo, ipRegistro/userAgent, null fallback, validacion Zod, 403 origin/referer, 5xx mask, 4xx propagate, network error. **Cierra regresion de #11.**

**Brechas de cobertura:**
- Cero tests sobre `GlobalExceptionHandler`/`SociosExceptionHandler` para verificar que el mensaje al cliente NO expone PII (relacionado con #4 abierto).
- Cero tests sobre `CrearSolicitudRegistroUseCase` directamente (la captura LOPDP server-side via `Instant.now()` no esta cubierta por unit test propio; solo indirectamente por el repository test).
- Cero tests sobre el endpoint admin que sirve `SolicitudRegistroResponseDTO` (#6).

**Typo menor:** `passwordTemporalNoAparereEnLogs` deberia ser `passwordTemporalNoApareceEnLogs` (cosmetico, no funcional).

## CONCLUSIONES Y PROXIMOS PASOS

1. **Bloqueante para merge:** cerrar #4 antes del merge. Cambio chico: hacer que las dos exceptions guarden el valor en un campo privado pero `getMessage()` devuelva un texto generico ("Datos ya registrados" o "Conflicto con un registro existente"), y/o que los handlers respondan con `"No es posible procesar la solicitud con los datos proporcionados"` y codigo HTTP 409 sin codigo de error tan especifico (o usar el mismo codigo para ambos: `REGISTRO_CONFLICTO`). Logueo interno puede mantener el valor; respuesta HTTP no.
2. **Bloqueante para merge:** cerrar #6 introduciendo un `SolicitudRegistroSummaryDTO` para listados (sin cedula completa: `V-***5678`; sin telefono o telefono enmascarado; sin email completo `j***@dominio.com`) y dejando el DTO actual solo en endpoints de detalle individual con permiso fino.
3. **No bloqueante pero recomendado:** resolver inconsistencias N-1 (precision/scale salario) y N-2 (indices duplicados) en la misma migracion V11 o en una V12 inmediata, antes que cualquier promocion con `ddl-auto: validate`.
4. **Fase 2 (siguiente PR):** captcha en `/api/v1/socios/solicitud` (#3) + validador RIF Venezuela (#5: regex `^[JGVECP]-\d{8}-\d$` + opcionalmente check digit del SENIAT).
5. **Fase 2 (siguiente PR):** email real (no mock) con plantilla HTML y un solo punto donde el password viva (sin re-loggearlo).
6. **Cobertura:** agregar tests de exception handler para los hallazgos #4 y #6 una vez aplicados los fixes, garantizando la regresion.
