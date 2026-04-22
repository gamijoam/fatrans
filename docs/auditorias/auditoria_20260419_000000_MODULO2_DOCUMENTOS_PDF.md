# AUDITORÍA DE SEGURIDAD FIN-TECH
## Módulo 2: Documentos PDF

**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Fecha:** 2026-04-19  
**Proyecto:** Fondo de Ahorro Platform  
**Módulo Auditado:** Documentos PDF (`feature/modulo-documentospdf`)  
**Estándares:** OWASP Top 10, SUDEBAN, LOPDP, Clean Architecture, DDD

---

## RESUMEN EJECUTIVO

| Categoría | Críticos | Altos | Medios | Bajos |
|-----------|----------|-------|--------|-------|
| Seguridad | 4 | 4 | 2 | 1 |
| Arquitectura | 0 | 1 | 1 | 0 |
| **TOTAL** | **4** | **5** | **3** | **1** |

**Estado General:** ❌ NO APROBADO - Módulo presenta vulnerabilidades críticas que impiden su aprobación para producción

---

## VIOLACIONES CRÍTICAS (🔴)

---

### 1. CLAMAV MALWARE SCANNER NO IMPLEMENTADO - SIMULACIÓN SOLO

- **[CRITICIDAD]**: 🔴 CRÍTICA
- **[CATEGORÍA]**: Seguridad
- **[DESCRIPCIÓN]**: El servicio `ClamAVMalwareScannerService` está completamente SIMULADO y NO se conecta al servicio real de ClamAV. El código siempre retorna `ScanResult(true, null, "Escaneo simulado")` lo que significa que NINGÚN PDF es realmente escaneado.
- **[IMPACTO]**: Vulnerabilidad CRÍTICA - Archivos PDF potencialmente maliciosos (malware, troyanos, scripts embebidos) serían almacenados en MinIO sin detección. Esto viola OWASP A01:2021 (Broken Access Control) y podría comprometer todo el sistema bancario.
- **[LOCATION]**: `documentospdf/infrastructure/storage/ClamAVMalwareScannerService.java:52-57`
- **[RECOMENDACIÓN]**:
```java
// IMPLEMENTACIÓN REAL REQUERIDA
@Override
public ScanResult scan(byte[] data) {
    if (!enabled) {
        log.warn("ClamAV deshabilitado, NO se debería permitir uploads sin escaneo");
        throw new IllegalStateException("ClamAV deshabilitado - NO se permite upload");
    }
    
    // Implementar conexión real a ClamAV:
    // 1. Conectar a socket TCP de ClamAV (localhost:3310)
    // 2. Enviar comando INSTREAM
    // 3. Recibir y parsear respuesta
    // 4. Retornar resultado real
    
    try (Socket socket = new Socket("localhost", 3310);
         OutputStream out = socket.getOutputStream();
         InputStream in = socket.getInputStream()) {
        
        // Enviar datos para escaneo
        out.write("zINSTREAM\0".getBytes());
        // Enviar archivo en chunks
        // ...
        
        // Leer respuesta
        // Retornar ScanResult con threatName si encuentra malware
    }
}
```

---

### 2. FIRMA DIGITAL RSA SIMULADA - NO ES PRODUCCIÓN

- **[CRITICIDAD]**: 🔴 CRÍTICA
- **[CATEGORÍA]**: Seguridad
- **[DESCRIPCIÓN]**: Cuando `documentospdf.firma-digital.clave-privada` no está configurada, el sistema usa una "firma simulada" que simplemente codifica los bytes del PDF en Base64. Esto NO es criptografía RSA SHA-256.
- **[IMPACTO]**: Documentos legales críticos (CONTRATO_ADHESION, PAGARE) serían generados sin firma digital válida. En caso de disputa legal, estos documentos NO tendrían valor probatorio. Violación directa del requisito CS-001.
- **[LOCATION]**:
  - `documentospdf/application/usecase/GenerarContratoAdhesionUseCase.java:162-164`
  - `documentospdf/application/usecase/GenerarPagareUseCase.java:159-161`
- **[RECOMENDACIÓN]**:
```java
private String firmarPdf(byte[] pdfBytes) {
    // NUNCA permitir firma simulada en producción
    if (clavePrivadaBase64 == null || clavePrivadaBase64.isEmpty()) {
        throw new FirmaDigitalException(
            "Firma digital no configurada - NO se puede generar contrato/pagare"
        );
    }
    
    // Usar keystore externo (HSM/Key Vault en producción)
    // Nunca hardcodear claves privadas
}
```

