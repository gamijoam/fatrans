# 👥 Guía de Contribución - FATRANS

¡Bienvenido al equipo! Antes de contribuir, lee este documento completo.

---

## 📋 Índice

1. [Flujo de Trabajo](#flujo-de-trabajo)
2. [Conventional Commits](#conventional-commits)
3. [Pull Requests](#pull-requests)
4. [Estándares de Código](#estándares-de-código)
5. [Code Review](#code-review)
6. [Reglas de Protected Branch](#reglas-de-protected-branch)

---

## 🔄 Flujo de Trabajo (Git Flow)

```
main (producción)
 └── develop (desarrollo)
      └── feature/nombre-feature
      └── fix/nombre-fix
      └── hotfix/nombre-hotfix
```

### Ramas permitidas:

| Prefijo | Ejemplo | Uso |
|---------|---------|-----|
| `feature/` | `feature/login-bff` | Nueva funcionalidad |
| `fix/` | `fix/error-deuda` | Corrección de bug |
| `hotfix/` | `hotfix/security-token` | Corrección urgente |
| `refactor/` | `refactor/auth-store` | Refactorización |
| `docs/` | `docs/readme-api` | Documentación |

### Ciclo de vida de una rama:

```bash
# 1. Crear rama desde develop
git checkout develop
git pull origin develop
git checkout -b feature/nombre-descriptivo

# 2. Trabajar y hacer commits
git add .
git commit -m "feat: agregar formulario de login"

# 3. Subir al remoto
git push -u origin feature/nombre-descriptivo

# 4. Crear Pull Request a develop (no a main)
# 5. Esperar approval
# 6. Hacer merge
```

---

## 🏷️ Conventional Commits

Los commits **DEBEN** seguir este formato:

```
<tipo>(<ámbito>): descripción breve

<tipos permitidos>:
├── feat      → Nueva funcionalidad
├── fix       → Corrección de bug
├── docs      → Solo documentación
├── style     → Formato, espacios, punto y coma (no código)
├── refactor  → Refactorización sin cambiar funcionalidad
├── perf      → Mejora de performance
├── test      → Agregar o modificar tests
├── build     → Cambios en build system
├── ci        → Cambios en CI/CD
├── chore     → Mantenimiento general

<ámbitos permitidos>:
├── auth      → Autenticación
├── cuentas   → Módulo de cuentas
├── creditos  → Módulo de créditos
├── kyc       → Verificación de identidad
├── beneficiarios → Módulo de beneficiarios
├── ui        → Componentes de interfaz
├── api       → API client
└── config    → Configuración general
```

### Ejemplos válidos:

```bash
feat(auth): implementar login con cookies httpOnly
fix(cuentas): corregir validación de monto negativo
docs(readme): actualizar instrucciones de setup
refactor(ui): extraer componente Button
style(layout): ajustar espaciado del sidebar
```

### Ejemplos INVÁLIDOS (no hacer):

```bash
git commit -m "fix"                           # ❌ Sin descripción
git commit -m "actualicé cosas"               # ❌ No descriptivo
git commit -m "WIP"                            # ❌ No descriptivo
git commit -m "fix stuff"                      # ❌ No descriptivo
git commit -m "feat: Login Page"              # ❌ Mayúsculas
```

---

## 📝 Pull Requests

### Template de PR:

Al crear un PR, usa este formato en el título:

```
[tipo](ámbitO): descripción corta

Ejemplos:
feat(auth): agregar logout con refresh token
fix(cuentas): corregiroverflowen saldo
```

### Body del PR (describir con:

```
## Resumen
Descripción breve de qué hace este PR.

## Tipo de cambio
- [ ] Nueva funcionalidad
- [ ] Corrección de bug
- [ ] Refactorización
- [ ] Documentación

## Checklist
- [ ] Tests agregados/actualizados
- [ ] Lint pasa sin errores
- [ ] TypeScript compila sin errores
- [ ] Commits siguen conventional commits

## Screenshots (si aplica)
Agregar fotos del cambio visual.
```

### Reglas para crear PR:

1. **Una funcionalidad por PR** - No mezclar cambios no relacionados
2. **Descripción clara** - Explicar el "¿por qué?"
3. **Tests incluidos** - Si agregas funcionalidad, agrega tests
4. **Branch correcta** - PR a `develop`, NO a `main`
5. **Sin conflictos** - Resolver antes de solicitar review

---

## 🎨 Estándares de Código

### TypeScript

```typescript
// ✅ CORRECTO
interface User {
  id: string;
  name: string;
  email: string;
}

const fetchUser = async (id: string): Promise<User> => {
  return apiClient.get(`/users/${id}`);
};

// ❌ INCORRECTO
const fetchUser = async (id) => {
  return apiClient.get('/users/' + id);
};
```

### Nombrado:

| Elemento | Estilo | Ejemplo |
|----------|--------|---------|
| Archivos | kebab-case | `user-profile.tsx` |
| Componentes | PascalCase | `UserProfile` |
| Funciones | camelCase | `getUserData()` |
| Constantes | SCREAMING_SNAKE | `MAX_RETRY_COUNT` |
| Variables | camelCase | `isLoading` |
| Types/Interfaces | PascalCase | `UserResponse` |

### Imports:

```typescript
// ✅ CORRECTO
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/stores/auth-store';
import { User } from '@/types/user';

// ❌ INCORRECTO
import Button from '@/components/ui/button';
import * as stores from '@/stores';
```

### Componentes React:

```typescript
// ✅ CORRECTO
'use client';

import { useState } from 'react';
import { cn } from '@/lib/utils/cn';

interface CardProps {
  title: string;
  children: React.ReactNode;
  className?: string;
}

export function Card({ title, children, className }: CardProps) {
  return (
    <div className={cn('rounded-lg border p-4', className)}>
      <h2>{title}</h2>
      {children}
    </div>
  );
}
```

---

## 👀 Code Review

### Para el autor del PR:

1. **Auto-review antes de solicitar:**
   - Leer el diff completo
   - Verificar que no haya console.logs
   - Confirmar que lint pasa

2. **Solicitar review correctamente:**
   - Asignar reviewers específicos
   - Responder todos los comentarios
   - No hacer force push después de approval

### Para el reviewer:

1. **Revisar con estos criterios:**
   - ¿El código hace lo que dice?
   - ¿Sigue los estándares?
   - ¿Hay tests?
   - ¿Es seguro?

2. **Comentarios:**
   - Ser constructivo
   - Proponer soluciones
   - Marcar como "Resolve" cuando esté corregido

### Aprobar PR:

- Mínimo **1 approval** para hacer merge a develop
- **2 approvals** para hacer merge a main
- El autor puede hacer merge después de approval

---

## 🛡️ Reglas de Protected Branch

### `main` (producción):

```
✅ Reglas activadas:
├── Require pull request before merge
├── Require 2 approvals
├── Dismiss stale reviews
├── Require status checks to pass
├── Require branches to be up to date
└── No force push
```

### `develop`:

```
✅ Reglas activadas:
├── Require pull request before merge
├── Require 1 approval
├── Require status checks to pass
└── No force push
```

### Status checks requeridos:

```bash
npm run lint        # ESLint pasa
npm run typecheck  # TypeScript compila
npm run build      # Build exitoso
```

---

## 🚨 Requisitos para hacer Merge

| Branch | Requirements |
|--------|-------------|
| `main` | 2 approvals + todos los checks verdes |
| `develop` | 1 approval + todos los checks verdes |
| `feature/*` | 1 approval (por implementar) |

---

## 🎯 Checklist antes de commit

```bash
# Antes de hacer git commit:
[ ] ¿Seguí el formato de conventional commits?
[ ] ¿El mensaje es descriptivo?
[ ] ¿Agregué tests si agregué funcionalidad?
[ ] ¿Lint pasa sin errores?
[ ] ¿TypeScript compila?

# Antes de crear PR:
[ ] ¿La rama está actualizada con develop?
[ ] ¿No hay conflictos?
[ ] ¿Todos los checks pasan?
[ ] ¿La descripción es clara?
[ ] ¿Asigné reviewers?
```

---

## 📞 Contacto

Para dudas sobre estas reglas, contactar al Team Lead.

---

**Versión:** 1.0.0
**Última actualización:** 2026-04-22