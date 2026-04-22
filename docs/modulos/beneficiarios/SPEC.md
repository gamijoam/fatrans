# Módulo BENEFICIARIOS - Especificación Técnica

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-19  
**Estado:** Implementado  
**Complejidad:** Baja

---

## Resumen

El módulo **Beneficiarios** gestiona la información de los beneficiarios asociados a cada socio del Fondo de Ahorro. Un beneficiario es la persona designada por el socio para recibir los fondos en caso de fallecimiento. Fue diseñado siguiendo Clean Architecture con enfoque en la validación de reglas de negocio críticas como la suma de porcentajes y el límite máximo de beneficiarios.

---

## 1. Objetivos del Módulo

### 1.1 Objetivo Principal
Administrar el ciclo de vida de los beneficiarios de los socios del Fondo de Ahorro, incluyendo registro, actualización, consulta y eliminación (soft delete).

### 1.2 Objetivos Secundarios
- Garantizar que la suma de porcentajes de beneficiarios activos sea exactamente 100%
- Limitar a máximo 5 beneficiarios por socio activo
- Prevenir duplicación de documentos entre beneficiarios activos del mismo socio
- Impedir que un beneficiario tenga el mismo número de documento que el socio titular
- Mantener auditoría completa de todos los cambios

### 1.3 Scope
- ✅ Registro de beneficiarios
- ✅ Consulta de beneficiarios por socio
- ✅ Actualización de beneficiarios
- ✅ Eliminación lógica (soft delete)
- ✅ Validación de porcentaje (0.01 - 100.00)
- ✅ Validación de suma de porcentajes = 100%
- ✅ Límite de 5 beneficiarios por socio
- ✅ Auditoría de cambios (CREATE, UPDATE, DELETE)
- ✅ Rate limiting por socio

### 1.4 Fuera del Scope
- ❌ Gestión de socio (módulo Socios)
- ❌ Validación KYC de beneficiarios (módulo KYC)
- ❌ Procesamiento de fallecimiento (módulo Mortis)
- ❌ Distribución automática de fondos

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura General (Clean Architecture)

```
backend/src/main/java/com/tufondo/beneficiaries/
├── domain/                          # Capa de Dominio (puro, sin dependencias externas)
│   ├── model/                      # Entidades de dominio inmutables
│   │   └── Beneficiario.java       # Entidad principal
│   ├── repository/                 # Interfaces de repositorios
│   │   └── BeneficiarioRepository.java
│   ├── exception/                  # Excepciones de dominio
│   │   ├── BeneficiarioNoEncontradoException.java
│   │   ├── BeneficiarioDuplicadoException.java
│   │   ├── PorcentajeInvalidoException.java
│   │   ├── PorcentajeSumExcedidoException.java
│   │   └── MaximoBeneficiariosExcedidoException.java
│   └── enums/
│       ├── TipoDocumento.java      # Tipos de documento válidos
│       └── Parentesco.java         # Tipos de parentesco
│
├── application/                    # Capa de Aplicación (CASOS DE USO)
│   ├── usecase/
│   │   ├── CreateBeneficiarioUseCase.java
│   │   ├── GetBeneficiariosBySocioUseCase.java
│   │   ├── GetBeneficiarioByIdUseCase.java
│   │   ├── UpdateBeneficiarioUseCase.java
│   │   ├── DeleteBeneficiarioUseCase.java
│   │   └── ValidatePorcentajesSumUseCase.java
│   ├── dto/                       # Data Transfer Objects
│   │   ├── CreateBeneficiarioRequestDTO.java
│   │   ├── UpdateBeneficiarioRequestDTO.java
│   │   └── BeneficiarioResponseDTO.java
│   └── port/                      # Puertos (interfaces)
│       ├── SocioQueryPort.java     # Consulta socio titular
│       └── BeneficiarioQueryPort.java # Consulta beneficiarios
│
└── infrastructure/                # Capa de Infraestructura
    ├── presentation/
    │   ├── controller/
    │   │   └── BeneficiarioController.java # REST Controller
    │   └── exception/
    │       └── BeneficiarioExceptionHandler.java
    ├── persistence/
    │   ├── entity/                # Entidades JPA
    │   │   ├── BeneficiarioEntity.java
    │   │   └── BeneficiarioAuditEntity.java
    │   ├── jpa/                   # Repositorios JPA
    │   │   ├── BeneficiarioJpaRepository.java
    │   │   └── BeneficiarioAuditJpaRepository.java
    │   └── adapter/               # Implementaciones de repositorios
    │       └── BeneficiarioRepositoryImpl.java
    └── security/
        └── RateLimitFilter.java   # Rate limiting (Bucket4j)
```

