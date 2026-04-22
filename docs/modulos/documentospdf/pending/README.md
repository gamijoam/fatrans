# Checklist de Producción - Módulo Documentos PDF

> Todo lo que debe estar configurado y verificado antes de pasar a producción

---

## 1. Infraestructura

### 1.1 MinIO - Buckets

| Bucket | Propósito | Retención | Creado |
|--------|-----------|-----------|--------|
| `bucket-documentos` | Estados de cuenta, constancias | 7 años | ☐ |
| `bucket-contratos` | Contratos de adhesión | Permanente | ☐ |
| `bucket-pagares` | Pagarés de crédito | 10 años | ☐ |
| `bucket-creditos` | Tablas de amortización | 10 años | ☐ |
| `bucket-temporal` | PDFs pendientes de escaneo | 24 horas | ☐ |

**Script de creación:**
```bash
mc alias set fondo ${MINIO_ENDPOINT} ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY}

mc mb fondo/bucket-documentos
mc mb fondo/bucket-contratos
mc mb fondo/bucket-pagares
mc mb fondo/bucket-creditos
mc mb fondo/bucket-temporal

# Configurar Object Locking (WORM) para buckets legales
mc object-lock set --default 2555days fondo/bucket-documentos
mc object-lock set --default --days 36500 fondo/bucket-contratos
mc object-lock set --default --days 36500 fondo/bucket-pagares
```

### 1.2 ClamAV - Servicio de Antivirus

| Item | Valor | Verificado |
|------|-------|-----------|
| ClamAV corriendo | Puerto 3310 | ☐ |
| Base de datos actualizada | daily.cvd | ☐ |
| Timeout configurado | 60000ms | ☐ |
| Puerto abierto desde app | 3310/tcp | ☐ |

**Verificación:**
```bash
# Ver que ClamAV está corriendo
clamdctl status

# Test de conexión
nc -zv localhost 3310

# Ver base de datos
clamscan --version
```

---

## 2. Seguridad

### 2.1 Firma Digital RSA

| Item | Estado | Notas |
|------|--------|-------|
| Keystore PKCS12 generado | ☐ | `firma-digital.p12` |
| Llave privada protegida | ☐ | Password fuerte (>16 chars) |
| Alias configurado | ☐ | `documentos-fondo` |
| Certificado vigente | ☐ | Verificar fecha expiración |
| Backup del keystore | ☐ | En location seguro |

**Variables de entorno requeridas:**
```bash
DOC_FIRMA_KEYSTORE=/etc/fondo/firma-digital.p12
DOC_FIRMA_PASSWORD=<password-seguro>
DOC_FIRMA_KEY_ALIAS=documentos-fondo
```

**Generar keystore:**
```bash
keytool -genkeypair \
  -alias documentos-fondo \
  -keyalg RSA \
  -keysize 4096 \
  -keystore firma-digital.p12 \
  -storetype PKCS12 \
  -storepass '<password-seguro>' \
  -validity 3650 \
  -dname "CN=FondoAhorro Documentos, OU=TI, O=FondoAhorro, L=Caracas, ST=DistritoCapital, C=VE"
```

**⚠️ IMPORTANTE:**
- El keystore NUNCA debe estar en el repositorio git
- El keystore debe tener backup en lugar seguro (HSM o vault)
- El password debe estar en un secrets manager

### 2.2 Permisos de Archivos

| Archivo/Directorio | Permisos | Propietario |
|--------------------|----------|-------------|
| `/etc/fondo/firma-digital.p12` | 600 | `fondoapp:fondoapp` |
| `/var/log/fondo/documentos/` | 750 | `fondoapp:fondoapp` |
| Buckets MinIO | WORM enabled | N/A |

### 2.3 Rate Limiting

| Endpoint | Límite | Verificado |
|----------|--------|-----------|
| Generación por usuario | 5 req/min | ☐ |
| Generación por IP | 20 req/min | ☐ |
| Descarga por usuario | 10 req/min | ☐ |
| Consulta por usuario | 30 req/min | ☐ |

