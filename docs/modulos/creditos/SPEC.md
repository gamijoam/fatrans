# Módulo de Gestión de Créditos - Especificación Técnica

## Resumen

El módulo de **Gestión de Créditos** es el componente central del sistema Fondo de Ahorro para administración de productos de crédito, evaluaciones crediticias, planes de amortización y gestión de pagos. Implementa el dominio financiero siguiendo Clean Architecture + DDD con strict compliance a regulaciones SUDEBAN/SUDECA mexicanas.

> **Estado del Módulo:** Especificación basada en PM con correcciones de auditoría de seguridad.
> **Precisión Monetaria:** NUMERIC(15,4) para todos los montos.

---

## Arquitectura del Módulo

### Estructura de Capas (Clean Architecture)

```
src/main/java/com/tufondo/creditos/
├── domain/
│   ├── model/              # Entidades del dominio (TipoCredito, SolicitudCredito, etc.)
│   ├── repository/         # Interfaces de repositorio
│   ├── service/            # Lógica de dominio pura
│   └── event/              # Eventos de dominio
├── application/
│   ├── usecase/            # Casos de uso (UC-CRE-01 a UC-CRE-09)
│   ├── dto/                # Data Transfer Objects
│   └── mapper/             # Mapeadores Entity<->DTO
├── infrastructure/
│   ├── persistence/        # Implementaciones JPA (repositories)
│   ├── external/           # Integración con módulo Ahorros ( CuentaGarantiaRepository)
│   └── security/           # Filtros, rate limiting
└── api/
    ├── controller/         # REST Controllers
    └── validator/          # Validaciones custom
```

---

## Entidades del Dominio

### 1. TipoCredito

```java
@Entity
@Table(name = "tipos_credito")
public class TipoCredito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    private String codigo;  // IDENTIFICADOR_UNICO
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "descripcion", length = 500)
    private String descripcion;
    
    @Column(name = "tasa_interes_anual", precision = 8, scale = 4, nullable = false)
    private BigDecimal tasaInteresAnual;
    
    @Column(name = "plazo_minimo_meses", nullable = false)
    private Integer plazoMinimoMeses;
    
    @Column(name = "plazo_maximo_meses", nullable = false)
    private Integer plazoMaximoMeses;
    
    @Column(name = "monto_minimo", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoMinimo;
    
    @Column(name = "monto_maximo", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoMaximo;
    
    @Column(name = "porcentaje_requerimiento_colateral", precision = 5, scale = 2)
    private BigDecimal porcentajeRequerimientoColateral;  // % del monto requerido como colateral
    
    @Column(name = "comision_apertura", precision = 5, scale = 4)
    private BigDecimal comisionApertura;
    
    @Column(name = "penalidad_mora_tasa", precision = 8, scale = 4)
    private BigDecimal penalidadMoraTasa;  // Tasa diaria de penalización
    
    @Column(name = "dias_gracia", nullable = false)
    private Integer diasGracia;  // Días antes de marcar vencida
    
    @Column(name = "activo", nullable = false)
    private Boolean activo;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}
```

---

### 2. SolicitudCredito

```java
@Entity
@Table(name = "solicitudes_credito")
public class SolicitudCredito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "numero_solicitud", unique = true, nullable = false, length = 25)
    private String numeroSolicitud;  // Formato: SOL-CRED-YYYY-XXXXXX (SecureRandom)
    
    @Column(name = "socio_id", nullable = false)
    private Long socioId;
    
    @Column(name = "tipo_credito_id", nullable = false)
    private Long tipoCreditoId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_credito_id", insertable = false, updatable = false)
    private TipoCredito tipoCredito;
    
    @Column(name = "monto_solicitado", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoSolicitado;
    
    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;
    
    @Column(name = "tasa_interes_aplicada", precision = 8, scale = 4)
    private BigDecimal tasaInteresAplicada;  // Puede diferir de la tasa base
    
    @Column(name = "cuota_mensual_estimada", precision = 19, scale = 4)
    private BigDecimal cuotaMensualEstimada;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado;
    
    @Column(name = "colateral_cuenta_id")
    private UUID colateralCuentaId;  // FK a cuenta de ahorro (nullable)
    
    @Column(name = "colateral_monto_retenido", precision = 19, scale = 4)
    private BigDecimal colateralMontoRetenido;
    
    @Column(name = "destino_credito", length = 500)
    private String destinoCredito;
    
    @Column(name = "evaluacion_id")
    private UUID evaluacionId;
    
    @Column(name = "plan_amortizacion_id")
    private UUID planAmortizacionId;
    
    @Column(name = "referencia_desembolso", length = 100)
    private String referenciaDesembolso;
    
    @Column(name = "cuenta_destino", length = 34)
    private String cuentaDestino;  // IBAN o número de cuenta
    
    @Column(name = "notas")
    private String notas;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    // Builder pattern
    private SolicitudCredito() {}
    
    public static SolicitudCreditoBuilder builder() {
        return new SolicitudCreditoBuilder();
    }
    
    // Método para generar número de solicitud no enumerable
    @PrePersist
    public void generarNumeroSolicitud() {
        if (this.numeroSolicitud == null) {
            int year = LocalDate.now().getYear();
            int secuencial = new SecureRandom().nextInt(999999);
            this.numeroSolicitud = String.format("SOL-CRED-%d-%06d", year, secuencial);
        }
    }
    
    // Validación de transición de estado
    public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
        return this.estado.puedeTransicionarA(nuevoEstado);
    }
}
```

