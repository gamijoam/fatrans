# Estructura de Páginas - Fondo de Ahorro

**Proyecto:** FATRANS
**Versión:** 1.0
**Fecha:** 2026-04-21

---

## 1. Jerarquía de Rutas

```
/                          → (public) Landing page
├── /productos             → (public) Productos
├── /quienes-somos         → (public) Quiénes somos
├── /terminos              → (public) Términos y condiciones
├── /privacidad            → (public) Política de privacidad
│
/auth                      → (auth) Layout de autenticación
├── /auth/login            → Login
├── /auth/registro         → Solicitud de registro
├── /auth/recuperar-password → Recuperar contraseña
└── /auth/reset-password    → Reset password (con token)
│
/dashboard                  → (app) Layout del socio
├── /dashboard              → Dashboard principal
├── /dashboard/cuentas      → Lista de cuentas
├── /dashboard/cuentas/[numero] → Detalle cuenta
├── /dashboard/cuentas/[numero]/depositar → Hacer depósito
├── /dashboard/cuentas/[numero]/retirar → Hacer retiro
├── /dashboard/creditos      → Lista de créditos
├── /dashboard/creditos/solicitar → Nueva solicitud crédito
├── /dashboard/creditos/simulador → Simulador de créditos
├── /dashboard/creditos/[numero] → Detalle crédito
├── /dashboard/creditos/[numero]/pagar → Pagar cuota
├── /dashboard/kyc          → Verificación KYC
├── /dashboard/beneficiarios → Beneficiarios
├── /dashboard/documentos    → Documentos PDF
├── /dashboard/perfil        → Mi perfil
└── /dashboard/configuracion → Configuración
│
/admin                      → (admin) Layout del administrador
├── /admin                   → Dashboard admin
├── /admin/socios            → Gestión de socios
├── /admin/socios/solicitudes → Solicitudes pendientes
├── /admin/socios/[id]       → Detalle socio
├── /admin/creditos          → Gestión de créditos
├── /admin/creditos/solicitudes → Cola de solicitudes
├── /admin/creditos/[numero] → Evaluar crédito
├── /admin/kyc               → Revisión KYC
├── /admin/documentos        → Reportes
└── /admin/estadisticas      → Estadísticas
```

---

## 2. Detalle de Páginas

### 2.1 Área Pública

#### `/` - Landing Page
**Tipo:** SSR/SSG (para SEO)
**Autenticación:** No requerida
**Layout:** PublicLayout

| Sección | Componentes | Estados |
|---------|-------------|---------|
| Hero | Headline, subtítulo, CTA buttons | loading, default |
| Features | Cards con iconos | loading, default |
| Beneficios | Grid de beneficios | loading, default |
| Cómo funciona | Steps/timeline | loading, default |
| Testimonios | Carousel quotes | loading, default |
| FAQ | Accordion | loading, default |
| CTA Final | Email capture | loading, success, error |
| Footer | Links, social, copyright | - |

**SEO:**
- Meta tags: title, description, og:title, og:description
- JSON-LD: Organization, FAQPage
- Sitemap.xml
- robots.txt

---

#### `/productos` - Productos
**Tipo:** SSG
**Autenticación:** No requerida

| Producto | Descripción |
|----------|-------------|
| Cuenta de Ahorro | Depósitos, retiros, rendimientos |
| Crédito Vehículo | Hasta 60 meses, tasa preferencial |
| Micro Crédito | Hasta 12 meses, aprobación rápida |
| Crédito Hipotecario | Largo plazo, bajo interés |

---

#### `/quienes-somos` - Quiénes Somos
**Tipo:** SSG
**Autenticación:** No requerida

| Sección | Contenido |
|---------|-----------|
| Historia | Origen del fondo |
| Misión | Propósito institucional |
| Visión | Objetivos a futuro |
| Valores | Principios del fondo |
| Equipo | Diretivo y personal |

---

#### `/terminos` - Términos y Condiciones
**Tipo:** SSG
**Autenticación:** No requerida
**Contenido:** Legal - términos de uso

---

#### `/privacidad` - Política de Privacidad
**Tipo:** SSG
**Autenticación:** No requerida
**Contenido:** Legal - LOPDP, protección de datos

---

### 2.2 Área de Autenticación

#### `/auth/login` - Login
**Tipo:** CSR
**Autenticación:** No requerida
**Layout:** AuthLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Identificador | text | Required, min 3 chars |
| Password | password | Required, min 8 chars |
| Recordarme | checkbox | Opcional |

