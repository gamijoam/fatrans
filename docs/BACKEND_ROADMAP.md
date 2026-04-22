# 🔧 Hoja de Ruta de Desarrollo Backend — FondoAhorro
**Versión:** 1.0 | **Fecha:** Abril 2026 | **Perspectiva:** Product Manager / Tech Lead

---

## 📈 Estado Actual de los Módulos (Auditoría)

| Módulo | Estado de Lógica | Endpoints | Observaciones |
|--------|------------------|-----------|---------------|
| **Auth** | ✅ 100% | 7 | Funciona login, sesiones y recuperación. Falta email real. |
| **Socios** | ✅ 100% | 12 | CRUD y solicitudes de registro completos. Falta email real. |
| **Ahorros** | ✅ 100% | 12 | Depósitos, retiros y rendimientos listos. Muy sólido. |
| **Créditos** | ✅ 100% | 14 | Evaluación, amortización y pagos listos. El más complejo. |
| **KYC** | ✅ 90% | 6 | Integrado con MinIO. Depende de ClamAV (antivirus). |

---

## 🚨 Brechas Críticas del MVP (Lo que FALTA)

Estos son los puntos que impiden que el backend sea considerado "listo para producción":

1. **Email real (Mocks actuales):** Ambos servicios de email solo imprimen en consola (`log.info`). Sin esto, el usuario no recibe su contraseña ni puede recuperarla.
2. **Dashboard Administrativo API:** No existe un punto de entrada que consolide métricas globales para el administrador (total en caja, socios activos, etc.).
3. **Generación de PDF:** El sistema de documentación (estados de cuenta, contratos) es 0% código actualmente.
4. **Endpoint `/socios/me`:** El frontend no tiene una forma fácil de pedir "mis datos" basándose solo en el token JWT.

---

## 🗺️ Plan de Ejecución Priorizado

### Fase 1: Desbloqueo de Integración (Semana 1)
*Objetivo: Permitir que la App Móvil funcione de punta a punta con datos reales.*

- [ ] **Crear `GET /api/v1/socios/me`:** Endpoint para que el socio vea su perfil tras el login.
- [ ] **Implementar Spring Mail:** Configurar un servidor SMTP real (Gmail, SendGrid, etc.) para reemplazar los logs.
- [ ] **API de Dashboard Admin:** Crear endpoints de agregación para el panel administrativo.
- [ ] **Migraciones reales (Flyway):** Activar Flyway y asegurar que el esquema de base de datos sea inmutable.

### Fase 2: Documentación y Operativa (Semana 2)
*Objetivo: Cumplir con la promesa de "Sistema de Documentación automatizado".*

- [ ] **Generación de PDF:** Implementar una librería (ej. iText o OpenPDF) para generar estados de cuenta mensuales.
- [ ] **Módulo de Beneficiarios:** Permitir que los socios registren quiénes heredarían sus ahorros.
- [ ] **Parámetros Dinámicos:** Mover las tasas de interés y límites del código a una tabla de configuración en la DB.

### Fase 3: Estabilización y Calidad (Semana 3)
*Objetivo: Reducir la deuda técnica y asegurar el futuro del código.*

- [ ] **Tests Unitarios:** Cobertura mínima de los casos de uso de Ahorros y Créditos (Mockito).
- [ ] **Validación Cruzada:** Lógica que bloquee saldo de ahorros si están sirviendo como garantía de un crédito activo.
- [ ] **Auditoría Global:** Sistema de logs inalterables para cumplimiento legal.

---

## 📌 Recomendaciones del PM

1. **No refactorizar Ahorros ni Créditos:** El código es limpio y sigue Clean Architecture. Úsalo como está.
2. **Priorizar el Endpoint `/me`:** Es el principal bloqueador del desarrollador frontend ahora mismo.
3. **Mantener los Mocks de KYC:** Si el servidor de ClamAV o el verificador externo fallan, que el sistema pueda seguir operando en modo manual.

---
*Documento generado para el equipo de desarrollo de FondoAhorro.*
