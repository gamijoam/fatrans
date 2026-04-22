# Guía de Desarrollo

## Agregar un Nuevo Módulo

### Paso 1: Crear Estructura

```bash
mkdir -p src/features/{mi-modulo}/{api,components,hooks,schemas,types}
```

### Paso 2: Estructura de Archivos

```
src/features/mi-modulo/
├── api/
│   └── mi-modulo.api.ts      # Llamadas HTTP
├── components/
│   ├── mi-modulo-list.tsx    # Lista
│   ├── mi-modulo-form.tsx     # Formulario
│   └── mi-modulo-detail.tsx  # Detalle
├── hooks/
│   └── use-mi-modulo.ts      # React Query hooks
├── schemas/
│   └── mi-modulo.schemas.ts  # Zod validations
├── types/
│   └── mi-modulo.types.ts    # Tipos específicos
└── index.ts                  # Exportaciones
```

### Paso 3: API Layer

```typescript
// src/features/mi-modulo/api/mi-modulo.api.ts
import { apiClient } from '@/lib/api/client';
import { MiModuloRequest, MiModuloResponse } from '@/types/api';

export const miModuloApi = {
  list: () => apiClient.get<MiModuloResponse[]>('/v1/mi-modulo'),

  create: (data: MiModuloRequest) =>
    apiClient.post<MiModuloResponse>('/v1/mi-modulo', data),

  getById: (id: string) =>
    apiClient.get<MiModuloResponse>(`/v1/mi-modulo/${id}`),

  update: (id: string, data: Partial<MiModuloRequest>) =>
    apiClient.put<MiModuloResponse>(`/v1/mi-modulo/${id}`, data),

  delete: (id: string) =>
    apiClient.delete(`/v1/mi-modulo/${id}`),
};
```

### Paso 4: Hooks con React Query

```typescript
// src/features/mi-modulo/hooks/use-mi-modulo.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { miModuloApi } from '../api/mi-modulo.api';
import { toast } from 'sonner';

export function useMiModulos() {
  return useQuery({
    queryKey: ['mi-modulos'],
    queryFn: miModuloApi.list,
    staleTime: 30_000, // 30 segundos
  });
}

export function useMiModulo(id: string) {
  return useQuery({
    queryKey: ['mi-modulo', id],
    queryFn: () => miModuloApi.getById(id),
    enabled: !!id,
  });
}

export function useCreateMiModulo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: miModuloApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mi-modulos'] });
      toast.success('Creado exitosamente');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    },
  });
}

export function useUpdateMiModulo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) =>
      miModuloApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mi-modulos'] });
      toast.success('Actualizado exitosamente');
    },
  });
}

export function useDeleteMiModulo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: miModuloApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mi-modulos'] });
      toast.success('Eliminado exitosamente');
    },
  });
}
```

### Paso 5: Schemas de Validación

```typescript
// src/features/mi-modulo/schemas/mi-modulo.schemas.ts
import { z } from 'zod';

export const miModuloSchema = z.object({
  campo1: z.string().min(3, 'Mínimo 3 caracteres'),
  campo2: z.number().positive('Debe ser positivo'),
  campo3: z.enum(['OPCION_A', 'OPCION_B', 'OPCION_C']),
});

export type MiModuloFormData = z.infer<typeof miModuloSchema>;
```

### Paso 6: Componente Lista

```typescript
// src/features/mi-modulo/components/mi-modulo-list.tsx
'use client';

import { useMiModulos, useDeleteMiModulo } from '../hooks/use-mi-modulo';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Loader2, Trash2 } from 'lucide-react';

export function MiModuloList() {
  const { data, isLoading, error } = useMiModulos();
  const deleteMutation = useDeleteMiModulo();

  if (isLoading) {
    return <Loader2 className="animate-spin" />;
  }

  if (error) {
    return <div className="text-destructive">Error: {error.message}</div>;
  }

  return (
    <div className="grid gap-4">
      {data?.map((item) => (
        <Card key={item.id}>
          <CardHeader>
            <CardTitle>{item.nombre}</CardTitle>
          </CardHeader>
          <CardContent>
            <p>{item.descripcion}</p>
            <Button
              variant="destructive"
              size="sm"
              onClick={() => deleteMutation.mutate(item.id)}
              disabled={deleteMutation.isPending}
            >
              <Trash2 className="w-4 h-4" />
            </Button>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
```

### Paso 7: Registrar Ruta

```typescript
// src/app/(dashboard)/mi-modulo/page.tsx
import { MiModuloList } from '@/features/mi-modulo/components/mi-modulo-list';

export default function MiModuloPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Mi Módulo</h1>
      </div>
      <MiModuloList />
    </div>
  );
}
```

---

## Agregar Nuevo Component UI

```bash
# Ubicación: src/components/ui/
# Crear archivo con nombre descriptivo
```

```typescript
// src/components/ui/mi-componente.tsx
import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils/cn';

const miComponenteStyles = cva(
  'rounded-lg border bg-card text-card-foreground shadow-sm',
  {
    variants: {
      variant: {
        default: '',
        secondary: 'bg-secondary text-secondary-foreground',
      },
      size: {
        default: 'p-4',
        sm: 'p-2',
        lg: 'p-6',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  }
);

export interface MiComponenteProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof miComponenteStyles> {}

const MiComponente = React.forwardRef<HTMLDivElement, MiComponenteProps>(
  ({ className, variant, size, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(miComponenteStyles({ variant, size, className }))}
        {...props}
      />
    );
  }
);
MiComponente.displayName = 'MiComponente';

export { MiComponente, miComponenteStyles };
```

---

## Patrones Comunes

### Uso de Forms

```typescript
// Usar con React Hook Form + Zod
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { miModuloSchema, MiModuloFormData } from '../schemas/mi-modulo.schemas';

export function MiForm() {
  const form = useForm<MiModuloFormData>({
    resolver: zodResolver(miModuloSchema),
    defaultValues: { campo1: '', campo2: 0 },
  });

  const onSubmit = (data: MiModuloFormData) => {
    // Llamar mutation
  };

  return (
    <form onSubmit={form.handleSubmit(onSubmit)}>
      <input {...form.register('campo1')} />
      <input type="number" {...form.register('campo2', { valueAsNumber: true })} />
      <Button type="submit">Enviar</Button>
    </form>
  );
}
```

### Uso de Dinero

```typescript
import { Money, parseMoney } from '@/lib/utils/money';

// Crear desde número
const monto = Money.fromNumber(1000.50);

// Crear desde string (input de usuario)
const monto2 = parseMoney('1,234.56');

// Operaciones seguras
const total = monto.add(monto2);
const formatted = total.format('es-VE', 'VES'); // "Bs. 2.234,06"

// Comparaciones
if (monto.isGreaterThan(monto2)) { /* ... */ }

// Validación de rango
const validate = validateMoneyRange(amount, 0.01, 500000);
if (!validate.valid) throw new Error(validate.error);
```
