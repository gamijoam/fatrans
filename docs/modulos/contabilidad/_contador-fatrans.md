---
tags: [contabilidad, sub-agente, claude-code, herramientas]
created: 2026-05-20
estado: activo
---

# 🧮 Sub-agente `contador-fatrans`

> [!summary] Para qué es
> Sub-agente especializado de Claude Code que revisa proactivamente cada
> decisión contable del proyecto. Conoce VEN-NIF, SUDECA/SUNACOOP, el plan
> de cuentas Fatrans (V21), y la realidad operativa específica del fondo
> (transferencias bancarias, no caja física).

## Ubicación del archivo

`.claude/agents/contador-fatrans.md` (en raíz del repo — versionado, queda para todo el equipo).

## Cuándo se invoca

Claude (agente principal) lo invoca **proactivamente** sin que el usuario
tenga que pedirlo, en los siguientes escenarios:

- ✅ Antes de codear cualquier mapping operación → asiento contable.
- ✅ Antes de mergear cualquier PR que toque `com.tufondo.contabilidad.*` o
  `*ContabilidadAdapter` / `*ContabilidadPort` en otros módulos.
- ✅ Cuando se diseñan reportes contables (Libro Diario, Libro Mayor,
  Balance General, Estado de Resultados).
- ✅ Cuando se discute si una operación de negocio debe ir a una cuenta u otra.
- ✅ Cuando se considera agregar/modificar cuentas al plan de cuentas
  (migration V22+).
- ✅ Cuando se diseñan jobs de cierre, devengo, provisión, depreciación, etc.

## Cómo invocarlo manualmente

Si el usuario quiere forzar una revisión:

> "Pasale esto al contador para revisión: [describir el mapping o decisión]"

O directamente desde una sesión Claude Code:

```
> Invocar Agent con subagent_type: contador-fatrans
```

## Qué evalúa formalmente

1. **Cuentas existen y son hoja** (no totalizadoras).
2. **Cuadre de partida doble** (Σdebe = Σhaber exacto).
3. **Naturaleza correcta** (DEUDORA/ACREEDORA según la operación).
4. **Refleja la realidad operativa** (no asentar a Caja si es transferencia).
5. **Desglose suficiente** (capital vs interés vs mora separados).
6. **Auditoría completa** (glosa clara, origen, referencia externa).
7. **Atomicidad** (asiento en la misma transacción que la operación).
8. **Cuentas correctoras correctas** (`1.3.99`, `1.5.99` con naturaleza opuesta — esperado).

## Errores comunes que detecta

- ❌ Usar `1.1.01` Caja Principal para operaciones bancarias en Fatrans
  → debe ser `1.1.03` Bancos Bs ([[_decisiones-contables#D-002|ver D-002]]).
- ❌ Mezclar intereses normales con moratorios en una sola cuenta
  → deben ser `4.1.01` vs `4.1.03` ([[_decisiones-contables#D-004|ver D-004]]).
- ❌ Asentar capital como ingreso (capital baja `1.3.01`, NO entra a `4.x.x`).
- ❌ Asientos contra cuentas no-hoja (`1.1`, `2.1`, etc).
- ❌ Asientos sin glosa o con glosa genérica.
- ❌ Sin `referenciaExterna` para asientos automáticos (rompe trazabilidad).
- ❌ `BigDecimal` con escala != 4 (BD es `NUMERIC(18,4)`).
- ❌ Hook contable fuera del `@Transactional` (rompe atomicidad).
- ❌ `DELETE` físico de asientos o partidas (solo ANULAR con motivo).
- ❌ Permitir asientos en período cerrado (cuando #272 se implemente).

## Formato de sus respuestas

1. **Veredicto**: ✅ Aprueba / ⚠️ Aprueba con observaciones / ❌ Rechaza
2. **Tabla del asiento propuesto vs. recomendado** (si difieren)
3. **Razón regulatoria** (qué norma o exigencia SUDECA aplica)
4. **Casos borde a considerar**
5. **Tests faltantes**

## Limitaciones

- ⚠️ **NO sustituye contador colegiado**: el sub-agente actúa como
  primera línea de defensa, pero antes de uso productivo formal hay que
  conseguir validación de contador venezolano colegiado.
- ⚠️ **No conoce normativas futuras**: si SUDECA emite resolución nueva,
  hay que actualizar el archivo del sub-agente con esa info.
- ⚠️ **Solo aplica al contexto Fatrans**: el sub-agente está hardcodeado al
  plan V21 y la realidad operativa Fatrans. Otro proyecto necesitaría
  configuración distinta.

## Mantenimiento del sub-agente

El archivo `.claude/agents/contador-fatrans.md` debe actualizarse cuando:

1. Se agregan/eliminan cuentas del plan (migration V22+).
2. Cambia la naturaleza jurídica de Fatrans (#274 resuelve a caja/cooperativa/etc).
3. Se identifican nuevos errores comunes.
4. Se toman decisiones contables nuevas ([[_decisiones-contables|registrar acá]]).
5. SUDECA emite normativa nueva relevante.

## Casos donde ya intervino

| Fecha | Asunto | Veredicto |
|---|---|---|
| 2026-05-20 | Revisión `1.1.01` vs `1.1.03` en #267 | ❌ Corregir antes de mergear ([[_decisiones-contables#D-002\|D-002]]) |
| 2026-05-20 | Aprobar mapping desembolso/cuota para #268 | ✅ Aprobado con observaciones ([[_decisiones-contables#D-003\|D-003]], [[_decisiones-contables#D-004\|D-004]]) |
| 2026-05-20 | Identificar pendientes regulatorios del EPIC | ⚠️ 5 red flags + 5 mejoras (ver [[_pendientes-criticos]]) |

## Cómo "actualizar" su conocimiento

Editar directamente `.claude/agents/contador-fatrans.md` en el repo. Los
cambios se cargan al reiniciar Claude Code en este proyecto (sesiones
nuevas lo ven actualizado; sesiones activas requieren reload).
