# Módulo SOCIOS - Especificación Técnica

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-14  
**Estado:** Implementado  
**Complejidad:** Media

---

## Resumen

El módulo **socios** es el corazón del Fondo de Ahorro. Administra el ciclo de vida de los socios (altas, bajas, suspensiones), toda la información KYC (Conoce a tu Cliente) y el sistema de solicitudes de registro. Fue diseñado siguiendo Clean Architecture para alta disponibilidad y baja deuda técnica, separando estrictamente el código de acceso (`auth`) de los datos del socio.

---

## 1. Objetivos del Módulo

### 1.1 Objetivo Principal
Administrar el ciclo de vida completo de los socios del Fondo de Ahorro, desde el registro inicial hasta la gestión de estados (activo, inactivo, eliminado).

### 1.2 Objetivos Secundarios
- Gestionar solicitudes de registro públicas (self-service)
- Mantener información personal y de contacto de cada socio
- Controlar el nivel KYC de cada socio
- Integrarse con el módulo AUTH para creación automática de usuarios
- Proporcionar mecanismos de búsqueda y filtrado eficientes

### 1.3 Scope
- ✅ Registro y gestión de socios
- ✅ Solicitudes de registro públicas
- ✅ Sistema de estados y transiciones
- ✅ Búsqueda por múltiples criterios
- ✅ Rate limiting por IP
- ✅ Soft delete para conservación de historial

### 1.4 Fuera del Scope
- ❌ Integración directa con APIs SAIME/SENIAT (módulo KYC)
- ❌ Gestión de créditos (módulo Créditos)
- ❌ Gestión de ahorros (módulo Ahorros)

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura General (Clean Architecture)

```
backend/src/main/java/com/tufondo/socios/
├── domain/                          # Capa de Dominio (puro, sin dependencias externas)
│   ├── model/                      # Entidades de dominio inmutables
│   │   ├── Socio.java             # Entidad principal
│   │   ├── SolicitudRegistro.java # Solicitud de registro público
│   │   ├── Direccion.java         # Value Object de dirección
│   │   └── ContactoEmergencia.java # Value Object de contacto
│   ├── repository/                 # Interfaces de repositorios
│   │   ├── SocioRepository.java
│   │   └── SolicitudRegistroRepository.java
│   ├── exception/                  # Excepciones de dominio
│   │   ├── SocioNoEncontradoException.java
│   │   ├── EstadoSocioInvalidoException.java
│   │   └── SolicitudNoEncontradaException.java
│   └── enums/
│       ├── EstadoSocio.java        # Estados del socio
│       ├── EstadoSolicitud.java    # Estados de solicitud
│       ├── TipoDocumento.java      # Tipos de documento
│       ├── Genero.java             # Géneros
│       ├── EstadoCivil.java       # Estados civiles
│       └── TipoContrato.java      # Tipos de contrato
│
├── application/                    # Capa de Aplicación (CASOS DE USO)
│   ├── usecase/
│   │   ├── CrearSocioUseCase.java
│   │   ├── ObtenerSocioUseCase.java
│   │   ├── ListarSociosUseCase.java
│   │   ├── ActualizarSocioUseCase.java
│   │   ├── EliminarSocioUseCase.java
│   │   ├── ActivarSocioUseCase.java
│   │   ├── DesactivarSocioUseCase.java
│   │   ├── BuscarSociosUseCase.java
│   │   ├── CrearSolicitudRegistroUseCase.java
│   │   ├── AprobarSolicitudUseCase.java
│   │   └── RechazarSolicitudUseCase.java
│   └── dto/                       # Data Transfer Objects
│       ├── CrearSocioRequestDTO.java
│       ├── ActualizarSocioRequestDTO.java
│       ├── ActualizarPerfilSocioRequestDTO.java
│       ├── SocioResponseDTO.java
│       ├── SocioSummaryDTO.java
│       ├── SolicitudRegistroRequestDTO.java
│       └── SolicitudRegistroResponseDTO.java
│
└── infrastructure/                # Capa de Infraestructura
    ├── presentation/
    │   ├── controller/
    │   │   └── SocioController.java # REST Controller
    │   └── exception/
    │       └── SocioExceptionHandler.java
    ├── persistence/
    │   ├── entity/                # Entidades JPA
    │   │   ├── SocioEntity.java
    │   │   └── SolicitudRegistroEntity.java
    │   ├── jpa/                   # Repositorios JPA
    │   │   ├── SocioJpaRepository.java
    │   │   └── SolicitudRegistroJpaRepository.java
    │   └── adapter/               # Implementaciones de repositorios
    │       ├── SocioRepositoryImpl.java
    │       └── SolicitudRegistroRepositoryImpl.java
    └── security/
        └── RateLimitFilter.java   # Rate limiting (Bucket4j)
```

