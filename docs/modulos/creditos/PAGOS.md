# Módulo de Créditos - Sistema de Pagos y Amortización

## Resumen

Documentación técnica del sistema de amortización de créditos del Fondo de Ahorro. Incluye las fórmulas matemáticas del sistema francés, manejo de mora, ejecución de colateral y ejemplos de cálculo detallados.

---

## Sistema de Amortización

### Tipos de Sistema

El módulo implementa el **Sistema Francés de Amortización** (cuota fija) como método padrão para todos los créditos. Este sistema se caracteriza por:

- Cuota mensual constante (capital + intereses)
- Mayor proporción de intereses al inicio
- Mayor proporción de capital al final
- Facilita la planificación financiera del socio

> **Nota:** El sistema también soporta amortización americana (solo intereses, capital al final) para productos específicos como créditos puente.

---

## Sistema Francés - Fórmula y Cálculo

### Fórmula de Cuota Fija

```
Cuota = P * (r * (1 + r)^n) / ((1 + r)^n - 1)

Donde:
- P = Principal (monto del crédito)
- r = Tasa de interés periódica (tasa anual / 12)
- n = Número de cuotas (plazo en meses)
```

### Ejemplo de Cálculo

**Datos del crédito:**
- Principal: $50,000.00 USD
- Tasa anual: 14.50% (0.145)
- Plazo: 12 meses

**Cálculo:**
```
r = 0.145 / 12 = 0.012083333

Cuota = 50000 * (0.012083333 * (1 + 0.012083333)^12) / ((1 + 0.012083333)^12 - 1)
Cuota = 50000 * (0.012083333 * 1.154865) / (1.154865 - 1)
Cuota = 50000 * 0.013946 / 0.154865
Cuota = 50000 * 0.090084
Cuota = $4,550.42 USD
```

### Tabla de Amortización Ejemplo

| Cuota | Fecha | Capital | Interés | Seguros | Comisiones | Cuota | Saldo |
|-------|-------|---------|---------|--------|------------|-------|-------|
| 1 | 2026-05-15 | $3,971.08 | $579.34 | $0.00 | $0.00 | $4,550.42 | $46,028.92 |
| 2 | 2026-06-15 | $4,019.18 | $531.24 | $0.00 | $0.00 | $4,550.42 | $42,009.74 |
| 3 | 2026-07-15 | $4,067.82 | $482.60 | $0.00 | $0.00 | $4,550.42 | $37,941.92 |
| 4 | 2026-08-15 | $4,117.01 | $433.41 | $0.00 | $0.00 | $4,550.42 | $33,824.91 |
| 5 | 2026-09-15 | $4,166.77 | $383.65 | $0.00 | $0.00 | $4,550.42 | $29,658.14 |
| 6 | 2026-10-15 | $4,217.09 | $333.33 | $0.00 | $0.00 | $4,550.42 | $25,441.05 |
| 7 | 2026-11-15 | $4,268.00 | $282.42 | $0.00 | $0.00 | $4,550.42 | $21,173.05 |
| 8 | 2026-12-15 | $4,319.48 | $230.94 | $0.00 | $0.00 | $4,550.42 | $16,853.57 |
| 9 | 2027-01-15 | $4,371.55 | $178.87 | $0.00 | $0.00 | $4,550.42 | $12,482.02 |
| 10 | 2027-02-15 | $4,424.22 | $126.20 | $0.00 | $0.00 | $4,550.42 | $8,057.80 |
| 11 | 2027-03-15 | $4,477.49 | $72.93 | $0.00 | $0.00 | $4,550.42 | $3,580.31 |
| 12 | 2027-04-15 | $4,531.38 | $19.04 | $0.00 | $0.00 | $4,550.42 | $0.00 |

**Resumen:**
- Total intereses: $4,654.97 USD
- Total pagado: $54,654.97 USD
- Costo anual efectivo (CAE): 15.42%

---

## Cálculo de Interés por Período

### Interés Mensual

```
interes_mensual = saldo_insoluto * (tasa_anual / 12)

Ejemplo:
- Saldo insoluto: $46,028.92
- Tasa anual: 14.50%

interes = 46028.92 * (0.145 / 12) = 46028.92 * 0.012083333 = $579.34
```

### Interés sobre Saldo

El interés de cada cuota se calcula sobre el saldo insoluto al inicio del período:

