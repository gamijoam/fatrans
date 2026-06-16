---
tags: [contabilidad, creditos, hooks, integracion]
sub-issue: "#268"
pr: en-curso
estado: implementado
created: 2026-05-20
actualizado: 2026-05-20 (implementación completa, 32 tests verdes)
---

# 🔌 Hooks contables — módulo Créditos

> [!summary] Sub-issue [[Home|#268]] — IMPLEMENTADO
> Cada desembolso de crédito y cada pago de cuota genera **automáticamente**
> su [[02-asientos-contables|asiento contable]] de partida doble, en la misma
> transacción que mueve los datos operativos.
>
> Mapping aprobado por [[_contador-fatrans|contador-fatrans]] —
> [[_decisiones-contables#D-003|D-003]] (desembolso) y
> [[_decisiones-contables#D-004|D-004]] (pago cuota).

## Mapping operación → asiento

### Desembolso (origen `CREDITO_DESEMBOLSO`)

```
DEBE  1.3.01 Créditos Personales por Cobrar    [monto bruto solicitado]
HABER 1.1.03 Bancos Cta Corriente Bs           [monto neto desembolsado al socio]
HABER 4.1.02 Comisiones por Otorgamiento       [comisión apertura, SI > 0]
```

> [!important] Por qué este mapping
> - **DEBE `1.3.01`**: la cartera sube por el monto bruto (lo que el socio debe).
> - **HABER `1.1.03`**: sale plata del banco del fondo (transferencia bancaria al banco EXTERNO del socio — NO a su cuenta de ahorro del fondo). Ver [[_decisiones-contables#D-003|D-003]].
> - **HABER `4.1.02`**: la comisión de apertura es ingreso inmediato del fondo (criterio VEN-NIF NIC-39 para préstamos a < 5 años).
>
> Cuadre: `bruto = neto + comisión` → balanceado.

### Pago de cuota (origen `CREDITO_COBRO`)

```
DEBE  1.1.03 Bancos Cta Corriente Bs           [monto total cobrado]
HABER 1.3.01 Créditos por Cobrar               [capital amortizado de la cuota]
HABER 4.1.01 Intereses sobre Créditos          [intereses normales de la cuota]
HABER 4.1.03 Intereses Moratorios              [SOLO si interesMora > 0]
```

> [!important] Por qué este desglose
> - **DEBE `1.1.03`**: entra plata al banco del fondo (transferencia desde banco externo del socio).
> - **HABER `1.3.01`**: la parte capital amortiza la cartera (NO es ingreso, solo baja pasivo del socio).
> - **HABER `4.1.01`**: intereses normales son ingreso del fondo.
> - **HABER `4.1.03`**: la mora es ingreso DIFERENCIADO de los intereses normales (clasificación SUDECA exige separación).
>
> Cuadre: `total = capital + interés + mora` → balanceado.

### Casos NO cubiertos en #268

Estos quedan documentados para sub-issues futuros:

- ⏳ **Crédito en USD** — actualmente todos los créditos se asumen Bs.
  Cuando aparezca, ver [[_pendientes-criticos#Cartera-y-operaciones-USD-separadas|P2 cartera USD]].
- ⏳ **Intereses devengados pero no cobrados** — estamos en criterio de caja modificado.
  Ver [[_decisiones-contables#D-005|D-005 pendiente]] y [[_pendientes-criticos#Devengo-mensual-de-intereses-sobre-cartera|devengo cartera]].
- ⏳ **Seguros y comisiones intra-cuota** — los campos existen en `Amortizacion`
  pero están en 0 en todos los flujos. Sub-issue dedicado con migration cuando se usen.
- ⏳ **Ejecución de colateral** — cuando una cuota se ejecuta sobre garantía,
  asiento distinto. Sub-issue dedicado.
- ⏳ **Reverso de pago** — si admin anula un pago registrado por error.
  Probablemente parte de #273 (reversión).
- ⏳ **Mora cobrada parcialmente** — actualmente el use case valida pago completo;
  si se permite pago parcial, asiento se complica.

## Arquitectura propuesta

Mismo patrón exacto que [[03-hooks-ahorros|#267]]:

```
DesembolsaCreditoUseCase     RegistrarPagoCuotaUseCase
            │                            │
            └────────────┬───────────────┘
                         │
                         ▼
            CreditosContabilidadPort    ← interfaz (application/port/output)
                         │
                         ▼
            CreditosContabilidadAdapter ← impl (infrastructure/contabilidad)
                         │                conoce códigos VEN-NIF
                         ▼
            AsientoContableService.registrar()
```

### Métodos del port

```java
public interface CreditosContabilidadPort {
    /**
     * Asiento del desembolso de un crédito.
     *
     * @param solicitud  solicitud aprobada con monto y comisión apertura
     * @param montoNeto  monto efectivamente desembolsado (monto bruto - comisión)
     * @param comisionApertura comisión cobrada (puede ser 0)
     */
    void registrarDesembolso(SolicitudCredito solicitud,
                             BigDecimal montoNeto,
                             BigDecimal comisionApertura);

    /**
     * Asiento del cobro de una cuota.
     *
     * @param solicitud  crédito al que pertenece la cuota
     * @param cuota      la cuota con desglose capital/interés/mora
     * @param referenciaPago referencia bancaria del pago (auditoría cruzada)
     */
    void registrarPagoCuota(SolicitudCredito solicitud,
                            Amortizacion cuota,
                            String referenciaPago);
}
```

## Cambios al codebase (implementado)

```
backend/src/main/java/com/tufondo/creditos/
├── application/
│   ├── port/output/
│   │   └── CreditosContabilidadPort.java            [NUEVO]
│   └── usecase/
│       ├── DesembolsaCreditoUseCase.java             [+inject port, +llamada al hook]
│       └── RegistrarPagoCuotaUseCase.java            [+inject port, +cargar solicitud, +llamada al hook]
└── infrastructure/contabilidad/
    └── CreditosContabilidadAdapter.java              [NUEVO — mapea códigos]

backend/src/test/java/com/tufondo/creditos/
├── application/usecase/
│   ├── DesembolsaCreditoUseCaseHookTest.java         [NUEVO — 7 tests]
│   └── RegistrarPagoCuotaUseCaseHookTest.java        [NUEVO — 6 tests]
└── infrastructure/contabilidad/
    ├── CreditosContabilidadAdapterTest.java          [NUEVO — 13 Mockito unit]
    └── CreditosContabilidadAdapterIntegrationTest    [NUEVO — 6 H2 E2E]
```

## Tests implementados (32 tests verdes)

| Test class | Casos | Estrategia |
|---|---|---|
| `CreditosContabilidadAdapterTest` | **13** (Mockito) | Unit — verifica `RegistrarAsientoCommand` para desembolso con/sin comisión, pago con/sin mora, validaciones locales de cuadre |
| `CreditosContabilidadAdapterIntegrationTest` | **6** (H2 E2E) | `@DataJpaTest` — asiento real persistido con partidas correctas, decimales NUMERIC(18,4) preservados |
| `DesembolsaCreditoUseCaseHookTest` | **7** (Mockito) | Hook se invoca con monto neto + comisión correctos, IDOR/estado cortan antes, error contable propaga sin notificar |
| `RegistrarPagoCuotaUseCaseHookTest` | **6** (Mockito) | Hook recibe (solicitud, cuota, monto, referencia), validaciones tempranas cortan antes, error propaga |

### Casos cubiertos

> [!check] Garantías de los tests
> - ✅ Desembolso sin comisión → 2 partidas (DEBE 1.3.01, HABER 1.1.03)
> - ✅ Desembolso con comisión > 0 → 3 partidas (la 3ra HABER 4.1.02)
> - ✅ Pago cuota sin mora → 3 partidas (DEBE 1.1.03, HABER 1.3.01, HABER 4.1.01)
> - ✅ Pago cuota con mora > 0 → 4 partidas (la 4ta HABER 4.1.03)
> - ✅ Cuadre exacto: `Σdebe == Σhaber` (validado por dominio + verificación local del adapter)
> - ✅ Si `AsientoContableService` lanza, el use case propaga y hace rollback
> - ✅ Si el plan de cuentas no tiene `1.3.01` o `1.1.03` → `AsientoContableException` clara
> - ✅ Doble pago de la misma `referenciaPago` se rechaza con `PagoDuplicadoException` antes del hook
> - ✅ Cuota con seguros/comisiones intra-cuota (campos no usados hoy) → rechazo local con mensaje explicativo
> - ✅ Decimales NUMERIC(18,4) preservados sin truncado
> - ✅ Orden: hook contable ANTES de notificación al socio (rollback evita notificar)
> - ✅ Referencia null/vacía → fallback `CUOTA-{id}` para auditoría
> - ✅ Comisión apertura override en request tiene precedencia sobre TipoCredito.comisionApertura

### Pendiente (no incluido en #268)

- ⏳ **Crédito con moneda USD**: la validación de moneda no existe en el modelo `SolicitudCredito` actualmente (no hay campo `moneda`). Cuando se agregue, hay que decidir si crear `1.3.04` Créditos USD separados o convertir a Bs. Ver [[_pendientes-criticos#Cartera-y-operaciones-USD-separadas|P2]].
- ⏳ **Ejecución de colateral**: cuando una cuota se ejecuta sobre garantía, el asiento es distinto (no es cobro normal). El use case `EjecutarColateralUseCase` existe pero NO tiene hook todavía. Sub-issue dedicado futuro.
- ⏳ **Pago parcial**: el flujo actual exige `monto >= montoRequerido`. Si se permite parcial en el futuro, el asiento de capital + interés debe prorratearse — sub-issue.

## Dependencias

- ✅ [[01-plan-cuentas|#264]] — Plan de cuentas con `1.3.01`, `1.1.03`, `4.1.01`, `4.1.02`, `4.1.03`.
- ✅ [[02-asientos-contables|#265 + #266]] — `AsientoContableService.registrar()`.
- 🚧 [[03-hooks-ahorros|#267]] — debe estar mergeado primero (mismo `AhorrosContabilidadPort` no se reutiliza, pero el patrón sí).

## Riesgo regulatorio (red flags identificados)

Ver [[_pendientes-criticos]] sección "Críticos antes del primer cierre fiscal":

- 🟠 Falta **devengo mensual de intereses** (criterio VEN-NIF) — pasaremos por
  criterio de caja modificado en #268. Sub-issue dedicado antes del primer cierre.
- 🟠 Falta **provisión de cartera** (cuenta `1.3.99` sin uso) — sub-issue dedicado.

## Referencias

- Issue: [[Home|#268]]
- Decisiones: [[_decisiones-contables#D-003|D-003]], [[_decisiones-contables#D-004|D-004]]
- Revisión contable: [[_contador-fatrans]]
- Dependencias: [[01-plan-cuentas]], [[02-asientos-contables]], [[03-hooks-ahorros]]