---

### 3. BENIFICIARIO_QUERY_PORT ES PLACEHOLDER - GENERACIÓN BLOQUEADA

- **[CRITICIDAD]**: 🔴 CRÍTICA
- **[CATEGORÍA]**: Arquitectura
- **[DESCRIPCIÓN]**: `BeneficiarioQueryPortAdapter` siempre retorna `Collections.emptyList()` con un log de warning. Esto hace que `GenerarCartaBeneficiariosUseCase` siempre falle en el paso 4 con `TipoDocumentoInvalidoException`.
- **[IMPACTO]**: La funcionalidad de Carta de Beneficiarios está COMPLETAMENTE ROTA. Los beneficiarios nunca podrán generar este documento.
- **[LOCATION]**: `beneficiarios/infrastructure/adapter/BeneficiarioQueryPortAdapter.java:25-26`
- **[RECOMENDACIÓN]**: Implementar el adaptador correctamente consultando la tabla `beneficiaries` cuando el módulo Beneficiarios esté completo.

---

### 4. SOCIO_QUERY_PORT.OBTENER_SOCIO_ID_POR_CUENTA() LANZA EXCEPCIÓN

- **[CRITICIDAD]**: 🔴 CRÍTICA
- **[CATEGORÍA]**: Arquitectura
- **[DESCRIPCIÓN]**: El método `obtenerSocioIdPorCuenta()` en `SocioQueryPortAdapter` lanza `UnsupportedOperationException`. Este método es necesario para verificar la relación cuenta-socio en validaciones IDOR.
- **[IMPACTO]**: La generación de Estado de Cuenta fallará cuando se intente verificar que el socio tiene acceso a la cuenta. Aunque el código en `GenerarEstadoCuentaUseCase` obtiene el socioId directamente de `cuentaQueryPort.obtenerDatosCuenta()`, la arquitectura está incompleta.
- **[LOCATION]**: `socios/infrastructure/adapter/SocioQueryPortAdapter.java:28-29`
- **[RECOMENDACIÓN]**: Implementar la relación cuenta-socio desde el módulo Ahorros o crear un método en `CuentaQueryPort` que retorne el socioId directamente.

---

## VIOLACIONES ALTAS (🟠)

---

### 5. RATE LIMITING CON LÓGICA INCORRECTA

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad
- **[DESCRIPCIÓN]**: La lógica de determinación de "download" es incorrecta: `isDownload = path.contains("/descargar") || "GET".equals(method)`. Todos los endpoints del controller usan GET, por lo que TODOS serían clasificados como "download" y nunca se aplicaría el rate limit de generación (5 req/min).
- **[IMPACTO]**: El rate limiting de generación (5 req/min por usuario) NO se aplica correctamente. Un atacante podría generar documentos ilimitados.
- **[LOCATION]**: `documentospdf/infrastructure/security/RateLimitDocumentosFilter.java:69`
- **[RECOMENDACIÓN]**:
```java
// Corregir la lógica
boolean isDownload = path.contains("/descargar");
boolean isGeneracion = path.matches(".*/(estado-cuenta|constancia-afiliacion|contrato|pagare|tabla-amortizacion|carta-beneficiarios)/.+");

if (isDownload) {
    // Rate limit descarga: 10 req/min
} else if (isGeneracion) {
    // Rate limit generación: 5 req/min usuario, 20 req/min IP
}
```

---

### 6. WATERMARK DÉBIL - USA HASHCODE Y RANDOM PREDECIBLE

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad
- **[DESCRIPCIÓN]**: El watermark usa `datos.hashCode()` (no es SHA-256 real del documento) y `Math.random()` (no criptográficamente seguro) para el número de copia.
- **[IMPACTO]**: El watermark no garantiza trazabilidad real del documento. Un atacante podría generar documentos con el mismo "hash" modificando los datos en memoria.
- **[LOCATION]**: `documentospdf/infrastructure/pdf/OpenPdfGeneratorService.java:370-387`
- **[RECOMENDACIÓN]**:
```java
private void addWatermark(Document document, String clasificacion, Map<String, Object> datos, byte[] pdfBytes) {
    // Usar SHA-256 real del PDF generado
    String hashReal = calcularSha256(pdfBytes).substring(0, 16);
    
    // Usar SecureRandom para número de copia
    SecureRandom sr = new SecureRandom();
    int numeroCopia = sr.nextInt(999999);
    
    String watermarkText = String.format(
        "%s | Generado: %s | Hash: SHA-256:%s | Copia: %06d",
        clasificacion,
        LocalDateTime.now().format(DateTimeFormatter...),
        hashReal,
        numeroCopia
    );
}
```