#### Enum: EstadoSolicitud

```java
public enum EstadoSolicitud {
    PENDIENTE,
    EN_EVALUACION,
    APROBADA,
    RECHAZADA,
    CANCELADA,
    DESEMBOLSADO,
    COLATERAL_EJECUTADO;  // Estado final cuando se ejecuta el colateral
    
    /**
     * Valida si la transición al nuevo estado es válida.
     * Transiciones válidas:
     * - PENDIENTE → EN_EVALUACION
     * - EN_EVALUACION → APROBADA o RECHAZADA
     * - APROBADA → DESEMBOLSADO
     * - DESEMBOLSADO → COLATERAL_EJECUTADO (si se ejecuta colateral por mora)
     * - RECHAZADA, CANCELADA, COLATERAL_EJECUTADO → (ninguna, son estados finales)
     */
    public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
        return switch (this) {
            case PENDIENTE -> nuevoEstado == EN_EVALUACION;
            case EN_EVALUACION -> nuevoEstado == APROBADA || nuevoEstado == RECHAZADA;
            case APROBADA -> nuevoEstado == DESEMBOLSADO;
            case DESEMBOLSADO -> nuevoEstado == COLATERAL_EJECUTADO;
            case RECHAZADA, CANCELADA, COLATERAL_EJECUTADO -> false;
        };
    }
}
```

---

### 3. EvaluacionCrediticia

```java
@Entity
@Table(name = "evaluaciones_crediticias")
public class EvaluacionCrediticia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "solicitud_id", nullable = false)
    private UUID solicitudId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", insertable = false, updatable = false)
    private SolicitudCredito solicitud;
    
    @Column(name = "socio_id", nullable = false)
    private Long socioId;
    
    // Factores de evaluación
    @Column(name = "puntaje_antiguedad", nullable = false)
    private Integer puntajeAntiguedad;  // 0-30 pts
    
    @Column(name = "puntaje_historial_ahorro", nullable = false)
    private Integer puntajeHistorialAhorro;  // 0-30 pts
    
    @Column(name = "puntaje_capacidad_pago", nullable = false)
    private Integer puntajeCapacidadPago;  // 0-40 pts
    
    @Column(name = "score_interno", nullable = false)
    private Integer scoreInterno;  // 0-100 pts (suma de los 3)
    
    // SEGURIDAD: Campos de auditoría criptográfica
    @Column(name = "score_hash", length = 64, nullable = false)
    private String scoreHash;  // SHA-256 del cálculo completo
    
    @Column(name = "factores_serializados", columnDefinition = "TEXT", nullable = false)
    private String factoresSerializados;  // JSON con breakdown del score
    
    @Column(name = "firma_verificable", length = 128)
    private String firmaVerificable;  // Firma con clave asimétrica
    
    @Column(name = "evaluacion_id_original")
    private UUID evaluacionIdOriginal;  // Para detectar modificaciones
    
    // Resultado
    @Column(name = "elegible", nullable = false)
    private Boolean elegible;
    
    @Column(name = "nivel_riesgo", length = 20)
    private String nivelRiesgo;  // BAJO, MEDIO, ALTO
    
    @Column(name = "tasa_interes_final", precision = 8, scale = 4)
    private BigDecimal tasaInteresFinal;
    
    @Column(name = "mensaje_decision", length = 500)
    private String mensajeDecision;
    
    @Column(name = "evaluador", length = 100)
    private String evaluador;  // Usuario que realizó la evaluación (admin o "SISTEMA")
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Version
    private Long version;  // Optimistic locking
    
    // Builder pattern
    private EvaluacionCrediticia() {}
    
    public static EvaluacionCrediticiaBuilder builder() {
        return new EvaluacionCrediticiaBuilder();
    }
    
    // Método para calcular y verificar hash
    public void calcularHash() {
        String factores = String.format("%d|%d|%d|%d|%s",
            puntajeAntiguedad, puntajeHistorialAhorro, puntajeCapacidadPago,
            scoreInterno, socioId);
        this.scoreHash = SHA256(factores);
        this.factoresSerializados = serializeFactores();
    }
    
    private String SHA256(String input) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
```

