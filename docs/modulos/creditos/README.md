# Módulo de Gestión de Créditos

## Resumen

El **Módulo de Créditos** es el componente central del sistema Fondo de Ahorro para la administración completa del ciclo de vida de productos de crédito: desde la solicitud inicial hasta el desembolso y cobro de cuotas. Implementa el dominio financiero siguiendo Clean Architecture + DDD.

**Ubicación:** `/backend/src/main/java/com/tufondo/creditos/`

---

## Funcionalidades Principales

El módulo proporciona las siguientes capacidades:

1. **Gestión de Solicitudes de Crédito**
   - Crear solicitudes de crédito con monto, plazo y destino
   - Consultar estado de solicitudes por número o por socio
   - Validación de elegibilidad del socio (sin créditos activos)

2. **Catálogo de Tipos de Crédito**
   - Listar tipos de crédito disponibles (vehículo, micro crédito, etc.)
   - Consultar detalle de tipos de crédito con tasas y límites

3. **Evaluación Crediticia**
   - Calcular score interno basado en antigüedad, historial de ahorro y capacidad de pago
   - Generar hash SHA-256 y firma RSA para verificación de integridad del score
   - Determinar elegibilidad y nivel de riesgo (BAJO, MEDIO, ALTO)

4. **Aprobación y Rechazo**
   - Aprobar solicitudes con retención de colateral si aplica
   - Rechazar solicitudes con motivo documentado
   - Aplicar tasas de interés según score (descuentos o recargos)

5. **Generación de Plan de Amortización**
   - Sistema francés de cuota fija mensual
   - Cálculo de capital, intereses y saldo insoluto por cuota
   - Consulta del plan completo con todas las cuotas

6. **Desembolso de Créditos**
   - Ejecutar transferencia de fondos al socio
   - Aplicar comisión de apertura
   - Actualizar estado a DESEMBOLSADO

7. **Gestión de Pagos de Cuotas**
   - Listar cuotas con paginación y filtros por estado
   - Registrar pagos con validación de idempotencia (referencia_pago única)
   - Prevención de double-payment con optimistic locking (@Version)
   - Cálculo automático de intereses moratorios

8. **Simulación de Créditos**
   - Calcular план de pagos sin crear solicitud
   - Rate limiting: máximo 10 solicitudes por minuto por IP

9. **Ejecución de Colateral**
   - Detectar cuotas en mora > 30 días
   - Transferir fondos de cuenta colateral si morosidad > 90 días
   - Estados: CURSO_MORA → EJECUTADA

---

## Casos de Uso

### Para SOCIOS

| Caso de Uso | Descripción | Endpoint |
|-------------|-------------|----------|
| Crear Solicitud | Solicitar un crédito указание monto, plazo y destino | `POST /creditos/solicitudes` |
| Consultar Mi Solicitud | Ver estado de una solicitud propia | `GET /creditos/solicitudes/{numero}` |
| Listar Mis Solicitudes | Ver todas mis solicitudes | `GET /creditos/solicitudes/socio/{socioId}` |
| Ver Mi Plan | Consultar план de amortización propio | `GET /creditos/solicitudes/{numero}/plan` |
| Ver Mis Cuotas | Listar cuotas pendientes de mis créditos | `GET /creditos/{numero}/cuotas` |
| Pagar Mi Cuota | Registrar pago de una cuota | `POST /creditos/cuotas/{cuotaId}/pago` |
| Consultar Mi Crédito | Ver estado completo de un crédito | `GET /creditos/{numero}` |
| Simular Crédito | Calcular план sin crear solicitud | `POST /simulador` |

### Para ADMIN

| Caso de Uso | Descripción | Endpoint |
|-------------|-------------|----------|
| Evaluar Solicitud | Calcular score y elegibilidad | `POST /creditos/solicitudes/{numero}/evaluar` |
| Aprobar Crédito | Aprobar con retención de colateral | `POST /creditos/solicitudes/{numero}/aprobar` |
| Rechazar Crédito | Rechazar con motivo | `POST /creditos/solicitudes/{numero}/rechazar` |
| Desembolsar | Ejecutar transferencia al socio | `POST /creditos/solicitudes/{numero}/desembolson` |
| Consultar Cualquier Crédito | Ver cualquier solicitud o crédito | `GET /creditos/**` |

### Para CAJERO

| Caso de Uso | Descripción | Endpoint |
|-------------|-------------|----------|
| Registrar Pago | Registrar pago de cuota de cualquier socio | `POST /creditos/cuotas/{cuotaId}/pago` |

