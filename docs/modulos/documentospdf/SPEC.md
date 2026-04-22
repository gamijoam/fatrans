# Módulo DOCUMENTOS PDF - Especificación Técnica

**Proyecto:** Plataforma Fondo de Ahorro
**Versión:** 1.0
**Fecha:** 2026-04-19
**Estado:** Por implementar
**Complejidad:** Alta

---

## Resumen

El módulo **Documentos PDF** gestiona la generación, almacenamiento y descarga de documentos legales y financieros en formato PDF para los socios del Fondo de Ahorro. El módulo garantiza la integridad documental mediante firma digital RSA SHA-256, marca de agua robusta y almacenamiento seguro en MinIO con pre-signed URLs.

**Nota:** Este módulo unifica la generación de documentos que anteriormente podrían estar duplicados en otros módulos (ej: Carta de Beneficiarios).

---

## 1. Objetivos del Módulo

### 1.1 Objetivo Principal
Generar documentos PDF oficiales del Fondo de Ahorro con validez legal, almacenamiento seguro y auditoría completa de todas las operaciones.

### 1.2 Objetivos Secundarios
- Garantizar integridad documental mediante firma digital RSA SHA-256
- Proteger documentos sensibles con marca de agua (watermark) robusta
- Almacenar documentos en MinIO con acceso seguro via pre-signed URLs
- Escanear documentos con ClamAV antes de almacenamiento
- Mantener auditoría inmutable de generaciones y descargas
- Cumplir requisitos regulatorios SUDEBAN y LOPDP

### 1.3 Scope
- ✅ Generación de 6 tipos de documentos PDF
- ✅ Firma digital RSA SHA-256 (para CONTRATO y PAGARÉ)
- ✅ Marca de agua con hash, clasificación y tipo de copia
- ✅ Almacenamiento en MinIO con buckets segregados
- ✅ Descarga segura via pre-signed URLs (TTL 15 min)
- ✅ Escaneo ClamAV obligatorio
- ✅ Auditoría de generaciones y descargas
- ✅ Rate limiting por usuario e IP
- ✅ Validación IDOR completa
- ❌ Firma digital avanzada (para Fase 3)

### 1.4 Fuera del Scope
- ❌ Generación de reportes Excel/CSV (módulo Reportes)
- ❌ Notificaciones por email (módulo Notificaciones)
- ❌ Biométricos (módulo Biometría)

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura General (Clean Architecture)

```
backend/src/main/java/com/tufondo/documentospdf/
├── domain/                              # Capa de Dominio (puro)
│   ├── model/
│   │   ├── Documento.java              # Entidad principal
│   │   └── enums/
│   │       ├── TipoDocumento.java       # Tipos de documento
│   │       └── EstadoDocumento.java     # Estados del documento
│   ├── repository/
│   │   └── DocumentoRepository.java
│   └── exception/
│       ├── DocumentoNoEncontradoException.java
│       ├── GeneracionPDFException.java
│       └── TipoDocumentoInvalidoException.java
│
├── application/                        # Capa de Aplicación
│   ├── usecase/
│   │   ├── GenerarEstadoCuentaUseCase.java
│   │   ├── GenerarConstanciaAfiliacionUseCase.java
│   │   ├── GenerarContratoAdhesionUseCase.java
│   │   ├── GenerarPagareUseCase.java
│   │   ├── GenerarTablaAmortizacionUseCase.java
│   │   ├── GenerarCartaBeneficiariosUseCase.java
│   │   └── DescargarDocumentoUseCase.java
│   ├── dto/
│   │   ├── DocumentoResponseDTO.java
│   │   └── DescargarDocumentoResponseDTO.java
│   └── port/
│       ├── PdfGeneratorPort.java
│       ├── StoragePort.java
│       └── MalwareScannerPort.java
│
└── infrastructure/                    # Capa de Infraestructura
    ├── presentation/
    │   ├── controller/
    │   │   └── DocumentoController.java
    │   └── exception/
    │       └── DocumentosExceptionHandler.java
    ├── persistence/
    │   ├── entity/
    │   │   ├── DocumentoEntity.java
    │   │   └── DocumentoAuditEntity.java
    │   ├── jpa/
    │   │   ├── DocumentoJpaRepository.java
    │   │   └── DocumentoAuditJpaRepository.java
    │   └── adapter/
    │       └── DocumentoRepositoryImpl.java
    ├── pdf/
    │   ├── OpenPdfGeneratorService.java  # Implementación OpenPDF
    │   └── templates/                    # Plantillas PDF
    ├── storage/
    │   └── MinIODocumentStorageService.java
    └── security/
        ├── RateLimitDocumentosFilter.java
        └── PdfSecurityValidator.java
```

