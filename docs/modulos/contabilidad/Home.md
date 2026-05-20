---
tags: [modulo, contabilidad, ven-nif, sudeca]
created: 2026-05-20
epic: "#263"
estado: en-construccion
---

# 📒 Módulo Contabilidad

> [!info] EPIC #263 — Contabilidad por partida doble
> Sistema contable completo para cumplimiento regulatorio **SUDECA / VEN-NIF**.
> Cada operación financiera del fondo (depósitos, retiros, créditos, intereses)
> genera automáticamente sus asientos de partida doble. Sin contabilidad
> alineada al estándar, Fatrans no puede operar como caja de ahorro
> habilitada por SUDECA.

## 📚 Documentación del módulo

### Trazabilidad transversal (leer primero)

- 🗺️ [[00-overview]] — **Historia y timeline** del EPIC, decisiones cronológicas, estado actual
- 📚 [[_decisiones-contables]] — **Log de decisiones** D-001, D-002... con razones y revisiones
- 🚨 [[_pendientes-criticos]] — **Red flags regulatorios** identificados (devengo, provisión, USD, etc)
- 🧮 [[_contador-fatrans]] — Sub-agente especializado en contaduría — **invocar antes de cada decisión contable**

### Sub-issues del EPIC

- 📋 [[01-plan-cuentas]] — #264 — Plan de cuentas VEN-NIF (V21, ~55 cuentas)
- 🧾 [[02-asientos-contables]] — #265 + #266 — Tablas + dominio + service partida doble
- 🔌 [[03-hooks-ahorros]] — #267 — Hooks Ahorros: depósito/retiro → asiento auto
- 🔌 [[04-hooks-creditos]] — #268 — Hooks Créditos: desembolso/cobro → asiento auto
- 📖 [[05-libro-diario]] — #269 — Reporte Libro Diario JSON + PDF SUDECA

## Mapa del módulo (código)

```
backend/src/main/java/com/tufondo/contabilidad/
├── domain/
│   ├── model/
│   │   ├── CuentaContable        ← Aggregate del plan de cuentas
│   │   ├── AsientoContable       ← Aggregate root del asiento (cabecera + partidas)
│   │   ├── PartidaAsiento        ← Entity hija (cada renglón DEBE/HABER)
│   │   └── enums/
│   │       ├── TipoCuentaContable   (ACTIVO/PASIVO/PATRIMONIO/INGRESO/EGRESO/CUENTA_ORDEN)
│   │       ├── NaturalezaSaldo      (DEUDORA / ACREEDORA)
│   │       ├── EstadoAsiento        (REGISTRADO / ANULADO)
│   │       └── OrigenAsiento        (10 valores: AHORRO_DEPOSITO, CREDITO_DESEMBOLSO, ...)
│   └── repository/  (ports)
├── application/
│   ├── dto/RegistrarAsientoCommand
│   ├── usecase/AsientoContableService.registrar()/anular()
│   └── exception/AsientoContableException
└── infrastructure/
    └── persistence/
        ├── entity/   (JPA)
        ├── jpa/      (Spring Data)
        └── adapter/  (implementación del port)

backend/src/main/java/com/tufondo/ahorros/
├── application/port/output/AhorrosContabilidadPort.java         ← #267
└── infrastructure/contabilidad/AhorrosContabilidadAdapter.java   ← #267
```

## Estado de los sub-issues

