# 📦 Catálogo de Módulos Propuestos — FondoAhorro
**Versión:** 1.0 | **Fecha:** Abril 2026 | **Perspectiva:** Product Manager

---

## Contexto

Este documento lista **todos los módulos que se pueden desarrollar** para la plataforma,
tomando como base:

- Los objetivos del proyecto (docs/README.md)
- Las fases definidas (MVP → Optimización → Expansión)
- Los 5 módulos backend ya existentes (Auth, Socios, Ahorros, Créditos, KYC)
- El estándar de seguridad del proyecto (21 auditorías ejecutadas, LOPDP, SUDEBAN)

Cada módulo incluye: descripción, justificación de negocio, dependencias, nivel de
seguridad requerido y puntos de auditoría.

---

## 📊 Resumen Rápido

| # | Módulo | Fase | Prioridad | Esfuerzo | ¿Existe código? |
|---|--------|------|-----------|----------|------------------|
| 1 | Beneficiarios | MVP | 🔴 Crítica | 2-3 días | ❌ No |
| 2 | Documentación PDF | MVP | 🔴 Crítica | 3-4 días | ❌ No |
| 3 | Panel Admin (API) | MVP | 🔴 Crítica | 2-3 días | ❌ No |
| 4 | Parámetros del Sistema | MVP | 🟠 Alta | 1-2 días | ❌ No |
| 5 | Notificaciones (Email + Push) | Fase 2 | 🟠 Alta | 3-4 días | ⚠️ Solo mocks |
| 6 | Reportes y Exportación | Fase 2 | 🟠 Alta | 3-4 días | ❌ No |
| 7 | Auditoría Centralizada | Fase 2 | 🟠 Alta | 2-3 días | ⚠️ Solo KYC tiene |
| 8 | Nómina / Aportes Automáticos | Fase 2 | 🟡 Media | 3-4 días | ❌ No |
| 9 | Aprobaciones Multi-Nivel | Fase 2 | 🟡 Media | 2-3 días | ❌ No |
| 10 | Biometría | Fase 3 | 🟡 Media | 2-3 días | ⚠️ Solo Flutter stub |
| 11 | Marketplace | Fase 3 | 🔵 Baja | 5+ días | ❌ No |
| 12 | Comunicaciones / Tablón | Fase 3 | 🔵 Baja | 2-3 días | ❌ No |

---

## Módulos Detallados

---

### 1. 📋 Módulo de Beneficiarios
**Fase:** MVP | **Prioridad:** 🔴 Crítica

**¿Qué es?**
Cada socio debe poder registrar beneficiarios (familiares o terceros) que recibirían
sus ahorros en caso de fallecimiento o retiro definitivo. Esto es un requisito legal
en fondos de ahorro formales.

**Funcionalidades:**
- CRUD de beneficiarios por socio (nombre, cédula, parentesco, porcentaje)
- Validación: los porcentajes deben sumar exactamente 100%
- Máximo 5 beneficiarios por socio
- Historial de cambios de beneficiarios (auditoría)
- Generación de carta de designación de beneficiarios (PDF)

**Entidades:**
```
Beneficiario {
  id: UUID
  socioId: UUID (FK → Socio)
  nombreCompleto: String
  numeroDocumento: String
  tipoDocumento: TipoDocumento
  parentesco: Parentesco (enum: CONYUGE, HIJO, PADRE, MADRE, HERMANO, OTRO)
  porcentaje: BigDecimal (precision 5, scale 2)
  telefono: String
  fechaRegistro: LocalDateTime
  activo: boolean
}
```