---

## 3. Modelo de Dominio

### 3.1 Documento - Entidad Principal

```java
public final class Documento {
    private final UUID id;
    private final UUID socioId;
    private final TipoDocumento tipo;
    private final EstadoDocumento estado;
    private final String nombreArchivo;
    private final String rutaAlmacenamiento;  // MinIO path
    private final String hashArchivo;         // SHA-256
    private final String firmaDigital;        // RSA SHA-256 (para contratos/pagares)
    private final Long tamanoBytes;
    private final LocalDateTime fechaGeneracion;
    private final LocalDateTime fechaExpiracion;
    private final String generadoPor;         // USER_ID o SYSTEM
    private final String clasificacion;        // CONFIDENCIAL, RESTRINGIDO, PUBLICO
}
```

**Métodos de fábrica:**
- `Documento.generar(...)` - Crea documento con validaciones
- `Documento.conExpiracion(...)` - Establece fecha de expiración

**Relaciones:**
- Relación N:1 con `Socio`
- Relación 1:N con `DocumentoAudit`

---

### 3.2 Enumeraciones

#### TipoDocumento
```java
public enum TipoDocumento {
    ESTADO_CUENTA,        // Estado de cuenta mensual
    CONSTANCIA_AFILIACION, // Constancia de afiliación
    CONTRATO_ADHESION,    // Contrato de adhesión (requiere firma digital)
    PAGARE,               // Pagaré de crédito (requiere firma digital)
    TABLA_AMORTIZACION,   // Tabla de amortización
    CARTA_BENEFICIARIOS   // Carta de designación de beneficiarios
}
```

#### EstadoDocumento
```java
public enum EstadoDocumento {
    GENERADO,     // PDF creado, pendiente de escaneo
    ALMACENADO,   // PDF escaneado y almacenado en MinIO
    EXPIRADO,     // PDF fuera de vigencia
    REVOCADO      // PDF revocado manualmente por ADMIN
}
```

#### ClasificacionDocumento
```java
public enum ClasificacionDocumento {
    CONFIDENCIAL,   // Datos financieros sensibles
    RESTRINGIDO,    // Contratos y pagarés
    PUBLICO         // Constancias de afiliación
}
```

---

## 4. Casos de Uso (Application Layer)

### 4.1 GenerarEstadoCuentaUseCase

Genera estado de cuenta mensual en PDF.

```java
@Component
@RequiredArgsConstructor
public class GenerarEstadoCuentaUseCase {
    public DocumentoResponseDTO ejecutar(UUID cuentaId, UUID socioIdToken, boolean isAdmin);
}
```

**Flujo:**
1. Validar JWT y extraer socioId
2. Validar IDOR: socioToken == socioId de la cuenta (o es ADMIN)
3. Obtener datos de cuenta y movimientos del periodo
4. Generar PDF con OpenPDF
5. Aplicar watermark robusto
6. Calcular hash SHA-256
7. Escanear con ClamAV
8. Subir a MinIO (bucket segregado)
9. Registrar en DocumentoRepository
10. Registrar auditoría (GENERAR)
11. Retornar response con documentId y pre-signed URL

---

### 4.2 GenerarConstanciaAfiliacionUseCase

Genera constancia de afiliación.

```java
@Component
@RequiredArgsConstructor
public class GenerarConstanciaAfiliacionUseCase {
    public DocumentoResponseDTO ejecutar(UUID socioId);
}
```

