---
tags: [contabilidad, pendientes, red-flags, backlog]
created: 2026-05-20
estado: documento-vivo
---

# 🚨 Pendientes críticos del módulo contabilidad

> [!warning] Para qué sirve este documento
> Lista priorizada de **red flags contables-regulatorios** identificados
> durante el EPIC #263 que NO están resueltos. Cada uno se debe convertir
> en sub-issue antes del primer cierre fiscal real (o antes según
> criticidad).
>
> Identificados mayormente por [[_contador-fatrans]] en sus revisiones.

---

## 🔴 Bloqueantes para PROD productivo contable

### Validación externa del plan V21
**Prioridad:** P0 (bloqueante absoluto pre-PROD)

El plan de cuentas seedeado en V21 está basado en plantillas públicas SUDECA,
pero **no está firmado por contador colegiado venezolano**. Antes de uso
productivo contable formal:

- [ ] Contratar contador colegiado para revisión del plan.
- [ ] Documentar firma/dictamen como referencia en `_validacion-contable.md`.
- [ ] Si el contador sugiere cambios, abrir migration V23+ (NUNCA editar V21 in-place — [[_decisiones-contables#D-001|D-001]]).

### Confirmar naturaleza jurídica Fatrans
**Issue:** #274 (existe, sin avance)
**Prioridad:** P0

Sigue sin confirmarse si Fatrans opera bajo:
- **SUDECA** (Caja de Ahorro) — exige ciertos planes y reservas.
- **SUNACOOP** (Cooperativa) — exige reserva de educación, solidaridad, otros.
- **Fideicomiso** — otro régimen distinto.

Esto **afecta el plan de cuentas** (algunas cuentas son específicas del
régimen) y los **reportes a entes reguladores** (#271).

**Acción:** Conseguir documento legal de constitución de Fatrans para
confirmar el régimen.

---

## 🟠 Críticos antes del primer cierre fiscal

### Devengo mensual de intereses sobre cartera
**Decisión origen:** [[_decisiones-contables#D-005|D-005]]
**Prioridad:** P1 (bloqueante antes del primer cierre serio)

Actualmente registramos intereses directo a `4.1.01` al cobro de cuota
(criterio de caja modificado). VEN-NIF NIC-1 exige criterio de devengo:
reconocer el ingreso cuando se genera el derecho, no cuando se cobra.

**Implementación correcta:**
Job mensual (Spring `@Scheduled` el último día de cada mes) que:

```
Para cada crédito con cuotas vigentes:
  DEBE  1.3.03 Intereses por Cobrar sobre Créditos   [interés del mes según plan]
  HABER 4.1.01 Intereses sobre Créditos              [ingreso reconocido]
```

Y al cobrar la cuota, los intereses se cargan a `1.3.03` (que estaba
provisionado), no a `4.1.01` nuevo.

**Tareas:**
- [ ] Diseñar job `DevengarInteresesCarteraJob`.
- [ ] Modificar hook de pago de cuota (#268) para que cuando exista devengo
      previo del período, descargue contra `1.3.03` en lugar de `4.1.01`.
- [ ] Tests del job + tests del fallback (qué pasa si no se ejecutó el job
      del mes anterior).

### Devengo mensual de intereses pasivos a pagar (cuentas de ahorro)
**Prioridad:** P1

Los socios ganan intereses sobre sus saldos de ahorro. Actualmente NO se
está reconociendo este gasto del fondo. Debería:

```
DEBE  5.1.01 Intereses sobre Cuentas de Ahorro     [interés calculado del mes]
HABER 2.1.04 Intereses por Pagar Captaciones       [provisión]
```

Y cuando se "paga" (acredita a la cuenta del socio):
```
DEBE  2.1.04 Intereses por Pagar Captaciones
HABER 2.1.01 Cuentas de Ahorro Bs (o el destino real del pago)
```

**Tareas:**
- [ ] Decidir periodicidad (mensual/trimestral) según política de Fatrans.
- [ ] Decidir cómo "calcular" el interés (tasa fija anual, variable, segmentado por tipo de cuenta).
- [ ] Implementar `DevengarInteresesAhorrosJob`.
- [ ] Cuando #267 madure: hook `OrigenAsiento.AHORRO_INTERES` para el pago.

### Provisión de cartera de créditos
**Prioridad:** P1

La cuenta `1.3.99` Provisión Cartera de Créditos existe en el plan (V21) pero
**ningún proceso la mueve**. Exigencia SUDECA para cooperativas: provisión
calculada periódicamente basada en morosidad.

**Esquema típico (Resolución 011-22 SUDEBAN, también aplica conceptualmente a SUDECA):**

| Estado | Riesgo | Provisión |
|---|---|---|
| Vigente | Normal | 1% |
| Vencido 1-30 días | A | 1% |
| Vencido 31-60 días | B | 5% |
| Vencido 61-90 días | C | 20% |
| Vencido 91-180 días | D | 50% |
| Vencido >180 días | E | 100% |

Asiento de constitución (sube provisión):
```
DEBE  5.2.08 Otros Gastos Operativos (o crear 5.2.09 Gasto por Provisión Cartera)
HABER 1.3.99 Provisión Cartera de Créditos
```

**Tareas:**
- [ ] Crear job `ProvisionarCarteraJob` mensual.
- [ ] Decidir tabla de % por estado (consultar contador para tabla local).
- [ ] Potencialmente migration V23 para crear `5.2.09 Gasto por Provisión Cartera` separada de "Otros gastos".

---

## 🟡 Importantes pero no bloqueantes inmediatos

### Cartera y operaciones USD separadas
**Prioridad:** P2

Actualmente #268 va a asumir todos los créditos en Bs. Si Fatrans abre
créditos USD, hay que decidir:

**Opción A — mezclar todo en `1.3.01`:**
Convertir USD a Bs a tasa BCV en cada asiento. Simpler pero pierde trazabilidad de la cartera en moneda original.

**Opción B — separar `1.3.04` Créditos en USD:**
Migration V23 que agrega:
- `1.3.04` Créditos Personales por Cobrar USD
- (potencialmente) `4.1.04` Intereses sobre Créditos USD

Permite reportes por moneda y tracking de diferencias en cambio.

**Recomendación contador-fatrans:** Opción B cuando se abra el flujo.

**Acción:** Esperar a que el negocio confirme si va a haber créditos USD.

### Diferencia en cambio (NIC-21)
**Prioridad:** P2

Cuando la tasa BCV se mueve, los saldos en USD reexpresados en Bs cambian.
VEN-NIF NIC-21 exige reconocer eso:

Si la tasa sube (USD se aprecia respecto a Bs):
```
DEBE  1.1.05 Bancos Cuentas USD (al re-expresar en Bs queda mayor)
HABER 4.3.02 Otros Ingresos Operativos (o crear 4.4.01 Diferencia en Cambio Positiva)
```

Si la tasa baja:
```
DEBE  5.3.02 Otros Egresos Financieros (o crear 5.3.03 Diferencia en Cambio Negativa)
HABER 1.1.05 Bancos Cuentas USD
```

**Tareas:**
- [ ] Diseñar job `RevisarDiferenciaCambioJob` que corre cuando hay nueva tasa BCV.
- [ ] Crear cuentas `4.4.01` y `5.3.03` en migration V24.
- [ ] Decidir periodicidad (diaria/mensual/al cierre).

### Reservas legales (excedentes del ejercicio)
**Prioridad:** P3

Al cierre anual, el `3.3.02 Excedente del Ejercicio` se cierra contra reservas:
```
DEBE  3.3.02 Excedente del Ejercicio
HABER 3.2.01 Reserva Legal (% que exija la ley)
HABER 3.2.02 Reserva de Educación (cooperativas)
HABER 3.2.03 Reserva de Solidaridad (cooperativas)
HABER 3.3.01 Excedentes Acumulados (el remanente)
```

Los `%` dependen de la naturaleza jurídica (caja ahorro vs cooperativa).

**Tareas:**
- [ ] Resolver #274 primero (régimen).
- [ ] Implementar como parte de #272 (cierre anual).

### Depreciación de bienes de uso
**Prioridad:** P3

Las cuentas `1.5.01` Mobiliario, `1.5.02` Equipo de Computación, `1.5.03`
Vehículos existen pero nadie las usa. Cuando se registre el primer activo
fijo, hay que:

1. Asiento de compra:
```
DEBE  1.5.01 Mobiliario (o el rubro correspondiente)
HABER 1.1.03 Bancos
```

2. Asiento mensual de depreciación:
```
DEBE  5.2.07 Depreciación
HABER 1.5.99 Depreciación Acumulada
```

**Tareas:**
- [ ] Definir tabla de vidas útiles (computación 3 años, mobiliario 10, vehículos 5).
- [ ] Job `DepreciarActivosFijosJob` mensual.
- [ ] Probablemente no urgente hasta que se registre el primer activo.

---

## 🟢 Mejoras de diseño / housekeeping

### Usuario "SISTEMA" para hooks automáticos
**Prioridad:** P4 (cosmético)

Actualmente los hooks pasan `creadoPorUsuarioId = null`. Algunos auditores
prefieren un usuario "SISTEMA" reservado con UUID conocido en `auth_users`
para que el log diga explícitamente "creado por el sistema" en lugar de NULL.

**Tareas:**
- [ ] Crear usuario SISTEMA en migration V23 con UUID hardcoded.
- [ ] Modificar hooks (#267, #268) para pasarlo.

### Constraint NOT NULL en `cuentas_ahorro.moneda`
**Prioridad:** P4

El adapter de #267 hace fallback a Bs si `moneda = null` (cuentas legacy).
Si esto se vuelve común, agregar constraint `NOT NULL` para forzar que toda
cuenta nueva declare moneda.

### Glosa estandarizada de asientos automáticos
**Prioridad:** P4

Los hooks generan glosa en español con formato fijo. Estandarizar como
constantes en un `GlosaTemplates.java` para facilitar i18n futuro y
mantener consistencia entre módulos.

---

## Cómo trabajar este documento

1. Cada vez que `contador-fatrans` (o cualquier otra fuente) identifique un
   nuevo pendiente regulatorio, agregar entrada acá.
2. Marcar prioridad: P0 bloqueante > P1 antes cierre > P2 importante > P3
   nice-to-have > P4 cosmético.
3. Cuando un pendiente se convierte en sub-issue real, agregar el #issue al
   título y marcar progreso.
4. Cuando se resuelve, **NO borrar** — mover a sección "Resueltos" al pie
   para mantener trazabilidad de qué se cerró cuándo.

## Resueltos

(ninguno todavía — todos están abiertos al 2026-05-20)
