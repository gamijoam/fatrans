# KYC - Requisitos de Producción

## Resumen

Documento técnico con los requisitos, configuración y consideraciones de seguridad para el despliegue en producción del módulo KYC (Know Your Customer).

---

## 1. Base de Datos PostgreSQL

### Extensiones Requeridas

```sql
CREATE EXTENSION IF NOT EXISTS uuid-ossp;
```

### Índices para Performance

```sql
-- Índices para verificacion_kyc
CREATE INDEX idx_verificacion_kyc_usuario ON verificacion_kyc(usuario_id);
CREATE INDEX idx_verificacion_kyc_estado ON verificacion_kyc(estado);
CREATE INDEX idx_verificacion_kyc_fecha ON verificacion_kyc(fecha_verificacion);

-- Índices para documento_identidad
CREATE INDEX idx_documento_identidad_verificacion ON documento_identidad(verificacion_kyc_id);
CREATE INDEX idx_documento_identidad_tipo ON documento_identidad(tipo_documento);

-- Índices para consentimiento_kyc
CREATE INDEX idx_consentimiento_kyc_usuario ON consentimiento_kyc(usuario_id);
CREATE INDEX idx_consentimiento_kyc_tipo ON consentimiento_kyc(tipo_consentimiento);

-- Índices para audit_kyc
CREATE INDEX idx_audit_kyc_usuario ON audit_kyc(usuario_id);
CREATE INDEX idx_audit_kyc_fecha ON audit_kyc(fecha_accion);
CREATE INDEX idx_audit_kyc_accion ON audit_kyc(accion);
```

### Tablas Principales

| Tabla | Descripción |
|-------|-------------|
| `verificacion_kyc` | Registro principal de verificación KYC |
| `documento_identidad` | Documentos de identidad uploadedos |
| `consentimiento_kyc` | Consentimientos informados registrados |
| `audit_kyc` | Log de auditoría de todas las operaciones |

---

## 2. Storage (MinIO/S3)

### Configuración del Bucket

```yaml
# Bucket privado para documentos KYC
bucket:
  name: kyc-documents
  policy: private

# Encriptación SSE-KMS
sse:
  type: SSE-KMS
  key-id: ${MINIO_KMS_KEY_ID:alias/fondo-ahorro-kyc}

# Lifecycle policy (retención 7 años por regulación)
lifecycle:
  enabled: true
  days: 2555
```

### Pre-signed URLs

```
TTL: 15 minutos máximo
Método: GET y PUT
No exponer URLs directas al cliente
```

### Seguridad de Archivos

- **No exponer URLs directas:** Siempre usar pre-signed URLs
- **Validación de nombres de archivo:** Prevenir path traversal
- **Tipo de contenido restringido:** Solo `image/jpeg`, `image/png`, `application/pdf`
- **Tamaño máximo:** 10MB por archivo

---

## 3. ClamAV - Escaneo de Malware

### Propósito

Escanear todos los archivos subidos (imágenes, PDFs) antes de almacenarlos para detectar malware.

### Arquitectura

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────┐
│  Application    │────▶│ MalwareScannerPort│────▶│ ClamAV      │
│  (Domain)       │     │  (Interface)     │     │ (TCP 3310)  │
└─────────────────┘     └──────────────────┘     └─────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │ ClamAVAdapter    │
                        │ (Infrastructure) │
                        └──────────────────┘
```

### Puerto de Dominio

```java
/**
 * Puerto de dominio para escaneo de malware
 */
public interface MalwareScannerPort {
    /**
     * Escanea un archivo en busca de malware
     * @param fileContent Contenido del archivo en bytes
     * @param filename Nombre original del archivo
     * @param contentType Tipo MIME del archivo
     * @return ScanResult con el resultado del escaneo
     */
    ScanResult scan(byte[] fileContent, String filename, String contentType);
}
```

### Adaptador ClamAV

```java
/**
 * Adaptador que conecta con ClamAV via TCP socket
 */
@Component
@RequiredArgsConstructor
public class ClamAVAdapter implements MalwareScannerPort {

