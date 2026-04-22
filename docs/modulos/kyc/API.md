# Módulo KYC Simplificado - Referencia de API

## Resumen

Documentación completa de los **12 endpoints + 1 endpoint adicional** del módulo KYC Simplificado con ejemplos de request/response, códigos de error y notas de seguridad. La API está diseñada para ser extensible hacia futuras integraciones SAIME/SENIAT.

**Estado de Implementación:** ✅ IMPLEMENTADO (Todos los endpoints operativos)

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

Los tokens JWT deben incluir:
- `socioId`: ID del socio autenticado
- `roles`: Array de roles (`SOCIO`, `ADMIN`, `ANALISTA_KYC`)
- `sessionId`: ID de sesión para auditoría

---

## Endpoints del Módulo KYC

### 1. POST /kyc/iniciar - Iniciar Proceso KYC

**Descripción:** Inicia un nuevo proceso de verificación KYC para el socio autenticado.

**Roles permitidos:** `SOCIO`

#### Request

```http
POST /api/v1/kyc/iniciar
Content-Type: application/json
Authorization: Bearer <token>

{
  "nivel": "BASICO",
  "consentimientoAceptado": true,
  "versionPolitica": "v1.0-20260401",
  "ipCliente": "190.234.123.45",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| nivel | Enum | Sí | BASICO, MEDIO, COMPLETO | Nivel de verificación |
| consentimientoAceptado | Boolean | Sí | true | Debe ser true para continuar |
| versionPolitica | String | Sí | Lista blanca: "1.0", "2.0", "2.1" | Versión de política aceptada |
| ipCliente | String | No | IP válida (IPv4/IPv6) | IP del cliente |
| userAgent | String | No | Max 500 chars | User agent del navegador |

#### Response - 201 Created

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "nivel": "BASICO",
  "estado": "PENDIENTE",
  "documentosRequeridos": [
    "CEDULA_ANVERSO",
    "CEDULA_REVERSO",
    "SELFIE_CEDULA",
    "COMPROBANTE_DOMICILIO"
  ],
  "mensaje": "Proceso KYC iniciado. Por favor suba los documentos requeridos."
}
```

#### Response - 409 Conflict (KYC Ya Existe)