**Estados:**
- idle: Formulario default
- loading: Spinner en botón, campos deshabilitados
- error: Mensaje de error bajo el formulario
- success: Redirect a dashboard

**Flujo:**
```
Login → POST /api/v1/auth/login
       ↓
   httpOnly Cookies: access_token, refresh_token
       ↓
   Store: setUser(user), setAuthenticated(true)
       ↓
   Redirect: /dashboard (SOCIO) o /admin (ADMIN)
```

---

#### `/auth/registro` - Solicitud de Registro
**Tipo:** CSR
**Autenticación:** No requerida
**Layout:** AuthLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Primer nombre | text | Required, max 50 |
| Segundo nombre | text | Optional, max 50 |
| Primer apellido | text | Required, max 50 |
| Segundo apellido | text | Optional, max 50 |
| Tipo documento | select | CEDULA_IDENTIDAD, PASAPORTE, CEDULA_EXTRANJERA |
| Número documento | text | Required, formato V-XXXXXXXX |
| Fecha nacimiento | date | Required, @Past |
| Género | select | MASCULINO, FEMENINO, OTRO |
| Estado civil | select | SOLTERO, CASADO, etc. |
| Nacionalidad | text | Required |
| Correo electrónico | email | Required, formato email |
| Teléfono principal | tel | Required, formato +XX-XXX-XXXXXXX |
| Teléfono secundario | tel | Optional |
| Empresa | text | Required |
| Departamento | text | Optional |
| Cargo | text | Optional |
| Tipo contrato | select | PERMANENTE, TEMPORAL, etc. |
| Salario | number | Required, min 0.01 |
| Banco | text | Required |
| Número cuenta | text | Required, 10-20 dígitos |
| Contacto emergencia | group | Nombre, teléfono, parentesco |

**Estados:**
- idle: Formulario vacío
- submitting: Validando, campos deshabilitados
- success: "Solicitud enviada. Recibirá notificación cuando sea procesada."
- error: Mensaje de error específico
- duplicate: "Ya existe una solicitud con estos datos"

**Flujo:**
```
POST /api/v1/socios/solicitud
       ↓
   Admin recibe notificación
       ↓
   Admin revisa → Aprueba o Rechaza
       ↓
   Si Aprueba → Socio recibe email con credenciales
```

---

#### `/auth/recuperar-password` - Recuperar Contraseña
**Tipo:** CSR
**Autenticación:** No requerida
**Layout:** AuthLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Correo electrónico | email | Required, formato email |

**Estados:**
- idle: Formulario
- loading: Spinner
- success: "Si el correo existe, recibirá un enlace de recuperación"
- error: Error de servidor

**Nota de seguridad:** Siempre retorna el mismo mensaje (no revela si el email existe).

---

#### `/auth/reset-password` - Reset Password
**Tipo:** CSR
**Autenticación:** No requerida (usa token de URL)
**Layout:** AuthLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Nueva contraseña | password | Required, min 8, 1 mayúscula, 1 minúscula, 1 número, 1 especial |
| Confirmar contraseña | password | Required, debe coincidir |

**Validación token:**
- Token inválido/expirado → "El enlace de recuperación no es válido o expiró"
- Token válido → Mostrar formulario

---

### 2.3 Portal del Socio

#### `/dashboard` - Dashboard Socio
**Tipo:** CSR (datos privados)
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Sección | Componentes | Datos |
|---------|-------------|-------|
| Bienvenida | "Hola, {nombre}" | GET /api/v1/auth/me |
| Stats cards | Saldo total, cuentas, créditos activos | GET /api/v1/cuentas/socio/{id}, GET /api/v1/creditos/solicitudes/socio/{id} |
| Últimos movimientos | Lista 5 movimientos | GET /api/v1/cuentas/{num}/movimientos?size=5 |
| Quick actions | Depósito, Solicitar crédito | Buttons |
| KYC status | Badge estado | GET /api/v1/kyc/estado |
| Notificaciones | Dropdown | GET /api/v1/notificaciones |

**Estados:**
- loading: Skeleton cards
- error: ErrorBoundary + retry
- empty: EmptyState + CTA crear cuenta

---

#### `/dashboard/cuentas` - Mis Cuentas
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Elemento | Componente | Datos |
|----------|------------|-------|
| Lista cuentas | CuentaCard[] | GET /api/v1/cuentas/socio/{socioId} |
| CuentaCard | saldo, estado, tipo | - |

**CuentaCard estados:**
- ACTIVA: Badge verde, acciones habilitadas
- SUSPENDIDA: Badge amarillo, solo lectura
- CERRADA: Badge rojo, sin acciones

---

