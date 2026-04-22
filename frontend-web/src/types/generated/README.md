# Tipos Generados

**Estado:** ⚠️ Tipos creados manualmente

Estos tipos fueron creados **manualmente** basado en `docs/base-proyecto/API_CONTRACTS.md`.

El backend no estaba corriendo para generar tipos desde OpenAPI.

---

## Cómo regenerar tipos automáticamente

### 1. Iniciar backend

```bash
cd infrastructure
docker compose up -d backend
# Esperar ~30 segundos
```

### 2. Generar tipos

```bash
cd frontend-web
npm run generate:types
```

### 3. Verificar

```bash
ls -la src/types/generated/
git diff src/types/generated/
```

---

## Cuándo regenerar

| Situación | Acción |
|-----------|--------|
| Backend agrega nuevo endpoint | Regenerar |
| Backend modifica schema | Regenerar |
| Breaking changes | Actualizar código que usa los tipos |

---

## Notas

- Los tipos en `api.ts` son **solo lectura**
- Regenerar sobrescribe todos los cambios manuales
- Para tipos frontend específicos (no del backend), usar `src/types/`