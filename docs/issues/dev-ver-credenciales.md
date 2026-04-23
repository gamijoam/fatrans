# ISSUE: Ver credenciales en Desarrollo (Email Mock)

**Fecha:** 2026-04-23
**Tipo:** Desarrollo / DevOps
**Etiquetas:** `development`, `devtools`

---

## Problema

En desarrollo local, el servicio de email está mockeado. Cuando se aprueba una solicitud, las credenciales se "envían" pero no llegan a ningún lado.

**Consecuencia:** No hay forma de saber qué password se generó para el nuevo usuario.

---

## Solución Temporal Actual

Se agregó un log en `AprobarSolicitudUseCase`:

```java
log.info("[DEV] Credenciales -> Email: {}, Usuario: {}, Password: {}",
        email, username, passwordTemporal);
```

**Ver logs:**
```bash
docker compose logs -f backend | grep DEV
```

---

## Alternativas Consideradas

| Opción | Descripción | Estado |
|--------|-------------|--------|
| Log en consola | Ya implementado | ✅ Hecho |
| Guardar en DB | Tabla `credenciales_dev` temporal | Pendiente |
| Email real | Configurar SMTP real | Pendiente |
| Dashboard admin | Ver credenciales desde UI | Pendiente |

---

## Antes de Producción

**REMOVER** el log `[DEV]` antes de desplegar a producción por seguridad.

```java
// En AprobarSolicitudUseCase.java línea ~103
if (log.isDebugEnabled()) {
    log.debug("[DEV] Credenciales -> Email: {}, Usuario: {}, Password: {}",
            email, username, passwordTemporal);
}
```

O remover completamente.

---

## Estado

**PARCIAL** - Log temporal implementado, pero debe removerse en producción.