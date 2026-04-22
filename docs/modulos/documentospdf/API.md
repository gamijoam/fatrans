# Módulo DOCUMENTOS PDF - Referencia de API

**Proyecto:** Plataforma Fondo de Ahorro
**Versión:** 1.0
**Fecha:** 2026-04-19

---

## Resumen

Documentación completa de los **9 endpoints** del módulo DOCUMENTOS PDF con matrices de permisos, ejemplos de request/response, códigos de error y notas de seguridad.

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

---

## Matriz de Permisos por Rol

| Documento | SOCIO | ADMIN | CAJERO | SISTEMA |
|-----------|:-----:|:-----:|:------:|:-------:|
| **Estado Cuenta** | Propio | Todos | No | No |
| **Constancia Afiliación** | Propio | Todos | No | No |
| **Contrato Adhesión** | No | Todos | No | Sí (auto) |
| **Pagaré** | No | Todos | No | Sí (auto) |
| **Tabla Amortización** | Propio | Todos | No | Sí (auto) |
| **Carta Beneficiarios** | Propio | Todos | No | No |
| **Descargar cualquier documento** | No | Todos | No | No |

---

## Endpoints del Módulo DOCUMENTOS PDF

### 1. GET /documentos/estado-cuenta/{cuentaId} - Generar Estado de Cuenta

**Descripción:** Genera un PDF con el estado de cuenta mensual de la cuenta de ahorros especificada.

**Roles permitidos:** `SOCIO` (solo propia cuenta), `ADMIN`

**Validación IDOR:** El socio autenticado solo puede generar su propio estado de cuenta.

#### Request

```http
GET /api/v1/documentos/estado-cuenta/550e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>
```

#### Request Headers

| Header | Requerido | Descripción |
|--------|-----------|-------------|
| Authorization | Sí | Bearer {jwt_token} |
| X-Request-Id | No | ID de correlación |

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440001",
  "tipo": "ESTADO_CUENTA",
  "nombreArchivo": "EstadoCuenta_2026-04_550e8400.pdf",
  "estado": "ALMACENADO",
  "tamanoBytes": 245678,
  "hashArchivo": "SHA-256:a1b2c3d4e5f6...",
  "clasificacion": "CONFIDENCIAL",
  "preSignedUrl": "https://minio.fondoahorro.com/bucket-documentos/...",
  "urlExpiraEn": 900,
  "fechaGeneracion": "2026-04-19T14:30:00Z",
  "fechaExpiracion": "2026-04-26T14:30:00Z"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Cuenta no encontrada |
| DOC_007 | 403 | Acceso no autorizado (violación IDOR) |
| DOC_010 | 429 | Rate limit excedido (5 req/min) |
| DOC_004 | 500 | Error al generar PDF |
| DOC_006 | 500 | PDF detectado como malicioso |

---

### 2. GET /documentos/constancia-afiliacion/{socioId} - Generar Constancia de Afiliación

**Descripción:** Genera una constancia de afiliación al Fondo de Ahorro.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

**Validación IDOR:** El socio autenticado solo puede generar su propia constancia.

#### Request

