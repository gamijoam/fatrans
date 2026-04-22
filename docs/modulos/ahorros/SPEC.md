# Módulo de Gestión de Ahorros - Especificación Técnica

## Resumen

El módulo de **Gestión de Ahorros** es el componente central del sistema Fondo de Ahorro, responsable de la administración de cuentas de ahorro, movimientos financieros y cálculo de rendimientos. Implementa el dominio bancario con strict compliance a regulaciones financieras mexicanas.

---

## Arquitectura del Módulo

### Estructura de Capas (Clean Architecture)

```
src/main/java/com/fondoahorro/
├── domain/
│   ├── model/          # Entidades del dominio
│   ├── repository/     # Interfaces de repositorio
│   └── service/        # Lógica de dominio pura
├── application/
│   ├── usecase/        # Casos de uso orchestration
│   ├── dto/            # Data Transfer Objects
│   └── mapper/         # Mapeadores Entity<->DTO
├── infrastructure/
│   ├── persistence/     # Implementaciones JPA
│   ├── messaging/      # Eventos de dominio
│   └── security/       # Filtros, interceptores
└── api/
    ├── controller/     # REST Controllers
    └── validator/       # Validaciones custom
```

### Componentes Principales

| Componente | Responsabilidad | Ubicación |
|------------|-----------------|-----------|
| `CuentaAhorro` | Entidad aggregate root | domain/model |
| `Movimiento` | Entidad hija de CuentaAhorro | domain/model |
| `Rendimiento` | Entidad para cálculo de intereses | domain/model |
| `CuentaAhorroRepository` | Acceso a datos cuenta | domain/repository |
| `MovimientoRepository` | Acceso a datos movimiento | domain/repository |
| `GestionarAhorroUseCase` | Orquestación casos de uso | application/usecase |

---

## Entidades del Dominio

### CuentaAhorro

```java
@Entity
@Table(name = "cuentas_ahorro")
public class CuentaAhorro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_cuenta", unique = true, nullable = false)
    private String numeroCuenta;  // Formato: AHO-YYYY-XXXXXX
    
    @Column(name = "socio_id", nullable = false)
    private Long socioId;
    
    @Column(name = "saldo_actual", precision = 19, scale = 4)
    private BigDecimal saldoActual;  // Balance disponible
    
    @Column(name = "saldo_retenido", precision = 19, scale = 4)
    private BigDecimal saldoRetenido;  // Fondos en proceso (no disponibles)
    
    @Column(name = "tasa_interes", precision = 8, scale = 6)
    private BigDecimal tasaInteres;  // Tasa anual
    
    @Column(name = "monto_minimo_querido", precision = 19, scale = 4)
    private BigDecimal montoMinimoRequerido;  // Saldo mínimo de protección
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCuenta estado;  // ACTIVA, SUSPENDIDA, CERRADA
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta")
    private TipoCuenta tipoCuenta;  // AHORRO, NOMINA, PLAZO_FIJO
    
    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;
    
    @Column(name = "fecha_ultima_operacion")
    private LocalDateTime fechaUltimaOperacion;
    
    @Version
    private Long version;  // Optimistic locking
}
```

#### Enums

```java
public enum EstadoCuenta {
    ACTIVA,      // Operaciones permitidas
    SUSPENDIDA,  // Solo consultas, sin operaciones
    CERRADA      // Sin operaciones, historial accesible
}

public enum TipoCuenta {
    AHORRO,      // Cuenta estándar
    NOMINA,      // Cuenta de nómina del socio
    PLAZO_FIJO   // Depósito a plazo fijo
}
```

#### Reglas de Negocio - CuentaAhorro

| Regla | Descripción | Validación |
|-------|-------------|------------|
| RN-001 | Un socio solo puede tener una cuenta por tipo | Verificar en creación |
| RN-002 | Número de cuenta único y generado automáticamente | Formato AHO-YYYY-XXXXXX |
| RN-003 | `saldoActual` nunca puede ser negativo | Validación en cada operación |
| RN-004 | `montoMinimoRequerido` debe ser >= 0.0001 | Validación en creación |
| RN-005 | Cuenta CERRADA no permite operaciones | Verificar estado antes de ops |

