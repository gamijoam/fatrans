# AUDITORÍA DE SEGURIDAD Y ARQUITECTURA - MÓDULO BENEFICIARIOS

**Proyecto:** Fondo de Ahorro (fondo-ahorro-platform)  
**Módulo:** Beneficiarios (MVP)  
**Fecha:** 2026-04-19  
**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Stack:** Java 21, Spring Boot 3.2.4, PostgreSQL  
**Alcance:** Documentación de especificación (`docs/modulos/beneficiarios/`)

---

## RESUMEN EJECUTIVO

| Severidad | Cantidad |
|-----------|----------|
| 🔴 CRÍTICA | 2 |
| 🟠 ALTA | 4 |
| 🟡 MEDIA | 0 |

### Estado General

**⚠️ MÓDULO NO IMPLEMENTADO** - El código fuente del módulo Beneficiarios no existe aún. Esta auditoría evalúa únicamente la documentación de especificación (SPEC.md, MODELO_DATOS.md, API.md).

### Hallazgos Principales

1. **BUG CRÍTICO en trigger de validación de suma de porcentajes** - La lógica NO garantiza que la suma sea exactamente 100%
2. **Race condition en validación documento igual al socio** - Vulnerable a ataque TOCTOU
3. **Rate limiting por IP en lugar de por socioId** - Viola la especificación de seguridad
4. **Validación IDOR incompleta en código existente** - El SocioController no valida que el usuario sea el dueño del recurso

### Nivel de Riesgo: 🔴 CRÍTICO

**El módulo requiere correcciones OBLIGATORIAS antes de implementación.**

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. BUG: Trigger de Validación de Porcentajes NO Garantiza Suma = 100%

**Archivo:** `MODELO_DATOS.md:315-349`  
**Severidad:** CRÍTICA  
**Categoría:** A04:2021 - Insecure Design / Violación Regla de Negocio RN-01

**Descripción:**

El trigger `trg_check_porcentaje_sum` en el DDL tiene una lógica DEFECTUOSA que viola la regla de negocio "Suma de porcentajes debe ser exactamente 100%":

```sql
-- MODELO_DATOS.md:315-349
CREATE OR REPLACE FUNCTION check_porcentaje_sum()
RETURNS TRIGGER AS $$
DECLARE
    v_sum DECIMAL(5,2);
    v_exclude_id UUID;
BEGIN
    -- Línea 322: ERROR - NEW.id puede ser NULL en INSERT
    v_exclude_id := COALESCE(NEW.id, OLD.id);
    
    SELECT COALESCE(SUM(porcentaje), 0) INTO v_sum
    FROM beneficiaries
    WHERE socio_id = NEW.socio_id 
      AND activo = true
      AND id != v_exclude_id;
    
    -- Líneas 331-333: INCORRECTO - Solo verifica > 100, no = 100
    IF TG_OP = 'INSERT' AND v_sum + NEW.porcentaje > 100.00 THEN
        RAISE EXCEPTION 'La suma de porcentajes excedería 100%%. Suma actual: %', v_sum;
    END IF;
    
    -- Líneas 335-338: INCORRECTO - Mismo problema
    IF TG_OP = 'UPDATE' AND v_sum + NEW.porcentaje > 100.00 THEN
        RAISE EXCEPTION 'La suma de porcentajes excedería 100%%. Suma actual (sin este): %', v_sum;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Problemas identificados:**

| Problema | Descripción |
|----------|-------------|
| Lógica > 100 en lugar de = 100 | La regla dice "exactamente 100%", pero el trigger solo rechaza si > 100 |
| NEW.id puede ser NULL en INSERT | `COALESCE(NEW.id, OLD.id)` resulta en NULL en INSERT (OLD.id no existe) |
| Suma parcial no se valida | Un beneficiario con 10%, otro con 20%, y al agregar 15% más: 10+20+15=45% < 100 y el trigger lo permite |
| No hay validación de completitud | Si al final la suma no es 100%, no hay mecanismo para obligar al socio a completar |

**Impacto:**

En un entorno FinTech/Bancario real:
- Un socio podría quedar con solo 45% de beneficiarios designados
- En caso de fallecimiento, solo 45% del fondo se distribuiría
- El 55% restante quedaría en un estado legal ambiguo
- **Incumplimiento regulatorio** - Los fondos de pensionesivatrimonia deben tener distribución completa

**Corrección:**

```sql
CREATE OR REPLACE FUNCTION check_porcentaje_sum()
RETURNS TRIGGER AS $$
DECLARE
    v_sum DECIMAL(5,2);
    v_exclude_id UUID;
