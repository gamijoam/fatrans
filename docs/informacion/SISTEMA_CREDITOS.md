# Sistema de Créditos - Fondo de Ahorro

## Resumen

El **Sistema de Créditos** permite a los socios del fondo solicitar préstamos, ser evaluados, y si son aprobados, recibir el dinero y pagar cuotas mensuales.

---

## Tipos de Crédito Disponibles

| Tipo | Descripción | Tasa Anual | Requerimiento Colateral |
|------|-------------|------------|-------------------------|
| Crédito Educación | Para estudios | 14.5% | 10% del monto |
| Micro Crédito | Pequeños negocios | 18% | 15% del monto |
| Crédito Vehículo | Compra de vehículos | 12% | 20% del monto |

---

## Flujo Completo de un Crédito

```
1. SOCIO CREA SOLICITUD
   ↓
2. ADMIN EVALÚA (score crediticio)
   ↓
3. ADMIN APRUEBA O RECHAZA
   ↓ (si aprobado)
4. SE DESEMBOLSA EL DINERO
   ↓
5. SOCIO PAGA CUOTAS MENSUALES
   ↓
6. CRÉDITO FINALIZADO
```

---

## Paso 1: Crear Solicitud

El socio solicita un crédito indicando:
- **Monto**: Cuánto dinero necesita
- **Plazo**: En cuántos meses pagarán (ej: 12, 24, 36 meses)
- **Destino**: Para qué lo usa (ej: "compra de vehículo")
- **Cuenta destino**: Dónde recibe el dinero

**Validaciones:**
- No puede tener otro crédito activo
- El monto debe estar dentro de los límites del tipo de crédito
- El plazo debe estar dentro del rango permitido

---

## Paso 2: Evaluación Crediticia

El administrador evalúa al socio con un **Score Crediticio** de 0 a 100 puntos.

### Cálculo del Score

```
Score = Puntaje Antigüedad + Puntaje Historial + Puntaje Capacidad
        (máx 30)           (máx 30)            (máx 40)
        ──────────────────────────────────────────────────────────────
                              Total máximo: 100 puntos
```

### Tabla de Puntajes

| Factor | Criterio | Puntos |
|--------|----------|--------|
| **Antigüedad** | < 6 meses | 0 |
| | 6-12 meses | 10 |
| | 1-2 años | 20 |
| | > 2 años | 30 |
| **Historial Ahorro** | Sin cuenta | 0 |
| | < 6 meses | 10 |
| | 6-12 meses | 20 |
| | > 12 meses | 30 |
| **Capacidad Pago** | Cuota > 30% salario | 0 |
| | Cuota 25-30% salario | 10 |
| | Cuota 15-25% salario | 25 |
| | Cuota ≤ 15% salario | 40 |

### Determinación de Elegibilidad

| Score | Elegible | Nivel de Riesgo | Tasa |
|-------|----------|------------------|------|
| ≥ 80 | ✅ Sí | BAJO | 14.5% × 0.85 = **12.33%** |
| 70-79 | ✅ Sí | BAJO | 14.5% × 0.95 = **13.78%** |
| 60-69 | ✅ Sí | MEDIO | 14.5% (normal) |
| 50-59 | ✅ Sí | ALTO | 14.5% × 1.10 = **15.95%** |
| < 50 | ❌ No | ALTO | -- |

---

## Paso 3: Aprobación o Rechazo

### Si se APRUEBA:

1. **Se retiene el colateral** de la cuenta de ahorro del socio
2. **Se genera el plan de pagos** (sistema francés)
3. **Se desembolsa** el dinero a la cuenta del socio

### Si se RECHAZA:

1. Se registra el **motivo del rechazo**
2. El socio puede volver a solicitar después

### Regla de Colateral

Si el **score es menor a 70**, el socio **debe tener** una cuenta de ahorro con saldo suficiente.

```
Ejemplo:
- Crédito Educativo requiere 10% de colateral
- Monto solicitado: 255,000 Bs
- Colateral requerido: 25,500 Bs

El socio DEBE tener una cuenta de ahorro con al menos 25,500 Bs
```

---

## Paso 4: Desembolso

Una vez aprobado:
1. Se transfiere el monto solicitado a la cuenta del socio
2. El estado cambia a "DESEMBOLSADO"
3. Comienza el conteo de cuotas

---

## Paso 5: Pago de Cuotas

El socio paga cuotas mensuales según el **Plan de Amortización Francés**:

```
Cuota fija mensual = Capital + Interés

Ejemplo: 255,000 Bs a 14.5% anual por 12 meses
- Cada cuota: ~22,500 Bs
- Primera cuota: más interés, menos capital
- Última cuota: menos interés, más capital
```

### Estados de una Cuota

