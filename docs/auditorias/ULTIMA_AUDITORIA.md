# Auditoría de Seguridad y Arquitectura
## Feature: admin_solicitudes_registro (Issue #69)

**Fecha:** 2026-04-21
**Auditor:** Lead Software Architect & Cyber-Security Auditor
**Proyecto:** Fondo de Ahorro Platform
**Versión Analizada:** Issue #69 - Gestión Solicitudes de Registro

---

## RESUMEN EJECUTIVO

| Severidad | Total |
|-----------|-------|
| 🔴 CRÍTICA | 1 |
| 🟠 ALTA | 4 |
| 🟡 MEDIA | 2 |
| ⚪ BAJA | 0 |

**Estado General:** ⚠️ APROBADO CON NOTAS

La implementación cumple con los requisitos funcionales básicos pero presenta vulnerabilidades de seguridad que requieren corrección antes de producción. La arquitectura sigue patrones de Clean Architecture en su mayoría, pero existen puntos de entrada sin validación suficiente.

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. Posible XSS en Motivo de Rechazo

**[CRITICIDAD]:** CRÍTICA
**[CATEGORÍA]:** Seguridad
**[DESCRIPCIÓN]:** En `solicitud_registro_card.dart:98`, el motivo de rechazo se renderiza directamente sin sanitización:

```dart
'Motivo rechazo: ${solicitud.motivoRechazo}'
```

**[IMPACTO]:** Si un atacante logra inyectar código JavaScript malicioso en el campo `motivoRechazo` a través del backend (o si el backend no valida este campo), cuando un administrador visualice la lista de solicitudes, el código se ejecutará en el contexto del navegador del admin. En un sistema bancario, esto podría llevar a:
- Robo de tokens de sesión del administrador
- Exposición de datos sensibles de usuarios
- Modificación maliciosa de datos

**[CORRECCIÓN]:**
```dart
// Usar Text instead of String interpolation directa
child: Text(
  'Motivo rechazo: ${solicitud.motivoRechazo}',
  style: TextStyle(
    fontSize: 12,
    color: Colors.red.shade700,
  ),
),
// O sanitizar si es necesario
```

O mejor aún, usar un widget que安全感 maneje el contenido:
```dart
child: HtmlSanitizer.sanitize(
  'Motivo rechazo: ${solicitud.motivoRechazo}',
),
```

---

## VIOLACIONES DE ARQUITECTURA (🟠)

### 2. Import Incorrecto en Repository Abstract

**[CRITICIDAD]:** ALTA
**[CATEGORÍA]:** Arquitectura
**[DESCRIPCIÓN]:** En `admin_solicitudes_registro_repository.dart:1`, el repositorio abstracto importa directamente la implementación del datasource:

```dart
import '../../data/datasources/admin_solicitudes_registro_remote_datasource.dart';
```

Esto viola el principio de Clean Architecture. El repositorio abstracto (interfaz) debería depender solo de contratos, no de implementaciones concretas.

**[IMPACTO]:** Acoplamiento indebido entre capas. Si se necesita cambiar la implementación del datasource (ej: agregar caché local), se debe modificar el interface de repositorio.

**[CORRECCIÓN]:**
Mover la clase `AdminSolicitudesRegistroResponse` a un archivo separado en `domain/models/` y que el repositorio solo referencie:
```dart
import '../../domain/models/admin_solicitudes_registro_response.dart';

abstract class AdminSolicitudesRegistroRepository {
  Future<AdminSolicitudesRegistroResponse> listarSolicitudes({...});
  Future<void> aprobarSolicitud(String id, {String? comentario});
  Future<void> rechazarSolicitud(String id, String motivo);
}
```

---

### 3. Estado Inconsistente tras Error en Cubit

**[CRITICIDAD]:** ALTA
**[CATEGORÍA]:** Arquitectura
**[DESCRIPCIÓN]:** En `admin_solicitudes_registro_cubit.dart:164-169`, tras un error en `aprobarSolicitud`:

```dart
} catch (e) {
  emit(currentState.copyWith(isAprobando: false));  // Estado A
  emit(AdminSolicitudesRegistroError(...));          // Estado B
  emit(currentState);                                 // Estado C - ¡BUG!
}
```

Se emite `currentState` original (sin `isAprobando: false`) al final. Hay una inconsistencia donde el flag `isAprobando` se setea a `false` pero luego se pierde cuando se re-emite `currentState`.

**[IMPACTO]:** El indicador de carga podría no reflejar el estado real. Si el usuario intenta otra acción mientras se muestra el error, el flag `isAprobando` podría estar en un estado inesperado.

**[CORRECCIÓN]:**
```dart
} catch (e) {
  emit(AdminSolicitudesRegistroError(...));
  emit(currentState.copyWith(isAprobando: false));
}
```

---

### 4. Indicadores de Carga Globales en Lista

**[CRITICIDAD]:** ALTA
**[CATEGORÍA]:** UX / Rendimiento
**[DESCRIPCIÓN]:** En `admin_solicitudes_registro_page.dart:274`, todos los cards comparten el mismo flag `isLoading`:

```dart
isLoading: state.isAprobando || state.isRechazando,
```

Y en `admin_solicitudes_registro_cubit.dart:33-34`, `isAprobando` e `isRechazando` son flags globales en `AdminSolicitudesRegistroLoaded`.

**[IMPACTO]:** Si un administrador está aprobando/rechazando UNA solicitud, TODAS las solicitudes muestran el indicador de carga. Esto genera:
- Confusión visual (el usuario no sabe cuál solicitud se está procesando)
- UX deficiente cuando hay múltiples solicitudes pendientes
- Bloqueo de acciones en solicitudes que no están siendo procesadas