---

### 4. PlanAmortizacion

```java
@Entity
@Table(name = "planes_amortizacion")
public class PlanAmortizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "solicitud_id", nullable = false)
    private UUID solicitudId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", insertable = false, updatable = false)
    private SolicitudCredito solicitud;
    
    @Column(name = "monto_principal", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoPrincipal;
    
    @Column(name = "tasa_interes", precision = 8, scale = 4, nullable = false)
    private BigDecimal tasaInteres;
    
    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;
    
    @Column(name = "frecuencia_pago", length = 20, nullable = false)
    private String frecuenciaPago;  // MENSUAL, QUINCENAL, SEMANAL
    
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    
    @Column(name = "total_intereses", precision = 19, scale = 4)
    private BigDecimal totalIntereses;
    
    @Column(name = "total_pagado", precision = 19, scale = 4)
    private BigDecimal totalPagado;
    
    @Column(name = "saldo_pendiente", precision = 19, scale = 4)
    private BigDecimal saldoPendiente;
    
    @Column(name = "numero_cuotas", nullable = false)
    private Integer numeroCuotas;
    
    @Column(name = "cuota_mensual", precision = 19, scale = 4)
    private BigDecimal cuotaMensual;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPlanAmortizacion estado;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    // Builder pattern
    private PlanAmortizacion() {}
    
    public static PlanAmortizacionBuilder builder() {
        return new PlanAmortizacionBuilder();
    }
}
```

#### Enum: EstadoPlanAmortizacion

```java
public enum EstadoPlanAmortizacion {
    ACTIVO,       // En curso
    CANCELADO,    // Cancelado anticipadamente
    FINALIZADO,   // Todas las cuotas pagadas
    VENCIDO       // Con cuotas en mora > 90 días
}
```

---

### 5. Amortizacion

```java
@Entity
@Table(name = "amortizaciones")
public class Amortizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "plan_id", nullable = false)
    private UUID planId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    private PlanAmortizacion plan;
    
    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;
    
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
    
    @Column(name = "fecha_pago")
    private LocalDate fechaPago;
    
    @Column(name = "capital", precision = 19, scale = 4, nullable = false)
    private BigDecimal capital;
    
    @Column(name = "interes", precision = 19, scale = 4, nullable = false)
    private BigDecimal interes;
    
    @Column(name = "seguros", precision = 19, scale = 4)
    private BigDecimal seguros;
    
    @Column(name = "comisiones", precision = 19, scale = 4)
    private BigDecimal comisiones;
    
    @Column(name = "monto_cuota", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoCuota;
    
    @Column(name = "saldo_insoluto", precision = 19, scale = 4)
    private BigDecimal saldoInsoluto;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAmortizacion estado;
    
    @Column(name = "dias_mora")
    private Integer diasMora;
    
    @Column(name = "interes_mora", precision = 19, scale = 4)
    private BigDecimal interesMora;
    
    @Column(name = "monto_pagado", precision = 19, scale = 4)
    private BigDecimal montoPagado;
    
    @Column(name = "referencia_pago", unique = true)
    private String referenciaPago;  // Clave de idempotencia
    
    @Column(name = "colateral_ejecutada", nullable = false)
    private Boolean colateralEjecutada = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;  // CRÍTICO: Previene double-payment
    
    // Builder pattern
    private Amortizacion() {}
    
    public static AmortizacionBuilder builder() {
        return new AmortizacionBuilder();
    }
}
```

