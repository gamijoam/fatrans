# AUDITORÍA DE SEGURIDAD Y ARQUITECTURA - RE-AUDITORÍA MÓDULO CRÉDITOS

**Proyecto:** Fondo de Ahorro (fondo-ahorro-platform)  
**Módulo:** Créditos (Re-auditoría post-correcciones)  
**Fecha:** 2026-04-15  
**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Stack:** Java 21, Spring Boot 3.2.4, PostgreSQL, Redis, MinIO

---

## RESUMEN EJECUTIVO

| Severidad | Cantidad |
|-----------|----------|
| 🔴 CRÍTICA | 1 |
| 🟠 ALTA | 4 |
| 🟡 MEDIA | 0 |

### Estado General

**Mejoras aplicadas correctamente:**
1. ✅ Typo `/desembolson` → `/desembolso` corregido (CreditoController.java:215)
2. ✅ Bypass colateral corregido (AprobarSolicitudCreditoUseCase.java:57-65)
3. ✅ `tasaInteresOverride` validado con @DecimalMin(0.001) y @DecimalMax(1.0)

**Nuevo hallazgo CRÍTICO descubierto:**
- Conversión UUID → Long defectuosa que permite manipular cuentas de colateral

**Problemas de arquitectura que persisten:**
- Entidades con @Setter público
- Race condition en verificación de crédito activo
- Validación de capacidad de pago no implementada

### Nivel de Riesgo: 🔴 ALTO

**El módulo NO está listo para producción** debido a la vulnerabilidad de conversión UUID→Long.

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. Conversión UUID → Long Permite Manipulación de Colateral

**Archivo:** `creditos/infrastructure/persistence/adapter/CuentaGarantiaRepositoryImpl.java:93-95`  
**Severidad:** CRÍTICA  
**Categoría:** A01:2021 Broken Access Control

**Descripción:**
El método `uuidToLong()` convierte un UUID a Long de manera determinista para buscar cuentas de ahorro:

```java
private Long uuidToLong(UUID uuid) {
    return uuid.getMostSignificantBits() >>> 32;
}
```

**Problema arquitectónico:**
- Módulo Créditos usa `UUID` para `colateralCuentaId` (SolicitudCredito.java:31)
- Módulo Ahorros usa `Long` con `@GeneratedValue(strategy = GenerationType.IDENTITY)` para `CuentaAhorro.id`
- Esta conversión mapea el UUID a un número en el rango [0, ~4.2 mil millones]

**Impacto:**
- Un atacante podría Craftar un UUID cuyo valor convertido apunte a una cuenta de ahorro que NO le pertenece
- Podría usar el saldo de otra persona como colateral para su propio crédito
- Error de tipos puede causar que se retenga saldo de cuentas equivocadas

**Pasos de ataque:**
1. El atacante crea solicitud de crédito indicando `colateralCuentaId`
2. Modifica el UUID para que al convertirlo obtenga el ID de la víctima
3. Al aprobar, el sistema busca la cuenta del atacante pero encuentra la de la víctima
4. El saldo de la víctima es retenido como colateral fraudulentamente

**Corrección:**
```java
// OPCIÓN 1: Cambiar CuentaAhorro para usar UUID como ID
@Entity
public class CuentaAhorroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;  // Cambiar de Long a UUID
    
    @Column(name = "socio_id", nullable = false)
    private UUID socioId;
    // ...
}

// OPCIÓN 2: Crear tabla de mapeo explícita
@Entity
@Table(name = "creditos_cuentas_garantia")
public class CuentaGarantiaEntity {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name = "credito_id", nullable = false)
    private UUID creditoId;
    
    @Column(name = "cuenta_ahorro_id", nullable = false)
    private UUID cuentaAhorroId;  // UUID real, no convertido
}

// Eliminar uuidToLong() completamente
```

---

## VIOLACIONES ALTAS (🟠)

### 2. Entidades de Dominio Mutables (PERSISTE)

**Archivos:**
- `domain/model/SolicitudCredito.java:18` - `@Setter` público
- `domain/model/Amortizacion.java`
- `domain/model/PlanAmortizacion.java`
- `domain/model/EvaluacionCrediticia.java`

**Severidad:** ALTA  
**Categoría:** Arquitectura Limpia

**Descripción:**
Las entidades usan `@Setter` de Lombok, permitiendo mutación directa de estado fuera de los métodos de negocio.

**Impacto:**
- Estado puede modificarse sin controles
- Violación de transiciones de estado en DDD
- Dificulta auditoría y trazabilidad

**Corrección:**
```java
// Reemplazar @Setter con setters package-private
@Getter
public class SolicitudCredito {
    private UUID id;
    private String numeroSolicitud;
    // ...
    
    // Solo para JPA, no para uso externo
    void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
}
```

---

### 3. Race Condition en Verificación de Crédito Activo (PERSISTE)

**Archivo:** `application/usecase/CrearSolicitudCreditoUseCase.java:41`  
**Severidad:** ALTA  
**Categoría:** A04:2021 Insecure Design

**Descripción:**
`existeCreditoActivoPorSocio()` se verifica sin lock ni transacción atómica. Dos solicitudes concurrentes del mismo socio podrían pasar la validación simultáneamente.

**Impacto:**
- Un socio podría obtener dos créditos activos
- Violación de regla de negocio

**Corrección:**
```java
@Transactional
public SolicitudCreditoResponse ejecutar(...) {
    // Usar SELECT FOR UPDATE
    if (solicitudRepository.existeCreditoActivoPorSocioWithLock(socioId)) {
        throw new CreditoActivoExistenteException(socioId);
    }
    // ...
}
```

