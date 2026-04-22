# Módulo de Documentos PDF

> Generación, almacenamiento y descarga de documentos legales y financieros en PDF

## Descripción

El **Módulo de Documentos PDF** gestiona la generación de documentos oficiales del Fondo de Ahorro con validez legal, incluyendo estados de cuenta, constancias de afiliación, contratos de adhesión, pagarés, tablas de amortización y cartas de beneficiarios.

El módulo garantiza la integridad documental mediante firma digital RSA SHA-256, marca de agua robusta y almacenamiento seguro en MinIO con pre-signed URLs.

**Ubicación:** `/backend/src/main/java/com/tufondo/documentospdf/`

---

## Alcance

### Incluido

- Generación de 6 tipos de documentos PDF
- Firma digital RSA SHA-256 para CONTRATO y PAGARÉ
- Marca de agua (watermark) robusta con hash y clasificación
- Almacenamiento seguro en MinIO con buckets segregados
- Descarga segura via pre-signed URLs (TTL 15 min)
- Escaneo obligatorio con ClamAV
- Auditoría completa de generaciones y descargas
- Rate limiting: 5 gen/min por usuario, 20 gen/min por IP
- Validación IDOR completa
- Unificación de Carta de Beneficiarios (antes en Beneficiarios)

### Fuera del Scope

- Generación de reportes Excel/CSV (módulo Reportes)
- Notificaciones por email (módulo Notificaciones)
- Firma digital avanzada (para Fase 3)

---

## Arquitectura

El módulo sigue **Clean Architecture** con las siguientes capas:

```
backend/src/main/java/com/tufondo/documentospdf/
├── domain/                              # Capa de Dominio (puro)
│   ├── model/
│   │   ├── Documento.java
│   │   └── enums/
│   │       ├── TipoDocumento.java
│   │       ├── EstadoDocumento.java
│   │       └── ClasificacionDocumento.java
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
    │   ├── OpenPdfGeneratorService.java
    │   └── templates/
    ├── storage/
    │   └── MinIODocumentStorageService.java
    └── security/
        ├── RateLimitDocumentosFilter.java
        └── PdfSecurityValidator.java
```

---

## Tipos de Documentos

| # | Tipo | Descripción | Firma Digital | Clasificación |
|---|------|-------------|--------------|--------------|
| 1 | ESTADO_CUENTA | Estado de cuenta mensual | No | CONFIDENCIAL |
| 2 | CONSTANCIA_AFILIACION | Constancia de afiliación | No | PUBLICO |
| 3 | CONTRATO_ADHESION | Contrato de adhesión | **Sí** | RESTRINGIDO |
| 4 | PAGARE | Pagaré de crédito | **Sí** | RESTRINGIDO |
| 5 | TABLA_AMORTIZACION | Tabla de amortización | No | CONFIDENCIAL |
| 6 | CARTA_BENEFICIARIOS | Carta de beneficiarios | No | CONFIDENCIAL |

---

## Seguridad

El módulo implementa múltiples controles de seguridad:

| Control | Descripción | Implementación |
|---------|-------------|----------------|
| **Autenticación** | JWT Bearer token requerido | Header `Authorization: Bearer <token>` |
| **Autorización RBAC** | Roles: SOCIO, ADMIN, SISTEMA | Validación por rol en cada endpoint |
| **Validación IDOR** | Socio solo genera/descarga sus propios docs | Verificación socioId en todos los endpoints |
| **Rate Limiting** | 5 req/min usuario, 20 req/min IP | Bucket4j filter |
| **Firma Digital** | RSA SHA-256 para contratos/pagarés | Bouncy Castle |
| **Watermark** | Hash + fecha + clasificación + tipo copia | En cada PDF generado |
| **Pre-signed URLs** | Acceso indirecto a MinIO (TTL 15 min) | MinIO SDK |
| **Escaneo ClamAV** | Malware scanning antes de almacenar | MalwareScannerPort |
| **Auditoría** | Logging de todas las operaciones | Tabla `documentos_pdf_audit` |

---

## Flujo de Negocio

### Generar Documento

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Socio     │───►│  Validar   │───►│  Obtener   │───►│  Generar   │
│  solicita   │    │   JWT +     │    │   datos     │    │   PDF       │
└─────────────┘    │   IDOR      │    └─────────────┘    └─────────────┘
                   └─────────────┘                              │
                                                             ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Registrar  │◄───│  Escanear   │◄───│  Aplicar    │◄───│  Watermark  │
│  auditoría  │    │  ClamAV     │    │  SHA-256    │    │  robusto    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**Pasos detallados:**

1. **Validar JWT y permisos**
   - Extraer socioId del token
   - Verificar rol

2. **Validar IDOR**
   - Socio solo puede generar sus propios documentos
   - ADMIN puede generar cualquier documento