**[CORRECCIÓN]:** Cambiar a flags por solicitud:
```dart
class AdminSolicitudesRegistroLoaded {
  final Map<String, bool> loadingById; // Instead of global flags
  
  bool isLoadingFor(String id) => loadingById[id] ?? false;
}
```

---

### 5. Validación Insuficiente en Diálogos de Entrada

**[CRITICIDAD]:** ALTA
**[CATEGORÍA]:** Seguridad
**[DESCRIPCIÓN]:** En `_showRechazoDialog` (`admin_solicitudes_registro_page.dart:64`) y `_showAprobarDialog` (`admin_solicitudes_registro_page.dart:120-127`):

- Solo se valida que el motivo no esté vacío
- No hay validación de longitud máxima
- No hay sanitización de caracteres especiales
- No hay validación del comentario opcional

**[IMPACTO]:** Un usuario malicioso podría:
- Enviar motivos de rechazo extremadamente largos para causar DoS
- Inyectar caracteres especiales que podrían causar problemas en el backend
- Enviar datos con formatos inesperados

**[CORRECCIÓN]:**
```dart
void _showRechazoDialog(SolicitudRegistroAdminItem solicitud) {
  final motivoController = TextEditingController();
  
  showDialog(
    context: context,
    builder: (dialogContext) => AlertDialog(
      // ...
      TextField(
        controller: motivoController,
        decoration: const InputDecoration(
          labelText: 'Motivo del rechazo',
          hintText: 'Ingrese el motivo...',
          border: OutlineInputBorder(),
        ),
        maxLines: 3,
        maxLength: 500, // Longitud máxima
      ),
      actions: [
        TextButton(
          onPressed: () {
            final motivo = motivoController.text.trim();
            if (motivo.isEmpty) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Debe ingresar un motivo'),
                  backgroundColor: Colors.red,
                ),
              );
              return;
            }
            if (motivo.length < 10) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('El motivo debe tener al menos 10 caracteres'),
                  backgroundColor: Colors.red,
                ),
              );
              return;
            }
            Navigator.pop(dialogContext);
            context.read<AdminSolicitudesRegistroCubit>().rechazarSolicitud(
              solicitud.id,
              htmlEscape(motivo), // Sanitizar
            );
          },
          // ...
        ),
      ],
    ),
  );
}
```

---

## MEJORAS RECOMENDADAS (🟡)

### 6. Duplicación de Lógica de Estados

**[CRITICIDAD]:** MEDIA
**[CATEGORÍA]:** Mantenibilidad
**[DESCRIPCIÓN]:** La lógica para convertir códigos de estado (`PENDIENTE`, `APROBADA`, etc.) a nombres legibles está duplicada:

- `EstadoBadge._getDisplayName()` (línea 27-40)
- `SolicitudRegistroAdminItem.estadoDisplay` (línea 73-86)

**[IMPACTO]:** Violación del principio DRY. Si se necesita cambiar el formato de display, hay que hacerlo en dos lugares.

**[CORRECCIÓN]:** Centralizar en el enum `EstadoSolicitudRegistro` y usar extensión en ambos widgets.

---

### 7. Estados como String Libre en Entity

**[CRITICIDAD]:** MEDIA
**[CATEGORÍA]:** Arquitectura
**[DESCRIPCIÓN]:** `SolicitudRegistroAdminItem.estado` es un `String` en lugar del enum `EstadoSolicitudRegistro`:

```dart
final String estado; // Debería ser EstadoSolicitudRegistro
```

Y en `fromJson`:
```dart
estado: json['estado'] as String? ?? '',
```

No hay validación de que el estado sea uno de los valores válidos.

**[IMPACTO]:** Estados inválidos podrían causar `SwitchError` en runtime, o comportamiento inesperado en la UI.

**[CORRECCIÓN]:**
```dart
final EstadoSolicitudRegistro estado;

factory SolicitudRegistroAdminItem.fromJson(Map<String, dynamic> json) {
  final estadoStr = json['estado'] as String? ?? 'PENDIENTE';
  return SolicitudRegistroAdminItem(
    // ...
    estado: EstadoSolicitudRegistro.values.firstWhere(
      (e) => e.apiValue == estadoStr,
      orElse: () => EstadoSolicitudRegistro.pendiente,
    ),
    // ...
  );
}
```

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo | Hallazgo |
|-----------|---------|----------|
| CRÍTICA | `presentation/widgets/solicitud_registro_card.dart:98` | XSS en motivo rechazo |
| ALTA | `domain/repositories/admin_solicitudes_registro_repository.dart:1` | Import incorrecto |
| ALTA | `presentation/cubit/admin_solicitudes_registro_cubit.dart:164-169` | Estado inconsistente |
| ALTA | `presentation/pages/admin_solicitudes_registro_page.dart:274` | Flags globales |
| ALTA | `presentation/pages/admin_solicitudes_registro_page.dart:64,120` | Validación insuficiente |
| MEDIA | `presentation/widgets/estado_badge.dart` + `domain/entities/solicitud_registro_admin_item.dart` | Duplicación |
| MEDIA | `domain/entities/solicitud_registro_admin_item.dart:57` | String libre para estado |

---

## VEREDICTO

### ⚠️ APROBADO CON NOTAS

La implementación es funcional y sigue la arquitectura general del proyecto. Sin embargo, antes de pasar a producción **es obligatorio corregir**:

1. **[CRÍTICA]** El bug de XSS potencial en el motivo de rechazo (debe sanitizarse la salida)
2. **[ALTA]** La validación de entradas en los diálogos de aprobar/rechazar
3. **[ALTA]** El import incorrecto en el repositorio abstracto

Las otras mejoras son recomendadas pero no bloqueantes.

---

*Generado por: Security Auditor - Fondo de Ahorro Platform*
*Fecha: 2026-04-21 21:04:47*