---

## 3. Modelo de Dominio

### 3.1 Socio - Entidad Principal

```java
public final class Socio {
    private final UUID id;
    private final String numeroSocio;              // Número único generado
    private final String primerNombre;
    private final String segundoNombre;
    private final String primerApellido;
    private final String segundoApellido;
    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;           // Único por tipo
    private final LocalDate fechaNacimiento;
    private final Genero genero;
    private final EstadoCivil estadoCivil;
    private final String nacionalidad;
    private final Direccion direccion;
    private final String correoElectronico;         // Único
    private final String telefonoPrincipal;
    private final String telefonoSecundario;
    private final ContactoEmergencia contactoEmergencia;
    private final String empresa;
    private final String departamento;
    private final String cargo;
    private final TipoContrato tipoContrato;
    private final BigDecimal salario;
    private final String banco;
    private final String numeroCuenta;              // Enmascarado en respuestas
    private final EstadoSocio estado;
    private final int nivelKYC;
    private final Instant fechaRegistro;
    private final Instant fechaActualizacion;
}
```

**Métodos de fábrica:**
- `Socio.crear(...)` - Crea un nuevo socio con estado PENDIENTE_APROBACION
- `Socio.desdeParametros(...)` - Crea desde parámetros existentes
- `Socio.conEstado(...)` - Cambia el estado (con validación de transiciones)
- `Socio.conActualizacion()` - Actualiza fecha de modificación

**Relaciones:**
- Relación 1:1 con `Usuario` (vía `UsuarioCreatorPort`)
- Relación 1:N con `SolicitudRegistro`

---

### 3.2 SolicitudRegistro - Entidad para Registro Público

```java
public final class SolicitudRegistro {
    private final UUID id;
    private final String primerNombre;
    private final String segundoNombre;
    private final String primerApellido;
    private final String segundoApellido;
    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;
    private final LocalDate fechaNacimiento;
    private final Genero genero;
    private final String correoElectronico;
    private final String telefono;
    private final String empresa;
    private final String departamento;
    private final String cargo;
    private final TipoContrato tipoContrato;
    private final BigDecimal salario;
    private final String banco;
    private final String numeroCuenta;
    private final EstadoSolicitud estado;
    private final String motivoRechazo;
    private final Instant fechaSolicitud;
    private final Instant fechaProcesamiento;
}
```

**Flujo:**
1. Socio envía `SolicitudRegistro` (estado: PENDIENTE)
2. Admin revisa y aprueba o rechaza
3. Si se aprueba → Se crea Socio + Usuario automáticamente

---

### 3.3 Direccion - Value Object

```java
public final class Direccion {
    private final String calle;
    private final String ciudad;
    private final String estado;
    private final String codigoPostal;
    private final String pais;
}
```

**Validaciones:**
- Longitud máxima de cada campo (100 caracteres)
- Longitud del código postal (4-10 caracteres)

---

### 3.4 ContactoEmergencia - Value Object

```java
public final class ContactoEmergencia {
    private final String nombreCompleto;
    private final String telefono;
    private final String parentesco;
}
```

**Validaciones:**
- Todos los campos son requeridos
- Teléfono con formato válido

---

### 3.5 Enumeraciones

#### EstadoSocio
```java
public enum EstadoSocio {
    PENDIENTE_APROBACION,  // Esperando aprobación de admin
    ACTIVO,                // Puede realizar operaciones
    INACTIVO,              // Inactivo temporal
    ELIMINADO              // Soft delete
}
```

#### EstadoSolicitud
```java
public enum EstadoSolicitud {
    PENDIENTE,    // Esperando revisión
    APROBADA,     // Aprobada - se crean Socio + Usuario
    RECHAZADA     // Rechazada por admin
}
```

#### TipoDocumento
```java
public enum TipoDocumento {
    CEDULA_IDENTIDAD,
    PASAPORTE,
    CEDULA_EXTRANJERA
}
```

#### Genero
```java
public enum Genero {
    MASCULINO,
    FEMENINO,
    OTRO
}
```

#### EstadoCivil
```java
public enum EstadoCivil {
    SOLTERO,
    CASADO,
    UNION_LIBRE,
    DIVORCIADO,
    VIUDO
}
```

#### TipoContrato
```java
public enum TipoContrato {
    PERMANENTE,
    TEMPORAL,
    PRESTACION_SERVICIOS,
    PASANTE
}
```

---

## 4. Casos de Uso (Application Layer)

