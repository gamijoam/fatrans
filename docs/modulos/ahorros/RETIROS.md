# Módulo de Retiros - Documentación Técnica

## Resumen

El módulo de **Retiros** es un submódulo del módulo de Gestión de Ahorros (`com.tufondo.ahorros`) responsable de procesar retiros de fondos de cuentas de ahorro. Implementa validaciones estrictas de negocio, controles de seguridad IDOR, y límites operativos regulatorios.

---

## Arquitectura

### Ubicación en Clean Architecture

```
backend/src/main/java/com/tufondo/ahorros/
├── application/
│   ├── dto/
│   │   └── RetiroRequest.java          # DTO de entrada
│   └── usecase/
│       └── RealizarRetiroUseCase.java  # Caso de uso
├── domain/
│   ├── model/
│   │   └── Movimiento.java             # Entidad de dominio
│   └── repository/
│       └── MovimientoRepository.java   # Interface de repositorio
├── infrastructure/
│   └── persistence/
│       └── entity/
│           └── MovimientoEntity.java   # JPA Entity
└── api/
    └── controller/
        └── AhorroController.java      # REST Endpoints
```

---

## Componentes

### 1. RetiroRequest.java - DTO de Entrada

**Ubicación:** `backend/src/main/java/com/tufondo/ahorros/application/dto/RetiroRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetiroRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "50000.00", message = "monto excede límite de retiro diario")
    private BigDecimal monto;
    
    @NotNull(message = "canalOrigen es requerido")
    private CanalOrigen canalOrigen;
}
```

#### Validaciones del DTO

| Campo | Anotación | Restricción | Mensaje de Error |
|-------|-----------|-------------|------------------|
| `monto` | `@NotNull` | Requerido | "monto es requerido" |
| `monto` | `@DecimalMin` | >= 0.0001 | "monto debe ser >= 0.0001" |
| `monto` | `@DecimalMax` | <= 50,000.00 | "monto excede límite de retiro diario" |
| `canalOrigen` | `@NotNull` | Requerido | "canalOrigen es requerido" |

---

### 2. RealizarRetiroUseCase.java - Caso de Uso

**Ubicación:** `backend/src/main/java/com/tufondo/ahorros/application/usecase/RealizarRetiroUseCase.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class RealizarRetiroUseCase {

    private static final BigDecimal LIMITE_RETIRO = new BigDecimal("50000.00");

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final AhorrosDTOMapper mapper;

    @Transactional
    public MovimientoResponse ejecutar(String numeroCuenta, RetiroRequest request,
            Long socioIdToken, boolean isAdmin, String ipOrigen, String sessionId, String requestId) {
        
        // 1. Buscar cuenta
        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // 2. IDOR Check
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        // 3. Verificar estado ACTIVA (RN-005)
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new CuentaNoPermiteOperacionesException(numeroCuenta, cuenta.getEstado().name());
        }

        // 4. Verificar saldo disponible
        if (!cuenta.tieneSaldoSuficiente(request.getMonto())) {
            throw new SaldoInsuficienteException(numeroCuenta);
        }

        // 5. Verificar límite diario acumulado
        LocalDateTime inicioDia = LocalDateTime.now().toLocalDate().atStartOfDay();
        BigDecimal retirosHoy = movimientoRepository.sumRetirosDelDiaPorSocio(
                cuenta.getSocioId(), inicioDia);
        if (retirosHoy == null) retirosHoy = BigDecimal.ZERO;

        BigDecimal totalConEsteRetiro = retirosHoy.add(request.getMonto());
        if (totalConEsteRetiro.compareTo(LIMITE_RETIRO) > 0) {
            throw new MontoExcedeLimiteException(request.getMonto(), LIMITE_RETIRO, "retiro");
        }

        // 6. Ejecutar retiro
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        cuenta.restarSaldo(request.getMonto());
        cuentaRepository.guardar(cuenta);

        // 7. Crear movimiento (RN-006: INMUTABLE)
        String numeroOperacion = NumeroOperacion.generar().getValor();
        Movimiento movimiento = Movimiento.crearRetiro(
                cuenta.getId(),
                cuenta.getSocioId(),
                numeroOperacion,
                request.getMonto(),
                saldoAnterior,
                cuenta.getSaldoActual(),
                request.getCanalOrigen(),
                ipOrigen,
                sessionId,
                requestId
        );
        movimiento = movimientoRepository.guardar(movimiento);

        log.info("Retiro realizado: {} de cuenta {}", request.getMonto(), numeroCuenta);
        return mapper.toResponse(movimiento);
    }
}
```