```http
GET /api/v1/documentos/constancia-afiliacion/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440002",
  "tipo": "CONSTANCIA_AFILIACION",
  "nombreArchivo": "ConstanciaAfiliacion_550e8400.pdf",
  "estado": "ALMACENADO",
  "tamanoBytes": 156234,
  "hashArchivo": "SHA-256:b2c3d4e5f6g7...",
  "clasificacion": "PUBLICO",
  "preSignedUrl": "https://minio.fondoahorro.com/bucket-documentos/...",
  "urlExpiraEn": 900,
  "fechaGeneracion": "2026-04-19T14:35:00Z",
  "fechaExpiracion": "2026-04-26T14:35:00Z"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Socio no encontrado |
| DOC_007 | 403 | Acceso no autorizado (violación IDOR) |
| DOC_010 | 429 | Rate limit excedido |

---

### 3. GET /documentos/contrato/{solicitudId} - Generar Contrato de Adhesión

**Descripción:** Genera el contrato de adhesión con firma digital RSA SHA-256.

**Roles permitidos:** `ADMIN`, `SISTEMA`

**Requisito de seguridad:** Este documento es firmado digitalmente.

#### Request

```http
GET /api/v1/documentos/contrato/550e8400-e29b-41d4-a716-446655440099
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440003",
  "tipo": "CONTRATO_ADHESION",
  "nombreArchivo": "ContratoAdhesion_550e8400.pdf",
  "estado": "ALMACENADO",
  "tamanoBytes": 312456,
  "hashArchivo": "SHA-256:c3d4e5f6g7h8...",
  "clasificacion": "RESTRINGIDO",
  "firmaDigital": "RSA-SHA256:...",
  "preSignedUrl": "https://minio.fondoahorro.com/bucket-contratos/...",
  "urlExpiraEn": 900,
  "fechaGeneracion": "2026-04-19T14:40:00Z",
  "fechaExpiracion": null
}
```

**Nota:** Los contratos no expiran (fechaExpiracion = null).

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Solicitud no encontrada |
| DOC_005 | 500 | Error en firma digital |
| DOC_007 | 403 | Acceso no autorizado (rol inválido) |

---

### 4. GET /documentos/pagare/{creditoId} - Generar Pagaré

**Descripción:** Genera el pagaré del crédito con firma digital RSA SHA-256.

**Roles permitidos:** `ADMIN`, `SISTEMA`

**Requisito de seguridad:** Este documento es firmado digitalmente.

#### Request

```http
GET /api/v1/documentos/pagare/550e8400-e29b-41d4-a716-446655440088
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440004",
  "tipo": "PAGARE",
  "nombreArchivo": "Pagare_Credito550e8400.pdf",
  "estado": "ALMACENADO",
  "tamanoBytes": 287654,
  "hashArchivo": "SHA-256:d4e5f6g7h8i9...",
  "clasificacion": "RESTRINGIDO",
  "firmaDigital": "RSA-SHA256:...",
  "preSignedUrl": "https://minio.fondoahorro.com/bucket-pagares/...",
  "urlExpiraEn": 900,
  "fechaGeneracion": "2026-04-19T14:45:00Z",
  "fechaExpiracion": null
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Crédito no encontrado |
| DOC_005 | 500 | Error en firma digital |
| DOC_007 | 403 | Acceso no autorizado (rol inválido) |

---

### 5. GET /documentos/tabla-amortizacion/{creditoId} - Generar Tabla de Amortización

**Descripción:** Genera la tabla de amortización del crédito.

**Roles permitidos:** `SOCIO` (dueño del crédito), `ADMIN`, `SISTEMA`

**Validación IDOR:** El socio solo puede ver la tabla de su propio crédito.

#### Request

```http
GET /api/v1/documentos/tabla-amortizacion/550e8400-e29b-41d4-a716-446655440088
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440005",
  "tipo": "TABLA_AMORTIZACION",
  "nombreArchivo": "TablaAmortizacion_Credito550e8400.pdf",
  "estado": "ALMACENADO",
  "tamanoBytes": 198765,
  "hashArchivo": "SHA-256:e5f6g7h8i9j0...",
  "clasificacion": "CONFIDENCIAL",
  "preSignedUrl": "https://minio.fondoahorro.com/bucket-creditos/...",
  "urlExpiraEn": 900,
  "fechaGeneracion": "2026-04-19T14:50:00Z",
  "fechaExpiracion": "2026-05-19T14:50:00Z"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Crédito no encontrado |
| DOC_007 | 403 | Acceso no autorizado (violación IDOR) |
| DOC_010 | 429 | Rate limit excedido |

---

### 6. GET /documentos/carta-beneficiarios/{socioId} - Generar Carta de Beneficiarios

**Descripción:** Genera la carta de designación de beneficiarios.

**Roles permitidos:** `SOCIO` (solo propio), `ADMIN`

**Validación IDOR:** El socio solo puede generar su propia carta.

**Requisito de negocio:** La suma de porcentajes de beneficiarios debe ser 100%.

#### Request

```http
GET /api/v1/documentos/carta-beneficiarios/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440006",
  "tipo": "CARTA_BENEFICIARIOS",
  "nombreArchivo": "CartaBeneficiarios_550e8400.pdf",
  "estado": "ALMACENADO",
  "tamanoBytes": 178934,
  "hashArchivo": "SHA-256:f6g7h8i9j0k1...",
  "clasificacion": "CONFIDENCIAL",
  "preSignedUrl": "https://minio.fondoahorro.com/bucket-documentos/...",
  "urlExpiraEn": 900,
  "fechaGeneracion": "2026-04-19T14:55:00Z",
  "fechaExpiracion": null
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Socio no encontrado |
| DOC_007 | 403 | Acceso no autorizado (violación IDOR) |
| DOC_008 | 400 | Beneficiarios no suman 100% |
| DOC_010 | 429 | Rate limit excedido |

