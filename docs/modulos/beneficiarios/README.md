# Módulo de Beneficiarios

> Gestión de beneficiarios de socios del Fondo de Ahorro

## 📋 Descripción

El **Módulo de Beneficiarios** gestiona el ciclo de vida completo de los beneficiarios designados por los socios del Fondo de Ahorro. Un beneficiario es la persona que recibirá los fondos acumulados en la cuenta de ahorro del socio en caso de fallecimiento.

Este módulo permite a los socios registrar, consultar, actualizar y eliminar beneficiarios, garantizando que la suma de porcentajes de asignación sea exactamente 100% y que se cumplan todas las reglas de negocio definidas.

**Ubicación:** `/backend/src/main/java/com/tufondo/beneficiaries/`

---

## 🎯 Alcance

### ✅ Incluido

- Registro de beneficiarios con validación de porcentaje
- Consulta de lista de beneficiarios por socio
- Actualización de datos de beneficiarios existentes
- Eliminación lógica (soft delete) de beneficiarios
- Validación de suma de porcentajes = 100%
- Límite máximo de 5 beneficiarios por socio
- Validación de documento único por socio
- Prevención de documento igual al titular
- Auditoría completa de todos los cambios
- Rate limiting por socio (10 req/min)
- Validación IDOR en todos los endpoints

### ❌ Fuera del Scope

- Gestión de socios (módulo Socios)
- Validación KYC de beneficiarios (módulo KYC)
- Procesamiento de fallecimiento (módulo Mortis)
- Distribución automática de fondos al Fallecimiento
- Gestión deWallet o cuentas de beneficiarios

---

## 🏗️ Arquitectura

El módulo sigue **Clean Architecture** con las siguientes capas:

```
backend/src/main/java/com/tufondo/beneficiaries/
├── domain/                          # Capa de Dominio (puro)
│   ├── model/                      # Entidades inmutables
│   │   └── Beneficiario.java
│   ├── repository/                 # Interfaces de repositorios
│   ├── exception/                 # Excepciones de negocio
│   └── enums/                     # TipoDocumento, Parentesco
│
├── application/                    # Capa de Aplicación
│   ├── usecase/                   # Casos de uso
│   ├── dto/                       # DTOs de request/response
│   └── port/                      # Puertos (interfaces)
│       ├── SocioQueryPort.java
│       └── BeneficiarioQueryPort.java
│
└── infrastructure/               # Capa de Infraestructura
    ├── presentation/
    │   ├── controller/
    │   │   └── BeneficiarioController.java
    │   └── exception/
    │       └── BeneficiarioExceptionHandler.java
    ├── persistence/
    │   ├── entity/               # BeneficiarioEntity
    │   ├── jpa/                  # Repositorios JPA
    │   └── adapter/             # BeneficiarioRepositoryImpl
    └── security/
        └── RateLimitFilter.java
```

---

## 🔐 Seguridad

El módulo implementa múltiples controles de seguridad:

| Control | Descripción | Implementación |
|---------|-------------|----------------|
| **Autenticación** | JWT Bearer token requerido | Header `Authorization: Bearer <token>` |
| **Autorización RBAC** | Roles: SOCIO, ADMIN | Validación por rol en cada endpoint |
| **Validación IDOR** | Socio solo accede a sus propios beneficiarios | Verificación socioId en todos los endpoints |
| **Rate Limiting** | 10 req/min por socio | Bucket4j filter |
| **Soft Delete** | Eliminación lógica, nunca física | Campo `activo = false` |
| **Auditoría** | Logging de todos los cambios | Tabla `beneficiaries_audit` |
| **Validaciones** | Campos, porcentajes, documentos | DTOs + Constraints BD |

---

## 📊 Métricas de Compliance

| Métrica | Valor | Estado |
|---------|-------|--------|
| Retención de auditoría | 7 años | ✅ Cumple |
| Retención de registros eliminados | 7 años | ✅ Cumple |
| Backups | 7 años (cold storage) | ✅ Cumple |
| Trazabilidad de cambios | 100% | ✅ Cumple |
| Validación IDOR | Todos los endpoints | ✅ Implementado |
| Rate Limiting | Configurado | ✅ Implementado |
| Encriptación en tránsito | TLS 1.2+ | ✅ Configurado |

---

## 🔄 Flujo de Negocio

### Registrar Beneficiario

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Socio     │───►│  Validar   │───►│  Validar    │───►│  Validar    │
│  solicita   │    │   socio     │    │ documento   │    │   límite    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                              │
                                                              ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Registrar  │◄───│  Crear      │◄───│  Validar    │◄───│  Validar    │
│  auditoría  │    │ beneficiario│    │ porcentaje  │    │  no existe  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**Pasos detallados:**

1. **Validar socio existe y está activo**
   - Consultar `SocioQueryPort.existsByIdAndActivoTrue()`

2. **Validar datos del request**
   - nombreCompleto: max 200 chars
   - numeroDocumento: max 20 chars
   - tipoDocumento: enum válido
   - parentesco: enum válido
   - porcentaje: 0.01 - 100.00

3. **Validar documento no igual al titular**
   - Consultar `SocioQueryPort.getNumeroDocumentoById()`
   - Comparar con documento del beneficiario

4. **Validar no existe beneficiario duplicado**
   - Buscar beneficiario activo con mismo documento

5. **Validar máximo 5 beneficiarios**
   - Contar beneficiarios activos del socio

6. **Validar suma de porcentajes**
   - Verificar que suma + nuevo porcentaje <= 100%

7. **Crear entidad y persistir**
   - Generar UUID
   - Establecer timestamps
   - Guardar en repositorio

