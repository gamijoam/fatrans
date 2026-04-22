# Módulo SOCIOS - Referencia de API

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-14

---

## Resumen

Documentación completa de los **13 endpoints** del módulo SOCIOS con ejemplos de request/response, códigos de error y notas de seguridad.

---

## Base URL

```
Production: https://api.fondoahorro.com/v1
Development: http://localhost:8080/api/v1
```

---

## Autenticación

Todos los endpoints (excepto `/solicitud`) requieren header de autenticación JWT:

```
Authorization: Bearer <jwt_token>
```

Roles permitidos:
- `ADMIN`: Acceso completo a todos los endpoints
- `SOCIO`: Solo puede acceder a sus propios datos y crear solicitudes

---

## Endpoints del Módulo SOCIOS

### 1. POST /socios - Crear Socio

**Descripción:** Crea un nuevo socio en el sistema (solo ADMIN).

**Roles permitidos:** `ADMIN`

#### Request

```http
POST /api/v1/socios
Content-Type: application/json
Authorization: Bearer <token>

{
  "primerNombre": "Juan",
  "segundoNombre": "Carlos",
  "primerApellido": "Pérez",
  "segundoApellido": "García",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "numeroDocumento": "V-12345678",
  "fechaNacimiento": "1990-05-15",
  "genero": "MASCULINO",
  "estadoCivil": "SOLTERO",
  "nacionalidad": "Venezolano",
  "direccion": {
    "calle": "Av. Principal",
    "ciudad": "Caracas",
    "estado": "Distrito Capital",
    "codigoPostal": "1001",
    "pais": "Venezuela"
  },
  "correoElectronico": "juan@empresa.com",
  "telefonoPrincipal": "+58-212-5551234",
  "telefonoSecundario": "+58-412-1234567",
  "contactoEmergencia": {
    "nombreCompleto": "María Pérez",
    "telefono": "+58-414-7654321",
    "parentesco": "Cónyuge"
  },
  "empresa": "ACME Corp",
  "departamento": "Recursos Humanos",
  "cargo": "Analista",
  "tipoContrato": "PERMANENTE",
  "salario": "1500.00",
  "banco": "Banco Nacional",
  "numeroCuenta": "0123456789"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| primerNombre | String | Sí | Max 50 chars | Primer nombre |
| segundoNombre | String | No | Max 50 chars | Segundo nombre |
| primerApellido | String | Sí | Max 50 chars | Primer apellido |
| segundoApellido | String | No | Max 50 chars | Segundo apellido |
| tipoDocumento | Enum | Sí | CEDULA_IDENTIDAD, PASAPORTE, CEDULA_EXTRANJERA | Tipo de documento |
| numeroDocumento | String | Sí | Formato: V-XXXXXXXX o E-XXXXXXXX | Número de documento |
| fechaNacimiento | Date | Sí | @Past | Fecha de nacimiento |
| genero | Enum | Sí | MASCULINO, FEMENINO, OTRO | Género |
| estadoCivil | Enum | Sí | SOLTERO, CASADO, UNION_LIBRE, DIVORCIADO, VIUDO | Estado civil |
| nacionalidad | String | Sí | Max 50 chars | Nacionalidad |
| direccion | Object | Sí | Objeto Direccion | Dirección del socio |
| correoElectronico | String | Sí | Formato email | Correo electrónico |
| telefonoPrincipal | String | Sí | Formato: +XX-XXX-XXXXXXX | Teléfono principal |
| telefonoSecundario | String | No | Formato: +XX-XXX-XXXXXXX | Teléfono secundario |
| contactoEmergencia | Object | Sí | Objeto ContactoEmergencia | Contacto de emergencia |
| empresa | String | Sí | Max 100 chars | Empresa donde trabaja |
| departamento | String | No | Max 100 chars | Departamento |
| cargo | String | No | Max 100 chars | Cargo |
| tipoContrato | Enum | Sí | PERMANENTE, TEMPORAL, PRESTACION_SERVICIOS, PASANTE | Tipo de contrato |
| salario | Decimal | Sí | Min 0.01 | Salario |
| banco | String | Sí | Max 50 chars | Banco |
| numeroCuenta | String | Sí | 10-20 dígitos | Número de cuenta |

#### Response - 201 Created

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSocio": "FA-2026-000001",
  "primerNombre": "Juan",
  "primerApellido": "Pérez",
  "correoElectronico": "juan@empresa.com",
  "empresa": "ACME Corp",
  "estado": "PENDIENTE_APROBACION",
  "fechaRegistro": "2026-04-14T10:00:00Z"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| SOCIO_DUPLICADO | 409 | Ya existe socio con mismo correo/documento |
| VALIDATION_ERROR | 400 | Datos inválidos |

---

### 2. GET /socios/{id} - Obtener Socio

**Descripción:** Obtiene los datos de un socio por ID.

**Roles permitidos:** `ADMIN`, `SOCIO` (solo propio)

#### Request

```http
GET /api/v1/socios/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSocio": "FA-2026-000001",
  "primerNombre": "Juan",
  "segundoNombre": "Carlos",
  "primerApellido": "Pérez",
  "segundoApellido": "García",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "numeroDocumento": "V-12345678",
  "fechaNacimiento": "1990-05-15",
  "genero": "MASCULINO",
  "estadoCivil": "SOLTERO",
  "nacionalidad": "Venezolano",
  "direccion": {
    "calle": "Av. Principal",
    "ciudad": "Caracas",
    "estado": "Distrito Capital",
    "codigoPostal": "1001",
    "pais": "Venezuela"
  },
  "correoElectronico": "juan@empresa.com",
  "telefonoPrincipal": "+58-212-5551234",
  "telefonoSecundario": "+58-412-1234567",
  "contactoEmergencia": {
    "nombreCompleto": "María Pérez",
    "telefono": "+58-414-7654321",
    "parentesco": "Cónyuge"
  },
  "empresa": "ACME Corp",
  "departamento": "Recursos Humanos",
  "cargo": "Analista",
  "tipoContrato": "PERMANENTE",
  "salario": "****",
  "banco": "Banco Nacional",
  "numeroCuenta": "****6789",
  "estado": "ACTIVO",
  "nivelKYC": 1,
  "fechaRegistro": "2026-04-14T10:00:00Z",
  "fechaActualizacion": "2026-04-14T10:00:00Z"
}
```

**Nota:** Los campos `salario` y `numeroCuenta` están enmascarados para seguridad.

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| SOCIO_NO_ENCONTRADO | 404 | Socio no existe |

---

### 3. GET /socios - Listar Socios

**Descripción:** Lista todos los socios con paginación.

**Roles permitidos:** `ADMIN`

#### Request

```http
GET /api/v1/socios?page=0&size=10
Authorization: Bearer <token>
```

#### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| page | Integer | No | 0 | Número de página |
| size | Integer | No | 20 | Tamaño de página (máx 100) |

#### Response - 200 OK

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "numeroSocio": "FA-2026-000001",
      "primerNombre": "Juan",
      "primerApellido": "Pérez",
      "correoElectronico": "juan@empresa.com",
      "empresa": "ACME Corp",
      "estado": "ACTIVO"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 150,
  "totalPages": 15
}
```

