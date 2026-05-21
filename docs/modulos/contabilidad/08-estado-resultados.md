---
tags: [contabilidad, reporte, estado-resultados, sudeca, ven-nif]
sub-issue: "#271"
pr: en-curso
estado: implementado
created: 2026-05-20
---

# 💰 Estado de Resultados — excedente del período

> [!summary] Sub-issue [[Home|#271]] (parte 2/2) — IMPLEMENTADO
> Reporte VEN-NIF del **excedente** (o déficit) del período: cuánto entró
> de ingresos vs cuánto se gastó. El resultado es el "Excedente del
> Ejercicio" que también aparece en el [[07-balance-general|Balance General]].

## Endpoint

```http
GET /api/v1/contabilidad/estado-resultados
GET /api/v1/contabilidad/estado-resultados/pdf
```

**Permisos**: `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`, `ROLE_SISTEMA`.

### Query parameters

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `desde` | `LocalDate` | **obligatorio** | Inicio del período (inclusive) |
| `hasta` | `LocalDate` | **obligatorio** | Fin del período (inclusive) |
| `incluirCeros` | `boolean` | `false` | Muestra todas las cuentas aunque sean cero |

### Validaciones

- `hasta >= desde`
- `(hasta - desde) ≤ 366 días`

## Estructura visual

```
═══════════════════════════════════════════════════════════════════
       ESTADO DE RESULTADOS
       Del 01/05/2026 al 31/05/2026
       Asociación Fatrans — RIF J-XXX
═══════════════════════════════════════════════════════════════════
INGRESOS
  4.1 Ingresos por Cartera                              3,800.00
    4.1.01 Intereses sobre Créditos       3,500.00
    4.1.02 Comisiones                       200.00
    4.1.03 Mora                             100.00
  4.3 Otros Ingresos                                        0.00
TOTAL INGRESOS                                          3,800.00

EGRESOS
  5.1 Egresos por Captaciones                             400.00
    5.1.01 Intereses sobre Ahorros          400.00
  5.2 Egresos Operativos                                1,000.00
    5.2.01 Sueldos                          800.00
    5.2.04 Servicios Básicos                200.00
TOTAL EGRESOS                                           1,400.00

═══════════════════════════════════════════════════════════════════
       EXCEDENTE DEL EJERCICIO                          2,400.00
       (fondo verde si excedente, rojo si déficit)
═══════════════════════════════════════════════════════════════════
```

## Cálculo del saldo del período

Para cada cuenta hoja de tipo INGRESO o EGRESO:

```
saldo_periodo = saldo_acumulado(hasta) - saldo_acumulado(desde - 1)
```

Esta fórmula **diferencial** garantiza que solo se cuente lo que ocurrió
EN el período, sin importar saldos previos (ej. ingresos de meses
anteriores). El cálculo se hace con `calcularSaldoCuentaHasta()` del
repositorio (#270), que ya excluye ANULADOS.

## Decisiones (D-008 compartido con Balance General)

### Roll-up jerárquico
Igual que el Balance: hojas → grupos → rubros. El total de la sección es
la suma de los rubros.

### Solo cuentas de tipo INGRESO (4) y EGRESO (5)
El use case filtra explícitamente. Cuentas de tipos 1/2/3 no aparecen
aunque tengan movimientos en el período (esas son del Balance General).

### Excedente firmado
- `INGRESOS − EGRESOS > 0` → "EXCEDENTE"
- `INGRESOS − EGRESOS < 0` → "DÉFICIT"
- `INGRESOS − EGRESOS = 0` → "—"

El campo `excedente` del DTO siempre es valor absoluto; la `excedenteEtiqueta`
indica el signo conceptual.

### Asientos ANULADOS excluidos
Mismo criterio que Mayor y Balance.

### Poda de ceros
Default: cuentas con saldo cero en el período NO se muestran. Toggle
`incluirCeros=true`.

## Tests (12 nuevos, todos verdes)

| Test | Casos |
|---|---|
| `EstadoResultadosFilterTest` | 5 (rango ≤ 1 año, hasta >= desde, factories) |
| `GenerarEstadoResultadosUseCaseTest` | 7 (vacío, excedente positivo/déficit, exclusión otros tipos, roll-up, incluirCeros, cálculo diferencial) |

### Garantías verificadas

- ✅ Período vacío → ingresos=0, egresos=0, excedente=0, etiqueta `—`
- ✅ Ingresos > Egresos → etiqueta `EXCEDENTE` y valor correcto
- ✅ Egresos > Ingresos → etiqueta `DÉFICIT` y valor correcto
- ✅ Cuentas de Activo (1.x) NO aparecen aunque tengan saldo grande
- ✅ Roll-up: rubro = grupo = hoja en jerarquía 4 → 4.1 → 4.1.01
- ✅ `incluirCeros=true` muestra árbol completo aunque cero
- ✅ Cálculo diferencial: `saldo_hasta(hasta) − saldo_hasta(desde-1)`

## Relación con el Balance General

El Balance General invoca este use case internamente para calcular el
"Excedente del Ejercicio" que aparece en Patrimonio. Cuando se invoca por
el Balance, se usa rango = `inicioEjercicio → fechaCorte` (típicamente
1-enero del año hasta la fecha de corte).

```
Balance General  →  invoca  →  Estado de Resultados (año fiscal)
                                          ↓
                              Excedente → Patrimonio del Balance
```

## Pendientes

- ⏳ **Asiento de cierre** (#272): cuando esté, el excedente se persiste en
  `3.3.02` y este reporte sigue funcionando normal; el Balance ya no
  necesita invocarlo on-the-fly.
- ⏳ **Estados comparativos** (período actual vs anterior).
- ⏳ **Notas a los Estados Financieros** (NIC-1).

## Referencias

- Issue: [[Home|#271]]
- Decisión: [[_decisiones-contables#D-008|D-008]]
- Dependencias: [[01-plan-cuentas|#264]], [[02-asientos-contables|#265+#266]], [[06-libro-mayor|#270]]
- Complementa: [[07-balance-general|Balance General]]
