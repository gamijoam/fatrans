# Roles y Permisos - Fondo de Ahorro

**Proyecto:** FATRANS
**Versión:** 1.0
**Fecha:** 2026-04-21

---

## 1. Roles del Sistema

### 1.1 Roles Definidos en Backend

```java
public enum Rol {
    SOCIO,       // Usuario miembro del fondo
    ADMIN,       // Administrador
    SUPER_ADMIN  // Admin supreme (futuro)
}
```

**Ubicación:** `backend/src/main/java/com/tufondo/auth/domain/model/enums/Rol.java`

### 1.2 Roles Mencionados en Controllers (pendientes de agregar al enum)

⚠️ **Inconsistencia detectada:** Los siguientes roles se usan en `@PreAuthorize` pero **no existen** en el enum `Rol`:

| Rol | Controller | Uso |
|-----|------------|-----|
| `CAJERO` | `CreditoController.java:305` | Registrar pagos de cuotas |
| `ANALISTA_KYC` | `AnalistaKYCController.java`, `AdminKYCController.java` | Revisar documentos KYC |

**Acción requerida:** Agregar `CAJERO` y `ANALISTA_KYC` al enum `Rol` en backend.

---

## 2. Matriz de Permisos por Módulo

### 2.1 Auth

| Endpoint | Público | SOCIO | ADMIN | SUPER_ADMIN |
|----------|---------|-------|-------|-------------|
| `POST /auth/login` | ✅ | - | - | - |
| `POST /auth/refresh` | ✅ | - | - | - |
| `POST /auth/logout` | - | ✅ | ✅ | ✅ |
| `GET /auth/me` | - | ✅ | ✅ | ✅ |
| `POST /auth/validar` | - | ✅ | ✅ | ✅ |
| `POST /auth/crear-usuario` | - | ❌ | ✅ | ✅ |
| `POST /auth/recuperar-password` | ✅ | - | - | - |
| `POST /auth/reset-password` | ✅ | - | - | - |
| `DELETE /auth/sesiones` | - | ✅ | ✅ | ✅ |

### 2.2 Socios

| Endpoint | Público | SOCIO | ADMIN | SUPER_ADMIN |
|----------|---------|-------|-------|-------------|
| `POST /socios` (crear) | ❌ | ❌ | ✅ | ✅ |
| `GET /socios/{id}` | - | Propio | Todas | Todas |
| `GET /socios` (listar) | - | ❌ | ✅ | ✅ |
| `PUT /socios/{id}` | - | ❌ | ✅ | ✅ |
| `DELETE /socios/{id}` | - | ❌ | ✅ | ✅ |
| `PATCH /socios/{id}/activar` | - | ❌ | ✅ | ✅ |
| `PATCH /socios/{id}/desactivar` | - | ❌ | ✅ | ✅ |
| `GET /socios/buscar` | - | ❌ | ✅ | ✅ |
| `POST /socios/solicitud` | ✅ | - | - | - |
| `GET /socios/solicitudes` | - | ❌ | ✅ | ✅ |
| `POST /socios/solicitudes/{id}/aprobar` | - | ❌ | ✅ | ✅ |
| `POST /socios/solicitudes/{id}/rechazar` | - | ❌ | ✅ | ✅ |
| `PATCH /socios/{id}/perfil` | - | Propio | ❌ | ❌ |

### 2.3 Cuentas/Ahorros

| Endpoint | SOCIO | ADMIN | SUPER_ADMIN |
|----------|-------|-------|-------------|
| `POST /cuentas` | ❌ | ✅ | ✅ |
| `GET /cuentas/{numeroCuenta}` | Propia | Todas | Todas |
| `GET /cuentas/socio/{socioId}` | Propio | Todas | Todas |
| `GET /cuentas/{numeroCuenta}/saldo` | Propia | Todas | Todas |
| `POST /cuentas/{numeroCuenta}/depositos` | Propia | Todas | Todas |
| `POST /cuentas/{numeroCuenta}/retiros` | Propia | Todas | Todas |
| `GET /cuentas/{numeroCuenta}/movimientos` | Propia | Todas | Todas |
| `GET /cuentas/{numeroCuenta}/movimientos/{numero}` | Propia | Todas | Todas |
| `POST /cuentas/{numeroCuenta}/rendimientos/calcular` | ❌ | ✅ | ✅ |
| `GET /cuentas/{numeroCuenta}/rendimientos` | Propia | Todas | Todas |
| `DELETE /cuentas/{numeroCuenta}` | ❌ | ✅ | ✅ |

### 2.4 Créditos

