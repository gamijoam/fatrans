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

## Mapa del módulo

```
contabilidad/
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
```

## Sub-issues del EPIC

| # | Estado | Tema |
|---|---|---|
| [[01-plan-cuentas\|#264]] | ✅ Merged | Plan de cuentas VEN-NIF (V21) — ~55 cuentas seed |
| [[02-asientos-contables\|#265 + #266]] | ✅ Merged | Tablas + dominio + service + 50+ tests |
| [[03-hooks-ahorros\|#267]] | 🚧 In progress | Hooks de Ahorros: depósito/retiro → asiento auto |
| #268 | ⏳ Pending | Hooks de Créditos (desembolso/cobro/intereses) |
| #269 | ⏳ Pending | Libro Diario + export PDF SUDECA |
| #270 | ⏳ Pending | Libro Mayor (saldos por cuenta) |
| #271 | ⏳ Pending | Balance General + Estado de Resultados |
| #272 | ⏳ Pending | Cierre mensual / anual con bloqueo de período |
| #273 | ⏳ Pending | Reversión de asientos (asiento inverso, sin DELETE) |
| #274 | ⚠️ **BLOCKER** | Confirmar naturaleza jurídica Fatrans (caja ahorro / cooperativa / fideicomiso) |

## Principios de diseño

> [!important] Reglas no negociables
> 1. **Partida doble** — `Σdebe = Σhaber` siempre. Validado en dominio + DB CHECK.
> 2. **Asientos NO se borran** — solo se `ANULAN`. FK `ON DELETE RESTRICT`.
> 3. **Correlativo continuo** — SEQUENCE `seq_asiento_numero` (sin gaps) por exigencia SUDECA.
> 4. **Cuentas hoja solo** — solo cuentas con `acepta_movimientos=TRUE` reciben partidas. Las totalizadoras (nivel 1-2) suman.
> 5. **Atomicidad** — el asiento se persiste en la misma transacción que la operación que lo dispara. Si falla, todo revierte.

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

Backend, vía Docker (no requiere Maven local):

```powershell
docker run --rm `
    -v "${PWD}/backend:/workspace" `
    -v "$env:USERPROFILE/.m2:/root/.m2" `
    -w /workspace `
    maven:3.9-eclipse-temurin-21 `
    mvn -B test "-Dtest=com.tufondo.contabilidad.**.*Test"
```

Última corrida verde: **120 tests, 0 failures, 0 errors** (2026-05-20).