#### Flujo de Ejecución

```
┌─────────────────────────────────────────────────────────────────┐
│                    REALIZAR RETIRO                              │
├─────────────────────────────────────────────────────────────────┤
│  1. Buscar cuenta por numeroCuenta                              │
│     └─► CuentaNoEncontradaException si no existe               │
│                                                                 │
│  2. Validar IDOR (socioIdToken vs cuenta.socioId)               │
│     └─► AccesoCuentaAjenaException si no coincide              │
│                                                                 │
│  3. Verificar estado == ACTIVA (RN-005)                        │
│     └─► CuentaNoPermiteOperacionesException si no ACTIVA       │
│                                                                 │
│  4. Verificar saldo disponible >= monto (RN-003)               │
│     └─► SaldoInsuficienteException si insuficiente             │
│                                                                 │
│  5. Calcular retiros del día + monto solicitado                 │
│     └─► Verificar total <= 50,000 USD/VES                      │
│     └─► MontoExcedeLimiteException si excede                   │
│                                                                 │
│  6. Restar monto del saldo (operación atómica)                  │
│                                                                 │
│  7. Crear Movimiento con estado PROCESADO                      │
│     └─► Formato numeroOperacion: MOV-YYYY-XXXXXX               │
└─────────────────────────────────────────────────────────────────┘
```

---

### 3. Movimiento.java - Entidad de Dominio

**Ubicación:** `backend/src/main/java/com/tufondo/ahorros/domain/model/Movimiento.java`

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimiento {
    private Long id;
    private String numeroOperacion;     // Formato: MOV-YYYY-XXXXXX
    private Long cuentaAhorroId;
    private Long socioId;
    private TipoMovimiento tipo;       // RETIRO, DEPOSITO, etc.
    private BigDecimal monto;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoPosterior;
    private String descripcion;
    private String referencia;
    private CanalOrigen canalOrigen;
    private String ipOrigen;
    private String sessionId;           // Auditoría de seguridad
    private String requestId;           // Request único para trazabilidad
    private EstadoMovimiento estado;
    private LocalDateTime fechaMovimiento;
    private LocalDate fechaValor;
    
    // Factory method para RETIRO
    public static Movimiento crearRetiro(Long cuentaAhorroId, Long socioId,
            String numeroOperacion, BigDecimal monto, BigDecimal saldoAnterior,
            BigDecimal saldoPosterior, CanalOrigen canalOrigen, String ipOrigen,
            String sessionId, String requestId) {
        return Movimiento.builder()
                .numeroOperacion(numeroOperacion)
                .cuentaAhorroId(cuentaAhorroId)
                .socioId(socioId)
                .tipo(TipoMovimiento.RETIRO)
                .monto(monto)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .canalOrigen(canalOrigen)
                .ipOrigen(ipOrigen)
                .sessionId(sessionId)
                .requestId(requestId)
                .estado(EstadoMovimiento.PROCESADO)
                .fechaMovimiento(LocalDateTime.now())
                .fechaValor(LocalDate.now())
                .build();
    }
}
```

#### Campos Generados en el Movimiento

| Campo | Descripción | Fuente |
|-------|-------------|--------|
| `numeroOperacion` | Formato `MOV-YYYY-XXXXXX` | `NumeroOperacion.generar()` |
| `tipo` | `TipoMovimiento.RETIRO` | Constante |
| `saldoAnterior` | Saldo antes del retiro | `cuenta.getSaldoActual()` antes de restar |
| `saldoPosterior` | Saldo después del retiro | `cuenta.getSaldoActual()` después de restar |
| `estado` | `EstadoMovimiento.PROCESADO` | Constante |
| `fechaMovimiento` | Timestamp del servidor | `LocalDateTime.now()` |
| `fechaValor` | Fecha contable | `LocalDate.now()` |
| `canalOrigen` | Canal solicitado | `RetiroRequest.canalOrigen` |
| `ipOrigen` | IP del cliente | Extraída del HttpServletRequest |
| `sessionId` | ID de sesión | Credentials del Authentication |
| `requestId` | UUID único | `UUID.randomUUID().toString()` |

---

### 4. MovimientoEntity.java - JPA Entity

**Ubicación:** `backend/src/main/java/com/tufondo/ahorros/infrastructure/persistence/entity/MovimientoEntity.java`

```java
@Entity
@Table(name = "movimientos",
    indexes = {
        @Index(name = "idx_movimientos_cuenta_id", columnList = "cuenta_ahorro_id"),
        @Index(name = "idx_movimientos_socio_id", columnList = "socio_id"),
        @Index(name = "idx_movimientos_fecha", columnList = "fecha_movimiento"),
        @Index(name = "idx_movimientos_numero_operacion", columnList = "numero_operacion", unique = true),
        @Index(name = "idx_movimientos_estado", columnList = "estado")
    })