---

### 7. GET /documentos/{documentoId} - Obtener Metadata de Documento

**Descripción:** Obtiene los metadatos de un documento generado.

**Roles permitidos:** `SOCIO` (propio), `ADMIN`

**Validación IDOR:** El socio solo puede ver sus propios documentos.

#### Request

```http
GET /api/v1/documentos/660e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440001",
  "socioId": "550e8400-e29b-41d4-a716-446655440000",
  "tipo": "ESTADO_CUENTA",
  "nombreArchivo": "EstadoCuenta_2026-04_550e8400.pdf",
  "estado": "ALMACENADO",
  "tamanoBytes": 245678,
  "hashArchivo": "SHA-256:a1b2c3d4e5f6...",
  "clasificacion": "CONFIDENCIAL",
  "generadoPor": "usuario-550e8400",
  "fechaGeneracion": "2026-04-19T14:30:00Z",
  "fechaExpiracion": "2026-04-26T14:30:00Z"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Documento no encontrado |
| DOC_002 | 410 | Documento expirado |
| DOC_003 | 403 | Documento revocado |
| DOC_007 | 403 | Acceso no autorizado |

---

### 8. GET /documentos/{documentoId}/descargar - Descargar Documento

**Descripción:** Obtiene una pre-signed URL para descargar el documento.

**Roles permitidos:** `SOCIO` (propio), `ADMIN`

**Validación IDOR:** El socio solo puede descargar sus propios documentos.

**Nota:** La pre-signed URL expira en 15 minutos (900 segundos).

#### Request

```http
GET /api/v1/documentos/660e8400-e29b-41d4-a716-446655440001/descargar
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "documentoId": "660e8400-e29b-41d4-a716-446655440001",
  "preSignedUrl": "https://minio.fondoahorro.com/bucket-documentos/...",
  "urlExpiraEn": 900,
  "fechaExpiracion": "2026-04-19T15:00:00Z"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Documento no encontrado |
| DOC_002 | 410 | Documento expirado |
| DOC_003 | 403 | Documento revocado |
| DOC_007 | 403 | Acceso no autorizado |

---

### 9. GET /documentos/socio/{socioId} - Listar Documentos de Socio

**Descripción:** Lista todos los documentos generados para un socio.

**Roles permitidos:** `SOCIO` (propio), `ADMIN`

**Validación IDOR:** El socio solo puede ver sus propios documentos.

#### Request