---

## 3. Modelo de Dominio

### 3.1 Beneficiario - Entidad Principal

```java
public final class Beneficiario {
    private final UUID id;
    private final UUID socioId;                    // FK hacia Socio
    private final String nombreCompleto;
    private final String numeroDocumento;
    private final TipoDocumento tipoDocumento;
    private final Parentesco parentesco;
    private final BigDecimal porcentaje;           // Rango: 0.01 - 100.00
    private final String telefono;                  // Opcional
    private final Instant fechaRegistro;
    private final Instant fechaActualizacion;
    private final boolean activo;                  // Soft delete
}
```

**Métodos de fábrica:**
- `Beneficiario.crear(...)` - Crea un nuevo beneficiario con validaciones
- `Beneficiario.desdeParametros(...)` - Crea desde parámetros existentes
- `Beneficiario.conActualizacion()` - Actualiza fecha de modificación
- `Beneficiario.marcarInactivo()` - Soft delete

**Relaciones:**
- Relación N:1 con `Socio` (cada beneficiario pertenece a un socio)
- Relación 1:N con `BeneficiarioAudit` (auditoría de cambios)

---

### 3.2 Enumeraciones

#### TipoDocumento
```java
public enum TipoDocumento {
    CEDULA_IDENTIDAD,    // Cédula de identidad venezolana
    RIF,                  // Registro de Información Fiscal
    PASAPORTE,            // Pasaporte
    CEDULA_EXTRANJERO     // Cédula de extranjero
}
```

#### Parentesco
```java
public enum Parentesco {
    CONYUGE,     // Cónyuge
    HIJO,        // Hijo/a
    PADRE,       // Padre
    MADRE,       // Madre
    HERMANO,     // Hermano/a
    ABUELO,      // Abuelo/a
    NIETO,       // Nieto/a
    SOBRINO,     // Sobrino/a
    TIO,         // Tío/a
    OTRO         // Otro parentesco
}
```

---

## 4. Casos de Uso (Application Layer)

### 4.1 CreateBeneficiarioUseCase

Crea un nuevo beneficiario para un socio.

```java
@Component
@RequiredArgsConstructor
public class CreateBeneficiarioUseCase {
    public BeneficiarioResponseDTO ejecutar(UUID socioId, CreateBeneficiarioRequestDTO request);
}
```

**Flujo:**
1. Valida que el socio exista y esté activo
2. Valida datos del request
3. Valida que número de documento no sea igual al del socio titular
4. Valida que no exista beneficiario activo con mismo documento
5. Valida máximo de 5 beneficiarios
6. Valida que la suma de porcentajes no exceda 100%
7. Crea entidad Beneficiario
8. Persiste en repositorio
9. Registra auditoría (CREATE)
10. Retorna response DTO

**Puerto definido:**
```java
public interface SocioQueryPort {
    boolean existsByIdAndActivoTrue(UUID socioId);
    String getNumeroDocumentoById(UUID socioId);
}
```

---

### 4.2 GetBeneficiariosBySocioUseCase

Lista todos los beneficiarios activos de un socio.

```java
@Component
@RequiredArgsConstructor
public class GetBeneficiariosBySocioUseCase {
    public List<BeneficiarioResponseDTO> ejecutar(UUID socioId);
}
```

**Flujo:**
1. Valida que el socio exista
2. Consulta beneficiarios activos por socioId
3. Retorna lista de DTOs

