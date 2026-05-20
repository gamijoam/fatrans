---
tags: [contabilidad, plan-cuentas, ven-nif, migration]
sub-issue: "#264"
pr: "#313"
estado: merged
created: 2026-05-19
---

# 📋 Plan de Cuentas VEN-NIF

> [!summary] Sub-issue [[Home|#264]] — bloque fundacional
> Tabla `plan_cuentas` con seed de ~55 cuentas según VEN-NIF / SUDECA.
> Es el catálogo del que se cuelgan todos los [[02-asientos-contables|asientos]].

## Estructura jerárquica del código

Códigos por niveles (formato `^[1-6](\.\d{1,3}){0,4}$`):

| Nivel | Ejemplo | Concepto |
|---|---|---|
| 1 | `1` | Rubro (ACTIVO, PASIVO, ...) |
| 2 | `1.1` | Grupo (Activo Disponible, Cartera de Créditos, ...) |
| 3 | `1.1.01` | **Cuenta operativa** ← acepta movimientos |
| 4–5 | `1.1.01.001` | Sub-cuentas analíticas (opcional, sin seed inicial) |

> [!tip] Regla del primer dígito
> El primer dígito **siempre** mapea al tipo:
> - `1` → `ACTIVO`
> - `2` → `PASIVO`
> - `3` → `PATRIMONIO`
> - `4` → `INGRESO`
> - `5` → `EGRESO`
> - `6` → `CUENTA_ORDEN`
>
> Esto se valida en el dominio Java (`CuentaContable`) **y** en BD (CHECK).

## Naturaleza del saldo (DEUDORA / ACREEDORA)

Cada cuenta tiene una `naturaleza_saldo` que determina cómo se calcula su saldo:

```
saldo = naturaleza × (Σ debe − Σ haber)
```

| Tipo | Naturaleza natural | Razón |
|---|---|---|
| ACTIVO | DEUDORA (+) | Aumenta al DEBE |
| PASIVO | ACREEDORA (−) | Aumenta al HABER |
| PATRIMONIO | ACREEDORA (−) | Idem pasivo |
| INGRESO | ACREEDORA (−) | Idem |
| EGRESO | DEUDORA (+) | Idem activo |

> [!warning] Cuentas correctoras
> Algunas cuentas son `ACTIVO` con naturaleza **ACREEDORA** (cuenta correctora):
> - `1.3.99` Provisión Cartera de Créditos (CR)
> - `1.5.99` Depreciación Acumulada (CR)
>
> Esto NO es un bug — se modela así para que el balance general las muestre restando del rubro.

## Cuentas operativas relevantes (referencia rápida)

### Activos (1)

| Código | Nombre | Uso |
|---|---|---|
| `1.1.01` | Caja Principal | Efectivo en bóveda |
| `1.1.02` | Caja Chica | Gastos menores |
| `1.1.03` | Bancos Cuenta Corriente Bs | Saldos cta corriente Bs |
| `1.1.04` | Bancos Cuenta de Ahorro Bs | Saldos cta ahorro Bs |
| `1.1.05` | Bancos Cuentas USD | Saldos en dólares |
| `1.3.01` | Créditos Personales por Cobrar | Cartera vigente |
| `1.3.02` | Créditos Hipotecarios por Cobrar | Hipotecas |
| `1.3.03` | Intereses por Cobrar sobre Créditos | Intereses devengados |
| `1.3.99` | Provisión Cartera (CR) | Provisión por incobrables |

### Pasivos (2) — Captaciones de socios

| Código | Nombre | Uso |
|---|---|---|
| `2.1.01` | Cuentas de Ahorro Bs | ← **Saldos de socios en Bs** |
| `2.1.02` | Cuentas de Ahorro USD | ← **Saldos de socios en USD** |
| `2.1.03` | Depósitos a Plazo Fijo Bs | Plazos fijos |
| `2.1.04` | Intereses por Pagar Captaciones | Intereses devengados |

### Patrimonio (3)

| Código | Nombre |
|---|---|
| `3.1.01` | Aportes Sociales |
| `3.2.01` | Reserva Legal |
| `3.3.02` | Excedente del Ejercicio |

### Ingresos (4)

| Código | Nombre |
|---|---|
| `4.1.01` | Intereses sobre Créditos |
| `4.1.02` | Comisiones por Otorgamiento |
| `4.1.03` | Intereses Moratorios |

### Egresos (5)

| Código | Nombre |
|---|---|
| `5.1.01` | Intereses sobre Cuentas de Ahorro |
| `5.1.02` | Intereses sobre Plazo Fijo |
| `5.2.01` | Sueldos y Salarios |
| `5.2.07` | Depreciación |

### Cuentas de orden (6)

| Código | Nombre |
|---|---|
| `6.1.01` | Garantías Recibidas |
| `6.2.01` | Garantías Otorgadas |

> 📎 Lista completa: ver migration [`V21__plan_cuentas_ven_nif.sql`](../../../backend/src/main/resources/db/migration/V21__plan_cuentas_ven_nif.sql).

## Defensa en profundidad (validaciones)

```
┌─────────────────────────────────────┐
│ Dominio Java (CuentaContable)       │  ← Regex código, nivel, prefijo
├─────────────────────────────────────┤
│ Repository adapter                  │  ← Map domain ↔ entity
├─────────────────────────────────────┤
│ JPA @Column constraints              │
├─────────────────────────────────────┤
│ PostgreSQL CHECK constraints         │  ← Regex codigo, nivel 1-5
│ - chk_plan_cuentas_codigo_formato    │
│ - chk_plan_cuentas_padre_segun_nivel │
└─────────────────────────────────────┘
```

> [!danger] Cambios al plan
> **NUNCA editar V21 in-place.** Cualquier cambio del plan en PROD requiere
> migration nueva V22+ que `INSERT`/`UPDATE` cuentas. El seed V21 está
> congelado para mantener consistencia entre ambientes.

## Tests

- `SeedV21PlanCuentasTest` — parsea V21 como texto y valida invariantes sin BD
- `CuentaContableTest` — dominio: regex, prefijo, niveles
- `CuentaContableRepositoryImplTest` — round-trip H2
- `TipoCuentaContableTest`, `NaturalezaSaldoTest` — enums

Última corrida: **todos verdes** (incluido en los 120 tests del módulo).

## Referencias

- PR merged: [#313](https://github.com/gamijoam/fatrans/pull/313)
- Migration: `backend/src/main/resources/db/migration/V21__plan_cuentas_ven_nif.sql`
- Issue padre: [[Home|EPIC #263]]