**Endpoints:**
| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/api/v1/socios/{socioId}/beneficiarios` | Registrar beneficiario | SOCIO |
| GET | `/api/v1/socios/{socioId}/beneficiarios` | Listar beneficiarios | SOCIO, ADMIN |
| PUT | `/api/v1/socios/{socioId}/beneficiarios/{id}` | Actualizar beneficiario | SOCIO |
| DELETE | `/api/v1/socios/{socioId}/beneficiarios/{id}` | Eliminar beneficiario | SOCIO |

**Seguridad y Auditoría:**
- Validación IDOR: un socio solo puede ver/editar sus propios beneficiarios
- Log de auditoría para cada cambio (quién, cuándo, qué cambió)
- Validar que `numeroDocumento` no sea el mismo del socio
- Rate limiting: 10 requests/min por socio

**Dependencias:** Módulo Socios

---

### 2. 📄 Módulo de Documentación PDF
**Fase:** MVP | **Prioridad:** 🔴 Crítica

**¿Qué es?**
Generación automática de documentos legales y financieros en PDF.
Definido explícitamente como parte del MVP en el README del proyecto.

**Documentos a generar:**
| Documento | Disparador | Datos de entrada |
|-----------|------------|------------------|
| Estado de Cuenta Mensual | Cron mensual o manual | Cuenta Ahorro + Movimientos del periodo |
| Constancia de Afiliación | Solicitud del socio | Datos del Socio |
| Contrato de Adhesión | Al aprobar solicitud de registro | Datos del Socio + Parámetros |
| Pagaré de Crédito | Al aprobar crédito | Solicitud Crédito + Plan Amortización |
| Tabla de Amortización | Al aprobar crédito | Plan de Amortización |
| Carta de Beneficiarios | Al registrar beneficiarios | Beneficiarios del Socio |

**Endpoints:**
| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| GET | `/api/v1/documentos/estado-cuenta/{cuentaId}` | Generar estado de cuenta | SOCIO, ADMIN |
| GET | `/api/v1/documentos/constancia-afiliacion/{socioId}` | Generar constancia | SOCIO, ADMIN |
| GET | `/api/v1/documentos/contrato/{solicitudId}` | Generar contrato | ADMIN |
| GET | `/api/v1/documentos/pagare/{creditoId}` | Generar pagaré | ADMIN |

**Librería recomendada:** OpenPDF (fork de iText, licencia LGPL — sin costo)

**Seguridad y Auditoría:**
- Validación IDOR: socio solo puede generar sus propios documentos
- Marca de agua con fecha/hora de generación y ID del socio
- Los PDFs se almacenan temporalmente en MinIO (ya integrado)
- Log: registrar cada generación (quién, qué documento, cuándo)
- Los PDFs deben ser firmados digitalmente en Fase 3

**Dependencias:** Módulo Socios, Ahorros, Créditos, Beneficiarios

---

### 3. 📊 Panel Administrativo (API)
**Fase:** MVP | **Prioridad:** 🔴 Crítica

**¿Qué es?**
API que consolida las métricas clave del fondo para que el administrador
tenga visibilidad total del estado operativo.

**Endpoints:**
| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| GET | `/api/v1/admin/dashboard` | Métricas generales del fondo | ADMIN |
| GET | `/api/v1/admin/dashboard/ahorros` | Métricas detalladas de ahorros | ADMIN |
| GET | `/api/v1/admin/dashboard/creditos` | Métricas detalladas de créditos | ADMIN |
| GET | `/api/v1/admin/dashboard/socios` | Métricas detalladas de socios | ADMIN |
| GET | `/api/v1/admin/actividad-reciente` | Últimas 50 operaciones del fondo | ADMIN |

**Datos del dashboard principal:**
```json
{
  "totalSocios": 1250,
  "sociosActivos": 1180,
  "solicitudesPendientes": 15,
  "totalAhorros": 2500000.00,
  "totalCreditosActivos": 800000.00,
  "morosidad": 2.3,
  "rendimientosMesActual": 12500.00,
  "ingresosMes": 450000.00,
  "egresosMes": 180000.00
}
```

**Seguridad y Auditoría:**
- Solo accesible con rol `ADMIN`
- Rate limiting: 30 requests/min
- IP whitelist para ambientes de producción
- Caché Redis (TTL 60 segundos) para evitar queries pesados en cada request
- Log de acceso al dashboard

**Dependencias:** Módulo Socios, Ahorros, Créditos

---

### 4. ⚙️ Módulo de Parámetros del Sistema
**Fase:** MVP | **Prioridad:** 🟠 Alta

**¿Qué es?**
Tabla de configuración para valores que hoy están hardcodeados en el código.
Permite al administrador ajustar parámetros sin tocar el código ni hacer deploy.

**Parámetros:**
| Clave | Tipo | Ejemplo | Usado por |
|-------|------|---------|-----------|
| `tasa_interes_ahorro_default` | BigDecimal | 0.05 | Ahorros |
| `monto_minimo_deposito` | BigDecimal | 0.0001 | Ahorros |
| `monto_maximo_deposito` | BigDecimal | 500000.00 | Ahorros |
| `monto_maximo_retiro_diario` | BigDecimal | 50000.00 | Ahorros |
| `moneda_default` | String | USD | Global |
| `max_beneficiarios_por_socio` | Integer | 5 | Beneficiarios |
| `dias_vigencia_solicitud` | Integer | 30 | Socios |
| `tasa_moratoria_credito` | BigDecimal | 0.03 | Créditos |
| `porcentaje_garantia_minimo` | BigDecimal | 0.50 | Créditos |

**Endpoints:**
| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| GET | `/api/v1/admin/parametros` | Listar todos los parámetros | ADMIN |
| GET | `/api/v1/admin/parametros/{clave}` | Obtener parámetro específico | ADMIN, SISTEMA |
| PUT | `/api/v1/admin/parametros/{clave}` | Actualizar parámetro | ADMIN |

**Seguridad y Auditoría:**
- Solo ADMIN puede modificar
- Auditoría: cada cambio queda registrado (valor anterior, valor nuevo, quién, cuándo)
- Validación de rangos para evitar configuraciones absurdas
- Caché Redis para no consultar la DB en cada operación

**Dependencias:** Ninguna (módulo base)

---

### 5. 📧 Módulo de Notificaciones
**Fase:** Fase 2 | **Prioridad:** 🟠 Alta

**¿Qué es?**
Sistema centralizado de notificaciones que reemplaza los mocks actuales
y agrega canales adicionales (push móvil, SMS futuro).

**Canales:**
| Canal | Tecnología | Fase |
|-------|-----------|------|
| Email | Spring Mail + SMTP (SendGrid/Gmail) | Fase 2 |
| Push Móvil | Firebase Cloud Messaging (FCM) | Fase 2 |
| SMS | Twilio o similar | Fase 3 |

**Eventos que disparan notificaciones:**
| Evento | Email | Push |
|--------|-------|------|
| Solicitud de registro recibida | ✅ | - |
| Solicitud aprobada + credenciales | ✅ | - |
| Solicitud rechazada | ✅ | - |
| Depósito recibido | ✅ | ✅ |
| Retiro realizado | ✅ | ✅ |
| Crédito aprobado | ✅ | ✅ |
| Cuota próxima a vencer (3 días antes) | ✅ | ✅ |
| Cuota vencida (mora) | ✅ | ✅ |
| Recuperación de contraseña | ✅ | - |
| Cambio de beneficiarios | ✅ | - |

**Arquitectura:**
```
NotificacionService (interface)
├── EmailNotificacionServiceImpl (Spring Mail)
├── PushNotificacionServiceImpl (Firebase)
└── SmsNotificacionServiceImpl (Twilio) [futuro]
```

**Seguridad y Auditoría:**
- Nunca incluir datos sensibles en notificaciones push (solo "tiene una nueva notificación")
- Template de emails con token anti-phishing
- Log de cada notificación enviada (canal, destinatario, éxito/fallo)
- Preferencias del socio: permitir desactivar canales específicos

**Dependencias:** Todos los módulos (es transversal)

---

### 6. 📈 Módulo de Reportes y Exportación
**Fase:** Fase 2 | **Prioridad:** 🟠 Alta

**¿Qué es?**
Generación de reportes financieros en múltiples formatos (Excel, CSV, PDF)
para el administrador y para cumplimiento regulatorio ante SUDEBAN/SUDECA.

**Reportes:**
| Reporte | Formato | Periodicidad | Rol |
|---------|---------|-------------|-----|
| Estado financiero del fondo | PDF + Excel | Mensual | ADMIN |
| Listado de socios activos | CSV + Excel | Bajo demanda | ADMIN |
| Movimientos del periodo | CSV + Excel | Mensual | ADMIN, SOCIO |
| Cartera de créditos vigentes | PDF + Excel | Mensual | ADMIN |
| Reporte de morosidad | PDF | Mensual | ADMIN |
| Declaración regulatoria | PDF | Trimestral | ADMIN |

**Endpoints:**
| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/api/v1/reportes/generar` | Solicitar generación de reporte | ADMIN |
| GET | `/api/v1/reportes/{id}/descargar` | Descargar reporte generado | ADMIN |
| GET | `/api/v1/reportes/historial` | Listar reportes generados | ADMIN |