| Endpoint | SOCIO | ADMIN | SUPER_ADMIN |
|----------|-------|-------|-------------|
| `POST /creditos/solicitudes` | ✅ | ✅ | ✅ |
| `GET /creditos/solicitudes/{num}` | Propia | Todas | Todas |
| `GET /creditos/solicitudes/socio/{socioId}` | Propias | Todas | Todas |
| `GET /creditos/tipos-credito` | ✅ | ✅ | ✅ |
| `GET /creditos/tipos-credito/{id}` | ✅ | ✅ | ✅ |
| `POST /creditos/solicitudes/{num}/evaluar` | ❌ | ✅ | ✅ |
| `POST /creditos/solicitudes/{num}/aprobar` | ❌ | ✅ | ✅ |
| `POST /creditos/solicitudes/{num}/rechazar` | ❌ | ✅ | ✅ |
| `POST /creditos/solicitudes/{num}/desembolson` | ❌ | ✅ | ✅ |
| `GET /creditos/solicitudes/{num}/plan` | Propia | Todas | Todas |
| `GET /creditos/{numero}` | Propio | Todas | Todas |
| `GET /creditos/{numero}/cuotas` | Propias | Todas | Todas |
| `POST /creditos/cuotas/{cuotaId}/pago` | Propia | ✅ (CAJERO) | ✅ |
| `POST /simulador` | ✅ | ✅ | ✅ |

### 2.5 KYC

| Endpoint | SOCIO | ADMIN | ANALISTA_KYC | SUPER_ADMIN |
|----------|-------|-------|--------------|-------------|
| `POST /kyc/iniciar` | ✅ | - | - | - |
| `GET /kyc/estado` | ✅ | - | - | - |
| `POST /kyc/documentos` | ✅ | - | - | - |
| `DELETE /kyc/documentos/{id}` | ✅ | - | - | - |
| `POST /kyc/enviar` | ✅ | - | - | - |
| `GET /kyc/cola-revision` | ❌ | ✅ | ✅ | ✅ |
| `GET /kyc/revision/{id}` | ❌ | ✅ | ✅ | ✅ |
| `POST /kyc/revision/{id}/aprobar` | ❌ | ✅ | ✅ | ✅ |
| `POST /kyc/revision/{id}/rechazar` | ❌ | ✅ | ✅ | ✅ |
| `POST /kyc/revision/{id}/solicitar-info` | ❌ | ✅ | ✅ | ✅ |
| `GET /kyc/historial` | ✅ | - | - | - |
| `GET /kyc/admin/estadisticas` | ❌ | ✅ | ❌ | ✅ |
| `POST /kyc/revocar-consentimiento` | ✅ | - | - | - |

### 2.6 Beneficiarios

| Endpoint | SOCIO | ADMIN | SUPER_ADMIN |
|----------|-------|-------|-------------|
| `GET /socios/{socioId}/beneficiarios` | Propio | Todas | Todas |
| `POST /socios/{socioId}/beneficiarios` | Propio | ✅ | ✅ |
| `PUT /socios/{socioId}/beneficiarios/{id}` | Propio | ✅ | ✅ |
| `DELETE /socios/{socioId}/beneficiarios/{id}` | Propio | ✅ | ✅ |

**Regla de negocio:** Suma de porcentajes = 100% desde el inicio

### 2.7 Documentos PDF

| Endpoint | SOCIO | ADMIN | CAJERO | SISTEMA | SUPER_ADMIN |
|----------|-------|-------|--------|---------|-------------|
| `GET /documentos/estado-cuenta/{cuentaId}` | Propia | Todas | ❌ | ❌ | ✅ |
| `GET /documentos/constancia-afiliacion/{socioId}` | Propio | Todas | ❌ | ❌ | ✅ |
| `GET /documentos/contrato/{solicitudId}` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `GET /documentos/pagare/{creditoId}` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `GET /documentos/tabla-amortizacion/{creditoId}` | Propio | Todas | ❌ | ✅ | ✅ |
| `GET /documentos/carta-beneficiarios/{socioId}` | Propio | Todas | ❌ | ❌ | ✅ |
| `GET /documentos/{documentoId}` | Propio | Todas | ❌ | ❌ | ✅ |
| `GET /documentos/{documentoId}/descargar` | Propio | Todas | ❌ | ❌ | ✅ |
| `GET /documentos/socio/{socioId}` | Propio | Todas | ❌ | ❌ | ✅ |

### 2.8 Admin Dashboard

| Endpoint | ADMIN | SUPER_ADMIN |
|----------|-------|-------------|
| `GET /admin/dashboard/estadisticas` | ✅ | ✅ |

---

## 3. Validaciones IDOR

### 3.1 ¿Qué es IDOR?
IDOR (Insecure Direct Object Reference) ocurre cuando un usuario puede acceder a recursos de otro usuario modificando el identificador en la URL.

### 3.2 Protección en Backend

El backend implementa validación IDOR en todos los endpoints sensibles:

| Módulo | Validación |
|--------|------------|
| Cuentas | `socioId` del token debe coincidir con `cuenta.socioId` |
| Créditos | `socioId` del token debe coincidir con `solicitud.socioId` |
| Beneficiarios | `socioId` del token debe coincidir con `beneficiario.socioId` |
| KYC | Solo el socio propietario puede ver su estado |
| Documentos | Solo el socio propietario puede generar/descargar sus documentos |

