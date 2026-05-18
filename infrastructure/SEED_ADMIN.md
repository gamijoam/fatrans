# Script de Admin Inicial

## ⚠️ IMPORTANTE: Este script es MANUAL

Este script **NO se ejecuta automáticamente** con Docker Compose. Debe ejecutarse después de que el backend haya creado las tablas de la base de datos.

---

## Pasos para crear el Admin Inicial

### 1. Asegurarse que el backend está corriendo

```bash
cd infrastructure
docker compose up -d
```

Esperar ~30 segundos hasta que el backend esté completamente iniciado.

### 2. Ejecutar el script de seed

```bash
docker exec -i infrastructure-postgres-1 psql -U app -d fondo < seed_admin.sql
```

### 3. Verificar que se creó

```bash
docker exec -i infrastructure-postgres-1 psql -U app -d fondo -c "SELECT id, nombre_usuario, correo_electronico, rol FROM usuarios WHERE nombre_usuario = 'admin';"
```

Debería mostrar:
```
                  id                  | nombre_usuario |         correo_electronico          | rol
----------------------------------------+----------------+---------------------------------------+------
 a1111111-1111-1111-1111-111111111111 | admin          | admin@fondoAhorro.test                | ADMIN
```

### 4. Credenciales del Admin

| Campo | Valor |
|-------|-------|
| Nombre de usuario | `admin` |
| Password | `Admin123!` |
| Rol | `ADMIN` |

---

## Solución de Problemas

### Error: "relation usuarios does not exist"

El backend aún no ha creado las tablas. Esperar más tiempo o verificar que el backend está corriendo sin errores:

```bash
docker logs infrastructure-backend-1 --tail 30
```

### Error: "duplicate key"

El usuario admin ya existe. Esto es normal si ya ejecutaste el script antes.

---

## Notas

- El password `Admin123!` es temporal. En producción debe cambiarse inmediatamente.
- Este script es solo para **desarrollo local**.
- En producción, los admins se crean a través del proceso normal de registro + aprobación.