**Librería recomendada:** Apache POI (Excel) + OpenPDF (PDF)

**Seguridad y Auditoría:**
- Solo ADMIN puede generar y descargar
- Los reportes se almacenan en MinIO con TTL de 30 días
- Log de cada generación y descarga
- Los reportes financieros NO deben contener datos personales (LOPDP)

**Dependencias:** Módulo Ahorros, Créditos, Socios

---

### 7. 🔍 Módulo de Auditoría Centralizada
**Fase:** Fase 2 | **Prioridad:** 🟠 Alta

**¿Qué es?**
Sistema de registro inmutable de todas las operaciones del sistema.
Actualmente solo KYC tiene su propio `AuditKYCEntity`. Este módulo lo globaliza.

**Entidad:**
```
AuditLog {
  id: UUID
  modulo: String (AUTH, SOCIOS, AHORROS, CREDITOS, KYC, ADMIN)
  accion: String (LOGIN, DEPOSITO, RETIRO, CAMBIO_PARAMETRO, etc.)
  entidadAfectada: String (nombre de la tabla)
  entidadId: UUID (PK del registro afectado)
  usuarioId: UUID (quién ejecutó)
  ipOrigen: String
  valorAnterior: JSON (snapshot antes del cambio)
  valorNuevo: JSON (snapshot después del cambio)
  fechaHora: LocalDateTime
  resultado: String (EXITOSO, FALLIDO, DENEGADO)
}
```