---

### 4.3 GetBeneficiarioByIdUseCase

Obtiene un beneficiario específico por ID.

```java
@Component
@RequiredArgsConstructor
public class GetBeneficiarioByIdUseCase {
    public BeneficiarioResponseDTO ejecutar(UUID socioId, UUID beneficiarioId);
}
```

**Validación IDOR:**
- Verifica que el beneficiario pertenezca al socio especificado

---

### 4.4 UpdateBeneficiarioUseCase

Actualiza los datos de un beneficiario.

```java
@Component
@RequiredArgsConstructor
public class UpdateBeneficiarioUseCase {
    public BeneficiarioResponseDTO ejecutar(UUID socioId, UUID beneficiarioId, UpdateBeneficiarioRequestDTO request);
}
```

**Flujo:**
1. Valida que el beneficiario exista y esté activo
2. Valida IDOR (beneficiario pertenece al socio)
3. Valida datos del request
4. Si cambia número de documento:
   - Valida que no sea igual al del socio titular
   - Valida que no exista otro beneficiario activo con ese documento
5. Valida que la suma de porcentajes no exceda 100% (considerando el cambio)
6. Actualiza entidad
7. Persiste en repositorio
8. Registra auditoría (UPDATE con snapshot de datos anteriores y nuevos)
9. Retorna response DTO

---

### 4.5 DeleteBeneficiarioUseCase

Elimina un beneficiario (soft delete).

```java
@Component
@RequiredArgsConstructor
public class DeleteBeneficiarioUseCase {
    public void ejecutar(UUID socioId, UUID beneficiarioId);
}
```

**Flujo:**
1. Valida que el beneficiario exista y esté activo
2. Valida IDOR (beneficiario pertenece al socio)
3. Marca beneficiario como inactivo (activo = false)
4. Registra auditoría (DELETE con snapshot de datos anteriores)
5. No elimina físicamente

---

### 4.6 ValidatePorcentajesSumUseCase

Valida que la suma de porcentajes de beneficiarios activos sea exactamente 100%.

```java
@Component
@RequiredArgsConstructor
public class ValidatePorcentajesSumUseCase {
    public boolean ejecutar(UUID socioId, BigDecimal porcentajeExcluir);
}
```

**Parámetros:**
- `socioId`: ID del socio
- `porcentajeExcluir`: Porcentaje a excluir del cálculo (usado en updates para no contar el beneficiario actual)

**Retorna:** `true` si la suma es exactamente 100.00, `false` en caso contrario

---

## 5. Estados y Transiciones

### 5.1 Estado del Beneficiario - Simple State Machine

```
┌──────────────────┐
│     ACTIVO       │◄─────────────────┐
└────────┬─────────┘                  │
         │ (soft delete)              │
         ▼                            │
┌──────────────────┐                  │
│    INACTIVO      │──────────────────┘
└──────────────────┘   (reactivación posible via UPDATE)
```

**Transiciones:**
- ACTIVO → INACTIVO (soft delete via DELETE endpoint)
- INACTIVO → ACTIVO (vía UPDATE endpoint, en casos excepcionales)

---

## 6. Excepciones

| Excepción | HTTP Status | Código | Descripción |
|-----------|-------------|--------|-------------|
| `BeneficiarioNoEncontradoException` | 404 | BENEFICIARIO_NO_ENCONTRADO | Beneficiario con ID especificado no existe |
| `BeneficiarioDuplicadoException` | 409 | BENEFICIARIO_DUPLICADO | Ya existe beneficiario activo con mismo documento |
| `PorcentajeInvalidoException` | 400 | PORCENTAJE_INVALIDO | Porcentaje fuera de rango (0.01 - 100.00) |
| `PorcentajeSumExcedidoException` | 400 | PORCENTAJE_SUM_EXCEDIDO | Suma de porcentajes excedería 100% |
| `MaximoBeneficiariosExcedidoException` | 400 | MAXIMO_BENEFICIARIOS_EXCEDIDO | Socio ya tiene 5 beneficiarios activos |
| `DocumentoIgualAlTitularException` | 400 | DOCUMENTO_IGUAL_TITULAR | Documento del beneficiario igual al del socio |
| `SocioNoEncontradoException` | 404 | SOCIO_NO_ENCONTRADO | Socio con ID especificado no existe |

