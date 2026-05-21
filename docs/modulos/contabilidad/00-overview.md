---
tags: [contabilidad, overview, epic-263, timeline]
created: 2026-05-20
estado: documento-vivo
---

# 📜 EPIC #263 — Contabilidad: visión general y timeline

> [!summary] Para qué existe este módulo
> Construir un sistema contable completo (partida doble) que cumpla
> **VEN-NIF / SUDECA**, de modo que cada operación financiera del fondo
> (depósitos, retiros, créditos, intereses, cierres) genere automáticamente
> sus asientos contables sin intervención manual del contador. Sin esto,
> Fatrans NO puede operar legalmente como asociación de ahorro y crédito.

## Por qué fue necesario

Antes del EPIC, Fatrans operaba **sin libro contable digital**: los
movimientos de saldo del socio (depósito, retiro, intereses) se registraban
en el módulo de Ahorros, pero NO se reflejaban en ninguna estructura
contable de partida doble. Eso significa:

- ❌ No se podía emitir Balance General ni Estado de Resultados.
- ❌ No había trazabilidad de ingresos por intereses vs amortización de cartera.
- ❌ Cero defensa frente a auditoría SUDECA — el primer requerimiento sería: "muéstrenme el libro diario".
- ❌ Imposible calcular reservas legales, provisiones, depreciaciones automáticamente.

El EPIC #263 cierra esa brecha desde la base (plan de cuentas) hasta los
reportes finales.

## Estado actual (2026-05-21, post #271)

```
Plan de cuentas (#264) ──► Asientos (#265+#266) ──► Hooks Ahorros (#267) ──► Hooks Créditos (#268) ──► Libro Diario (#269) ──► Libro Mayor (#270) ──► Balance + ER (#271) ──► Cierre (#272+#273)
       ✅                       ✅                        ✅                        ✅                        ✅                       ✅                     🚧 En PR                ⏳
```

## Timeline cronológico de decisiones y entregas

### 2026-05-19 — Bloque fundacional