#### Enum: EstadoAmortizacion

```java
public enum EstadoAmortizacion {
    PENDIENTE,       // Cuota pendiente de pago
    PAGADA,          // Pagada exitosamente
    VENCIDA,         // Pasó fechaVencimiento sin pago (después de grace period)
    CURSO_MORA,      // Con más de 30 días en mora
    CANCELADA,       // Cancelada por prepago o refinanciamiento
    EJECUTADA        // Colateral ejecutada por incumplimiento
}
```

---

## Reglas de Negocio

### RF-01: Elegibilidad para Crédito

| Condición | Requisito | Validación |
|-----------|-----------|------------|
| RN-E-01 | Score interno >= 50 | Verificar `evaluacionCrediticia.scoreInterno >= 50` |
| RN-E-02 | Colateral si score < 70 | Si score < 70, `colateralCuentaId` es requerido |
| RN-E-03 | Capacidad de pago | Cuota mensual <= 30% del salario estimado |
| RN-E-04 | Sin créditos activos | El socio no debe tener solicitudes en estado DESEMBOLSADO |

### RF-02: Score Interno

```
scoreInterno = puntajeAntiguedad + puntajeHistorialAhorro + puntajeCapacidadPago

Factores:
- Antigüedad: hasta 30 puntos
  * < 6 meses: 0 pts
  * 6-12 meses: 10 pts
  * 1-2 años: 20 pts
  * > 2 años: 30 pts

- Historial de ahorro: hasta 30 puntos
  * Sin cuenta: 0 pts
  * < 6 meses: 10 pts
  * 6-12 meses: 20 pts
  * > 12 meses: 30 pts

- Capacidad de pago: hasta 40 puntos
  * Cuota <= 15% salario: 40 pts
  * Cuota 15-25% salario: 25 pts
  * Cuota 25-30% salario: 10 pts
  * Cuota > 30% salario: 0 pts
```

### RF-03: Colateral

| Regla | Descripción |
|-------|-------------|
| RN-C-01 | Si score < 70, requiere colateral |
| RN-C-02 | Monto colateral >= (porcentajeRequerimientoColateral * montoSolicitado) |
| RN-C-03 | Saldo disponible en cuenta de ahorro debe cubrir el requerimiento |
| RN-C-04 | Al aprobar: `saldoRetenido` se actualiza en CuentaAhorro |
| RN-C-05 | Al pagar última cuota: liberar `saldoRetenido` |
| RN-C-06 | Al entrar en mora > 30 días: ejecutar colateral |

### RF-04: Tasas de Interés

| Condición | Tasa Aplicada |
|-----------|--------------|
| Score >= 80 | tasaBase * 0.85 (15% descuento) |
| Score 70-79 | tasaBase * 0.95 (5% descuento) |
| Score 60-69 | tasaBase (sin modificación) |
| Score 50-59 | tasaBase * 1.10 (10% recargo) |

### RF-05: Plazos y Montos

| Tipo Crédito | Plazo Mín | Plazo Máx | Monto Mín | Monto Máx |
|--------------|-----------|-----------|-----------|-----------|
| IDENTIFICADOR_UNICO | 6 meses | 36 meses | 10,000 | 500,000 |
| MICRO_CREDITO | 1 mes | 12 meses | 1,000 | 50,000 |
| CREDITO_VEHICULO | 12 meses | 60 meses | 50,000 | 1,000,000 |

---

## Casos de Uso

### UC-CRE-01: Crear Solicitud de Crédito

| Atributo | Valor |
|----------|-------|
| Actor | SOCIO |
| Descripción | Permite a un socio solicitar un crédito indicando monto, plazo y destino |
| Precondiciones | Socio autenticado, sin créditos activos |
| Postcondiciones | Solicitud creada en estado PENDIENTE |

### UC-CRE-02: Evaluar Solicitud de Crédito

| Atributo | Valor |
|----------|-------|
| Actor | ADMIN / SISTEMA |
| Descripción | Calcula score interno, verifica elegibilidad y colateral |
| Precondiciones | Solicitud en estado PENDIENTE |
| Postcondiciones | Evaluación creada, solicitud pasa a EN_EVALUACION |

### UC-CRE-03: Aprobar/Rechazar Crédito