#### `/dashboard/cuentas/[numero]` - Detalle Cuenta
**Tipo:** CSR
**Autenticación:** Requiere SOCIO (IDOR)
**Layout:** DashboardLayout

| Sección | Componentes | Datos |
|---------|-------------|-------|
| Header | Número cuenta, estado, tipo | GET /api/v1/cuentas/{numero} |
| Saldo | Saldo actual, disponible, retenido | GET /api/v1/cuentas/{numero}/saldo |
| Acciones | Depositar, Retirar, Ver movimientos | Buttons |
| Movimientos recientes | MovimientoList (últimos 10) | GET /api/v1/cuentas/{numero}/movimientos?size=10 |
| Rendimientos | RendimientoCard | GET /api/v1/cuentas/{numero}/rendimientos |

---

#### `/dashboard/cuentas/[numero]/depositar` - Hacer Depósito
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Monto | number | Required, min 0.01, max 500,000 |
| Descripción | textarea | Optional, max 500 |
| Referencia | text | Optional, max 100 |
| Canal origen | select (readonly) | WEB |

**Estados:**
- idle: Formulario
- validating: Verificando saldo mínimo
- submitting: Procesando depósito
- success: "Depósito procesado. Monto: XXXXX"
- error: Mensaje específico

---

#### `/dashboard/cuentas/[numero]/retirar` - Hacer Retiro
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Monto | number | Required, min 0.01, no puede exceder saldo disponible |
| Canal origen | select (readonly) | WEB |

**Validaciones frontend:**
- Saldo disponible >= monto
- Límite diario no excedido
- Cuenta en estado ACTIVA

**Estados:**
- idle: Formulario
- validating: Verificando saldo
- submitting: Procesando
- success: "Retiro procesado. Nuevo saldo: XXXXX"
- error: Insufficient funds / Límite excedido

---

#### `/dashboard/creditos` - Mis Créditos
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Elemento | Componente | Datos |
|----------|------------|-------|
| Catálogo tipos | TipoCreditoCard[] | GET /api/v1/creditos/tipos-credito |
| Mis solicitudes | SolicitudCard[] | GET /api/v1/creditos/solicitudes/socio/{socioId} |

**SolicitudCard estados:**
- PENDIENTE: Badge amarillo
- EN_EVALUACION: Badge azul
- APROBADA: Badge verde
- RECHAZADA: Badge rojo
- DESEMBOLSADO: Badge verde oscuro

---

#### `/dashboard/creditos/solicitar` - Solicitar Crédito
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

**Prerrequisito:** KYC debe estar APROBADO

| Campo | Tipo | Validación |
|-------|------|------------|
| Tipo crédito | select | Required, de tipos disponibles |
| Monto solicitado | number | min/max según tipo |
| Plazo meses | number | min/max según tipo |
| Destino crédito | textarea | Optional |
| Cuenta destino | select | Cuentas del socio |

**Validaciones:**
- KYC aprobado
- No tener crédito activo en estado DESEMBOLSADO
- Monto dentro del rango del tipo

---

#### `/dashboard/creditos/simulador` - Simulador
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Monto | number | Required, min 1000, max 5000000 |
| Plazo meses | number | Required, 1-360 |
| Tipo crédito | select | Required |

**Resultado:**
- Cuota mensual estimada
- Total intereses
- Total a pagar
- Tabla de amortización preview (primeras 6 cuotas)

**Endpoint:** `POST /api/v1/simulador`

---

#### `/dashboard/creditos/[numero]` - Detalle Crédito
**Tipo:** CSR
**Autenticación:** Requiere SOCIO (IDOR)
**Layout:** DashboardLayout

| Sección | Componentes | Datos |
|---------|-------------|-------|
| Header | Número, estado, tipo, monto | GET /api/v1/creditos/{numero} |
| Progreso | Barra de progreso | Cuotas pagadas / total |
| Plan amortización | Table | GET /api/v1/creditos/solicitudes/{num}/plan |
| Cuotas pendientes | CuotaList | GET /api/v1/creditos/{numero}/cuotas?estado=PENDIENTE |

**CuotaRow estados:**
- PENDIENTE: Gris
- VENCIDA: Rojo (días mora > 0)
- PAGADA: Verde con check

---

#### `/dashboard/creditos/[numero]/pagar` - Pagar Cuota
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Campo | Tipo | Validación |
|-------|------|------------|
| Cuota | readonly | Detalle de la cuota seleccionada |
| Monto | number | Pre-filled con monto de cuota |
| Referencia pago | text | Optional, max 100 |
| Canal origen | select | SUCURSAL, WEB, MOBILE, ATM |