```
I[n] = S[n-1] * r
C[n] = Cuota - I[n]  (parte de capital)
S[n] = S[n-1] - C[n]  (nuevo saldo)

Donde:
- I[n] = Interés de la cuota n
- S[n-1] = Saldo inicial (antes de cuota n)
- C[n] = Capital de la cuota n
- S[n] = Saldo final (después de cuota n)
```

---

## Estados de las Cuotas

### Ciclo de Vida de una Amortización

```
PENDIENTE ──► PAGADA (pago normal)
    │
    ├──► VENCIDA (pasó fechaVencimiento + grace period)
    │         │
    │         └──► CURSO_MORA (> 30 días vencida)
    │                   │
    │                   └──► EJECUTADA (colateral transferido)
    │
    └──► CANCELADA (prepago anticipado)
```

### Descripción de Estados

| Estado | Descripción | Condición |
|--------|-------------|-----------|
| `PENDIENTE` | Cuota vigente, esperando pago | Fecha actual < fechaVencimiento + grace period |
| `PAGADA` | Completamente pagada | montoPagado >= montoCuota |
| `VENCIDA` | Pasó fecha de vencimiento | Fecha actual > fechaVencimiento + diasGracia |
| `CURSO_MORA` | Con más de 30 días de mora | diasMora > 30 |
| `CANCELADA` | Cancelada por prepago | Estado anterior: PENDIENTE o VENCIDA |
| `EJECUTADA` | Colateral ejecutado | Cuota vencida > 90 días sin pago |

---

## Manejo de Mora

### Cálculo de Interés Moratorio

Cuando una cuota entra en estado `VENCIDA`, se calcula intereses moratorios:

```
interes_mora_diario = saldo_pendiente * (tasa_mora_diaria)

Donde:
- tasa_mora_diaria = penalidadMoraTasa / 30
- saldo_pendiente = monto_cuota - monto_pagado (si hay pago parcial)
```

### Ejemplo de Cálculo de Mora

**Datos:**
- Cuota vencer: 15/05/2026
- Días de gracia: 5
- Fecha actual: 25/05/2026
- Días de mora: 10
- Monto cuota: $4,550.42
- Tasa penalidad mora: 0.0005 (0.05% diario)

**Cálculo:**
```
interes_mora = 4550.42 * 10 * (0.0005 / 30)
interes_mora = 4550.42 * 10 * 0.000016667
interes_mora = 4550.42 * 0.00016667
interes_mora = $0.7584 por día

Total intereses moratorios (10 días) = $7.58 USD
```

### Notificaciones de Mora

El sistema genera notificaciones automáticas:

| Días Mora | Acción |
|-----------|--------|
| 0 (fecha vencimiento) | Recordatorio 3 días antes |
| 1-5 | Notificación de vencimiento (grace period) |
| 6-15 | Aviso de mora |
| 16-30 | Warning de ejecución de colateral |
| 31-60 | Notificación de ejecución inminente |
| 61-90 | Proceso de ejecución de colateral |

---

## Ejecución de Colateral

### Condiciones para Ejecución

Una cuota entra en proceso de ejecución cuando:
1. `estado = CURSO_MORA` (diasMora > 30)
2. `estado = VENCIDA` por más de 90 días
3. Existe colateral asignado (`colateral_cuenta_id`)

### Proceso de Ejecución

```
1. DETECCIÓN: Sistema detecta cuota con diasMora > 30
   └─► Evento: CuotaEnMoraEvent

2. NOTIFICACIÓN: Se notifica al socio (email, SMS)
   └─► Evento: NotificacionMoraEnviadaEvent

3. GRACIA: 60 días para regularizar (sin ejecutar)
   └─► Evento: PeriodoGraciaIniciadoEvent

4. EVALUACIÓN: Si diasMora > 90 sin regularización
   └─► Evento: EjecucionColateralPendienteEvent

5. EJECUCIÓN:
   - Transferir saldo retenido de cuenta colateral a cuenta de crédito
   - Actualizar estado de cuota a EJECUTADA
   - Liberar colateral (saldoRetenido = 0)
   └─► Evento: ColateralEjecutadoEvent

6. REGISTRO: Registrar en auditoría regulatoria (SUDEBAN)
   └─► Evento: ReporteRegulatorioEnviadoEvent
```

### Ejemplo de Ejecución

**Datos:**
- Crédito con colateral: $10,000.00 USD retenidos
- Cuota vencida #6: $4,550.42 + $45.50 mora = $4,595.92 total
- Saldo disponible cuenta colateral: $12,000.00