```json
{
  "error": "KYC_001",
  "mensaje": "El socio ya tiene un proceso KYC activo",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 2. GET /kyc/estado - Consultar Estado KYC

**Descripción:** Obtiene el estado actual del KYC del socio autenticado.

**Roles permitidos:** `SOCIO`

#### Request

```http
GET /api/v1/kyc/estado
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "socioId": "330e8400-e29b-41d4-a716-446655440004",
  "nivel": "BASICO",
  "estado": "PENDIENTE",
  "descripcionEstado": "Documentos enviados, esperando validación",
  "fechaInicio": "2026-04-14T10:30:00Z",
  "fechaExpiracion": "2028-04-14T10:30:00Z",
  "diasRestantes": 730,
  "documentosRequeridos": 4,
  "documentosValidos": 2,
  "documentos": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "tipo": "CEDULA_ANVERSO",
      "descripcion": "Cédula de Identidad - Anverso",
      "estado": "VALIDADO",
      "nombreOriginal": "cedula_frente.jpg",
      "fechaSubida": "2026-04-14T10:35:00Z",
      "motivoRechazo": null
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "tipo": "CEDULA_REVERSO",
      "descripcion": "Cédula de Identidad - Reverso",
      "estado": "VALIDADO",
      "nombreOriginal": "cedula_atras.jpg",
      "fechaSubida": "2026-04-14T10:36:00Z",
      "motivoRechazo": null
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440003",
      "tipo": "SELFIE_CEDULA",
      "descripcion": "Selfie con Cédula",
      "estado": "PENDIENTE",
      "nombreOriginal": "selfie.jpg",
      "fechaSubida": "2026-04-14T10:37:00Z",
      "motivoRechazo": null
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440004",
      "tipo": "COMPROBANTE_DOMICILIO",
      "descripcion": "Comprobante de Domicilio",
      "estado": "PENDIENTE",
      "nombreOriginal": null,
      "fechaSubida": null,
      "motivoRechazo": null
    }
  ],
  "comentarioRevision": null,
  "motivoRechazo": null
}
```

#### Response - 404 Not Found

```json
{
  "error": "KYC_005",
  "mensaje": "No se encontró KYC activo para este socio",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 3. POST /kyc/documentos - Subir Documento

**Descripción:** Sube un documento de identidad para el proceso KYC.

**Roles permitidos:** `SOCIO`

#### Request

```http
POST /api/v1/kyc/documentos
Content-Type: application/json
Authorization: Bearer <token>

{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "tipoDocumento": "SELFIE_CEDULA",
  "archivoBase64": "BASE64_ENCODED_FILE_DATA...",
  "nombreOriginal": "selfie_con_cedula.jpg",
  "tamanoBytes": 2457600,
  "mimeType": "image/jpeg",
  "fechaExpiracionDocumento": null
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| verificacionId | UUID | Sí | Valido UUID | ID de la verificación |
| tipoDocumento | Enum | Sí | CEDULA_ANVERSO, CEDULA_REVERSO, SELFIE_CEDULA, etc. | Tipo de documento |
| archivoBase64 | String | Sí | Max 10MB decodificado | Contenido del archivo en Base64 |
| nombreOriginal | String | Sí | Max 255 chars | Nombre original del archivo |
| tamanoBytes | Long | Sí | Max 10MB (10,485,760 bytes) | Tamaño del archivo en bytes |
| mimeType | String | Sí | image/jpeg, image/png, application/pdf | Tipo MIME del archivo |
| fechaExpiracionDocumento | LocalDate | No | Fecha futura o null | Fecha de expiración del documento |

#### Response - 201 Created

```json
{
  "documentoId": "770e8400-e29b-41d4-a716-446655440001",
  "tipoDocumento": "SELFIE_CEDULA",
  "nombreOriginal": "selfie_con_cedula.jpg",
  "estado": "PENDIENTE",
  "mensaje": "Documento subido exitosamente"
}
```

#### Response - 400 Bad Request (Formato Inválido)

```json
{
  "error": "KYC_002",
  "mensaje": "Formato no permitido. Use JPEG, PNG o PDF.",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

#### Response - 400 Bad Request (Tamaño Excedido)

```json
{
  "error": "KYC_002",
  "mensaje": "El archivo excede el tamaño máximo de 10MB",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 4. DELETE /kyc/documentos/{documentoId} - Eliminar Documento

**Descripción:** Elimina un documento que aún no ha sido enviado para validación.

**Roles permitidos:** `SOCIO`

#### Request

```http
DELETE /api/v1/kyc/documentos/770e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "eliminado": true,
  "mensaje": "Documento eliminado exitosamente"
}
```

#### Response - 409 Conflict

```json
{
  "error": "KYC_007",
  "mensaje": "El documento no puede eliminarse en estado VALIDADO",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 5. POST /kyc/enviar - Enviar Documentos para Revisión

**Descripción:** Envía todos los documentos subidos para revisión por un analista.

**Roles permitidos:** `SOCIO`

#### Request

```http
POST /api/v1/kyc/enviar
Content-Type: application/json
Authorization: Bearer <token>

{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001"
}
```

#### Response - 200 OK

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "estado": "EN_REVISION",
  "documentosEnviados": 4,
  "mensaje": "Documentos enviados para revisión. Se le notificará cuando estén listos."
}
```

#### Response - 422 Unprocessable Entity (Documentos Incompletos)

```json
{
  "error": "KYC_003",
  "mensaje": "Faltan documentos. Requeridos: 4, Subidos: 2",
  "timestamp": "2026-04-14T10:30:00Z"
}
```

---

### 6. GET /kyc/cola-revision - Cola de Revisión (Analista)

**Descripción:** Obtiene la lista de verificaciones pendientes de revisión.

**Roles permitidos:** `ANALISTA_KYC`, `ADMIN`

#### Request

```http
GET /api/v1/kyc/cola-revision?page=0&size=10
Authorization: Bearer <token>
```

#### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| page | Integer | No | 0 | Número de página |
| size | Integer | No | 10 | Tamaño de página (máx 50) |
| nivel | Enum | No | null | Filtrar por nivel |
| estado | Enum | No | EN_REVISION | Filtrar por estado |

#### Response - 200 OK

```json
{
  "pagina": 0,
  "tamanio": 10,
  "totalElementos": 25,
  "totalPaginas": 3,
  "cola": [
    {
      "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
      "socioId": "330e8400-e29b-41d4-a716-446655440004",
      "nivel": "BASICO",
      "estado": "EN_REVISION",
      "fechaEnvio": "2026-04-14T10:40:00Z",
      "tiempoEnCola": "2 horas",
      "documentos": [
        {
          "tipo": "CEDULA_ANVERSO",
          "estado": "PENDIENTE",
          "nombreOriginal": "cedula_frente.jpg"
        },
        {
          "tipo": "CEDULA_REVERSO",
          "estado": "PENDIENTE",
          "nombreOriginal": "cedula_atras.jpg"
        }
      ]
    }
  ]
}
```

---

### 7. GET /kyc/revision/{verificacionId} - Detalle de Revisión

**Descripción:** Obtiene el detalle completo de una verificación para revisión del analista.

**Roles permitidos:** `ANALISTA_KYC`, `ADMIN`

#### Request

```http
GET /api/v1/kyc/revision/550e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "socioId": "330e8400-e29b-41d4-a716-446655440004",
  "nivel": "BASICO",
  "estado": "EN_REVISION",
  "fechaInicio": "2026-04-14T10:30:00Z",
  "fechaEnvio": "2026-04-14T10:40:00Z",
  "documentos": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "tipo": "CEDULA_ANVERSO",
      "descripcion": "Cédula de Identidad - Anverso",
      "estado": "PENDIENTE",
      "urlVisualizacion": "https://storage.fondoahorro.com/kyc/.../cedula_frente.jpg?token=...",
      "nombreOriginal": "cedula_frente.jpg",
      "tamanoBytes": 2457600,
      "fechaSubida": "2026-04-14T10:35:00Z",
      "metadatosValidacion": null
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "tipo": "CEDULA_REVERSO",
      "descripcion": "Cédula de Identidad - Reverso",
      "estado": "PENDIENTE",
      "urlVisualizacion": "https://storage.fondoahorro.com/kyc/.../cedula_atras.jpg?token=...",
      "nombreOriginal": "cedula_atras.jpg",
      "tamanoBytes": 2512000,
      "fechaSubida": "2026-04-14T10:36:00Z",
      "metadatosValidacion": null
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440003",
      "tipo": "SELFIE_CEDULA",
      "descripcion": "Selfie con Cédula",
      "estado": "PENDIENTE",
      "urlVisualizacion": "https://storage.fondoahorro.com/kyc/.../selfie.jpg?token=...",
      "nombreOriginal": "selfie.jpg",
      "tamanoBytes": 1894400,
      "fechaSubida": "2026-04-14T10:37:00Z",
      "metadatosValidacion": null
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440004",
      "tipo": "COMPROBANTE_DOMICILIO",
      "descripcion": "Comprobante de Domicilio",
      "estado": "PENDIENTE",
      "urlVisualizacion": "https://storage.fondoahorro.com/kyc/.../comprobante.pdf?token=...",
      "nombreOriginal": "comprobante_servicio.pdf",
      "tamanoBytes": 524288,
      "fechaSubida": "2026-04-14T10:38:00Z",
      "metadatosValidacion": null
    }
  ],
  "consentimiento": {
    "aceptado": true,
    "fechaConsentimiento": "2026-04-14T10:30:00Z"
    // NOTA: versionPolitica e ipCliente eliminados por seguridad (A6)
  }
}
```

---

### 8. POST /kyc/revision/{verificacionId}/aprobar - Aprobar Verificación

**Descripción:** Aprueba una verificación KYC después de revisar los documentos.

**Roles permitidos:** `ANALISTA_KYC`, `ADMIN`

#### Request

```http
POST /api/v1/kyc/revision/550e8400-e29b-41d4-a716-446655440001/aprobar
Content-Type: application/json
Authorization: Bearer <token>