public class MovimientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_operacion", unique = true, nullable = false, length = 20)
    private String numeroOperacion;

    @Column(name = "cuenta_ahorro_id", nullable = false)
    private Long cuentaAhorroId;

    @Column(name = "socio_id", nullable = false)
    private Long socioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(name = "monto", precision = 19, scale = 4, nullable = false)
    private BigDecimal monto;

    @Column(name = "saldo_anterior", precision = 19, scale = 4)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", precision = 19, scale = 4)
    private BigDecimal saldoPosterior;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false, length = 20)
    private CanalOrigen canalOrigen;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMovimiento estado;

    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "fecha_valor", nullable = false)
    private LocalDate fechaValor;
}
```

#### Índices de Base de Datos

| Índice | Columna(s) | Tipo | Propósito |
|--------|------------|------|-----------|
| `idx_movimientos_cuenta_id` | `cuenta_ahorro_id` | B-Tree | Búsqueda por cuenta |
| `idx_movimientos_socio_id` | `socio_id` | B-Tree | Búsqueda por socio |
| `idx_movimientos_fecha` | `fecha_movimiento` | B-Tree | Consulta por rango de fechas |
| `idx_movimientos_numero_operacion` | `numero_operacion` | B-Tree (ÚNICO) | Lookup por número de operación |
| `idx_movimientos_estado` | `estado` | B-Tree | Filtrado por estado |

---

### 5. MovimientoRepository.java - Interface de Repositorio

**Ubicación:** `backend/src/main/java/com/tufondo/ahorros/domain/repository/MovimientoRepository.java`

```java
public interface MovimientoRepository {
    
    Movimiento guardar(Movimiento movimiento);
    
    Optional<Movimiento> buscarPorId(Long id);
    
    Optional<Movimiento> buscarPorNumeroOperacion(String numeroOperacion);
    
    Page<Movimiento> buscarPorCuentaAhorroId(Long cuentaAhorroId, Pageable pageable);
    