**Flujo:**
1. Validar JWT y extraer socioId
2. Validar IDOR: socioToken == socioId (o es ADMIN)
3. Obtener datos del socio
4. Generar PDF
5. Aplicar watermark
6. Calcular hash SHA-256
7. Escanear con ClamAV
8. Subir a MinIO
9. Registrar en repositorio
10. Registrar auditoría
11. Retornar response

---

### 4.3 GenerarContratoAdhesionUseCase

Genera contrato de adhesión con firma digital RSA SHA-256.

```java
@Component
@RequiredArgsConstructor
public class GenerarContratoAdhesionUseCase {
    public DocumentoResponseDTO ejecutar(UUID solicitudId);
}
```

**Flujo:**
1. Validar JWT y rol ADMIN o SISTEMA
2. Obtener datos de solicitud y socio
3. Generar PDF
4. Aplicar watermark robusto
5. **Escanear con ClamAV** (seguridad CS-004: antes de firmar)
6. Calcular hash SHA-256
7. **FIRMAR digitalmente con RSA SHA-256**
8. Subir a MinIO (bucket contratos)
9. Registrar con estado RESTRINGIDO
10. Registrar auditoría
11. Retornar response

**Nota de seguridad (CS-004):** El escaneo se realiza ANTES de la firma para evitar firmar contenido malicioso.

---

### 4.4 GenerarPagareUseCase

Genera pagaré de crédito con firma digital RSA SHA-256.

```java
@Component
@RequiredArgsConstructor
public class GenerarPagareUseCase {
    public DocumentoResponseDTO ejecutar(UUID creditoId);
}
```

**Flujo:**
1. Validar JWT y rol ADMIN o SISTEMA
2. Obtener datos del crédito y plan de amortización
3. Generar PDF
4. Aplicar watermark robusto
5. **Escanear con ClamAV** (seguridad CS-004: antes de firmar)
6. Calcular hash SHA-256
7. **FIRMAR digitalmente con RSA SHA-256**
8. Subir a MinIO (bucket pagarés)
9. Registrar con estado RESTRINGIDO
10. Registrar auditoría
11. Retornar response

**Nota de seguridad (CS-004):** El escaneo se realiza ANTES de la firma para evitar firmar contenido malicioso.

---

### 4.5 GenerarTablaAmortizacionUseCase

Genera tabla de amortización del crédito.

```java
@Component
@RequiredArgsConstructor
public class GenerarTablaAmortizacionUseCase {
    public DocumentoResponseDTO ejecutar(UUID creditoId, UUID socioIdToken, boolean isAdmin);
}
```

**Flujo:**
1. Validar JWT y extraer socioId
2. Validar IDOR: socioToken == socioId del crédito (o es ADMIN)
3. Obtener plan de amortización
4. Generar PDF
5. Aplicar watermark
6. Calcular hash SHA-256
7. Escanear con ClamAV
8. Subir a MinIO
9. Registrar auditoría
10. Retornar response

---

### 4.6 GenerarCartaBeneficiariosUseCase

Genera carta de beneficiarios (unificado desde módulo Beneficiarios).

```java
@Component
@RequiredArgsConstructor
public class GenerarCartaBeneficiariosUseCase {
    public DocumentoResponseDTO ejecutar(UUID socioId);
}
```

**Flujo:**
1. Validar JWT y extraer socioId
2. Validar IDOR: socioToken == socioId (o es ADMIN)
3. Obtener lista de beneficiarios activos
4. Validar que suma porcentajes = 100%
5. Generar PDF
6. Aplicar watermark
7. Calcular hash SHA-256
8. Escanear con ClamAV
9. Subir a MinIO
10. Registrar auditoría
11. Retornar response

**Nota:** Este use case es invocado por el módulo Beneficiarios para evitar duplicación.

---

### 4.7 DescargarDocumentoUseCase

Obtiene pre-signed URL para descargar documento.

```java
@Component
@RequiredArgsConstructor
public class DescargarDocumentoUseCase {
    public DescargarDocumentoResponseDTO ejecutar(UUID documentoId, UUID socioIdToken, boolean isAdmin);
}
```