**Validación:** Monto >= monto cuota (incluye intereses mora si aplica)

---

#### `/dashboard/kyc` - Mi Verificación KYC
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Sección | Componentes | Estados |
|---------|-------------|---------|
| Progress | KycProgress | pasos: iniciado, documentos, verificando, aprobado |
| Documentos requeridos | DocumentUpload[] | pending, uploading, uploaded, error |
| Acciones | Enviar a revisión | disabled hasta completar |

**Documentos requeridos (BASICO):**
- Cédula anverso
- Cédula reverso
- Selfie con cédula
- Comprobante domicilio

---

#### `/dashboard/beneficiarios` - Beneficiarios
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Elemento | Componentes | Validación |
|----------|-------------|------------|
| Lista | BeneficiarioCard[] | GET /api/v1/socios/{id}/beneficiarios |
| Suma porcentajes | ProgressBar | Debe ser 100% |
| Agregar | Button → Form | max 5 beneficiarios |

**Validación estricta:** Suma = 100% desde el inicio

**BeneficiarioCard:**
| Campo | Tipo |
|-------|------|
| Nombre completo | text |
| Documento | text |
| Parentesco | badge |
| Porcentaje | number |
| Acciones | Editar, Eliminar |

---

#### `/dashboard/documentos` - Documentos
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Elemento | Componentes | Datos |
|----------|-------------|-------|
| Lista | DocumentoCard[] | GET /api/v1/documentos/socio/{socioId} |
| Tipos | Filter | ESTADO_CUENTA, CONSTANCIA, etc. |

**DocumentoCard estados:**
- GENERADO: Spinner
- ALMACENADO: Listo para descargar
- EXPIRADO: Badge expirado

**Acciones:**
- Generar: GET /documentos/estado-cuenta/{cuentaId}
- Descargar: GET /documentos/{docId}/descargar (pre-signed URL)

---

#### `/dashboard/perfil` - Mi Perfil
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Campo | Tipo | Editable |
|-------|------|----------|
| Nombre completo | text | No |
| Correo | email | No |
| Teléfono | tel | Sí |
| Dirección | group | Sí |
| Empresa | text | No |
| Cargo | text | Sí |

**Acción:** PATCH /api/v1/socios/{id}/perfil

---

#### `/dashboard/configuracion` - Configuración
**Tipo:** CSR
**Autenticación:** Requiere SOCIO
**Layout:** DashboardLayout

| Opción | Descripción |
|--------|-------------|
| Notificaciones email | Toggle |
| Notificaciones SMS | Toggle |
| Tema | Light/Dark (futuro) |

---

### 2.4 Portal Admin

#### `/admin` - Dashboard Admin
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Métrica | Componentes | Datos |
|---------|-------------|-------|
| Total socios | StatCard | GET /api/v1/admin/dashboard/estadisticas |
| Socios activos | StatCard | - |
| Cuentas | StatCard | - |
| Créditos activos | StatCard | - |
| Solicitudes pendientes | StatCard | - |
| Mora | StatCard | - |

**Gráficos:**
- Evolución mensual de socios
- Distribución de créditos por estado

---

#### `/admin/socios` - Gestión Socios
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Elemento | Componentes | Datos |
|----------|-------------|-------|
| Búsqueda | SearchInput | GET /api/v1/socios/buscar |
| Filtros | Select estado | - |
| Tabla | SocioTable | GET /api/v1/socios?page=0&size=20 |

**SocioTable columnas:**
- Número socio
- Nombre
- Cédula
- Email
- Empresa
- Estado (badge)
- Acciones (ver, editar, activar/desactivar)

---

#### `/admin/socios/solicitudes` - Solicitudes Pendientes
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Elemento | Componentes | Datos |
|----------|-------------|-------|
| Tabla | SolicitudTable | GET /api/v1/socios/solicitudes?estado=PENDIENTE |
| Detalle | Modal | GET /api/v1/socios/solicitudes/{id} |

**Acciones por fila:**
- Aprobar → POST /api/v1/socios/solicitudes/{id}/aprobar
- Rechazar → POST /api/v1/socios/solicitudes/{id}/rechazar (con motivo)

---

#### `/admin/socios/[id]` - Detalle Socio
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Sección | Datos |
|---------|-------|
| Info personal | GET /api/v1/socios/{id} |
| Cuentas | GET /api/v1/cuentas/socio/{socioId} |
| Créditos | GET /api/v1/creditos/solicitudes/socio/{socioId} |
| KYC | GET /api/v1/kyc/estado |
| Beneficiarios | GET /api/v1/socios/{id}/beneficiarios |