**[[01-plan-cuentas|#264 — Plan de cuentas VEN-NIF]]** (PR #313, merged)

Trabajado y mergeado el plan de cuentas con ~55 cuentas seedeadas en
migration V21. Tres niveles jerárquicos: rubros (1-6), grupos (1.1, 1.2, ...),
cuentas operativas (1.1.01, ...). Defensa en profundidad: validaciones en
dominio Java + JPA + PostgreSQL CHECK constraints.

**Hallazgo durante la implementación**: el test `version_se_incrementa` falló
porque el modelo `CuentaContable` es **inmutable** — cada `guardar()` recibe
un objeto nuevo, no dirty-check. Fix: testear `@Version` directamente a nivel
entity con `saveAndFlush()`.

### 2026-05-20 — Asientos contables

**[[02-asientos-contables|#265 + #266 — Asientos]]** (PR #314, merged)

Migration V22 con tablas `asientos_contables` (cabecera) y `partidas_asientos`
(detalle). Modelo de dominio inmutable con aggregate root `AsientoContable`
que valida los 4 mandamientos de partida doble. Service de aplicación
`AsientoContableService.registrar()` + `anular()`. Repositorio JPA con
batch loading para evitar N+1.

**Decisiones técnicas tomadas**:
- SEQUENCE PostgreSQL `seq_asiento_numero` con `CACHE 1` para correlativo continuo SUDECA.
- FK `ON DELETE RESTRICT` en ambas tablas para impedir borrado físico.
- `BigDecimal` con escala 4 (NUMERIC(18,4)) — exacto, sin truncado.
- Asientos NUNCA se DELETE, solo ANULAN con motivo.
- `OrigenAsiento` enum con 10 valores para clasificar la fuente.

**Hallazgos durante implementación**:
- Test `round_trip` falló por llamar `entity.toDomain(List.of())` antes de
  persistir las partidas — viola la invariante "mínimo 2 partidas". Fix:
  trabajar con la entity directamente para obtener el ID y luego reconstruir.
- Test `solo_haber` falló porque el mensaje esperado no era el que el dominio
  lanza primero (validación de balance precede a la de partidas). Fix:
  relajar la assertion al tipo de excepción.

### 2026-05-20 — Hooks de Ahorros (en curso)

**[[03-hooks-ahorros|#267 — Hooks Ahorros]]** (PR #315, abierto)

Conectado `RealizarDepositoUseCase` y `RealizarRetiroUseCase` con el módulo
Contabilidad vía un port + adapter pattern:

```
RealizarDepositoUseCase → AhorrosContabilidadPort (interfaz)
                              ↓
                       AhorrosContabilidadAdapter (impl, conoce códigos VEN-NIF)
                              ↓
                       AsientoContableService.registrar()
```

**28 tests nuevos, todos verdes**. Suite completa del backend pasa con
**555 tests** (incluye los 120 del módulo contabilidad).

**🚨 Hallazgo crítico post-implementación**: el usuario aclaró que Fatrans
**NO opera con caja física** — todo se mueve por transferencia bancaria.
Por tanto, el mapping `1.1.01` Caja Principal (que codeé inicialmente para
operaciones Bs) es **incorrecto**: debe ser `1.1.03` Bancos Cuenta Corriente Bs.

Este hallazgo desencadenó:
1. La creación del [[_contador-fatrans|sub-agente especializado en contaduría]]
   para que evalúe proactivamente cada decisión contable.
2. La revisión formal del contador (ver [[_decisiones-contables#D-002|D-002]]):
   veredicto **corregir antes de mergear**.
3. La identificación de varios pendientes regulatorios que documenté en
   [[_pendientes-criticos]].

### 2026-05-20 — #268 Hooks de Créditos (PR #316, MERGED)

**[[04-hooks-creditos|#268 — Hooks Créditos]]** (merged)

Mismo patrón que #267 aplicado al módulo Créditos, con mapping previamente
aprobado por [[_contador-fatrans]] ([[_decisiones-contables#D-003|D-003]] y
[[_decisiones-contables#D-004|D-004]]):
- **Desembolso**: DEBE `1.3.01` Cartera / HABER `1.1.03` Bancos / HABER `4.1.02` Comisión (si > 0)
- **Pago cuota**: DEBE `1.1.03` Bancos / HABER `1.3.01` capital / HABER `4.1.01` intereses / HABER `4.1.03` mora (si > 0)

**Decisiones de implementación adicionales**:
- Adapter valida localmente el cuadre antes de invocar contabilidad → mensajes de error claros indicando si es problema de cálculo del use case vs. plan de cuentas.
- En `RegistrarPagoCuotaUseCase`: refactor mínimo para cargar `SolicitudCredito` siempre (antes solo se cargaba si era el último pago).
- Hook contable se invoca **antes** de la notificación al socio (verificado con `InOrder`), para que un rollback contable evite notificar un desembolso que no ocurrió.

**Tests**: 32 nuevos, todos verde.

**Pendientes deliberadamente fuera de scope** (documentados en `04-hooks-creditos.md`):
- Crédito en USD (modelo no tiene campo `moneda`)
- Ejecución de colateral (use case existe pero sin hook)
- Pago parcial (flujo actual exige completo)

### 2026-05-20 — #269 Libro Diario (PR #317 — primer reporte SUDECA)

**[[05-libro-diario|#269 — Libro Diario]]** (PR en revisión)

Primer reporte contable exigido por SUDECA: listado secuencial de todos los
asientos del período. Endpoint `GET /api/v1/contabilidad/libro-diario` (JSON)
y `/pdf` (descarga). Solo ADMIN/SUPER_ADMIN/SISTEMA por ahora.

**Decisiones tomadas** ([[_decisiones-contables#D-006|D-006]]):
- Incluir asientos ANULADOS por defecto con marca visual (SUDECA exige auditoría completa, no censura).
- Correlativo formateado como `AÑO-NNNNNN` visualmente. Reset anual real
  queda como pendiente P1 antes del primer cierre fiscal.
- Puerto PDF dedicado al módulo contabilidad (no reutilizar `documentospdf`).
- Razón social y RIF vía properties configurables en `application.yml`.

**21 tests nuevos, todos verde**:
- `LibroDiarioFilterTest` (7) — validaciones de rango
- `GenerarLibroDiarioUseCaseTest` (9) — Mockito
- `LibroDiarioPdfAdapterTest` (5) — verificación de bytes PDF

**Pendientes abiertos**:
- Reset anual real del correlativo (P1, antes de cierre fiscal)
- Crear rol `CONTADOR` dedicado (P2)
- Firma digital del PDF (sub-issue futuro)

### 2026-05-20 — #270 Libro Mayor (PR — saldos por cuenta)

**[[06-libro-mayor|#270 — Libro Mayor]]** (PR en revisión)

Segundo reporte SUDECA. Agrupa movimientos por cuenta (no por asiento como
el Diario), con saldo inicial real al comienzo del período, contracuenta
resuelta por movimiento, saldo acumulado por línea, y saldo final.

**Decisiones tomadas** ([[_decisiones-contables#D-007|D-007]]):
- Saldo inicial REAL: `SUM` agregado de partidas previas a `desde-1`.
- Solo cuentas hoja por default (totalizadoras para Balance General).
- Cuentas sin movimientos excluidas por default (`incluirSinMovimientos=true` opt).
- Asientos ANULADOS **excluidos** del Mayor (diferencia clave con Diario).
- Saldos formato absoluto + tag `D/A/—` (convención SUDECA).
- Contracuenta visible por movimiento (cuenta principal del lado opuesto).
- Adapter PDF unificado en `LibroDiarioPdfAdapter` (renombre a futuro).

**2 queries nuevas al port**:
- `calcularSaldoCuentaHasta(cuentaId, fechaCorte)` — native SQL `SUM` agregado.
- `listarAsientosDeCuentaEnRango(cuentaId, desde, hasta)` — JOIN + hidratación batch.

**31 tests nuevos, todos verde**:
- `LibroMayorFilterTest` (8) — validaciones rango y flags
- `GenerarLibroMayorUseCaseTest` (11) — Mockito, saldo inicial, contracuenta, etiquetas
- `LibroMayorPdfAdapterTest` (5) — verificación bytes PDF, casos borde
- `AsientoContableRepositoryLibroMayorTest` (7) — `@DataJpaTest` H2: SUM, filtros, exclusión ANULADOS

**Pendientes nuevos**:
- Cachear saldos cerrados al cierre mensual (parte de #272) para performance.

### 2026-05-21 — #271 Balance General + Estado de Resultados (PR — reportes VEN-NIF finales)

**[[07-balance-general|#271 Balance]]** y **[[08-estado-resultados|#271 ER]]** (PR en revisión)

Los **dos reportes regulatorios obligatorios VEN-NIF** del bloque inicial.

**Decisiones D-008** ([[_decisiones-contables#D-008|completas acá]]):
- Roll-up jerárquico Rubro → Grupo → Cuenta hoja.
- 🐛 **Bug detectado y fix crítico**: cuentas correctoras (`1.3.99` Provisión Cartera, `1.5.99` Depreciación) ahora restan correctamente del rubro padre. El bug usaba `c.getNaturaleza()` cuando debe ser `c.getTipo().naturalezaNatural()`. Test `correctora_resta` lo detectó (esperaba 9500, obtuvo 10500).
- Excedente del Ejercicio integrado on-the-fly desde el ER (hasta #272 implemente persistencia en `3.3.02`).
- Asientos ANULADOS excluidos (consistente con #270).
- Saldos en formato absoluto + etiqueta D/A/—.
- Validación de cuadre defensiva con marca visual.

**Endpoints**:
- `GET /api/v1/contabilidad/balance-general` (+ `/pdf`) — fecha de corte
- `GET /api/v1/contabilidad/estado-resultados` (+ `/pdf`) — rango

**33 tests nuevos, todos verde** (205 totales en el módulo contabilidad):
- `BalanceGeneralFilterTest` (6) + `EstadoResultadosFilterTest` (5) — validaciones rango
- `GenerarBalanceGeneralUseCaseTest` (10) — Mockito: cuadrado, correctora, excedente, déficit, poda
- `GenerarEstadoResultadosUseCaseTest` (7) — Mockito: excedente/déficit, exclusión otros tipos, cálculo diferencial
- `BalanceYEstadoResultadosPdfAdapterTest` (5) — generación bytes válidos

**Pendientes nuevos**:
- Asiento de cierre del Excedente (parte de #272 — para persistir en `3.3.02`).
- Estados comparativos (período actual vs anterior).
- Notas a los Estados Financieros (NIC-1).
- Estado de Cambios en el Patrimonio (3er reporte VEN-NIF).

## Lo que viene después de #271

| # | Tema | Comentario |
|---|---|---|
| #272 | Cierre mensual/anual | Bloqueo de período + asientos de cierre (persiste Excedente en `3.3.02`) |
| #273 | Reversión de asientos | Generación automática de asiento inverso para anulación |

## Red flags identificados durante el EPIC (no resueltos aún)

Ver lista completa en [[_pendientes-criticos]]. Resumen:

- ⚠️ **Devengo mensual de intereses** (criterio VEN-NIF NIC-1) — actualmente
  estamos en criterio de caja, no de devengo. Bloqueante antes del primer cierre fiscal real.
- ⚠️ **Provisión de cartera** (`1.3.99`) — cuenta existe en el plan pero ningún
  proceso la mueve. Exigencia SUDECA para cooperativas.
- ⚠️ **Cartera USD vs Bs** — actualmente todos los créditos se asumen Bs.
  Si Fatrans abre créditos USD, hay que separar contablemente.
- ⚠️ **Diferencia en cambio** (NIC-21) — al re-expresar saldos USD a Bs por
  cambio de tasa BCV, se generan asientos de re-expresión. No diseñado aún.
- ⚠️ **Naturaleza jurídica Fatrans** (#274) — sigue sin confirmarse si opera como
  caja de ahorro (SUDECA) o cooperativa (SUNACOOP). Cambia el plan de cuentas.

## Cómo continuar el trabajo

1. **Antes de cada cambio contable nuevo**: invocar [[_contador-fatrans|contador-fatrans]]
   para revisar el mapping propuesto.
2. **Antes de cada PR del EPIC**: actualizar este overview con la entrada nueva
   en el timeline.
3. **Cada decisión contable**: dejar entrada en [[_decisiones-contables]].
4. **Cada red flag nuevo**: agregar a [[_pendientes-criticos]].

> [!important] Validación contable formal
> Antes de uso productivo, un contador colegiado venezolano debe firmar
> conformidad sobre el plan V21 y las reglas de mapeo de #267/#268. La
> implementación técnica es solo la mitad — la otra mitad es la auditabilidad
> regulatoria. Ver [[_pendientes-criticos#Validación-externa|pendiente]].