#### Saldo Disponible

```
saldoDisponible = saldoActual - saldoRetenido
```

> **Nota de Auditoría:** El campo `saldoRetenido` representa fondos bloqueados por operaciones pendientes (e.g., retiros en proceso). Este campo NO es un "hold" bancario tradicional, sino un mecanismo de tracking de operaciones en curso.

---

### Movimiento

```java
@Entity
@Table(name = "movimientos")
public class Movimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_operacion", unique = true, nullable = false)
    private String numeroOperacion;  // Formato: MOV-YYYY-XXXXXX
    
    @Column(name = "cuenta_ahorro_id", nullable = false)
    private Long cuentaAhorroId;
    
    @Column(name = "socio_id", nullable = false)
    private Long socioId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMovimiento tipo;
    
    @Column(name = "monto", precision = 19, scale = 4, nullable = false)
    private BigDecimal monto;
    
    @Column(name = "saldo_anterior", precision = 19, scale = 4)
    private BigDecimal saldoAnterior;
    
    @Column(name = "saldo_posterior", precision = 19, scale = 4)
    private BigDecimal saldoPosterior;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "referencia")
    private String referencia;  // Referencia bancaria externa
    
    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen")
    private CanalOrigen canalOrigen;  // WEB, MOBILE, ATM, SUCURSAL
    
    @Column(name = "ip_origen")
    private String ipOrigen;
    
    @Column(name = "session_id")
    private String sessionId;  // Auditoría de seguridad
    
    @Column(name = "request_id")
    private String requestId;  // Request único para trazabilidad
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoMovimiento estado;
    
    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;  // Timestamp del servidor
    
    @Column(name = "fecha_valor")
    private LocalDateTime fechaValor;  // Fecha valor contable
}
```

#### Enums

```java
public enum TipoMovimiento {
    DEPOSITO,
    RETIRO,
    TRANSFERENCIA_ENTRADA,
    TRANSFERENCIA_SALIDA,
    COMISION,
    INTERES_CREDITO,
    INTERES_DEBITO,
    AJUSTE
}

public enum CanalOrigen {
    WEB,
    MOBILE,
    ATM,
    SUCURSAL,
    API,
    BATCH
}

public enum EstadoMovimiento {
    PROCESADO,       // Completado exitosamente
    RECHAZADO,       // Rechazado por validación
    PENDIENTE,       // En proceso de confirmación
    CANCELADO,       // Cancelado por usuario
    REVERTIDO        // Compensación realizada
}
```

#### Reglas de Negocio - Movimiento

| Regla | Descripción |
|-------|-------------|
| RN-006 | Movimientos son **INMUTABLES** una vez creados |
| RN-007 | `numeroOperacion` generado con formato MOV-YYYY-XXXXXX |
| RN-008 | `fechaMovimiento` = timestamp del servidor (no cliente) |
| RN-009 | `sessionId` y `requestId` requeridos para trazabilidad |

#### Campos de Auditoría

> **Corrección de Auditoría:** Se incluyen `sessionId` y `requestId` para trazabilidad completa de operaciones. Esto permite rastrear una operación desde la solicitud del cliente hasta el registro en base de datos.

---

### Rendimiento

```java
@Entity
@Table(name = "rendimientos")
public class Rendimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cuenta_ahorro_id", nullable = false)
    private Long cuentaAhorroId;
    
    @Column(name = "periodo_inicio")
    private LocalDate periodoInicio;
    
    @Column(name = "periodo_fin")
    private LocalDate periodoFin;
    
    @Column(name = "saldo_promedio_periodo", precision = 19, scale = 4)
    private BigDecimal saldoPromedioPeriodo;
    
    @Column(name = "tasa_aplicada", precision = 8, scale = 6, nullable = false)
    private BigDecimal tasaAplicada;
    
    @Column(name = "monto_rendimiento", precision = 19, scale = 4)
    private BigDecimal montoRendimiento;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoRendimiento tipo;  // DIARIO, MENSUAL, ANUAL
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aplicacion")
    private EstadoAplicacion estadoAplicacion;
    
    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;
}
```

