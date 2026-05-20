---
tags: [contabilidad, ahorros, hooks, integracion]
sub-issue: "#267"
pr: "#315"
estado: en-construccion
created: 2026-05-20
actualizado: 2026-05-20 (D-002 fix 1.1.01 → 1.1.03)
---

# 🔌 Hooks contables — módulo Ahorros

> [!summary] Sub-issue [[Home|#267]]
> Cada depósito y retiro que se realiza por el módulo de [[../ahorros/Home|Ahorros]]
> genera **automáticamente** su [[02-asientos-contables|asiento contable]] de
> partida doble, dentro de la **misma transacción** que mueve el saldo.

> [!danger] Decisión correctiva D-002 (2026-05-20)
> El mapping inicial usaba `1.1.01` Caja Principal para operaciones Bs.
> **Eso fue corregido** a `1.1.03` Bancos Cta Corriente Bs antes de mergear
> el PR #315, porque **Fatrans no opera con caja física**. Ver
> [[_decisiones-contables#D-002|D-002]] para razón completa y análisis del
> contador-fatrans.

## Mapping operación → asiento (POST-fix D-002)

> [!important] Las 4 variantes
> El adapter [`AhorrosContabilidadAdapter`](../../../backend/src/main/java/com/tufondo/ahorros/infrastructure/contabilidad/AhorrosContabilidadAdapter.java)
> mapea cada operación según la moneda de la cuenta:

### Depósito (origen `AHORRO_DEPOSITO`)

| Moneda | DEBE (cuenta) | HABER (cuenta) | Significado |
|---|---|---|---|
| **VES** | `1.1.03` Bancos Cta Corriente Bs | `2.1.01` Cuentas de Ahorro Bs | Entra transferencia, sube obligación con socio |
| **USD** | `1.1.05` Bancos Cuentas USD | `2.1.02` Cuentas de Ahorro USD | Entra USD, sube obligación con socio |

### Retiro (origen `AHORRO_RETIRO`)

| Moneda | DEBE (cuenta) | HABER (cuenta) | Significado |
|---|---|---|---|
| **VES** | `2.1.01` Cuentas de Ahorro Bs | `1.1.03` Bancos Cta Corriente Bs | Baja obligación con socio, sale transferencia |
| **USD** | `2.1.02` Cuentas de Ahorro USD | `1.1.05` Bancos Cuentas USD | Idem en dólares |

> [!info] ¿Por qué este mapping?
> Cuando un socio deposita Bs en su cuenta del fondo:
> - **Activamente** el fondo recibe una transferencia → Bancos Bs sube (ACTIVO, naturaleza DEUDORA → al **DEBE**)
> - **Simultáneamente** el fondo le debe ese dinero al socio → Captaciones sube (PASIVO, naturaleza ACREEDORA → al **HABER**)
>
> Si el socio retira, es el espejo exacto: la deuda con el socio baja y los bancos bajan.

### Por qué bancos y no caja

> Cita del [[_contador-fatrans|contador-fatrans]] (revisión 2026-05-20):
>
> **VEN-NIF NIC-1 (presentación razonable)** exige que las partidas reflejen
> la sustancia económica, no la forma. Si la operación es una transferencia
> interbancaria (que es lo que Fatrans hace), asentarla en Caja distorsiona
> el rubro **Disponible** y rompe la conciliación bancaria.
>
> Consecuencias si se hubiera mergeado con `1.1.01`:
> 1. La conciliación bancaria del fondo no cuadraría: Banesco mostraría el ingreso, pero el libro mayor lo tendría en Caja → diferencia permanente.
> 2. Auditoría externa lo observaría en la primera revisión.
> 3. Reportes a SUDECA (#271) mostrarían Caja inflada artificialmente y Bancos subevaluado.
>
> Ver decisión completa en [[_decisiones-contables#D-002|D-002]].

### Caso futuro: si Fatrans abre caja física

Si en el futuro Fatrans abre oficina con caja de cobranza (pagos en billete),
se necesitaría:
- Agregar `canalOrigen` distinto en `DepositoRequest` (EFECTIVO vs TRANSFERENCIA).
- Mapeo condicional en el adapter.
- Sub-issue dedicado.

**Por ahora, NO anticipamos** — el adapter usa `1.1.03` fijo.

## Arquitectura

```
RealizarDepositoUseCase  (módulo Ahorros)
        │
        │ contabilidadPort.registrarDeposito(cuenta, movimiento)
        ▼
AhorrosContabilidadPort   ← interfaz (port de salida)
        │
        ▼
AhorrosContabilidadAdapter ← implementación, conoce los códigos VEN-NIF
        │
        │ asientoContableService.registrar(cmd)
        ▼
AsientoContableService    (módulo Contabilidad)
        │
        ▼
asientos_contables + partidas_asientos    (BD)
```

> [!tip] Por qué un port en lugar de inyectar `AsientoContableService` directo
> El port `AhorrosContabilidadPort` desacopla el módulo Ahorros del API interno
> de Contabilidad. Si mañana cambiamos el modelo (ej. usamos eventos en lugar
> de llamada síncrona), Ahorros no se entera — solo el adapter cambia.

## Atomicidad — el punto crítico

> [!danger] La contabilidad NO es best-effort
> A diferencia de notificaciones email/SMS (que son resilientes — si fallan,
> la operación sigue), un **asiento no generado** significa que la BD contable
> está **desincronizada** con la BD operativa. Eso viola partida doble y nos
> deja en infracción regulatoria SUDECA.

Implementación:

```java
@Transactional
public MovimientoResponse ejecutar(...) {
    // 1. Validaciones de negocio
    // 2. Mover saldo
    // 3. Persistir movimiento
    // 4. Notificar LOCDOFT (resiliente)
    // 5. ⭐ Hook contable — si falla, ROLLBACK TOTAL
    contabilidadPort.registrarDeposito(cuenta, movimiento);
    return mapper.toResponse(movimiento);
}
```

Si el adapter lanza `AsientoContableException`:
- El `@Transactional` propaga la excepción.
- Spring hace rollback de la transacción.
- Se revierten: cambio de saldo, persistencia del movimiento, asiento parcial.
- La operación devuelve HTTP 422 al cliente.

## Casos borde cubiertos

> [!note] Moneda null (cuentas legacy)
> Las cuentas creadas antes del enum `Moneda` no tienen valor. El adapter
> hace **fallback a Bs** (usa `1.1.03` / `2.1.01`) para no romper. Si esto
> es común en PROD, vale la pena agregar un constraint `NOT NULL` en
> `cuentas_ahorro.moneda` (ver [[_pendientes-criticos#Constraint-NOT-NULL-en-cuentas_ahorromoneda|P4 cosmético]]).

## Tests

| Test class | Casos | Estrategia |
|---|---|---|
| `AhorrosContabilidadAdapterTest` | 10 (2 nested) | Mockito — verifica que el `RegistrarAsientoCommand` es construido correctamente para cada combinación (Bs/USD × depósito/retiro) |
| `RealizarDepositoUseCaseTest` | 6 | Mockito — verifica que el hook se invoque, y que NO se invoque si una validación previa falla (IDOR, cuenta cerrada, etc) |
| `RealizarRetiroUseCaseTest` | 7 | Idem para retiro + saldo insuficiente |
| `AhorrosContabilidadAdapterIntegrationTest` | 5 | `@DataJpaTest` con BD H2 real — ejecuta el adapter end-to-end y verifica que el asiento aparece en `asientos_contables` con las partidas correctas |

### Verificaciones específicas

> [!check] Garantías que dan los tests (post-fix D-002)
> - ✅ Depósito Bs usa exactamente `1.1.03` / `2.1.01`
> - ✅ Depósito USD usa exactamente `1.1.05` / `2.1.02`
> - ✅ Retiro invierte (DEBE el pasivo, HABER el activo)
> - ✅ Si la cuenta contable referenciada no existe → `AsientoContableException`
> - ✅ Si una validación de Ahorros falla (IDOR, saldo, cuenta cerrada), el hook NO se invoca
> - ✅ El asiento queda balanceado (`Σdebe = Σhaber`) — invariante delegada al dominio
> - ✅ Decimales (NUMERIC(18,4)) se preservan sin truncado
> - ✅ `referenciaExterna` del asiento = `numeroOperacion` del movimiento (trazabilidad cruzada)

## Cambios al codebase

```
backend/src/main/java/com/tufondo/ahorros/
├── application/
│   ├── port/output/
│   │   └── AhorrosContabilidadPort.java          [NUEVO]
│   └── usecase/
│       ├── RealizarDepositoUseCase.java           [+inject port +call hook]
│       └── RealizarRetiroUseCase.java             [+inject port +call hook]
└── infrastructure/contabilidad/
    └── AhorrosContabilidadAdapter.java            [NUEVO — mapea códigos]

backend/src/test/java/com/tufondo/ahorros/
├── application/usecase/
│   ├── RealizarDepositoUseCaseTest.java           [NUEVO — 6 tests]
│   └── RealizarRetiroUseCaseTest.java             [NUEVO — 7 tests]
└── infrastructure/contabilidad/
    ├── AhorrosContabilidadAdapterTest.java        [NUEVO — 10 tests unit]
    └── AhorrosContabilidadAdapterIntegrationTest  [NUEVO — 5 tests E2E]
```

> [!warning] Cero cambios en el módulo Contabilidad
> Este sub-issue NO toca `com.tufondo.contabilidad.*`. El service y dominio
> ya estaban listos desde el [[02-asientos-contables|PR #314]]. Solo
> agregamos un consumidor de su API.

## Pendiente intencionalmente fuera de scope

- ⏳ **Aplicar rendimiento** — actualmente `CalcularRendimientoUseCase` solo
  calcula y persiste con estado `CALCULADO`. NO toca saldo ni genera asiento.
  Cuando se decida cómo "aplicar" (workflow de aprobación, batch nocturno),
  se agregará un hook con `OrigenAsiento.AHORRO_INTERES`.
- ⏳ **Devengo mensual de intereses pasivos** (a pagar al socio sobre sus ahorros)
  — ver [[_pendientes-criticos#Devengo-mensual-de-intereses-pasivos-a-pagar-cuentas-de-ahorro|P1]].

## Sub-issues relacionados

- 🚧 [[04-hooks-creditos|#268]] — Hooks de Créditos (desembolso/cobro/intereses).
  Mismo patrón exactamente, distintos códigos (`1.3.01`, `4.1.01`, etc).

## Referencias

- Issue: [[Home|#267]]
- PR: [#315](https://github.com/gamijoam/fatrans/pull/315)
- Decisión clave: [[_decisiones-contables#D-002|D-002 — usar 1.1.03 no 1.1.01]]
- Revisión contable: [[_contador-fatrans]]
- Dependencias: [[01-plan-cuentas|#264]], [[02-asientos-contables|#265 + #266]]
- Use cases existentes (no creados aquí): `RealizarDepositoUseCase`, `RealizarRetiroUseCase`