| # | Estado | Tema | Decisiones clave |
|---|---|---|---|
| [[01-plan-cuentas\|#264]] | ✅ Merged (PR #313) | Plan de cuentas VEN-NIF (V21) — ~55 cuentas seed | [[_decisiones-contables#D-001\|D-001]] V21 congelado |
| [[02-asientos-contables\|#265 + #266]] | ✅ Merged (PR #314) | Tablas + dominio + service + 50+ tests | Asientos inmutables, ON DELETE RESTRICT |
| [[03-hooks-ahorros\|#267]] | ✅ Merged (PR #315) | Hooks de Ahorros: depósito/retiro → asiento auto | [[_decisiones-contables#D-002\|D-002]] usar `1.1.03` no `1.1.01` |
| [[04-hooks-creditos\|#268]] | ✅ Merged (PR #316) | Hooks de Créditos: desembolso/cobro → asiento auto | [[_decisiones-contables#D-003\|D-003]] [[_decisiones-contables#D-004\|D-004]] |
| [[05-libro-diario\|#269]] | 🚧 PR #317 abierto | Libro Diario JSON + PDF SUDECA | [[_decisiones-contables#D-006\|D-006]] incluir anulados |
| #270 | ⏳ Pending | Libro Mayor (saldos por cuenta) | |
| #271 | ⏳ Pending | Balance General + Estado de Resultados | |
| #272 | ⏳ Pending | Cierre mensual / anual con bloqueo de período | |
| #273 | ⏳ Pending | Reversión de asientos (asiento inverso, sin DELETE) | |
| #274 | ⚠️ **BLOCKER LEGAL** | Confirmar naturaleza jurídica Fatrans (caja ahorro / cooperativa / fideicomiso) | Ver [[_pendientes-criticos#Confirmar-naturaleza-jurídica-Fatrans\|aquí]] |

## Principios de diseño (no negociables)

> [!important] Reglas de oro
> 1. **Partida doble** — `Σdebe = Σhaber` siempre. Validado en dominio + DB CHECK.
> 2. **Asientos NO se borran** — solo se `ANULAN`. FK `ON DELETE RESTRICT`.
> 3. **Correlativo continuo** — SEQUENCE `seq_asiento_numero` (sin gaps) por exigencia SUDECA.
> 4. **Cuentas hoja solo** — solo cuentas con `acepta_movimientos=TRUE` reciben partidas. Las totalizadoras (nivel 1-2) suman.
> 5. **Atomicidad** — el asiento se persiste en la misma transacción que la operación que lo dispara. Si falla, todo revierte.
> 6. **Contador-fatrans revisa antes de codear** — cada mapping operación → cuentas pasa por [[_contador-fatrans|el sub-agente]] antes de implementarse.

## Realidad operativa Fatrans (contexto crítico)

> [!warning] Fatrans NO opera con caja física relevante
> Todo dinero se mueve por **transferencia bancaria interbancaria**. Las cuentas
> bancarias del fondo (`1.1.03` Bancos Bs, `1.1.05` Bancos USD) son las
> operativas. La cuenta `1.1.01` Caja Principal queda en cero en la práctica.
>
> Esto fue aclarado el 2026-05-20 y motivó la decisión [[_decisiones-contables#D-002|D-002]]
> (corregir mapping #267 antes de mergear).

## Referencias normativas

- **VEN-NIF** — Normas Venezolanas de Información Financiera (basadas en IFRS).
- **SUDECA** — Superintendencia de Cajas de Ahorro.
- **SUNACOOP** — Superintendencia de Cooperativas (si Fatrans califica como cooperativa).
- Plan de cuentas tipo de SUDECA para cajas de ahorro venezolanas.

> [!warning] Validación pendiente
> El seed V21 está basado en plantillas públicas. **Antes de uso productivo
> contable real**, un contador colegiado debe validarlo y firmar conformidad.
> Ver [[01-plan-cuentas]] para detalles del proceso de cambios.

## Cómo correr los tests

Backend, vía Docker (no requiere Maven local — el host puede no tener Java instalado):

```powershell
docker run --rm `
    -v "${PWD}/backend:/workspace" `
    -v "$env:USERPROFILE/.m2:/root/.m2" `
    -w /workspace `
    maven:3.9-eclipse-temurin-21 `
    mvn -B test "-Dtest=com.tufondo.contabilidad.**.*Test"
```

Última corrida verde:
- **Módulo contabilidad solo**: 120 tests, 0 failures (2026-05-20)
- **#267 (Ahorros)**: 28 tests nuevos, todos verde
- **#268 (Créditos)**: 32 tests nuevos, todos verde — 13 adapter unit + 6 E2E + 7 desembolso hook + 6 pago hook
- **Suite completa backend**: 555+ tests, 0 failures (2026-05-20)

## Cómo trabajar el módulo

1. **Antes de cualquier código contable**: invocar [[_contador-fatrans]] para revisar el mapping.
2. **Antes de cada PR del EPIC**: actualizar [[00-overview]] con la entrada en el timeline.
3. **Cada decisión contable**: dejar entrada en [[_decisiones-contables]].
4. **Cada red flag identificado**: agregar a [[_pendientes-criticos]].
5. **Si se agregan cuentas al plan**: migration V22+, NUNCA editar V21.
6. **Si se modifican operaciones existentes**: validar que los asientos siguen cuadrando.