    private final ClamAVProperties properties;

    @Override
    public ScanResult scan(byte[] fileContent, String filename, String contentType) {
        // Implementación del escaneo via socket TCP
    }
}
```

### Flujo de Validación de Documentos

```
┌─────────────────────────────────────────────────────────────┐
│                    VALIDACIÓN DE DOCUMENTOS                  │
├─────────────────────────────────────────────────────────────┤
│  1. Validar tamaño (max 10MB)                                │
│  2. Validar formato MIME (jpeg/png/pdf)                      │
│  3. Decodificar Base64                                       │
│  4. Verificar tamaño decodificado                            │
│  5. Validar magic number (firma del archivo)                 │
│  6. Escanear con ClamAV  ← NUEVO                             │
│  7. Validar verificación editable                            │
│  8. Validar tipo documento según nivel KYC                   │
│  9. Subir a MinIO                                            │
└─────────────────────────────────────────────────────────────┘
```

### Respuesta de ClamAV

| Resultado | Acción |
|-----------|--------|
| **Limpio** | Continúa con el upload normal |
| **Malware detectado** | Lanza `DocumentoMaliciosoException` (código KYC_017) |
| **ClamAV no disponible** | Permite upload con flag `requiereRevisionManual=true` |

### Configuración de ClamAV

```yaml
clamav:
  host: localhost          # Docker: clamav
  port: 3310               # Puerto default ClamAV
  timeout-ms: 60000        # 60 segundos
  enabled: true            # false para desarrollo