**Flujo:**
1. Validar JWT y extraer socioId
2. Obtener documento por ID
3. Validar estado documento (no EXPIRADO ni REVOCADO)
4. Validar IDOR: socioToken == socioId del documento (o es ADMIN)
5. Generar pre-signed URL (TTL 15 minutos)
6. Registrar auditoría (DESCARGA)
7. Retornar URL

---

## 5. Estados y Transiciones

### 5.1 Ciclo de Vida del Documento

```
┌──────────────┐
│   GENERADO   │◄─────────────────────────┐
└──────┬───────┘                          │
       │ (escaneo ClamAV OK)              │
       ▼                                  │
┌──────────────┐                          │
│  ALMACENADO  │──────────────────────────┤
└──────┬───────┘                          │
       │                                  │
       ├──────────────────┬───────────────┤
       │ (expiración)     │ (revocación)  │
       ▼                  ▼               │
┌──────────────┐    ┌──────────────┐       │
│   EXPIRADO   │    │   REVOCADO   │───────┘
└──────────────┘    └──────────────┘
```

### 5.2 Estados

| Estado | Descripción | Puede descargar? |
|--------|-------------|------------------|
| GENERADO | PDF creado, en proceso de escaneo | No |
| ALMACENADO | PDF escaneado y en MinIO | Sí (con pre-signed URL) |
| EXPIRADO | PDF fuera de vigencia | No (generar nuevo) |
| REVOCADO | Revocado por ADMIN | No |

---

## 6. Excepciones

| Excepción | HTTP Status | Código | Descripción |
|-----------|-------------|--------|-------------|
| `DocumentoNoEncontradoException` | 404 | DOC_001 | Documento no existe |
| `DocumentoExpiradoException` | 410 | DOC_002 | Documento ha expirado |
| `DocumentoRevocadoException` | 403 | DOC_003 | Documento ha sido revocado |
| `GeneracionPDFException` | 500 | DOC_004 | Error al generar PDF |
| `FirmaDigitalException` | 500 | DOC_005 | Error en firma digital |
| `EscaneoMalwareException` | 500 | DOC_006 | PDF detectado como malicioso |
| `AccesoNoAutorizadoException` | 403 | DOC_007 | Violación IDOR |
| `TipoDocumentoInvalidoException` | 400 | DOC_008 | Tipo de documento no válido |
| `BucketNoEncontradoException` | 500 | DOC_009 | Bucket MinIO no existe |
| `RateLimitExcedidoException` | 429 | DOC_010 | Límite de generación excedido |

---

## 7. Integración con Otros Módulos

### 7.1 Puertos Definidos

```java
// Puerto para generación de PDFs
public interface PdfGeneratorPort {
    byte[] generarEstadoCuenta(CuentaAhorro cuenta, List<Movimiento> movimientos);
    byte[] generarConstanciaAfiliacion(Socio socio);
    byte[] generarContratoAdhesion(Solicitud solicitud, Socio socio);
    byte[] generarPagare(Credito credito, PlanAmortizacion plan);
    byte[] generarTablaAmortizacion(PlanAmortizacion plan);
    byte[] generarCartaBeneficiarios(Socio socio, List<Beneficiario> beneficiarios);
}

// Puerto para almacenamiento MinIO
public interface StoragePort {
    UploadResult upload(String bucket, String path, byte[] data, String mimeType);
    String generatePresignedUrl(String bucket, String path, int expirationMinutes);
    void delete(String bucket, String path);
    boolean exists(String bucket, String path);
}

// Puerto para escaneo de malware
public interface MalwareScannerPort {
    ScanResult scan(byte[] data);
}
```

### 7.2 Módulos Dependientes

| Módulo | Datos Consumidos |
|--------|-----------------|
| Socios | Datos del socio para constancia, contrato |
| Ahorros | Cuenta y movimientos para estado de cuenta |
| Créditos | Crédito y plan de amortización para pagaré/tabla |
| Beneficiarios | Lista de beneficiarios para carta |
| KYC (futuro) | Validación de documentos |

---

## 8. Seguridad Implementada

### 8.1 Validación IDOR