| Atributo | Valor |
|----------|-------|
| Actor | ADMIN |
| Descripción | Aprueba o rechaza la solicitud basado en la evaluación |
| Precondiciones | Solicitud en estado EN_EVALUACION |
| Postcondiciones | Solicitud pasa a APROBADA o RECHAZADA |

### UC-CRE-04: Generar Plan de Amortización

| Atributo | Valor |
|----------|-------|
| Actor | SISTEMA |
| Descripción | Genera el calendario de pagos (sistema francés) |
| Precondiciones | Solicitud en estado APROBADA |
| Postcondiciones | PlanAmortizacion creado con 12 cuotas mensuales |

### UC-CRE-05: Desembolsar Crédito

| Atributo | Valor |
|----------|-------|
| Actor | ADMIN / SISTEMA |
| Descripción | Ejecuta la transferencia al socio y actualiza estado |
| Precondiciones | Solicitud APROBADA con plan generado |
| Postcondiciones | Estado DESEMBOLSADO, fondos transferidos, colateral retenida |

### UC-CRE-06: Registrar Pago de Cuota

| Atributo | Valor |
|----------|-------|
| Actor | CAJERO / ADMIN / SOCIO (auto-pago) |
| Descripción | Registra el pago de una cuota específica |
| Precondiciones | Cuota en estado PENDIENTE o VENCIDA |
| Postcondiciones | Cuota pasa a PAGADA, saldo pendiente actualizado |

### UC-CRE-07: Consultar Estado de Crédito

| Atributo | Valor |
|----------|-------|
| Actor | SOCIO (solo propio), ADMIN |
| Descripción | Consulta el detalle de un crédito |
| Precondiciones | Crédito existe |
| Postcondiciones | Validación IDOR: socio solo ve sus propios créditos |

### UC-CRE-08: Simular Crédito

| Atributo | Valor |
|----------|-------|
| Actor | SOCIO, PÚBLICO (con restricción) |
| Descripción | Simula condiciones de crédito sin crear solicitud |
| Precondiciones | Ninguna (requiere autenticación ligera + rate limiting) |
| Postcondiciones | Retorna план de pagos estimado |

### UC-CRE-09: Ejecutar Colateral

| Atributo | Valor |
|----------|-------|
| Actor | SISTEMA |
| Descripción | Ejecuta el colateral cuando cuota está en mora > 30 días |
| Precondiciones | Cuota en estado CURSO_MORA |
| Postcondiciones | Fondos transferidos a cuenta de crédito, colateral liberada |

---

## Correcciones de Seguridad (Auditoría)

### CS-01: Rate Limiting en `/simulador`

```java
@PostMapping("/api/v1/simulador")
public ResponseEntity<SimulacionResponse> simular(
        @RequestBody @Valid SimulacionRequest request,
        @RequestHeader(value = "X-Forwarded-For", required = false) String clientIp,
        HttpServletRequest httpRequest) {
    
    // Rate limiting: máx 10 solicitudes por IP por minuto
    String rateLimitKey = "simulador:" + (clientIp != null ? clientIp : httpRequest.getRemoteAddr());
    rateLimiter.acquire(rateLimitKey, 10, 60); // 10 por minuto
    
    // Log de auditoría
    auditLogger.log("SIMULADOR_ACCESO", clientIp, request);
}
```

### CS-02: Score con Hash y Firma

```java
// En EvaluacionCrediticia - campos de auditoría criptográfica
@Column(name = "score_hash", length = 64, nullable = false)
private String scoreHash;  // SHA-256

@Column(name = "firma_verificable", length = 128)
private String firmaVerificable;  // RSA signature
```

### CS-03: Número de Solicitud No Enumerable

```java
@PrePersist
public void generarNumeroSolicitud() {
    // SecureRandom, no secuencial
    this.numeroSolicitud = "SOL-CRED-" + 
        LocalDate.now().getYear() + "-" + 
        String.format("%06d", new SecureRandom().nextInt(999999));
}
```

### CS-04: Optimistic Locking en Amortización

```java
@Version
private Long version;  // Previene double-payment

// En el use case
Optional<Amortizacion> amort = amortizacionRepository.findByIdWithLock(id);
if (amort.get().getVersion() != request.getVersion()) {
    throw new OptimisticLockException();
}
```

### CS-05: Validación IDOR en Consultas

