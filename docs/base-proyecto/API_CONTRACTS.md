# Contratos API Frontend-Backend - Fondo de Ahorro

**Proyecto:** FATRANS
**Versión:** 1.1
**Fecha:** 2026-04-22
**Estado:** Firmado con correcciones

---

> **Historial de correcciones:**
> - v1.1: Corregido `versionPolitica` - ahora refleja whitelist real del backend

---

## ⚠️ NOTA SOBRE ROLES

**Roles definidos en backend enum `Rol.java`:**
```java
public enum Rol {
    SOCIO,
    ADMIN,
    SUPER_ADMIN
}
```

**⚠️ ROLES PENDIENTES EN BACKEND:** Los siguientes roles se usan en `@PreAuthorize` del backend pero **NO existen** en el enum `Rol.java`. Requieren ser agregados al backend antes de implementar:

| Rol | Ubicación en backend | Uso previsto |
|-----|---------------------|-------------|
| `CAJERO` | `CreditoController.java:305` | Registrar pagos de cuotas |
| `ANALISTA_KYC` | `AnalistaKYCController.java`, `AdminKYCController.java` | Revisar documentos KYC |

**Acción requerida:** Agregar `CAJERO` y `ANALISTA_KYC` al enum `Rol.java` en backend.

---

## 1. Contratos por Módulo

### 1.1 Auth

#### POST /api/v1/auth/login
**Descripción:** Autenticación de usuario

**Request:**
```typescript
interface LoginRequest {
  identificador: string;  // 3-100 chars (email o username)
  password: string;       // min 8, 1 mayúscula, 1 minúscula, 1 número, 1 especial
}
```

**Response (200):**
```typescript
interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: 900;  // 15 minutos
  usuario: {
    id: string;           // UUID
    nombreUsuario: string;
    correoElectronico: string;
    nombreCompleto: string;
    rol: 'SOCIO' | 'ADMIN' | 'SUPER_ADMIN';
  };
}
```

**Errores:**
| Código | HTTP | Descripción |
|--------|------|-------------|
| CREDENCIALES_INVALIDAS | 401 | Usuario o contraseña incorrectos |
| CUENTA_BLOQUEADA | 403 | Cuenta bloqueada por 5 intentos fallidos |
| CUENTA_DESACTIVADA | 403 | Cuenta desactivada |

---

#### POST /api/v1/auth/refresh
**Descripción:** Refrescar tokens

**Request:**
```typescript
interface RefreshRequest {
  refreshToken: string;
}
```

**Response (200):**
```typescript
interface RefreshResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: 900;
  usuario: { ... };
}
```

---

#### POST /api/v1/auth/logout
**Descripción:** Cerrar sesión

**Request:** (vacío, usa Bearer token del header)

**Response (200):**
```typescript
interface LogoutResponse {
  mensaje: 'Sesión cerrada correctamente';
}
```

---

#### GET /api/v1/auth/me
**Descripción:** Obtener usuario actual

**Response (200):**
```typescript
interface MeResponse {
  id: string;
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  rol: 'SOCIO' | 'ADMIN' | 'SUPER_ADMIN';
}
```

---

#### POST /api/v1/auth/recuperar-password
**Descripción:** Solicitar recuperación de contraseña

**Request:**
```typescript
interface RecuperarPasswordRequest {
  correoElectronico: string;  // Formato email
}
```

**Response (200):**
```typescript
interface RecuperarPasswordResponse {
  mensaje: 'Si el correo existe en nuestro sistema, recibirá un enlace de recuperación.';
}
```

**Nota:** Siempre retorna el mismo mensaje (no revela si el email existe).

---

#### POST /api/v1/auth/reset-password
**Descripción:** Restablecer contraseña con token

**Request:**
```typescript
interface ResetPasswordRequest {
  token: string;
  nuevaPassword: string;      // min 8, 1 mayúscula, 1 minúscula, 1 número, 1 especial
  confirmarPassword: string;  // Debe coincidir con nuevaPassword
}
```

**Response (200):**
```typescript
interface ResetPasswordResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: 900;
  mensaje: 'Contraseña restablecida exitosamente';
}
```

---

### 1.2 Socios