---

### 4. PUT /socios/{id} - Actualizar Socio

**Descripción:** Actualiza los datos de un socio (solo ADMIN).

**Roles permitidos:** `ADMIN`

#### Request

```http
PUT /api/v1/socios/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
Authorization: Bearer <token>

{
  "primerNombre": "Juan",
  "segundoNombre": "Carlos",
  "primerApellido": "Pérez",
  "segundoApellido": "García",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "numeroDocumento": "V-12345678",
  "fechaNacimiento": "1990-05-15",
  "genero": "MASCULINO",
  "estadoCivil": "CASADO",
  "nacionalidad": "Venezolano",
  "direccion": {
    "calle": "Av. Principal",
    "ciudad": "Caracas",
    "estado": "Distrito Capital",
    "codigoPostal": "1001",
    "pais": "Venezuela"
  },
  "correoElectronico": "juan@empresa.com",
  "telefonoPrincipal": "+58-212-5551234",
  "telefonoSecundario": "+58-412-1234567",
  "contactoEmergencia": {
    "nombreCompleto": "María Pérez",
    "telefono": "+58-414-7654321",
    "parentesco": "Cónyuge"
  },
  "empresa": "ACME Corp",
  "departamento": "Recursos Humanos",
  "cargo": "Analista Senior",
  "tipoContrato": "PERMANENTE",
  "salario": "1800.00",
  "banco": "Banco Nacional",
  "numeroCuenta": "0123456789"
}
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSocio": "FA-2026-000001",
  "primerNombre": "Juan",
  "primerApellido": "Pérez",
  "correoElectronico": "juan@empresa.com",
  "empresa": "ACME Corp",
  "estado": "ACTIVO",
  "mensaje": "Socio actualizado exitosamente"
}
```

---

### 5. DELETE /socios/{id} - Eliminar Socio (Soft Delete)

**Descripción:** Marca un socio como eliminado (soft delete).