```
PENDIENTE → PAGADA (cuando el socio paga)
    ↓
Si pasa la fecha sin pagar → VENCIDA
    ↓
Si pasan 30+ días → CURSO MORA
    ↓
Si pasan 90+ días sin pagar → SE EJECUTA EL COLATERAL
```

---

## Sistema de Amortización Francesa

El sistema francés calcula **cuotas fijas mensuales** donde:

- Al inicio, la cuota tiene **más interés** y **menos capital**
- Al final, la cuota tiene **menos interés** y **más capital**

```
Mes 1: Interés = 255,000 × 14.5% / 12 = 3,081 Bs
        Capital = 22,500 - 3,081 = 19,419 Bs
        Saldo = 255,000 - 19,419 = 235,581 Bs

Mes 12: Interés = 2,500 × 14.5% / 12 = 30 Bs
        Capital = 22,500 - 30 = 22,470 Bs
        Saldo = 0 Bs (crédito terminado)
```

---

## Ejemplo Práctico Completo

### Solicitud
- **Socio**: Juan Pérez
- **Tipo**: Crédito Vehículo
- **Monto**: 100,000 Bs
- **Plazo**: 24 meses
- **Tasa base**: 12%

### Evaluación
- Antigüedad en fondo: 3 años → 30 pts
- Historial ahorro: 2 años → 30 pts
- Capacidad pago: cuota = 4,700 Bs / salario 20,000 Bs = 23.5% → 25 pts
- **Score total**: 85 pts → **ELEGIBLE**

### Resultado
- **Nivel de riesgo**: BAJO
- **Tasa aplicada**: 12% × 0.85 = **10.2%** (15% descuento)
- **Colateral requerido**: 100,000 × 20% = 20,000 Bs
- **Cuota mensual**: ~4,700 Bs
- **Total a pagar**: ~112,800 Bs

---

## Estados del Crédito

```
┌─────────────┐     ┌───────────────┐     ┌───────────┐
│  PENDIENTE  │────▶│ EN_EVALUACION │────▶│  APROBADA │
└─────────────┘     └───────────────┘     └───────────┘
                           │                    │
                           ▼                    ▼
                    ┌─────────────┐     ┌──────────────┐
                    │  RECHAZADA  │     │ DESEMBOLSADO │
                    └─────────────┘     └──────────────┘
                                                │
                           ┌────────────────────┤
                           ▼                    ▼
                    ┌─────────────┐     ┌─────────────┐
                    │   PAGADA     │     │ COLATERAL   │
                    │  (final)    │     │ EJECUTADA   │
                    └─────────────┘     │  (final)    │
                                         └─────────────┘
```

---

## Validaciones Importantes

| Regla | Descripción |
|-------|-------------|
| Un socio | No puede tener dos créditos activos |
| Score mínimo | Score < 50 = NO elegible |
| Capacidad pago | Cuota no puede exceder 30% del salario |
| Colateral | Score < 70 requiere colateral en cuenta de ahorro |
| Saldo colateral | Saldo disponible debe cubrir el requerimiento |

---

## Permisos por Rol

| Operación | Socio | Admin | Cajero |
|-----------|-------|-------|--------|
| Crear solicitud | ✅ | ✅ | ❌ |
| Evaluar | ❌ | ✅ | ❌ |
| Aprobar/Rechazar | ❌ | ✅ | ❌ |
| Desembolsar | ❌ | ✅ | ✅ |
| Registrar pago | ❌ | ✅ | ✅ |
| Ver mis solicitudes | ✅ | ✅ | ✅ |
| Ver cualquier solicitud | ❌ | ✅ | ✅ |

---

## Archivos Técnicos

Para más detalles técnicos:

| Documento | Descripción |
|-----------|-------------|
| [README.md](../modulos/creditos/README.md) | Documentación técnica completa |
| [API.md](../modulos/creditos/API.md) | Referencia de endpoints REST |
| [MODELO_DATOS.md](../modulos/creditos/MODELO_DATOS.md) | Esquema de base de datos |

---

## Glosario

| Término | Significado |
|---------|-------------|
| **Colateral** | Dinero en cuenta de ahorro que se retiene como garantía |
| **Score** | Puntuación crediticia (0-100) |
| **Amortización** | Proceso de pagar el préstamo en cuotas |
| **Desembolso** | Transferencia del dinero al socio |
| **Mora** | Retraso en el pago de cuotas |
| **Interés moratorio** | Penalidad por pagar tarde |
| **Saldo insoluto** | Lo que queda por pagar del capital |
| **Cuota** | Pago mensual fijo (capital + interés) |

---

*Última actualización: 2026-04-21*
*Versión del sistema: Issue #71 - Evaluar/Aprobar/Rechazar*
