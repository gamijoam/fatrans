# Módulo de Gestión de Créditos - Referencia de API

## Resumen

Documentación completa de los **14 endpoints** del módulo de Gestión de Créditos con ejemplos de request/response, códigos de error y notas de seguridad. Incluye correcciones de auditoría integradas.

---

## Base URL

```
Production: https://api.fondoahorro.com/v1
Development: http://localhost:8080/api/v1
```

## Autenticación

Todos los endpoints requieren header de autenticación (excepto `/simulador` con restricciones):

```
Authorization: Bearer <jwt_token>
```

Los tokens JWT deben incluir:
- `socioId`: ID del socio autenticado
- `roles`: Array de roles (`SOCIO`, `ADMIN`, `SISTEMA`, `CAJERO`)
- `sessionId`: ID de sesión para auditoría

---

## Endpoints de Solicitud de Crédito

### 1. POST /solicitudes - Crear Solicitud de Crédito

**Descripción:** Registra una nueva solicitud de crédito para un socio.

**Roles permitidos:** `SOCIO`, `ADMIN`

#### Request

```http
POST /api/v1/creditos/solicitudes
Content-Type: application/json
Authorization: Bearer <token>

{
  "tipoCreditoId": 1,
  "montoSolicitado": 50000.0000,
  "plazoMeses": 12,
  "destinoCredito": "Compra de vehículo usado",
  "cuentaDestino": "MX82FAWA0000123456789012"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| tipoCreditoId | Long | Sí | > 0 | ID del tipo de crédito |
| montoSolicitado | BigDecimal | Sí | >= 0.0001, <= 999999999.9999 | Monto solicitado |
| plazoMeses | Integer | Sí | 1-360 | Plazo en meses |
| destinoCredito | String | No | max 500 chars | Destino del crédito |
| cuentaDestino | String | No | max 34 chars | IBAN/cuenta destino |

#### Response - 201 Created

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSolicitud": "SOL-CRED-2026-482917",
  "socioId": 12345,
  "tipoCreditoId": 1,
  "montoSolicitado": 50000.0000,
  "plazoMeses": 12,
  "estado": "PENDIENTE",
  "cuentaDestino": "MX82FAWA0000123456789012",
  "createdAt": "2026-04-14T10:30:00Z"
}
```

#### Response - 400 Bad Request

```json
{
  "error": "VALIDATION_ERROR",
  "mensaje": "Datos de entrada inválidos",
  "detalles": [
    {
      "campo": "montoSolicitado",
      "mensaje": "debe ser >= 0.0001"
    }
  ],
  "timestamp": "2026-04-14T10:30:00Z"
}
```

#### Response - 409 Conflict