#### POST /api/v1/socios/solicitud (PÚBLICO)
**Descripción:** Crear solicitud de registro

**Request:**
```typescript
interface SolicitudRegistroRequest {
  primerNombre: string;           // max 50
  segundoNombre?: string;          // max 50
  primerApellido: string;          // max 50
  segundoApellido?: string;         // max 50
  tipoDocumento: 'CEDULA_IDENTIDAD' | 'PASAPORTE' | 'CEDULA_EXTRANJERA';
  numeroDocumento: string;         // Formato: V-XXXXXXXX
  fechaNacimiento: string;         // ISO date
  genero: 'MASCULINO' | 'FEMENINO' | 'OTRO';
  estadoCivil: 'SOLTERO' | 'CASADO' | 'UNION_LIBRE' | 'DIVORCIADO' | 'VIUDO';
  nacionalidad: string;            // max 50
  direccion: {
    calle: string;
    ciudad: string;
    estado: string;
    codigoPostal: string;
    pais: string;
  };
  correoElectronico: string;       // Formato email
  telefonoPrincipal: string;        // Formato: +XX-XXX-XXXXXXX
  telefonoSecundario?: string;
  contactoEmergencia: {
    nombreCompleto: string;
    telefono: string;
    parentesco: string;
  };
  empresa: string;                 // max 100
  departamento?: string;           // max 100
  cargo?: string;                   // max 100
  tipoContrato: 'PERMANENTE' | 'TEMPORAL' | 'PRESTACION_SERVICIOS' | 'PASANTE';
  salario: number;                  // min 0.01
  banco: string;                    // max 50
  numeroCuenta: string;             // 10-20 dígitos
}
```

**Response (201):**
```typescript
interface SolicitudRegistroResponse {
  id: string;              // UUID
  estado: 'PENDIENTE';
  mensaje: 'Solicitud enviada correctamente. Recibirá notificación cuando sea procesada.';
  fechaSolicitud: string;  // ISO datetime
}
```

---

#### GET /api/v1/socios/solicitudes
**Descripción:** Listar solicitudes de registro (Admin)

**Query Params:**
```typescript
{
  estado?: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
  page?: number;   // default 0
  size?: number;   // default 20, max 100
}
```

**Response (200):**
```typescript
interface SolicitudesListResponse {
  content: Array<{
    id: string;
    primerNombre: string;
    primerApellido: string;
    numeroDocumento: string;
    correoElectronico: string;
    empresa: string;
    estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
    fechaSolicitud: string;
  }>;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
```

---

#### POST /api/v1/socios/solicitudes/{id}/aprobar
**Descripción:** Aprobar solicitud (Admin)

**Response (200):**
```typescript
interface AprobarSolicitudResponse {
  id: string;
  numeroSocio: string;           // FA-YYYY-NNNNNN
  primerNombre: string;
  primerApellido: string;
  correoElectronico: string;
  empresa: string;
  estado: 'ACTIVO';
  mensaje: 'Usuario creado exitosamente. Las credenciales fueron enviadas al correo.';
}
```

---

#### POST /api/v1/socios/solicitudes/{id}/rechazar
**Descripción:** Rechazar solicitud (Admin)

**Request:**
```typescript
interface RechazarSolicitudRequest {
  motivo: string;  // min 10 chars
}
```

**Response (200):**
```typescript
interface RechazarSolicitudResponse {
  id: string;
  estado: 'RECHAZADA';
  motivoRechazo: string;
  mensaje: 'Solicitud rechazada';
}
```

---

### 1.3 Cuentas/Ahorros

#### GET /api/v1/cuentas/socio/{socioId}
**Descripción:** Listar cuentas de un socio

**Response (200):**
```typescript
interface CuentasSocioResponse {
  socioId: string;
  totalCuentas: number;
  cuentas: Array<{
    id: number;
    numeroCuenta: string;   // AHO-YYYY-NNNNNN
    saldoActual: number;
    estado: 'ACTIVA' | 'SUSPENDIDA' | 'CERRADA';
    tipoCuenta: 'AHORRO' | 'NOMINA';
    fechaApertura: string;
  }>;
}
```

---

