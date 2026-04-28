# Sistema de Diseño - Fondo de Ahorro

**Proyecto:** FATRANS
**Versión:** 1.0
**Fecha:** 2026-04-21

---

## 1. Filosofía de Diseño

### 1.1 Principios
- **Profesional y Confiable:** Transmite seguridad financiera
- **Limpio y Minimalista:** Fácil de usar, sin distracciones
- **Accessible:** WCAG AA compliance
- **Responsive:** Mobile-first approach

### 1.2 Identidad Visual
El diseño debe reflejar los valores de una institución de ahorro:
- Confianza
- Estabilidad
- Modernidad
- Accesibilidad

---

## 2. Paleta de Colores

### 2.1 Colores Principales (definidos por el usuario)

| Color | Hex | RGB | Uso |
|-------|-----|-----|-----|
| Blanco | `#FFFFFF` | rgb(255,255,255) | Fondo principal |
| Verde claro | `#90EE90` | rgb(144,238,144) | Acentos, éxito, dinero |
| Azul claro | `#ADD8E6` | rgb(173,216,230) | Secundario, información |

### 2.2 Colores Extendidos (basados en los principales)

#### Verde (Acentos - Success)
| Nombre | Hex | Uso |
|--------|-----|-----|
| Green-50 | `#ECFDF5` | Background success |
| Green-100 | `#D1FAE5` | Border success |
| Green-500 | `#10B981` | Text success |
| Green-600 | `#059669` | Solid button |
| Green-700 | `#047857` | Solid button hover |
| Green-900 | `#064E3B` | Text emphasis |

#### Azul (Secundario - Info)
| Nombre | Hex | Uso |
|--------|-----|-----|
| Blue-50 | `#EFF6FF` | Background info |
| Blue-100 | `#DBEAFE` | Border info |
| Blue-500 | `#3B82F6` | Text info |
| Blue-600 | `#2563EB` | Solid button |
| Blue-700 | `#1D4ED8` | Solid button hover |
| Blue-900 | `#1E3A8A` | Text emphasis |

#### Neutral (Texto - Fondo)
| Nombre | Hex | Uso |
|--------|-----|-----|
| Gray-50 | `#F9FAFB` | Background subtle |
| Gray-100 | `#F3F4F6` | Background muted |
| Gray-200 | `#E5E7EB` | Border |
| Gray-300 | `#D1D5DB` | Border emphasis |
| Gray-500 | `#6B7280` | Text muted |
| Gray-700 | `#374151` | Text |
| Gray-900 | `#111827` | Text emphasis |

#### Semánticos
| Color | Hex | Uso |
|-------|-----|-----|
| Success | `#10B981` | Éxito, aprobado, dinero |
| Warning | `#F59E0B` | Precaución, pendiente |
| Error | `#EF4444` | Error, rechazado |
| Info | `#3B82F6` | Información |

---

## 3. Tipografía

### 3.1 Familia Tipográfica

**Primary Font:** Inter (Google Fonts)
```css
font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
```

**Monospace (código/números):** JetBrains Mono
```css
font-family: 'JetBrains Mono', 'Fira Code', monospace;
```

### 3.2 Escala Tipográfica

| Token | Size | Weight | Line Height | Uso |
|-------|------|--------|-------------|-----|
| `text-xs` | 12px | 400 | 16px | Labels pequeños |
| `text-sm` | 14px | 400 | 20px | Texto secundario |
| `text-base` | 16px | 400 | 24px | Texto principal |
| `text-lg` | 18px | 500 | 28px | Subtítulos |
| `text-xl` | 20px | 600 | 28px | Títulos de sección |
| `text-2xl` | 24px | 700 | 32px | Títulos de página |
| `text-3xl` | 30px | 700 | 36px | Headlines |
| `text-4xl` | 36px | 800 | 40px | Hero headlines |

### 3.3 Números de Moneda

Para importes monetarios usar **JetBrains Mono**:
```css
font-variant-numeric: tabular-nums;
```

---

## 4. Espaciado

### 4.1 Sistema de Espaciado (8px base)

| Token | Valor | Uso |
|-------|-------|-----|
| `space-0` | 0px | - |
| `space-1` | 4px | Separación mínima |
| `space-2` | 8px | Entre elementos cercanos |
| `space-3` | 12px | Padding pequeño |
| `space-4` | 16px | Padding estándar |
| `space-6` | 24px | Separación entre secciones |
| `space-8` | 32px | Padding grande |
| `space-12` | 48px | Separación de secciones |
| `space-16` | 64px | Espaciado hero |

### 4.2 Border Radius

| Token | Valor | Uso |
|-------|-------|-----|
| `rounded-none` | 0px | Sin radio |
| `rounded-sm` | 4px | Botones pequeños |
| `rounded` | 6px | Botones, inputs |
| `rounded-md` | 8px | Cards |
| `rounded-lg` | 12px | Modals |
| `rounded-xl` | 16px | Cards grandes |
| `rounded-full` | 9999px | Avatars, pills |

---

## 5. Sombras

