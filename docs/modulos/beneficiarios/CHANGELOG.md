# Changelog - Módulo de Beneficiarios

Historial de cambios, correcciones de seguridad y mejoras del módulo Beneficiarios.

---

## 1.0.0 (2026-04-19)

### Agregado

- **Estructura inicial del módulo**
  - Entities: `Beneficiario`, `BeneficiarioAudit`
  - UseCases: Create, Get, Update, Delete, ValidatePorcentajesSum
  - DTOs: CreateBeneficiarioRequestDTO, UpdateBeneficiarioRequestDTO, BeneficiarioResponseDTO

- **Validaciones de negocio implementadas**
  - Validación de porcentaje (0.01 - 100.00)
  - Validación de suma de porcentajes = 100%
  - Límite máximo de 5 beneficiarios por socio
  - Validación de documento único por socio
  - Validación de documento diferente al titular

- **Seguridad implementada**
  - Validación IDOR en todos los endpoints
  - Rate limiting (Bucket4j): 10 req/min por socio
  - Auditoría completa (beneficiaries_audit)
  - Soft delete obligatorio

- **Puertos definidos**
  - `SocioQueryPort`: Consulta desacoplada de socios
  - `KYCQueryPort`: Definido para validación futura de documentos

- **Documentación completa**
  - SPEC.md - Especificación técnica
  - MODELO_DATOS.md - Modelo de datos y DDL
  - API.md - Referencia de API REST
  - README.md - Punto de entrada
  - CHANGELOG.md - Este archivo

### Correcciones de Seguridad

| ID | Descripción | Fecha | Autor |
|----|-------------|-------|-------|
| CS-001 | Implementación inicial de validación IDOR | 2026-04-19 | @programador-java |
| CS-002 | Rate limiting configurado (10 req/min) | 2026-04-19 | @programador-java |
| CS-003 | Auditoría de cambios completa (CREATE, UPDATE, DELETE) | 2026-04-19 | @programador-java |
| CS-004 | Validación documento diferente al titular | 2026-04-19 | @programador-java |
| CS-005 | Validación documento único por socio | 2026-04-19 | @programador-java |

### Issues Resueltos

| Issue | Descripción | Estado |
|-------|-------------|--------|
| #BEN-001 | Crear estructura inicial del módulo | ✅ Resuelto |
| #BEN-002 | Implementar CRUD de beneficiarios | ✅ Resuelto |
| #BEN-003 | Validar suma de porcentajes = 100% | ✅ Resuelto |
| #BEN-004 | Límite de 5 beneficiarios por socio | ✅ Resuelto |
| #BEN-005 | Auditoría de cambios | ✅ Resuelto |
| #BEN-006 | Rate limiting | ✅ Resuelto |

---

## Cambios Pendientes

| ID | Descripción | Prioridad | Estado |
|----|-------------|-----------|--------|
| #BEN-010 | Integración con módulo KYC para validación de documentos | Media | ⏳ Pendiente |
| #BEN-011 | Implementar endpoint de historial de cambios (auditoría) | Baja | ⏳ Pendiente |
| #BEN-012 | Agregar métricas de uso para reporting | Baja | ⏳ Pendiente |

---

## Referencias

- Specification: [SPEC.md](./SPEC.md)
- API: [API.md](./API.md)
- Modelo de Datos: [MODELO_DATOS.md](./MODELO_DATOS.md)