#### GET /api/v1/cuentas/{numeroCuenta}
**Descripción:** Detalle de cuenta

**Response (200):**
```typescript
interface CuentaDetailResponse {
  id: number;
  numeroCuenta: string;
  socioId: string;
  saldoActual: number;
  saldoRetenido: number;
  saldoDisponible: number;
  tasaInteres: number;
  montoMinimoRequerido: number;
  estado: 'ACTIVA' | 'SUSPENDIDA' | 'CERRADA';
  tipoCuenta: 'AHORRO' | 'NOMINA';
  fechaApertura: string;
  fechaUltimaOperacion: string | null;
}
```

---

#### GET /api/v1/cuentas/{numeroCuenta}/saldo
**Descripción:** Consultar saldo detallado

**Response (200):**
```typescript
interface SaldoResponse {
  numeroCuenta: string;
  saldoActual: number;
  saldoRetenido: number;
  saldoDisponible: number;
  fechaConsulta: string;
  limiteDeposito: number;         // 500000.00
  limiteRetiroDiario: number;      // 50000.00
  retirosRealizadosHoy: number;
  retirosRestantesHoy: number;
}
```

---

#### POST /api/v1/cuentas/{numeroCuenta}/depositos
**Descripción:** Realizar depósito

**Request:**
```typescript
interface DepositoRequest {
  monto: number;           // min 0.01, max 500000.00
  descripcion?: string;   // max 500
  referencia?: string;    // max 100
  canalOrigen: 'WEB' | 'MOBILE' | 'ATM' | 'SUCURSAL' | 'API';
}
```

**Response (201):**
```typescript
interface DepositoResponse {
  id: number;
  numeroOperacion: string;   // MOV-YYYY-NNNNNN
  cuentaAhorroId: number;
  tipo: 'DEPOSITO';
  monto: number;
  saldoAnterior: number;
  saldoPosterior: number;
  descripcion: string | null;
  referencia: string | null;
  canalOrigen: 'WEB';
  estado: 'PROCESADO';
  fechaMovimiento: string;
  fechaValor: string;  // ISO date
}
```

---

#### POST /api/v1/cuentas/{numeroCuenta}/retiros
**Descripción:** Realizar retiro

**Request:**
```typescript
interface RetiroRequest {
  monto: number;           // min 0.01, no puede exceder saldoDisponible
  canalOrigen: 'WEB' | 'MOBILE' | 'ATM' | 'SUCURSAL' | 'API';
}
```

**Response (201):**
```typescript
interface RetiroResponse {
  id: number;
  numeroOperacion: string;
  cuentaAhorroId: number;
  tipo: 'RETIRO';
  monto: number;
  saldoAnterior: number;
  saldoPosterior: number;
  canalOrigen: 'WEB';
  estado: 'PROCESADO';
  fechaMovimiento: string;
  fechaValor: string;
  warnings?: Array<{
    codigo: 'SALDO_BAJO_MINIMO';
    mensaje: string;
  }>;
}
```

**Errores:**
| Código | HTTP | Descripción |
|--------|------|-------------|
| SALDO_INSUFICIENTE | 400 | Saldo disponible menor al monto |
| LIMITE_DIARIO_EXCEDIDO | 400 | Excedió límite de retiro diario |
| CUENTA_NO_PERMITE_OPERACIONES | 422 | Cuenta no está ACTIVA |

---

#### GET /api/v1/cuentas/{numeroCuenta}/movimientos
**Descripción:** Listar movimientos

**Query Params:**
```typescript
{
  page?: number;
  size?: number;         // default 20, max 100
  fechaInicio?: string;  // ISO date
  fechaFin?: string;     // ISO date
  tipo?: 'DEPOSITO' | 'RETIRO';
}
```

**Response (200):**
```typescript
interface MovimientosResponse {
  numeroCuenta: string;
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
  movimientos: Array<{
    id: number;
    numeroOperacion: string;
    tipo: 'DEPOSITO' | 'RETIRO';
    monto: number;
    saldoPosterior: number;
    descripcion: string | null;
    fechaMovimiento: string;
  }>;
}
```

---

### 1.4 Créditos

#### GET /api/v1/creditos/tipos-credito
**Descripción:** Listar tipos de crédito disponibles