**Proceso:**
```
1. Verificar saldo disponible >= requerimiento
   - Saldo disponible: $12,000.00
   - Colateral retenido: $10,000.00
   - Disponible para ejecución: $12,000.00

2. Transferir monto necesario
   - Monto a ejecutar: min(cuota + mora, colateral retenido)
   - Monto: $4,595.92

3. Actualizar cuenta colateral
   - Anterior saldo retenido: $10,000.00
   - Nuevo saldo retenido: $10,000.00 - $4,595.92 = $5,404.08

4. Actualizar cuota
   - estado: CURSO_MORA → EJECUTADA
   - colateral_ejecutada: true
   - monto_pagado: $4,595.92
```

### Código de Implementación

```java
@Service
@RequiredArgsConstructor
public class EjecutarColateralUseCase {
    
    private final CuentaGarantiaRepository cuentaGarantiaRepository;
    private final AmortizacionRepository amortizacionRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public void ejecutarColateral(UUID amortizacionId, int diasMora) {
        
        // 1. Obtener amortización con lock
        Amortizacion amortizacion = amortizacionRepository
            .findByIdWithLock(amortizacionId)
            .orElseThrow(() -> new CuotaNoEncontradaException(amortizacionId));
        
        // 2. Verificar condiciones
        if (amortizacion.estado() != EstadoAmortizacion.CURSO_MORA) {
            throw new EstadoInvalidoException("Cuota no está en curso de mora");
        }
        
        if (diasMora < 90 && !amortizacion.colateralEjecutada()) {
            throw new CondicionNoCumplidaException("No cumple días mínimos de mora para ejecución");
        }
        
        // 3. Obtener solicitud para extraer colateral
        SolicitudCredito solicitud = amortizacion.obtenerSolicitud();
        
        if (solicitud.colateralCuentaId() == null) {
            throw new SinColateralException("Solicitud no tiene colateral asignado");
        }
        
        // 4. Calcular monto a ejecutar
        BigDecimal montoTotal = amortizacion.montoCuota()
            .add(amortizacion.interesMora());
        
        BigDecimal montoColateral = solicitud.colateralMontoRetenido();
        BigDecimal montoEjecutar = montoTotal.min(montoColateral);
        
        // 5. Transferir de colateral
        cuentaGarantiaRepository.retenerSaldo(
            solicitud.colateralCuentaId(),
            montoEjecutar.negate()  // Liberar (quitar retención y transferir)
        );
        
        // 6. Actualizar amortización
        amortizacion.marcarEjecutada(montoEjecutar, diasMora);
        amortizacionRepository.save(amortizacion);
        
        // 7. Publicar eventos
        eventPublisher.publish(new ColateralEjecutadoEvent(
            solicitud.id(),
            amortizacionId,
            montoEjecutar,
            diasMora
        ));
        
        eventPublisher.publish(new ReporteRegulatorioEvent(
            "SUDEBAN",
            "EJECUCION_COLATERAL",
            solicitud.id(),
            montoEjecutar
        ));
    }
}
```

---

## Prepago (Cancelación Anticipada)

### Cálculo de Prepago

Cuando un socio decide pagar anticipadamente (cancelar el crédito), se debe calcular el saldo insoluto más una penalidad por cierre anticipado:

```
MontoPrepago = saldoInsoluto + penalidadCancelacion

Donde:
- penalidadCancelacion = min(saldoInsoluto * 0.02, montoMaximoPenalidad)
- montoMaximoPenalidad = 3 * cuotaMensual
```

### Proceso de Cancelación

```
1. VALIDAR: Crédito está en estado DESEMBOLSADO
2. CALCULAR: Saldo insoluto + penalidad
3. PAGAR: Socio paga el monto total
4. ACTUALIZAR: Plan → CANCELADO, todas las cuotas → CANCELADA
5. LIBERAR: Colateral (si existe)
6. GENERAR: Constancia de cancelación
```

---

## Flujo de Pago de Cuotas

### Flujo Completo