Todos los endpoints validan que el socio autenticado solo pueda generar/descargar sus propios documentos.

```java
// Ejemplo en GenerarEstadoCuentaUseCase
if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
    throw new AccesoNoAutorizadoException("DOC_007");
}
```

---

### 8.2 Rate Limiting

| Operación | Límite | Por |
|-----------|--------|-----|
| Generación de documentos | 5 req/min | Usuario |
| Generación de documentos | 20 req/min | IP |
| Descarga de documentos | 10 req/min | Usuario |

```java
// DocumentosRateLimitFilter (Bucket4j)
@Bean
public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
    // Límite: 5 gen/min por usuario
    // Límite: 20 gen/min por IP
}
```

---

### 8.3 Firma Digital

**Obligatoria para:** CONTRATO_ADHESION, PAGARE

```java
// En GenerarContratoAdhesionUseCase
Signature signature = Signature.getInstance("SHA256withRSA");
signature.initSign(privateKey); // Llave privada del fondo
signature.update(pdfBytes);
byte[] signedPdf = signature.encode();
// Incrustar firma en metadata del PDF
```

**No obligatoria para:** ESTADO_CUENTA, CONSTANCIA, TABLA_AMORTIZACION, CARTA_BENEFICIARIOS (se implementará en Fase 3)

---

### 8.4 Watermark Robusto

Todo PDF generado incluye watermark con:

```
┌─────────────────────────────────────────────────────────────┐
│  CONFIDENCIAL                                              │
│  Generado: 19/04/2026 14:30:45 UTC                        │
│  Socio: ***-***-1234 (ID: xxx-xxx)                        │
│  Hash: SHA-256:abc123...                                  │
│  Copia Controlada № 001                                   │
│  Generado por: USUARIO-QUE-SOLICITO                       │
└─────────────────────────────────────────────────────────────┘
```

---

### 8.5 Pre-signed URLs

**Nunca** se devuelven bytes del PDF directamente.

```
1. POST /documentos/generar → { documentId }
2. GET /documentos/{documentId}/descargar → { presignedUrl, expiraEn: 15min }
3. Cliente descarga desde URL de MinIO directamente
```

---

### 8.6 Escaneo ClamAV

Todo PDF es escaneado antes de almacenarse en MinIO:

```java
// En DocumentoStorageService.upload()
ScanResult scanResult = malwareScannerPort.scan(pdfBytes);
if (scanResult.isMalicious()) {
    throw new EscaneoMalwareException("DOC_006");
}
```

---

## 9. Auditoría

### 9.1 Tabla documentos_pdf_audit

```json
{
  "timestamp": "2026-04-19T14:30:45Z",
  "usuarioId": "uuid-del-usuario",
  "usuarioRol": "SOCIO",
  "ipOrigen": "192.168.1.100",
  "accion": "GENERAR",
  "documentoId": "uuid-del-documento",
  "documentoTipo": "ESTADO_CUENTA",
  "documentoHash": "SHA-256:abc123...",
  "resultado": "EXITOSO",
  "razonFallo": null
}
```

### 9.2 Acciones Auditadas

| Acción | Descripción |
|--------|-------------|
| GENERAR | Documento PDF generado exitosamente |
| DESCARGAR | Documento descargado via pre-signed URL |
| REVOCAR | Documento revocado por ADMIN |
| EXPIRAR | Documento expirado automáticamente |

---

## 10. Dependencias Externas

| Dependencia | Propósito |
|-------------|-----------|
| `com.github.librepdf:openpdf` | Generación de PDFs |
| `io.minio:minio` | Cliente MinIO |
| `spring-boot-starter-data-jpa` | Persistencia JPA |
| `spring-boot-starter-validation` | Validación de DTOs |
| `bucket4j` | Rate limiting |
| `springdoc-openapi` | Documentación Swagger |
| ClamAV (externo) | Escaneo de malware |

---

## 11. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-19 | @product-manager | Creación inicial del módulo |
| 1.0 | 2026-04-19 | @auditoria | Revisión de seguridad (4 CRÍTICAS, 6 ALTAS) |
| 1.0 | 2026-04-19 | @documentador | Documentación técnica formal |

