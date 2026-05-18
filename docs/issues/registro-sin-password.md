# ISSUE: Flujo de Contraseña en Registro de Socios

**Fecha:** 2026-04-23
**Tipo:** Diseño / UX
**Etiquetas:** `registration`, `password`, `ux`

---

## Situación Actual

El formulario de registro (`/registro`) **no solicita contraseña** al usuario.

### Flujo actual:
1. Usuario llena formulario de registro (nombre, cédula, email, teléfono, empresa)
2. Se crea `SolicitudRegistro` con estado `PENDIENTE`
3. Admin revisa y **aprueba** la solicitud
4. Sistema genera:
   - Usuario con nombre generado (ej: `juan.perez`)
   - Password temporal aleatorio (12 caracteres)
5. Email enviado con credenciales al socio

### Código relevante:
- **Frontend:** `registration-form.tsx` - no tiene campo password
- **Backend:** `AprobarSolicitudUseCase.generarPasswordTemporal()` genera password

---

## Decisión de Diseño Pendiente

### Opción A: Mantener flujo actual (sin password en registro)
- Usuario no elige password durante registro
- Admin approves → sistema genera credenciales
- Email enviado con username y password temporal
- **Ventaja:** Simple, seguro (password fuerte generado por sistema)
- **Desventaja:** Usuario no elige su propia password

### Opción B: Usuario elige password durante registro
- Agregar campo `password` al formulario de registro
- Validar requisitos (8+ chars, mayúscula, número, especial)
- Al aprobar, usar esa password en lugar de generar temporal
- **Ventaja:** Usuario elige su password
- **Desventaja:** Password podría ser débil, mayor complejidad

### Opción C: Usuario elige password + verificar email primero
- Opción B + verificar email antes de permitir registro
- Token de verificación enviado por email
- **Ventaja:** Email válido confirmado
- **Desventaja:** Más complejo, mayor fricción

---

## Requisitos de Password (Backend)

El backend valida password con regex:
```java
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$
// Mínimo 8 chars, 1 mayúscula, 1 número, 1 especial
```

---

## Screenshots/Referencias

- Formulario registro: `frontend-web/src/components/features/auth/registration-form.tsx`
- Aprobar solicitud: `backend/src/main/java/.../AprobarSolicitudUseCase.java`

---

## Recomendación

**Opción A (recomendada para MVP):** Mantener flujo actual.
- Más simple de implementar
- Passwords más seguros (generados por sistema)
- Flujo ya funcional

Si se elige cambiar, la tarea sería:
1. Modificar `registration-form.tsx` para agregar campo password
2. Modificar `SolicitudRegistroRequestDTO` para incluir password
3. Modificar `CrearSolicitudRegistroUseCase` para guardar password encriptada
4. Modificar `AprobarSolicitudUseCase` para usar password del socio en lugar de generar

---

## Estado

**ABIERTO** - Pendiente decisión de diseño