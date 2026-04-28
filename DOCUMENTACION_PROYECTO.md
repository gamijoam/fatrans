# FONDO DE AHORRO TUFONDO - DOCUMENTO DE ESPECIFICACIÓN

## 1. VISIÓN GENERAL DEL SISTEMA

**Nombre del proyecto:** TuFondo - Fondo de Ahorro Platform
**Tipo:** Plataforma financiera web multiusuario
**Descripción:** Sistema de gestión integral para un fondo de ahorro en Venezuela que permite a los socios administrar cuentas de ahorro, solicitar créditos, completar verificación de identidad (KYC), gestionar beneficiarios y acceder a documentos.
**País de operación:** Venezuela (VES/USD)

### 1.1 Stack Tecnológico

| Capa | Tecnología | Versión |
|------|------------|---------|
| Backend | Spring Boot | 3.2.4 |
| Lenguaje Backend | Java | 21 |
| Frontend | Next.js | 14.2.0 |
| UI Framework | React | 18 |
| Base de Datos | PostgreSQL | 15 |
| Cache/Sesiones | Redis | 7 |
| Object Storage | MinIO | (compatible S3) |
| Generación PDF | OpenPDF + @react-pdf/renderer | - |

### 1.2 Estructura de Monedas

| Moneda | Código | Tipo |
|--------|--------|------|
| Bolívar Venezolano | VES | Principal |
| Dólar Americano | USD | Secundaria |

**Tasa de cambio default:** 50 VES = 1 USD

---

## 2. ARQUITECTURA DEL SISTEMA

### 2.1 Patrón General

El sistema sigue una arquitectura BFF (Backend for Frontend) con:
- **Backend:** API REST completa en Spring Boot que sirve a múltiples clientes
- **Frontend:** Aplicación Next.js que actúa como proxy y consume las APIs del backend
- **Separación de concerns:** Frontend solo hace renderizado y proxy; toda la lógica de negocio está en el backend

### 2.2 Flujo de Autenticación

```
[Usuario] → [Frontend Next.js :3000] → [Backend Spring Boot :18080] → [PostgreSQL]
                                                        ↓
                                                    [Redis] (sesiones)
                                                    [MinIO] (archivos)
```

### 2.3 Roles del Sistema

| Rol | Descripción | Privilegios |
|-----|-------------|-------------|
| SOCIO | Usuario regular del fondo | Consultar sus cuentas, solicitar créditos, KYC, gestionar beneficiarios |
| ADMIN | Administrador | Gestionar usuarios, aprobar/rechazar solicitudes, dashboard |
| SUPER_ADMIN | Super administrador | Gestión de tipos de cambio, configuración global |
| CAJERO | Operador de caja | Realizar depósitos y retiros |
| ANALISTA_KYC | Revisor de KYC | Revisar y validar verificaciones de identidad |
| SISTEMA | Usuario automático | Procesos batch automáticos |

### 2.4 Permisos Granulares (48 permisos)

El sistema implementa permisos granulares para control fino:
- GESTIONAR_USUARIOS, GESTIONAR_SOCIOS, GESTIONAR_CREDITOS
- GESTIONAR_KYC, REVISAR_KYC, VER_KYC_ESTADISTICAS
- GESTIONAR_BENEFICIARIOS, EXPORTAR_ESTADO_CUENTA
- VER_AUDITORIA, GESTIONAR_PARAMETROS, GESTIONAR_TIPOS_CAMBIO
- y otros 38 permisos más

---

## 3. MÓDULOS DEL SISTEMA

---

### 3.1 MÓDULO: AUTENTICACIÓN (auth)

**Paquete Java:** `com.tufondo.auth`
**Responsabilidad:** Gestionar login, logout,刷新 de tokens, recuperación de contraseña y gestión de sesiones.

#### 3.1.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Login con credenciales | Validar usuario y password, generar JWT tokens |
| Login Web con cookies | Login específico para Flutter Web usando cookies httpOnly |
| Logout | Invalidar sesión y tokens |
| Refresh token | Generar nuevos access tokens usando refresh token |
| Obtener usuario actual | Devolver datos del usuario autenticado |
| Validar token | Verificar si un token JWT es válido |
| Crear usuario | Crear usuario vinculado a un socio existente |
| Recuperar password | Solicitar código/token para restablecer contraseña |
| Reset password | Establecer nueva contraseña con token válido |
| Cambiar password | Cambiar contraseña del usuario logueado |

#### 3.1.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| usuarios | Usuarios del sistema con credenciales |
| sesiones | Sesiones activas con refresh tokens |
| password_reset_tokens | Tokens de recuperación de contraseña |
| password_history | Historial de contraseñas (últimas 5, no reutilizables) |

#### 3.1.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/v1/auth/login | Login con credenciales |
| POST | /api/v1/auth/login-web | Login para web (cookies httpOnly) |
| POST | /api/v1/auth/logout | Cerrar sesión |
| POST | /api/v1/auth/logout-web | Cerrar sesión web |
| POST | /api/v1/auth/refresh | Refrescar access token |
| POST | /api/v1/auth/refresh-web | Refrescar token vía cookie |
| GET | /api/v1/auth/me | Obtener usuario actual |
| POST | /api/v1/auth/validar | Validar token JWT |
| POST | /api/v1/auth/crear-usuario | Crear usuario vinculado a socio |
| POST | /api/v1/auth/recuperar-password | Solicitar recuperación |
| POST | /api/v1/auth/reset-password | Restablecer contraseña |
| POST | /api/v1/auth/cambiar-password | Cambiar contraseña |

#### 3.1.4 Reglas de Seguridad del Módulo

| Regla | Valor |
|-------|-------|
| Límite de login | 5 intentos por minuto por IP |
| Bloqueo de cuenta | Después de 5 intentos fallidos |
| Historial de passwords | No permitir reutilizar últimas 5 contraseñas |
| Access token TTL | 15 minutos |
| Refresh token TTL | 7 días |

#### 3.1.5 JWT Token - Datos Contenidos

El access token JWT contiene:
- `userId`: UUID del usuario
- `nombreUsuario`: nombre de login
- `socioId`: UUID del socio asociado (puede ser null para admins)
- `rol`: Rol del usuario
- `authorities`: Permisos granulares

---

### 3.2 MÓDULO: ADMINISTRACIÓN (admin)