{
  "comentario": "Documentos verificados y válidos. Socio aprobado."
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| comentario | String | No | Max 1000 chars | Comentario del analista |

#### Response - 200 OK

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "estadoAnterior": "EN_REVISION",
  "estadoNuevo": "APROBADO",
  "mensaje": "Su verificación KYC ha sido aprobada."
}
```

---

### 9. POST /kyc/revision/{verificacionId}/rechazar - Rechazar Verificación

**Descripción:** Rechaza una verificación KYC con motivo documentado.

**Roles permitidos:** `ANALISTA_KYC`, `ADMIN`

#### Request

```http
POST /api/v1/kyc/revision/550e8400-e29b-41d4-a716-446655440001/rechazar
Content-Type: application/json
Authorization: Bearer <token>

{
  "comentario": "La foto del reverso de la cédula no es legible. Por favor tome una nueva foto con mejor iluminación.",
  "documentosRechazados": [
    "660e8400-e29b-41d4-a716-446655440002"
  ]
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| comentario | String | Sí | Min 10 chars, Max 1000 chars | Motivo del rechazo |
| documentosRechazados | Array[UUID] | No | IDs de documentos específicos | IDs de los documentos con problemas |

#### Response - 200 OK

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "estadoAnterior": "EN_REVISION",
  "estadoNuevo": "RECHAZADO",
  "mensaje": "Su verificación KYC ha sido rechazada. Revise el motivo e intente nuevamente."
}
```

---

### 10. POST /kyc/revision/{verificacionId}/solicitar-info - Solicitar Más Información

**Descripción:** Solicita documentos o información adicional sin rechazar la verificación.

**Roles permitidos:** `ANALISTA_KYC`, `ADMIN`

#### Request

```http
POST /api/v1/kyc/revision/550e8400-e29b-41d4-a716-446655440001/solicitar-info
Content-Type: application/json
Authorization: Bearer <token>