---

## 7. Integración con Otros Módulos (Puertos)

### 7.1 SocioQueryPort

Para desacoplar BENEFICIARIOS de SOCIOS, se define un puerto para consulta:

```java
public interface SocioQueryPort {
    boolean existsByIdAndActivoTrue(UUID socioId);
    String getNumeroDocumentoById(UUID socioId);
}
```

**Beneficio:** Los módulos BENEFICIARIOS y SOCIOS pueden desplegarse independientemente.

---

### 7.2 KYCQueryPort (Futuro)

Para futura validación de documentos contra APIs externas:

```java
public interface KYCQueryPort {
    boolean validarDocumento(String tipoDocumento, String numeroDocumento);
}
```

**Estado:** Puerto definido para implementación futura.

---

## 8. Seguridad Implementada

### 8.1 Rate Limiting

Filtro usando **Bucket4j** con configuración de 10 solicitudes por minuto por socio.

**Endpoints protegidos:**
- `POST /api/v1/socios/{socioId}/beneficiarios` - 10 req/min
- `PUT /api/v1/socios/{socioId}/beneficiarios/{id}` - 10 req/min
- `DELETE /api/v1/socios/{socioId}/beneficiarios/{id}` - 10 req/min

**Response cuando se excede (429 Too Many Requests):**
```json
{
  "codigo": "RATE_LIMIT_EXCEDIDO",
  "mensaje": "Demasiadas solicitudes. Intente nuevamente en 60 segundos."
}
```

---

### 8.2 Soft Delete

- Los beneficiarios con `activo = false` son excluidos de consultas normales
- El `BeneficiarioRepositoryImpl` filtra `activo = true` en métodos de listado
- El DELETE físico nunca se ejecuta en la base de datos

---

### 8.3 Validación IDOR (Insecure Direct Object Reference)

- El socioId en la URL debe coincidir con el socioId del beneficiario
- Un socio solo puede ver, editar o eliminar SUS propios beneficiarios
- Validación enforced en todos los endpoints del controller

```java
// Ejemplo de validación IDOR
if (!beneficiario.getSocioId().equals(socioId)) {
    throw new BeneficiarioNoEncontradoException();
}
```

---

### 8.4 Auditoría de Cambios

Todos los cambios son registrados en la tabla `beneficiaries_audit`:
- CREATE: Snapshot del nuevo registro en `datos_nuevos`
- UPDATE: Snapshot anterior en `datos_anteriores`, nuevo en `datos_nuevos`
- DELETE: Snapshot anterior en `datos_anteriores`

```json
{
  "entidad_tipo": "BENEFICIARIO",
  "entidad_id": "uuid-del-beneficiario",
  "accion": "UPDATE",
  "usuario_id": "uuid-del-socio",
  "rol_usuario": "SOCIO",
  "ip_cliente": "192.168.1.100",
  "datos_anteriores": { ... },
  "datos_nuevos": { ... },
  "fecha_evento": "2026-04-19T10:00:00Z"
}
```

---

## 9. Dependencias Externas

| Dependencia | Propósito |
|-------------|-----------|
| `spring-boot-starter-data-jpa` | Persistencia JPA |
| `spring-boot-starter-validation` | Validación de DTOs |
| `bucket4j` | Rate limiting |
| `springdoc-openapi` | Documentación Swagger |
| `postgresql` | Driver PostgreSQL |

---

## 10. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-19 | @product-manager | Creación inicial del módulo - Especificación técnica |
| 1.0 | 2026-04-19 | @documentador | Documentación formal del módulo Beneficiarios |

---

## 12. Notas de Seguridad Adicionales

### 12.1 OWASP Top 10 - Controles Implementados