BEGIN
    -- Para INSERT, NEW.id se genera con uuid_generate_v4() antes del trigger
    -- Para UPDATE, usamos OLD.id para excluir el registro actual
    IF TG_OP = 'INSERT' THEN
        v_exclude_id := NEW.id;  -- No usar COALESCE, NEW.id ya tiene valor
    ELSE
        v_exclude_id := OLD.id;
    END IF;
    
    -- Calcular suma ACTUAL de porcentajes (sin incluir el registro nuevo/modificado)
    SELECT COALESCE(SUM(porcentaje), 0) INTO v_sum
    FROM beneficiaries
    WHERE socio_id = NEW.socio_id 
      AND activo = true
      AND id != v_exclude_id;
    
    -- Para INSERT: La suma total DEBE ser exactamente 100%
    IF TG_OP = 'INSERT' AND v_sum + NEW.porcentaje != 100.00 THEN
        RAISE EXCEPTION 'La suma de porcentajes debe ser exactamente 100%%. Suma actual: % + nuevo: % = % (debe ser 100)', 
            v_sum, NEW.porcentaje, v_sum + NEW.porcentaje;
    END IF;
    
    -- Para UPDATE: Si cambia el porcentaje, la suma total DEBE ser 100%
    IF TG_OP = 'UPDATE' AND v_sum + NEW.porcentaje != 100.00 THEN
        RAISE EXCEPTION 'La suma de porcentajes debe ser exactamente 100%%. Suma actual (sin este): % + nuevo: % = % (debe ser 100)', 
            v_sum, NEW.porcentaje, v_sum + NEW.porcentaje;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Alternativa: Permitir suma < 100% pero marcar al socio como "INCOMPLETO" para requerir redistribución
```

**Adicionalmente**, se debe agregar un scheduled job que alerte cuando la suma != 100%:

```sql
CREATE OR REPLACE FUNCTION check_beneficiarios_incompletos()
RETURNS TABLE(socio_id UUID, suma_porcentajes DECIMAL) AS $$
BEGIN
    RETURN QUERY
    SELECT b.socio_id, SUM(b.porcentaje) as suma
    FROM beneficiaries b
    WHERE b.activo = true
    GROUP BY b.socio_id
    HAVING SUM(b.porcentaje) != 100.00;
END;
$$ LANGUAGE plpgsql;
```

---

### 2. Race Condition: Validación Documento Igual al Socio (TOCTOU)

**Archivo:** `SPEC.md:281-284`  
**Severidad:** CRÍTICA  
**Categoría:** A01:2021 - Broken Access Control

**Descripción:**

La validación de "Documento del beneficiario ≠ documento del socio" está implementada A NIVEL DE APLICACIÓN, creando una vulnerabilidad de Time-of-Check-Time-of-Use (TOCTOU):

```java
// SPEC.md:281-284 - CreateBeneficiarioUseCase
String numeroDocumentoSocio = socioQueryPort.getNumeroDocumentoById(socioId);
if (beneficiario.getNumeroDocumento().equals(numeroDocumentoSocio)) {
    throw new DocumentoIgualAlTitularException();
}
```

**Problema:** Esta validación ocurre en código Java, NO en la base de datos. Entre el momento que se consulta el documento del socio y se inserta el beneficiario, existe una ventana de carrera donde:

1. Thread A consulta documento del socio = "V-12345678" ✓
2. Thread B consulta documento del socio = "V-12345678" ✓
3. Thread A inserta beneficiario con documento "V-12345678" ✗ (debería fallar)
4. Thread B inserta beneficiario con documento "V-12345678" ✗ (debería fallar)

**Impacto:**

- **Fraude interno**: Un empleado malicioso podría crear un beneficiario con el mismo documento que el socio titular para desviar fondos
- **Incumplimiento KYC**: Se viola la regla de que un beneficiario debe ser persona diferente al titular
- **Pérdida financiera**: Fondos podrían pagarse a una persona que no es beneficiario legítimo

**Corrección OBLIGATORIA:**

```sql
-- Agregar a la tabla beneficiaries una foreign key compuesta que incluya
-- una referencia al documento del socio para validación en BD

