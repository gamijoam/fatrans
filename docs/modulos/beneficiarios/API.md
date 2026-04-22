# Módulo BENEFICIARIOS - Referencia de API

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-19

---

## Resumen

Documentación completa de los **4 endpoints** del módulo BENEFICIARIOS con ejemplos de request/response, códigos de error y notas de seguridad.

---

## Base URL

```
Production: https://api.fondoahorro.com/v1
Development: http://localhost:8080/api/v1
```

---

## Autenticación

Todos los endpoints requieren header de autenticación JWT:

```
Authorization: Bearer <jwt_token>
```

Roles permitidos:
- `SOCIO`: Puede acceder y modificar SOLO sus propios beneficiarios
- `ADMIN`: Puede acceder a beneficiarios de cualquier socio (para auditoría)

---

## Endpoints del Módulo BENEFICIARIOS

### 1. POST /socios/{socioId}/beneficiarios - Registrar Beneficiario

**Descripción:** Registra un nuevo beneficiario para un socio.

**Roles permitidos:** `SOCIO`, `ADMIN`

**Validaciones:**
- Socio debe existir y estar activo
- Número de documento no puede ser igual al del socio titular
- Máximo 5 beneficiarios activos por socio
- Suma de porcentajes no puede exceder 100%
- Documento no puede repetirse entre beneficiarios activos del mismo socio

#### Request

```http
POST /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/beneficiarios
Content-Type: application/json
Authorization: Bearer <token>

{
  "nombreCompleto": "María Elena Pérez",
  "numeroDocumento": "V-87654321",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "parentesco": "CONYUGE",
  "porcentaje": "50.00",
  "telefono": "+58-414-5551234"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| nombreCompleto | String | Sí | Max 200 chars | Nombre completo del beneficiario |
| numeroDocumento | String | Sí | Max 20 chars, formato según tipo | Número de documento |
| tipoDocumento | Enum | Sí | CEDULA_IDENTIDAD, RIF, PASAPORTE, CEDULA_EXTRANJERO | Tipo de documento |
| parentesco | Enum | Sí | CONYUGE, HIJO, PADRE, MADRE, HERMANO, ABUELO, NIETO, SOBRINO, TIO, OTRO | Parentesco con el socio |
| porcentaje | Decimal | Sí | Min 0.01, Max 100.00, 2 decimales | Porcentaje de asignación |
| telefono | String | No | Max 20 chars, formato internacional | Teléfono de contacto |

#### Response - 201 Created

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "socioId": "550e8400-e29b-41d4-a716-446655440000",
  "nombreCompleto": "María Elena Pérez",
  "numeroDocumento": "V-87654321",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "parentesco": "CONYUGE",
  "porcentaje": 50.00,
  "telefono": "+58-414-5551234",
  "activo": true,
  "fechaRegistro": "2026-04-19T10:00:00Z",
  "fechaActualizacion": "2026-04-19T10:00:00Z"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| SOCIO_NO_ENCONTRADO | 404 | Socio no existe |
| BENEFICIARIO_DUPLICADO | 409 | Ya existe beneficiario activo con mismo documento |
| MAXIMO_BENEFICIARIOS_EXCEDIDO | 400 | Socio ya tiene 5 beneficiarios activos |
| PORCENTAJE_SUM_EXCEDIDO | 400 | Suma de porcentajes excedería 100% |
| DOCUMENTO_IGUAL_TITULAR | 400 | Documento del beneficiario igual al del socio |
| VALIDATION_ERROR | 400 | Datos inválidos |

---

### 2. GET /socios/{socioId}/beneficiarios - Listar Beneficiarios

**Descripción:** Lista todos los beneficiarios activos de un socio.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

**Validación IDOR:** El socio autenticado solo puede ver sus propios beneficiarios.

#### Request

```http
GET /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/beneficiarios
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "beneficiarios": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "socioId": "550e8400-e29b-41d4-a716-446655440000",
      "nombreCompleto": "María Elena Pérez",
      "numeroDocumento": "V-87654321",
      "tipoDocumento": "CEDULA_IDENTIDAD",
      "parentesco": "CONYUGE",
      "porcentaje": 50.00,
      "telefono": "+58-414-5551234",
      "activo": true,
      "fechaRegistro": "2026-04-19T10:00:00Z",
      "fechaActualizacion": "2026-04-19T10:00:00Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "socioId": "550e8400-e29b-41d4-a716-446655440000",
      "nombreCompleto": "Juan Carlos Pérez",
      "numeroDocumento": "V-11223344",
      "tipoDocumento": "CEDULA_IDENTIDAD",
      "parentesco": "HIJO",
      "porcentaje": 50.00,
      "telefono": "+58-412-1234567",
      "activo": true,
      "fechaRegistro": "2026-04-19T10:05:00Z",
      "fechaActualizacion": "2026-04-19T10:05:00Z"
    }
  ],
  "total": 2,
  "sumaPorcentajes": 100.00
}
```

**Nota:** El campo `sumaPorcentajes` muestra la suma total de porcentajes de los beneficiarios activos (debe ser exactamente 100.00).

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| SOCIO_NO_ENCONTRADO | 404 | Socio no existe |

---

### 3. PUT /socios/{socioId}/beneficiarios/{id} - Actualizar Beneficiario

**Descripción:** Actualiza los datos de un beneficiario existente.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

**Validaciones IDOR:**
- Beneficiario debe existir
- Beneficiario debe pertenecer al socio especificado en la URL
- Beneficiario debe estar activo

**Validaciones de negocio:**
- Si cambia número de documento: no puede ser igual al del socio titular
- Si cambia documento: no puede repetirse con otro beneficiario activo del mismo socio
- La suma de porcentajes no puede exceder 100%

#### Request

```http
PUT /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/beneficiarios/660e8400-e29b-41d4-a716-446655440001
Content-Type: application/json
Authorization: Bearer <token>