| OWASP Top 10 | Control | Implementación |
|--------------|---------|----------------|
| A01 - Broken Access Control | Validación IDOR | Verificación socioId en todos los endpoints |
| A02 - Cryptographic Failures | Datos sensibles | Documentos no expuestos en logs |
| A03 - Injection | Validación de输入 | DTOs con validación de Jakarta |
| A04 - Insecure Design | Rate limiting | Bucket4j 10 req/min |
| A05 - Security Misconfiguration | Headers seguros | Configurados en SecurityConfig |
| A07 - Identification Failures | Autenticación JWT | Validación de token en cada request |
| A08 - Software Integrity | Auditoría | Log de todos los cambios |

### 12.2 Validación de Documentos

**Validación en aplicación:**
- El documento del beneficiario no puede ser igual al del socio titular
- No puede haber dos beneficiarios activos con el mismo documento
- Tipos válidos: CEDULA_IDENTIDAD, RIF, PASAPORTE, CEDULA_EXTRANJERO

**Validación en base de datos:**
```sql
-- Unique constraint para documento activo
CREATE UNIQUE INDEX idx_beneficiaries_documento_activo 
ON beneficiaries (socio_id, tipo_documento, numero_documento) 
WHERE activo = true;

-- Check constraint para tipo de documento
ALTER TABLE beneficiaries 
ADD CONSTRAINT chk_tipo_documento 
CHECK (tipo_documento IN ('CEDULA_IDENTIDAD', 'RIF', 'PASAPORTE', 'CEDULA_EXTRANJERO'));
```

### 12.3 Configuración de Rate Limiting

```yaml
# Bucket4j Configuration
endpoints:
  POST /api/v1/socios/{socioId}/beneficiarios:
    bandwidth:
      limit: 10
      refill: 10
      period: 1 minute
  PUT /api/v1/socios/{socioId}/beneficiarios/{id}:
    bandwidth:
      limit: 10
      refill: 10
      period: 1 minute
  DELETE /api/v1/socios/{socioId}/beneficiarios/{id}:
    bandwidth:
      limit: 10
      refill: 10
      period: 1 minute
```

### 12.4 Headers de Seguridad

| Header | Valor | Propósito |
|--------|-------|-----------|
| X-Content-Type-Options | nosniff | Previene MIME type sniffing |
| X-Frame-Options | DENY | Previene clickjacking |
| X-XSS-Protection | 1; mode=block | Protección XSS legacy |
| Strict-Transport-Security | max-age=31536000 | Force HTTPS |

---

## 13. Métricas de Compliance

### 13.1 Retención de Datos

| Tipo de Dato | Duración | Razón | Estado |
|--------------|----------|-------|--------|
| Registros de beneficiarios | 7 años post-eliminación | Regulación financiera | ✅ Cumple |
| Logs de auditoría | 7 años | Compliance | ✅ Cumple |
| Backups | 7 años (cold storage) | Recovery | ✅ Cumple |

### 13.2 Auditoría

| Métrica | Valor | Estado |
|---------|-------|--------|
| Trazabilidad de cambios | 100% | ✅ Cumple |
| Registros de auditoría完整性 | 100% | ✅ Cumple |
| Retention policy | 7 años | ✅ Configurado |

### 13.3 Seguridad

| Control | Cobertura | Estado |
|---------|-----------|--------|
| Validación IDOR | 100% endpoints | ✅ Implementado |
| Rate Limiting | 100% endpoints mutación | ✅ Implementado |
| Auditoría de cambios | 100% operaciones | ✅ Implementado |
| Soft Delete | 100% eliminaciones | ✅ Implementado |

---

## 14. Diagramas de Flujo de Uso

