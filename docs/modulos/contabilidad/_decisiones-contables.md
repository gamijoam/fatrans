---
tags: [contabilidad, decisiones, adr, registro]
created: 2026-05-20
estado: documento-vivo
---

# 📚 Registro de decisiones contables (D-xxx)

> [!info] Propósito
> Cada decisión contable tomada durante el EPIC #263 queda registrada acá con:
> - **Contexto** (qué situación la disparó)
> - **Opciones consideradas**
> - **Decisión tomada y razón**
> - **Consecuencias** (qué cambia, qué queda abierto)
> - **Revisor** (quién aprobó — [[_contador-fatrans]] cuando aplica)
>
> Estilo ADR (Architecture Decision Record) pero más informal y enfocado
> en contabilidad. **No editar decisiones pasadas** — si cambian, agregar
> nueva D-xxx que las supersede.

---

## D-001 — Plan de cuentas seedeado en migration V21, no editable in-place

**Fecha:** 2026-05-19
**Sub-issue:** [[01-plan-cuentas|#264]]
**Estado:** ✅ Aprobada e implementada

### Contexto
Necesitábamos un plan de cuentas inicial sin esperar a un contador colegiado.

### Decisión
Seedeamos ~55 cuentas en `V21__plan_cuentas_ven_nif.sql` basadas en plantillas
públicas SUDECA. **NUNCA editar V21 in-place** — cualquier cambio futuro va
en V22+ con `INSERT`/`UPDATE`.

### Razón
- Mantener consistencia entre ambientes (dev/QA/PROD).
- Permitir auditoría del histórico de cambios al plan.
- Si V21 cambia in-place, ambientes con la versión vieja quedarían huérfanos.

### Consecuencias
- 🔒 V21 está congelado.
- ⚠️ Antes de uso productivo formal, contador colegiado debe firmar el plan.
- ✅ Cambios al plan son auditables (cada migration documenta qué y por qué).

---

## D-002 — Usar `1.1.03` Bancos Cuenta Corriente Bs (NO `1.1.01` Caja Principal) para operaciones de ahorro en Bs

**Fecha:** 2026-05-20
**Sub-issue:** [[03-hooks-ahorros|#267]] (PR #315)
**Estado:** ✅ Aprobada (en proceso de implementación — corrigiendo PR #315)
**Revisor:** [[_contador-fatrans|contador-fatrans]]

### Contexto
Implementación inicial de #267 usaba `1.1.01` Caja Principal como cuenta DEBE
en depósitos Bs (y HABER en retiros Bs). Tests pasaban (28 verdes), pero
durante la review se aclaró que **Fatrans no opera con caja física relevante**.

> Cita del usuario (gamijoam, 2026-05-20):
> "Ahorita estamos en un proceso manual, o sea, si una persona quiere un
> crédito, por lo general le hace una transferencia a la cuenta del socio,
> pero a otra cuenta, no a la del fondo de ahorro. Porque no estamos
> enlazados como tal a un instrumento financiero, o sea, el socio puede ver
> su saldo, pero no es como un banco que tienes tu saldo ahí y lo retiras
> directamente para pagar."

### Opciones consideradas
1. **Dejar `1.1.01` Caja Principal** — mantiene mapping actual.
2. **Cambiar a `1.1.03` Bancos Cuenta Corriente Bs** — refleja la realidad operativa.
3. **Hacer configurable por operación** (canal: efectivo vs transferencia).

### Decisión
**Opción 2** — cambiar a `1.1.03` fijo. La opción 3 introduce complejidad
que no se justifica hasta que aparezca el flujo de pago en efectivo (que hoy
no existe).

### Razón regulatoria (cita del contador-fatrans)
> **VEN-NIF NIC-1 (presentación razonable)**: las partidas deben reflejar la
> sustancia económica, no la forma. Si la operación es una transferencia
> interbancaria, asentarla en Caja distorsiona el rubro **Disponible** y
> rompe la conciliación bancaria.
>
> Consecuencias prácticas si no se corrige:
> 1. La conciliación bancaria del fondo no cuadrará: Banesco mostrará el
>    ingreso, pero el libro mayor lo tiene en Caja → diferencia permanente.
> 2. Auditoría externa lo va a observar en la primera revisión — es de los
>    hallazgos más frecuentes en cooperativas.
> 3. Reportes a SUDECA (cuando #271 esté) mostrarán Caja inflada
>    artificialmente y Bancos subevaluado.

### Mapping correcto post-D-002

| Operación | Moneda | DEBE | HABER |
|---|---|---|---|
| Depósito | VES | **`1.1.03` Bancos Cta Corriente Bs** | `2.1.01` Cuentas de Ahorro Bs |
| Depósito | USD | `1.1.05` Bancos USD | `2.1.02` Cuentas de Ahorro USD |
| Retiro | VES | `2.1.01` Cuentas de Ahorro Bs | **`1.1.03` Bancos Cta Corriente Bs** |
| Retiro | USD | `2.1.02` Cuentas de Ahorro USD | `1.1.05` Bancos USD |

(`1.1.05` USD se mantiene sin cambios — esa cuenta sí es "bancos", no caja).

### Consecuencias
- Cambio en `AhorrosContabilidadAdapter.java` línea ~54: renombrar
  `CUENTA_CAJA_BS` → `CUENTA_BANCO_BS`, cambiar valor `"1.1.01"` → `"1.1.03"`.
- Actualizar 4 tests del adapter (`AhorrosContabilidadAdapterTest`).
- Actualizar 5 tests E2E (`AhorrosContabilidadAdapterIntegrationTest`).
- Documentación: actualizar [[03-hooks-ahorros]] con el mapping nuevo.
- Push al mismo PR #315 (no mergeado todavía).

### Excepción documentada
Si en el futuro Fatrans abre oficina física con caja de cobranza (pagos en
billete), entonces SÍ se necesitaría usar `1.1.01` para esos casos
específicos. Eso requeriría:
- Un parámetro en `DepositoRequest` para distinguir el canal (transferencia vs efectivo).
- Un mapeo condicional en el adapter.
- Un sub-issue dedicado.

**Por ahora, no anticipamos**.

---

## D-003 — Desembolso de crédito sale del banco del fondo, NO de la cuenta de ahorro del socio

**Fecha:** 2026-05-20
**Sub-issue:** [[04-hooks-creditos|#268]] (en diseño)
**Estado:** ✅ Aprobada (a implementar)
**Revisor:** [[_contador-fatrans|contador-fatrans]]

### Contexto
Para el sub-issue #268, había que decidir cómo modelar contablemente el
desembolso de un crédito. Hay dos opciones operativas posibles:

1. **Salir por banco del fondo (transferencia externa)** — el fondo transfiere
   desde su cuenta corriente Bs al banco externo del socio.
2. **Acreditar a la cuenta de ahorro del socio (en el fondo)** — el "saldo
   virtual" del socio en la app crece, pero no sale plata real.

### Opciones consideradas

**Asiento si Opción 1 (transferencia externa):**
```
DEBE  1.3.01 Créditos por Cobrar           [monto bruto]
HABER 1.1.03 Bancos Cta Corriente Bs       [monto neto]
HABER 4.1.02 Comisiones por Otorgamiento   [comisión, si > 0]
```

**Asiento si Opción 2 (acreditar a ahorros):**
```
DEBE  1.3.01 Créditos por Cobrar           [monto bruto]
HABER 2.1.01 Cuentas de Ahorro Bs          [monto neto]
HABER 4.1.02 Comisiones por Otorgamiento   [comisión, si > 0]
```

### Decisión
**Opción 1** — el desembolso siempre sale del banco del fondo.

### Razón
- Refleja la realidad operativa de Fatrans (transferencia bancaria al banco
  externo del socio).
- Mantiene separados contablemente los módulos Ahorros (captaciones) y
  Créditos (cartera).
- Evita que un desembolso "infle" artificialmente el saldo de ahorros del socio.

### Consecuencias
- El campo `cuentaDestino` que ya existe en `SolicitudCredito` se documenta
  como **cuenta bancaria externa del socio** (no como cuenta de ahorro del
  fondo). Es el dato que el admin usa para hacer la transferencia.
- El `referenciaDesembolso` se usa como auditoría de la transferencia bancaria
  real.

---

## D-004 — Pago de cuota se desglosa en 3+ cuentas (capital + interés + mora si aplica)

**Fecha:** 2026-05-20
**Sub-issue:** [[04-hooks-creditos|#268]] (en diseño)
**Estado:** ✅ Aprobada (a implementar)
**Revisor:** [[_contador-fatrans|contador-fatrans]]

### Contexto
Al cobrar una cuota, el monto total se compone de capital, intereses
normales, mora, y (potencialmente) seguros y comisiones intra-cuota.
Pregunta: ¿cuál es el nivel de desglose contable correcto?

### Opciones consideradas

**A. Mínimo (Capital + Interés + Mora):**
```
DEBE  1.1.03 Bancos
HABER 1.3.01 Cartera (capital)
HABER 4.1.01 Intereses normales
HABER 4.1.03 Mora (solo si > 0)
```

**B. Simplificado (Capital + todo lo demás junto):**
```
DEBE  1.1.03 Bancos
HABER 1.3.01 Cartera (capital)
HABER 4.1.01 Intereses (= intereses + mora + seguros + comisiones)
```

**C. Completo (Capital + Interés + Mora + Seguros + Comisiones):**
```
DEBE  1.1.03 Bancos
HABER 1.3.01 Cartera
HABER 4.1.01 Intereses normales
HABER 4.1.03 Mora
HABER 4.1.04 Seguros (cuenta a crear)
HABER 4.1.05 Comisiones Otras (cuenta a crear)
```

### Decisión
**Opción A** — desglose mínimo (Capital + Interés + Mora).

### Razón regulatoria
> Es el **desglose mínimo legalmente exigido** por SUDECA:
> - Capital → amortiza pasivo del socio (no es ingreso)
> - Intereses → ingreso operativo de la entidad
> - Mora → ingreso diferenciado (clasificación distinta, base de cálculo distinta)
>
> La opción B (mezclar mora con intereses) dificulta reportes de morosidad
> y crea riesgo de hallazgo en auditoría.
>
> La opción C requiere migration nueva con `4.1.04` y `4.1.05` para campos
> (`seguros`, `comisiones` en `Amortizacion`) que actualmente están en 0 en
> todos los flujos. Agregar cuentas para campos vacíos es ruido.

### Consecuencias
- En el adapter de #268, el método `registrarPagoCuota` arma 3 o 4 partidas
  según si hay mora.
- Cuando el negocio empiece a usar `seguros`/`comisiones` intra-cuota, se
  abre sub-issue dedicado con migration V23 que agrega las cuentas.
- Se documenta explícitamente que estamos en **criterio de caja modificado**
  para intereses (ver [[_pendientes-criticos#Devengo-mensual|D-005 pendiente]]).

---

## D-006 — Libro Diario incluye anulados por defecto + formato visual anual del correlativo

**Fecha:** 2026-05-20
**Sub-issue:** [[05-libro-diario|#269]]
**Estado:** ✅ Aprobada e implementada
**Revisor:** [[_contador-fatrans|contador-fatrans]] (rol asumido en sesión, archivo activo)

### Contexto
Al diseñar el Libro Diario surgieron dos preguntas:
1. ¿Incluir asientos ANULADOS o solo REGISTRADOS?
2. ¿El correlativo debe resetearse anualmente como exige SUDECA?

### Decisión

**1. Incluir ANULADOS por defecto, con marca visual.**

El parámetro `incluirAnulados` default es `true`. Los anulados aparecen con
fondo rojo claro, texto rojo, tag `[ANULADO]` y motivo visible.

**Razón regulatoria**: SUDECA exige auditoría completa. La inmutabilidad legal
aplica a TODO lo registrado, no solo a lo activo. Censurar anulados del Libro
Diario sería hallazgo de auditoría.

**2. Formato anual visual del correlativo (sin cambiar BD por ahora).**

El campo `numeroFormateado` muestra el número como `AÑO-NNNNNN` (ej. `2026-000001`),
construido como `{año de fechaContable}-{numero a 6 dígitos}`. La SEQUENCE
BD `seq_asiento_numero` sigue corriendo continua.

**Razón temporal**: cambiar la SEQUENCE para reset anual requiere migration
+ posiblemente cambio del modelo `AsientoContable` (agregar campo `año_fiscal`).
Es trabajo no trivial. Para esta iteración, formateamos visualmente como
quick win. Bloqueante real: antes del primer cierre fiscal serio.

### Consecuencias

- Endpoint `GET /libro-diario?incluirAnulados=true|false` permite a admin
  toggear si necesita vista limpia.
- PDF marca visualmente los anulados.
- **Pendiente P1 abierto**: "Reset anual del correlativo de asientos" en
  [[_pendientes-criticos]]. Sub-issue dedicado antes del primer cierre.

---

## D-007 — Libro Mayor: saldo inicial real, solo hojas, ANULADOS excluidos

**Fecha:** 2026-05-20
**Sub-issue:** [[06-libro-mayor|#270]]
**Estado:** ✅ Aprobada e implementada
**Revisor:** [[_contador-fatrans|contador-fatrans]] (rol asumido en sesión)

### Contexto
Al diseñar el Libro Mayor (siguiente reporte después del Diario), surgieron
varias decisiones clave que cambian el comportamiento respecto al Diario.

### Decisiones

**1. Saldo inicial REAL (no asumir cero).**

Se calcula con `SUM` agregado de todas las partidas previas a `desde-1`,
una sola query nativa SQL por cuenta. Excluye ANULADOS.

*Razón*: el Libro Mayor sin saldo inicial real es un dato falso. Si pides
el Mayor de Mayo, necesitas ver el saldo arrastrado desde antes. Exigencia
VEN-NIF.

**2. Solo cuentas HOJA (operativas) por default.**

Las totalizadoras (nivel 1-2) se calculan sumando hijas — eso pertenece al
Balance General (#271), no al Mayor. Parámetro `incluirTotalizadoras=true`
opcional.

**3. Cuentas SIN movimientos excluidas por default.**

Reporte limpio. Parámetro `incluirSinMovimientos=true` para vista completa.

**4. Asientos ANULADOS EXCLUIDOS del Mayor.**

A diferencia del Diario (que los incluye con marca para auditoría completa),
el Mayor refleja saldos vigentes — un asiento anulado NO afecta saldos.
Esto se enforza a nivel query: el repo solo cuenta los `REGISTRADO`.

*Razón*: separación clara Diario (historial) vs Mayor (saldo). El contador
que quiere ver el historial completo va al Diario.

**5. Saldos en formato absoluto + tag (D/A/—).**

Convención SUDECA: `3,800.00 (D)` en lugar de signos. La cuenta sabe por su
naturaleza si el saldo es "esperado" (D para deudoras, A para acreedoras).
Si el saldo queda del lado opuesto (caso atípico pero válido), el tag se
invierte.

**6. Contracuenta resuelta y visible por movimiento.**

Para cada partida de la cuenta del mayor, se busca la cuenta del lado
opuesto en el mismo asiento. Si hay > 1 (ej. desembolso con comisión),
se toma la de mayor monto + tag `(múltiple)`. Mejora UX masivamente.

### Consecuencias

- 2 queries nuevas en `AsientoContableRepository` port:
  `calcularSaldoCuentaHasta()` y `listarAsientosDeCuentaEnRango()`.
- Record nuevo `SaldoCuenta` en `domain.model`.
- DTOs nuevos `LibroMayorFilter` y `LibroMayorResponse`.
- Performance: `2N+1` queries para N cuentas. Aceptable para ~50 cuentas
  hoja típicas. Optimización futura: cachear saldos cerrados (parte de #272).
- Adapter PDF unificado en `LibroDiarioPdfAdapter` (renombre a futuro).

---

## D-008 — Balance General y Estado de Resultados: roll-up, correctoras, Excedente integrado

**Fecha:** 2026-05-21
**Sub-issue:** [[07-balance-general|#271]] (parte 1) y [[08-estado-resultados|#271]] (parte 2)
**Estado:** ✅ Aprobada e implementada
**Revisor:** [[_contador-fatrans|contador-fatrans]] (rol asumido en sesión)

### Contexto
Los dos reportes finales del bloque inicial del EPIC. Balance General es
foto a una fecha (Activo = Pasivo + Patrimonio); Estado de Resultados es
ingresos vs egresos del período. Ambos requieren roll-up jerárquico del
plan de cuentas y manejo cuidadoso de cuentas correctoras.

### Decisiones tomadas

**D-008.1 — Roll-up por jerarquía**
Cada cuenta hoja aporta su saldo al grupo padre; cada grupo al rubro. Use
case construye árbol en memoria recorriendo el plan + un saldo por hoja
vía `calcularSaldoCuentaHasta`. Costo: ~2N queries (N = cuentas hoja).
Aceptable; optimización futura con saldos cacheados en #272.

**D-008.2 — Cuentas correctoras restan del padre (¡bug crítico inicial!)**
Implementación inicial usaba `c.getNaturaleza()` para firmar el saldo.
Para `1.3.99` Provisión Cartera (ACTIVO + naturaleza ACREEDORA): saldo
HABER 500 → firmado por ACREEDORA = +500 → sumaba al padre ACTIVO en
lugar de restar.

**Fix**: usar `c.getTipo().naturalezaNatural()` al firmar el saldo. Para
1.3.99 (tipo ACTIVO): firmado por DEUDORA = `debe - haber = 0 - 500 = -500`.
Negativo → resta correctamente del padre.

Test que detectó el bug: `correctora_resta` esperaba 9500 y obtuvo 10500.
Sin este test el bug habría llegado a producción y los Balances reales
estarían incorrectos por 2× la provisión.

**D-008.3 — Asientos ANULADOS excluidos**
Consistente con D-007 (Mayor): saldos vigentes, no historial. Exclusión a
nivel repository por `calcularSaldoCuentaHasta`.

**D-008.4 — Excedente del Ejercicio integrado on-the-fly**
El Balance invoca al use case del Estado de Resultados para calcular
excedente y agregarlo virtualmente al patrimonio. Cuando #272 (cierre)
persista el excedente en `3.3.02`, este cálculo se reemplaza por lectura
directa.

**D-008.5 — Ejercicio fiscal calendario por default**
`inicioEjercicio` default = 1-enero del año de `fechaCorte`. Si Fatrans
operara con ejercicio fiscal no calendario, contador pasa explícito.

**D-008.6 — Poda de cuentas en cero por default**
`incluirCeros=false`: reporte limpio. `=true`: vista completa para auditoría.

**D-008.7 — Validación de cuadre defensiva**
Si `Σ Activo ≠ Σ Pasivo + Σ Patrimonio + Excedente`, log ERROR + marca
visual `⚠ DESBALANCEADO`. NO debería ocurrir si cada asiento individual
cuadra (invariante dominio).

### Consecuencias

- 2 use cases nuevos en application: `GenerarBalanceGeneralUseCase` y
  `GenerarEstadoResultadosUseCase`.
- 4 DTOs (filter + response × 2 reportes).
- Extensión del port `ContabilidadPdfPort` con 2 métodos.
- Adapter PDF unificado en `LibroDiarioPdfAdapter` (ya hace 4 reportes).
- 2 controllers nuevos.
- 33 tests nuevos cubriendo casos críticos.

### Pendientes que abre

- **#272 Cierre del Excedente**: persistir el excedente en `3.3.02` para
  que el Balance lea saldo real en lugar de cálculo on-the-fly.
- **Estados comparativos** (período actual vs anterior).
- **Notas a los Estados Financieros** (VEN-NIF NIC-1).
- **Estado de Cambios en el Patrimonio** (3er reporte VEN-NIF).

---

## D-005 — (PENDIENTE) Criterio de caja vs devengo para intereses de cartera

**Fecha:** 2026-05-20
**Sub-issue:** sin asignar — abrir antes del primer cierre fiscal
**Estado:** ⏳ Diferida, documentada

### Contexto
El contador-fatrans observó que VEN-NIF (criterio de devengo) exige reconocer
ingresos cuando se genera el derecho (devengo mensual), no cuando se cobra.
Actualmente #268 va a registrar intereses directamente a `4.1.01` al momento
de cobrar la cuota.

### Decisión temporal
Implementar #268 en **criterio de caja modificado** (intereses al cobro,
no al devengo). Aceptable para cooperativa pequeña sin cierre fiscal aún.

### Implementación correcta a futuro
Job mensual (Spring `@Scheduled`) que asienta:
```
DEBE  1.3.03 Intereses por Cobrar sobre Créditos   [intereses devengados del mes]
HABER 4.1.01 Intereses sobre Créditos              [ingreso reconocido]
```

Y al cobrar la cuota:
```
DEBE  1.1.03 Bancos
HABER 1.3.01 Cartera (capital)
HABER 1.3.03 Intereses por Cobrar (los devengados — NO 4.1.01)
HABER 4.1.03 Mora (si aplica)
```

### Acción
Abrir issue separado "Implementar devengo mensual de intereses sobre cartera
(NIC-1)" cuando se acerque el primer cierre fiscal serio.

---

## Plantilla para decisiones nuevas

```markdown
## D-xxx — [Título corto, decisorio]

**Fecha:** YYYY-MM-DD
**Sub-issue:** [[link]]
**Estado:** ⏳ En discusión / ✅ Aprobada / ❌ Rechazada / 🔄 Superseded by D-yyy
**Revisor:** [[_contador-fatrans]] / contador externo / etc

### Contexto
[¿Qué problema o situación motivó esta decisión?]

### Opciones consideradas
1. ...
2. ...

### Decisión
[Cuál se elige]

### Razón
[Por qué — preferentemente con cita regulatoria si aplica]

### Consecuencias
- [Qué cambia en código / docs / proceso]
- [Qué queda abierto]
```
