# 🎯 Guía de Componentes UI - Loading, Toast y Notificaciones

**Fecha:** 2026-04-22
**Proyecto:** FATRANS
**Módulo:** Frontend - UI Components

---

## 📋 Resumen

Se implementaron componentes de UI para manejo de estados de carga, notificaciones y progreso, mejorando significativamente la experiencia del usuario.

### Componentes Implementados

| Categoría | Componentes |
|-----------|-------------|
| **Skeleton** | Skeleton, SkeletonText, SkeletonCard, SkeletonTable, SkeletonList |
| **Spinner** | Spinner, SpinnerOverlay, LoadingButton |
| **Progress** | ProgressBar, ProgressUpload |
| **Toast** | toastSuccess, toastError, toastInfo, toastWarning, toastPromise |

---

## 🤔 ¿Por qué son importantes?

### El Problema

Sin estos componentes, la UX es mala:

| Situación | UX Problem |
|-----------|------------|
| Carga de datos | Pantalla en blanco o texto "Cargando..." |
| Click en botón | Usuario no sabe si apretó |
| Upload de archivo | Sin feedback del progreso |
| Error | Alert nativo del navegador (feo) |

### La Solución

```
┌─────────────────────────────────────────────────────┐
│  🔄 Spinner mientras carga                           │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  ████████████░░░░░░░░░  45%               │   │ ← Progress bar
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  ○  ████████████████████████                │   │ ← Skeleton
│  │     ████████████████████████                │   │
│  │     ████████████████████████                │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  ✅ ¡Operación exitosa!                    [TOAST]│
└─────────────────────────────────────────────────────┘
```

---

## 📁 Estructura de Archivos

```
frontend-web/src/components/ui/
├── skeleton.tsx       # 5 componentes de skeleton
├── spinner.tsx        # 3 componentes de spinner
├── progress.tsx      # 2 componentes de progress
├── toast-helpers.ts  # 7 funciones de toast
└── index.ts         # Exports centralizados
```

---

## 🚀 Cómo Usar los Componentes

### 1. Skeleton Components

Muestra placeholders mientras los datos cargan.

```tsx
import { Skeleton, SkeletonText, SkeletonCard, SkeletonTable, SkeletonList } from '@/components/ui';

// Skeleton base
<Skeleton className="h-4 w-32" />

// Texto con múltiples líneas
<SkeletonText lines={4} />

// Card de perfil
<SkeletonCard />

// Tabla con filas
<SkeletonTable rows={5} />

// Lista de cards
<SkeletonList items={3} />
```

**Ejemplo en una lista de cuentas:**

```tsx
export function CuentasList() {
  const { data: cuentas, isLoading } = useCuentas();

  if (isLoading) {
    return <SkeletonList items={5} />;
  }

  return (
    <div>
      {cuentas?.map(cuenta => (
        <CuentaCard key={cuenta.numeroCuenta} cuenta={cuenta} />
      ))}
    </div>
  );
}
```

---

### 2. Spinner Components

Feedback visual para acciones en progreso.

```tsx
import { Spinner, SpinnerOverlay, LoadingButton } from '@/components/ui';

// Spinner básico con 3 tamaños
<Spinner size="sm" />  // Pequeño
<Spinner size="md" /> // Mediano (default)
<Spinner size="lg" /> // Grande

// Overlay para toda la página
{isLoading && <SpinnerOverlay />}

// Botón con estado de loading
<LoadingButton
  isLoading={isSubmitting}
  onClick={handleSubmit}
  className="bg-blue-600 text-white px-4 py-2 rounded"
>
  Guardar
</LoadingButton>
```

**Ejemplo en formulario:**

```tsx
<form onSubmit={handleSubmit}>
  <input {...register('nombre')} />
  <LoadingButton
    type="submit"
    isLoading={isSubmitting}
    className="w-full"
  >
    {isSubmitting ? 'Guardando...' : 'Guardar'}
  </LoadingButton>
</form>
```

---

### 3. Progress Bar

Muestra progreso de operaciones largas.

```tsx
import { ProgressBar, ProgressUpload } from '@/components/ui';

// Barra simple
<ProgressBar value={45} />

// Con porcentaje visible
<ProgressBar value={75} showLabel />

// Variantes de color
<ProgressBar value={100} variant="success" /> // Verde
<ProgressBar value={100} variant="error" />   // Rojo
<ProgressBar value={50} variant="default" />  // Azul (default)

// Para uploads
<ProgressUpload
  filename="documento.pdf"
  progress={progress}
  status="uploading"
  onCancel={handleCancel}
/>
```