### 14.1 Flujo: Registrar Beneficiario

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLUJO: REGISTRAR BENEFICIARIO                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  SOCIO                              SISTEMA                                  │
│    │                                  │                                      │
│    │ POST /socios/{id}/beneficiarios  │                                      │
│    │ {datos del beneficiario}         │                                      │
│    │───────────────────────────────────►                                      │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Validar JWT y permisos  │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Socio existe y activo?  │                         │
│    │                     │ SocioQueryPort         │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Validar campos del DTO    │                        │
│    │                    │ (nombre, doc, %, etc.)    │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Documento ≠ Socio titular?│                        │
│    │                    │ SocioQueryPort           │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Doc único entre activos? │                        │
│    │                    │ BeneficiarioRepository   │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ < 5 beneficiarios activos?│                        │
│    │                    │ BeneficiarioRepository   │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Suma porcentajes ≤ 100%? │                        │
│    │                    │ ValidatePorcentajesSum  │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Crear Beneficiario       │                        │
│    │                    │ Persistir en BD          │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Registrar auditoría      │                        │
│    │                    │ CREATE en audit table    │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │◄──────────────────────────────── 201 Created                          │
│    │         {beneficiario creado}     {beneficiarioResponseDTO}            │
│    │                                  │                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 14.2 Flujo: Eliminar Beneficiario

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLUJO: ELIMINAR BENEFICIARIO                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  SOCIO                              SISTEMA                                  │
│    │                                  │                                      │
│    │ DELETE /socios/{id}/benefs/{id}  │                                      │
│    │───────────────────────────────────►                                      │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Validar JWT y permisos  │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Beneficiario existe?   │                         │
│    │                     │ BeneficiarioRepository │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Beneficiario belongs   │                         │
│    │                     │ to this socio? (IDOR)   │                         │
│    │                     │ throw 404 if not       │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Beneficiario activo?   │                         │
│    │                     │ throw 404 if not      │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Soft delete              │                        │
│    │                    │ activo = false          │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Calcular suma restante   │                        │
│    │                    │ de porcentajes           │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Registrar auditoría      │                        │
│    │                    │ DELETE en audit table   │                        │
│    │                    │ con snapshot de datos   │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Verificar = 100%?        │                        │
│    │                    │ Agregar warning si no   │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │◄──────────────────────────────── 200 OK                                │
│    │         {beneficiario inactivo}   {con warning si ≠ 100%}             │
│    │                                  │                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 14.3 Estados del Beneficiario

```
                    ┌───────────────────┐
                    │                   │
                    │    ACTIVO         │
                    │                   │
                    │  - Visible en     │
                    │    consultas      │
                    │  - Contabiliza    │
                    │    en suma %      │
                    │  - Puede ser      │
                    │    modificado     │
                    │                   │
                    └────────┬──────────┘
                             │
              ┌──────────────┴──────────────┐
              │  (soft delete)              │
              │                             │
              ▼                             │
    ┌───────────────────┐                  │
    │                   │                  │
    │   INACTIVO        │◄─────────────────┘
    │   (ELIMINADO)     │  (reactivación vía UPDATE)
    │                   │   (casos excepcionales)
    │  - Oculto en      │
    │    consultas      │
    │    normales       │
    │  - No contabiliza │
    │    en suma %      │
    │  - Registro       │
    │    preservado     │
    │    en BD          │
    │                   │
    └───────────────────┘
```

---

## 15. Historial de Cambios Completado

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-19 | @product-manager | Creación inicial del módulo - Especificación técnica |
| 1.0 | 2026-04-19 | @documentador | Documentación formal del módulo Beneficiarios |
| 1.0 | 2026-04-19 | @programador-java | Implementación de correcciones de seguridad (IDOR, rate limiting, auditoría) |
| 1.1 | 2026-04-19 | @documentador | Agregadas notas de seguridad OWASP, métricas de compliance y diagramas de flujo |

---

## 16. Referencias

- API: `/docs/modulos/beneficiarios/API.md`
- Modelo de datos: `/docs/modulos/beneficiarios/MODELO_DATOS.md`
- README: `/docs/modulos/beneficiarios/README.md`
- CHANGELOG: `/docs/modulos/beneficiarios/CHANGELOG.md`
- Módulo Socios: `/docs/modulos/socios/SPEC.md`
- Módulo KYC: `/docs/modulos/kyc/SPEC.md`
- OWASP Top 10: https://owasp.org/Top10/es/