```

### Docker Compose (Desarrollo)

```yaml
services:
  clamav:
    image: clamav/clamav:latest
    ports:
      - "3310:3310"
    volumes:
      - clamav-db:/var/lib/clamav
    environment:
      - CLAMAV_NO_MILTERD=true
    healthcheck:
      test: ["CMD", "clamd", "/tmp/clamd.sock", "HealthCheck"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  clamav-db:
```

---

## 4. Seguridad Implementada

### Rate Limiting (Bucket4j)

```java
@Bean
public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
    // Límite: 100 solicitudes por minuto por IP
    // Límite: 10 uploads por minuto por usuario
}
```

### Validación de IP

- Soporte IPv4 e IPv6
- Lista blanca configurable
- Detección de proxies transparents

### Pre-signed URLs

```java
// Generación de URL con TTL de 15 minutos
String presignedUrl = minioClient.getPresignedObjectUrl(
    GetPresignedObjectUrlArgs.builder()
        .bucket("kyc-documents")
        .object(documentId)
        .expiry(15, TimeUnit.MINUTES)
        .method(Method.GET)
        .build()
);
```

### Optimistic Locking

```java
@Entity
public class VerificacionKyc {
    @Version
    private Long version;
}
```

### Auditoría Completa

Todas las operaciones se registran en `audit_kyc`:

```java
@MappedSuperclass
public abstract class Auditable {
    @Column(name = "usuario_modificacion")
    private String usuarioModificacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
```

### Validación Path Traversal

```java
private String sanitizeFilename(String filename) {
    // Eliminar caracteres ../ y ..\
    // Validar que no contenga path absoluto
    return FilenameUtils.getName(filename);
}
```

---

## 5. Variables de Entorno

### Variables Requeridas

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_URL` | URL de conexión PostgreSQL | `jdbc:postgresql://localhost:5432/fondo_ahorro` |
| `DB_USER` | Usuario de base de datos | `fondo_ahorro_user` |
| `DB_PASS` | Contraseña de base de datos | `***` |
| `JWT_SECRET` | Secreto para firmar JWTs | `***` (min 256 bits) |

### Variables ClamAV

| Variable | Descripción | Default |
|----------|-------------|---------|
| `CLAMAV_HOST` | Host del servidor ClamAV | `localhost` |
| `CLAMAV_PORT` | Puerto TCP de ClamAV | `3310` |
| `CLAMAV_TIMEOUT` | Timeout en milisegundos | `60000` |
| `CLAMAV_ENABLED` | Habilitar/deshabilitar escaneo | `true` |

### Variables MinIO

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `MINIO_ENDPOINT` | Endpoint del servidor MinIO | `localhost:9000` |
| `MINIO_ACCESS_KEY` | Access key de MinIO | `minioadmin` |
| `MINIO_SECRET_KEY` | Secret key de MinIO | `***` |
| `MINIO_BUCKET_KYC` | Bucket para documentos KYC | `kyc-documents` |

### Variables Opccionales

| Variable | Descripción | Default |
|----------|-------------|---------|
| `RATE_LIMIT_REQUESTS` | Solicitudes por minuto | `100` |
| `RATE_LIMIT_UPLOADS` | Uploads por minuto | `10` |
| `MAX_FILE_SIZE_MB` | Tamaño máximo de archivo | `10` |
| `PRESIGNED_URL_EXPIRY_MINUTES` | TTL de pre-signed URLs | `15` |

### Ejemplo .env Production

```bash
# Base de Datos
DB_URL=jdbc:postgresql://db-prod:5432/fondo_ahorro
DB_USER=fondo_ahorro_prod
DB_PASS=super_secret_password

# JWT
JWT_SECRET=your-256-bit-secret-key-here-min-32-chars

# ClamAV
CLAMAV_HOST=clamav.internal
CLAMAV_PORT=3310
CLAMAV_TIMEOUT=60000
CLAMAV_ENABLED=true

# MinIO/S3
MINIO_ENDPOINT=minio.internal:9000
MINIO_ACCESS_KEY=minio_access_key
MINIO_SECRET_KEY=minio_secret_key
MINIO_BUCKET_KYC=kyc-documents
MINIO_KMS_KEY_ID=alias/fondo-ahorro-kyc

# Opcionales
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_UPLOADS=10
MAX_FILE_SIZE_MB=10
PRESIGNED_URL_EXPIRY_MINUTES=15
```

---

## 6. Códigos de Error KYC

| Código | Descripción |
|--------|-------------|
| KYC_001 | Usuario no encontrado |
| KYC_002 | Verificación KYC no encontrada |
| KYC_003 | Nivel KYC no válido |
| KYC_004 | Documento no encontrado |
| KYC_005 | Formato de documento no soportado |
| KYC_006 | Tamaño de archivo excede el límite |
| KYC_007 | Consentimiento requerido |
| KYC_008 | Verificación no editable |
| KYC_009 | Error en validación de datos |
| KYC_010 | Error en subida de archivo |
| KYC_011 | Error en descarga de archivo |
| KYC_012 | Error en generación de pre-signed URL |
| KYC_013 | Verificación vencida |
| KYC_014 | nonce inválido o expirado |
| KYC_015 | IP no autorizada |
| KYC_016 | Rate limit excedido |
| KYC_017 | Documento malicioso detectado |
| KYC_018 | Magic number inválido |
| KYC_019 | Error en escaneo de malware |
| KYC_020 | Revisión manual requerida |

---

## 7. Checklist de Despliegue

### Pre-despliegue

- [ ] Extensión `uuid-ossp` habilitada en PostgreSQL
- [ ] Índices creados en todas las tablas
- [ ] Bucket `kyc-documents` creado en MinIO
- [ ] Política de bucket configurada como `private`
- [ ] Encriptación SSE-KMS habilitada
- [ ] ClamAV desplegado y saludable
- [ ] Variables de entorno configuradas
- [ ] JWT_SECRET generado (256 bits)

### Post-despliegue

- [ ] Verificar conectividad a ClamAV
- [ ] Probar upload de documento limpio
- [ ] Probar detección de malware (EICAR test)
- [ ] Verificar rate limiting activo
- [ ] Verificar auditoría en `audit_kyc`
- [ ] Probar pre-signed URLs (15 min TTL)
- [ ] Validar optimistic locking

### Monitoreo

- [ ] Dashboard de métricas KYC
- [ ] Alertas de documentos maliciosos
- [ ] Alertas de fallos de ClamAV
- [ ] Logs de auditoría centralizados