```
SOCIO/CAJERO                                    SISTEMA
    │                                               │
    │──► POST /cuotas/{id}/pago ─────────────────► │
    │                                               │
    │                                               │ 1. Validar DTO
    │                                               │ 2. Verificar estado == PENDIENTE
    │                                               │ 3. Verificar idempotencia (referencia_pago)
    │                                               │ 4. Verificar version (optimistic lock)
    │                                               │
    │                                               │ 5. Obtener lock en amortización
    │                                               │
    │                                               │ 6. Validar monto >= monto_cuota
    │                                               │
    │                                               │ 7. Actualizar: monto_pagado, fecha_pago, estado=PAGADA
    │                                               │
    │                                               │ 8. Recalcular saldo_insoluto del plan
    │                                               │
    │                                               │ 9. Si ultima cuota: verificar plan completo
    │                                               │
    │                                               │ 10. Si todo OK: COMMIT
    │                                               │
    │◄──── Response: 200 OK ────────────────────── │
    │                                               │
    │                                               │ Async: Verificar si todas PAGADAS → plan.FINALIZADO
    │                                               │ Async: Si colateral: liberar cuando plan.FINALIZADO
```

### Código del Use Case

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarPagoCuotaUseCase {
    
    private final AmortizacionRepository amortizacionRepository;
    private final PlanAmortizacionRepository planRepository;
    private final CuentaGarantiaRepository cuentaGarantiaRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public PagoCuotaResponse ejecutar(PagoCuotaRequest request) {
        
        // 1. Verificar idempotencia
        if (request.referenciaPago() != null) {
            amortizacionRepository.findByReferenciaPago(request.referenciaPago())
                .ifPresent(a -> {
                    throw new PagoDuplicadoException(request.referenciaPago());
                });
        }
        
        // 2. Obtener con lock
        Amortizacion amortizacion = amortizacionRepository
            .findByIdWithLock(request.amortizacionId())
            .orElseThrow(() -> new CuotaNoEncontradaException(request.amortizacionId()));
        
        // 3. Verificar estado
        if (amortizacion.estado() != EstadoAmortizacion.PENDIENTE &&
            amortizacion.estado() != EstadoAmortizacion.VENCIDA) {
            throw new EstadoCuotaInvalidoException(amortizacion.estado());
        }
        
        // 4. Validar monto
        BigDecimal montoRequerido = amortizacion.montoCuota()
            .add(amortizacion.interesMora() != null ? amortizacion.interesMora() : BigDecimal.ZERO);
        
        if (request.monto().compareTo(montoRequerido) < 0) {
            throw new MontoInsuficienteException(request.monto(), montoRequerido);
        }
        
        // 5. Registrar pago
        amortizacion.registrarPago(
            request.monto(),
            request.fechaPago() != null ? request.fechaPago() : LocalDate.now(),
            request.referenciaPago()
        );
        
        amortizacionRepository.save(amortizacion);
        
        // 6. Actualizar plan
        PlanAmortizacion plan = planRepository.findById(amortizacion.planId())
            .orElseThrow(() -> new PlanNoEncontradoException(amortizacion.planId()));
        
        plan.registrarPago(amortizacion.montoPagado());
        planRepository.save(plan);
        
        // 7. Verificar si es última cuota
        if (plan.estaCompletamentePagado()) {
            plan.marcarFinalizado();
            planRepository.save(plan);
            
            // Liberar colateral
            SolicitudCredito solicitud = plan.obtenerSolicitud();
            if (solicitud.colateralCuentaId() != null) {
                cuentaGarantiaRepository.liberarSaldo(
                    solicitud.colateralCuentaId(),
                    solicitud.colateralMontoRetenido()
                );
            }
            
            eventPublisher.publish(new CreditoFinalizadoEvent(solicitud.id()));
        }
        
        log.info("Pago registrado: cuota {} por monto {}", 
            amortizacion.numeroCuota(), request.monto());
        
        return new PagoCuotaResponse(
            amortizacion.id(),
            amortizacion.numeroCuota(),
            EstadoAmortizacion.PAGADA,
            amortizacion.montoPagado(),
            amortizacion.fechaPago(),
            request.referenciaPago(),
            plan.saldoPendiente()
        );
    }
}
```

---

## Servicios de Dominio

### CuentaGarantiaRepository (Interfaz)

```java
public interface CuentaGarantiaRepository {
    
    /**
     * Obtiene el saldo disponible para garantía
     * @param cuentaId ID de la cuenta de ahorro
     * @return saldo disponible (total - retenido)
     */
    BigDecimal obtenerSaldoDisponible(UUID cuentaId);
    
    /**
     * Verifica si el saldo disponible cubre el requerimiento
     * @param cuentaId ID de la cuenta
     * @param montoRequerido Monto requerido como colateral
     * @return true si cubre, false si no
     */
    boolean verificarSaldoParaColateral(UUID cuentaId, BigDecimal montoRequerido);
    
    /**
     * Retiene saldo para colateral (al aprobar crédito)
     * @param cuentaId ID de la cuenta
     * @param monto Monto a retener
     */
    void retenerSaldo(UUID cuentaId, BigDecimal monto);
    