**Response (200):**
```typescript
interface TiposCreditoResponse {
  tiposCredito: Array<{
    id: number;
    codigo: string;
    nombre: string;
    descripcion: string;
    tasaInteresAnual: number;
    plazoMinimoMeses: number;
    plazoMaximoMeses: number;
    montoMinimo: number;
    montoMaximo: number;
    porcentajeRequerimientoColateral: number;
    comisionApertura: number;
    penalidadMoraTasa: number;
    diasGracia: number;
    activo: boolean;
  }>;
}
```

---

#### POST /api/v1/creditos/solicitudes
**Descripción:** Crear solicitud de crédito

**Prerrequisito:** KYC debe estar APROBADO

**Request:**
```typescript
interface CrearSolicitudCreditoRequest {
  tipoCreditoId: number;
  montoSolicitado: number;
  plazoMeses: number;         // 1-360
  destinoCredito?: string;    // max 500
  cuentaDestino?: string;     // max 34
}
```

**Response (201):**
```typescript
interface CrearSolicitudCreditoResponse {
  id: string;
  numeroSolicitud: string;    // SOL-CRED-YYYY-NNNNNN
  socioId: number;
  tipoCreditoId: number;
  montoSolicitado: number;
  plazoMeses: number;
  estado: 'PENDIENTE';
  cuentaDestino: string | null;
  createdAt: string;
}
```

---

#### GET /api/v1/creditos/solicitudes/socio/{socioId}
**Descripción:** Listar solicitudes de crédito del socio

**Response (200):**
```typescript
interface SolicitudesCreditoSocioResponse {
  socioId: number;
  totalSolicitudes: number;
  solicitudes: Array<{
    id: string;
    numeroSolicitud: string;
    tipoCredito: string;
    montoSolicitado: number;
    plazoMeses: number;
    estado: 'PENDIENTE' | 'EN_EVALUACION' | 'APROBADA' | 'RECHAZADA' | 'DESEMBOLSADO' | 'CANCELADO';
    fechaCreacion: string;
  }>;
}
```

---

#### GET /api/v1/creditos/solicitudes/{numeroSolicitud}/plan
**Descripción:** Obtener plan de amortización

**Response (200):**
```typescript
interface PlanAmortizacionResponse {
  id: string;
  solicitudId: string;
  montoPrincipal: number;
  tasaInteres: number;
  plazoMeses: number;
  frecuenciaPago: 'MENSUAL';
  fechaInicio: string;
  fechaFin: string;
  numeroCuotas: number;
  cuotaMensual: number;
  totalIntereses: number;
  totalPagado: number;
  saldoPendiente: number;
  estado: 'ACTIVO' | 'CANCELADO' | 'COMPLETADO';
  cuotas: Array<{
    id: string;
    numeroCuota: number;
    fechaVencimiento: string;
    capital: number;
    interes: number;
    montoCuota: number;
    saldoInsoluto: number;
    estado: 'PENDIENTE' | 'VENCIDA' | 'PAGADA';
    fechaPago?: string;
    diasMora?: number;
    interesMora?: number;
  }>;
}
```

---

#### POST /api/v1/simulador
**Descripción:** Simular crédito (requiere auth)

**Request:**
```typescript
interface SimuladorRequest {
  monto: number;           // >= 1000, <= 5000000
  plazoMeses: number;     // 1-360
  tasa: number;            // > 0, <= 1.0
}
```

**Response (200):**
```typescript
interface SimuladorResponse {
  monto: number;
  plazoMeses: number;
  tasaInteresAnual: number;
  cuotaMensual: number;
  totalIntereses: number;
  totalAPagar: number;
  planSimulado: Array<{
    numeroCuota: number;
    fechaVencimiento: string;
    capital: number;
    interes: number;
    montoCuota: number;
    saldoInsoluto: number;
  }>;
  nota: string;
}
```

---

#### POST /api/v1/creditos/cuotas/{cuotaId}/pago
**Descripción:** Registrar pago de cuota

**Request:**
```typescript
interface PagoCuotaRequest {
  monto: number;              // >= monto de la cuota
  referenciaPago?: string;   // max 100, clave de idempotencia
  canalOrigen: 'SUCURSAL' | 'WEB' | 'MOBILE' | 'ATM';
}
```