8. **Registrar auditoría**
   - Insertar en `beneficiaries_audit`

9. **Retornar response DTO**

---

## 📝 Reglas de Negocio

| ID | Regla | Validación |
|----|-------|------------|
| RN-B-01 | Porcentaje válido | Rango: 0.01 - 100.00 |
| RN-B-02 | Suma = 100% | La suma de porcentajes de beneficiarios activos debe ser exactamente 100% |
| RN-B-03 | Máximo 5 beneficiarios | Un socio no puede tener más de 5 beneficiarios activos |
| RN-B-04 | Documento único | No puede haber dos beneficiarios activos con el mismo documento |
| RN-B-05 | Documento diferente al titular | El documento del beneficiario no puede ser igual al del socio titular |
| RN-B-06 | Socio debe existir | El socio debe existir y estar activo |
| RN-B-07 | Beneficiario debe estar activo | Para modificar/eliminar, el beneficiario debe estar activo |

---

## 👥 Roles y Permisos

| Rol | Permisos |
|-----|----------|
| **SOCIO** | Registrar, consultar, actualizar y eliminar SUS PROPIOS beneficiarios |
| **ADMIN** | Acceso total a beneficiarios de cualquier socio (auditoría) |

**Restricciones:**
- Un socio solo puede ver/modificar sus propios beneficiarios
- Validación IDOR enforced en todos los endpoints
- ADMIN puede acceder a cualquier beneficiario

---

## 📌 Endpoints Disponibles

| # | Método | Path | Descripción | Rate Limit |
|---|--------|------|-------------|------------|
| 1 | POST | `/api/v1/socios/{socioId}/beneficiarios` | Registrar beneficiario | 10 req/min |
| 2 | GET | `/api/v1/socios/{socioId}/beneficiarios` | Listar beneficiarios | 30 req/min |
| 3 | GET | `/api/v1/socios/{socioId}/beneficiarios/{id}` | Consultar uno | 30 req/min |
| 4 | PUT | `/api/v1/socios/{socioId}/beneficiarios/{id}` | Actualizar | 10 req/min |
| 5 | DELETE | `/api/v1/socios/{socioId}/beneficiarios/{id}` | Eliminar (soft) | 10 req/min |

---

## 🔗 Integraciones

El módulo se integra con los siguientes módulos mediante puertos (interfaces):

### Módulo Socios (SocioQueryPort)

```java
public interface SocioQueryPort {
    boolean existsByIdAndActivoTrue(UUID socioId);
    String getNumeroDocumentoById(UUID socioId);
}
```

**Beneficio:** Los módulos pueden desplegarse independientemente.

### Módulo KYC (Futuro - KYCQueryPort)

```java
public interface KYCQueryPort {
    boolean validarDocumento(String tipoDocumento, String numeroDocumento);
}
```

**Estado:** Puerto definido para implementación futura.

---

## 📦 Estado del Módulo

| Aspecto | Estado | Notas |
|---------|--------|-------|
| Desarrollo | ✅ Completado | Implementación funcional |
| Code Review | ⏳ Pendiente | Esperando revisión |
| Seguridad | ✅ Aprobado | Controles IDOR, rate limiting, auditoría |
| Pruebas | ⏳ Pendiente | Unitarias e integración |
| Producción | ⏳ En espera | Depende de code review |

---

## 📝 Versión

**Versión actual:** 1.0.0
**Última actualización:** 2026-04-19
**Autor:** @documentador

---

## 🔗 Enlaces Útiles

| Documento | Descripción |
|-----------|-------------|
| [SPEC.md](./SPEC.md) | Especificación técnica completa |
| [MODELO_DATOS.md](./MODELO_DATOS.md) | Modelo de datos y DDL |
| [API.md](./API.md) | Referencia de API REST |
| [CHANGELOG.md](./CHANGELOG.md) | Historial de cambios |
| [Módulo Socios](../socios/SPEC.md) | Documentación del módulo Socios |

---

## 📋 Casos de Uso

### Para SOCIOS

| Caso de Uso | Descripción | Endpoint |
|-------------|-------------|----------|
| Registrar Beneficiario | Crear nuevo beneficiario | `POST /api/v1/socios/{socioId}/beneficiarios` |
| Listar Mis Beneficiarios | Ver todos mis beneficiarios | `GET /api/v1/socios/{socioId}/beneficiarios` |
| Ver Detalle | Consultar un beneficiario | `GET /api/v1/socios/{socioId}/beneficiarios/{id}` |
| Actualizar Beneficiario | Modificar datos | `PUT /api/v1/socios/{socioId}/beneficiarios/{id}` |
| Eliminar Beneficiario | Soft delete | `DELETE /api/v1/socios/{socioId}/beneficiarios/{id}` |

### Para ADMIN

| Caso de Uso | Descripción | Endpoint |
|-------------|-------------|----------|
| Ver Beneficiarios de Cualquer Socio | Auditoría | Cualquier endpoint con otro socioId |
| Consultar Auditoría | Ver historial de cambios | Tabla `beneficiaries_audit` |

---

## Estados de Transición

```
┌──────────────────┐
│     ACTIVO       │◄─────────────────┐
└────────┬─────────┘                  │
         │ (soft delete)              │
         ▼                            │
┌──────────────────┐                  │
│    INACTIVO      │──────────────────┘
└──────────────────┘   (reactivación vía UPDATE)
```

**Transiciones:**
- ACTIVO → INACTIVO: DELETE endpoint
- INACTIVO → ACTIVO: UPDATE endpoint (casos excepcionales)