### Para SISTEMA

| Caso de Uso | Descripción | Endpoint |
|-------------|-------------|----------|
| Evaluar Auto | Evaluación automática de solicitudes | `POST /creditos/solicitudes/{numero}/evaluar` |
| Desembolsar Auto | Desembolso automático | `POST /creditos/solicitudes/{numero}/desembolson` |

---

## Entidades Principales

### 1. TipoCredito

Define los productos de crédito disponibles con sus características.

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| id | Long | Identificador único |
| codigo | String | Código único (ej: "CRED_VEHICULO") |
| nombre | String | Nombre comercial |
| tasaInteresAnual | BigDecimal | Tasa anual (0.145 = 14.5%) |
| plazoMinimoMeses | Integer | Plazo mínimo |
| plazoMaximoMeses | Integer | Plazo máximo |
| montoMinimo | BigDecimal | Monto mínimo |
| montoMaximo | BigDecimal | Monto máximo |
| porcentajeRequerimientoColateral | BigDecimal | % colateral requerido |
| comisionApertura | BigDecimal | Comisión (%) |
| penalidadMoraTasa | BigDecimal | Tasa diaria mora |
| diasGracia | Integer | Días antes de marcar vencida |
| activo | Boolean | Si está disponible |

**Relación:** 1 TipoCredito → N SolicitudCredito

---

### 2. SolicitudCredito

Representa la solicitud de crédito de un socio.

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| id | UUID | Identificador único |
| numeroSolicitud | String | Número público (SOL-CRED-2026-XXXXXX) |
| socioId | Long | ID del socio solicitante |
| tipoCreditoId | Long | FK a TipoCredito |
| montoSolicitado | BigDecimal | Monto solicitado |
| plazoMeses | Integer | Plazo en meses |
| tasaInteresAplicada | BigDecimal | Tasa final aplicada |
| cuotaMensualEstimada | BigDecimal | Couta estimada |
| estado | EstadoSolicitud | Estado actual |
| colateralCuentaId | UUID | FK a cuenta de ahorro (nullable) |
| colateralMontoRetenido | BigDecimal | Monto retenido como colateral |
| evaluacionId | UUID | FK a EvaluacionCrediticia |
| planAmortizacionId | UUID | FK a PlanAmortizacion |
| cuentaDestino | String | IBAN para desembolso |

**Relación:** N SolicitudCredito → 1 TipoCredito, 1 EvaluacionCrediticia, 1 PlanAmortizacion

---

### 3. EvaluacionCrediticia

Almacena el resultado de la evaluación crediticia.

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| id | UUID | Identificador único |
| solicitudId | UUID | FK a SolicitudCredito (unique) |
| socioId | Long | ID del socio |
| puntajeAntiguedad | Integer | Puntos antigüedad (0-30) |
| puntajeHistorialAhorro | Integer | Puntos historial (0-30) |
| puntajeCapacidadPago | Integer | Puntos capacidad (0-40) |
| scoreInterno | Integer | Score total (0-100) |
| scoreHash | String | SHA-256 del cálculo (auditoría) |
| factoresSerializados | String | JSON con breakdown |
| firmaVerificable | String | Firma RSA (auditoría) |
| elegible | Boolean | Si es elegible |
| nivelRiesgo | String | BAJO, MEDIO, ALTO |
| tasaInteresFinal | BigDecimal | Tasa final ajustada |
| evaluador | String | Admin o "SISTEMA" |

**Relación:** 1 EvaluacionCrediticia → 1 SolicitudCredito

---

### 4. PlanAmortizacion

Calendario de pagos del crédito.

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| id | UUID | Identificador único |
| solicitudId | UUID | FK a SolicitudCredito (unique) |
| montoPrincipal | BigDecimal | Monto del crédito |
| tasaInteres | BigDecimal | Tasa anual |
| plazoMeses | Integer | Número de cuotas |
| frecuenciaPago | String | MENSUAL, QUINCENAL, SEMANAL |
| fechaInicio | LocalDate | Fecha primera cuota |
| fechaFin | LocalDate | Fecha última cuota |
| totalIntereses | BigDecimal | Total intereses |
| totalPagado | BigDecimal | Total pagado |
| saldoPendiente | BigDecimal | Saldo insoluto |
| numeroCuotas | Integer | Cantidad de cuotas |
| cuotaMensual | BigDecimal | Monto de cuota fija |
| estado | EstadoPlan | ACTIVO, CANCELADO, FINALIZADO, VENCIDO |

