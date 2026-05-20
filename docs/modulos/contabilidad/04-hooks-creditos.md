---
tags: [contabilidad, creditos, hooks, integracion, planificacion]
sub-issue: "#268"
pr: pending
estado: en-diseño
created: 2026-05-20
---

# 🔌 Hooks contables — módulo Créditos (PLAN, no implementado todavía)

> [!summary] Sub-issue [[Home|#268]] (en diseño)
> Cada desembolso de crédito y cada pago de cuota debe generar
> automáticamente su [[02-asientos-contables|asiento contable]] de partida
> doble, en la misma transacción que mueve los datos operativos.
>
> Mapping **aprobado** por [[_contador-fatrans|contador-fatrans]] con
> observaciones documentadas en [[_decisiones-contables#D-003|D-003]] y
> [[_decisiones-contables#D-004|D-004]].

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

## Cambios al codebase planificados

```
backend/src/main/java/com/tufondo/creditos/
├── application/
│   ├── port/output/
│   │   └── CreditosContabilidadPort.java            [NUEVO]
│   └── usecase/
│       ├── DesembolsaCreditoUseCase.java             [+inject port +call hook]
│       └── RegistrarPagoCuotaUseCase.java            [+inject port +call hook]
└── infrastructure/contabilidad/
    └── CreditosContabilidadAdapter.java              [NUEVO — mapea códigos]

backend/src/test/java/com/tufondo/creditos/
├── application/usecase/
│   ├── DesembolsaCreditoUseCaseTest.java             [NUEVO]
│   └── RegistrarPagoCuotaUseCaseTest.java            [NUEVO]
└── infrastructure/contabilidad/
    ├── CreditosContabilidadAdapterTest.java          [NUEVO — Mockito unit]
    └── CreditosContabilidadAdapterIntegrationTest    [NUEVO — H2 E2E]
```

## Tests planificados

| Test class | Casos esperados | Estrategia |
|---|---|---|
| `CreditosContabilidadAdapterTest` | ~12 unit | Mockito — verifica `RegistrarAsientoCommand` para desembolso con/sin comisión, pago con/sin mora |
| `CreditosContabilidadAdapterIntegrationTest` | ~6 E2E | `@DataJpaTest` con H2 — asiento real persistido con partidas correctas |
| `DesembolsaCreditoUseCaseTest` | ~6 unit | Mockito — verifica que el hook se invoque tras estado DESEMBOLSADO, propagación de errores |
| `RegistrarPagoCuotaUseCaseTest` | ~7 unit | Mockito — verifica hook tras estado PAGADA, idempotencia por `referenciaPago` |

### Casos específicos a cubrir

- ✅ Desembolso sin comisión → 2 partidas (DEBE 1.3.01, HABER 1.1.03).
- ✅ Desembolso con comisión > 0 → 3 partidas (la 3ra HABER 4.1.02).
- ✅ Pago cuota sin mora → 3 partidas (DEBE 1.1.03, HABER 1.3.01, HABER 4.1.01).
- ✅ Pago cuota con mora > 0 → 4 partidas (la 4ta HABER 4.1.03).
- ✅ Cuadre exacto: `Σdebe == Σhaber` con decimales (forzar cuotas decimales raras).
- ✅ Si AsientoContableService lanza, el use case propaga y hace rollback.
- ✅ Si el plan de cuentas no tiene `1.3.01` o `1.1.03` → falla clara.
- ✅ Doble pago de la misma `referenciaPago` se rechaza (idempotencia).
- ✅ Crédito con moneda USD → excepción clara (no implementado, evitar bug silencioso).

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
