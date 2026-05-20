---
tags: [contabilidad, asientos, partida-doble, ven-nif]
sub-issue: "#265, #266"
pr: "#314"
estado: merged
created: 2026-05-20
---

# 🧾 Asientos Contables — Partida Doble

> [!summary] Sub-issues [[Home|#265 + #266]]
> Tablas + dominio + service + 50+ tests. Permite registrar y anular asientos
> de partida doble con todas las validaciones contables clásicas.

## Modelo conceptual

```
AsientoContable (aggregate root)            ← cabecera
├── numero          BIGSERIAL (seq_asiento_numero)  ← correlativo SUDECA
├── fechaContable   LocalDate
├── glosa           String  (descripción del asiento)
├── origen          OrigenAsiento (10 valores)
├── estado          REGISTRADO | ANULADO
├── referenciaExterna  String (ej. MOV-2026-000001)
└── partidas        List<PartidaAsiento>   ← entity hija
    ├── cuentaId    UUID FK a plan_cuentas
    ├── debe        NUMERIC(18,4)
    ├── haber       NUMERIC(18,4)
    ├── orden       INT
    └── glosa       String
```

## Invariantes (validadas en dominio + BD)

> [!important] Los 4 mandamientos del asiento contable
> 1. **`Σ debe = Σ haber`** — la suma de todos los DEBEs iguala la de todos los HABERs (partida doble)
> 2. **`MIN_PARTIDAS = 2`** — al menos 2 renglones por asiento
> 3. **Cada partida: `(debe > 0 AND haber = 0)` XOR `(debe = 0 AND haber > 0)`** — nunca ambos, nunca ninguno
> 4. **Sin duplicados en el mismo lado** — no dos partidas del mismo lado (DEBE/HABER) sobre la misma cuenta (sí permitido en lados distintos)

## Estados y transiciones

```
       (crear)              (anular)
  ()  ──────►  REGISTRADO  ──────►  ANULADO
                                    │
                              (no se puede revertir;
                               para corregir → asiento nuevo)
```

> [!danger] Inmutabilidad de asientos
> Los asientos **NUNCA se actualizan ni borran**. Para corregir un error:
> 1. `anular()` el original → estado ANULADO + motivo
> 2. Generar un nuevo asiento con los valores correctos
>
> Sub-issue [[Home|#273]] cubrirá la reversión automática (asiento inverso) como atajo.

## Origen del asiento (qué evento lo disparó)

10 valores de `OrigenAsiento`:

| Categoría | Valor | Disparado por |
|---|---|---|
| **Ahorros** | `AHORRO_DEPOSITO` | [[03-hooks-ahorros#Depósito|RealizarDepositoUseCase]] |
| | `AHORRO_RETIRO` | [[03-hooks-ahorros#Retiro|RealizarRetiroUseCase]] |
| | `AHORRO_INTERES` | (pendiente — cuando se "aplique" un rendimiento) |
| **Créditos** | `CREDITO_DESEMBOLSO` | (pendiente sub-issue #268) |
| | `CREDITO_COBRO` | (pendiente #268) |
| | `CREDITO_INTERES` | (pendiente #268) |
| **Operativo** | `MANUAL` | Contador ingresa asiento manual |
| | `CIERRE` | Cierre mensual/anual (#272) |
| | `REVERSION` | Generado por `revertir()` (#273) |
| | `AJUSTE` | Corrección o reclasificación |

## Correlativo continuo (exigencia SUDECA)

SUDECA exige que los asientos tengan numeración **continua sin huecos** dentro
del año fiscal. Esto se garantiza con:

```sql
CREATE SEQUENCE seq_asiento_numero START WITH 1 INCREMENT BY 1 CACHE 1;
```

`CACHE 1` evita que PostgreSQL pre-asigne rangos que podrían perderse en
crashes. Cada `nextval()` devuelve el siguiente entero sin gaps.

> [!warning] Gaps en reinicios
> En la práctica, un rollback de transacción SÍ "consume" un número de
> secuencia (PostgreSQL no devuelve el número al rollback). Estos huecos
> son legalmente aceptables porque corresponden a operaciones abortadas, no
> a asientos contables persistidos. Para reportes SUDECA, mostramos solo los
> asientos persistidos en orden de `numero`.

## API del servicio

```java
@Transactional
public AsientoContable registrar(RegistrarAsientoCommand cmd)
    throws AsientoContableException;

@Transactional
public AsientoContable anular(UUID asientoId, String motivo)
    throws AsientoContableException;
```

### `RegistrarAsientoCommand` — ejemplo

```java
RegistrarAsientoCommand.builder()
    .fechaContable(LocalDate.now())
    .glosa("Depósito MOV-2026-000001 en cuenta AHO-2026-000001")
    .origen(OrigenAsiento.AHORRO_DEPOSITO)
    .referenciaExterna("MOV-2026-000001")
    .creadoPorUsuarioId(null)  // automático
    .asientoReversaId(null)
    .partidas(List.of(
        Partida.builder()
            .codigoCuenta("1.1.01")  // ← referenciada por código, no UUID
            .debe(new BigDecimal("1500.00"))
            .glosa("Ingreso de efectivo")
            .build(),
        Partida.builder()
            .codigoCuenta("2.1.01")
            .haber(new BigDecimal("1500.00"))
            .glosa("Crédito en cuenta de ahorro")
            .build()
    ))
    .build();
```

El service:
1. Resuelve los códigos de cuenta a UUIDs vía `CuentaContableRepository`.
2. Valida que **todas las cuentas existan**, sean **hojas** (`acepta_movimientos=true`) y estén **activas**.
3. Construye las `PartidaAsiento` (valida debe XOR haber, monto > 0, etc).
4. Construye el `AsientoContable` (valida partida doble).
5. Asigna el correlativo vía `nextval('seq_asiento_numero')`.
6. Persiste atómicamente cabecera + partidas.

Cualquier fallo lanza `AsientoContableException` y rollback completo.

## Esquema SQL (V22)

Resumen — ver `V22__asientos_contables.sql` completo:

```sql
CREATE TABLE asientos_contables (
    id UUID PRIMARY KEY,
    numero BIGINT NOT NULL UNIQUE,
    fecha_contable DATE NOT NULL,
    glosa VARCHAR(500),
    origen VARCHAR(30) NOT NULL CHECK (origen IN (...10 valores...)),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('REGISTRADO','ANULADO')),
    referencia_externa VARCHAR(100),
    -- ...
);

CREATE TABLE partidas_asientos (
    id UUID PRIMARY KEY,
    asiento_id UUID NOT NULL REFERENCES asientos_contables(id) ON DELETE RESTRICT,
    cuenta_id UUID NOT NULL REFERENCES plan_cuentas(id) ON DELETE RESTRICT,
    debe NUMERIC(18,4) NOT NULL DEFAULT 0,
    haber NUMERIC(18,4) NOT NULL DEFAULT 0,
    orden INT NOT NULL,
    -- XOR: una y solo una de las dos > 0
    CONSTRAINT chk_partida_debe_xor_haber
        CHECK ((debe > 0 AND haber = 0) OR (debe = 0 AND haber > 0))
);
```

> [!important] `ON DELETE RESTRICT`
> Ambas FKs (asiento→cuenta, partida→asiento) son `RESTRICT`. Esto
> previene a nivel BD que se borre una cuenta usada en asientos, o un asiento
> con partidas. La inmutabilidad legal queda garantizada por el SGBD aunque
> alguien intente DELETE manual.

## Tests (50+ casos)

| Test class | Casos | Cubre |
|---|---|---|
| `PartidaAsientoTest` | 11 | Factories `alDebe`/`alHaber`, validación XOR, escala 4 decimales, MONTO_MAXIMO, equals por id |
| `AsientoContableTest` | 16 (4 nested) | Invariantes partida doble, mínimo 2 partidas, anular, duplicados |
| `AsientoContableServiceTest` | 10 | Validación cuentas: inexistente / totalizadora / inactiva, desbalanceado, anular |
| `AsientoContableRepositoryImplTest` | 6 | Round-trip BD, queries por fecha/origen/estado/referencia |

## Referencias

- PR merged: [#314](https://github.com/gamijoam/fatrans/pull/314)
- Migration: `backend/src/main/resources/db/migration/V22__asientos_contables.sql`
- Issues: [[Home|#265 + #266]]
- Siguiente: [[03-hooks-ahorros|#267 — Hooks de Ahorros]]