### 2.4 IP Spoofing Protection

| Config | Valor | Verificado |
|--------|-------|-----------|
| `server.trusted-proxies` | IPs de load balancer | ☐ |
| X-Forwarded-For validation | Habilitado | ☐ |

---

## 3. Variables de Entorno Obligatorias

```bash
# ====================
# DOCUMENTOS PDF
# ====================

# ClamAV
CLAMAV_HOST=clamav.internal
CLAMAV_PORT=3310
CLAMAV_TIMEOUT=60000
CLAMAV_ENABLED=true

# Firma Digital RSA
DOC_FIRMA_KEYSTORE=/etc/fondo/firma-digital.p12
DOC_FIRMA_PASSWORD=<password>
DOC_FIRMA_KEY_ALIAS=documentos-fondo

# MinIO
MINIO_ENDPOINT=https://minio.internal:9000
MINIO_ACCESS_KEY=<access-key>
MINIO_SECRET_KEY=<secret-key>

# Rate Limiting
DOCUMENTOS_RATE_LIMIT_GEN_USER=5
DOCUMENTOS_RATE_LIMIT_GEN_IP=20
DOCUMENTOS_RATE_LIMIT_DOWNLOAD=10

# Trusted Proxies (IPs de LB/proxies separados por coma)
SERVER_TRUSTED_PROXIES=10.0.1.100,10.0.1.101
```

---

## 4. Base de Datos

### 4.1 Migraciones Flyway

| Migración | Descripción | Ejecutada |
|-----------|-------------|-----------|
| V6__create_documentos_pdf.sql | Tablas documentos y auditoría | ☐ |

### 4.2 Índices y Constraints

Verificar que existen:
- [ ] `idx_documentos_socio_id`
- [ ] `idx_documentos_tipo`
- [ ] `idx_documentos_estado`
- [ ] `idx_documentos_hash`
- [ ] `chk_tipo` constraint
- [ ] `chk_estado` constraint
- [ ] `trg_validar_firma_digital_documento` trigger

### 4.3 Retención de Auditoría

| Tabla | Retención | Verificado |
|-------|-----------|-----------|
| `documentos_pdf_audit` | 7 años | ☐ |

```sql
-- Verificar que existe política de purga
SELECT * FROM pg_policies WHERE tablename = 'documentos_pdf_audit';
```

---

## 5. Monitoreo y Alertas

### 5.1 Métricas a Monitorear

| Métrica | Threshold | Alerta |
|---------|----------|--------|
| `documentos.generados.total` | > 1000/min | Warning |
| `documentos.generados.errors` | > 1% | Critical |
| `documentos.clamav.scan_time` | > 5s | Warning |
| `documentos.firma.digital.errors` | > 0 | Critical |
| `documentos.rate_limit.exceeded` | > 100/min | Warning |

### 5.2 Logs a Revisar

- [ ] `documentospdf.generacion` - INFO por cada PDF generado
- [ ] `documentospdf.auditoria` - Todas las operaciones
- [ ] `documentospdf.clamav` - Scans y resultados
- [ ] `documentospdf.firma` - Intentos de firma digital

### 5.3 Alerts de Grafana/Prometheus

```yaml
# Ejemplo de alerta
- alert: DocumentosPDFHighErrorRate
  expr: rate(documentos_generados_errors_total[5m]) > 0.01
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "Alta tasa de errores en generación de documentos PDF"
```

---

## 6. Backup y Disaster Recovery

### 6.1 Backups Requeridos

| Item | Frecuencia | Retention | Verificado |
|------|------------|-----------|-----------|
| Base de datos (documentos_pdf) | Daily | 7 años | ☐ |
| Keystore firma digital | Weekly + onChange | 7 años | ☐ |
| Configuración (application.yml) | OnChange | 7 años | ☐ |
| Logs de auditoría | Daily | 7 años | ☐ |

### 6.2 Procedimiento de Restore