| Token | Valor | Uso |
|-------|-------|-----|
| `shadow-sm` | 0 1px 2px rgba(0,0,0,0.05) | Elevación sutil |
| `shadow` | 0 1px 3px rgba(0,0,0,0.1) | Cards |
| `shadow-md` | 0 4px 6px rgba(0,0,0,0.1) | Dropdowns |
| `shadow-lg` | 0 10px 15px rgba(0,0,0,0.1) | Modals |
| `shadow-xl` | 0 20px 25px rgba(0,0,0,0.1) | Dialogs importantes |

---

## 6. Componentes UI Base

### 6.1 Button

**Variantes:**
| Variante | Descripción | Uso |
|----------|-------------|-----|
| `primary` | Fondo verde-600, texto blanco | CTA principales |
| `secondary` | Fondo azul-600, texto blanco | Acciones secundarias |
| `outline` | Borde gray-300, fondo transparente | Opciones alternativas |
| `ghost` | Sin borde, fondo transparente | Links, acciones menores |
| `destructive` | Fondo red-600, texto blanco | Eliminación, acciones peligrosas |

**Tamaños:**
| Size | Padding | Font Size | Border Radius |
|------|---------|-----------|--------------|
| `sm` | 8px 12px | 14px | 4px |
| `md` | 10px 16px | 16px | 6px |
| `lg` | 12px 20px | 18px | 6px |

**Estados:**
- Default
- Hover: opacity 90%
- Active: opacity 80%
- Disabled: opacity 50%, cursor not-allowed
- Loading: spinner + texto "Procesando..."

---

### 6.2 Input

**Estructura:**
```
┌─────────────────────────────────────┐
│ Label (text-sm, text-gray-700)     │
├─────────────────────────────────────┤
│ Input                               │
│ - Border: gray-300                  │
│ - Radius: 6px                       │
│ - Padding: 10px 12px                │
│ - Font: 16px                        │
├─────────────────────────────────────┤
│ Helper text (text-xs, text-gray-500)│
└─────────────────────────────────────┘
```

**Estados:**
- Default: border-gray-300
- Focus: border-blue-500, ring-2 ring-blue-500/20
- Error: border-red-500, ring-2 ring-red-500/20
- Disabled: bg-gray-100, cursor not-allowed

---

### 6.3 Card

**Estructura:**
```tsx
<Card>
  <CardHeader>
    <CardTitle> Título </CardTitle>
    <CardDescription> Descripción </CardDescription>
  </CardHeader>
  <CardContent>
    {/* Contenido */}
  </CardContent>
  <CardFooter>
    {/* Acciones */}
  </CardFooter>
</Card>
```

**Specs:**
- Background: white
- Border: gray-200
- Border radius: 8px
- Shadow: shadow-sm
- Padding: 16px (content), 12px (header/footer)

---

### 6.4 Badge

**Variantes:**
| Variante | Background | Text | Uso |
|----------|------------|------|-----|
| `success` | green-100 | green-700 | Aprobado, activo |
| `warning` | yellow-100 | yellow-700 | Pendiente |
| `error` | red-100 | red-700 | Rechazado, error |
| `info` | blue-100 | blue-700 | En proceso |
| `neutral` | gray-100 | gray-700 | Inactivo |

**Specs:**
- Padding: 4px 8px
- Font size: 12px
- Font weight: 500
- Border radius: 4px (rounded-sm)

---

### 6.5 Dialog/Modal

**Estructura:**
```
┌─────────────────────────────────────────────┐
│ Header                          ✕ Close     │
│ Title                                            │
├─────────────────────────────────────────────┤
│                                             │
│ Content                                      │
│                                             │
├─────────────────────────────────────────────┤
│ Footer                                       │
│ [Cancel]                    [Confirm]       │
└─────────────────────────────────────────────┘
```

**Specs:**
- Overlay: bg-black/50
- Content: white, rounded-lg, shadow-lg
- Max width: 480px (sm), 640px (md), 800px (lg)
- Padding: 24px
- Animation: fade in + scale

---

### 6.6 Table

**Estructura:**
```
┌──────────┬──────────┬──────────┐
│ Header 1 │ Header 2 │ Header 3 │
├──────────┼──────────┼──────────┤
│ Cell 1   │ Cell 2   │ Cell 3   │
├──────────┼──────────┼──────────┤
│ Cell 4   │ Cell 5   │ Cell 6   │
└──────────┴──────────┴──────────┘
```

**Specs:**
- Header: bg-gray-50, font-weight 600, text-left
- Cell: border-b, py-3, px-4
- Hover row: bg-gray-50
- Border: gray-200

**Variantes de estado:**
- Loading: Skeleton rows
- Empty: EmptyState con mensaje
- Error: ErrorState con retry

---

### 6.7 Select

Mismo estilo que Input, con chevron icon.
Dropdown con max-height y scroll.

---

### 6.8 Alert