**Seguridad:**
- Los registros de auditoría son **INMUTABLES** (INSERT only, nunca UPDATE/DELETE)
- Solo ADMIN puede consultar
- Retención mínima: 5 años (SUDEBAN)
- Particionamiento por mes en PostgreSQL para rendimiento
- Exportación para auditoría externa

**Dependencias:** Todos los módulos (interceptor global con AOP)

---

### 8. 💰 Módulo de Nómina / Aportes Automáticos
**Fase:** Fase 2 | **Prioridad:** 🟡 Media

**¿Qué es?**
Sistema que permite registrar aportes recurrentes de los socios (descuento de nómina)
de forma automatizada. Típico de fondos de ahorro empresariales.

**Funcionalidades:**
- Registrar un aporte periódico (monto fijo o porcentaje del salario)
- Procesamiento batch mensual de aportes
- Carga masiva de aportes por CSV (el departamento de RRHH sube el archivo)
- Historial de aportes por socio
- Reconciliación: comparar lo que debía aportarse vs lo que se depositó

**Seguridad y Auditoría:**
- Validar que el CSV venga de una IP autorizada (RRHH)
- Doble verificación antes de ejecutar el batch (preview → confirmación)
- Log detallado de cada batch ejecutado
- Notificación al socio por cada aporte procesado

**Dependencias:** Módulo Socios, Ahorros

---

### 9. ✅ Módulo de Aprobaciones Multi-Nivel
**Fase:** Fase 2 | **Prioridad:** 🟡 Media

**¿Qué es?**
Sistema de flujo de aprobación para operaciones críticas que requieren
más de una persona para ejecutarse (principio de "cuatro ojos").

**Operaciones que requieren doble aprobación:**
| Operación | Primer Aprobador | Segundo Aprobador |
|-----------|------------------|-------------------|
| Crédito > 10,000 USD | Analista | Director |
| Retiro > 25,000 USD | Cajero | Director |
| Cambio de parámetros del sistema | Admin | Director |
| Cierre de cuenta | Admin | Director |
| Eliminación de socio | Admin | Director |

**Seguridad y Auditoría:**
- Cada aprobación debe tener token único con expiración (24 horas)
- No puede aprobar la misma persona que solicitó
- Notificación al aprobador pendiente
- Log de cada paso del flujo

**Dependencias:** Módulo Auth (roles), Notificaciones

---

### 10. 🔐 Módulo de Biometría
**Fase:** Fase 3 | **Prioridad:** 🟡 Media

**¿Qué es?**
Autenticación por huella dactilar o reconocimiento facial para operaciones
financieras críticas desde la app móvil.

> Nota: Ya existe `biometric_auth.dart` en el frontend Flutter como stub.

**Operaciones protegidas por biometría:**
- Confirmación de retiros
- Confirmación de transferencias
- Cambio de beneficiarios
- Cambio de contraseña

