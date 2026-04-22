# Módulo de Gestión de Ahorros - Referencia de API

## Resumen

Documentación completa de los **12 endpoints** del módulo de Gestión de Ahorros con ejemplos de request/response, códigos de error y notas de seguridad.

---

## Base URL

```
Production: https://api.fondoahorro.com/v1
Development: http://localhost:8080/api/v1
```

## Autenticación

Todos los endpoints requieren header de autenticación:
```
Authorization: Bearer <jwt_token>
```

Los tokens JWT deben incluir:
- `socioId`: ID del socio autenticado
- `roles`: Array de roles (`SOCIO`, `ADMIN`, `SISTEMA`)
- `sessionId`: ID de sesión para auditoría

---

## Endpoints de Cuentas

### 1. POST /cuentas - Crear Cuenta de Ahorro

**Descripción:** Registra una nueva cuenta de ahorro para un socio.

**Roles permitidos:** `ADMIN`, `SISTEMA`

#### Request

```http
POST /api/v1/cuentas
Content-Type: application/json
Authorization: Bearer <token>

{
  "socioId": 12345,
  "tipoCuenta": "AHORRO",
  "montoMinimoRequerido": 1000.00,
  "tasaInteres": 0.045
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| socioId | Long | Sí | > 0 | ID del socio propietario |
| tipoCuenta | Enum | Sí | ACTIVA, SUSPENDIDA, CERRADA | Tipo de cuenta |
| montoMinimoRequerido | BigDecimal | No | >= 0.0001, <= 999999999.9999 | Saldo mínimo de protección |
| tasaInteres | BigDecimal | No | >= 0.0, <= 1.0 | Tasa anual de interés |

#### Response - 201 Created

```json
{
  "id": 1,
  "numeroCuenta": "AHO-2026-000001",
  "socioId": 12345,
  "saldoActual": 0.0000,
  "saldoRetenido": 0.0000,
  "tasaInteres": 0.045,
  "montoMinimoRequerido": 1000.0000,
  "estado": "ACTIVA",
  "tipoCuenta": "AHORRO",
  "fechaApertura": "2026-04-14T10:30:00Z",
  "fechaUltimaOperacion": null
}
```

#### Response - 400 Bad Request

```json
{
  "error": "VALIDATION_ERROR",
  "mensaje": "Datos de entrada inválidos",
  "detalles": [
    {
      "campo": "montoMinimoRequerido",
      "mensaje": "debe ser >= 0.0001"
    }
  ],
  "timestamp": "2026-04-14T10:30:00Z"
}
```

#### Response - 409 Conflict

```json
{
  "error": "CUENTA_DUPLICADA",
  "mensaje": "El socio ya tiene una cuenta de tipo AHORRO",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 2. GET /cuentas/{numeroCuenta} - Consultar Cuenta

**Descripción:** Obtiene los datos completos de una cuenta de ahorro.

**Roles permitidos:** `SOCIO` (solo su cuenta), `ADMIN`

#### Request

```http
GET /api/v1/cuentas/AHO-2026-000001
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": 1,
  "numeroCuenta": "AHO-2026-000001",
  "socioId": 12345,
  "saldoActual": 15000.5000,
  "saldoRetenido": 500.0000,
  "saldoDisponible": 14500.5000,
  "tasaInteres": 0.045,
  "montoMinimoRequerido": 1000.0000,
  "estado": "ACTIVA",
  "tipoCuenta": "AHORRO",
  "fechaApertura": "2026-04-14T10:30:00Z",
  "fechaUltimaOperacion": "2026-04-14T15:45:00Z"
}
```

#### Response - 403 Forbidden (IDOR Check Failed)

```json
{
  "error": "ACCESO_CUENTA_AJENA",
  "mensaje": "No tiene permiso para acceder a esta cuenta",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

#### Response - 404 Not Found

```json
{
  "error": "CUENTA_NO_ENCONTRADA",
  "mensaje": "No existe cuenta con número AHO-2026-999999",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

> **Nota de Seguridad:** El sistema verifica que `socioId` del token coincida con el socio propietario de la cuenta. Si no coincide y el rol no es `ADMIN`, se retorna 403.

---

### 3. GET /cuentas/socio/{socioId} - Listar Cuentas por Socio

**Descripción:** Obtiene todas las cuentas de ahorro asociadas a un socio.

**Roles permitidos:** `SOCIO` (solo sus cuentas), `ADMIN`

#### Request

```http
GET /api/v1/cuentas/socio/12345
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "socioId": 12345,
  "totalCuentas": 2,
  "cuentas": [
    {
      "id": 1,
      "numeroCuenta": "AHO-2026-000001",
      "saldoActual": 15000.5000,
      "estado": "ACTIVA",
      "tipoCuenta": "AHORRO",
      "fechaApertura": "2026-04-14T10:30:00Z"
    },
    {
      "id": 2,
      "numeroCuenta": "AHO-2026-000002",
      "saldoActual": 50000.0000,
      "estado": "ACTIVA",
      "tipoCuenta": "NOMINA",
      "fechaApertura": "2026-03-01T08:00:00Z"
    }
  ]
}
```

---

### 4. GET /cuentas/{numeroCuenta}/saldo - Consultar Saldo

**Descripción:** Consulta el saldo de una cuenta con desglose detallado.

**Roles permitidos:** `SOCIO` (solo su cuenta), `ADMIN`

#### Request

```http
GET /api/v1/cuentas/AHO-2026-000001/saldo
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "numeroCuenta": "AHO-2026-000001",
  "saldoActual": 15000.5000,
  "saldoRetenido": 500.0000,
  "saldoDisponible": 14500.5000,
  "fechaConsulta": "2026-04-14T16:00:00Z",
  "limiteDeposito": 500000.00,
  "limiteRetiroDiario": 50000.00,
  "retirosRealizadosHoy": 5000.00,
  "retirosRestantesHoy": 45000.00
}
```

---

## Endpoints de Operaciones Financieras

### 5. POST /cuentas/{numeroCuenta}/depositos - Realizar Depósito

**Descripción:** Registra un depósito en la cuenta de ahorro.

**Roles permitidos:** `SOCIO` (solo su cuenta), `ADMIN`

#### Request

```http
POST /api/v1/cuentas/AHO-2026-000001/depositos
Content-Type: application/json
Authorization: Bearer <token>

{
  "monto": 5000.00,
  "descripcion": "Depósito por nómina",
  "referencia": "NOM-2026-04-001",
  "canalOrigen": "MOBILE"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| monto | BigDecimal | Sí | >= 0.0001, <= 500000.00 | Monto del depósito |
| descripcion | String | No | max 500 chars | Descripción de la operación |
| referencia | String | No | max 100 chars | Referencia bancaria externa |
| canalOrigen | Enum | Sí | WEB, MOBILE, ATM, SUCURSAL, API | Canal donde se realizó |

#### Response - 201 Created

```json
{
  "id": 1,
  "numeroOperacion": "MOV-2026-000001",
  "cuentaAhorroId": 1,
  "tipo": "DEPOSITO",
  "monto": 5000.0000,
  "saldoAnterior": 10000.5000,
  "saldoPosterior": 15000.5000,
  "descripcion": "Depósito por nómina",
  "referencia": "NOM-2026-04-001",
  "canalOrigen": "MOBILE",
  "ipOrigen": "192.168.1.100",
  "sessionId": "sess_abc123",
  "requestId": "req_xyz789",
  "estado": "PROCESADO",
  "fechaMovimiento": "2026-04-14T16:05:00Z",
  "fechaValor": "2026-04-14"
}
```

#### Response - 400 Bad Request

```json
{
  "error": "MONTO_EXCEDE_LIMITE",
  "mensaje": "El monto de depósito excede el límite de 500,000.00 USD",
  "detalles": {
    "montoRecibido": 600000.00,
    "limitePermitido": 500000.00
  },
  "timestamp": "2026-04-14T16:05:00Z"
}
```

#### Response - 422 Unprocessable Entity

```json
{
  "error": "CUENTA_NO_PERMITE_OPERACIONES",
  "mensaje": "La cuenta AHO-2026-000001 está en estado CERRADA y no permite depósitos",
  "timestamp": "2026-04-14T16:05:00Z"
}
```

---

### 6. POST /cuentas/{numeroCuenta}/retiros - Realizar Retiro

**Descripción:** Registra un retiro de la cuenta de ahorro.

**Roles permitidos:** `SOCIO` (solo su cuenta), `ADMIN`

#### Request

```http
POST /api/v1/cuentas/AHO-2026-000001/retiros
Content-Type: application/json
Authorization: Bearer <token>

{
  "monto": 3000.00,
  "canalOrigen": "ATM"
}
```

#### Validaciones

| Validación | Descripción | Resultado |
|------------|-------------|-----------|
| V-001 | Cuenta en estado ACTIVA | Error si no |
| V-002 | Monto >= 0.0001 | Error de validación |
| V-003 | Monto <= 50000.00 (límite diario) | Error si excede |
| V-004 | Saldo disponible >= monto | Error si insuficiente |
| V-005 | Verificar IDOR | Error si acceso no autorizado |

#### Response - 201 Created

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
  "ipOrigen": "192.168.1.101",
  "sessionId": "sess_abc123",
  "requestId": "req_def456",
  "estado": "PROCESADO",
  "fechaMovimiento": "2026-04-14T16:10:00Z",
  "fechaValor": "2026-04-14"
}
```

#### Response - 200 OK with Warning

> Cuando el retiro deja el saldo por debajo del monto mínimo requerido, se retorna success con warning.

```json
{
  "id": 3,
  "numeroOperacion": "MOV-2026-000003",
  "cuentaAhorroId": 1,
  "tipo": "RETIRO",
  "monto": 5000.0000,
  "saldoAnterior": 6000.5000,
  "saldoPosterior": 500.5000,
  "estado": "PROCESADO",
  "warnings": [
    {
      "codigo": "SALDO_BAJO_MINIMO",
      "mensaje": "El saldo resultante (500.50) está por debajo del monto mínimo requerido (1000.00)"
    }
  ],
  "fechaMovimiento": "2026-04-14T16:15:00Z"
}
```

#### Response - 429 Too Many Requests

```json
{
  "error": "RATE_LIMIT_EXCEEDED",
  "mensaje": "Ha excedido el límite de requests. Máximo 30 intentos de retiro por minuto",
  "retryAfter": 60,
  "timestamp": "2026-04-14T16:10:00Z"
}
```

---

### 7. GET /cuentas/{numeroCuenta}/movimientos - Listar Movimientos

**Descripción:** Lista los movimientos de una cuenta con paginación.

**Roles permitidos:** `SOCIO` (solo su cuenta), `ADMIN`

#### Request

```http
GET /api/v1/cuentas/AHO-2026-000001/movimientos?page=0&size=20&fechaInicio=2026-04-01&fechaFin=2026-04-30
Authorization: Bearer <token>
```

#### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| page | Integer | No | 0 | Número de página (0-indexed) |
| size | Integer | No | 20 | Tamaño de página (máx 100) |
| fechaInicio | Date | No | 30 días atrás | Fecha inicio del rango |
| fechaFin | Date | No | Hoy | Fecha fin del rango |
| tipo | Enum | No | null | Filtrar por tipo de movimiento |

#### Response - 200 OK

```json
{
  "numeroCuenta": "AHO-2026-000001",
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 45,
  "totalPaginas": 3,
  "movimientos": [
    {
      "id": 45,
      "numeroOperacion": "MOV-2026-000045",
      "tipo": "DEPOSITO",
      "monto": 5000.0000,
      "saldoPosterior": 15000.5000,
      "descripcion": "Depósito por nómina",
      "fechaMovimiento": "2026-04-14T08:00:00Z"
    },
    {
      "id": 44,
      "numeroOperacion": "MOV-2026-000044",
      "tipo": "RETIRO",
      "monto": 2000.0000,
      "saldoPosterior": 10000.5000,
      "descripcion": "Retiro cajero",
      "fechaMovimiento": "2026-04-13T15:30:00Z"
    }
  ]
}
```

---

### 8. GET /cuentas/{numeroCuenta}/movimientos/{numeroOperacion} - Detalle de Movimiento

**Descripción:** Obtiene el detalle completo de un movimiento específico.

**Roles permitidos:** `SOCIO` (solo su cuenta), `ADMIN`

#### Request

```http
GET /api/v1/cuentas/AHO-2026-000001/movimientos/MOV-2026-000045
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": 45,
  "numeroOperacion": "MOV-2026-000045",
  "cuentaAhorroId": 1,
  "socioId": 12345,
  "tipo": "DEPOSITO",
  "monto": 5000.0000,
  "saldoAnterior": 10000.5000,
  "saldoPosterior": 15000.5000,
  "descripcion": "Depósito por nómina",
  "referencia": "NOM-2026-04-001",
  "canalOrigen": "SUCURSAL",
  "ipOrigen": "192.168.1.100",
  "sessionId": "sess_abc123",
  "requestId": "req_xyz789",
  "estado": "PROCESADO",
  "fechaMovimiento": "2026-04-14T08:00:00Z",
  "fechaValor": "2026-04-14"
}
```

#### Response - 404 Not Found

```json
{
  "error": "MOVIMIENTO_NO_ENCONTRADO",
  "mensaje": "No existe movimiento con número MOV-2026-999999",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

## Endpoints de Rendimientos

### 9. POST /cuentas/{numeroCuenta}/rendimientos/calcular - Calcular Rendimiento

**Descripción:** Calcula el rendimiento de una cuenta para un periodo.

**Roles permitidos:** `ADMIN`, `SISTEMA`

#### Request

```http
POST /api/v1/cuentas/AHO-2026-000001/rendimientos/calcular
Content-Type: application/json
Authorization: Bearer <token>

{
  "periodoInicio": "2026-03-01",
  "periodoFin": "2026-03-31",
  "tipo": "MENSUAL"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| periodoInicio | LocalDate | Sí | <= periodoFin | Inicio del periodo |
| periodoFin | LocalDate | Sí | >= periodoInicio | Fin del periodo |
| tipo | Enum | Sí | DIARIO, MENSUAL, ANUAL | Tipo de rendimiento |

#### Response - 201 Created

```json
{
  "id": 15,
  "cuentaAhorroId": 1,
  "periodoInicio": "2026-03-01",
  "periodoFin": "2026-03-31",
  "saldoPromedioPeriodo": 12500.0000,
  "tasaAplicada": 0.045,
  "montoRendimiento": 46.8750,
  "tipo": "MENSUAL",
  "estadoAplicacion": "CALCULADO",
  "fechaCalculo": "2026-04-01T00:00:00Z"
}
```

#### Response - 422 Unprocessable Entity

```json
{
  "error": "RENDIMIENTO YA APLICADO",
  "mensaje": "El periodo 2026-03-01 a 2026-03-31 ya tiene un rendimiento aplicado",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 10. GET /cuentas/{numeroCuenta}/rendimientos - Listar Rendimientos

**Descripción:** Lista todos los rendimientos de una cuenta.

**Roles permitidos:** `SOCIO` (solo su cuenta), `ADMIN`

#### Request

```http
GET /api/v1/cuentas/AHO-2026-000001/rendimientos?page=0&size=20
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "numeroCuenta": "AHO-2026-000001",
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 3,
  "rendimientos": [
    {
      "id": 15,
      "periodoInicio": "2026-03-01",
      "periodoFin": "2026-03-31",
      "saldoPromedioPeriodo": 12500.0000,
      "tasaAplicada": 0.045,
      "montoRendimiento": 46.8750,
      "tipo": "MENSUAL",
      "estadoAplicacion": "APLICADO",
      "fechaCalculo": "2026-04-01T00:00:00Z"
    },
    {
      "id": 14,
      "periodoInicio": "2026-02-01",
      "periodoFin": "2026-02-28",
      "saldoPromedioPeriodo": 12000.0000,
      "tasaAplicada": 0.045,
      "montoRendimiento": 44.2500,
      "tipo": "MENSUAL",
      "estadoAplicacion": "APLICADO",
      "fechaCalculo": "2026-03-01T00:00:00Z"
    }
  ]
}
```

---

### 11. DELETE /cuentas/{numeroCuenta} - Cerrar Cuenta

**Descripción:** Cierra una cuenta de ahorro.

**Roles permitidos:** `ADMIN`

#### Request

```http
DELETE /api/v1/cuentas/AHO-2026-000001
Authorization: Bearer <token>
```

#### Validaciones

| Validación | Descripción | Resultado |
|------------|-------------|-----------|
| V-006 | Saldo actual = 0 | Error si saldo > 0 |
| V-007 | No hay movimientos pendientes | Error si existen |

#### Response - 200 OK

```json
{
  "id": 1,
  "numeroCuenta": "AHO-2026-000001",
  "estado": "CERRADA",
  "fechaCierre": "2026-04-14T17:00:00Z",
  "saldoFinal": 0.0000,
  "mensaje": "Cuenta cerrada exitosamente"
}
```

#### Response - 409 Conflict

```json
{
  "error": "SALDO_NO_CERO",
  "mensaje": "No se puede cerrar la cuenta. Saldo actual: 150.50",
  "timestamp": "2026-04-14T17:00:00Z"
}
```

---

### 12. POST /rendimientos/calcular-batch - Calcular Rendimientos Batch

**Descripción:** Calcula rendimientos para múltiples cuentas en una sola operación.

**Roles permitidos:** `ADMIN`, `SISTEMA`

#### Request

```http
POST /api/v1/rendimientos/calcular-batch
Content-Type: application/json
Authorization: Bearer <token>

{
  "periodoInicio": "2026-03-01",
  "periodoFin": "2026-03-31",
  "tipo": "MENSUAL",
  "cuentaIds": [1, 2, 3, 4, 5]
}
```

#### Response - 200 OK

```json
{
  "totalCuentas": 5,
  "procesadas": 5,
  "exitosas": 4,
  "fallidas": 1,
  "resultados": [
    {
      "cuentaId": 1,
      "numeroCuenta": "AHO-2026-000001",
      "exitoso": true,
      "rendimientoId": 15
    },
    {
      "cuentaId": 2,
      "numeroCuenta": "AHO-2026-000002",
      "exitoso": true,
      "rendimientoId": 16
    },
    {
      "cuentaId": 3,
      "numeroCuenta": "AHO-2026-000003",
      "exitoso": false,
      "error": "CUENTA_NO_PERMITE_RENDIMIENTOS"
    }
  ],
  "fechaProcesamiento": "2026-04-14T23:00:00Z"
}
```

> **Nota de Arquitectura:** El endpoint batch implementa **Saga Pattern** para compensación. Si una cuenta falla, las anteriores no se revierten (procesamiento idempotente). Para escenarios de alto riesgo, considerar implementación de saga completa.

---

## Códigos de Error Comunes

| Código HTTP | Error Code | Descripción |
|-------------|------------|-------------|
| 400 | VALIDATION_ERROR | Datos de entrada inválidos |
| 400 | MONTO_EXCEDE_LIMITE | Monto supera límite regulatorio |
| 401 | NO_AUTORIZADO | Token inválido o expirado |
| 403 | ACCESO_CUENTA_AJENA | IDOR - Sin acceso a la cuenta |
| 404 | CUENTA_NO_ENCONTRADA | Cuenta no existe |
| 404 | MOVIMIENTO_NO_ENCONTRADO | Movimiento no existe |
| 409 | CUENTA_DUPLICADA | Socio ya tiene cuenta de este tipo |
| 409 | SALDO_NO_CERO | No se puede cerrar con saldo |
| 422 | CUENTA_NO_PERMITE_OPERACIONES | Estado no permite la operación |
| 422 | RENDIMIENTO_YA_APLICADO | Periodo ya tiene rendimiento |
| 429 | RATE_LIMIT_EXCEEDED | Demasiadas solicitudes |
| 500 | ERROR_INTERNO | Error interno del servidor |

---

## Headers de Response

Todos los responses incluyen:

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| X-Request-Id | ID único de la request | `req_xyz789` |
| X-Response-Time | Tiempo de procesamiento (ms) | `45` |
| X-Correlation-Id | ID de correlación para logs | `corr_abc123` |

---

## Rate Limiting

| Plan | Límite | Ventana |
|------|--------|---------|
| Default | 100 requests/min | Por IP |
| Operaciones financieras | 30 requests/min | Por endpoint |
| Consultas | 60 requests/min | Por endpoint |

Los headers de rate limiting en response:
- `X-RateLimit-Remaining`: Requests restantes
- `X-RateLimit-Reset`: Timestamp de reset
- `Retry-After`: Segundos para reintentar (cuando se excede)

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0.0 | 2026-04-14 | @documentador | Creación inicial basada en spec PM |
| 1.0.1 | 2026-04-14 | @documentador | Integración hallazgos auditoría seguridad |

---

## Referencias

- Especificación técnica: `/docs/modulos/ahorros/SPEC.md`
- Modelo de datos: `/docs/modulos/ahorros/MODELO_DATOS.md`
- Auditoría de seguridad: `/docs/auditorias/seguridad_YYYYMMDD.md`