-- Opción 1: CHECK constraint a nivel de tabla (PostgreSQL)
ALTER TABLE beneficiaries
ADD CONSTRAINT chk_documento_diff_socio 
CHECK (
    (numero_documento, tipo_documento, socio_id) NOT IN (
        SELECT s.numero_documento, s.tipo_documento, b.id
        FROM socios s
        CROSS JOIN beneficiaries b
        WHERE b.socio_id = s.id
    )
);

-- Opción 2: Trigger a nivel de base de datos
CREATE OR REPLACE FUNCTION check_documento_diff_socio()
RETURNS TRIGGER AS $$
DECLARE
    v_doc_socio VARCHAR(20);
    v_tipo_doc_socio VARCHAR(30);
BEGIN
    SELECT numero_documento, tipo_documento 
    INTO v_doc_socio, v_tipo_doc_socio
    FROM socios
    WHERE id = NEW.socio_id;
    
    IF NEW.numero_documento = v_doc_socio AND NEW.tipo_documento = v_tipo_doc_socio THEN
        RAISE EXCEPTION 'El documento del beneficiario no puede ser igual al del socio titular';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_documento_diff_socio
BEFORE INSERT OR UPDATE ON beneficiaries
FOR EACH ROW
EXECUTE FUNCTION check_documento_diff_socio();
```

---

## VIOLACIONES ALTAS (🟠)

### 3. Rate Limiting por IP en Lugar de por SocioId

**Archivo:** `SPEC.md:376-392` / `SocioController.java:151-170`  
**Severidad:** ALTA  
**Categoría:** A04:2021 - Insecure Design

**Descripción:**

La especificación del módulo Beneficiarios dice claramente:

> Rate limiting: 10 requests/min **por socio**

Pero el código existente en `SocioController.java` implementa rate limiting **por IP**:

```java
// SocioController.java:151-170
private void checkRateLimit(HttpServletRequest request) {
    String clientIp = getClientIp(request);  // <-- Por IP, no por socioId
    long currentMinute = System.currentTimeMillis() / 1000 / 60;
    // ...
}
```

**Impacto:**

- Un atacante desde UNA IP podría agotar el rate limit de TODOS los socios
- Legítimos usuarios detrás de proxy/NAT comparten el mismo rate limit
- **Violación de SLA** para socios que compartan IP

**Corrección:**

```java
// RateLimitFilter.java - De acuerdo a SPEC.md debería ser por socioId
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class BeneficiariosRateLimitFilter extends OncePerRequestFilter {

    private static final int REQUESTS_PER_MINUTE = 10;
    private static final Map<UUID, Bucket> BUCKETS = new ConcurrentHashMap<>();
    
    // Extraer socioId del JWT, NO de la IP
    @Override
    protected void doFilterInternal(...) {
        UUID socioId = extractSocioIdFromJWT(request);  // <-- Por socioId
        Bucket bucket = BUCKETS.computeIfAbsent(socioId, this::createBucket);
        // ...
    }
}
```

---

### 4. Falta Validación IDOR en Controlador vs. Especificación

**Archivo:** `SPEC.md:403-414` vs `SocioController.java:63-71`  
**Severidad:** ALTA  
**Categoría:** A01:2021 - Broken Access Control

**Descripción:**

La especificación delega la validación IDOR al UseCase:

```java
// SPEC.md:411-414
if (!beneficiario.getSocioId().equals(socioId)) {
    throw new BeneficiarioNoEncontradoException();
}
```

Pero en el `SocioController` existente NO hay validación de que el usuario autenticado sea el dueño del recurso:

```java
// SocioController.java:63-71 - NO hay validación IDOR
@GetMapping("/{id}")
public ResponseEntity<SocioResponseDTO> obtenerSocio(
        @PathVariable UUID id,
        HttpServletRequest request) {
    checkRateLimit(request);  // <-- Solo rate limiting, NO IDOR
    SocioResponseDTO response = obtenerSocioUseCase.ejecutar(id);
    return ResponseEntity.ok(response);
}
```

**Impacto:**

- Un socio podría ver/editar/eliminar datos de OTRO socio si conoce su ID
- Violación directa de A01:2021 (Broken Access Control)
- En módulo Financiero: acceso no autorizado a datos sensibles

**Corrección:**

```java
@GetMapping("/{id}")
public ResponseEntity<SocioResponseDTO> obtenerSocio(
        @PathVariable UUID id,
        HttpServletRequest request,
        @AuthenticationPrincipal SocioDetails socioDetails) {  // <-- Extraer usuario
    
    // VALIDACIÓN IDOR: Verificar que el usuario autenticado es el dueño
    if (!socioDetails.getSocioId().equals(id)) {
        throw new AccesoNoAutorizadoException();
    }
    
    SocioResponseDTO response = obtenerSocioUseCase.ejecutar(id);
    return ResponseEntity.ok(response);
}
```

---

### 5. Falta @PreAuthorize en Endpoints

**Archivo:** `API.md:32-35` / Controlador no implementado  
**Severidad:** ALTA  
**Categoría:** A01:2021 - Broken Access Control

**Descripción:**

La API especifica:

> Roles permitidos:
> - `SOCIO`: Puede acceder y modificar SOLO sus propios beneficiarios
> - `ADMIN`: Puede acceder a beneficiarios de cualquier socio

**Pero no hay anotaciones `@PreAuthorize`** en el código existente ni en la especificación del controlador.

**Corrección:**

```java
@RestController
@RequestMapping("/api/v1/socios/{socioId}/beneficiarios")
@RequiredArgsConstructor
public class BeneficiarioController {