**Backend necesario:**
- Endpoint para registrar que el dispositivo tiene biometría habilitada
- Flag en `Usuario` indicando si la biometría está activa
- El token biométrico se valida localmente en el dispositivo (Flutter)
- El backend recibe un claim adicional en el JWT: `biometric_verified: true`

**Seguridad y Auditoría:**
- La biometría es complementaria, NO reemplaza el JWT
- Log de cada operación verificada con biometría
- Si el dispositivo cambia, se debe re-registrar la biometría

**Dependencias:** Módulo Auth, Frontend Flutter

---

### 11. 🛒 Módulo de Marketplace
**Fase:** Fase 3 | **Prioridad:** 🔵 Baja

**¿Qué es?**
Espacio para que aliados comerciales ofrezcan productos y servicios
a los afiliados del fondo, con posibilidad de descuento directo del saldo.

**Funcionalidades:**
- Catálogo de comercios aliados
- Ofertas/descuentos exclusivos para socios
- Sistema de cupones vinculados al saldo de ahorro
- Cobro directo del saldo de ahorro (como medio de pago)

**Seguridad y Auditoría:**
- Doble confirmación para cualquier pago desde el saldo
- Comercios deben pasar proceso de verificación
- Límite diario de compras en marketplace

**Dependencias:** Módulo Ahorros, Socios, Notificaciones

---

### 12. 📢 Módulo de Comunicaciones / Tablón de Anuncios
**Fase:** Fase 3 | **Prioridad:** 🔵 Baja

**¿Qué es?**
Canal de comunicación interna para que la administración del fondo publique
avisos, noticias y convocatorias a los socios.

**Funcionalidades:**
- CRUD de anuncios (título, cuerpo, fecha publicación, fecha vencimiento)
- Categorías: INFORMATIVO, URGENTE, CONVOCATORIA
- Marcar como leído por socio
- Push notification al publicar un anuncio URGENTE
- Anuncios con adjuntos (PDF almacenado en MinIO)

**Seguridad y Auditoría:**
- Solo ADMIN puede crear/editar anuncios
- Los socios solo pueden leer
- Log de publicación

**Dependencias:** Módulo Auth, Notificaciones

---

## 📌 Orden de Ejecución Recomendado (Visual)

```
 MVP (AHORA)                     FASE 2                      FASE 3
 ─────────────                   ──────                      ──────
 ┌─────────────┐                ┌──────────────┐            ┌──────────┐
 │ Beneficiarios│────────┐      │ Notificaciones│            │ Biometría│
 └─────────────┘        │      └──────────────┘            └──────────┘
 ┌─────────────┐        │      ┌──────────────┐            ┌───────────┐
 │ PDF/Docs    │────────┤      │ Reportes     │            │ Marketplace│
 └─────────────┘        │      └──────────────┘            └───────────┘
 ┌─────────────┐        ├──►   ┌──────────────┐            ┌───────────┐
 │ Panel Admin │────────┤      │ Auditoría    │            │ Tablón    │
 └─────────────┘        │      └──────────────┘            └───────────┘
 ┌─────────────┐        │      ┌──────────────┐
 │ Parámetros  │────────┘      │ Nómina/Batch │
 └─────────────┘               └──────────────┘
                               ┌──────────────┐
                               │ Multi-Nivel  │
                               └──────────────┘
```

---

## 🔒 Estándar de Seguridad por Módulo

Cada módulo nuevo **DEBE** cumplir con el siguiente checklist antes de considerarse "listo":

- [ ] **Validación IDOR:** Un socio NUNCA puede acceder a datos de otro socio
- [ ] **Rate Limiting:** Máximo configurable de requests por IP y por usuario
- [ ] **@PreAuthorize:** Roles explícitos en cada endpoint
- [ ] **Input Validation:** DTOs con `@Valid`, `@NotNull`, `@Size`, `@DecimalMin/Max`
- [ ] **Auditoría:** Cada operación de escritura queda registrada
- [ ] **Exception Handler:** Handler dedicado por módulo (no excepciones genéricas)
- [ ] **Tests Unitarios:** Mínimo tests para cada UseCase
- [ ] **Documentación OpenAPI:** Cada endpoint documentado con `@Operation` y `@ApiResponse`
- [ ] **Migración Flyway:** Script SQL para tablas nuevas

---

*Este documento debe actualizarse conforme se completen módulos.*
*Última revisión: Abril 2026*