{
  "nombreCompleto": "María Elena García de Pérez",
  "numeroDocumento": "V-87654321",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "parentesco": "CONYUGE",
  "porcentaje": "60.00",
  "telefono": "+58-414-5555678"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| nombreCompleto | String | Sí | Max 200 chars | Nombre completo |
| numeroDocumento | String | Sí | Max 20 chars | Número de documento |
| tipoDocumento | Enum | Sí | CEDULA_IDENTIDAD, RIF, PASAPORTE, CEDULA_EXTRANJERO | Tipo de documento |
| parentesco | Enum | Sí | CONYUGE, HIJO, PADRE, MADRE, HERMANO, ABUELO, NIETO, SOBRINO, TIO, OTRO | Parentesco |
| porcentaje | Decimal | Sí | Min 0.01, Max 100.00, 2 decimales | Porcentaje de asignación |
| telefono | String | No | Max 20 chars | Teléfono de contacto |

#### Response - 200 OK

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "socioId": "550e8400-e29b-41d4-a716-446655440000",
  "nombreCompleto": "María Elena García de Pérez",
  "numeroDocumento": "V-87654321",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "parentesco": "CONYUGE",
  "porcentaje": 60.00,
  "telefono": "+58-414-5555678",
  "activo": true,
  "fechaRegistro": "2026-04-19T10:00:00Z",
  "fechaActualizacion": "2026-04-19T11:30:00Z",
  "mensaje": "Beneficiario actualizado exitosamente"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| BENEFICIARIO_NO_ENCONTRADO | 404 | Beneficiario no existe o no pertenece al socio |
| BENEFICIARIO_DUPLICADO | 409 | Documento ya existe en otro beneficiario activo |
| PORCENTAJE_SUM_EXCEDIDO | 400 | Suma de porcentajes excedería 100% |
| DOCUMENTO_IGUAL_TITULAR | 400 | Documento del beneficiario igual al del socio |
| VALIDATION_ERROR | 400 | Datos inválidos |

---

### 4. DELETE /socios/{socioId}/beneficiarios/{id} - Eliminar Beneficiario

**Descripción:** Elimina un beneficiario (soft delete). El beneficiario no se elimina físicamente, solo se marca como inactivo.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

**Validaciones IDOR:**
- Beneficiario debe existir
- Beneficiario debe pertenecer al socio especificado en la URL
- Beneficiario debe estar activo

**Nota importante:** Después de eliminar, la suma de porcentajes de los beneficiarios restantes debe ser recalculada. Si queda en un valor distinto a 100%, se devolverá un warning en la respuesta.

#### Request

```http
DELETE /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/beneficiarios/660e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "socioId": "550e8400-e29b-41d4-a716-446655440000",
  "activo": false,
  "mensaje": "Beneficiario eliminado exitosamente",
  "sumaPorcentajesRestantes": 50.00,
  "warning": "La suma de porcentajes restantes es 50.00%. Considere agregar o redistribuir beneficiarios."
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| BENEFICIARIO_NO_ENCONTRADO | 404 | Beneficiario no existe o no pertenece al socio |

---

## Códigos de Error Comunes

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| BENEFICIARIO_NO_ENCONTRADO | 404 | Beneficiario con ID especificado no existe |
| BENEFICIARIO_DUPLICADO | 409 | Ya existe beneficiario activo con mismo documento |
| SOCIO_NO_ENCONTRADO | 404 | Socio con ID especificado no existe |
| PORCENTAJE_INVALIDO | 400 | Porcentaje fuera de rango (0.01 - 100.00) |
| PORCENTAJE_SUM_EXCEDIDO | 400 | Suma de porcentajes excedería 100% |
| MAXIMO_BENEFICIARIOS_EXCEDIDO | 400 | Socio ya tiene 5 beneficiarios activos |
| DOCUMENTO_IGUAL_TITULAR | 400 | Documento del beneficiario igual al del socio |
| RATE_LIMIT_EXCEDIDO | 429 | Demasiadas solicitudes |

---

## Rate Limiting

| Endpoint | Límite | Ventana |
|----------|--------|---------|
| POST /socios/{socioId}/beneficiarios | 10 req/min | Por socio |
| PUT /socios/{socioId}/beneficiarios/{id} | 10 req/min | Por socio |
| DELETE /socios/{socioId}/beneficiarios/{id} | 10 req/min | Por socio |
| GET /socios/{socioId}/beneficiarios | 30 req/min | Por socio |

**Response cuando se excede (429 Too Many Requests):**
```json
{
  "codigo": "RATE_LIMIT_EXCEDIDO",
  "mensaje": "Demasiadas solicitudes. Intente nuevamente en 60 segundos.",
  "retryAfter": 60
}
```

---

## Headers de Response

Todos los responses incluyen:

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| X-Request-Id | ID único de la request | `req_xyz789` |
| X-Response-Time | Tiempo de procesamiento (ms) | `45` |
| X-Correlation-Id | ID de correlación para logs | `corr_abc123` |

---

## Enums de Referencia

### TipoDocumento

| Valor | Descripción |
|-------|-------------|
| CEDULA_IDENTIDAD | Cédula de identidad venezolana |
| RIF | Registro de Información Fiscal |
| PASAPORTE | Pasaporte |
| CEDULA_EXTRANJERO | Cédula de extranjero |

### Parentesco

| Valor | Descripción |
|-------|-------------|
| CONYUGE | Cónyuge |
| HIJO | Hijo/a |
| PADRE | Padre |
| MADRE | Madre |
| HERMANO | Hermano/a |
| ABUELO | Abuelo/a |
| NIETO | Nieto/a |
| SOBRINO | Sobrino/a |
| TIO | Tío/a |
| OTRO | Otro parentesco |

---

## Ejemplo de Flujo Completo

### Registrar Beneficiarios para Alcanzar 100%

**Paso 1:** Registrar primer beneficiario (50%)
```http
POST /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/beneficiarios
{
  "nombreCompleto": "María Elena Pérez",
  "numeroDocumento": "V-87654321",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "parentesco": "CONYUGE",
  "porcentaje": "50.00"
}
```
Response: 201 Created

**Paso 2:** Registrar segundo beneficiario (50%)
```http
POST /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/beneficiarios
{
  "nombreCompleto": "Juan Carlos Pérez",
  "numeroDocumento": "V-11223344",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "parentesco": "HIJO",
  "porcentaje": "50.00"
}
```
Response: 201 Created

**Paso 3:** Listar beneficiarios
```http
GET /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/beneficiarios
```
Response:
```json
{
  "beneficiarios": [...],
  "total": 2,
  "sumaPorcentajes": 100.00
}
```

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-19 | @product-manager | Creación inicial de la API |
| 1.0 | 2026-04-19 | @documentador | Documentación formal de la API |

---

## Referencias

- Especificación técnica: `/docs/modulos/beneficiarios/SPEC.md`
- Modelo de datos: `/docs/modulos/beneficiarios/MODELO_DATOS.md`
- Módulo Socios: `/docs/modulos/socios/API.md`