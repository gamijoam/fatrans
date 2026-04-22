# Módulos del Frontend

## Mapa de Cobertura

Cada módulo del backend tiene su correspondiente feature module:

| Módulo Backend | Endpoints | Feature Frontend | Prioridad |
|----------------|-----------|-----------------|----------|
| **Auth** | 9 | `features/auth` | 🔴 Crítica |
| **Socios** | 13 | `features/socios` | 🔴 Crítica |
| **Ahorros** | 12 | `features/cuentas` | 🔴 Crítica |
| **Créditos** | 14 | `features/creditos` | 🔴 Alta |
| **KYC** | 12 | `features/kyc` | 🟡 Media |
| **Beneficiarios** | 4 | `features/beneficiarios` | 🟡 Media |
| **Documentos PDF** | 9 | `features/documentos` | 🟡 Media |

---

## Auth (`features/auth`)

### Endpoints

- `POST /v1/auth/login` - Login
- `POST /v1/auth/logout` - Logout
- `POST /v1/auth/refresh` - Refresh token
- `GET /v1/auth/me` - Usuario actual
- `POST /v1/auth/recuperar-password` - Solicitar recuperación
- `POST /v1/auth/reset-password` - Reset password

### Componentes

- `LoginForm` - Formulario de login
- `LogoutButton` - Botón de logout
- `ResetPasswordForm` - Formulario de recuperación

---

## Cuentas (`features/cuentas`)

### Endpoints

- `GET /v1/cuentas/socio/{socioId}` - Listar cuentas
- `GET /v1/cuentas/{numeroCuenta}` - Detalle cuenta
- `GET /v1/cuentas/{numeroCuenta}/saldo` - Consultar saldo
- `POST /v1/cuentas/{numeroCuenta}/depositos` - Realizar depósito
- `POST /v1/cuentas/{numeroCuenta}/retiros` - Realizar retiro
- `GET /v1/cuentas/{numeroCuenta}/movimientos` - Listar movimientos

### Componentes

- `CuentaList` - Lista de cuentas
- `CuentaDetail` - Detalle de cuenta
- `SaldoCard` - Tarjeta de saldo
- `DepositoForm` - Formulario de depósito
- `RetiroForm` - Formulario de retiro
- `MovimientoList` - Lista de movimientos
- `MovimientoDetail` - Detalle de movimiento

### Hooks

- `useCuentasSocio(socioId)` - Lista de cuentas
- `useSaldo(numeroCuenta)` - Saldo actual
- `useMovimientos(numeroCuenta, page)` - Movimientos paginados
- `useDeposito()` - Mutation para depósito
- `useRetiro()` - Mutation para retiro

---

## Créditos (`features/creditos`)

### Endpoints

- `GET /v1/creditos/tipos` - Listar tipos de crédito
- `GET /v1/creditos/solicitudes` - Listar solicitudes
- `POST /v1/creditos/solicitudes` - Crear solicitud
- `GET /v1/creditos/solicitudes/{id}` - Detalle solicitud
- `PUT /v1/creditos/solicitudes/{id}/evaluar` - Evaluar solicitud
- `GET /v1/creditos/cuotas/{solicitudId}` - Listar cuotas
- `POST /v1/creditos/pagos` - Registrar pago

### Componentes

- `TipoCreditoList` - Tipos disponibles
- `SolicitudForm` - Formulario de solicitud
- `SolicitudList` - Lista de solicitudes
- `CuotaList` - Lista de cuotas
- `PagoForm` - Formulario de pago

### Hooks

- `useTiposCredito()` - Tipos de crédito
- `useSolicitudes()` - Solicitudes del socio
- `useCreateSolicitud()` - Crear solicitud
- `useCuotas(solicitudId)` - Cuotas de una solicitud
- `usePago()` - Registrar pago

---

## KYC (`features/kyc`)

### Endpoints

- `GET /v1/kyc/verificacion` - Estado de verificación
- `POST /v1/kyc/documentos` - Subir documento
- `GET /v1/kyc/documentos` - Listar documentos
- `DELETE /v1/kyc/documentos/{id}` - Eliminar documento

### Componentes

- `VerificacionStatus` - Estado actual
- `DocumentoUpload` - Selector de archivos
- `DocumentoList` - Documentos subidos
- `VerificacionProgress` - Progreso de verificación

### Tipos de Documentos

- `CEDULA_ANVERSO` - Cédula de identidad (anverso)
- `CEDULA_REVERSO` - Cédula de identidad (reverso)
- `SELFIE_CEDULA` - Selfie con cédula
- `COMPROBANTE_DOMICILIO` - Comprobante de domicilio

### Hooks

- `useVerificacion()` - Estado KYC
- `useDocumentos()` - Documentos del socio
- `useUploadDocumento()` - Subir documento
- `useDeleteDocumento()` - Eliminar documento

---

## Beneficiarios (`features/beneficiarios`)

### Endpoints

- `GET /v1/beneficiarios` - Listar beneficiarios
- `POST /v1/beneficiarios` - Crear beneficiario
- `GET /v1/beneficiarios/{id}` - Detalle beneficiario
- `PUT /v1/beneficiarios/{id}` - Actualizar beneficiario
- `DELETE /v1/beneficiarios/{id}` - Eliminar beneficiario

### Componentes

- `BeneficiarioList` - Lista de beneficiarios
- `BeneficiarioForm` - Formulario (crear/editar)
- `BeneficiarioCard` - Tarjeta de beneficiario

### Hooks

- `useBeneficiarios()` - Lista de beneficiarios
- `useBeneficiario(id)` - Detalle
- `useCreateBeneficiario()` - Crear
- `useUpdateBeneficiario()` - Actualizar
- `useDeleteBeneficiario()` - Eliminar

---

## Documentos (`features/documentos`)

### Endpoints

- `GET /v1/documentos` - Listar documentos
- `POST /v1/documentos/generar` - Generar documento
- `GET /v1/documentos/{id}/descargar` - Descargar PDF
- `GET /v1/documentos/{id}/presigned-url` - URL temporal

### Tipos de Documentos

- `ESTADO_CUENTA` - Estado de cuenta mensual
- `CONSTANCIA_AFILIACION` - Constancia de afiliación
- `CARTA_BENEFICIARIOS` - Carta de designación de beneficiarios
- `CONTRATO_ADHESION` - Contrato de adhesión
- `PAGARE` - Pagaré

### Componentes

- `DocumentoList` - Lista de documentos
- `DocumentoPreview` - Preview del PDF
- `DocumentoDownload` - Botón de descarga
- `GenerarDocumentoForm` - Formulario de generación

### Hooks

- `useDocumentos()` - Lista de documentos
- `useGenerarDocumento()` - Generar nuevo
- `usePresignedUrl(id)` - Obtener URL temporal

---

## Agregar Nuevo Módulo

Ver [Guía de Desarrollo](./DEVELOPMENT.md#agregar-un-nuevo-módulo)