---

## 12. Notas de Seguridad Adicionales

### 12.1 OWASP Top 10 - Controles Implementados

| OWASP Top 10 | Control | Implementación |
|--------------|---------|----------------|
| A01 - Broken Access Control | Validación IDOR | Verificación socioId en todos los endpoints |
| A02 - Cryptographic Failures | Firma digital RSA SHA-256 | Para contratos y pagarés |
| A03 - Injection | Validación de input | DTOs con @Valid |
| A04 - Insecure Design | Rate limiting | 5/min usuario, 20/min IP |
| A05 - Security Misconfiguration | Buckets segregados | Por tipo de documento |
| A07 - Identification Failures | Autenticación JWT | En todos los endpoints |
| A08 - Software Integrity | Escaneo ClamAV | PDF antes de almacenamiento |
| A10 - Server Request Forgery | Pre-signed URLs | No exposición directa de MinIO |

---

## 13. Métricas de Compliance

### 13.1 Retención de Datos

| Tipo de Dato | Duración | Razón | Estado |
|--------------|----------|-------|--------|
| Documentos PDF | 7 años | Regulación financiera | ✅ Cumple |
| Logs de auditoría | 7 años | Compliance | ✅ Cumple |
| Backups | 7 años (cold storage) | Recovery | ✅ Cumple |

### 13.2 SUDEBAN

| Requisito | Implementación | Estado |
|-----------|----------------|--------|
| Integridad documental | Hash SHA-256 + Firma RSA | ✅ Cumple |
| Trazabilidad | Auditoría completa | ✅ Cumple |
| Retención | 7 años | ✅ Configurado |

### 13.3 LOPDP

| Requisito | Implementación | Estado |
|-----------|----------------|--------|
| Protección de datos | Watermark con ID ofuscado | ✅ Cumple |
| Consentimiento | Logs de generación | ✅ Cumple |
| Derecho al olvido | Revocación + Expiración | ✅ Implementado |

---

## 14. Diagramas de Flujo

### 14.1 Flujo: Generar Estado de Cuenta

```
┌─────────────────────────────────────────────────────────────────────────────┐
│               FLUJO: GENERAR ESTADO DE CUENTA                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  SOCIO                              SISTEMA                                  │
│    │                                  │                                      │
│    │ GET /documentos/estado-cuenta/{cuentaId}                               │
│    │───────────────────────────────────►                                      │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Validar JWT y permisos  │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Validar IDOR            │                         │
│    │                     │ socioToken == cuenta.socioId                     │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Obtener cuenta +        │                         │
│    │                     │ movimientos del periodo │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Generar PDF            │                         │
│    │                     │ OpenPDF                │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Aplicar Watermark       │                         │
│    │                     │ robusto (hash, fecha)  │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Calcular SHA-256        │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                     ┌────────────┴────────────┐                         │
│    │                     │ Escanear con ClamAV    │                         │
│    │                     │ MalwareScannerPort     │                         │
│    │                     └────────────┬────────────┘                         │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Upload a MinIO            │                        │
│    │                    │ bucket-documentos        │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Persistir Documento       │                        │
│    │                    │ en repositorio            │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │                    ┌─────────────┴─────────────┐                        │
│    │                    │ Registrar auditoría      │                        │
│    │                    │ GENERAR                   │                        │
│    │                    └─────────────┬─────────────┘                        │
│    │                                  │                                      │
│    │◄──────────────────────────────── 200 OK                             │
│    │         { documentId, preSignedUrl }                                │
│    │                                  │                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 15. Referencias

- API: `/docs/modulos/documentospdf/API.md`
- Modelo de datos: `/docs/modulos/documentospdf/MODELO_DATOS.md`
- README: `/docs/modulos/documentospdf/README.md`
- CHANGELOG: `/docs/modulos/documentospdf/CHANGELOG.md`
- Módulo Beneficiarios: `/docs/modulos/beneficiarios/SPEC.md`
- Módulo KYC: `/docs/modulos/kyc/SPEC.md`
- OWASP Top 10: https://owasp.org/Top10/es/