### 4.1 CrearSocioUseCase

Crea un nuevo socio en el sistema.

```java
@Component
@RequiredArgsConstructor
public class CrearSocioUseCase {
    public SocioResponseDTO ejecutar(CrearSocioRequestDTO request);
}
```

**Flujo:**
1. Valida datos del request
2. Genera número de socio único (SHA-256)
3. Crea entidad Socio
4. Persiste en repositorio
5. Retorna response DTO (con datos enmascarados)

---

### 4.2 ObtenerSocioUseCase

Obtiene un socio por ID.

```java
@Component
@RequiredArgsConstructor
public class ObtenerSocioUseCase {
    public SocioResponseDTO ejecutar(UUID id);
}
```

---

### 4.3 ListarSociosUseCase

Lista socios con paginación.

```java
@Component
@RequiredArgsConstructor
public class ListarSociosUseCase {
    public Page<SocioSummaryDTO> ejecutar(int page, int size);
}
```

**Configuración:**
- Página por defecto: 0
- Tamaño máximo: 100

---

### 4.4 ActualizarSocioUseCase

Actualiza datos de un socio (solo ADMIN).

```java
@Component
@RequiredArgsConstructor
public class ActualizarSocioUseCase {
    public SocioResponseDTO ejecutar(UUID id, ActualizarSocioRequestDTO request);
}
```

**Validaciones:**
- Estado válido para transición
- Campos sensibles no editables por el propio socio

---

### 4.5 ActivarSocioUseCase / DesactivarSocioUseCase

```java
@Component
@RequiredArgsConstructor
public class ActivarSocioUseCase {
    public SocioResponseDTO ejecutar(UUID id);
}

@Component
@RequiredArgsConstructor
public class DesactivarSocioUseCase {
    public SocioResponseDTO ejecutar(UUID id);
}
```

---

### 4.6 BuscarSociosUseCase

Búsqueda por criterios múltiples.

```java
@Component
@RequiredArgsConstructor
public class BuscarSociosUseCase {
    public Page<SocioSummaryDTO> ejecutar(
        String numeroSocio,
        String nombre,
        String cedula,
        String correo,
        String empresa,
        String estado,
        int page,
        int size
    );
}
```

---

### 4.7 CrearSolicitudRegistroUseCase

Permite registro público de nuevos socios.

```java
@Component
@RequiredArgsConstructor
public class CrearSolicitudRegistroUseCase {
    public SolicitudRegistroResponseDTO ejecutar(SolicitudRegistroRequestDTO request);
}
```

**Flujo (Self-Service):**
1. Valida datos del request
2. Crea SolicitudRegistro (estado: PENDIENTE)
3. Admin recibe notificación
4. Admin revisa y aprueba/rechaza

---

### 4.8 AprobarSolicitudUseCase

Aprueba una solicitud y crea Socio + Usuario automáticamente.

```java
@Component
@RequiredArgsConstructor
public class AprobarSolicitudUseCase {
    public SocioResponseDTO ejecutar(UUID solicitudId);
}
```

**Flujo:**
1. Busca SolicitudRegistro
2. Valida que esté PENDIENTE
3. Crea Socio (estado: ACTIVO)
4. Invoca `UsuarioCreatorPort` para crear Usuario
5. Actualiza SolicitudRegistro (estado: APROBADA)
6. Retorna SocioResponseDTO

**Puerto definido:**
```java
public interface UsuarioCreatorPort {
    void crearUsuario(UUID socioId, String correoElectronico, String nombreCompleto);
}
```

---

### 4.9 RechazarSolicitudUseCase

Rechaza una solicitud con motivo.

```java
@Component
@RequiredArgsConstructor
public class RechazarSolicitudUseCase {
    public void ejecutar(UUID solicitudId, String motivo);
}
```

---

## 5. Estados y Transiciones

### 5.1 EstadoSocio - State Machine

```
┌─────────────────────┐
│ PENDIENTE_APROBACION│
└─────────┬───────────┘
          │ (aprobación admin)
          ▼
┌─────────────────────┐     ┌───────────┐
│      ACTIVO         │────►│ INACTIVO  │
└─────────┬───────────┘     └───────────┘
          │
          │ (soft delete)
          ▼
┌─────────────────────┐
│     ELIMINADO       │
└─────────────────────┘
```

**Transiciones válidas:**
- PENDIENTE_APROBACION → ACTIVO (solo via aprobación de admin)
- ACTIVO → INACTIVO (desactivación)
- INACTIVO → ACTIVO (reactivación)
- ACTIVO → ELIMINADO (soft delete)
- INACTIVO → ELIMINADO (soft delete)