---

### 7. SPOOFING DE IP EN RATE LIMITING

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad
- **[DESCRIPCIÓN]**: El filtro obtiene la IP del cliente desde headers `X-Forwarded-For` y `X-Real-IP` sin validación. Un atacante podría enviar headers falsificados para evadir rate limiting por IP.
- **[IMPACTO]**: Rate limiting por IP puede ser completamente evitado mediante IP spoofing.
- **[LOCATION]**: `documentospdf/infrastructure/security/RateLimitDocumentosFilter.java:138-148`
- **[RECOMENDACIÓN]**:
```java
private String getClientIp(HttpServletRequest request) {
    // Validar que X-Forwarded-For viene de un proxy de confianza
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && isTrustedProxy(request)) {
        return xForwardedFor.split(",")[0].trim();
    }
    // Solo usar X-Real-IP si viene de proxy confiable
    // En producción, siempre usar request.getRemoteAddr() como fallback
}

private boolean isTrustedProxy(HttpServletRequest request) {
    String remoteAddr = request.getRemoteAddr();
    // Verificar que la IP de origen es un proxy conocido
    return trustedProxies.contains(remoteAddr);
}
```

---

### 8. CREDITO_QUERY_PORT RETORNA NULL SIN MANEJO

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad
- **[DESCRIPCIÓN]**: `CreditoQueryPortAdapter.obtenerSocioIdPorCredito()` retorna `null` si no encuentra el crédito. Los UseCases que lo usan (`GenerarTablaAmortizacionUseCase`, `GenerarPagareUseCase`) no validan este null antes de usar `socioId` para IDOR.
- **[IMPACTO]**: NullPointerException potencial o bypass de validación IDOR si `socioId` es null.
- **[LOCATION]**: `creditos/infrastructure/adapter/CreditoQueryPortAdapter.java:73-75`
- **[RECOMENDACIÓN]**:
```java
@Override
public UUID obtenerSocioIdPorCredito(UUID creditoId) {
    return solicitudCreditoJpaRepository.findById(creditoId)
        .map(SolicitudCreditoEntity::getSocioId)
        .orElseThrow(() -> new DocumentoNoEncontradoException(
            "Crédito no encontrado: " + creditoId));
}
```

---

### 9. DEPENDENCIA CIRCULAR POTENCIAL - DOMINIO DEPENDE DE INFRAESTRUCTURA

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Arquitectura
- **[DESCRIPCIÓN]**: Los UseCases (capa `application`) dependen de adaptadores de otros módulos (ej: `CreditoQueryPortAdapter` que está en `creditos/infrastructure/adapter`). Esto crea acoplamiento indirecto que podría causar dependencias circulares en builds.
- **[IMPACTO]**: Acoplamiento frágil entre módulos. Si el módulo Créditos cambia su adaptador, podría romper Documentos PDF.
- **[LOCATION]**: Múltiples UseCases en `documentospdf/application/usecase/`
- **[RECOMENDACIÓN]**: Todos los QueryPorts deben estar en `core/port/` y las implementaciones en adapters separados. Considerar usar SPI (Service Provider Interface) de Java para cargar adaptadores dinámicamente.

---

## VIOLACIONES MEDIAS (🟡)

---

### 10. CÓDIGO DUPLICADO - SHA-256 CALCULADO 6 VECES

- **[CRITICIDAD]**: 🟡 MEDIA
- **[CATEGORÍA]**: Mantenibilidad
- **[DESCRIPCIÓN]**: El método `calcularHashSha256()` está duplicado en 6 UseCases diferentes: `GenerarContratoAdhesionUseCase`, `GenerarPagareUseCase`, `GenerarEstadoCuentaUseCase`, `GenerarTablaAmortizacionUseCase`, `GenerarCartaBeneficiariosUseCase`, y también en `PdfSecurityValidator`.
- **[IMPACTO]**: Violación DRY. Si el algoritmo SHA-256 necesita cambiar, debe modificarse en 6 lugares.
- **[LOCATION]**: Múltiples archivos en `documentospdf/application/usecase/`
- **[RECOMENDACIÓN]**: Crear una clase utilitaria `PdfHashUtil` en `domain/util/` con el método estático.

---

### 11. NULLEDAD EN DTO BUILDER