```json
{
  "error": "CREDITO_ACTIVO_EXISTENTE",
  "mensaje": "El socio ya tiene un crédito activo en estado DESEMBOLSADO",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 2. GET /solicitudes/{numeroSolicitud} - Consultar Solicitud

**Descripción:** Obtiene los datos de una solicitud de crédito. **Importante:** Validación IDOR implementada - un socio solo puede ver sus propias solicitudes.

**Roles permitidos:** `SOCIO` (solo propia), `ADMIN`

#### Request

```http
GET /api/v1/creditos/solicitudes/SOL-CRED-2026-482917
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSolicitud": "SOL-CRED-2026-482917",
  "socioId": 12345,
  "tipoCredito": {
    "id": 1,
    "nombre": "Crédito Vehículo",
    "tasaInteresAnual": 0.1450
  },
  "montoSolicitado": 50000.0000,
  "plazoMeses": 12,
  "tasaInteresAplicada": 0.1378,
  "cuotaMensualEstimada": 4550.0000,
  "estado": "EN_EVALUACION",
  "colateralMontoRetenido": null,
  "destinoCredito": "Compra de vehículo usado",
  "evaluacion": {
    "scoreInterno": 72,
    "elegible": true,
    "nivelRiesgo": "MEDIO"
  },
  "createdAt": "2026-04-14T10:30:00Z"
}
```

#### Response - 403 Forbidden (IDOR Check)

```json
{
  "error": "ACCESO_NO_AUTORIZADO",
  "mensaje": "No tiene permiso para acceder a esta solicitud",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

#### Response - 404 Not Found

```json
{
  "error": "SOLICITUD_NO_ENCONTRADA",
  "mensaje": "No existe solicitud con número SOL-CRED-2026-999999",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

> **Corrección de Seguridad:** Verifica que `socioId` del token coincida con el socio propietario de la solicitud. Si no coincide y el rol no es `ADMIN`, retorna 403.

---

### 3. GET /solicitudes/socio/{socioId} - Listar Solicitudes por Socio

**Descripción:** Obtiene todas las solicitudes de crédito asociadas a un socio.

**Roles permitidos:** `SOCIO` (solo sus solicitudes), `ADMIN`

#### Request

```http
GET /api/v1/creditos/solicitudes/socio/12345
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "socioId": 12345,
  "totalSolicitudes": 2,
  "solicitudes": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "numeroSolicitud": "SOL-CRED-2026-482917",
      "tipoCredito": "Crédito Vehículo",
      "montoSolicitado": 50000.0000,
      "plazoMeses": 12,
      "estado": "EN_EVALUACION",
      "fechaCreacion": "2026-04-14T10:30:00Z"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "numeroSolicitud": "SOL-CRED-2026-315622",
      "tipoCredito": "Micro Crédito",
      "montoSolicitado": 15000.0000,
      "plazoMeses": 6,
      "estado": "DESEMBOLSADO",
      "fechaCreacion": "2026-02-01T08:00:00Z"
    }
  ]
}
```

---

## Endpoints de Tipos de Crédito

### 4. GET /tipos-credito - Listar Tipos de Crédito

**Descripción:** Lista todos los tipos de crédito disponibles.

**Roles permitidos:** `SOCIO`, `ADMIN`

#### Request

```http
GET /api/v1/creditos/tipos-credito
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "tiposCredito": [
    {
      "id": 1,
      "codigo": "CRED_VEHICULO",
      "nombre": "Crédito Vehículo",
      "descripcion": "Crédito para adquisición de vehículos",
      "tasaInteresAnual": 0.1450,
      "plazoMinimoMeses": 12,
      "plazoMaximoMeses": 60,
      "montoMinimo": 50000.0000,
      "montoMaximo": 1000000.0000,
      "porcentajeRequerimientoColateral": 0.20,
      "comisionApertura": 0.0050,
      "penalidadMoraTasa": 0.0005,
      "diasGracia": 5,
      "activo": true
    },
    {
      "id": 2,
      "codigo": "MICRO_CREDITO",
      "nombre": "Micro Crédito",
      "descripcion": "Crédito para pequeñas necesidades",
      "tasaInteresAnual": 0.2400,
      "plazoMinimoMeses": 1,
      "plazoMaximoMeses": 12,
      "montoMinimo": 1000.0000,
      "montoMaximo": 50000.0000,
      "porcentajeRequerimientoColateral": 0.10,
      "comisionApertura": 0.0100,
      "penalidadMoraTasa": 0.0010,
      "diasGracia": 3,
      "activo": true
    }
  ]
}
```

---

### 5. GET /tipos-credito/{id} - Consultar Tipo de Crédito

**Descripción:** Obtiene el detalle de un tipo de crédito específico.

**Roles permitidos:** `SOCIO`, `ADMIN`

#### Request

```http
GET /api/v1/creditos/tipos-credito/1
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": 1,
  "codigo": "CRED_VEHICULO",
  "nombre": "Crédito Vehículo",
  "descripcion": "Crédito para adquisición de vehículos",
  "tasaInteresAnual": 0.1450,
  "plazoMinimoMeses": 12,
  "plazoMaximoMeses": 60,
  "montoMinimo": 50000.0000,
  "montoMaximo": 1000000.0000,
  "porcentajeRequerimientoColateral": 0.20,
  "comisionApertura": 0.0050,
  "penalidadMoraTasa": 0.0005,
  "diasGracia": 5,
  "activo": true
}
```

---

## Endpoints de Evaluación

### 6. POST /solicitudes/{numeroSolicitud}/evaluar - Evaluar Solicitud

**Descripción:** Genera la evaluación crediticia calculando el score interno y determinando elegibilidad.

**Roles permitidos:** `ADMIN`, `SISTEMA`

#### Request

```http
POST /api/v1/creditos/solicitudes/SOL-CRED-2026-482917/evaluar
Content-Type: application/json
Authorization: Bearer <token>