**Paquete Java:** `com.tufondo.admin`
**Responsabilidad:** Panel administrativo, dashboard con estadísticas, auditoría y gestión de usuarios/sesiones.

#### 3.2.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Dashboard estadístico | Estadísticas generales del sistema |
| Consulta de auditoría | Ver logs de eventos de seguridad |
| Gestión de usuarios | CRUD de usuarios del sistema |
| Activar/Desactivar usuario | Habilitar o deshabilitar usuarios |
| Control de sesiones | Ver sesiones activas e invalidarlas |
| Invalidar sesión específica | Cerrar una sesión particular |
| Invalidar todas las sesiones de usuario | Forzar logout total de un usuario |

#### 3.2.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| usuarios | Gestión de usuarios |
| sesiones | Control de sesiones activas |
| audit_log | Log de eventos de seguridad |

#### 3.2.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/admin/dashboard/estadisticas | Estadísticas del dashboard |
| GET | /api/v1/admin/auditoria | Consulta de logs de auditoría |
| GET | /api/v1/admin/usuarios | Listar usuarios |
| GET | /api/v1/admin/usuarios/{id} | Detalle de usuario |
| PUT | /api/v1/admin/usuarios/{id} | Actualizar usuario |
| POST | /api/v1/admin/usuarios/{id}/activar | Activar usuario |
| POST | /api/v1/admin/usuarios/{id}/desactivar | Desactivar usuario |
| GET | /api/v1/admin/sesiones | Listar sesiones activas |
| DELETE | /api/v1/admin/sesiones/{id} | Invalidar sesión |
| DELETE | /api/v1/admin/sesiones/usuario/{usuarioId} | Invalidar todas las sesiones |

---

### 3.3 MÓDULO: SOCIOS (socios)

**Paquete Java:** `com.tufondo.socios`
**Responsabilidad:** Gestión completa del ciclo de vida de los socios del fondo de ahorro.

#### 3.3.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Crear socio | Registrar nuevo socio en el sistema |
| Consultar socio | Obtener datos completos de un socio |
| Listar socios | Listar todos los socios con paginación |
| Buscar socios | Búsqueda por filtros |
| Actualizar socio | Modificar datos del socio |
| Activar/Desactivar socio | Cambiar estado del socio |
| Eliminar socio | Soft delete (marcado como eliminado) |
| Solicitud de registro | Registrar solicitud de nuevo socio |
| Aprobar solicitud | Admin aprueba solicitud de registro |
| Rechazar solicitud | Admin rechaza solicitud de registro |

#### 3.3.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| socios | Información de socios |
| solicitudes_registro | Solicitudes de nuevo registro |

#### 3.3.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/v1/socios | Crear socio |
| GET | /api/v1/socios/{id} | Obtener socio |
| GET | /api/v1/socios | Listar socios (paginado) |
| GET | /api/v1/socios/buscar | Buscar socios |
| PUT | /api/v1/socios/{id} | Actualizar socio |
| PATCH | /api/v1/socios/{id}/activar | Activar socio |
| PATCH | /api/v1/socios/{id}/desactivar | Desactivar socio |
| DELETE | /api/v1/socios/{id} | Eliminar socio |
| POST | /api/v1/socios/solicitud | Crear solicitud de registro |
| GET | /api/v1/socios/solicitudes | Listar solicitudes (admin) |
| POST | /api/v1/socios/solicitudes/{id}/aprobar | Aprobar solicitud |
| POST | /api/v1/socios/solicitudes/{id}/rechazar | Rechazar solicitud |

#### 3.3.4 Enums del Módulo

| Enum | Valores |
|------|---------|
| TipoDocumento | CEDULA_IDENTIDAD, RIF, PASAPORTE, CEDULA_EXTRANJERO |
| Genero | MASCULINO, FEMENINO, OTRO |
| EstadoCivil | SOLTERO, CASADO, VIUDO, DIVORCIADO, CONCUBINO |
| EstadoSocio | ACTIVO, INACTIVO, ELIMINADO, SUSPENDIDO |
| TipoContrato | FIJO, INDEFINIDO, CONTRATADO, PASANTE |

#### 3.3.5 Modelo de Datos - Socio

Un Socio contiene:
- Número de socio (identificador público)
- Tipo y número de documento
- Nombres y apellidos completos
- Fecha de nacimiento
- Género
- Estado civil
- Correo electrónico
- Teléfono principal y secundario
- Dirección de residencia (completa)
- Dirección laboral (completa)
- Datos laborales: empresa, departamento, cargo, tipo de contrato
- Salario
- Datos de nómina: número de cuenta, banco
- Contacto de emergencia: nombre, teléfono, parentesco
- Estado (ACTIVO, INACTIVO, etc.)
- Fecha de ingreso

#### 3.3.6 Rate Limiting

