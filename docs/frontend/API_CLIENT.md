# API Client

## Configuración

El API client está configurado en `src/lib/api/client.ts` usando Axios con interceptors.

## Características

- **Base URL**: Configurable via `NEXT_PUBLIC_API_URL`
- **Timeout**: 10 segundos
- **Credentials**: Envío automático de cookies (para HttpOnly)
- **CSRF Protection**: Token automático en requests modify
- **Retry Logic**: Refresh token automático en 401

## Uso Básico

```typescript
import { apiClient } from '@/lib/api/client';

// GET
const data = await apiClient.get('/v1/resource');

// POST
const result = await apiClient.post('/v1/resource', { campo: 'valor' });

// PUT
await apiClient.put('/v1/resource/1', { campo: 'nuevo' });

// DELETE
await apiClient.delete('/v1/resource/1');
```

## Estructura de Feature API

Cada feature tiene su propio archivo de API:

```typescript
// src/features/mi-modulo/api/mi-modulo.api.ts
import { apiClient } from '@/lib/api/client';
import { MiModuloRequest, MiModuloResponse } from '@/types/api';

export const miModuloApi = {
  list: () => apiClient.get<MiModuloResponse[]>('/v1/mi-modulo'),
  create: (data: MiModuloRequest) => apiClient.post('/v1/mi-modulo', data),
  // ...
};
```

## Interceptors

### Request Interceptor

Añade CSRF token a requests que lo requieren (POST, PUT, DELETE, PATCH):

```typescript
apiClient.interceptors.request.use((config) => {
  if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase() || '')) {
    const csrfToken = Cookies.get('csrf_token');
    if (csrfToken) {
      config.headers['X-CSRF-Token'] = csrfToken;
    }
  }
  return config;
});
```

### Response Interceptor

Maneja errores 401 con refresh token automático:

```typescript
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const newToken = await refreshAccessToken();
        if (newToken) {
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        }
      } catch {
        await logout();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
```

## Endpoints del Backend

| Módulo | Prefijo | Endpoints principales |
|--------|---------|----------------------|
| Auth | `/v1/auth` | login, logout, refresh, me |
| Socios | `/v1/socios` | CRUD, solicitudes |
| Cuentas | `/v1/cuentas` | saldo, depositos, retiros, movimientos |
| Créditos | `/v1/creditos` | tipos, solicitudes, cuotas |
| KYC | `/v1/kyc` | verificacion, documentos |
| Beneficiarios | `/v1/beneficiarios` | CRUD |
| Documentos | `/v1/documentos` | generar, descargar |

## Tipos Generated

Generar tipos desde Swagger:

```bash
npx openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-fetch \
  -o src/types/generated \
  --additional-properties=typescriptSingleModule=true
```

Los tipos se generan en `src/types/generated/` y pueden importarse directamente:

```typescript
import { CuentaAhorroResponse, MovimientoResponse } from '@/types/generated/api';
```