3. **Obtener datos**
   - Consultar datos según tipo de documento
   - Generar PDF con OpenPDF

4. **Aplicar watermark robusto**
   ```
   CONFIDENCIAL
   Generado: {fecha}
   Socio: ***-***-{ultimos 4}
   Hash: SHA-256:{hash}
   ```

5. **Calcular hash SHA-256**

6. **Firmar digitalmente** (solo CONTRATO y PAGARE)

7. **Escanear con ClamAV**

8. **Subir a MinIO** (bucket segregado)

9. **Registrar en repositorio**

10. **Registrar auditoría**

11. **Retornar pre-signed URL**

---

## Endpoints Disponibles

| # | Método | Path | Descripción | Rol |
|---|--------|------|-------------|-----|
| 1 | GET | `/documentos/estado-cuenta/{cuentaId}` | Generar estado de cuenta | SOCIO, ADMIN |
| 2 | GET | `/documentos/constancia-afiliacion/{socioId}` | Generar constancia | SOCIO, ADMIN |
| 3 | GET | `/documentos/contrato/{solicitudId}` | Generar contrato | ADMIN, SISTEMA |
| 4 | GET | `/documentos/pagare/{creditoId}` | Generar pagaré | ADMIN, SISTEMA |
| 5 | GET | `/documentos/tabla-amortizacion/{creditoId}` | Generar tabla amort. | SOCIO, ADMIN, SISTEMA |
| 6 | GET | `/documentos/carta-beneficiarios/{socioId}` | Generar carta benef. | SOCIO, ADMIN |
| 7 | GET | `/documentos/{documentoId}` | Obtener metadata | SOCIO, ADMIN |
| 8 | GET | `/documentos/{documentoId}/descargar` | Descargar PDF | SOCIO, ADMIN |
| 9 | GET | `/documentos/socio/{socioId}` | Listar documentos | SOCIO, ADMIN |

---

## Integraciones

El módulo se integra con los siguientes módulos y servicios:

### MinIO

```yaml
buckets:
  bucket-documentos: 7 años
  bucket-contratos: permanente
  bucket-pagares: 10 años
  bucket-creditos: 10 años
  bucket-temporal: 24 horas
```

### ClamAV (externo)

```java
public interface MalwareScannerPort {
    ScanResult scan(byte[] data);
}
```

### OpenPDF

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.35</version>
</dependency>
```

---

## Estado del Módulo

| Aspecto | Estado | Notas |
|---------|--------|-------|
| Desarrollo | ✅ Completado | Implementación Java/Spring Boot |
| Documentación | ✅ Completada | SPEC, API, MODELO_DATOS, README, CHANGELOG |
| Code Review | ✅ Completado | Correcciones de seguridad aplicadas |
| Seguridad | ✅ Completada | CS-004: Flujo reordenado (escaneo antes de firma) |
| Pruebas | ✅ Completadas | Unitarias + tests con firma RSA real |
| Producción | ⏳ En espera | Depende de despliegue |

---

## Configuración para Desarrollo

### Dos Métodos de Configuración de Firma Digital

El módulo soporta **dos formas** de configurar la firma digital RSA:

#### Método 1: Clave Privada Base64 (para UseCases)
Usado por `GenerarContratoAdhesionUseCase` y `GenerarPagareUseCase`:

```bash
# Variable de entorno
DOCUMENTOS_PDF_FIRMA_DIGITAL_CLAVE_PRIVADA=<clave-privada-base64>

# En application.yml:
documentospdf:
  firma-digital:
    clave-privada: <clave-privada-base64>
```

El código de estos UseCases decodifica la clave Base64 y la usa directamente para firmar.

#### Método 2: Keystore PKCS12 (para OpenPdfGeneratorService)
Usado por `OpenPdfGeneratorService` en tests de integración y cuando se usa el generador directamente:

```bash
# En application.yml:
documentospdf:
  firma-digital:
    enabled: true
    keystore-path: /path/to/keystore.p12
    keystore-password: <password>
    key-alias: documentos-fondo
```

### Generar Clave Privada para Uso en Tests

1. **Generar par de claves RSA 2048 bits:**
```bash
# Crear directorio para la clave
mkdir -p backend/test-resources/firma

# Generar clave privada RSA
openssl genrsa -out backend/test-resources/firma/private_key.pem 2048

# Convertir a PKCS#8 (formato Java estándar)
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt \
  -in backend/test-resources/firma/private_key.pem \
  -out backend/test-resources/firma/private_key.der

# Obtener Base64 para variable de entorno
base64 -w 0 backend/test-resources/firma/private_key.der > backend/test-resources/firma/private_key_base64.txt
```

2. **Crear Keystore PKCS12 (para OpenPdfGeneratorService):**
```bash
# Crear certificado autofirmado
openssl req -new -x509 -key backend/test-resources/firma/private_key.pem \
  -out backend/test-resources/firma/cert.pem -days 365 \
  -subj "/CN=FondoAhorro/O=Test"

