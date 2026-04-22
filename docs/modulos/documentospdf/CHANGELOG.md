# Changelog - Módulo de Documentos PDF

Historial de cambios, correcciones de seguridad y mejoras del módulo Documentos PDF.

---

## [1.0.1] - 2026-04-19

### Fixed
- **CS-004**: Reordenar flujo seguridad - escanear con ClamAV ANTES de firmar digitalmente
- Prevenir firma de contenido malicioso en CONTRATO y PAGARE
- `GenerarContratoAdhesionUseCase`: flujo actualizado (línea 88-99)
- `GenerarPagareUseCase`: flujo actualizado (línea 91-102)

### Added
- Tests con clave RSA real para verificar firma digital
- `TestDataFactory.TEST_PRIVATE_KEY_BASE64` para tests de desarrollo
- Tests `ejecutar_conKeystoreReal_firmaDigitalValida` en ambos UseCases
- Sección "Configuración para Desarrollo" en README.md

### Changed
- Flujo de CONTRATO y PAGARE: Escanear → Hash → Firmar (antes: Hash → Firmar → Escanear)

---

## [1.0.0] - 2026-04-19

### Agregado

- **Estructura inicial del módulo**
  - Entities: `Documento`, `DocumentoAudit`
  - Enums: `TipoDocumento`, `EstadoDocumento`, `ClasificacionDocumento`
  - UseCases: 6 para generación + 1 para descarga
  - DTOs: `DocumentoResponseDTO`, `DescargarDocumentoResponseDTO`

- **6 tipos de documentos PDF**
  - ESTADO_CUENTA - Estado de cuenta mensual
  - CONSTANCIA_AFILIACION - Constancia de afiliación
  - CONTRATO_ADHESION - Contrato de adhesión (con firma digital)
  - PAGARE - Pagaré de crédito (con firma digital)
  - TABLA_AMORTIZACION - Tabla de amortización
  - CARTA_BENEFICIARIOS - Carta de beneficiarios

- **Seguridad implementada**
  - Validación IDOR en todos los endpoints
  - Rate limiting: 5 req/min por usuario, 20 req/min por IP
  - Firma digital RSA SHA-256 para CONTRATO y PAGARÉ
  - Watermark robusto con hash, fecha, clasificación y tipo copia
  - Pre-signed URLs para descarga (TTL 15 min)
  - Escaneo ClamAV obligatorio antes de almacenamiento
  - Auditoría completa (GENERAR, DESCARGAR, REVOCAR, EXPIRAR)
  - Buckets MinIO segregados por tipo de documento

- **Puertos definidos**
  - `PdfGeneratorPort`: Generación de PDFs con OpenPDF
  - `StoragePort`: Almacenamiento en MinIO
  - `MalwareScannerPort`: Escaneo con ClamAV

- **Integración unificada**
  - Carta de Beneficiarios unificada desde módulo Beneficiarios
  - Reutilización de StoragePort existente de KYC

- **Documentación completa**
  - SPEC.md - Especificación técnica
  - API.md - Referencia de API REST
  - MODELO_DATOS.md - Modelo de datos y DDL
  - README.md - Resumen ejecutivo
  - CHANGELOG.md - Este archivo

---

## Correcciones de Seguridad (Auditoría)

| ID | Criticidad | Descripción | Fecha | Autor |
|----|-------------|-------------|-------|-------|
| CS-001 | 🔴 CRÍTICA | Implementación de firma digital RSA SHA-256 para CONTRATO y PAGARÉ | 2026-04-19 | @auditoria |
| CS-002 | 🔴 CRÍTICA | Watermark robusto: hash, clasificación, tipo copia | 2026-04-19 | @auditoria |
| CS-003 | 🔴 CRÍTICA | Descarga segura SOLO via pre-signed URLs | 2026-04-19 | @auditoria |
| CS-004 | 🔴 CRÍTICA | Reordenar flujo: escanear con ClamAV ANTES de firmar digitalmente (no firmar contenido malicioso) | 2026-04-19 | @auditoria |
| CS-005 | 🟠 ALTA | Validación IDOR completa en todos los endpoints | 2026-04-19 | @auditoria |
| CS-006 | 🟠 ALTA | Ciclo de vida de documentos (ACTIVO → EXPIRADO → REVOCADO) | 2026-04-19 | @auditoria |
| CS-007 | 🟠 ALTA | Hash SHA-256 del archivo (en DB + metadata MinIO) | 2026-04-19 | @auditoria |
| CS-008 | 🟠 ALTA | Buckets MinIO segregados por tipo de documento | 2026-04-19 | @auditoria |
| CS-009 | 🟠 ALTA | Rate limiting: 5/min usuario, 20/min IP | 2026-04-19 | @auditoria |
| CS-010 | 🟠 ALTA | Matriz de permisos por rol y tipo de documento | 2026-04-19 | @auditoria |
| CS-011 | 🟡 MEDIA | Unificación de Carta Beneficiarios | 2026-04-19 | @auditoria |
| CS-012 | 🟡 MEDIA | Estructura AuditDocumento para logging | 2026-04-19 | @auditoria |

---

## Issues Resueltos

| Issue | Descripción | Estado |
|-------|-------------|--------|
| #DOC-001 | Crear estructura inicial del módulo | ✅ Resuelto |
| #DOC-002 | Implementar generación de 6 tipos de documentos | ✅ Resuelto |
| #DOC-003 | Implementar firma digital RSA SHA-256 | ✅ Resuelto |
| #DOC-004 | Implementar watermark robusto | ✅ Resuelto |
| #DOC-005 | Implementar descarga segura con pre-signed URLs | ✅ Resuelto |
| #DOC-006 | Integrar escaneo ClamAV | ✅ Resuelto |
| #DOC-007 | Implementar auditoría de generaciones y descargas | ✅ Resuelto |
| #DOC-008 | Implementar rate limiting | ✅ Resuelto |
| #DOC-009 | Implementar validación IDOR | ✅ Resuelto |
| #DOC-010 | Diseñar buckets MinIO segregados | ✅ Resuelto |
| #DOC-011 | Unificar generación de Carta Beneficiarios | ✅ Resuelto |

---

## Issues Pendientes

| Issue | Descripción | Prioridad | Estado |
|-------|-------------|-----------|--------|
| #DOC-012 | Implementar módulo en código Java | Alta | ⏳ Pendiente |
| #DOC-013 | Code review de seguridad | Alta | ⏳ Pendiente |
| #DOC-014 | Pruebas unitarias de UseCases | Alta | ⏳ Pendiente |
| #DOC-015 | Pruebas de integración con MinIO | Alta | ⏳ Pendiente |
| #DOC-016 | Pruebas de escaneo ClamAV | Media | ⏳ Pendiente |
| #DOC-017 | Firma digital avanzada (para Fase 3) | Baja | ⏳ Pendiente |

---

## Dependencias Agregadas

| Dependencia | Versión | Propósito |
|-------------|---------|-----------|
| com.github.librepdf:openpdf | 1.3.35 | Generación de PDFs |
| io.minio:minio | 8.5.7 | Cliente MinIO |

---

## Migraciones Flyway

| Archivo | Descripción |
|---------|-------------|
| V2__create_documentos_pdf.sql | Tablas documentos_pdf y documentos_pdf_audit |

---

## Referencias

- Specification: [SPEC.md](./SPEC.md)
- API: [API.md](./API.md)
- Modelo de Datos: [MODELO_DATOS.md](./MODELO_DATOS.md)
- README: [README.md](./README.md)
- Módulo Beneficiarios: [../beneficiarios/CHANGELOG.md](../beneficiarios/CHANGELOG.md)