```bash
# 1. Restaurar keystore si se perdió
cp /backup/keystore/firma-digital.p12 /etc/fondo/
chown fondoapp:fondoapp /etc/fondo/firma-digital.p12
chmod 600 /etc/fondo/firma-digital.p12

# 2. Restaurar base de datos
psql -h localhost -U app -d fondo < backup/documentos_pdf_$(date +%Y%m%d).sql

# 3. Verificar integridad
SELECT COUNT(*) FROM documentos_pdf WHERE estado = 'GENERADO';
```

---

## 7. Documentación Requerida

- [ ] Runbook de operación del módulo
- [ ] Procedimiento de rotación de keystore
- [ ] Procedimiento de emergencia si ClamAV cae
- [ ] Contactos de equipo de seguridad
- [ ] SLAs documentados

---

## 8. Pruebas de Aceptación

### 8.1 Pruebas Funcionales

| Prueba | Resultado |
|--------|-----------|
| Generar estado de cuenta | ☐ |
| Generar constancia de afiliación | ☐ |
| Generar contrato (con firma digital) | ☐ |
| Generar pagaré (con firma digital) | ☐ |
| Generar tabla de amortización | ☐ |
| Intentar generar carta beneficiarios (debe fallar) | ☐ |
| Descargar documento via pre-signed URL | ☐ |
| Verificar watermark en PDF | ☐ |
| Verificar firma digital en contrato | ☐ |

### 8.2 Pruebas de Seguridad

| Prueba | Resultado |
|--------|-----------|
| IDOR - Intentar ver documento de otro socio | ☐ Bloqueado |
| Rate limit - Exceder límites | ☐ Bloqueado |
| IP Spoofing - X-Forwarded-For falsificado | ☐ Bloqueado |
| ClamAV - PDF con malware | ☐ Bloqueado |
| Sin keystore - Generar contrato | ☐ Falla audible |

### 8.3 Pruebas de Carga

| Prueba | Resultado |
|--------|-----------|
| 100 generaciones concurrentes | ☐ |
| 50 descargas concurrentes | ☐ |
| ClamAV con 100 archivos | ☐ Time < 5s avg |

---

## 9. Compliance y Auditoría

### 9.1 SUDEBAN

| Requisito | Cumplimiento | Evidencia |
|-----------|---------------|-----------|
| Integridad documental (hash) | ☐ | SHA-256 calculado |
| Firma digital para contratos | ☐ | RSA SHA-256 |
| Trazabilidad completa | ☐ | Shadow table |
| Retención 7 años | ☐ | Policy configurada |

### 9.2 LOPDP

| Requisito | Cumplimiento | Evidencia |
|-----------|---------------|-----------|
| Datos protegidos en PDF | ☐ | Marca de agua |
| Consentimiento logged | ☐ | Auditoría |
| Derecho al olvido | ☐ | Revocación implementada |

---

## 10. Checklist Final de Go-Live

### Infraestructura
- [ ] MinIO buckets creados y configurados
- [ ] ClamAV corriendo y funcional
- [ ] Keystore de firma digital configurado
- [ ] Base de datos migrada

### Seguridad
- [ ] Todas las pruebas de seguridad passing
- [ ] Rate limiting verificado en prod
- [ ] IP spoofing protection activo
- [ ] Permisos de archivos configurados

### Monitoreo
- [ ] Dashboards de Grafana creados
- [ ] Alertas configuradas
- [ ] Runbook disponible

### Documentación
- [ ] Runbook de operación
- [ ] Procedimientos de emergencia
- [ ] Team entrenado

### Aprobaciones
- [ ] Code review aprobado
- [ ] Security review aprobado
- [ ] Product owner aprobado

---

## Referencias

- Especificación: [../SPEC.md](../SPEC.md)
- API: [../API.md](../API.md)
- Modelo de datos: [../MODELO_DATOS.md](../MODELO_DATOS.md)
- Changelog: [../CHANGELOG.md](../CHANGELOG.md)
- Auditoría: [/docs/auditorias/](../auditorias/)

---

**Documento creado:** 2026-04-19
**Última actualización:** 2026-04-19
**Autor:** @programador-java
**Versión:** 1.0