**Response (200):**
```typescript
interface PagoCuotaResponse {
  id: string;
  numeroCuota: number;
  estado: 'PAGADA';
  montoPagado: number;
  fechaPago: string;
  referenciaPago: string | null;
  saldoInsolutoRestante: number;
  mensaje: 'Pago registrado exitosamente';
}
```

---

### 1.5 KYC

#### POST /api/v1/kyc/iniciar
**Descripción:** Iniciar proceso KYC

**Request:**
```typescript
interface KycIniciarRequest {
  nivel: 'BASICO' | 'MEDIO' | 'COMPLETO';
  consentimientoAceptado: true;  // Siempre true
  versionPolitica: string;         // Formato: "v{version}-{YYYYMMDD}" ej: "v1.0-20260401"
  ipCliente?: string;
  userAgent?: string;
}
```

**Response (201):**
```typescript
interface KycIniciarResponse {
  verificacionId: string;
  nivel: 'BASICO' | 'MEDIO' | 'COMPLETO';
  estado: 'PENDIENTE';
  documentosRequeridos: string[];
  mensaje: string;
}
```

---

#### GET /api/v1/kyc/estado
**Descripción:** Consultar estado KYC

**Response (200):**
```typescript
interface KycEstadoResponse {
  verificacionId: string;
  socioId: string;
  nivel: 'BASICO' | 'MEDIO' | 'COMPLETO';
  estado: 'PENDIENTE' | 'EN_REVISION' | 'APROBADO' | 'RECHAZADO';
  descripcionEstado: string;
  fechaInicio: string;
  fechaExpiracion: string;
  diasRestantes: number;
  documentosRequeridos: number;
  documentosValidos: number;
  documentos: Array<{
    id: string;
    tipo: string;
    descripcion: string;
    estado: 'PENDIENTE' | 'VALIDADO' | 'RECHAZADO';
    nombreOriginal: string | null;
    fechaSubida: string | null;
    motivoRechazo: string | null;
  }>;
}
```

---

#### POST /api/v1/kyc/documentos
**Descripción:** Subir documento

**Request:**
```typescript
interface KycDocumentoRequest {
  verificacionId: string;
  tipoDocumento: 'CEDULA_ANVERSO' | 'CEDULA_REVERSO' | 'SELFIE_CEDULA' | 'COMPROBANTE_DOMICILIO';
  archivoBase64: string;       // max 10MB decodificado
  nombreOriginal: string;      // max 255
  tamanoBytes: number;          // max 10,485,760
  mimeType: 'image/jpeg' | 'image/png' | 'application/pdf';
  fechaExpiracionDocumento?: string;
}
```

**Response (201):**
```typescript
interface KycDocumentoResponse {
  documentoId: string;
  tipoDocumento: string;
  nombreOriginal: string;
  estado: 'PENDIENTE';
  mensaje: 'Documento subido exitosamente';
}
```

---

### 1.6 Beneficiarios

#### GET /api/v1/socios/{socioId}/beneficiarios
**Descripción:** Listar beneficiarios

**Response (200):**
```typescript
interface BeneficiariosResponse {
  beneficiarios: Array<{
    id: string;
    socioId: string;
    nombreCompleto: string;
    numeroDocumento: string;
    tipoDocumento: 'CEDULA_IDENTIDAD' | 'RIF' | 'PASAPORTE' | 'CEDULA_EXTRANJERO';
    parentesco: 'CONYUGE' | 'HIJO' | 'PADRE' | 'MADRE' | 'HERMANO' | 'ABUELO' | 'NIETO' | 'SOBRINO' | 'TIO' | 'OTRO';
    porcentaje: number;           // 0.01 - 100.00
    telefono: string | null;
    activo: boolean;
    fechaRegistro: string;
    fechaActualizacion: string;
  }>;
  total: number;
  sumaPorcentajes: number;       // DEBE SER 100.00
}
```

---

#### POST /api/v1/socios/{socioId}/beneficiarios
**Descripción:** Crear beneficiario

**Regla:** Suma de porcentajes no puede exceder 100%