**Relación:** 1 PlanAmortizacion → 1 SolicitudCredito, 1:N Amortizacion

---

### 5. Amortizacion (Cuota)

Cada cuota individual del plan de amortización.

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| id | UUID | Identificador único |
| planId | UUID | FK a PlanAmortizacion |
| numeroCuota | Integer | Número de cuota |
| fechaVencimiento | LocalDate | Fecha de vencimiento |
| fechaPago | LocalDate | Fecha de pago (nullable) |
| capital | BigDecimal | Parte capital de la cuota |
| interes | BigDecimal | Parte interés de la cuota |
| montoCuota | BigDecimal | Total cuota (capital + interes) |
| saldoInsoluto | BigDecimal | Saldo después de pagar |
| estado | EstadoAmortizacion | PENDIENTE, PAGADA, VENCIDA, etc. |
| diasMora | Integer | Días en mora |
| interesMora | BigDecimal | Intereses moratorios |
| montoPagado | BigDecimal | Monto pagado |
| referenciaPago | String | Clave de idempotencia (unique) |
| colateralEjecutada | Boolean | Si se ejecutó colateral |

**Relación:** N Amortizacion → 1 PlanAmortizacion

---

## Estados

### Estados de Solicitud (EstadoSolicitud)

```
PENDIENTE ──► EN_EVALUACION ──► APROBADA ──► DESEMBOLSADO ──► COLATERAL_EJECUTADO
                      │
                      └──► RECHAZADA

CANCELADA (desde cualquier estado no final)
```

| Estado | Descripción |
|--------|-------------|
| PENDIENTE | Solicitud creada, esperando evaluación |
| EN_EVALUACION | En proceso de evaluación crediticia |
| APROBADA | Aprobada, esperando desembolso |
| RECHAZADA | Rechazada (estado final) |
| CANCELADA | Cancelada por el socio (estado final) |
| DESEMBOLSADO | Fondos transferidos, crédito activo |
| COLATERAL_EJECUTADO | Colateral ejecutado por incumplimiento (estado final) |

---

### Estados de Amortización (EstadoAmortizacion)

```
PENDIENTE ──► PAGADA (pago normal)
     │
     ├──► VENCIDA (pasó vencimiento + grace period)
     │         │
     │         └──► CURSO_MORA (> 30 días)
     │                   │
     │                   └──► EJECUTADA (colateral)
     │
     └──► CANCELADA (prepago)
```

| Estado | Descripción |
|--------|-------------|
| PENDIENTE | Cuota vigente, esperando pago |
| PAGADA | Pagada exitosamente |
| VENCIDA | Pasó fecha de vencimiento |
| CURSO_MORA | Más de 30 días en mora |
| CANCELADA | Cancelada por prepago |
| EJECUTADA | Colateral ejecutada |

---

## Reglas de Negocio

### Elegibilidad (RN-E)

| ID | Regla | Validación |
|----|-------|------------|
| RN-E-01 | Score interno >= 50 | `evaluacion.scoreInterno >= 50` |
| RN-E-02 | Score < 70 requiere colateral | Si score < 70, `colateralCuentaId` obligatorio |
| RN-E-03 | Capacidad de pago | Cuota mensual <= 30% salario estimado |
| RN-E-04 | Sin créditos activos | No puede tener solicitudes en estado DESEMBOLSADO |

### Score Interno (RN-S)

```
scoreInterno = puntajeAntiguedad + puntajeHistorialAhorro + puntajeCapacidadPago
               (máx 30)            (máx 30)                   (máx 40)
               Total máximo: 100 puntos
```

**Antigüedad:**
- < 6 meses: 0 pts
- 6-12 meses: 10 pts
- 1-2 años: 20 pts
- > 2 años: 30 pts

**Historial de Ahorro:**
- Sin cuenta: 0 pts
- < 6 meses: 10 pts
- 6-12 meses: 20 pts
- > 12 meses: 30 pts

**Capacidad de Pago:**
- Cuota <= 15% salario: 40 pts
- Cuota 15-25% salario: 25 pts
- Cuota 25-30% salario: 10 pts
- Cuota > 30% salario: 0 pts

### Tasas de Interés (RN-T)

