# 🎯 Guía de Tipos Generados - OpenAPI

**Fecha:** 2026-04-22
**Proyecto:** FATRANS
**Módulo:** Frontend - API Integration

---

## 📋 Resumen

Se generaron **94 modelos TypeScript** y **16 APIs** desde el backend real mediante OpenAPI.

### Estadísticas

| Tipo | Cantidad |
|------|----------|
| Modelos | 94 |
| APIs | 16 |
| Endpoints cubiertos | 71 |

---

## 🤔 ¿Qué son los Tipos Generados?

Son archivos TypeScript que definen la forma exacta de los datos que intercambian frontend y backend.

### Ejemplo

Cuando el backend devuelve una cuenta de ahorro:

```json
{
  "numeroCuenta": "FA-2026-000001",
  "saldo": 5000.00,
  "estado": "ACTIVA"
}
```

El tipo generado sería:

```typescript
// src/types/generated/models/CuentaAhorroResponse.ts
export interface CuentaAhorroResponse {
  numeroCuenta: string;
  saldo: number;
  estado: 'ACTIVA' | 'BLOQUEADA' | 'CERRADA';
  // ... más campos
}
```

---

## ✅ Beneficios para el Desarrollador

### 1. **Type Safety (Seguridad de Tipos)**

```typescript
// ❌ ANTES: Error solo en runtime (cuando el usuario ve el bug)
const saldo = response.data.saldo;

// ✅ AHORA: Error en compile time (antes de ejecutar)
const saldo: number = response.data.saldo;
```

### 2. **Autocompletado en tu IDE**

```typescript
response.data. // ← Tu IDE te muestra TODOS los campos disponibles
```

### 3. **Documentación Automática**

El tipo mismo es la documentación. No necesitas consultar Swagger.

### 4. **Refactoring Seguro**

Cambias un campo y TypeScript te muestra todos los lugares que lo usan.

---

## 📁 Estructura de Archivos

```
frontend-web/src/types/generated/
├── apis/                    # Funciones de API
│   ├── AuthApi.ts          # Login, logout, refresh
│   ├── CuentasAhorroApi.ts # Cuentas, depósitos, retiros
│   ├── CreditosApi.ts      # Créditos, simulaciones
│   ├── KYCApi.ts           # Verificación de identidad
│   ├── BeneficiariosApi.ts  # Beneficiarios
│   └── DocumentosApi.ts    # PDFs, estados de cuenta
├── models/                 # Tipos de datos
│   ├── CuentaAhorroResponse.ts
│   ├── MovimientoResponse.ts
│   ├── UsuarioDTO.ts
│   └── ... (92 más)
└── runtime.ts              # Configuración interna
```

---

## 🚀 Cómo Usar los Tipos

### Ejemplo 1: Obtener Cuentas

```typescript
import { CuentasAhorroApi } from '@/types/generated/apis';
import { CuentaAhorroResponse } from '@/types/generated/models';

const api = new CuentasAhorroApi();

async function getMisCuentas(socioId: string) {
  const response = await api.listarCuentasSocio(socioId);
  const cuentas: CuentaAhorroResponse[] = response.content;

  cuentas.forEach(cuenta => {
    console.log(`${cuenta.numeroCuenta}: ${cuenta.saldo}`);
  });
}
```

### Ejemplo 2: Hacer un Depósito

```typescript
import { CuentasAhorroApi } from '@/types/generated/apis';
import { DepositoRequest } from '@/types/generated/models';

const api = new CuentasAhorroApi();

async function hacerDeposito(numeroCuenta: string, monto: number) {
  const request: DepositoRequest = {
    monto: monto
  };

  const response = await api.realizarDeposito(numeroCuenta, request);

  // TypeScript sabe que response tiene:
  // - numeroCuenta
  // - monto
  // - nuevoSaldo
  // - referencia
  // - fecha

  console.log(`Depósito exitoso. Nuevo saldo: ${response.nuevoSaldo}`);
}
```

### Ejemplo 3: Solicitud de Crédito

```typescript
import { CreditosApi } from '@/types/generated/apis';
import { CrearSolicitudCreditoRequest } from '@/types/generated/models';

const api = new CreditosApi();

async function solicitarCredito(
  tipoCreditoId: string,
  monto: number,
  plazoMeses: number
) {
  const request: CrearSolicitudCreditoRequest = {
    tipoCreditoId: tipoCreditoId,
    montoSolicitado: monto,
    plazoMeses: plazoMeses,
    proposito: 'Compra de vehículo'
  };

  const solicitud = await api.crearSolicitud(solicitud);

  console.log(`Solicitud creada: ${solicitud.numeroSolicitud}`);
}
```

---

## 🔄 Cómo Regenerar los Tipos

Cuando el backend cambia (nuevos endpoints o campos), regenera los tipos:

### Paso 1: Asegurarse que el backend está corriendo

```bash
cd infrastructure
docker compose up -d backend
# Esperar ~30 segundos
```

### Paso 2: Regenerar tipos

```bash
cd frontend-web
npm run generate:types
```

### Paso 3: Verificar cambios

```bash
git diff src/types/generated/
```

### Paso 4: Commit y PR

```bash
git add src/types/generated/
git commit -m "feat(api): regenerar tipos desde backend"
```

---

## ⚠️ Notas Importantes

### Los tipos son **SOLO LECTURA**

No edites los archivos en `src/types/generated/` directamente. Se sobrescriben cuando regeneras.

### Si necesitas tipos custom

Para tipos frontend específicos (UI state, formularios locales), usa:

```
src/types/
├── api/           # Tipos generados (no editar)
└── local/         # Tus tipos custom
```

### Errores después de regenerar

Si regeneras y TypeScript marca errores:

1. **Campo eliminado del backend** → El frontend ya no lo usa (correcto)
2. **Campo renombrado** → Actualizar el código frontend
3. **Nuevo campo requerido** → TypeScript te lo marca para que lo agregues

---

## 🎓 Recursos

- [Documentación de OpenAPI Generator](https://openapi-generator.tech/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [TanStack Query + TypeScript](https://tanstack.com/query/latest/docs/react/typescript)

---

## 📝 Changelog

| Fecha | Descripción |
|-------|-------------|
| 2026-04-22 | Tipos generados desde backend real (94 models, 16 APIs) |