{
  "puntajeAntiguedad": 20,
  "puntajeHistorialAhorro": 25,
  "puntajeCapacidadPago": 27,
  "salarioEstimado": 25000.00
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| puntajeAntiguedad | Integer | Sí | 0-30 | Puntos por antigüedad |
| puntajeHistorialAhorro | Integer | Sí | 0-30 | Puntos por historial |
| puntajeCapacidadPago | Integer | Sí | 0-40 | Puntos por capacidad |
| salarioEstimado | BigDecimal | Sí | > 0 | Salario estimado del socio |

#### Response - 201 Created

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "solicitudId": "550e8400-e29b-41d4-a716-446655440000",
  "socioId": 12345,
  "puntajeAntiguedad": 20,
  "puntajeHistorialAhorro": 25,
  "puntajeCapacidadPago": 27,
  "scoreInterno": 72,
  "scoreHash": "a3f2c1d9e8b7...",
  "elegible": true,
  "nivelRiesgo": "MEDIO",
  "tasaInteresFinal": 0.1378,
  "mensajeDecision": "Solicitud elegible con tasa estándar",
  "evaluador": "admin@fondoahorro.com",
  "createdAt": "2026-04-14T11:00:00Z"
}
```

> **Seguridad:** El campo `scoreHash` contiene SHA-256 de los factores para verificación de integridad. El campo `firmaVerificable` contiene la firma RSA del cálculo.

#### Response - 422 Unprocessable Entity

```json
{
  "error": "ESTADO_INVALIDO_PARA_EVALUACION",
  "mensaje": "La solicitud SOL-CRED-2026-482917 no está en estado PENDIENTE",
  "timestamp": "2026-04-14T11:00:00Z"
}
```

---

### 7. POST /solicitudes/{numeroSolicitud}/aprobar - Aprobar Crédito

**Descripción:** Aprueba una solicitud que ha sido evaluada. Retiene el colateral si aplica.

**Roles permitidos:** `ADMIN`

#### Request

```http
POST /api/v1/creditos/solicitudes/SOL-CRED-2026-482917/aprobar
Content-Type: application/json
Authorization: Bearer <token>

{
  "comentario": "Aprobado según evaluación crediticia favorable",
  "tasaInteresOverride": null
}
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSolicitud": "SOL-CRED-2026-482917",
  "estado": "APROBADA",
  "tasaInteresAplicada": 0.1378,
  "colateralMontoRetenido": 10000.0000,
  "mensaje": "Crédito aprobado. Colateral retenido de cuenta de ahorro.",
  "fechaAprobacion": "2026-04-14T12:00:00Z"
}
```

---

### 8. POST /solicitudes/{numeroSolicitud}/rechazar - Rechazar Crédito

**Descripción:** Rechaza una solicitud con motivo documented.

**Roles permitidos:** `ADMIN`

#### Request

```http
POST /api/v1/creditos/solicitudes/SOL-CRED-2026-482917/rechazar
Content-Type: application/json
Authorization: Bearer <token>