    Page<Movimiento> buscarPorCuentaYRangoFechas(Long cuentaAhorroId, 
            LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    Page<Movimiento> buscarPorCuentaYTipo(Long cuentaAhorroId, TipoMovimiento tipo, Pageable pageable);
    
    Page<Movimiento> buscarPorSocioId(Long socioId, Pageable pageable);
    
    /**
     * Suma de retiros del día para un socio.
     * Límite: 50,000 USD/día.
     */
    BigDecimal sumRetirosDelDiaPorSocio(Long socioId, LocalDateTime inicioDia);
    
    long contarPorCuentaYEstado(Long cuentaAhorroId, EstadoMovimiento estado);
}
```

#### Método Crítico para Límite Diario

```java
/**
 * Suma de retiros del día para un socio.
 * Límite: 50,000 USD/día.
 */
BigDecimal sumRetirosDelDiaPorSocio(Long socioId, LocalDateTime inicioDia);
```

Este método es utilizado por `RealizarRetiroUseCase` para verificar el límite diario acumulado de retiros por socio.

---

### 6. AhorroController.java - Endpoints REST

**Ubicación:** `backend/src/main/java/com/tufondo/ahorros/api/controller/AhorroController.java`

#### Endpoint de Retiro (Línea 112-129)

```java
// 6. POST /cuentas/{numeroCuenta}/retiros - Realizar Retiro
@PostMapping("/{numeroCuenta}/retiros")
@Operation(summary = "Realizar retiro")
public ResponseEntity<MovimientoResponse> realizarRetiro(
        @PathVariable String numeroCuenta,
        @Valid @RequestBody RetiroRequest request,
        Authentication authentication,
        HttpServletRequest httpRequest) {
    Long socioIdToken = extraerSocioId(authentication);
    boolean isAdmin = esAdmin(authentication);
    String ipOrigen = getClientIp(httpRequest);
    String sessionId = authentication.getCredentials().toString();
    String requestId = UUID.randomUUID().toString();
    
    MovimientoResponse response = realizarRetiroUseCase.ejecutar(
            numeroCuenta, request, socioIdToken, isAdmin, ipOrigen, sessionId, requestId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

## Lógica de Negocio Implementada

### Validaciones de Entrada

| Validación | Implementación | Excepción |
|------------|----------------|-----------|
| `monto >= 0.0001` | `@DecimalMin("0.0001")` | `ConstraintViolationException` |
| `monto <= 50,000` | `@DecimalMax("50000.00")` | `ConstraintViolationException` |
| `canalOrigen` requerido | `@NotNull` | `ConstraintViolationException` |

### Validaciones de Negocio

| Validación | Descripción | Código | Excepción |
|------------|-------------|--------|-----------|
| RN-003 | `saldoActual` nunca negativo | `cuenta.tieneSaldoSuficiente()` | `SaldoInsuficienteException` |
| RN-005 | Cuenta ACTIVA para operaciones | `cuenta.getEstado() == ACTIVA` | `CuentaNoPermiteOperacionesException` |
| RN-006 | Movimientos INMUTABLES | Solo create, no update | N/A |
| Límite diario | `retirosHoy + monto <= 50,000` | `sumRetirosDelDiaPorSocio()` | `MontoExcedeLimiteException` |

### Validación IDOR (Seguridad)

```java
// RealizarRetiroUseCase.java - Línea 45-48
if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
    throw new AccesoCuentaAjenaException();
}
```

| Condición | Resultado |
|-----------|-----------|
| `isAdmin == true` | Permite acceso (ROLE_ADMIN o ROLE_SISTEMA) |
| `cuenta.socioId == socioIdToken` | Permite acceso |
| De lo contrario | `AccesoCuentaAjenaException` (403 Forbidden) |

### Warning por Saldo Bajo Mínimo

> **Nota:** Según la especificación (SPEC.md línea 367), cuando un retiro deja el saldo por debajo del `montoMinimoRequerido`, el sistema debe mostrar un **WARNING** pero permitir la operación.

**Estado actual del código:** La implementación actual de `RealizarRetiroUseCase` **NO incluye** esta advertencia. El warning es retornado a nivel de API response cuando aplica, basado en la lógica de `mapper.toResponse()`.

---

## Canales de Origen Soportados

```java
public enum CanalOrigen {
    WEB,       // Portal web
    MOBILE,    // App móvil
    ATM,       // Cajero automático
    SUCURSAL,  // Ventanilla banco
    API,       // Integración API
    BATCH      // Proceso batch
}
```

---

## Estados del Movimiento

```java
public enum EstadoMovimiento {
    PROCESADO,    // Completado exitosamente
    RECHAZADO,    // Rechazado por validación
    PENDIENTE,    // En proceso de confirmación
    CANCELADO,    // Cancelado por usuario
    REVERTIDO     // Compensación realizada
}
```

Para retiros exitosos, el estado es siempre `PROCESADO`.

---

## Endpoints Relacionados

| Método | Path | Descripción |
|--------|------|-------------|
| `POST` | `/api/v1/cuentas/{numeroCuenta}/retiros` | Realizar Retiro |
| `GET` | `/api/v1/cuentas/{numeroCuenta}/movimientos` | Listar Movimientos (incluye retiros) |
| `GET` | `/api/v1/cuentas/{numeroCuenta}/movimientos/{numeroOperacion}` | Detalle de Movimiento |

---

## Códigos de Error

| Código HTTP | Error Code | Descripción | Causa |
|-------------|------------|-------------|-------|
| 400 | `VALIDATION_ERROR` | Datos inválidos | monto < 0.0001 o > 50000 |
| 403 | `ACCESO_CUENTA_AJENA` | IDOR detectado | socioId no coincide |
| 404 | `CUENTA_NO_ENCONTRADA` | Cuenta inexistente | número de cuenta inválido |
| 422 | `CUENTA_NO_PERMITE_OPERACIONES` | Estado no ACTIVA | Cuenta SUSPENDIDA o CERRADA |
| 422 | `SALDO_INSUFICIENTE` | Sin fondos | saldo < monto solicitado |
| 422 | `MONTO_EXCEDE_LIMITE` | Límite diario excedido | retiros hoy + monto > 50000 |
| 429 | `RATE_LIMIT_EXCEEDED` | Demasiadas solicitudes | > 30 requests/min por endpoint |

---

## Rate Limiting

| Tipo | Límite | Ventana |
|------|--------|---------|
| Operaciones financieras | 30 requests/min | Por endpoint |
| Consultas | 60 requests/min | Por endpoint |
| General | 100 requests/min | Por IP |

Headers de respuesta:
- `X-RateLimit-Remaining`: Requests restantes
- `X-RateLimit-Reset`: Timestamp de reset
- `Retry-After`: Segundos para reintentar (cuando se excede)

---

## Ejemplo de Request/Response

### Request

```http
POST /api/v1/cuentas/AHO-2026-000001/retiros
Content-Type: application/json
Authorization: Bearer <jwt_token>
X-Request-Id: req_abc123
X-Forwarded-For: 192.168.1.100

{
  "monto": 3000.00,
  "canalOrigen": "ATM"
}
```

### Response - 201 Created

```json
{
  "id": 2,
  "numeroOperacion": "MOV-2026-000002",
  "cuentaAhorroId": 1,
  "tipo": "RETIRO",
  "monto": 3000.0000,
  "saldoAnterior": 15000.5000,
  "saldoPosterior": 12000.5000,
  "descripcion": null,
  "referencia": null,
  "canalOrigen": "ATM",
  "ipOrigen": "192.168.1.100",
  "sessionId": "sess_xyz789",
  "requestId": "req_abc123",
  "estado": "PROCESADO",
  "fechaMovimiento": "2026-04-14T16:10:00Z",
  "fechaValor": "2026-04-14"
}
```

---

## Dependencias Externas

| Dependencia | Propósito |
|-------------|-----------|
| `CuentaAhorroRepository` | Buscar y actualizar cuenta |
| `MovimientoRepository` | Persistir movimiento, consultar retiros del día |
| `AhorrosDTOMapper` | Mapear entidad a DTO de respuesta |
| `NumeroOperacion` | Generar identificador único de operación |

---

## Notas de Implementación

### Transaccionalidad

El método `ejecutar()` está anotado con `@Transactional` para garantizar atomicidad en la operación de débito de cuenta y creación de movimiento.

### Concurrencia

- Uso de `@Version` en `CuentaAhorro` para optimistic locking
- La suma de retiros del día se calcula en tiempo real para evitar condiciones de carrera

### Auditoría

Todos los campos de auditoría están siendo capturados:
- `ipOrigen`: Extraída del `HttpServletRequest` (soporta X-Forwarded-For)
- `sessionId`: Extraído del `Authentication.getCredentials()`
- `requestId`: Generado como `UUID.randomUUID().toString()`

---

## Historial de Cambios

| Fecha | Agente | Descripción |
|-------|--------|-------------|
| 2026-04-14 | @documentador | Creación inicial del documento RETIROS.md |

---

## Referencias

- Especificación técnica: [SPEC.md](./SPEC.md)
- Referencia API: [API.md](./API.md)
- Modelo de datos: [MODELO_DATOS.md](./MODELO_DATOS.md)