- **[CRITICIDAD]**: 🟡 MEDIA
- **[CATEGORÍA]**: Mantenibilidad
- **[DESCRIPCIÓN]**: Los builders en DTOs (`DocumentoResponseDTO.Builder`) no validan que campos requeridos no sean null. Podrían crearse DTOs con estado inválido.
- **[IMPACTO]**: DTOs podrían ser transmitidos con campos null al cliente, causando NullPointerExceptions en frontend.
- **[LOCATION]**: `documentospdf/application/dto/DocumentoResponseDTO.java:66-72`
- **[RECOMENDACIÓN]**: Usar `@NotNull` en el Builder o migrar a Records con validación en constructor.

---

### 12. FALTAN PRUEBAS UNITARIAS

- **[CRITICIDAD]**: 🟡 MEDIA
- **[CATEGORÍA]**: Calidad
- **[DESCRIPCIÓN]**: No se encontraron archivos de pruebas para el módulo Documentos PDF.
- **[IMPACTO]**: Sin tests, no hay garantía de que la funcionalidad IDOR, rate limiting, y firma digital funcione correctamente.
- **[LOCATION]**: Sin ubicación (no existen tests)
- **[RECOMENDACIÓN]**: Crear tests unitarios con Mockito para:
  - Validación IDOR en todos los UseCases
  - Rate limiting filter
  - Firma digital (happy path y exceptions)
  - Escaneo malware

---

## MEJORAS BAJAS (⚪)

---

### 13. IMPORTACIÓN SIN USO

- **[CRITICIDAD]**: ⚪ BAJA
- **[CATEGORÍA]**: Estilo
- **[DESCRIPCIÓN]**: `DocumentoController.java:28` importa `fromString` de UUID estático pero no lo usa directamente.
- **[LOCATION]**: `documentospdf/infrastructure/presentation/controller/DocumentoController.java:28`
- **[RECOMENDACIÓN]**: Eliminar import no utilizado.

---

## ARCHIVOS AFECTADOS

| Criticidad | Archivos |
|------------|----------|
| 🔴 CRÍTICA | `ClamAVMalwareScannerService.java`, `GenerarContratoAdhesionUseCase.java`, `GenerarPagareUseCase.java`, `BeneficiarioQueryPortAdapter.java`, `SocioQueryPortAdapter.java` |
| 🟠 ALTA | `RateLimitDocumentosFilter.java`, `OpenPdfGeneratorService.java`, `CreditoQueryPortAdapter.java`, múltiples UseCases |
| 🟡 MEDIA | Todos los UseCases (código duplicado), `DocumentoResponseDTO.java` |
| ⚪ BAJA | `DocumentoController.java` |

---

## VEREDICTO

| Aspecto | Estado | Comentario |
|---------|--------|------------|
| IDOR | ⚠️ PARCIAL | Validaciones IDOR implementadas en UseCases, pero fallan con adaptadores no implementados |
| Rate Limiting | ⚠️ DEFECTUOSO | Lógica incorrecta - todos los endpoints se clasifican como "download" |
| Firma Digital | ❌ NO FUNCIONAL | Implementación simulada, no sirve para producción |
| Watermark | ⚠️ DÉBIL | Usa hashCode() y Random, no es criptográficamente seguro |
| Pre-signed URLs | ✅ CORRECTO | Implementación correcta con MinIO |
| Escaneo ClamAV | ❌ SIMULADO | No hay implementación real, solo simulación |
| Auditoría | ✅ CORRECTO | Logging adecuado en todos los UseCases |
| Input Validation | ⚠️ PARCIAL | DTOs sin validación, algunos adapters retornan null |
| Arquitectura | ⚠️ MEJORABLE | Clean Architecture respetado pero con acoplamiento entre módulos |

---

## VEREDICTO FINAL: ❌ NO APROBADO

**Razones para rechazo:**

1. **CLAMAV NO IMPLEMENTADO** - El módulo almacenar malware sin detección
2. **FIRMA DIGITAL SIMULADA** - Contratos y pagarés no tendrían valor legal
3. **PLACEHOLDER EN BENEFICIARIOS** - Funcionalidad completamente rota
4. **RATE LIMITING DEFECTUOSO** - Puede ser evadido fácilmente
5. **DEPENDENCIAS NO RESUELTAS** - Adaptadores de otros módulos lanzan excepciones

**Recomendación:** NO MERGEAR a producción hasta que todas las violaciones CRÍTICAS y ALTAS sean corregidas. Re-auditar después de las correcciones.

---

**Firma del Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Próxima Auditoría Programada:** Después de correcciones CRÍTICAS
