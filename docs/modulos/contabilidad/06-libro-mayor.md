---
tags: [contabilidad, reporte, libro-mayor, sudeca, ven-nif]
sub-issue: "#270"
pr: en-curso
estado: implementado
created: 2026-05-20
---

# 📚 Libro Mayor — saldos y movimientos por cuenta

> [!summary] Sub-issue [[Home|#270]] — IMPLEMENTADO
> Reporte SUDECA que agrupa los movimientos contables **por cuenta**: para
> cada cuenta muestra saldo inicial al comienzo del período, todos los
> movimientos del rango con su contracuenta, saldo acumulado por línea, y
> saldo final. Base de los reportes superiores ([[07-balance-general|#271]]).

## Endpoint

```http
GET /api/v1/contabilidad/libro-mayor
GET /api/v1/contabilidad/libro-mayor/pdf
```

**Permisos**: `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`, `ROLE_SISTEMA`. Falta rol
`CONTADOR` dedicado — pendiente P2 documentado.

### Query parameters

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `desde` | `LocalDate` | **obligatorio** | Inicio del período (inclusive) |
| `hasta` | `LocalDate` | **obligatorio** | Fin del período (inclusive) |
| `codigoCuenta` | `String` | `null` | Filtra a una sola cuenta (ej. `1.1.03`) |
| `incluirSinMovimientos` | `boolean` | `false` | Si `true`, muestra cuentas sin movimientos en el período |
| `incluirTotalizadoras` | `boolean` | `false` | Si `true`, muestra también cuentas no-hoja |

### Validaciones de rango

- `hasta >= desde` — sino `400 Bad Request`
- `(hasta - desde) ≤ 366 días` — sino `400 Bad Request`

## Diferencia con Libro Diario

| Aspecto | [[05-libro-diario\|Libro Diario]] (#269) | Libro Mayor (#270) |
|---|---|---|
| Agrupación | Por **asiento** cronológico | Por **cuenta** alfabético |
| Asientos ANULADOS | **Incluidos** con marca visual | **Excluidos** (saldos vigentes) |
| Para qué sirve | Historial / auditoría | Saldos por cuenta / base de reportes |
| Saldo acumulado | No aplica | Sí, por línea de movimiento |

> [!note] Por qué los ANULADOS se excluyen en el Mayor pero se incluyen en el Diario
> El Libro Diario es **historial** (la verdad histórica de lo que pasó, incluso si se anuló). El Libro Mayor es **saldo vigente** — un asiento anulado no debe afectar saldos. Esta diferencia es clave por exigencia VEN-NIF.

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
    "filtroCuenta": null,
    "incluyeSinMovimientos": false,
    "incluyeTotalizadoras": false
  },
  "cuentas": [
    {
      "codigo": "1.1.03",
      "nombre": "Bancos Cuenta Corriente Bs",
      "tipo": "ACTIVO",
      "naturaleza": "DEUDORA",
      "saldoInicialDebe": "5000.00",
      "saldoInicialHaber": "0.00",
      "saldoInicialNeto": "5000.00",
      "saldoInicialEtiqueta": "D",
      "movimientos": [
        {
          "fechaContable": "2026-05-03",
          "numeroAsiento": 5,
          "numeroAsientoFormateado": "2026-000005",
          "origen": "AHORRO_DEPOSITO",
          "glosaAsiento": "Depósito MOV-001 en cuenta AHO-001",
          "referenciaExterna": "MOV-2026-000001",
          "contracuentaCodigo": "2.1.01",
          "contracuentaNombre": "Cuentas de Ahorro Bs",
          "contracuentaResumen": null,
          "debe": "1500.00",
          "haber": "0.00",
          "saldoAcumulado": "6500.00"
        }
      ],
      "totalDebePeriodo": "1500.00",
      "totalHaberPeriodo": "0.00",
      "cantidadMovimientos": 1,
      "saldoFinalDebe": "6500.00",
      "saldoFinalHaber": "0.00",
      "saldoFinalNeto": "6500.00",
      "saldoFinalEtiqueta": "D"
    }
  ],
  "totales": {
    "cantidadCuentas": 1,
    "cantidadMovimientos": 1,
    "totalDebe": "1500.00",
    "totalHaber": "0.00",
    "balanceado": false
  }
}
```

> [!info] Sobre `totales.balanceado`
> Cuando se filtran solo algunas cuentas (no todo el plan), el total puede no estar balanceado — es esperado. Solo cuando se generar el Mayor SIN filtros (todas las cuentas hoja) los totales deberían cuadrar porque cada partida DEBE de un asiento tiene su HABER correspondiente en otra cuenta.

## Formato del PDF

- **Tamaño**: A4 horizontal.
- **Cabecera**: razón social, RIF, "LIBRO MAYOR", período, filtro aplicado.
- **Por cada cuenta**:
  - Cabecera con código + nombre + tipo + naturaleza
  - "Saldo inicial: X.XX D/A/—"
  - Tabla con 8 columnas: `Fecha | Nº Asiento | Origen | Contracuenta | Glosa | Debe | Haber | Saldo`
  - Si no hay movimientos: fila única "(sin movimientos en el período)"
  - Línea de resumen con totales del período + saldo final
- **Totales generales** al pie con cantidad de cuentas, movimientos, Σ Debe, Σ Haber.
- **Folio "Página N"** al pie de cada página.

## Decisiones de diseño (D-007)

### 1. Saldo inicial real (no asumir cero)

Se calcula con `SUM` agregado de todas las partidas previas a `desde-1`,
excluyendo asientos ANULADOS. Una sola query SQL eficiente por cuenta.

**Razón**: el Libro Mayor sin saldo inicial real es un dato falso. SUDECA y
VEN-NIF lo exigen.

### 2. Solo cuentas hoja por default

Las totalizadoras (nivel 1-2) se calculan sumando hijas — eso es trabajo del
**Balance General** (#271), no del Mayor. Aquí solo cuentas operativas
(con `acepta_movimientos=true`). Parámetro `incluirTotalizadoras=true`
para verlas igual.

### 3. Cuentas sin movimientos excluidas por default

Reporte limpio. Si el contador necesita ver TODAS las cuentas (ej. mostrar
`1.1.03 = saldo 0`), toggle `incluirSinMovimientos=true`.

### 4. Saldo en formato absoluto + tag (D/A/—)

Convención SUDECA: `3,800.00 (D)` en lugar de `+3,800.00` o `-3,800.00`.
La cuenta sabe por su naturaleza si el saldo es "esperado" (D para deudoras,
A para acreedoras). Si el saldo queda del lado opuesto (caso atípico pero
válido), el tag se invierte.

### 5. Contracuenta visible en cada movimiento

Para cada partida de la cuenta del mayor, se busca la **cuenta principal del lado opuesto** en el mismo asiento (la de mayor monto si hay varias). Esto mejora masivamente la legibilidad: "esta partida de Bancos vino de un depósito en Ahorros". Si el asiento tiene > 2 partidas en el lado opuesto, se muestra la principal + tag `(múltiple)`.

### 6. Asientos ANULADOS excluidos del Mayor

A diferencia del Diario, el Mayor es saldo vigente, no historial. Los
anulados no afectan saldos. Si el contador quiere ver el historial completo
con anulados, va al Libro Diario.

### 7. Adapter PDF unificado en `LibroDiarioPdfAdapter`

El archivo se llama `LibroDiarioPdfAdapter` por razones históricas (creado
en #269) pero implementa **todo el `ContabilidadPdfPort`** — Diario y Mayor.
Cuando #269 esté mergeado en develop, vale la pena un commit de refactor
que renombre a `ContabilidadPdfAdapter`. Por ahora, evita diff masivo entre
PRs hermanos.

## Performance

| Operación | Costo |
|---|---|
| Plan de cuentas | 1 query (cacheable en memoria) |
| Saldo inicial por cuenta | 1 query SQL `SUM` agregada |
| Movimientos del período por cuenta | 1 query JPA con hidratación batch (sin N+1) |
| **Total para N cuentas** | `2N + 1` queries |

Para ~50 cuentas operativas típicas: ~100 queries. Aceptable.

**Optimización futura** (si se vuelve lento): cachear saldos cerrados al
final de cada mes en una tabla `saldos_mensuales` poblada por el job de
cierre (#272). El Mayor solo lee del último cierre + movimientos posteriores.

## Tests (31 nuevos, todos verde)

| Test class | Casos | Estrategia |
|---|---|---|
| `LibroMayorFilterTest` | 8 | Unit — validaciones de rango, flags, factories |
| `GenerarLibroMayorUseCaseTest` | 11 | Mockito — filtros, saldo inicial, contracuenta, saldo acumulado, totales |
| `LibroMayorPdfAdapterTest` | 5 | Verificación bytes PDF, casos borde (vacío, sin movimientos, muchas cuentas, desbalanceado) |
| `AsientoContableRepositoryLibroMayorTest` | 7 | `@DataJpaTest` con H2 — queries nativas SUM + JOIN, filtrado por cuenta y estado |

### Garantías verificadas

> [!check]
> - ✅ Rango ≤ 1 año fiscal validado
> - ✅ Sin filtro → solo hojas por default (totalizadora 1.1 excluida)
> - ✅ `incluirSinMovimientos=true` muestra cuentas vacías
> - ✅ `incluirTotalizadoras=true` incluye cuentas no-hoja
> - ✅ Filtro por código devuelve solo esa cuenta
> - ✅ Código inexistente → `AsientoContableException`
> - ✅ Saldo inicial se calcula con SUM hasta `desde-1`
> - ✅ Etiqueta D/A/— según naturaleza y signo del saldo
> - ✅ Contracuenta correctamente resuelta (cuenta opuesta del asiento)
> - ✅ Saldo acumulado calculado progresivamente movimiento a movimiento
> - ✅ Asientos ANULADOS excluidos por la query (no aparecen en saldo ni movimientos)
> - ✅ SUM agregado a fecha de corte funciona en H2
> - ✅ Rango inclusive en ambos extremos
> - ✅ Cuenta no involucrada en el asiento no aparece en sus resultados
> - ✅ Decimales NUMERIC(18,4) preservados

## Cómo usar

```bash
# Mayor completo del mes de mayo (solo cuentas con movimientos)
curl -X GET \
  -H "Authorization: Bearer $JWT" \
  "https://qa-api.fatrans.com.ve/api/v1/contabilidad/libro-mayor?desde=2026-05-01&hasta=2026-05-31"

# Mayor de una cuenta específica con saldo aunque sea cero
curl -X GET \
  -H "Authorization: Bearer $JWT" \
  "https://qa-api.fatrans.com.ve/api/v1/contabilidad/libro-mayor?desde=2026-05-01&hasta=2026-05-31&codigoCuenta=1.1.03&incluirSinMovimientos=true"

# Descargar PDF
curl -X GET -H "Authorization: Bearer $JWT" \
  -o LibroMayor_Mayo2026.pdf \
  "https://qa-api.fatrans.com.ve/api/v1/contabilidad/libro-mayor/pdf?desde=2026-05-01&hasta=2026-05-31"
```

## Pendientes intencionalmente fuera de scope

- ⏳ **Tabla `saldos_mensuales` cacheada** — para acelerar el Mayor cuando crezcan los volúmenes. Parte del cierre mensual (#272).
- ⏳ **Rol `CONTADOR` dedicado** ([[_pendientes-criticos|P2]]).
- ⏳ **Firma digital del PDF** — sub-issue futuro.
- ⏳ **Reset anual real del correlativo** ([[_pendientes-criticos|P1]]).

## Referencias

- Issue: [[Home|#270]]
- Decisión: [[_decisiones-contables#D-007|D-007 — saldo real, solo hojas, anulados excluidos]]
- Dependencias: [[01-plan-cuentas|#264]], [[02-asientos-contables|#265+#266]], [[05-libro-diario|#269]] (reutiliza `EntidadProperties` y `ContabilidadPdfPort`)
- Próximo: [[07-balance-general]] (#271)