{
  "motivo": "Score interno por debajo del umbral mínimo (45 pts)"
}
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSolicitud": "SOL-CRED-2026-482917",
  "estado": "RECHAZADA",
  "motivoRechazo": "Score interno por debajo del umbral mínimo (45 pts)",
  "fechaRechazo": "2026-04-14T12:00:00Z"
}
```

---

## Endpoints de Plan de Amortización

### 9. GET /solicitudes/{numeroSolicitud}/plan - Consultar Plan de Amortización

**Descripción:** Obtiene el plan de amortización completo con el detalle de todas las cuotas.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

#### Request

```http
GET /api/v1/creditos/solicitudes/SOL-CRED-2026-482917/plan
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "solicitudId": "550e8400-e29b-41d4-a716-446655440000",
  "montoPrincipal": 50000.0000,
  "tasaInteres": 0.1378,
  "plazoMeses": 12,
  "frecuenciaPago": "MENSUAL",
  "fechaInicio": "2026-04-15",
  "fechaFin": "2027-04-15",
  "numeroCuotas": 12,
  "cuotaMensual": 4550.0000,
  "totalIntereses": 4600.0000,
  "totalPagado": 0.0000,
  "saldoPendiente": 50000.0000,
  "estado": "ACTIVO",
  "cuotas": [
    {
      "id": "880e8400-e29b-41d4-a716-446655440001",
      "numeroCuota": 1,
      "fechaVencimiento": "2026-05-15",
      "capital": 3971.0000,
      "interes": 579.0000,
      "montoCuota": 4550.0000,
      "saldoInsoluto": 46029.0000,
      "estado": "PENDIENTE"
    },
    {
      "id": "880e8400-e29b-41d4-a716-446655440002",
      "numeroCuota": 2,
      "fechaVencimiento": "2026-06-15",
      "capital": 4016.0000,
      "interes": 534.0000,
      "montoCuota": 4550.0000,
      "saldoInsoluto": 42013.0000,
      "estado": "PENDIENTE"
    }
  ]
}
```

---

## Endpoints de Desembolso

### 10. POST /solicitudes/{numeroSolicitud}/desembolson - Desembolsar Crédito

**Descripción:** Ejecuta el desembolso de fondos al socio y actualiza el estado a DESEMBOLSADO.

**Roles permitidos:** `ADMIN`, `SISTEMA`

#### Request

```http
POST /api/v1/creditos/solicitudes/SOL-CRED-2026-482917/desembolson
Content-Type: application/json
Authorization: Bearer <token>

{
  "referenciaDesembolso": "TRANSF-2026-04-001",
  "comisionAperturaAplicada": 250.0000
}
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSolicitud": "SOL-CRED-2026-482917",
  "estado": "DESEMBOLSADO",
  "referenciaDesembolso": "TRANSF-2026-04-001",
  "montoDesembolsado": 49750.0000,
  "comisionApertura": 250.0000,
  "fechaDesembolso": "2026-04-14T14:00:00Z",
  "mensaje": "Desembolso ejecutado exitosamente a cuenta MX82FAWA0000123456789012"
}
```

#### Response - 409 Conflict

```json
{
  "error": "ESTADO_NO_PERMITE_DESEMBOLSO",
  "mensaje": "La solicitud debe estar en estado APROBADA para proceder",
  "timestamp": "2026-04-14T14:00:00Z"
}
```

---

## Endpoints de Pagos (Cuotas)

### 11. GET /creditos/{numeroSolicitud}/cuotas - Listar Cuotas

**Descripción:** Lista todas las cuotas del plan de amortización con su estado actual.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

#### Request

```http
GET /api/v1/creditos/SOL-CRED-2026-482917/cuotas?page=0&size=12&estado=PENDIENTE
Authorization: Bearer <token>
```

#### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| page | Integer | No | 0 | Número de página |
| size | Integer | No | 12 | Tamaño de página (máx 24) |
| estado | Enum | No | null | Filtrar por estado |

#### Response - 200 OK

```json
{
  "numeroSolicitud": "SOL-CRED-2026-482917",
  "planId": "770e8400-e29b-41d4-a716-446655440000",
  "pagina": 0,
  "tamanio": 12,
  "totalElementos": 12,
  "cuotas": [
    {
      "id": "880e8400-e29b-41d4-a716-446655440001",
      "numeroCuota": 1,
      "fechaVencimiento": "2026-05-15",
      "fechaPago": null,
      "capital": 3971.0000,
      "interes": 579.0000,
      "montoCuota": 4550.0000,
      "saldoInsoluto": 46029.0000,
      "estado": "PENDIENTE",
      "diasMora": 0
    },
    {
      "id": "880e8400-e29b-41d4-a716-446655440002",
      "numeroCuota": 2,
      "fechaVencimiento": "2026-06-15",
      "fechaPago": null,
      "capital": 4016.0000,
      "interes": 534.0000,
      "montoCuota": 4550.0000,
      "saldoInsoluto": 42013.0000,
      "estado": "VENCIDA",
      "diasMora": 5,
      "interesMora": 11.3750
    }
  ]
}
```

---

### 12. POST /creditos/cuotas/{cuotaId}/pago - Registrar Pago de Cuota

**Descripción:** Registra el pago de una cuota específica. Implementa idempotencia y optimistic locking para prevenir double-payment.

**Roles permitidos:** `CAJERO`, `ADMIN`, `SOCIO` (auto-pago)

#### Request

```http
POST /api/v1/creditos/cuotas/880e8400-e29b-41d4-a716-446655440002/pago
Content-Type: application/json
Authorization: Bearer <token>