```java
@GetMapping("/api/v1/creditos/{numeroSolicitud}")
public ResponseEntity<CreditoResponse> consultarEstado(
        @PathVariable String numeroSolicitud,
        @Authentication UsuarioActual usuario) {
    
    return solicitudCreditoRepository.findByNumeroSolicitud(numeroSolicitud)
        .map(solicitud -> {
            // CRÍTICO: Verificar acceso
            if (usuario.rol() == Rol.SOCIO && 
                !solicitud.getSocioId().equals(usuario.getSocioId())) {
                throw new AccesoNoAutorizadoException();
            }
            return creditoMapper.toResponse(solicitud);
        })
        .orElseThrow(() -> new SolicitudNoEncontradaException());
}
```

---

## Validaciones de DTOs

### CrearSolicitudCreditoRequest

```java
public class CrearSolicitudCreditoRequest {
    
    @NotNull(message = "tipoCreditoId es requerido")
    private Long tipoCreditoId;
    
    @NotNull(message = "montoSolicitado es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "999999999.9999", message = "monto excede límite máximo")
    private BigDecimal montoSolicitado;
    
    @NotNull(message = "plazoMeses es requerido")
    @Min(value = 1, message = "plazo mínimo 1 mes")
    @Max(value = 360, message = "plazo máximo 360 meses")
    private Integer plazoMeses;
    
    @Size(max = 500, message = "destinoCredito no puede exceder 500 caracteres")
    private String destinoCredito;
    
    @Size(max = 34, message = "cuentaDestino no puede exceder 34 caracteres")
    private String cuentaDestino;
}
```

### SimulacionRequest

```java
public class SimulacionRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    private BigDecimal monto;
    
    @NotNull(message = "plazoMeses es requerido")
    @Min(value = 1, message = "plazo mínimo 1 mes")
    @Max(value = 360, message = "plazo máximo 360 meses")
    private Integer plazoMeses;
    
    @NotNull(message = "tasa es requerida")
    @DecimalMin(value = "0.0001", message = "tasa debe ser > 0")
    @DecimalMax(value = "1.0", message = "tasa no puede exceder 100%")
    private BigDecimal tasa;
}
```

---

## Eventos de Dominio

| Evento | Descripción | Payload |
|--------|-------------|---------|
| SolicitudCreada | Nueva solicitud registrada | `{solicitudId, numeroSolicitud, socioId, monto}` |
| SolicitudEvaluada | Evaluación completada | `{evaluacionId, solicitudId, scoreInterno, elegible}` |
| SolicitudAprobada | Crédito aprobado | `{solicitudId, monto, tasaInteres}` |
| SolicitudRechazada | Crédito rechazado | `{solicitudId, motivo}` |
| CreditoDesembolsado | Fondos transferidos | `{solicitudId, referenciaDesembolso, monto}` |
| CuotaPagada | Pago registrado | `{amortizacionId, cuota, monto, saldoPendiente}` |
| CuotaVencida | Cuota no pagada a tiempo | `{amortizacionId, diasMora}` |
| ColateralEjecutada | Colateral transferido | `{solicitudId, montoEjecutado, cuotaId}` |

---

## Métricas y Monitoreo

| Métrica | Descripción | Umbral Alert |
|---------|-------------|-------------|
| solicitudes_por_dia | Volumen de solicitudes | > 1000/min |
| tiempo_evaluacion_p95 | Latencia evaluación | > 5s |
| tasa_aprobacion | % de solicitudes aprobadas | < 30% o > 90% |
| mora_30_dias | Cuotas en mora > 30 días | > 5% |
| tempo_desembolso_p95 | Tiempo de desembolso | > 24h |

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0.0 | 2026-04-14 | @documentador | Creación inicial basada en spec PM |
| 1.0.1 | 2026-04-14 | @documentador | Integración correcciones auditoría seguridad |
| 1.0.2 | 2026-04-14 | @documentador | Agregados campos de auditoría criptográfica en EvaluacionCrediticia |

---

## Referencias

- Documento de Arquitectura General: `/docs/arquitectura/ARQUITECTURA.md`
- Especificación API: `/docs/modulos/creditos/API.md`
- Modelo de Datos: `/docs/modulos/creditos/MODELO_DATOS.md`
- Auditoría de Seguridad: `/docs/auditorias/ULTIMA_AUDITORIA.md`
- Sistema de Pagos: `/docs/modulos/creditos/PAGOS.md`