| Score | Tasa Aplicada |
|-------|---------------|
| >= 80 | tasaBase * 0.85 (15% descuento) |
| 70-79 | tasaBase * 0.95 (5% descuento) |
| 60-69 | tasaBase (sin modificación) |
| 50-59 | tasaBase * 1.10 (10% recargo) |

### Colateral (RN-C)

| ID | Regla |
|----|-------|
| RN-C-01 | Si score < 70, requiere colateral |
| RN-C-02 | Monto colateral >= (porcentajeRequerimientoColateral * montoSolicitado) |
| RN-C-03 | Saldo disponible en cuenta debe cubrir requerimiento |
| RN-C-04 | Al aprobar: saldo retenido en CuentaAhorro |
| RN-C-05 | Al pagar última cuota: liberar saldo retenido |
| RN-C-06 | Mora > 30 días: ejecutar colateral |

### Mora

- **Días de gracia:** Configurable por tipo de crédito (default 5 días)
- **Interés moratorio:** `saldo_pendiente * (penalidadMoraTasa / 30) * dias_mora`
- **Ejecución:** Si mora > 90 días sin regularización

---

## Endpoints Principales

El módulo implementa **14 endpoints** REST:

### Solicitudes de Crédito

| # | Método | Path | Descripción | Roles |
|---|--------|------|-------------|-------|
| 1 | POST | `/api/v1/creditos/solicitudes` | Crear solicitud | SOCIO, ADMIN |
| 2 | GET | `/api/v1/creditos/solicitudes/{numero}` | Consultar solicitud | SOCIO, ADMIN |
| 3 | GET | `/api/v1/creditos/solicitudes/socio/{socioId}` | Listar por socio | SOCIO, ADMIN |

### Tipos de Crédito

| # | Método | Path | Descripción | Roles |
|---|--------|------|-------------|-------|
| 4 | GET | `/api/v1/creditos/tipos-credito` | Listar tipos | SOCIO, ADMIN |
| 5 | GET | `/api/v1/creditos/tipos-credito/{id}` | Consultar tipo | SOCIO, ADMIN |

### Evaluación y Aprobación

| # | Método | Path | Descripción | Roles |
|---|--------|------|-------------|-------|
| 6 | POST | `/api/v1/creditos/solicitudes/{numero}/evaluar` | Evaluar | ADMIN, SISTEMA |
| 7 | POST | `/api/v1/creditos/solicitudes/{numero}/aprobar` | Aprobar | ADMIN |
| 8 | POST | `/api/v1/creditos/solicitudes/{numero}/rechazar` | Rechazar | ADMIN |

### Plan y Desembolso

| # | Método | Path | Descripción | Roles |
|---|--------|------|-------------|-------|
| 9 | GET | `/api/v1/creditos/solicitudes/{numero}/plan` | Consultar plan | SOCIO, ADMIN |
| 10 | POST | `/api/v1/creditos/solicitudes/{numero}/desembolson` | Desembolsar | ADMIN, SISTEMA |

### Pagos

| # | Método | Path | Descripción | Roles |
|---|--------|------|-------------|-------|
| 11 | GET | `/api/v1/creditos/{numero}/cuotas` | Listar cuotas | SOCIO, ADMIN |
| 12 | POST | `/api/v1/creditos/cuotas/{cuotaId}/pago` | Registrar pago | CAJERO, ADMIN, SOCIO |

### Estado y Simulación

| # | Método | Path | Descripción | Roles |
|---|--------|------|-------------|-------|
| 13 | GET | `/api/v1/creditos/{numero}` | Consultar estado crédito | SOCIO, ADMIN |
| 14 | POST | `/api/v1/simulador` | Simular crédito | SOCIO, ADMIN |

> **Nota:** El endpoint `/simulador` tiene rate limiting de 10 req/min por IP.

---

## Seguridad

El módulo implementa múltiples capas de seguridad:

### Autenticación y Autorización

- Todos los endpoints (excepto `/simulador` con restricciones) requieren JWT token
- El token debe contener: `socioId`, `roles[]`, `sessionId`
- Control de acceso basado en roles (RBAC)

### Validación IDOR

Prevención de Insecure Direct Object Reference:
- Un socio solo puede ver SUS PROPIAS solicitudes y créditos
- Validación en cada endpoint: `socioId` del token debe coincidir con el recurso
- ADMIN puede ver cualquier recurso

```java
// Ejemplo de validación IDOR
if (usuario.rol() == Rol.SOCIO && 
    !solicitud.getSocioId().equals(usuario.getSocioId())) {
    throw new AccesoNoAutorizadoException();
}
```