**Request:**
```typescript
interface CrearBeneficiarioRequest {
  nombreCompleto: string;         // max 200
  numeroDocumento: string;        // max 20
  tipoDocumento: 'CEDULA_IDENTIDAD' | 'RIF' | 'PASAPORTE' | 'CEDULA_EXTRANJERO';
  parentesco: 'CONYUGE' | 'HIJO' | 'PADRE' | 'MADRE' | 'HERMANO' | 'ABUELO' | 'NIETO' | 'SOBRINO' | 'TIO' | 'OTRO';
  porcentaje: number;              // 0.01 - 100.00
  telefono?: string;              // max 20
}
```

**Errores:**
| Código | HTTP | Descripción |
|--------|------|-------------|
| PORCENTAJE_SUM_EXCEDIDO | 400 | Suma excedería 100% |
| MAXIMO_BENEFICIARIOS_EXCEDIDO | 400 | Ya tiene 5 beneficiarios |
| DOCUMENTO_IGUAL_TITULAR | 400 | Documento igual al socio |

---

### 1.7 Documentos PDF

#### GET /api/v1/documentos/estado-cuenta/{cuentaId}
**Descripción:** Generar estado de cuenta

**Response (200):**
```typescript
interface GenerarDocumentoResponse {
  documentoId: string;
  tipo: 'ESTADO_CUENTA';
  nombreArchivo: string;
  estado: 'ALMACENADO';
  tamanoBytes: number;
  hashArchivo: string;
  clasificacion: 'CONFIDENCIAL';
  preSignedUrl: string;
  urlExpiraEn: 900;  // 15 minutos
  fechaGeneracion: string;
  fechaExpiracion: string;
}
```

---

## 2. Códigos de Error Comunes

| Código | HTTP | Módulo | Descripción |
|--------|------|--------|-------------|
| CREDENCIALES_INVALIDAS | 401 | Auth | Usuario o contraseña incorrectos |
| TOKEN_EXPIRADO | 401 | Auth | Token JWT expirado |
| TOKEN_INVALIDO | 401 | Auth | Token malformado |
| CUENTA_BLOQUEADA | 403 | Auth | Cuenta bloqueada |
| CUENTA_DESACTIVADA | 403 | Auth | Cuenta desactivada |
| ACCESO_NO_AUTORIZADO | 403 | Todos | Violación IDOR |
| SOCIO_NO_ENCONTRADO | 404 | Socios | Socio no existe |
| SOLICITUD_NO_ENCONTRADA | 404 | Socios/Créditos | Solicitud no existe |
| CUENTA_NO_ENCONTRADA | 404 | Ahorros | Cuenta no existe |
| VALIDATION_ERROR | 400 | Todos | Datos inválidos |
| RATE_LIMIT_EXCEDIDO | 429 | Todos | Demasiadas solicitudes |

---

## 3. Headers Estándar

### Request
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
X-Request-Id: <uuid> (opcional)
```

### Response
```
X-Request-Id: req_xyz789
X-Response-Time: 45
X-Correlation-Id: corr_abc123
```

---

## 4. Rate Limiting

| Módulo | Endpoint | Límite | Ventana |
|--------|----------|--------|---------|
| Auth | /login | 5 req | 1 min |
| Auth | /recuperar-password | 3 req | 1 min |
| Auth | /reset-password | 3 req | 1 min |
| Socios | /solicitud | 60 req | 1 min |
| Ahorros | /depositos | 30 req | 1 min |
| Ahorros | /retiros | 30 req | 1 min |
| Créditos | /simulador | 10 req | 1 min |
| KYC | /documentos | 20 req | 1 min |

---

## 5. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-21 | @product-manager | Creación inicial de contratos |

---

## 6. Referencias

- API Auth: `/docs/modulos/auth/API.md`
- API Socios: `/docs/modulos/socios/API.md`
- API Ahorros: `/docs/modulos/ahorros/API.md`
- API Créditos: `/docs/modulos/creditos/API.md`
- API KYC: `/docs/modulos/kyc/API.md`
- API Beneficiarios: `/docs/modulos/beneficiarios/API.md`
- API Documentos: `/docs/modulos/documentospdf/API.md`