    /**
     * Libera saldo retenido (al pagar última cuota o ejecutar colateral)
     * @param cuentaId ID de la cuenta
     * @param monto Monto a liberar
     */
    void liberarSaldo(UUID cuentaId, BigDecimal monto);
    
    /**
     * Transfiere saldo (para ejecución de colateral)
     * @param cuentaOrigenId Cuenta de donde se toma
     * @param cuentaDestinoId Cuenta a donde se transfiere
     * @param monto Monto a transferir
     */
    void transferirSaldo(UUID cuentaOrigenId, UUID cuentaDestinoId, BigDecimal monto);
}
```

---

## Métricas de Morosidad

### Indicadores de Alerta

| Indicador | Fórmula | Umbral Alerta |
|-----------|---------|---------------|
| Tasa de Mora | (cuotasVencidas / totalCuotas) * 100 | > 5% |
| Cartera Vencida | sum(saldosVencidos) / sum(saldosCartera) | > 3% |
| Días Promedio de Mora | sum(diasMora * monto) / sum(monto) | > 45 días |
| Colaterales Ejecutados | count(colateral_ejecutada = true) / mes | > 2% |

### Reportes Regulatorios

El sistema genera reportes automáticos para SUDEBAN:

| Reporte | Frecuencia | Contenido |
|---------|------------|-----------|
| Cartera de Créditos | Mensual | Estados, montos, plazos |
| Morosidad | Quincenal | Cuotas vencidas, días mora |
| Ejecuciones | Eventual | Colaterales ejecutados, montos |
| Incumplimiento | Eventual | Créditos fallidos, recuperaciones |

---

## Ejemplos de Escenarios

### Escenario 1: Pago Normal a Tiempo

**Situación:** Socio paga cuota #3 el día 10/07/2026 (5 días antes del vencimiento 15/07/2026)

**Resultado:**
```
Cuota #3:
- Estado: PAGADA
- Fecha pago: 2026-07-10
- Monto pagado: $4,550.42
- Intereses moratorios: $0.00
- Saldo insoluto restante: $33,824.91
```

### Escenario 2: Pago con Mora Leve (10 días)

**Situación:** Socio paga cuota #3 el día 20/07/2026 (5 días después del vencimiento + 5 días grace period)

**Resultado:**
```
Cuota #3:
- Estado: PAGADA
- Fecha pago: 2026-07-20
- Monto pagado: $4,558.17 ($4,550.42 + $7.75 mora)
- Intereses moratorios: $7.75 (10 días * $0.775/día)
- Saldo insoluto restante: $33,824.91
```

### Escenario 3: Prepago Total

**Situación:** Socio decide pagar todo el crédito después de la cuota #6

**Cálculo:**
```
Plan al momento del prepago (cuota #7 pendiente):
- Saldo insoluto: $21,173.05
- Penalidad (2%): $423.46 (min del 2%)
- MontoPrepago: $21,596.51

Resultado:
- Plan: CANCELADO
- Todas las cuotas pendientes: CANCELADA
- Colateral: Liberado
- Comprobante de cancelación generado
```

### Escenario 4: Ejecución de Colateral

**Situación:** Socio incumple por 95 días en cuota #8

**Proceso:**
```
1. Día 31: Cuota #8 pasa a CURSO_MORA
2. Día 45: Notificación de ejecución inminente
3. Día 90: Sin regularización → Ejecución de colateral
4. Día 91: Se ejecuta
   - Saldo retenido colateral: $10,000.00
   - Monto necesario: $4,595.92
   - Saldo transferido a cuenta de crédito: $4,595.92
   - Saldo restante colateral: $5,404.08
   - Cuota #8: EJECUTADA, montoPagado = $4,595.92
5. Día 92: Reporte a SUDEBAN
```

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0.0 | 2026-04-14 | @documentador | Creación inicial |
| 1.0.1 | 2026-04-14 | @documentador | Agregado proceso de ejecución de colateral |
| 1.0.2 | 2026-04-14 | @documentador | Agregadas métricas de morosidad |

---

## Referencias

- Especificación técnica: `/docs/modulos/creditos/SPEC.md`
- Referencia API: `/docs/modulos/creditos/API.md`
- Modelo de datos: `/docs/modulos/creditos/MODELO_DATOS.md`
- Auditoría de seguridad: `/docs/auditorias/ULTIMA_AUDITORIA.md`