#### Enums

```java
public enum TipoRendimiento {
    DIARIO,
    MENSUAL,
    ANUAL
}

public enum EstadoAplicacion {
    CALCULADO,       // Generado pero no aplicado
    APLICADO,        // Añadido al saldo
    CANCELADO        // Reversado
}
```

#### Reglas de Negocio - Rendimiento

| Regla | Descripción | Validación |
|-------|-------------|------------|
| RN-010 | `tasaAplicada` debe estar en rango válido (0.0001 - 1.0) | **CRÍTICO: Validación de overflow** |
| RN-011 | `saldoPromedioPeriodo` requiere al menos 30 días de historia | Verificar periodo mínimo |
| RN-012 | Un periodo no puede ser recalculado si ya fue aplicado | Estado aplicación |

---

## Casos de Uso

### Gestión de Cuentas

| Caso de Uso | Actor | Descripción |
|-------------|-------|-------------|
| CrearCuentaAhorro | Socio/Administrador | Registra nueva cuenta de ahorro |
| ConsultarCuenta | Socio/Administrador | Consulta datos de cuenta por número |
| ConsultarCuentasPorSocio | Socio/Administrador | Lista todas las cuentas de un socio |
| ConsultarSaldo | Socio | Consulta saldo disponible y detalle |
| CerrarCuenta | Administrador | Cierra cuenta (solo si saldo = 0) |

### Operaciones Financieras

| Caso de Uso | Actor | Descripción |
|-------------|-------|-------------|
| RealizarDeposito | Socio | Registra depósito en cuenta |
| RealizarRetiro | Socio | Registra retiro con validaciones |
| ConsultarMovimientos | Socio/Administrador | Lista movimientos de cuenta |
| ConsultarMovimientoDetalle | Socio/Administrador | Detalle de movimiento específico |

### Rendimientos

| Caso de Uso | Actor | Descripción |
|-------------|-------|-------------|
| CalcularRendimiento | Sistema/Administrador | Calcula rendimiento para cuenta |
| ConsultarRendimientos | Socio/Administrador | Lista rendimientos de cuenta |
| CalcularRendimientosBatch | Sistema | Procesa rendimientos de múltiples cuentas |

---

## Reglas de Negocio Detalladas

### Límites Operativos

| Operación | Límite | Notas |
|-----------|--------|-------|
| Depósito | 500,000.00 USD por operación | Límite regulatorio |
| Retiro | 50,000.00 USD por día | Límite diario acumulado |
| Monto mínimo | 0.0001 USD | Cualquier operación |

### Validaciones de Seguridad

| Validación | Descripción | severidad |
|------------|-------------|-----------|
| IDOR Check | Verificar que socioId en token coincide con cuenta | CRÍTICA |
| Rate Limiting | Máximo 100 requests/min por IP, 30/min por endpoint | ALTA |
| IP whitelist | APIs administrativas con IPs restringidas | MEDIA |

### Estados de Cuenta y Operaciones Permitidas

| Estado | Consultas | Depósitos | Retiros | Rendimientos |
|--------|-----------|----------|---------|--------------|
| ACTIVA | ✅ | ✅ | ✅ | ✅ |
| SUSPENDIDA | ✅ | ❌ | ❌ | ❌ |
| CERRADA | ✅ | ❌ | ❌ | ❌ |

> **Warning de Negocio:** Al intentar retiro que deje saldo < montoMinimoRequerido, el sistema debe mostrar **WARNING** pero permitir la operación si el socio lo confirma.

---

## Validaciones Implementadas (DTOs)

### CreateCuentaAhorroRequest