```http
GET /api/v1/documentos/socio/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

#### Query Parameters

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| tipo | String | null | Filtrar por tipo de documento |
| estado | String | null | Filtrar por estado |
| page | int | 0 | Número de página |
| size | int | 20 | Tamaño de página |

#### Response - 200 OK

```json
{
  "documentos": [
    {
      "documentoId": "660e8400-e29b-41d4-a716-446655440001",
      "tipo": "ESTADO_CUENTA",
      "nombreArchivo": "EstadoCuenta_2026-04_550e8400.pdf",
      "estado": "ALMACENADO",
      "clasificacion": "CONFIDENCIAL",
      "fechaGeneracion": "2026-04-19T14:30:00Z"
    },
    {
      "documentoId": "660e8400-e29b-41d4-a716-446655440002",
      "tipo": "CONSTANCIA_AFILIACION",
      "nombreArchivo": "ConstanciaAfiliacion_550e8400.pdf",
      "estado": "ALMACENADO",
      "clasificacion": "PUBLICO",
      "fechaGeneracion": "2026-04-19T14:35:00Z"
    }
  ],
  "total": 2,
  "page": 0,
  "size": 20
}
```

---

## Códigos de Error Comunes

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| DOC_001 | 404 | Documento/Cuenta/Crédito/Solicitud no encontrado |
| DOC_002 | 410 | Documento ha expirado |
| DOC_003 | 403 | Documento ha sido revocado |
| DOC_004 | 500 | Error al generar PDF |
| DOC_005 | 500 | Error en firma digital |
| DOC_006 | 500 | PDF detectado como malicioso |
| DOC_007 | 403 | Acceso no autorizado (violación IDOR) |
| DOC_008 | 400 | Tipo de documento no válido o regla de negocio fallida |
| DOC_009 | 500 | Bucket MinIO no encontrado |
| DOC_010 | 429 | Rate limit excedido |

---

## Rate Limiting

| Operación | Límite | Ventana | Por |
|-----------|--------|---------|-----|
| Generar documento | 5 req/min | 1 minuto | Usuario |
| Generar documento | 20 req/min | 1 minuto | IP |
| Descargar documento | 10 req/min | 1 minuto | Usuario |
| Listar documentos | 30 req/min | 1 minuto | Usuario |

**Response cuando se excede (429 Too Many Requests):**
```json
{
  "codigo": "DOC_010",
  "mensaje": "Rate limit excedido. Intente nuevamente en 60 segundos.",
  "retryAfter": 60
}
```

---

## Headers de Response

Todos los responses incluyen:

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| X-Request-Id | ID único de la request | `req_xyz789` |
| X-Response-Time | Tiempo de procesamiento (ms) | `145` |
| X-Correlation-Id | ID de correlación para logs | `corr_abc123` |

---

## Enums de Referencia

### TipoDocumento

| Valor | Descripción | Requiere Firma Digital |
|-------|-------------|------------------------|
| ESTADO_CUENTA | Estado de cuenta mensual | No |
| CONSTANCIA_AFILIACION | Constancia de afiliación | No |
| CONTRATO_ADHESION | Contrato de adhesión | **Sí** |
| PAGARE | Pagaré de crédito | **Sí** |
| TABLA_AMORTIZACION | Tabla de amortización | No |
| CARTA_BENEFICIARIOS | Carta de beneficiarios | No |

### EstadoDocumento

| Valor | Descripción | Puede descargar? |
|-------|-------------|------------------|
| GENERADO | PDF creado, en escaneo | No |
| ALMACENADO | PDF escaneado y en MinIO | Sí |
| EXPIRADO | PDF fuera de vigencia | No |
| REVOCADO | Revocado por ADMIN | No |

### ClasificacionDocumento

| Valor | Descripción |
|-------|-------------|
| CONFIDENCIAL | Datos financieros sensibles |
| RESTRINGIDO | Contratos y pagarés |
| PUBLICO | Constancias de afiliación |

---

## Ejemplo de Flujo Completo

### Generar y Descargar Estado de Cuenta

**Paso 1:** Generar estado de cuenta
```http
GET /api/v1/documentos/estado-cuenta/550e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>
```
Response: 200 OK con documentId y preSignedUrl

**Paso 2:** (Opcional) Obtener metadata
```http
GET /api/v1/documentos/660e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>
```
Response: 200 OK con metadatos completos

**Paso 3:** Descargar documento
```http
GET /api/v1/documentos/660e8400-e29b-41d4-a716-446655440001/descargar
Authorization: Bearer <token>
```
Response: 200 OK con preSignedUrl (válida por 15 min)

**Paso 4:** Cliente descarga directamente de MinIO usando preSignedUrl

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-19 | @product-manager | Creación inicial de la API |
| 1.0 | 2026-04-19 | @auditoria | Revisión de seguridad |
| 1.0 | 2026-04-19 | @documentador | Documentación formal de la API |

---

## Referencias

- Especificación técnica: `/docs/modulos/documentospdf/SPEC.md`
- Modelo de datos: `/docs/modulos/documentospdf/MODELO_DATOS.md`
- Módulo Socios: `/docs/modulos/socios/API.md`
- Módulo Créditos: `/docs/modulos/creditos/API.md`