O agregar constraint en BD:
```sql
CREATE UNIQUE INDEX idx_solicitud_activa_socio 
ON solicitudes_credito(socio_id) WHERE estado = 'DESEMBOLSADO';
```

---

### 4. Validación de Capacidad de Pago No Implementada (PERSISTE)

**Archivo:** `application/usecase/EvaluarSolicitudUseCase.java`  
**Severidad:** ALTA  
**Categoría:** A04:2021 Insecure Design

**Descripción:**
El request recibe `salarioEstimado` (líneas 46, 53) pero **nunca lo usa** para validar que la cuota no exceda un porcentaje razonable.

**Impacto:**
- Pueden aprobarse créditos que el socio no puede pagar
- Alta probabilidad de default

**Corrección:**
```java
BigDecimal cuotaEstimada = PlanAmortizacion.calcularCuotaFrances(
    solicitud.getMontoSolicitado(),
    tipoCredito.getTasaInteresAnual(),
    solicitud.getPlazoMeses()
);

BigDecimal porcentajeComprometido = cuotaEstimada.divide(
    salarioEstimado, 4, RoundingMode.HALF_UP);

if (porcentajeComprometido.compareTo(new BigDecimal("0.30")) > 0) {
    throw new CapacidadPagoInsuficienteException(
        "Cuota compromete " + porcentajeComprometido.multiply(new BigDecimal("100")) + "% del salario");
}
```

---

### 5. Rate Limit con Memory Store (PERSISTE)

**Archivo:** `infrastructure/security/SimulacionRateLimitFilter.java:35`  
**Severidad:** ALTA  
**Categoría:** A05:2021 Security Misconfiguration

**Descripción:**
`Map<String, Bucket> BUCKETS` es un `ConcurrentHashMap` estático en memoria.

**Impacto:**
- Se reinicia con cada despliegue
- No funciona en entornos con múltiples instancias
- Potential memory leak

**Corrección:**
Usar Redis (ya disponible en docker-compose):
```java
private final RedisBucket4jProxy redisProxy;  // Bucket4j con Redis backend
```

---

## MEJORAS RECOMENDADAS (🟡)

Ninguna ( menos de 3 hallazgos en CRÍTICA + ALTA combinados)

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo | Problema | Estado |
|-----------|---------|----------|--------|
| 🔴 CRÍTICA | `CuentaGarantiaRepositoryImpl.java:93-95` | uuidToLong() defectuoso | **NUEVO** |
| 🟠 ALTA | `SolicitudCredito.java:18` | @Setter público | PERSISTE |
| 🟠 ALTA | `CrearSolicitudCreditoUseCase.java:41` | Race condition | PERSISTE |
| 🟠 ALTA | `EvaluarSolicitudUseCase.java` | Sin validación salario | PERSISTE |
| 🟠 ALTA | `SimulacionRateLimitFilter.java:35` | Memory store | PERSISTE |

---

## RESPUESTAS A PREGUNTAS

### 1. ¿Cómo evalúas la salud del proyecto AHORA después de las correcciones?

**Mejora significativa** en el flujo de Créditos:
- El typo del endpoint de desembolso está corregido
- El bypass de colateral está corregido  
- La tasa de interés override está validada

**Sin embargo**, se descubrió una vulnerabilidad de seguridad nueva y crítica en la conversión UUID→Long que afecta la integridad del colateral entre módulos.

### 2. ¿Qué riesgos CRÍTICOS permanecen?

1. **Manipulación de cuentas de colateral** - La conversión uuidToLong() permite a un atacante usar el saldo de otra persona como su colateral
2. Error de tipo puede causar que se retenga saldo de cuentas equivocadas

### 3. ¿Qué módulo recomiendas auditar o trabajar a continuación?

**Prioridad 1: Módulo AHORROS**
- Verificar consistencia de IDs (Long vs UUID)
- Auditar la tabla `cuentas_ahorro` y sus relaciones

**Prioridad 2: Módulo KYC**  
- Verificar el renombrado de `findByVerificacionIdAndTipo` → `findByVerificacionIdAndTipoDocumento`

**Prioridad 3: Módulo AUTH**
- El JWT usa UUID como subject correctamente
- Pero el JWT_SECRET por defecto en docker-compose es débil para producción

### 4. ¿Hay vulnerabilidades que se arrastran de un módulo a otro?

**SÍ - Problema de UUID/Long entre Créditos y Ahorros:**

```
Créditos                           Ahorros
┌─────────────────┐               ┌─────────────────┐
│ colateralCuentaId│              │ CuentaAhorro.id │
│     UUID        │──uuidToLong()│     Long        │
│                 │               │ (IDENTITY)      │
└─────────────────┘               └─────────────────┘
```

**El uuidToLong() crea un puente de datos inseguro entre módulos.**

### 5. Recomendación de producción

**NO LISTO PARA PRODUCCIÓN** hasta que:
1. Se corrija la vulnerabilidad de conversión UUID→Long
2. Se implementen las 4 correcciones de arquitectura que persisten

---

## OBSERVACIONES ADICIONALES

### JWT Secret en docker-compose
```yaml
JWT_SECRET: ${JWT_SECRET:-Desarrollo2026SecretoBackendMinimo256Bits}
```
Este valor por defecto es visible en el repositorio. En producción debe ser generado dinámicamente y nunca hardcodeado.

### Docker Compose funcional
El ambiente de desarrollo incluye PostgreSQL, Redis y MinIO correctamente configurados.

---

*Reporte generado: 2026-04-15*  
*Auditor: Lead Software Architect & Cyber-Security Auditor*