**Variantes:**
| Variante | Border | Background | Icon | Uso |
|----------|--------|------------|------|-----|
| `info` | left-4 border-blue-500 | blue-50 | Info | Información |
| `success` | left-4 border-green-500 | green-50 | Check | Éxito |
| `warning` | left-4 border-yellow-500 | yellow-50 | Alert | Precaución |
| `error` | left-4 border-red-500 | red-50 | X | Error |

---

### 6.9 Skeleton

Para estados de carga:

```tsx
<Skeleton className="h-4 w-1/4" />        // Línea de texto
<Skeleton className="h-12 w-12 rounded-full" /> // Avatar
<Skeleton className="h-32 w-full" />      // Card
```

---

## 7. Layouts

### 7.1 Public Layout

```
┌─────────────────────────────────────────────┐
│ PublicNavbar                                │
│ Logo          Links (Productos, Nosotros)   │
│                              [Iniciar Sesión]│
├─────────────────────────────────────────────┤
│                                             │
│ Page Content                                │
│                                             │
├─────────────────────────────────────────────┤
│ Footer                                      │
│ Links │ Social │ Copyright                  │
└─────────────────────────────────────────────┘
```

### 7.2 Auth Layout

```
┌─────────────────────────────────────────────┐
│                                             │
│              ┌─────────────────┐            │
│              │   Logo          │            │
│              │                 │            │
│              │   AuthForm      │            │
│              │                 │            │
│              │   [Submit]      │            │
│              │                 │            │
│              │   Links         │            │
│              └─────────────────┘            │
│                                             │
└─────────────────────────────────────────────┘
```

### 7.3 Dashboard Layout

```
┌───────────────────────────────────────────────────────────┐
│ Header: Logo │ Breadcrumb │ Notifications │ User Menu     │
├────────┬──────────────────────────────────────────────────┤
│        │                                                  │
│ Sidebar│  Page Content                                    │
│        │                                                  │
│ - Dashboard                                                │
│ - Cuentas                                                  │
│ - Créditos     ┌─────────────────────────────────────┐   │
│ - KYC          │ Stats Cards                         │   │
│ - Docs         │ ┌────┐ ┌────┐ ┌────┐ ┌────┐        │   │
│        │       │ │    │ │    │ │    │ │    │        │   │
│        │       │ └────┘ └────┘ └────┘ └────┘        │   │
│        │       └─────────────────────────────────────┘   │
│        │                                                  │
│        │  Content Area                                    │
│        │                                                  │
│        │                                                  │
└────────┴──────────────────────────────────────────────────┘
```

**Responsive:**
- Desktop (>1024px): Sidebar visible
- Tablet (640-1024px): Sidebar collapsed (icons only)
- Mobile (<640px): Sidebar hidden, hamburger menu

### 7.4 Admin Layout

Igual que Dashboard Layout pero con sidebar extendido:

```
Admin Sidebar:
- Dashboard
- Socios
  - Lista
  - Solicitudes
- Créditos
  - Solicitudes
  - Evaluaciones
- KYC
- Documentos
- Estadísticas
```

---

## 8. Breakpoints

| Breakpoint | Width | Layout |
|------------|-------|--------|
| `sm` | 640px | Mobile landscape |
| `md` | 768px | Tablet |
| `lg` | 1024px | Desktop |
| `xl` | 1280px | Large desktop |
| `2xl` | 1536px | Extra large |

---

## 9. Animaciones

### 9.1 Transiciones

| Propiedad | Duración | Easing |
|-----------|----------|--------|
| `transition-colors` | 150ms | ease-in-out |
| `transition-opacity` | 150ms | ease-in-out |
| `transition-transform` | 200ms | ease-out |
| `transition-all` | 300ms | ease-in-out |

### 9.2 Loading States

| Elemento | Animación |
|----------|-----------|
| Spinner | rotate 360deg, 1s, linear, infinite |
| Skeleton | opacity 0 → 1 → 0, 1.5s, ease-in-out, infinite |
| Progress | width animation, 300ms |
| Toast | slide in from right, 200ms |

### 9.3 Page Transitions

```tsx
// Framer Motion example
const pageVariants = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -20 }
};
```

---

## 10. Iconografía

**Library:** Lucide React (o Heroicons)

**Tamaños:**
| Size | Uso |
|------|-----|
| `h-4 w-4` (16px) | Inline con texto |
| `h-5 w-5` (20px) | Botones, nav |
| `h-6 w-6` (24px) | Secciones |
| `h-8 w-8` (32px) | Empty states |
| `h-12 w-12` (48px) | Hero icons |

---

## 11. Z-Index Scale

| Value | Layer |
|-------|-------|
| 0 | Contenido normal |
| 10 | Dropdowns |
| 20 | Sticky headers |
| 30 | Modals overlay |
| 40 | Modal content |
| 50 | Popovers |
| 100 | Toast notifications |

---

## 12. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-21 | @product-manager | Creación inicial del design system |

---

## 13. Referencias

- shadcn/ui: https://ui.shadcn.com
- Tailwind CSS: https://tailwindcss.com
- Lucide Icons: https://lucide.dev
- Inter font: https://rsms.me/inter/