### Optimistic Locking

Previene condiciones de carrera en pagos concurrentes:
- Campo `@Version` en `Amortizacion` y otras entidades
- Excepción `OptimisticLockException` si versión no coincide

### Idempotencia en Pagos

- Campo `referencia_pago` como UNIQUE en base de datos
- Previene double-payment si el mismo pago se envía dos veces

### Rate Limiting

- `/simulador`: máximo 10 solicitudes por minuto por IP
- Headers: `X-RateLimit-Remaining`, `X-RateLimit-Reset`

### Auditoría Criptográfica

- `scoreHash`: SHA-256 del cálculo del score
- `firmaVerificable`: Firma RSA para verificar integridad

### Número de Solicitud No Enumerable

- Formato: `SOL-CRED-YYYY-XXXXXX` con SecureRandom
- Previene enumeración de solicitudes

---

## Estructura del Proyecto

```
src/main/java/com/tufondo/creditos/
├── api/
│   └── controller/
│       └── CreditoController.java      # 14 endpoints REST
├── application/
│   ├── dto/                            # DTOs de request/response
│   ├── mapper/                         # Mapeadores Entity<->DTO
│   └── usecase/                        # Casos de uso
│       ├── CrearSolicitudCreditoUseCase.java
│       ├── EvaluarSolicitudUseCase.java
│       ├── AprobarSolicitudUseCase.java
│       ├── RechazarSolicitudUseCase.java
│       ├── DesembolsaCreditoUseCase.java
│       ├── ObtenerPlanAmortizacionUseCase.java
│       ├── ListarCuotasUseCase.java
│       ├── RegistrarPagoCuotaUseCase.java
│       ├── ObtenerEstadoCreditoUseCase.java
│       ├── SimularCreditoUseCase.java
│       └── EjecutarColateralUseCase.java
├── domain/
│   ├── model/
│   │   ├── TipoCredito.java
│   │   ├── SolicitudCredito.java
│   │   ├── EvaluacionCrediticia.java
│   │   ├── PlanAmortizacion.java
│   │   ├── Amortizacion.java
│   │   └── enums/
│   │       ├── EstadoSolicitud.java
│   │       ├── EstadoAmortizacion.java
│   │       ├── EstadoPlanAmortizacion.java
│   │       ├── NivelRiesgo.java
│   │       ├── FrecuenciaPago.java
│   │       └── CanalOrigen.java
│   ├── repository/                     # Interfaces de repositorio
│   └── exception/                      # Excepciones de dominio
└── infrastructure/
    ├── persistence/
    │   ├── adapter/                    # Implementaciones de repositorio
    │   ├── jpa/                        # JPA Repositories
    │   └── entity/                     # Entidades JPA
    ├── security/
    │   └── SimulacionRateLimitFilter.java
    └── exception/
        └── CreditosExceptionHandler.java
```

---

## Dependencias Externas

### Módulo de Ahorros (CuentaGarantiaRepository)

El módulo de Créditos depende del módulo de Ahorros para gestión de colaterales:

```java
public interface CuentaGarantiaRepository {
    BigDecimal obtenerSaldoDisponible(UUID cuentaId);
    boolean verificarSaldoParaColateral(UUID cuentaId, BigDecimal montoRequerido);
    void retenerSaldo(UUID cuentaId, BigDecimal monto);
    void liberarSaldo(UUID cuentaId, BigDecimal monto);
    void transferirSaldo(UUID cuentaOrigenId, UUID cuentaDestinoId, BigDecimal monto);
}
```

---

## Historial de Cambios

| Fecha | Descripción |
|-------|-------------|
| 2026-04-14 | Creación del módulo con 14 endpoints |
| 2026-04-14 | Integración de correcciones de auditoría de seguridad |
| 2026-04-14 | Agregados campos de auditoría criptográfica (scoreHash, firmaVerificable) |
| 2026-04-14 | Implementación de validación IDOR y rate limiting |

---

## Documentación Relacionada

| Documento | Descripción |
|------------|-------------|
| [API.md](./API.md) | Referencia completa de endpoints |
| [MODELO_DATOS.md](./MODELO_DATOS.md) | Esquema de base de datos |
| [PAGOS.md](./PAGOS.md) | Sistema de amortización y pagos |
| [SPEC.md](./SPEC.md) | Especificación técnica detallada |

---

## Rama

`feature/modulo-creditos`