| Endpoint | Límite |
|----------|--------|
| /socios/** | 60 requests/minuto por IP |

---

### 3.4 MÓDULO: AHORROS (ahorros)

**Paquete Java:** `com.tufondo.ahorros`
**Responsabilidad:** Gestión de cuentas de ahorro, movimientos y rendimientos.

#### 3.4.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Listar cuentas de socio | Obtener todas las cuentas de un socio |
| Consultar cuenta | Obtener datos de una cuenta específica |
| Consultar saldo | Ver saldo actual de una cuenta |
| Historial de movimientos | Ver todas las transacciones |
| Realizar depósito | Acreditar fondos a la cuenta |
| Realizar retiro | Debitar fondos de la cuenta |
| Generar rendimientos | Cálculo automático de intereses |

#### 3.4.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| cuentas_ahorro | Cuentas de los socios |
| movimientos | Historial de transacciones |
| rendimientos | Intereses generados |

#### 3.4.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/cuentas/socio/{socioId} | Listar cuentas de socio |
| GET | /api/v1/cuentas/{numeroCuenta} | Obtener cuenta |
| GET | /api/v1/cuentas/{numeroCuenta}/saldo | Consultar saldo |
| GET | /api/v1/cuentas/{numeroCuenta}/movimientos | Historial de movimientos |
| POST | /api/v1/cuentas/{numeroCuenta}/depositos | Realizar depósito |
| POST | /api/v1/cuentas/{numeroCuenta}/retiros | Realizar retiro |

#### 3.4.4 Enums del Módulo

| Enum | Valores |
|------|---------|
| EstadoCuenta | ACTIVA, INACTIVA, CERRADA, BLOQUEADA |
| TipoCuenta | AHORRO, NOMINA, PROGRAMADA |
| Moneda | VES, USD |
| TipoMovimiento | DEPOSITO, RETIRO, INTERES_CREDITO, COMISION, TRANSFERENCIA |
| EstadoMovimiento | PROCESADO, PENDIENTE, CANCELADO |
| CanalOrigen | SUCURSAL, ATM, WEB, MOVIL, BATCH |

#### 3.4.5 Formato de Identificadores

| Recurso | Formato | Ejemplo |
|---------|---------|---------|
| Número de cuenta | AHO-YYYY-XXXXXX | AHO-2026-000001 |
| Número de movimiento | MOV-YYYY-XXXXXX | MOV-2026-000001 |

#### 3.4.6 Reglas de Negocio (RN)

| RN | Descripción |
|----|-------------|
| RN-001 | Un socio solo puede tener una cuenta por tipo (ej: solo una cuenta de ahorro VES) |
| RN-003 | El saldo actual nunca puede ser negativo |
| RN-004 | El monto mínimo requerido debe ser >= 0.0001 |
| RN-005 | Una cuenta en estado CERRADA no permite operaciones |
| RN-006 | Los movimientos son INMUTABLES una vez creados |

#### 3.4.7 Tasa de Interés para Ahorro

| Parámetro | Valor |
|-----------|-------|
| TASA_INTERES_AHORRO | 5% anual |

---

### 3.5 MÓDULO: BENEFICIARIOS (beneficiarios)

**Paquete Java:** `com.tufondo.beneficiarios`
**Responsabilidad:** Gestión de beneficiarios designados por los socios para recibir fondos en caso de fallecimiento.

#### 3.5.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Crear beneficiario | Agregar beneficiario a un socio |
| Listar beneficiarios | Ver todos los beneficiarios de un socio |
| Consultar beneficiario | Ver detalle de un beneficiario |
| Actualizar beneficiario | Modificar datos del beneficiario |
| Eliminar beneficiario | Soft delete de beneficiario |
| Auditoría de cambios | Registrar todos los cambios en tabla de auditoría |

#### 3.5.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| beneficiaries | Beneficiarios designados |
| beneficiaries_audit | Auditoría de cambios |

#### 3.5.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/v1/socios/{socioId}/beneficiarios | Crear beneficiario |
| GET | /api/v1/socios/{socioId}/beneficiarios | Listar beneficiarios |
| GET | /api/v1/socios/{socioId}/beneficiarios/{id} | Obtener beneficiario |
| PUT | /api/v1/socios/{socioId}/beneficiarios/{id} | Actualizar beneficiario |
| DELETE | /api/v1/socios/{socioId}/beneficiarios/{id} | Eliminar (soft delete) |

#### 3.5.4 Enums del Módulo

| Enum | Valores |
|------|---------|
| Parentesco | CONYUGE, HIJO, PADRE, MADRE, HERMANO, ABUELO, NIETO, SOBRINO, TIO, OTRO |
| TipoDocumento | CEDULA_IDENTIDAD, RIF, PASAPORTE, CEDULA_EXTRANJERO |

#### 3.5.5 Validaciones

| Validación | Rango |
|------------|-------|
| Porcentaje de asignación | 0.01% - 100.00% |
| Total por socio | Debe sumar 100% entre todos los beneficiarios |

---

### 3.6 MÓDULO: CRÉDITOS (creditos)

**Paquete Java:** `com.tufondo.creditos`
**Responsabilidad:** Gestión completa de solicitudes de crédito, simulación, evaluación, aprobación y desembolso.

#### 3.6.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Listar tipos de crédito | Ver productos crediticios disponibles |
| Simular crédito | Calcular cuota y plan de amortización |
| Solicitar crédito | Crear nueva solicitud de crédito |
| Ver mis solicitudes | Listar solicitudes propias del socio |
| Ver detalle de solicitud | Información completa de una solicitud |
| Ver plan de amortización | Tabla de cuotas con intereses |
| Evaluar solicitud | Analista evalúa riesgo crediticio |
| Aprobar crédito | Admin aprobar solicitud |
| Rechazar crédito | Admin rechazar con motivo |
| Desembolsar crédito | Admin ejecuta desembolso |
| Consultar cuotas | Ver estado de cuotas del crédito |
| Gestión de tipos (admin) | CRUD de tipos de crédito |

#### 3.6.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| tipos_credito | Catálogo de productos crediticios |
| solicitudes_credito | Solicitudes de crédito |
| evaluaciones_crediticias | Evaluaciones de riesgo |
| planes_amortizacion | Planes de pago (sistema francés) |
| amortizaciones | Cuotas individuales |
| cuenta_garantia | Colateral de garantía |

#### 3.6.3 Endpoints REST - Socio

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/creditos/tipos-credito | Listar tipos disponibles |
| POST | /api/v1/creditos/simular | Simular crédito |
| POST | /api/v1/creditos/solicitudes | Crear solicitud |
| GET | /api/v1/creditos/solicitudes/socio/{socioId} | Mis solicitudes |
| GET | /api/v1/creditos/solicitudes/{numero} | Detalle de solicitud |
| GET | /api/v1/creditos/solicitudes/{numero}/plan | Plan de amortización |
| GET | /api/v1/creditos/{numero} | Detalle crédito |
| GET | /api/v1/creditos/{numero}/cuotas | Listar cuotas |

#### 3.6.4 Endpoints REST - Admin

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/admin/tipos-credito | Listar todos los tipos |
| POST | /api/v1/admin/tipos-credito | Crear tipo de crédito |
| PUT | /api/v1/admin/tipos-credito/{id} | Actualizar tipo |
| POST | /api/v1/admin/tipos-credito/{id}/activar | Activar tipo |
| POST | /api/v1/admin/tipos-credito/{id}/desactivar | Desactivar tipo |
| GET | /api/v1/admin/creditos/solicitudes | Listar solicitudes |
| GET | /api/v1/admin/creditos/solicitudes/{numero} | Detalle solicitud |
| POST | /api/v1/admin/creditos/solicitudes/{numero}/evaluar | Evaluar |
| POST | /api/v1/admin/creditos/solicitudes/{numero}/aprobar | Aprobar |
| POST | /api/v1/admin/creditos/solicitudes/{numero}/rechazar | Rechazar |
| POST | /api/v1/admin/creditos/solicitudes/{numero}/desembolsar | Desembolsar |
| GET | /api/v1/admin/creditos/solicitudes/{numero}/plan | Plan de amortización |

#### 3.6.5 Flujo de Estados de Solicitud

```
PENDIENTE → EN_REVISION → APROBADA → DESEMBOLSADO → CANCELADO
                    ↓
               RECHAZADA
```

#### 3.6.6 Tipos de Crédito Configurados

| Código | Nombre | Tasa Anual | Plazo Max | Monto Max | Colateral |
|--------|--------|------------|-----------|-----------|-----------|
| MICRO_CRED | Micro Crédito | 24% | 12 meses | 50,000 | 10% |
| CRED_PERSONAL | Crédito Personal | 14.5% | 48 meses | 200,000 | 15% |
| CRED_VEHICULO | Crédito Vehículo | 10.5% | 72 meses | 1,000,000 | 20% |
| CRED_HIPOTECARIO | Crédito Hipotecario | 8.5% | 240 meses | 5,000,000 | 30% |
| CRED_EDUCATIVO | Crédito Educativo | 9.5% | 60 meses | 500,000 | 10% |
| CRED_EMPRENDEDOR | Crédito Emprendedor | 18% | 36 meses | 150,000 | 15% |
| TARJETA_CREDITO | Tarjeta de Crédito | 22% | - | 100,000 | 0% |

#### 3.6.7 Sistema de Amortización

El sistema utiliza **Sistema Francés** con:
- Cuota fija mensual
- Interés sobre saldo insoluto
- Amortización creciente del capital

#### 3.6.8 Formato de Identificadores

| Recurso | Formato | Ejemplo |
|---------|---------|---------|
| Número de solicitud | SOL-CRED-YYYY-XXXXXX | SOL-CRED-2026-000001 |

---

### 3.7 MÓDULO: KYC - VERIFICACIÓN DE IDENTIDAD (kyc)

**Paquete Java:** `com.tufondo.kyc`
**Responsabilidad:** Proceso de Know Your Customer (KYC) con carga de documentos y validación de identidad.

#### 3.7.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Iniciar proceso KYC | Comenzar verificación de identidad |
| Consultar estado | Ver estado actual del KYC |
| Subir documento | Cargar documento de identidad |
| Eliminar documento | Eliminar documento cargado |
| Enviar a validación | Enviar documentos para revisión |
| Revocar consentimiento | Retirar consentimiento LOPDP |
| Historial de verificaciones | Ver historial de KYC |
| Cola de revisión (admin) | Ver solicitudes pendientes |
| Revisar KYC (admin) | Aprobar o rechazar verificación |
| Ver detalle de revisión (admin) | Ver documentos y datos |

#### 3.7.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| verificacion_kyc | Verificaciones KYC |
| documento_identidad | Documentos cargados |
| consentimiento_kyc | Consentimientos LOPDP |
| audit_kyc | Auditoría compliance |

#### 3.7.3 Endpoints REST - Socio

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/v1/kyc/iniciar | Iniciar proceso KYC |
| GET | /api/v1/kyc/estado | Consultar estado |
| POST | /api/v1/kyc/documentos | Subir documento |
| DELETE | /api/v1/kyc/documentos/{id} | Eliminar documento |
| POST | /api/v1/kyc/enviar | Enviar a validación |
| POST | /api/v1/kyc/revocar | Revocar consentimiento |
| GET | /api/v1/kyc/historial | Historial de verificaciones |

#### 3.7.4 Endpoints REST - Admin/Analista

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/admin/kyc/cola-revision | Cola de revisión |
| GET | /api/v1/admin/kyc/revision/{id} | Detalle revisión |
| POST | /api/v1/admin/kyc/revision/{id}/{action} | Aprobar/rechazar |

#### 3.7.5 Niveles de Verificación

| Nivel | Documentos Requeridos |
|-------|----------------------|
| BASICO | 1 documento (cédula) |
| MEDIO | 2 documentos (cédula + comprobante) |
| COMPLETO | 3+ documentos |

#### 3.7.6 Estados de Verificación

| Estado | Descripción |
|--------|-------------|
| PENDIENTE | Socilio iniciado, esperando documentos |
| EN_REVISION | Enviado, en cola de revisión |
| APROBADO | Verificación exitosa |
| RECHAZADO | Rechazado, debe reenviar |
| REENVIADO | Reenviado después de rechazo |
| EXPIRADO | Por tiempo de vigencia |
| CANCELADO | Revocado por el socio |

#### 3.7.7 Tipos de Documentos KYC

| Tipo | Descripción |
|------|-------------|
| CEDULA_IDENTIDAD | Cédula de identidad venezolana |
| RIF | Registro de Información Fiscal |
| PASAPORTE | Pasaporte |
| COMPROBANTE_DOMICILIO | Comprobante de domicilio reciente |
| CONSTANCIA_LABORAL | Constancia de trabajo |

---

### 3.8 MÓDULO: PARÁMETROS (parametros)

**Paquete Java:** `com.tufondo.parametros`
**Responsabilidad:** Almacenar y gestionar parámetros configurables del sistema como tasas, límites y comisiones.

#### 3.8.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Listar parámetros | Ver todos los parámetros |
| Filtrar por categoría | Ver parámetros de una categoría |
| Obtener uno | Ver valor de un parámetro específico |
| Actualizar | Modificar valor de un parámetro (admin) |

#### 3.8.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| parametros_sistema | Parámetros del sistema |

#### 3.8.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/parametros | Listar todos |
| GET | /api/v1/parametros/categoria/{categoria} | Por categoría |
| GET | /api/v1/parametros/{key} | Obtener uno |
| PUT | /api/v1/parametros/{key} | Actualizar (admin) |

#### 3.8.4 Categorías de Parámetros

| Categoría | Descripción |
|-----------|-------------|
| TASA | Tasas de interés |
| LIMITE | Límites de operaciones |
| COMISION | Comisiones |
| CUENTA | Configuración de cuentas |
| KYC | Verificación de identidad |
| SISTEMA | Configuración general |

#### 3.8.5 Parámetros Configurados

| Key | Valor | Descripción |
|-----|-------|-------------|
| TASA_INTERES_AHORRO | 0.05 | 5% anual para cuentas de ahorro |
| TASA_INTERES_MORA | 0.024 | 2.4% mensual para mora |
| LIMITE_RETIRO_DIARIO | 5,000,000 VES | Límite diario de retiro |
| LIMITE_DEPOSITO_DIARIO | 10,000,000 VES | Límite diario de depósito |
| MONEDA_PRINCIPAL | VES | Moneda principal |
| MONEDA_SECUNDARIA | USD | Moneda secundaria |
| TASA_CAMBIO_USD | 50.00 | Tasa de cambio default |
| KYC_DIAS_EXPIRACION | 365 | Días de vigencia KYC |

---

### 3.9 MÓDULO: TIPO DE CAMBIO (tipocambio)

**Paquete Java:** `com.tufondo.tipocambio`
**Responsabilidad:** Gestión de tasas de cambio USD/VES.

#### 3.9.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Ver tasa actual | Obtener tasa de cambio vigente |
| Ver tasa por fecha | Consultar tasa de una fecha específica |
| Ver historial | Historial de tasas de cambio |
| Listar todas (admin) | Ver todas las tasas registradas |
| Crear tasa (SUPER_ADMIN) | Registrar nueva tasa |
| Actualizar tasa | Modificar tasa existente |
| Eliminar tasa | Eliminar registro de tasa |

#### 3.9.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| tipos_cambio | Tasas de cambio |

#### 3.9.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/tipos-cambio/actual | Tasa actual |
| GET | /api/v1/tipos-cambio/fecha/{fecha} | Por fecha |
| GET | /api/v1/tipos-cambio/historial | Historial |
| GET | /api/v1/tipos-cambio | Listar todos (admin) |
| POST | /api/v1/tipos-cambio | Crear (SUPER_ADMIN) |
| PUT | /api/v1/tipos-cambio/{id} | Actualizar |
| DELETE | /api/v1/tipos-cambio/{id} | Eliminar |

#### 3.9.4 Modelo de TipoCambio

| Campo | Descripción |
|-------|-------------|
| fecha | Fecha de la tasa |
| tasaCompra | Tasa para compra de USD |
| tasaVenta | Tasa para venta de USD |
| fuente | Fuente de la tasa |

---

### 3.10 MÓDULO: DOCUMENTOS PDF (documentospdf)

**Paquete Java:** `com.tufondo.documentospdf`
**Responsabilidad:** Generación de documentos PDF con y sin firma digital.

#### 3.10.1 Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| Generar estado de cuenta | PDF con resumen de cuenta |
| Generar constancia de afiliación | Certificado de ser socio |
| Generar contrato | Contrato de adhesión con firma digital |
| Generar pagaré | Documento pagaré con firma digital |
| Generar tabla de amortización | Tabla del plan de pago |
| Generar carta de beneficiarios | Carta a beneficiario |
| Metadata de documento | Ver información del documento |
| Descargar documento | Obtener URL para descarga |
| Listar documentos por socio | Ver todos los documentos de un socio |

#### 3.10.2 Tablas de Datos

| Tabla | Propósito |
|-------|-----------|
| documentos_pdf | Documentos generados |
| documentos_pdf_audit | Auditoría de documentos |

#### 3.10.3 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/v1/documentos/estado-cuenta/{cuentaId} | Generar estado de cuenta |
| GET | /api/v1/documentos/constancia-afiliacion/{socioId} | Constancia de afiliación |
| GET | /api/v1/documentos/contrato/{solicitudId} | Contrato (firma digital) |
| GET | /api/v1/documentos/pagare/{creditoId} | Pagaré (firma digital) |
| GET | /api/v1/documentos/tabla-amortizacion/{creditoId} | Tabla de amortización |
| GET | /api/v1/documentos/carta-beneficiarios/{socioId} | Carta de beneficiarios |
| GET | /api/v1/documentos/{documentoId} | Metadata del documento |
| GET | /api/v1/documentos/{documentoId}/descargar | Descargar (pre-signed URL) |
| GET | /api/v1/documentos/socio/{socioId} | Listar documentos |

#### 3.10.4 Tipos de Documento

| Tipo | ¿Firma Digital? | ¿Expira? | Días Exp. |
|------|-----------------|----------|-----------|
| ESTADO_CUENTA | No | Sí | 7 |
| CONSTANCIA_AFILIACION | No | Sí | 365 |
| CONTRATO_ADHESION | Sí | No | - |
| PAGARE | Sí | No | - |
| TABLA_AMORTIZACION | No | Sí | 30 |
| CARTA_BENEFICIARIOS | No | Sí | 30 |

#### 3.10.5 Clasificaciones de Documentos

| Clasificación | Uso |
|---------------|-----|
| CONFIDENCIAL | Valor por defecto para documentos privados |
| RESTRINGIDO | Documentos con acceso muy limitado |
| PUBLICO | Documentos sin restricciones |

#### 3.10.6 Configuración de Firma Digital

| Parámetro | Valor |
|-----------|-------|
| Algoritmo | SHA256withRSA |
| Keystore | PKCS12 (.p12) |
| Path | /etc/fondo/firma-digital.p12 |

---

## 4. ESTRUCTURA DEL FRONTEND (Next.js)

### 4.1 Rutas de Autenticación (públicas)

| Ruta | Componente | Descripción |
|------|------------|-------------|
| /login | LoginPage | Formulario de inicio de sesión |
| /registro | RegistroPage | Formulario de registro |
| /recuperar-password | RecuperarPasswordPage | Solicitar recuperación |
| /reset-password | ResetPasswordPage | Nueva contraseña con token |

### 4.2 Rutas del Socio (dashboard)

| Ruta | Componente | Descripción |
|------|------------|-------------|
| /dashboard | DashboardPage | Vista principal con resumen |
| /dashboard/cuentas | CuentasPage | Listado de cuentas de ahorro |
| /dashboard/cuentas/[numero] | CuentaDetallePage | Información de cuenta específica |
| /dashboard/cuentas/[numero]/movimientos | MovimientosPage | Historial de transacciones |
| /dashboard/cuentas/[numero]/saldo | SaldoPage | Consultar saldo |
| /dashboard/creditos | CreditosPage | Listado de créditos del socio |
| /dashboard/creditos/solicitar | SolicitarCreditoPage | Formulario de solicitud |
| /dashboard/creditos/simulador | SimuladorPage | Simulador de créditos |
| /dashboard/creditos/[numero] | CreditoDetallePage | Información del crédito |
| /dashboard/beneficiarios | BeneficiariosPage | Gestión de beneficiarios |
| /dashboard/kyc | KYCPage | Verificación de identidad |
| /dashboard/documentos | DocumentosPage | Centro de documentos |
| /dashboard/simulador | SimuladorGeneralPage | Simulador general |
| /perfil | PerfilPage | Perfil del usuario socio |

### 4.3 Rutas Administrativas

| Ruta | Componente | Descripción |
|------|------------|-------------|
| /admin | AdminDashboardPage | Panel principal admin |
| /admin/auditoria | AuditoriaPage | Logs de auditoría |
| /admin/configuracion | ConfiguracionPage | Parámetros del sistema |
| /admin/creditos | AdminCreditosPage | Gestión de solicitudes |
| /admin/creditos/[numero] | AdminCreditoDetallePage | Detalle completo |
| /admin/kyc | AdminKYCPage | Cola de revisión KYC |
| /admin/kyc/[id] | AdminKYCDetallePage | Revisión de verificación |
| /admin/reportes | ReportesPage | Menú de reportes |
| /admin/reportes/creditos | ReporteCreditosPage | Reporte de créditos |
| /admin/reportes/estado-cuenta | ReporteEstadosPage | Reporte de estados |
| /admin/reportes/socios | ReporteSociosPage | Reporte de socios |
| /admin/sesiones | SesionesPage | Control de sesiones |
| /admin/socios | AdminSociosPage | Gestión de socios |
| /admin/socios/[id] | AdminSocioDetallePage | Detalle de socio |
| /admin/solicitudes | AdminSolicitudesPage | Solicitudes de registro |
| /admin/tipos-cambio | AdminTiposCambioPage | Tasas de cambio |
| /admin/tipos-credito | AdminTiposCreditoPage | Productos crediticios |
| /admin/usuarios | AdminUsuariosPage | Gestión de usuarios |

### 4.4 API Routes del Frontend (Proxy al Backend)

El frontend actúa como proxy transladando requests al backend:

#### Auth APIs
| Ruta Frontend | Método | Destino Backend |
|---------------|--------|-----------------|
| /api/auth/login | POST | POST /api/v1/auth/login |
| /api/auth/logout | POST | POST /api/v1/auth/logout |
| /api/auth/me | GET | GET /api/v1/auth/me |
| /api/auth/cambiar-password | POST | POST /api/v1/auth/cambiar-password |
| /api/auth/registro | POST | POST /api/v1/auth/crear-usuario |
| /api/auth/recuperar-password | POST | POST /api/v1/auth/recuperar-password |
| /api/auth/reset-password | POST | POST /api/v1/auth/reset-password |

#### Cuentas APIs
| Ruta Frontend | Método | Destino Backend |
|---------------|--------|-----------------|
| /api/cuentas/socio/[socioId] | GET | GET /api/v1/cuentas/socio/{socioId} |
| /api/cuentas/[numeroCuenta]/saldo | GET | GET /api/v1/cuentas/{numero}/saldo |
| /api/cuentas/[numeroCuenta]/movimientos | GET | GET /api/v1/cuentas/{numero}/movimientos |
| /api/cuentas/[numeroCuenta]/depositos | POST | POST /api/v1/cuentas/{numero}/depositos |
| /api/cuentas/[numeroCuenta]/retiros | POST | POST /api/v1/cuentas/{numero}/retiros |

#### Créditos APIs
| Ruta Frontend | Método | Destino Backend |
|---------------|--------|-----------------|
| /api/creditos/tipos-credito | GET | GET /api/v1/creditos/tipos-credito |
| /api/creditos/simulador | POST | POST /api/v1/creditos/simular |
| /api/creditos/solicitudes | POST | POST /api/v1/creditos/solicitudes |
| /api/creditos/solicitudes/socio/[socioId] | GET | GET /api/v1/creditos/solicitudes/socio/{socioId} |
| /api/creditos/solicitudes/[numero]/plan | GET | GET /api/v1/creditos/solicitudes/{numero}/plan |

#### KYC APIs
| Ruta Frontend | Método | Destino Backend |
|---------------|--------|-----------------|
| /api/kyc/estado | GET | GET /api/v1/kyc/estado |
| /api/kyc/documentos | POST | POST /api/v1/kyc/documentos |
| /api/kyc/enviar | POST | POST /api/v1/kyc/enviar |

#### Admin APIs
| Ruta Frontend | Método | Destino Backend |
|---------------|--------|-----------------|
| /api/admin/dashboard/estadisticas | GET | GET /api/v1/admin/dashboard/estadisticas |
| /api/admin/kyc/cola-revision | GET | GET /api/v1/admin/kyc/cola-revision |
| /api/admin/kyc/revision/[id]/[action] | POST | POST /api/v1/admin/kyc/revision/{id}/{action} |
| /api/admin/tipos-credito/[id] | GET/PUT | GET/PUT /api/v1/admin/tipos-credito/{id} |
| /api/admin/tipos-cambio | GET/POST | GET/POST /api/v1/tipos-cambio |
| /api/admin/parametros | GET/PUT | GET/PUT /api/v1/parametros |
| /api/admin/creditos/solicitudes/[numero]/aprobar | POST | POST /api/v1/admin/creditos/solicitudes/{numero}/aprobar |
| /api/admin/creditos/solicitudes/[numero]/rechazar | POST | POST /api/v1/admin/creditos/solicitudes/{numero}/rechazar |
| /api/admin/creditos/solicitudes/[numero]/desembolsar | POST | POST /api/v1/admin/creditos/solicitudes/{numero}/desembolsar |

---

## 5. BASE DE DATOS

### 5.1 Esquema de Tablas Principales

#### Tablas de Autenticación
| Tabla | Propósito |
|-------|-----------|
| usuarios | Usuarios del sistema |
| sesiones | Sesiones JWT activas |
| password_reset_tokens | Tokens de recuperación |
| password_history | Historial de contraseñas |

#### Tablas de Negocio
| Tabla | Propósito |
|-------|-----------|
| socios | Información de socios |
| solicitudes_registro | Solicitudes de registro |
| cuentas_ahorro | Cuentas de ahorro |
| movimientos | Transacciones |
| rendimientos | Intereses generados |
| beneficiaries | Beneficiarios |
| beneficiaries_audit | Auditoría de beneficiarios |
| tipos_credito | Productos crediticios |
| solicitudes_credito | Solicitudes de crédito |
| evaluaciones_crediticias | Evaluaciones de riesgo |
| planes_amortizacion | Planes de pago |
| amortizaciones | Cuotas |
| cuenta_garantia | Colateral |

#### Tablas de KYC
| Tabla | Propósito |
|-------|-----------|
| verificacion_kyc | Verificaciones KYC |
| documento_identidad | Documentos cargados |
| consentimiento_kyc | Consentimientos LOPDP |
| audit_kyc | Auditoría compliance |

#### Tablas de Documentos
| Tabla | Propósito |
|-------|-----------|
| documentos_pdf | Documentos generados |
| documentos_pdf_audit | Auditoría de documentos |

#### Tablas de Configuración
| Tabla | Propósito |
|-------|-----------|
| tipos_cambio | Tasas de cambio |
| parametros_sistema | Parámetros |

#### Tablas de Auditoría
| Tabla | Propósito |
|-------|-----------|
| audit_log | Logs de eventos |

### 5.2 Migraciones Flyway

| Versión | Nombre | Descripción |
|---------|--------|-------------|
| V1 | initial_schema | Schema base |
| V2 | migrate_cuentas_ahorro_to_uuid | Migración cuentas a UUID |
| V3 | migrate_movimientos_to_uuid | Migración movimientos |
| V4 | migrate_rendimientos_to_uuid | Migración rendimientos |
| V5 | create_kyc_tables | Tablas KYC |
| V6 | create_documentos_pdf | Tablas documentos PDF |
| V7 | add_debe_cambiar_password_column | Columna password |
| V8 | create_password_history | Historial passwords |
| V9 | add_registro_venezuela_fields | Campos Venezuela |
| V10 | create_verificacion_token | Tokens de verificación |
| V11 | create_parametros_sistema | Parámetros del sistema |
| V20260419_001 | create_beneficiaries | Tabla beneficiarios |

---

## 6. SEGURIDAD IMPLEMENTADA

### 6.1 Autenticación

| Característica | Implementación |
|----------------|----------------|
| Tipo de token | JWT |
| Algoritmo | HS384 (symmetric) |
| Access token TTL | 15 minutos |
| Refresh token TTL | 7 días |
| Refresh token storage | Base de datos (tabla sesiones) |
| Rate limiting login | 5 intentos/minuto por IP |
| Bloqueo de cuenta | 5 intentos fallidos |

### 6.2 Autorización

| Característica | Implementación |
|----------------|----------------|
| Framework | Spring Security |
| Anotaciones | @PreAuthorize |
| Roles | 6 roles predefinidos |
| Permisos | 48 permisos granulares |
| Validación IDOR | Verificación en todos los endpoints |

### 6.3 Rate Limiting por Módulo

| Módulo | Límite |
|--------|--------|
| Login | 5/min |
| Registro | 3/min |
| Admin | 30/min |
| Socios | 60/min |
| Cuentas | 30/min |
| Simulador | 10/min |
| Documentos | 20/min |

### 6.4 Protección de Datos

| Medida | Descripción |
|--------|-------------|
| CORS | Configurado con orígenes específicos |
| Headers seguridad | X-Frame-Options, X-Content-Type-Options, etc. |
| Validación input | Bean Validation (JSR-380) |
| SQL Injection | Previenido por JPA/Hibernate |
| XSS | Validación de inputs |
| Password hashing | BCrypt |

---

## 7. INTEGRACIONES EXTERNAS

### 7.1 MinIO (Object Storage)

Almacenamiento de archivos compatible con S3:
- Bucket KYC: bucket-kyc (documentos de identidad)
- Bucket Documents: bucket-documentos (estados de cuenta)
- Bucket Contratos: bucket-contratos
- Bucket Pagarés: bucket-pagares
- Bucket Créditos: bucket-creditos
- Bucket Temporal: bucket-temporal

### 7.2 Redis

- Rate limiting centralizado
- Almacenamiento de sesiones
- Cache de parámetros

### 7.3 PostgreSQL

- Base de datos principal
- Migraciones via Flyway

---

## 8. CONFIGURACIONES FINANCIERAS

### 8.1 Tasas de Interés

| Concepto | Valor | Tipo |
|----------|-------|------|
| Interés ahorro | 5% | Anual |
| Interés mora | 2.4% | Mensual |

### 8.2 Límites de Operaciones (VES)

| Operación | Límite |
|-----------|--------|
| Retiro diario | 5,000,000 |
| Depósito diario | 10,000,000 |
| Transferencia | 5,000,000 |
| Crédito mínimo | 1,000,000 |
| Crédito máximo | 100,000,000 |

### 8.3 Comisiones (VES)

| Concepto | Monto |
|----------|-------|
| Comisión retiro | 1,000 |
| Comisión transferencia | 500 |
| ATM otro banco | 2,000 |

---

## 9. VARIABLES DE ENTORNO

### 9.1 Backend (application.yml)

```
# Database
DB_URL=jdbc:postgresql://localhost:5432/fondo
DB_USER=app
DB_PASS=<password>

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=<password>

# JWT
JWT_SECRET=<very-long-secret-key>
JWT_ISSUER=fondo-ahorro-platform
JWT_ACCESS_EXPIRATION_MINUTES=15
JWT_REFRESH_EXPIRATION_DAYS=7

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=<password>

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:18081

# ClamAV (opcional para escaneo de archivos)
CLAMAV_HOST=localhost
CLAMAV_PORT=3310
CLAMAV_ENABLED=true

# Firma Digital
DOC_FIRMA_KEYSTORE=/etc/fondo/firma-digital.p12
DOC_FIRMA_PASSWORD=<password>
DOC_FIRMA_KEY_ALIAS=documentos-fondo
```

### 9.2 Frontend (.env.local)

```
NEXT_PUBLIC_API_URL=http://localhost:18080/api
```

---

## 10. USUARIOS DE PRUEBA

### 10.1 Admin por Defecto

| Campo | Valor |
|-------|-------|
| Usuario | admin |
| Password | Admin123! |
| Rol | ADMIN |
| ID | a1111111-1111-1111-1111-111111111111 |

### 10.2 Usuario Socio de Prueba

| Campo | Valor |
|-------|-------|
| Usuario | gamijoam |
| Password | GaboMac12* |
| Socio ID | e08ef6d3-4dd0-4648-8268-c94271742488 |

---

## 11. ESTRUCTURA DE PROYECTOS

```
/fondo-ahorro-platform/
├── backend/
│   ├── src/main/java/com/tufondo/
│   │   ├── auth/                    # Autenticación y sesiones
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   └── infrastructure/
│   │   │       ├── security/
│   │   │       └── persistence/
│   │   ├── admin/                   # Panel administrativo
│   │   │   ├── api/controller/
│   │   │   ├── application/
│   │   │   └── domain/
│   │   ├── socios/                  # Gestión de socios
│   │   │   ├── api/controller/
│   │   │   ├── application/
│   │   │   └── domain/
│   │   ├── ahorros/                 # Cuentas de ahorro
│   │   │   ├── api/controller/
│   │   │   ├── application/
│   │   │   └── domain/
│   │   ├── beneficiarios/           # Beneficiarios
│   │   ├── creditos/                # Sistema de créditos
│   │   │   ├── api/controller/
│   │   │   ├── application/
│   │   │   └── domain/
│   │   ├── kyc/                     # Verificación de identidad
│   │   │   ├── api/controller/
│   │   │   ├── application/
│   │   │   └── domain/
│   │   ├── parametros/              # Configuración
│   │   ├── tipocambio/              # Tasas de cambio
│   │   ├── documentospdf/            # Documentos PDF
│   │   │   ├── api/controller/
│   │   │   ├── application/
│   │   │   ├── domain/
│   │   │   └── infrastructure/
│   │   ├── core/                    # Puertos y compartidos
│   │   └── config/                  # Configuración
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/            # Flyway migrations
│   └── pom.xml
│
├── frontend-web/
│   ├── src/
│   │   ├── app/                     # Rutas Next.js App Router
│   │   │   ├── (auth)/              # Rutas públicas
│   │   │   ├── (dashboard)/         # Dashboard socio
│   │   │   ├── admin/               # Panel admin
│   │   │   └── api/                 # API routes (proxy)
│   │   ├── components/              # Componentes React
│   │   │   ├── ui/                  # Componentes base
│   │   │   ├── forms/               # Formularios
│   │   │   └── layouts/            # Layouts
│   │   ├── hooks/                   # Custom hooks
│   │   ├── lib/                     # Utilidades
│   │   ├── stores/                  # Zustand stores
│   │   └── types/                   # Tipos TypeScript
│   ├── package.json
│   └── next.config.js
│
├── infrastructure/
│   ├── docker-compose.yml
│   ├── postgres-init.sql
│   ├── seed_admin.sql
│   ├── seed_socio.sql
│   ├── seed_tipos_credito.sql
│   └── SEED_ADMIN.md
│
├── .env.example
├── pom.xml (parent)
└── README.md
```

---

## 12. FLUJOS PRINCIPALES DE USUARIO

### 12.1 Flujo de Login

1. Usuario abre /login
2. Ingresa usuario y password
3. Frontend envía a /api/auth/login
4. Backend valida credenciales
5. Si válido, genera access token y refresh token
6. Frontend almacena tokens (cookies httpOnly para web)
7. Redirecciona a /dashboard

### 12.2 Flujo de Solicitud de Crédito

1. Socio ve tipos de crédito disponibles
2. Usa simulador para calcular cuota
3. Llena formulario de solicitud
4. Adjunta colateral (cuenta de garantía)
5. Envía solicitud → estado PENDIENTE
6. Admin revisa y cambia a EN_REVISION
7. Admin evalúa riesgo
8. Admin APRUEBA o RECHAZA
9. Si aprobado, Admin desembolsa → DESEMBOLSADO
10. Socio ve cuotas y plan de amortización

### 12.3 Flujo de KYC

1. Socio inicia KYC
2. Selecciona nivel (BASICO, MEDIO, COMPLETO)
3. Sube documentos requeridos
4. Da consentimiento LOPDP
5. Envía a validación
6. Analista revisa cola
7. Aprueba o rechaza
8. Si rechazado, socio puede reenviar

### 12.4 Flujo de Beneficiarios

1. Socio va a /dashboard/beneficiarios
2. Agrega beneficiario con datos y porcentaje
3. Sistema valida porcentaje (0.01-100)
4. Guarda con auditoría
5. Puede actualizar o eliminar

---

## 13. COLORES Y DISEÑO

### 13.1 Paleta de Colores

| Color | Uso |
|-------|-----|
| Blanco | Fondo principal |
| Verde claro | Acentos, estados exitosos |
| Azul claro | Botones primarios, links |

### 13.2 Moneda de Display

| Moneda | Símbolo |
|--------|---------|
| Bolívar | Bs |
| Dólar | $ |

---

## 14. ENDPOINTS EXTERNOS DEL FRONTEND (Proxy APIs)

### 14.1 Socios APIs

| Ruta | Método | Descripción |
|------|--------|-------------|
| /api/v1/socios/[id] | GET | Obtener socio |
| /api/v1/socios/[id]/perfil | PUT | Actualizar perfil |

---

## 15. NOTAS DE IMPLEMENTACIÓN

### 15.1 Puerto del Backend

El backend corre en el puerto 18080 por defecto.

### 15.2 Puerto del Frontend

El frontend Next.js corre en el puerto 3000.

### 15.3 Puerto de MinIO

MinIO corre en el puerto 9000 (consola en 9001).

### 15.4 Puerto de PostgreSQL

PostgreSQL corre en el puerto 5432.

### 15.5 Puerto de Redis

Redis corre en el puerto 6379.

---

## 16. ESTADO ACTUAL DEL PROYECTO

### 16.1 Funcionalidades Completadas

- Sistema de autenticación JWT completo
- CRUD de socios
- Cuentas de ahorro con movimientos
- Sistema de créditos con evaluación
- KYC con carga de documentos
- Gestión de beneficiarios
- Generación de documentos PDF
- Panel administrativo con dashboard
- Rate limiting implementado
- Auditoría de eventos

### 16.2 Items en Progreso

- Error 403 en endpoint de documentos para rol SOCIO (DOC_007 bloqueado por RateLimitDocumentosFilter)

### 16.3 Testing

- Tests backend: 347 tests pasando
- Tests frontend: 342 tests pasando

---

*Documento generado para permitir que otra IA pueda comprender y continuar el desarrollo del proyecto.*
