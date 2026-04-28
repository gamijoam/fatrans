# 🚀 TuFondo: Plan Maestro de Estructura y Mejora Integral (v2026)

Este documento unifica la visión estratégica, técnica y de diseño para convertir a **TuFondo** en la plataforma financiera líder del sector transporte en Venezuela.

---

## 1. Visión y Filosofía de Producto
**"TuFondo no es un banco; es el respaldo que los bancos no te dan."**
- **Socio Objetivo:** Transportistas venezolanos (choferes, propietarios, mecánicos).
- **Promesa de Valor:** Soluciones financieras que entienden el recaudo diario, la necesidad de repuestos inmediata y la falta de historial crediticio formal.

---

## 2. Los 6 Pilares del Sistema (Estructura de Módulos)
El proyecto se organiza en 6 pilares innegociables que deben reflejarse tanto en el código (Backend/Frontend) como en la base de datos:

1.  **Identidad y Confianza (Identity):** Auth, KYC (Cédula/RIF/Licencia), Aval Grupal y Score Crediticio Alternativo.
2.  **Ahorro Adaptado (Savings):** Cuentas VES/USD, Microahorro automático (% del recaudo) y Ahorro Programado (Metas como "Nuevos Cauchos").
3.  **Crédito Express (Credit):** Créditos de emergencia (24h para repuestos), Créditos SOAT/Seguro y Financiamiento de Unidades.
4.  **Gestión de Transporte (Transport):** Perfil de la Unidad (Placa/Marca/Año), Gestión de Rutas, Turnos y Registro de Recaudos Diarios.
5.  **Protección y Bienestar (Protection):** Gestión de Beneficiarios, Fondo de Solidaridad Grupal (Ayuda mutua) y Seguros Colectivos.
6.  **Administración y Control (Admin):** Dashboard de KPIs, Gestión de Riesgos, Auditoría Inmutable y Configuración de Parámetros.

---

## 3. Design System "Estándar Bancario"
La UI debe proyectar solidez. Se adopta la **Regla de Oro del Color**:
- **Verde Financiero (#2E7D32):** Solo para dinero a favor, éxitos y confirmaciones.
- **Azul Marino (#1A3C6E):** Autoridad, navegación y headers.
- **Rojo Urgencia (#B71C1C):** Obligaciones pendientes, deudas o errores críticos.
- **Tipografía:** *Inter* para todo el sistema (claridad y legibilidad).

### Componentes Clave:
- **Account Card:** Debe mostrar saldo VES (grande) y equivalente USD (pequeño) con tasa BCV del día.
- **Stepper de Onboarding:** Registro progresivo en 5 pasos para no abrumar al socio.

---

## 4. Stack Tecnológico y Arquitectura
- **Backend:** Spring Boot 3.2.4 + Java 21 (Robustez transaccional).
- **Frontend Web:** Next.js 14.2.0 + Tailwind CSS (SEO y SSR para portal administrativo y de socios).
- **App Móvil:** Flutter 3.19 (Experiencia nativa para el chofer en la ruta).
- **Infraestructura:** PostgreSQL 15, Redis 7 (Sesiones), RabbitMQ (Colas de crédito), MinIO (Documentos KYC).

---

## 5. Diferenciadores Funcionales (Prioridad de Implementación)
1.  **Crédito de Emergencia 24h:** Aprobación automática basada en el saldo de ahorro (colateral) y score interno.
2.  **Ahorro por Meta:** Visualización del progreso hacia un objetivo físico (ej. "Motor nuevo").
3.  **Perfil de Unidad:** Vincular al socio con su vehículo para ofrecer productos específicos.
4.  **Integración WhatsApp:** Notificaciones críticas de seguridad y movimientos directamente al celular (Canal #1 en Venezuela).

---

## 6. Estándares de Seguridad
- **JWT con HS384:** Tokens de acceso cortos (15 min) y Refresh Tokens seguros en `httpOnly` cookies.
- **Auditoría Inmutable:** Registro de cada acción (quién, qué, cuándo, desde dónde) sin posibilidad de borrado.
- **Protección Anti-Fraude:** Límites geográficos de login, bloqueo automático tras 5 intentos fallidos y PIN de pánico.

---

## 7. Roadmap Estratégico
- **Fase 1 (MVP):** Estabilizar Onboarding, Ahorro básico y KYC.
- **Fase 2 (Diferenciadores):** Implementar Crédito Express, Ahorro por Metas y Gestión de Unidades.
- **Fase 3 (Escala):** Integración completa con WhatsApp, App Móvil v1 y Módulo de Solidaridad.

---
*Este documento es la única fuente de verdad para el desarrollo de TuFondo.*