# Crear keystore PKCS12
openssl pkcs12 -export -in backend/test-resources/firma/cert.pem \
  -inkey backend/test-resources/firma/private_key.pem \
  -out backend/test-resources/firma/test-keystore.p12 \
  -name test-documents -password pass:test123
```

3. **Usar en tests:**
```java
// En OpenPdfGeneratorServiceTest.java
ReflectionTestUtils.setField(pdfGeneratorService, "firmaDigitalEnabled", true);
ReflectionTestUtils.setField(pdfGeneratorService, "keystorePath", "/path/to/test-keystore.p12");
ReflectionTestUtils.setField(pdfGeneratorService, "keystorePassword", "test123");
ReflectionTestUtils.setField(pdfGeneratorService, "keyAlias", "test-documents");
```

### Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `DOC_005: No se puede generar CONTRATO. Contacte al administrador.` | Clave privada no configurada | Configurar `DOCUMENTOS_PDF_FIRMA_DIGITAL_CLAVE_PRIVADA` |
| `DOC_005: Error al firmar digitalmente el documento` | Keystore no encontrado o password incorrecto | Verificar `keystore-path` y `keystore-password` |
| `Firma digital no configurada` | `firmaDigitalEnabled=false` | Cambiar a `true` en configuración |

### Ejecutar Tests

```bash
# Ejecutar todos los tests del módulo
cd backend
./mvnw test -pl :documentospdf -am

# Ejecutar solo tests de firma digital
./mvnw test -pl :documentospdf -am \
  -Dtest="GenerarContratoAdhesionUseCaseTest,GenerarPagareUseCaseTest"

# Verificar cobertura
./mvnw test -pl :documentospdf -am -jacoco
```

### Flujo de Seguridad (CONTRATO y PAGARE)

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Generar    │───►│  Escanear   │───►│  Calcular   │───►│   Firmar    │
│  PDF        │    │  ClamAV     │    │  SHA-256    │    │  RSA-256    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                              │
                                                              ▼
                   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
                   │  Registrar  │◄───│  Subir a    │◄───│  Metadata   │
                   │  auditoría   │    │  MinIO      │    │  + firma    │
                   └─────────────┘    └─────────────┘    └─────────────┘
```

**Nota de seguridad (CS-004):** El escaneo con ClamAV se realiza ANTES de la firma digital para evitar firmar contenido malicioso. Si el PDF es detectado como malicioso, se lanza `EscaneoMalwareException` y el documento NO se firma.

---

## Versión

**Versión actual:** 1.0.1
**Última actualización:** 2026-04-19
**Autores:** @product-manager, @auditoria, @documentador

---

## Enlaces Útiles

| Documento | Descripción |
|-----------|-------------|
| [SPEC.md](./SPEC.md) | Especificación técnica completa |
| [API.md](./API.md) | Referencia de API REST |
| [MODELO_DATOS.md](./MODELO_DATOS.md) | Modelo de datos y DDL |
| [CHANGELOG.md](./CHANGELOG.md) | Historial de cambios |
| [Módulo Beneficiarios](../beneficiarios/SPEC.md) | Documentación del módulo Beneficiarios |
| [Módulo Créditos](../creditos/SPEC.md) | Documentación del módulo Créditos |
| [Módulo KYC](../kyc/SPEC.md) | Documentación del módulo KYC |

---

## Reglas de Negocio

| ID | Regla | Validación |
|----|-------|------------|
| RN-D-01 | Hash obligatorio | Todo PDF debe tener hash SHA-256 |
| RN-D-02 | Firma para contratos | CONTRATO y PAGARE requieren firma RSA |
| RN-D-03 | Pre-signed URL | Nunca devolver bytes directamente |
| RN-D-04 | Escaneo obligatorio | Todo PDF debe ser escaneado con ClamAV |
| RN-D-05 | Bucket segregado | Documentos en buckets según tipo |
| RN-D-06 | IDOR | Socio solo puede acceder a sus propios documentos |
| RN-D-07 | Rate limit | 5 gen/min por usuario, 20 gen/min por IP |
| RN-D-08 | Expiración | Estados de cuenta expiran en 7 días |

---

## Métricas de Compliance

| Métrica | Valor | Estado |
|---------|-------|--------|
| Retención de documentos | 7 años | ✅ Cumple |
| Retención de contratos | Permanente | ✅ Cumple |
| Retención de pagarés | 10 años | ✅ Cumple |
| Retención de auditoría | 7 años | ✅ Cumple |
| Integridad (hash SHA-256) | 100% | ✅ Cumple |
| Firma digital (contratos) | 100% | ✅ Implementado |
| Trazabilidad | 100% | ✅ Cumple |