    // Solo SOCIO o ADMIN pueden acceder
    @PreAuthorize("hasRole('SOCIO') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BeneficiarioResponseDTO> crearBeneficiario(...) {
        // ...
    }
    
    // Solo el SOCIO dueño o ADMIN pueden listar
    @PreAuthorize("hasRole('SOCIO') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<BeneficiarioResponseDTO>> listarBeneficiarios(...) {
        // ...
    }
    
    // Solo ADMIN puede acceder a beneficiarios de OTRO socio
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SOCIO') and #socioId == authentication.socioId)")
    @GetMapping("/{id}")
    public ResponseEntity<BeneficiarioResponseDTO> obtenerBeneficiario(...) {
        // ...
    }
}
```

---

### 6. Missing Constraint Única para Documento + SocioId

**Archivo:** `MODELO_DATOS.md:171-173`  
**Severidad:** ALTA  
**Categoría:** A04:2021 - Insecure Design

**Descripción:**

La unique constraint actual es:

```sql
-- MODELO_DATOS.md:171-173
CREATE UNIQUE INDEX idx_beneficiaries_documento_activo 
ON beneficiaries (socio_id, tipo_documento, numero_documento) 
WHERE activo = true;
```

**Problema:** Esta constraint NO previene que un beneficiario tenga el mismo documento que el socio titular.

**Corrección:**

```sql
-- Constraint a nivel de trigger (ver hallazgo CRÍTICO #2)
-- O alternatively, una función de validación
CREATE OR REPLACE FUNCTION validate_documento_unico_por_socio()
RETURNS TRIGGER AS $$
BEGIN
    -- Verificar que no existe beneficiario activo con mismo documento
    IF EXISTS (
        SELECT 1 FROM beneficiaries 
        WHERE socio_id = NEW.socio_id 
          AND tipo_documento = NEW.tipo_documento 
          AND numero_documento = NEW.numero_documento
          AND activo = true
          AND id != COALESCE(NEW.id, OLD.id)
    ) THEN
        RAISE EXCEPTION 'Ya existe beneficiario activo con el mismo documento';
    END IF;
    
    -- Verificar que el documento no sea igual al del socio titular
    IF EXISTS (
        SELECT 1 FROM socios 
        WHERE id = NEW.socio_id 
          AND tipo_documento = NEW.tipo_documento 
          AND numero_documento = NEW.numero_documento
    ) THEN
        RAISE EXCEPTION 'El documento del beneficiario no puede ser igual al del socio';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo | Problema | Acción Requerida |
|-----------|---------|----------|------------------|
| 🔴 CRÍTICA | `MODELO_DATOS.md:315-349` | Trigger de suma de % defectuoso | Corregir lógica a = 100% |
| 🔴 CRÍTICA | `SPEC.md:281-284` | Race condition documento=socio | Mover validación a BD |
| 🟠 ALTA | `SPEC.md:376-392` | Rate limiting por IP | Implementar por socioId |
| 🟠 ALTA | `SocioController.java:63-71` | Falta IDOR | Agregar validación |
| 🟠 ALTA | Controlador no existe | Falta @PreAuthorize | Agregar anotaciones |
| 🟠 ALTA | `MODELO_DATOS.md:171-173` | Constraint insuficiente | Agregar validación |

---

## VERIFICACIONES DE ARQUITECTURA

### Arquitectura Clean Architecture ✅ (especificación)

| Capa | Especificada | Implementada | Estado |
|------|--------------|--------------|--------|
| Domain | Sí | No | ⚠️ No existe código |
| Application (UseCases) | Sí | No | ⚠️ No existe código |
| Infrastructure | Sí | No | ⚠️ No existe código |
| Presentation (Controller) | Sí | Parcial | ⚠️ Solo SocioController |

### Modelo de Datos

| Componente | Estado | Observación |
|------------|--------|-------------|
| Tabla beneficiaries | Especificada | Falta trigger correcto |
| Tabla audit | Especificada | ✅ Correcto diseño |
| Índices | Especificados | ✅ Correctos |
| CHECK constraints | Especificados | ⚠️ Incompletos |
| Soft delete | Especificado | ✅ Implementable |

### Seguridad OWASP Top 10

| Vulnerabilidad | Estado |
|----------------|--------|
| A01 Broken Access Control | ❌ Sin IDOR en controlador |
| A02 Cryptographic Failures | N/A (no aplica) |
| A03 Injection | N/A (JPA parametrizado) |
| A04 Insecure Design | ❌ Trigger % incorrecto |
| A05 Security Misconfiguration | ⚠️ Falta rate limit por socioId |
| A06 Vulnerable Components | N/A (no hay código) |
| A07 Auth & Auth Failures | ⚠️ Falta @PreAuthorize |
| A08 Software Integrity | ⚠️ Falta validación BD |
| A09 Logging & Monitoring | ✅ Especificado |
| A10 SSRF | N/A (no aplica) |

---

## CONCLUSIONES

1. **Módulo NO implementado** - Solo existe documentación de especificación

2. **BUG CRÍTICO en regla de negocio** - El trigger de porcentaje NO garantiza suma = 100%, violando RN-01

3. **Vulnerabilidad CRÍTICA de race condition** - La validación documento=socio es vulnerable a TOCTOU

4. **Rate limiting incorrecto** - Por IP en lugar de por socioId (especificación vs implementación)

5. **Falta validación IDOR** - El controlador existente no verifica propiedad del recurso

6. **Falta @PreAuthorize** - Roles especificados pero no implementados en anotaciones

### Recomendación: **NO PROCEDER** con implementación hasta corregir hallazgos CRÍTICOS

---

*Reporte generado: 2026-04-19T100000*  
*Auditor: Lead Software Architect & Cyber-Security Auditor*