### 3.3 Implementación Frontend

El frontend **nunca** debe confiar en la validación del lado del cliente. Siempre se debe:

1. Usar IDs del contexto autenticado (no de la URL para operaciones sensibles)
2. Manejar apropiadamente los errores 403
3. No exponer IDs sensibles en la UI

```typescript
// ❌ INCORRECTO
const { data } = useQuery({
  queryKey: ['cuenta', params.numero],
  queryFn: () => apiClient.get(`/cuentas/${params.numero}`)
});

// ✅ CORRECTO (usar ID del usuario autenticado)
const { data: user } = useAuthStore();
const { data } = useQuery({
  queryKey: ['cuentas', user.socioId],
  queryFn: () => apiClient.get(`/cuentas/socio/${user.socioId}`)
});
```

---

## 4. Navegación por Rol

### 4.1 SOCIO

```
/dashboard                    → Dashboard
├── /dashboard/cuentas       → Cuentas
│   └── /dashboard/cuentas/[numero] → Detalle
│       ├── /depositar       → Depósito
│       └── /retirar         → Retiro
├── /dashboard/creditos      → Créditos
│   ├── /solicitar           → Nueva solicitud
│   ├── /simulador           → Simulador
│   └── /[numero]            → Detalle
│       └── /pagar           → Pagar cuota
├── /dashboard/kyc          → KYC
├── /dashboard/beneficiarios → Beneficiarios
├── /dashboard/documentos    → Documentos
├── /dashboard/perfil        → Perfil
└── /dashboard/configuracion → Configuración
```

### 4.2 ADMIN

```
/admin                       → Dashboard Admin
├── /admin/socios           → Gestión Socios
│   ├── /solicitudes        → Solicitudes pendientes
│   └── /[id]              → Detalle socio
├── /admin/creditos         → Gestión Créditos
│   ├── /solicitudes        → Cola solicitudes
│   └── /[numero]           → Evaluar
├── /admin/kyc              → Revisión KYC
├── /admin/documentos       → Reportes
└── /admin/estadisticas     → Estadísticas
```

### 4.3 SUPER_ADMIN (futuro)

Hereda todos los permisos de ADMIN más:
- Gestión de usuarios admins
- Configuración del sistema
- Auditoría global

---

## 5. Reglas de Negocio

### 5.1 Beneficiarios
- Máximo 5 beneficiarios activos por socio
- Suma de porcentajes **debe ser exactamente 100%** desde el inicio
- Documento del beneficiario no puede ser igual al del socio titular
- No puede haber documentos duplicados entre beneficiarios activos

### 5.2 KYC
- KYC debe estar APROBADO antes de solicitar créditos
- El consentimiento LOPDP es obligatorio para iniciar KYC
- Los documentos deben ser JPEG, PNG o PDF (max 10MB)

### 5.3 Créditos
- No se puede solicitar crédito si ya hay uno en estado DESEMBOLSADO
- El simulador requiere autenticación (rate limiting)
- Pagos de cuotas son idempotentes (prevenir double-payment)

### 5.4 Cuentas
- Depósito máximo: 500,000.00
- Retiro máximo diario: 50,000.00
- Saldo no puede quedar por debajo del mínimo requerido (warning)

---

## 6. Estados de Recursos

### 6.1 Socio
```
PENDIENTE_APROBACION → ACTIVO → INACTIVO → ELIMINADO
```

### 6.2 Solicitud Registro
```
PENDIENTE → APROBADA → RECHAZADA
```

### 6.3 Cuenta
```
ACTIVA → SUSPENDIDA → CERRADA
```

### 6.4 Solicitud Crédito
```
PENDIENTE → EN_EVALUACION → APROBADA/RECHAZADA → DESEMBOLSADO → CANCELADO
```

### 6.5 KYC
```
PENDIENTE → EN_REVISION → APROBADO/RECHAZADO/PENDIENTE (info solicitada)
```

### 6.6 Cuota
```
PENDIENTE → VENCIDA → PAGADA
```

---

## 7. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-21 | @product-manager | Creación inicial |
| 1.1 | 2026-04-21 | @product-manager | Agregada nota sobre roles pendientes CAJERO y ANALISTA_KYC |

---

## 8. Referencias

- Roadmap: [FRONTEND_ROADMAP.md](./FRONTEND_ROADMAP.md)
- Páginas: [ESTRUCTURA_PAGINAS.md](./ESTRUCTURA_PAGINAS.md)
- API Auth: `/docs/modulos/auth/API.md`
- API Socios: `/docs/modulos/socios/API.md`
- API Créditos: `/docs/modulos/creditos/API.md`
- API KYC: `/docs/modulos/kyc/API.md`
- API Beneficiarios: `/docs/modulos/beneficiarios/API.md`
- API Documentos: `/docs/modulos/documentospdf/API.md`
