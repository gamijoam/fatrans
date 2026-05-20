---
tags: [contabilidad, reporte, libro-diario, sudeca, ven-nif]
sub-issue: "#269"
pr: en-curso
estado: implementado
created: 2026-05-20
---

# 📖 Libro Diario — primer reporte SUDECA

> [!summary] Sub-issue [[Home|#269]] — IMPLEMENTADO
> Genera el Libro Diario contable: listado secuencial completo de todos los
> asientos del período por orden de número correlativo. **Primer reporte
> exigido por SUDECA** para auditoría regulatoria.

## Endpoint

```http
GET /api/v1/contabilidad/libro-diario
GET /api/v1/contabilidad/libro-diario/pdf
```

**Permisos**: solo `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`, `ROLE_SISTEMA`.

> [!note] ROL_CONTADOR pendiente
> Idealmente este endpoint debería ser accesible también a un `ROLE_CONTADOR`
> dedicado. El enum `Rol` actual no lo contempla. Ver pendiente "Crear rol
> CONTADOR" en [[_pendientes-criticos]].

### Query parameters

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `desde` | `LocalDate` (`YYYY-MM-DD`) | **obligatorio** | Inicio del período (inclusive) |
| `hasta` | `LocalDate` (`YYYY-MM-DD`) | **obligatorio** | Fin del período (inclusive) |
| `incluirAnulados` | `boolean` | `true` | Si incluir asientos anulados con marca visual ([[_decisiones-contables#D-006\|D-006]]) |

### Validaciones de rango

- `hasta >= desde` — sino `400 Bad Request`
- `(hasta - desde) ≤ 366 días` — sino `400 Bad Request`. Para reportes más largos, dividir consulta.

## Estructura del response JSON

```json
{
  "encabezado": {
    "razonSocial": "Asociación Fatrans de Ahorro y Crédito",
    "rif": "J-XXXXXXXX-X",
    "desde": "2026-05-01",
    "hasta": "2026-05-31",
    "generadoEn": "2026-05-20T14:30:00Z",
    "generadoPorUsuarioId": "...",
    "incluyeAnulados": true
  },
  "asientos": [
    {
      "numero": 1,
      "numeroFormateado": "2026-000001",
      "fechaContable": "2026-05-01",
      "origen": "AHORRO_DEPOSITO",
      "estado": "REGISTRADO",
      "glosa": "Depósito MOV-... en cuenta AHO-...",
      "referenciaExterna": "MOV-2026-000001",
      "motivoAnulacion": null,
      "totalDebe": "1500.00",
      "totalHaber": "1500.00",
      "partidas": [
        {
          "codigoCuenta": "1.1.03",
          "nombreCuenta": "Bancos Cta Corriente Bs",
          "debe": "1500.00",
          "haber": "0.00",
          "glosa": "Ingreso por transferencia del socio",
          "orden": 1
        },
        {
          "codigoCuenta": "2.1.01",
          "nombreCuenta": "Cuentas de Ahorro Bs",
          "debe": "0.00",
          "haber": "1500.00",
          "glosa": "Crédito en cuenta de ahorro",
          "orden": 2
        }
      ]
    }
  ],
  "totales": {
    "cantidadAsientos": 1,
    "cantidadAnulados": 0,
    "totalDebe": "1500.00",
    "totalHaber": "1500.00",
    "balanceado": true
  }
}
```

## Formato del PDF

- **Tamaño**: A4 horizontal (paisaje) — para acomodar 7 columnas legibles.
- **Cabecera**: razón social + RIF + título "LIBRO DIARIO" + período + fecha generación.
- **Tabla**: columnas `Fecha | Nº Asiento | Origen | Código | Cuenta | Glosa | Debe | Haber`.
- **Asientos**: fila "agrupadora" con datos de cabecera (fecha, número, origen, glosa) en gris claro, seguida de N filas con cada partida.
- **Asientos ANULADOS**: fondo rojo claro + texto rojo + tag `[ANULADO]` + motivo.
- **Totales**: al pie del documento — cantidad, total DEBE, total HABER, indicador BALANCEADO ✓ / ⚠ DESBALANCEADO.
- **Folio**: `Página N` al pie de cada página (event handler PdfWriter).

## Decisiones de diseño tomadas

### Asientos anulados se incluyen por defecto ([[_decisiones-contables#D-006|D-006]])

SUDECA exige el libro **completo** — la inmutabilidad legal aplica a TODO lo
registrado, no solo a lo activo. Los anulados se muestran con marca visual
(fondo rojo, tag `[ANULADO]`, motivo). El parámetro `incluirAnulados=false`
permite ocultarlos solo si se necesita una vista limpia para fines internos.

### Formato del número correlativo: `AÑO-NNNNNN`

El `numeroFormateado` se construye como `{año de fechaContable}-{numero a 6 dígitos}` (ej. `"2026-000001"`). Esto presenta el número de forma "anual" sin
requerir cambio del modelo de BD.

> [!warning] Pendiente regulatorio
> La SEQUENCE BD (`seq_asiento_numero`) actualmente NO se resetea por año
> fiscal. SUDECA exige reset anual real. Para esta iteración solo formateamos
> visualmente. **Antes del primer cierre fiscal serio** hay que implementar
> el reset real — ver [[_pendientes-criticos#Reset-anual-del-correlativo-de-asientos|P1]].

### Properties configurables para identidad de la entidad

Razón social y RIF se configuran vía `application.yml`:

```yaml
entidad:
  razonSocial: "Asociación Fatrans de Ahorro y Crédito"
  rif: "J-XXXXXXXX-X"
  direccion: "..."
  sigla: "FATRANS"
```

Defaults documentan que es necesario configurar (`"(configurar)"`). Si en el
futuro se necesita UI de admin para editarlos, sub-issue dedicado migra a
`parametros_sistema` en BD.

### Puerto PDF dedicado al módulo contabilidad

Se creó `ContabilidadPdfPort` propio del módulo en lugar de reutilizar el
`PdfGeneratorPort` de `documentospdf`. Razón:

- `PdfGeneratorPort` está acoplado a `TipoDocumento` enum del otro módulo (acoplaría módulos).
- Los reportes contables tienen contrato distinto (DTO tipado vs `Map<String, Object>`).
- Misma librería (OpenPDF) puede compartirse — solo el contrato es separado.

## Arquitectura (3 capas)

```
LibroDiarioController (api/controller)
        │
        │ GET /api/v1/contabilidad/libro-diario[?...formato=PDF]
        ▼
GenerarLibroDiarioUseCase (application/usecase)
        │   - valida filtro vía LibroDiarioFilter
        │   - lookup asientos vía AsientoContableRepository.listarPorRangoFecha
        │   - lookup nombres de cuenta vía CuentaContableRepository.listarTodas
        │   - construye LibroDiarioResponse con totales
        ▼ (si pidieron PDF)
ContabilidadPdfPort (application/port/output)
        │
        ▼
LibroDiarioPdfAdapter (infrastructure/pdf — OpenPDF)
```

## Tests (21 nuevos, todos verde)

| Test class | Casos | Estrategia |
|---|---|---|
| `LibroDiarioFilterTest` | 7 | Validaciones de rango (≤ 1 año, hasta ≥ desde, null check) |
| `GenerarLibroDiarioUseCaseTest` | 9 | Mockito — happy path, encabezado, partidas resueltas, anulados con/sin, período vacío, cuenta faltante (fallback), formato del número |
| `LibroDiarioPdfAdapterTest` | 5 | Verificación de bytes PDF (header `%PDF`), períodos vacíos / con asientos / con anulados / con muchas filas (paginación) / desbalance defensivo |

### Casos cubiertos

> [!check] Garantías
> - ✅ Rango ≤ 1 año fiscal (366 días). Rangos mayores rechazados.
> - ✅ `hasta < desde` rechazado.
> - ✅ Encabezado completo (razón social, RIF, período, auditoría).
> - ✅ Partidas resuelven nombre de cuenta vía lookup (1 query, indexado en memoria).
> - ✅ Asientos anulados con marca visual + motivo de anulación visible.
> - ✅ `incluirAnulados=false` filtra los ANULADOS.
> - ✅ Período sin asientos: response vacío balanceado en cero (no rompe).
> - ✅ Cuenta no encontrada en plan: fallback a UUID + nombre genérico (no rompe).
> - ✅ Totales del período son la suma exacta de cada asiento.
> - ✅ PDF se genera con bytes válidos `%PDF`, escala a 50+ asientos sin OOM.
> - ✅ PDF marca visualmente desbalances (caso defensivo, no debería ocurrir).

## Pendientes intencionalmente fuera de scope

- ⏳ **Reset anual real del correlativo** ([[_pendientes-criticos#Reset-anual-del-correlativo-de-asientos|P1]]) — actualmente solo formateamos visualmente, BD usa SEQUENCE continua.
- ⏳ **Firma digital del PDF** — SUDECA exige firma del contador y representante legal en formato impreso. Versión digital con FEA queda como sub-issue dedicado.
- ⏳ **Rol `CONTADOR` dedicado** — actualmente solo admins acceden. Sub-issue para agregar al enum `Rol`.
- ⏳ **Razón social en BD** — actualmente properties. Si se necesita UI de admin, migrar a `parametros_sistema`.

## Cómo usar

### Desde un cliente HTTP (admin autenticado)

```bash
# JSON
curl -X GET \
  -H "Authorization: Bearer $JWT" \
  "https://qa-api.fatrans.com.ve/api/v1/contabilidad/libro-diario?desde=2026-05-01&hasta=2026-05-31"

# PDF
curl -X GET \
  -H "Authorization: Bearer $JWT" \
  -o LibroDiario_Mayo2026.pdf \
  "https://qa-api.fatrans.com.ve/api/v1/contabilidad/libro-diario/pdf?desde=2026-05-01&hasta=2026-05-31"
```

### Desde el frontend admin (cuando se construya)

El frontend de admin puede:
1. Mostrar tabla con los asientos del JSON, paginada client-side.
2. Botón "Descargar PDF" que invoca el endpoint `/pdf` con los mismos filtros.

## Referencias

- Issue: [[Home|#269]]
- Decisión: [[_decisiones-contables#D-006|D-006 — incluir anulados, formato anual del correlativo]]
- Dependencias: [[01-plan-cuentas|#264]], [[02-asientos-contables|#265+#266]]
- Próximo: [[06-libro-mayor]] (#270) — saldos acumulados por cuenta