```java
public class CreateCuentaAhorroRequest {
    
    @NotNull(message = "socioId es requerido")
    private Long socioId;
    
    @NotNull(message = "tipoCuenta es requerido")
    private TipoCuenta tipoCuenta;
    
    @DecimalMin(value = "0.0001", message = "montoMinimoRequerido debe ser >= 0.0001")
    @DecimalMax(value = "999999999.9999", message = "montoMinimoRequerido excede límite máximo")
    private BigDecimal montoMinimoRequerido;
    
    @DecimalMin(value = "0.0", message = "tasaInteres no puede ser negativa")
    @DecimalMax(value = "1.0", message = "tasaInteres no puede exceder 100%")
    private BigDecimal tasaInteres;
}
```

### DepositoRequest

```java
public class DepositoRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "500000.00", message = "monto excede límite de depósito (500,000 USD)")
    private BigDecimal monto;
    
    @Size(max = 500, message = "descripcion no puede exceder 500 caracteres")
    private String descripcion;
    
    @Size(max = 100, message = "referencia no puede exceder 100 caracteres")
    private String referencia;
    
    @NotNull(message = "canalOrigen es requerido")
    private CanalOrigen canalOrigen;
}
```

### RetiroRequest

```java
public class RetiroRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "50000.00", message = "monto excede límite de retiro (50,000 USD)")
    private BigDecimal monto;
    
    @NotBlank(message = "canalOrigen es requerido")
    private CanalOrigen canalOrigen;
}
```

---

## Eventos de Dominio

| Evento | Descripción | Payload |
|--------|-------------|---------|
| CuentaCreada | Nueva cuenta registrada | `{cuentaId, numeroCuenta, socioId, tipoCuenta}` |
| DepositoRealizado | Depósito completado | `{movimientoId, cuentaId, monto, saldoPosterior}` |
| RetiroRealizado | Retiro completado | `{movimientoId, cuentaId, monto, saldoPosterior}` |
| RendimientoCalculado | Rendimiento generado | `{rendimientoId, cuentaId, monto, periodo}` |
| CuentaCerrada | Cuenta cerrada | `{cuentaId, numeroCuenta, saldoFinal}` |

---

## Métricas y Monitoreo

| Métrica | Descripción | Umbral |
|---------|-------------|--------|
| operaciones_por_minuto | Throughput del módulo | < 1000 ops/min |
| tiempo_respuesta_p95 | Latencia P95 | < 500ms |
| errores_validacion | Validaciones fallidas | Alertar si > 1% |
| intentos_retiro_rechazados | Intentos de retiro rechazados | Alertar si > 10/min |

---

## Notas de Implementación

### Concurrencia y Bloqueo

- **Optimistic Locking:** Uso de `@Version` en `CuentaAhorro` para prevenir condiciones de carrera
- **Transacciones:** Todos los movimientos de dinero envueltos en `@Transactional`
- **Saga Pattern:** Para operaciones batch, implementar compensación ante fallos

### Seguridad

- **Token JWT:** Incluir `socioId` en claims para verificación de acceso
- **Rate Limiting:** Implementar por IP + por cuenta + por endpoint
- **Logging:** Registrar todos los intentos de acceso a cuentas

### Rendimiento

- **Índices:** `numero_cuenta`, `socio_id`, `fecha_movimiento` en tablas principales
- **Paginación:** Todos los endpoints de listado con cursor-based pagination
- **Caché:** Saldos calculados con TTL de 30 segundos

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0.0 | 2026-04-14 | @documentador | Creación inicial basada en spec PM |
| 1.0.1 | 2026-04-14 | @documentador | Integración hallazgos auditoría seguridad |

---

## Referencias

- Documento de Arquitectura General: `/docs/arquitectura/ARQUITECTURA.md`
- Especificación API: `/docs/modulos/ahorros/API.md`
- Modelo de Datos: `/docs/modulos/ahorros/MODELO_DATOS.md`
- Auditoría de Seguridad: `/docs/auditorias/seguridad_YYYYMMDD.md`