**Roles permitidos:** `ADMIN`

#### Request

```http
DELETE /api/v1/socios/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "estado": "ELIMINADO",
  "mensaje": "Socio eliminado exitosamente"
}
```

**Nota:** El socio no se elimina físicamente de la base de datos, solo cambia su estado a ELIMINADO.

---

### 6. PATCH /socios/{id}/activar - Activar Socio

**Descripción:** Activa un socio que estaba inactivo.

**Roles permitidos:** `ADMIN`

#### Request

```http
PATCH /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/activar
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "estado": "ACTIVO",
  "mensaje": "Socio activado exitosamente"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| ESTADO_SOCIO_INVALIDO | 400 | Transición de estado no permitida |

---

### 7. PATCH /socios/{id}/desactivar - Desactivar Socio

**Descripción:** Desactiva un socio activo.

**Roles permitidos:** `ADMIN`

#### Request

```http
PATCH /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/desactivar
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "estado": "INACTIVO",
  "mensaje": "Socio desactivado exitosamente"
}
```

---

### 8. GET /socios/buscar - Buscar Socios por Criterios

**Descripción:** Busca socios por múltiples criterios.

**Roles permitidos:** `ADMIN`

#### Request

```http
GET /api/v1/socios/buscar?numeroSocio=FA-2026&nombre=Juan&cedula=V-123&page=0&size=10
Authorization: Bearer <token>
```

#### Query Parameters

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| numeroSocio | String | No | Número de socio (búsqueda parcial) |
| nombre | String | No | Nombre o apellido (búsqueda parcial) |
| cedula | String | No | Número de documento (búsqueda parcial) |
| correo | String | No | Correo electrónico (búsqueda parcial) |
| empresa | String | No | Empresa (búsqueda parcial) |
| estado | String | No | Estado del socio |
| page | Integer | No | Número de página |
| size | Integer | No | Tamaño de página (máx 100) |

#### Response - 200 OK

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "numeroSocio": "FA-2026-000001",
      "primerNombre": "Juan",
      "primerApellido": "Pérez",
      "correoElectronico": "juan@empresa.com",
      "empresa": "ACME Corp",
      "estado": "ACTIVO"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 9. POST /socios/solicitud - Crear Solicitud de Registro (Público)

**Descripción:** Permite el registro público de nuevos socios sin necesidad de cuenta previa.

**Autenticación:** No requerida

#### Request

```http
POST /api/v1/socios/solicitud
Content-Type: application/json

{
  "primerNombre": "Juan",
  "segundoNombre": "Carlos",
  "primerApellido": "Pérez",
  "segundoApellido": "García",
  "tipoDocumento": "CEDULA_IDENTIDAD",
  "numeroDocumento": "V-12345678",
  "fechaNacimiento": "1990-05-15",
  "genero": "MASCULINO",
  "estadoCivil": "SOLTERO",
  "nacionalidad": "Venezolano",
  "direccion": {
    "calle": "Av. Principal",
    "ciudad": "Caracas",
    "estado": "Distrito Capital",
    "codigoPostal": "1001",
    "pais": "Venezuela"
  },
  "correoElectronico": "juan@empresa.com",
  "telefonoPrincipal": "+58-212-5551234",
  "telefonoSecundario": "+58-412-1234567",
  "contactoEmergencia": {
    "nombreCompleto": "María Pérez",
    "telefono": "+58-414-7654321",
    "parentesco": "Cónyuge"
  },
  "empresa": "ACME Corp",
  "departamento": "Recursos Humanos",
  "cargo": "Analista",
  "tipoContrato": "PERMANENTE",
  "salario": "1500.00",
  "banco": "Banco Nacional",
  "numeroCuenta": "0123456789"
}
```

#### Response - 201 Created

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "estado": "PENDIENTE",
  "mensaje": "Solicitud enviada correctamente. Recibirá notificación cuando sea procesada.",
  "fechaSolicitud": "2026-04-14T10:00:00Z"
}
```

**Flujo:**
1. Socio envía la solicitud
2. Admin recibe notificación
3. Admin revisa y aprueba/rechaza
4. Si se aprueba → Se crea Socio + Usuario automáticamente

---

### 10. GET /socios/solicitudes - Listar Solicitudes

**Descripción:** Lista todas las solicitudes de registro.

**Roles permitidos:** `ADMIN`

#### Request

```http
GET /api/v1/socios/solicitudes?page=0&size=10
Authorization: Bearer <token>
```

#### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| estado | String | No | null | Filtrar por estado (PENDIENTE, APROBADA, RECHAZADA) |
| page | Integer | No | 0 | Número de página |
| size | Integer | No | 20 | Tamaño de página |