---

#### `/admin/creditos` - Gestión Créditos
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Elemento | Componentes | Datos |
|----------|-------------|-------|
| Tabla | CreditoTable | GET /api/v1/creditos/solicitudes?page=0&size=20 |
| Filtros | Select estado | PENDIENTE, EN_EVALUACION, APROBADA, etc. |

---

#### `/admin/creditos/solicitudes` - Cola Créditos
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Elemento | Componentes | Datos |
|----------|-------------|-------|
| Tabla | SolicitudCreditoTable | GET /api/v1/creditos/solicitudes?estado=PENDIENTE |

---

#### `/admin/creditos/[numero]` - Evaluar Crédito
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Sección | Componentes | Endpoint |
|---------|-------------|----------|
| Solicitud | DetailCard | GET /api/v1/creditos/solicitudes/{num} |
| Evaluación | EvaluationForm | POST /api/v1/creditos/solicitudes/{num}/evaluar |
| Resultado | ScoreDisplay | score, nivel riesgo, elegibilidad |
| Acciones | Approve/Reject buttons | POST /aprobar, POST /rechazar |

**EvaluationForm campos:**
- Puntaje antigüedad (0-30)
- Puntaje historial ahorro (0-30)
- Puntaje capacidad pago (0-40)
- Salario estimado

---

#### `/admin/kyc` - Revisión KYC
**Tipo:** CSR
**Autenticación:** Requiere ANALISTA_KYC o ADMIN
**Layout:** AdminLayout

| Elemento | Componentes | Datos |
|----------|-------------|-------|
| Cola | KycQueueTable | GET /api/v1/kyc/cola-revision |
| Detalle | DocumentPreviewModal | GET /api/v1/kyc/revision/{id} |

**Acciones:**
- Aprobar → POST /api/v1/kyc/revision/{id}/aprobar
- Rechazar → POST /api/v1/kyc/revision/{id}/rechazar (con motivo)
- Solicitar info → POST /api/v1/kyc/revision/{id}/solicitar-info

---

#### `/admin/estadisticas` - Estadísticas
**Tipo:** CSR
**Autenticación:** Requiere ADMIN
**Layout:** AdminLayout

| Gráfico | Tipo | Datos |
|---------|------|-------|
| Socios por mes | Line | Evolución mensual |
| Créditos por estado | Pie | Distribución |
| KYC stats | Bar | Aprobados, rechazados, pendientes |
| Mora | Table | Créditos en mora |

---

## 3. Componentes Compartidos

### Navigation
| Componente | Descripción |
|------------|-------------|
| PublicNavbar | Logo, links públicos, botón login |
| AuthNavbar | Logo, help, user menu |
| DashboardSidebar | Navegación con iconos, collapsible |
| AdminSidebar | Navegación extendida admin |
| Breadcrumb | Navegación jerárquica |
| UserMenu | Avatar, dropdown, logout |

### Data Display
| Componente | Estados |
|------------|---------|
| StatCard | loading, value, trend |
| DataTable | loading, empty, error, data, pagination |
| TableSkeleton | Skeleton rows |
| Badge | success, warning, error, info |
| ProgressBar | default, animated |
| EmptyState | icon, title, description, action |

### Forms
| Componente | Estados |
|------------|---------|
| Input | default, focus, error, disabled |
| Select | default, open, disabled |
| Checkbox | unchecked, checked, disabled |
| RadioGroup | options con selection |
| Textarea | default, focus, error |
| DatePicker | calendar popup |
| FileUpload | idle, dragover, uploading, complete, error |
| PasswordInput | con toggle visibility |

### Feedback
| Componente | Variantes |
|------------|-----------|
| Button | primary, secondary, destructive, ghost, loading |
| Dialog | open, closed |
| Alert | info, success, warning, error |
| Toast | (via sonner) |
| Spinner | sm, md, lg |
| Skeleton | text, avatar, card |

### Layout
| Componente | Descripción |
|------------|-------------|
| Container | max-width wrapper |
| Card | header, content, footer |
| Tabs | navegación por tabs |
| Accordion | collapsible sections |
| Modal | overlay + content |

---

## 4. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-21 | @product-manager | Creación inicial |

---

## 5. Referencias

- Roadmap: [FRONTEND_ROADMAP.md](./FRONTEND_ROADMAP.md)
- Roles: [ROLES_PERMISOS.md](./ROLES_PERMISOS.md)
- Diseño: [DISENO_SISTEMA.md](./DISENO_SISTEMA.md)
