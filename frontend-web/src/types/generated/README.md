# Tipos Generados

**Estado:** ✅ Generados automáticamente desde OpenAPI

**Fecha:** 2026-04-22
**Backend:** http://localhost:18080/v3/api-docs

---

## Estadísticas

| Tipo | Cantidad |
|------|----------|
| Models | 94 |
| APIs | 16 |

---

## Modelos Principales

- `CuentaAhorroResponse.ts`
- `MovimientoResponse.ts`
- `SolicitudCreditoResponse.ts`
- `CuotaResponse.ts`
- `BeneficiarioResponseDTO.ts`
- `UsuarioResponse.ts`
- `VerificacionKYCResponse.ts`

---

## APIs Generadas

- `AuthApi.ts`
- `CuentasAhorroApi.ts`
- `CreditosApi.ts`
- `KYCApi.ts`
- `BeneficiariosApi.ts`
- `DocumentosApi.ts`
- `SociosApi.ts`

---

## Cómo regenerar tipos

```bash
# 1. Asegurarse que backend está corriendo
cd infrastructure && docker compose up -d backend

# 2. Esperar ~30 segundos

# 3. Regenerar tipos
cd frontend-web
npm run generate:types
```

---

## Uso

```typescript
import { CuentaAhorroResponse } from '@/types/generated/models';
import { CuentasAhorroApi } from '@/types/generated/apis';

const api = new CuentasAhorroApi();
const cuentas = await api.listarCuentasSocio(socioId);
```

---

## Notas

- Los tipos son **solo lectura**
- Regenerar sobrescribe todos los cambios
- Para tipos frontend específicos (no del backend), usar `src/types/`
