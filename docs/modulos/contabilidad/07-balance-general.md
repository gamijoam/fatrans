---
tags: [contabilidad, reporte, balance-general, sudeca, ven-nif]
sub-issue: "#271"
pr: en-curso
estado: implementado
created: 2026-05-20
---

# 🏛️ Balance General — situación patrimonial a una fecha

> [!summary] Sub-issue [[Home|#271]] (parte 1/2) — IMPLEMENTADO
> Foto del fondo a una fecha de corte: qué tiene (Activo) vs qué debe
> (Pasivo) y qué le pertenece (Patrimonio). **Reporte regulatorio
> obligatorio VEN-NIF** para auditoría externa y SUDECA.

## Endpoint

```http
GET /api/v1/contabilidad/balance-general
GET /api/v1/contabilidad/balance-general/pdf
```

**Permisos**: `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`, `ROLE_SISTEMA`.

### Query parameters

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `fechaCorte` | `LocalDate` | **obligatorio** | Fecha a la que se evalúa (inclusive) |
| `inicioEjercicio` | `LocalDate` | `1-enero del año de fechaCorte` | Inicio del ejercicio fiscal para calcular Excedente |
| `incluirCeros` | `boolean` | `false` | Si `true`, muestra todas las cuentas aunque tengan saldo cero |

## Estructura visual

```
═══════════════════════════════════════════════════════════════════
       BALANCE GENERAL AL 31/05/2026
       Asociación Fatrans — RIF J-XXX
═══════════════════════════════════════════════════════════════════
ACTIVO                                  PASIVO
  1.1 Disponible          6,500.00       2.1 Depósitos       4,500.00
    1.1.03 Bancos Bs        6,500.00       2.1.01 Ahorros Bs   4,500.00
  1.3 Cartera              9,500.00     Subtotal PASIVO      4,500.00
    1.3.01 Créditos        10,000.00     PATRIMONIO
    (−) 1.3.99 Provisión     (500.00)     3.1 Aportes         10,000.00
                                            3.1.01 Sociales    10,000.00
                                          EXCEDENTE Ejercicio   2,400.00
                                        TOTAL P+P+E           16,900.00
TOTAL ACTIVO              16,000.00     ......................

[Si los totales cuadran: ✓ BALANCEADO]
[Si no:                   ⚠ DESBALANCEADO — diferencia: X]
```

## Fórmula contable

```
Total Activo  =  Total Pasivo  +  Total Patrimonio  +  Excedente del Ejercicio
```

> [!important] El Excedente del Ejercicio
> A diferencia de las cuentas del balance que se leen directamente de los
> saldos del plan, el **Excedente del Ejercicio** se calcula on-the-fly:
> el use case del Balance invoca internamente al
> [[08-estado-resultados|Estado de Resultados]] del rango
> `inicioEjercicio → fechaCorte` y extrae el resultado.
>
> Esto es porque la cuenta `3.3.02 Excedente del Ejercicio` solo se persiste
> con el saldo real cuando corre el **asiento de cierre** (#272). Mientras
> #272 no esté implementado, el Balance lo calcula on-the-fly. Cuando
> #272 mergee, este cálculo se reemplaza por lectura directa de `3.3.02`.

## Decisiones de diseño (D-008)

### D-008.1 — Roll-up por jerarquía
Cada cuenta hoja aporta su saldo al grupo padre; cada grupo aporta a su
rubro padre. El use case construye el árbol en memoria recorriendo el plan
una sola vez.

### D-008.2 — Cuentas correctoras restan del padre (corrección crítica del bug inicial)
Cuentas como `1.3.99` Provisión Cartera (tipo ACTIVO con naturaleza
ACREEDORA) tienen saldo en HABER. Para integrarlas correctamente al rubro
padre, el use case usa la **naturaleza del TIPO** (`ACTIVO → DEUDORA`) al
firmar el saldo, no la naturaleza de la cuenta individual. Resultado:
`(0 debe - 500 haber)` con fórmula DEUDORA = **−500** (negativo). Suma así
al padre correctamente restando.

🐛 **Bug detectado durante implementación**: la versión inicial usaba
`c.getNaturaleza()` en lugar de `c.getTipo().naturalezaNatural()`. Tests
detectaron `10000 + 500 = 10500` cuando se esperaba `9500`. Fix documentado
explícitamente en el código y en este doc para evitar regresión.

### D-008.3 — Excluir asientos ANULADOS
Mismo criterio que Libro Mayor (D-007): el Balance refleja saldos
vigentes, no historial. Exclusión por el repo (`calcularSaldoCuentaHasta`
ya filtra REGISTRADOS).

### D-008.4 — Excedente integrado automáticamente
Si el ER da EXCEDENTE, suma al patrimonio. Si da DÉFICIT, resta. Si es
cero, no afecta. Etiqueta visual `EXCEDENTE / DÉFICIT / —` clara.

### D-008.5 — Ejercicio fiscal calendario por default
Si `inicioEjercicio` no se provee, default = **1-enero del año de
fechaCorte**. Para ejercicios no calendarios (ej. julio-junio) el contador
debe pasarlo explícito.

### D-008.6 — Poda de ceros por default
Cuentas con saldo cero NO se muestran salvo `incluirCeros=true`. Reporte
limpio para el contador típico; vista completa disponible si auditoría lo
exige.

### D-008.7 — Validación de cuadre defensiva
Si `Σ Activo ≠ Σ Pasivo + Σ Patrimonio + Excedente`, el sistema tiene un
bug. El reporte muestra `⚠ DESBALANCEADO` con la diferencia exacta y
loguea ERROR. Esto NO debería ocurrir si cada asiento individual cuadra
(invariante del dominio), pero defensa en profundidad.

## Tests (16 nuevos, todos verdes)

| Test | Casos |
|---|---|
| `BalanceGeneralFilterTest` | 6 (validaciones fechaCorte, inicioEjercicio, incluirCeros) |
| `GenerarBalanceGeneralUseCaseTest` | 10 (Mockito: vacío, simple cuadrado, correctora, excedente integrado, déficit, poda, encabezado, desbalance) |

### Garantías verificadas

- ✅ Balance vacío → totales cero, `balanceado=true`
- ✅ Balance simple cuadrado (Activo = Pasivo + Patrimonio)
- ✅ Cuenta correctora `1.3.99` resta del rubro padre correctamente (fix bug)
- ✅ Cuenta correctora marcada `esCorrectora=true` en el response
- ✅ Excedente del ER se suma al patrimonio para cuadrar
- ✅ Déficit se resta del patrimonio
- ✅ `incluirCeros=false` poda cuentas vacías
- ✅ `incluirCeros=true` muestra árbol completo
- ✅ Encabezado completo con `inicioEjercicio` resuelto
- ✅ Desbalance se reporta con `diferencia` y `balanceado=false`

## Pendientes

- ⏳ **Asiento de cierre del Excedente** (parte de #272) → permitirá leer
  saldo persistido de `3.3.02` en lugar de calcular on-the-fly.
- ⏳ **Estados comparativos** (período actual vs anterior) — exigencia
  VEN-NIF para reportes anuales. Sub-issue futuro.
- ⏳ **Notas a los Estados Financieros** (NIC-1) — sub-issue dedicado.
- ⏳ **Estado de Cambios en el Patrimonio** — tercer reporte VEN-NIF. Sub-issue dedicado.

## Referencias

- Issue: [[Home|#271]]
- Decisión: [[_decisiones-contables#D-008|D-008]]
- Dependencias: [[01-plan-cuentas|#264]], [[02-asientos-contables|#265+#266]], [[06-libro-mayor|#270]] (reutiliza `calcularSaldoCuentaHasta`)
- Complementa: [[08-estado-resultados|Estado de Resultados]]