{
  "comentario": "Por favor envelve una constancia de trabajo ya que el monto solicitado requiere verificación de ingresos."
}
```

#### Response - 200 OK

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "estadoAnterior": "EN_REVISION",
  "estadoNuevo": "PENDIENTE",
  "mensaje": "Se requieren documentos adicionales para completar su verificación."
}
```

---

### 11. GET /kyc/historial - Historial de Verificaciones

**Descripción:** Obtiene el historial de verificaciones KYC del socio.

**Roles permitidos:** `SOCIO`

#### Request

```http
GET /api/v1/kyc/historial
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "socioId": "330e8400-e29b-41d4-a716-446655440004",
  "totalVerificaciones": 2,
  "historial": [
    {
      "verificacionId": "550e8400-e29b-41d4-a716-446655440005",
      "nivel": "BASICO",
      "estado": "APROBADO",
      "fechaInicio": "2025-01-15T10:00:00Z",
      "fechaCompletado": "2025-01-17T14:30:00Z",
      "fechaExpiracion": "2027-01-15T10:00:00Z",
      "diasRestantes": null,
      "revisadoPor": "analista01@fondoahorro.com"
    },
    {
      "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
      "nivel": "BASICO",
      "estado": "RECHAZADO",
      "fechaInicio": "2026-04-14T10:30:00Z",
      "fechaCompletado": "2026-04-15T09:00:00Z",
      "fechaExpiracion": null,
      "diasRestantes": null,
      "revisadoPor": "analista02@fondoahorro.com",
      "motivoRechazo": "Documentos ilegibles"
    }
  ]
}
```

---

### 12. GET /kyc/admin/estadisticas - Estadísticas (Admin)

**Descripción:** Obtiene estadísticas de KYC para el dashboard administrativo.

**Roles permitidos:** `ADMIN`

#### Implementación

Las métricas se calculan mediante queries reales a la base de datos:

| Métrica | Query/Cálculo |
|---------|---------------|
| `tiempoPromedioRevisionHoras` | `SELECT AVG(EXTRACT(EPOCH FROM (fecha_revision - fecha_envio)) / 3600) FROM verificacion_kyc WHERE estado = 'APROBADO' AND fecha_revision IS NOT NULL` |
| `kycPorExpirarProximoMes` | `SELECT COUNT(*) FROM verificacion_kyc WHERE estado = 'APROBADO' AND fecha_expiracion BETWEEN NOW() AND NOW() + INTERVAL '30 days'` |

#### Request

```http
GET /api/v1/kyc/admin/estadisticas
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "totalVerificaciones": 150,
  "estadoActual": {
    "pendientes": 12,
    "enRevision": 5,
    "aprobados": 125,
    "rechazados": 8,
    "expirados": 15
  },
  "metricas": {
    "tiempoPromedioRevisionHoras": 18.5,
    "tasaAprobacion": 0.83,
    "tasaRechazo": 0.05,
    "kycPorExpirarProximoMes": 3
  },
  "porNivel": {
    "BASICO": {
      "total": 100,
      "aprobados": 85,
      "rechazados": 5
    },
    "MEDIO": {
      "total": 40,
      "aprobados": 32,
      "rechazados": 3
    },
    "COMPLETO": {
      "total": 10,
      "aprobados": 8,
      "rechazados": 0
    }
  }
}
```

---

### 13. POST /kyc/revocar-consentimiento - Revocar Consentimiento (LOPDP Art. 7)