{
  "monto": 4561.3750,
  "referenciaPago": "PAGO-2026-04-001",
  "canalOrigen": "SUCURSAL"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| monto | BigDecimal | Sí | >= 0.0001 | Monto del pago |
| referenciaPago | String | No | max 100 chars | Clave de idempotencia |
| canalOrigen | Enum | Sí | SUCURSAL, WEB, MOBILE, ATM | Canal del pago |

#### Response - 200 OK

```json
{
  "id": "880e8400-e29b-41d4-a716-446655440002",
  "numeroCuota": 2,
  "estado": "PAGADA",
  "montoPagado": 4561.3750,
  "fechaPago": "2026-04-20",
  "referenciaPago": "PAGO-2026-04-001",
  "saldoInsolutoRestante": 37997.6250,
  "mensaje": "Pago registrado exitosamente"
}
```

#### Response - 409 Conflict (Double Payment Prevention)

```json
{
  "error": "CUOTA_YA_PAGADA",
  "mensaje": "La cuota ya fue pagada. No se puede procesar doble cobro.",
  "timestamp": "2026-04-14T15:00:00Z"
}
```

#### Response - 409 Conflict (Optimistic Lock)

```json
{
  "error": "CONCURRENCY_CONFLICT",
  "mensaje": "La cuota fue modificada por otro proceso. Por favor reintente.",
  "timestamp": "2026-04-14T15:00:00Z"
}
```

> **Corrección de Seguridad:** El campo `@Version` en `Amortizacion` previene pagos concurrentes. Si la versión no coincide, se lanza `OptimisticLockException`.

---

### 13. GET /creditos/{numeroSolicitud} - Consultar Estado de Crédito

**Descripción:** Obtiene el estado completo de un crédito incluyendo plan de amortización y progreso de pagos.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

#### Request

```http
GET /api/v1/creditos/SOL-CRED-2026-482917
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSolicitud": "SOL-CRED-2026-482917",
  "socioId": 12345,
  "tipoCredito": {
    "id": 1,
    "nombre": "Crédito Vehículo"
  },
  "montoSolicitado": 50000.0000,
  "plazoMeses": 12,
  "tasaInteresAplicada": 0.1378,
  "estado": "DESEMBOLSADO",
  "colateralMontoRetenido": 10000.0000,
  "referenciaDesembolso": "TRANSF-2026-04-001",
  "fechaDesembolso": "2026-04-14T14:00:00Z",
  "plan": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "numeroCuotas": 12,
    "cuotaMensual": 4550.0000,
    "totalPagado": 4550.0000,
    "saldoPendiente": 45450.0000,
    "proximoVencimiento": "2026-06-15",
    "estado": "ACTIVO"
  },
  "resumen": {
    "cuotasPagadas": 1,
    "cuotasPendientes": 11,
    "cuotasVencidas": 0,
    "totalIntereses": 4600.0000,
    "totalPagadoIntereses": 579.0000
  }
}
```

> **Corrección de Seguridad:** Validación IDOR aplicada - socio solo puede ver sus propios créditos.

---

## Endpoint de Simulación

### 14. POST /simulador - Simular Crédito

**Descripción:** Simula las condiciones de un crédito sin crear solicitud. **Requiere autenticación ligera + rate limiting.**

**Roles permitidos:** `SOCIO`, `ADMIN` (o header `X-Api-Key` para público limitado)

#### Request

```http
POST /api/v1/simulador
Content-Type: application/json
X-Forwarded-For: 192.168.1.100

{
  "monto": 50000.0000,
  "plazoMeses": 12,
  "tasa": 0.1450
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| monto | BigDecimal | Sí | >= 1000, <= 5000000 | Monto a simular |
| plazoMeses | Integer | Sí | 1-360 | Plazo en meses |
| tasa | BigDecimal | Sí | > 0, <= 1.0 | Tasa de interés anual |

#### Response - 200 OK

```json
{
  "monto": 50000.0000,
  "plazoMeses": 12,
  "tasaInteresAnual": 0.1450,
  "cuotaMensual": 4550.0000,
  "totalIntereses": 4600.0000,
  "totalAPagar": 54600.0000,
  "planSimulado": [
    {
      "numeroCuota": 1,
      "fechaVencimiento": "2026-05-15",
      "capital": 3971.0000,
      "interes": 579.0000,
      "montoCuota": 4550.0000,
      "saldoInsoluto": 46029.0000
    },
    {
      "numeroCuota": 2,
      "fechaVencimiento": "2026-06-15",
      "capital": 4016.0000,
      "interes": 534.0000,
      "montoCuota": 4550.0000,
      "saldoInsoluto": 42013.0000
    }
  ],
  "nota": "Simulación sin compromiso. Tasa final depende de evaluación crediticia."
}
```

#### Response - 429 Too Many Requests

```json
{
  "error": "RATE_LIMIT_EXCEEDED",
  "mensaje": "Ha excedido el límite de simulaciones. Máximo 10 por minuto.",
  "retryAfter": 60,
  "timestamp": "2026-04-14T10:30:00Z"
}
```

> **Corrección de Seguridad:** Rate limiting de 10 solicitudes por IP por minuto. Registro de auditoría en cada acceso.

---

## Códigos de Error

| Código HTTP | Error Code | Descripción |
|-------------|------------|-------------|
| 400 | VALIDATION_ERROR | Datos de entrada inválidos |
| 400 | MONTO_EXCEDE_LIMITE | Monto supera límite del tipo de crédito |
| 400 | PLAZO_INVALIDO | Plazo fuera de rango permitido |
| 401 | NO_AUTORIZADO | Token inválido o expirado |
| 403 | ACCESO_NO_AUTORIZADO | IDOR - Sin acceso a la solicitud |
| 404 | SOLICITUD_NO_ENCONTRADA | Solicitud no existe |
| 404 | TIPO_CREDITO_NO_ENCONTRADO | Tipo de crédito no existe |
| 404 | CUOTA_NO_ENCONTRADA | Cuota no existe |
| 409 | CREDITO_ACTIVO_EXISTENTE | Socio ya tiene crédito activo |
| 409 | ESTADO_NO_PERMITE_DESEMBOLSO | Estado no permite desembolso |
| 409 | CUOTA_YA_PAGADA | Double-payment prevention |
| 409 | CONCURRENCY_CONFLICT | Optimistic lock conflict |
| 422 | ESTADO_INVALIDO_PARA_EVALUACION | Solicitud no está en estado PENDIENTE |
| 422 | COLATERAL_INSUFICIENTE | Saldo en cuenta colateral insuficiente |
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
| `/simulador` | 10 requests/min | Por IP |
| `/cuotas/{id}/pago` | 30 requests/min | Por endpoint |
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
| 1.0.1 | 2026-04-14 | @documentador | Integración correcciones auditoría seguridad |

---

## Referencias

- Especificación técnica: `/docs/modulos/creditos/SPEC.md`
- Modelo de datos: `/docs/modulos/creditos/MODELO_DATOS.md`
- Sistema de pagos: `/docs/modulos/creditos/PAGOS.md`
- Auditoría de seguridad: `/docs/auditorias/ULTIMA_AUDITORIA.md`