**Ejemplo para upload de documentos:**

```tsx
const [uploadProgress, setUploadProgress] = useState(0);
const [status, setStatus] = useState<'uploading' | 'complete' | 'error'>('uploading');

<ProgressUpload
  filename={file.name}
  progress={uploadProgress}
  status={status}
  onCancel={() => setUploadProgress(0)}
/>
```

---

### 4. Toast Notifications

Notificaciones discretas para feedback de operaciones.

```tsx
import { toastSuccess, toastError, toastInfo, toastWarning, toastPromise } from '@/components/ui';

// Éxito (verde)
toastSuccess('Registro guardado', 'Fue redirigido al dashboard');

// Error (rojo)
toastError('Error al guardar', 'Intente nuevamente');

// Información (azul)
toastInfo('Sesión expira en 5 minutos');

// Advertencia (amarillo)
toastWarning('Límite de caracteres excedido');

// Con promise (loading → success/error automático)
const promise = fetch('/api/create', { method: 'POST', body: JSON.stringify(data) });
toastPromise(promise, {
  loading: 'Guardando...',
  success: '¡Guardado!',
  error: 'Error al guardar',
});
```

**Ejemplo completo con React Query mutation:**

```tsx
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toastSuccess, toastError } from '@/components/ui';

function CreateBeneficiarioForm() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (data) => api.createBeneficiario(data),
    onSuccess: () => {
      toastSuccess('Beneficiario creado', 'Fue agregado a la lista');
      queryClient.invalidateQueries({ queryKey: ['beneficiarios'] });
    },
    onError: (error) => {
      toastError('Error al crear beneficiario', error.message);
    },
  });

  return (
    <form onSubmit={mutation.mutate}>
      {/* ... */}
    </form>
  );
}
```

---

## ✅ Mejores Prácticas

### Cuándo usar Skeleton

| Usar Skeleton ✓ | No usar Skeleton ✗ |
|-----------------|-------------------|
| Datos que tardan >300ms | Datos instantáneos |
| Listas y tablas | Contenido estático |
| Cards y perfiles | Errores |
| Dashboard stats | Footer/Header |

### Cuándo usar Spinner

| Usar Spinner ✓ | No usar Spinner ✗ |
|-----------------|-------------------|
| Botón de submit | En línea de texto |
| Acciones de guardar | Indicadores de estado |
| Carga de página completa | Skeletons ya visibles |

### Cuándo usar Toast

| Usar Toast ✓ | No usar Toast ✗ |
|--------------|-----------------|
| Éxito/Error de operación | Errores críticos (usar modal) |
| Confirmaciones | Acciones irreversibles (usar confirmación) |
| Warnings | Información importante (usar banner) |

---

## 🎨 Clases de Toast por Tipo

Los toasts usan clases de colores automáticamente:

| Tipo | Color | Clase CSS |
|------|-------|-----------|
| Success | Verde | `bg-green-50 border-green-200 text-green-800` |
| Error | Rojo | `bg-red-50 border-red-200 text-red-800` |
| Warning | Amarillo | `bg-yellow-50 border-yellow-200 text-yellow-800` |
| Info | Azul | `bg-blue-50 border-blue-200 text-blue-800` |
| Loading | Gris | `bg-gray-50 border-gray-200 text-gray-800` |

---

## 🔄 Integración con React Query

Los componentes están diseñados para trabajar con TanStack Query:

```tsx
// Patrón estándar
const { data, isLoading, isError, error } = useQuery({
  queryKey: ['recurso'],
  queryFn: fetchRecurso,
});

// Loading → Skeleton
// Error → Toast + Mensaje de error
// Success → Contenido
```

```tsx
const mutation = useMutation({
  mutationFn: crearRecurso,
  onSuccess: () => toastSuccess('Creado'),
  onError: () => toastError('Error'),
});
```

---

## 📝 Changelog

| Fecha | Descripción |
|-------|-------------|
| 2026-04-22 | Implementación inicial (5 skeleton, 3 spinner, 2 progress, 7 toast helpers) |

---

## 🎓 Recursos

- [Sonner - Toast library](https://sonner.emortwick.dev/)
- [Tailwind CSS Animation](https://tailwindcss.com/docs/animation)
- [TanStack Query - Loading States](https://tanstack.com/query/latest/docs/react/guides/loading-states)