**Descripción:** Revoca el consentimiento para tratamiento de datos personales. Crea un nuevo registro de consentimiento con `aceptado=false` (no modifica registros existentes, cumpliendo con LOPDP Art. 7).

**Roles permitidos:** `SOCIO`

#### Request

```http
POST /api/v1/kyc/revocar-consentimiento
Content-Type: application/json
Authorization: Bearer <token>

{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "motivo": "Ya no deseo continuar con el proceso"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| verificacionId | UUID | Sí | Verificación válida | ID de la verificación activa |
| motivo | String | No | Max 500 chars | Motivo de la revocación |

#### Response - 200 OK

```json
{
  "verificacionId": "550e8400-e29b-41d4-a716-446655440001",
  "estado": "CANCELADO",
  "mensaje": "Consentimiento revocado. Su proceso ha sido cancelado.",
  "fechaRevocacion": "2026-04-14T11:00:00Z"
}
```

#### Response - 400 Bad Request

```json
{
  "error": "KYC_016",
  "mensaje": "Solo se puede revocar consentimiento de verificaciones pendientes",
  "timestamp": "2026-04-14T11:00:00Z"
}
```

---

## Códigos de Error (Ampliados)

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| `KYC_001` | 409 | Socio ya tiene KYC activo |
| `KYC_002` | 400 | Documento no cumple formato/tamaño |
| `KYC_003` | 422 | Documentos incompletos para enviar |
| `KYC_004` | 403 | Sin permisos de analista |
| `KYC_005` | 404 | Verificación no encontrada |
| `KYC_006` | 409 | Verificación no editable |
| `KYC_007` | 409 | Documento no puede eliminarse |
| `KYC_008` | 503 | Servicio de storage no disponible |
| `KYC_009` | 400 | Tipo de documento no permitido para nivel |
| `KYC_010` | 400 | Tamaño Base64 decodificado excede límite |
| `KYC_011` | 400 | Magic number de archivo inválido (no es JPEG/PNG/PDF) |
| `KYC_012` | 409 | Documento duplicado (ya existe del mismo tipo) |
| `KYC_013` | 400 | Storage path con caracteres inválidos |
| `KYC_014` | 409 | Límite de documentos por verificación excedido |
| `KYC_015` | 429 | Límite de almacenamiento diario excedido |
| `KYC_016` | 400 | Revocación de consentimiento no válida para estado |

---

## Rate Limiting (IMPLEMENTADO con Bucket4j)

| Plan | Límite | Ventana | Notas |
|------|--------|---------|-------|
| Default | 60 requests/min | Por IP | Implementado con Bucket4j |
| `/kyc/documentos` | 20 requests/min | Por usuario | Implementado con Bucket4j |
| `/kyc/cola-revision` | 30 requests/min | Por analista | Implementado con Bucket4j |
| Límite diario | 100 MB/socio/día | Por usuario | **C5** |
| Verificaciones | 3/socio/día | Por usuario | **C5** |
| Documentos por verificación | 10 máx | Por verificación | **A1** |

### Validaciones de Seguridad Implementadas

| Validación | Descripción | Implementación |
|------------|-------------|----------------|
| Rate Limiting | Prevención de ataques de fuerza bruta | Bucket4j |
| Validación IP | IPv4 e IPv6 válidas | `InetAddressValidator` |
| Pre-signed URLs | Acceso temporal a documentos (15 min) | `StorageService.generatePresignedUrl()` |
| Path Traversal | Validación de nombres contra `..` | Validación en `StorageService` |
| Optimistic Locking | Prevención de condiciones de carrera | `@Version` en entidades JPA |

---

## Headers de Response

Todos los responses incluyen:

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| X-Request-Id | ID único de la request | `req_xyz789` |
| X-Response-Time | Tiempo de procesamiento (ms) | `45` |
| X-Correlation-Id | ID de correlación para logs | `corr_abc123` |

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @documentador | Creación inicial - KYC Simplificado |
| 1.1 | 2026-04-14 | @auditoria | Integración correcciones seguridad (C1-C5, A1-A7) |
| 1.2 | 2026-04-14 | @documentador | Actualización post-implementación: 12 endpoints + revocación consentimiento, rate limiting Bucket4j, auditoría LOPDP/SUDEBAN |

---

## Referencias

- Especificación técnica: `/docs/modulos/kyc/SPEC.md`
- Modelo de datos: `/docs/modulos/kyc/MODELO_DATOS.md`
- Sistema de documentación: `/docs/modulos/documentacion/SPEC.md`