#### Response - 200 OK

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "primerNombre": "Juan",
      "primerApellido": "Pérez",
      "numeroDocumento": "V-12345678",
      "correoElectronico": "juan@empresa.com",
      "empresa": "ACME Corp",
      "estado": "PENDIENTE",
      "fechaSolicitud": "2026-04-14T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

---

### 11. POST /socios/solicitudes/{id}/aprobar - Aprobar Solicitud

**Descripción:** Aprueba una solicitud y crea automáticamente Socio + Usuario.

**Roles permitidos:** `ADMIN`

#### Request

```http
POST /api/v1/socios/solicitudes/550e8400-e29b-41d4-a716-446655440001/aprobar
Content-Type: application/json
Authorization: Bearer <token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numeroSocio": "FA-2026-000001",
  "primerNombre": "Juan",
  "primerApellido": "Pérez",
  "correoElectronico": "juan@empresa.com",
  "empresa": "ACME Corp",
  "estado": "ACTIVO",
  "mensaje": "Usuario creado exitosamente. Las credenciales fueron enviadas al correo."
}
```

**Flujo interno:**
1. Busca SolicitudRegistro
2. Valida que esté PENDIENTE
3. Crea Socio (estado: ACTIVO)
4. Invoca `UsuarioCreatorPort` para crear Usuario
5. Envía email con credenciales
6. Actualiza SolicitudRegistro (estado: APROBADA)

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| SOLICITUD_NO_ENCONTRADA | 404 | Solicitud no existe |
| SOLICITUD_YA_PROCESADA | 400 | Solicitud ya aprobada/rechazada |

---

### 12. POST /socios/solicitudes/{id}/rechazar - Rechazar Solicitud

**Descripción:** Rechaza una solicitud con motivo.

**Roles permitidos:** `ADMIN`

#### Request

```http
POST /api/v1/socios/solicitudes/550e8400-e29b-41d4-a716-446655440001/rechazar
Content-Type: application/json
Authorization: Bearer <token>

{
  "motivo": "Documentos ilegibles, por favor reenvíe"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| motivo | String | Sí | Min 10 chars | Motivo del rechazo |

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "estado": "RECHAZADA",
  "motivoRechazo": "Documentos ilegibles, por favor reenvíe",
  "mensaje": "Solicitud rechazada"
}
```

---

### 13. PATCH /socios/{id}/perfil - Actualizar Perfil (Propio Socio)

**Descripción:** Permite a un socio actualizar sus propios datos de perfil.

**Roles permitidos:** `SOCIO` (solo propio)

#### Request

```http
PATCH /api/v1/socios/550e8400-e29b-41d4-a716-446655440000/perfil
Content-Type: application/json
Authorization: Bearer <token>

{
  "telefonoSecundario": "+58-412-1234567",
  "departamento": "Ventas",
  "cargo": "Ejecutivo de Ventas",
  "direccion": {
    "calle": "Av. Secundaria",
    "ciudad": "Caracas",
    "estado": "Distrito Capital",
    "codigoPostal": "1002",
    "pais": "Venezuela"
  }
}
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "mensaje": "Perfil actualizado exitosamente",
  "fechaActualizacion": "2026-04-14T11:00:00Z"
}
```

**Nota:** Solo campos no sensibles pueden ser editados por el propio socio.

---

## Códigos de Error Comunes

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| SOCIO_NO_ENCONTRADO | 404 | Socio con ID especificado no existe |
| SOCIO_DUPLICADO | 409 | Ya existe socio con mismo correo/documento |
| ESTADO_SOCIO_INVALIDO | 400 | Transición de estado no permitida |
| SOLICITUD_NO_ENCONTRADA | 404 | Solicitud no existe |
| SOLICITUD_YA_PROCESADA | 400 | Solicitud ya aprobada/rechazada |
| RATE_LIMIT_EXCEDIDO | 429 | Demasiadas solicitudes |

---

## Rate Limiting

| Endpoint | Límite | Ventana |
|----------|--------|---------|
| POST /socios | 60 req/min | Por IP |
| PUT /socios/{id} | 60 req/min | Por IP |
| GET /socios/buscar | 60 req/min | Por IP |
| POST /socios/solicitud | 60 req/min | Por IP |

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
| 1.0 | 2026-04-14 | @documentador | Creación inicial - Reorganización de documentación |

---

## Referencias

- Especificación técnica: `/docs/modulos/socios/SPEC.md`
- Modelo de datos: `/docs/modulos/socios/MODELO_DATOS.md`