**Transiciones inválidas (cortadas inmediatamente):**
- ELIMINADO → cualquier estado
- PENDIENTE_APROBACION → INACTIVO

### 5.2 EstadoSolicitud - State Machine

```
[SOLICITUD CREADA] → estado: PENDIENTE
                           │
                           ├── Admin aprueba ──► APROBADA
                           │                        │
                           │                   (Crea Socio + Usuario)
                           │
                           └── Admin rechaza ──► RECHAZADA
                                                    │
                                              (Solo PENDIENTE puede rechazarse)
```

---

## 6. Excepciones

| Excepción | HTTP Status | Código | Descripción |
|-----------|-------------|--------|-------------|
| `SocioNoEncontradoException` | 404 | SOCIO_NO_ENCONTRADO | Socio con ID especificado no existe |
| `EstadoSocioInvalidoException` | 400 | ESTADO_SOCIO_INVALIDO | Transición de estado no permitida |
| `SocioDuplicadoException` | 409 | SOCIO_DUPLICADO | Ya existe socio con mismo correo/documento |
| `SolicitudNoEncontradaException` | 404 | SOLICITUD_NO_ENCONTRADA | Solicitud no existe |
| `SolicitudYaProcesadaException` | 400 | SOLICITUD_YA_PROCESADA | Solicitud ya aprobada/rechazada |

---

## 7. Integración con AUTH (Puerto + Adaptador)

### 7.1 Puerto UsuarioCreatorPort

Para desacoplar SOCIOS de AUTH, se define un puerto:

```java
public interface UsuarioCreatorPort {
    void crearUsuario(UUID socioId, String correoElectronico, String nombreCompleto);
}
```

Este puerto es implementado por el módulo AUTH y registrado como bean. Cuando `AprobarSolicitudUseCase` se ejecuta, invoca este puerto para crear el usuario vinculado.

**Beneficio:** Los módulos SOCIOS y AUTH pueden desplegarse independientemente.

---

## 8. Seguridad Implementada

### 8.1 Rate Limiting

Filtro usando **Bucket4j** con configuración de 60 solicitudes por minuto por IP.

**Endpoints protegidos:**
- `POST /api/v1/socios` - 60 req/min
- `PUT /api/v1/socios/{id}` - 60 req/min
- `GET /api/v1/socios/buscar` - 60 req/min

**Response cuando se excede (429 Too Many Requests):**
```json
{
  "codigo": "RATE_LIMIT_EXCEDIDO",
  "mensaje": "Demasiadas solicitudes. Intente nuevamente en 60 segundos."
}
```

---

### 8.2 Soft Delete

- Los socios con estado `ELIMINADO` son excluidos automáticamente de TODAS las consultas
- El `SocioRepositoryImpl` filtra `estado <> 'ELIMINADO'` en todos los métodos
- Nunca se ejecuta `DELETE` físico en la base de datos

---

### 8.3 Mass Assignment Prevention

- `ActualizarSocioDTO` solo contiene campos editables por ADMIN
- `ActualizarPerfilSocioDTO` contiene campos editables por el propio socio
- Campos sensibles (`estado`, `roles`, `salario`, `numeroCuenta`) eliminados de DTOs públicos

---

### 8.4 Enmascaramiento de Datos Financieros

**SocioResponseDTO** aplica:
- `numeroCuenta`: `****1234` (solo últimos 4 dígitos visibles)
- `salario`: `****` (completamente enmascarado)
- Nunca se incluye `passwordHash` en respuestas

---

### 8.5 Validación en Constructores

- `Direccion`: Longitud máxima 100 caracteres por campo, código postal 4-10 caracteres
- `ContactoEmergencia`: Todos los campos requeridos
- `Socio`: Valida número de documento único por tipo

---

## 9. Dependencias Externas

| Dependencia | Propósito |
|-------------|-----------|
| `spring-boot-starter-data-jpa` | Persistencia JPA |
| `spring-boot-starter-validation` | Validación de DTOs |
| `bucket4j` | Rate limiting |
| `springdoc-openapi` | Documentación Swagger |

---

## 10. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @documentador | Creación inicial - Reorganización de documentación |
| 1.0 | 2026-04-14 | @auditoria | Correcciones: SQL Injection, Mass Assignment, Soft Delete, Enmascaramiento datos financieros, Rate Limiting |

---

## 11. Referencias

- API: `/docs/modulos/socios/API.md`
- Modelo de datos: `/docs/modulos/socios/MODELO_DATOS.md`
- Módulo AUTH: `/docs/modulos/auth/SPEC.md`
- Módulo KYC: `/docs/modulos/kyc/SPEC.md`
