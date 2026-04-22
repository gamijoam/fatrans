<!-- FILE: docs/README.md -->
# Fondo de Ahorro - Plataforma Digital Backend

## Descripción del Proyecto

Plataforma digital para la administración de Fondos de Ahorro, diseñada para automatizar procesos de ahorros, créditos, generación de documentación (PDFs) y notificaciones a socios. El sistema está diseñado para escalar desde cientos hasta 20,000 usuarios sin refactorizar el núcleo.

La arquitectura está pensada bajo la filosofía de **"empezar ligero, validar rápido y escalar sin límites"**, cumpliendo con las normativas venezolanas de protección de datos y estándares financieros.

---

## Objetivos del Proyecto

- **Digitalización Integral:** Centralizar la información de socios, ahorros y créditos en una arquitectura segura y accesible.
- **Automatización Operativa:** Eliminar errores manuales mediante el cálculo automático de intereses, tablas de amortización y generación de documentos legales.
- **Escalabilidad Masiva:** Diseñar un sistema capaz de soportar desde un grupo inicial hasta comunidades de 10,000 a 20,000 afiliados sin reconstruir el núcleo.
- **Transparencia y Autoservicio:** Proporcionar a los socios un portal dedicado para consulta de saldos y movimientos en tiempo real.

---

## Alcance Funcional y Módulos

### Fase 1 - MVP (Sistema Base)
| Módulo | Descripción |
|--------|-------------|
| **Gestión de Socios** | Expediente digital completo, registro de beneficiarios y estatus de membresía. |
| **Gestión de Ahorros** | Registro automatizado de aportes, consulta de historiales y cálculo de rendimientos. |
| **Sistema de Documentación** | Generación automática de contratos, pagarés y estados de cuenta en PDF. |
| **Panel Administrativo** | Dashboard con métricas clave y control de parámetros operativos. |

### Fases Posteriores
- **Gestión de Créditos:** Solicitudes en línea con validación de elegibilidad y tablas de amortización.
- **Validación de Identidad (KYC):** Integración con servicios de consulta (ej. SAIME) para garantizar veracidad.
- **Seguridad Biométrica:** Autenticación mediante huella dactilar para operaciones críticas.
- **Ecosistema Marketplace:** Espacio para que aliados comerciales ofrezcan productos a los afiliados.

---

## Seguridad y Cumplimiento

- **Protección de Datos:** Autenticación JWT y cifrado de datos en reposo y tránsito.
- **Auditoría:** Registro (Logs) detallado e inalterable de todas las operaciones (¿Quién hizo qué y cuándo?).
- **Marco Legal:** Alineado con la **Ley de Protección de Datos Personales (LOPDP)** y lineamientos de **SUDEBAN/SUDECA**.

---

## Frontend Flutter

### Guía de Inicio Rápido
📄 **[GUIA_INICIO_RAPIDO_FLUTTER.md](frontend/GUIA_INICIO_RAPIDO_FLUTTER.md)** - Cómo ejecutar Flutter Web

### Documentación Técnica
- [ARQUITECTURA_FRONTEND.md](frontend/ARQUITECTURA_FRONTEND.md)
- [FLUTTER_ARCHITECTURE_GUIDE.md](frontend/FLUTTER_ARCHITECTURE_GUIDE.md)

### Ejecución Rápida

```bash
# Flutter Web (siempre usar puerto 18081 para CORS)
cd frontend-mobile
flutter run -d chrome --release --web-port 18081
```

---

## Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Lenguaje** | Java | 21 |
| **Framework** | Spring Boot | 3.2.4 |
| **Base de Datos** | PostgreSQL | 15 |
| **Caché** | Redis | 7 |
| **Frontend App** | Flutter | 3.x |
| **Frontend Landing** | Astro | - |
| **Documentación API** | OpenAPI/Swagger | 2.5.0 |

---

## Estrategia de Implementación

1. **Fase 1 (MVP):** Sistema base funcional. Plazo: 2-3 semanas. Inversión: $400 USD.
2. **Fase 2 (Optimización):** Reportes avanzados, créditos y sistema de notificaciones.
3. **Fase 3 (Expansión):** Biometría, Marketplace y escalabilidad de alto volumen.

---

## Requisitos Previos

### Java 21