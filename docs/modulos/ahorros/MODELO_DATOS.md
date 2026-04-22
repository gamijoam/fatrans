# Módulo de Gestión de Ahorros - Modelo de Datos

## Resumen

Documentación del modelo de datos del módulo de Gestión de Ahorros incluyendo entidades, relaciones, índices y consideraciones de integridad.

---

## Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CUENTA_AHORRO                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: Long (PK)                                                              │
│ numero_cuenta: String (UK, NN)  ──► Formato: AHO-YYYY-XXXXXX               │
│ socio_id: Long (NN, FK)        ──► FK → socios.id                          │
│ saldo_actual: Decimal(19,4)    ──► >= 0                                    │
│ saldo_retenido: Decimal(19,4)  ──► >= 0                                    │
│ tasa_interes: Decimal(8,6)     ──► 0.0001 - 1.0                            │
│ monto_minimo_requerido: Decimal(19,4) ──► >= 0.0001                        │
│ estado: Enum               ──► ACTIVA, SUSPENDIDA, CERRADA                  │
│ tipo_cuenta: Enum          ──► AHORRO, NOMINA, PLAZO_FIJO                  │
│ fecha_apertura: DateTime                                                    │
│ fecha_ultima_operacion: DateTime (nullable)                                │
│ version: Long              ──► Optimistic locking                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │ 1:N
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                MOVIMIENTO                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: Long (PK)                                                              │
│ numero_operacion: String (UK, NN) ──► Formato: MOV-YYYY-XXXXXX              │
│ cuenta_ahorro_id: Long (NN, FK)  ──► FK → cuentas_ahorro.id                │
│ socio_id: Long (NN, FK)          ──► FK → socios.id                        │
│ tipo: Enum                ──► DEPOSITO, RETIRO, TRANSFERENCIA_ENTRADA...    │
│ monto: Decimal(19,4) (NN)        ──► >= 0.0001                            │
│ saldo_anterior: Decimal(19,4)                                                    │
│ saldo_posterior: Decimal(19,4)                                                 │
│ descripcion: String (500)                                                     │
│ referencia: String (100)                                                      │
│ canal_origen: Enum          ──► WEB, MOBILE, ATM, SUCURSAL, API, BATCH      │
│ ip_origen: String (45)       ──► IPv4 or IPv6                              │
│ session_id: String (255)     ──► Auditoría de seguridad                     │
│ request_id: String (255)     ──► Trazabilidad de request                    │
│ estado: Enum                ──► PROCESADO, RECHAZADO, PENDIENTE, CANCELADO   │
│ fecha_movimiento: DateTime (NN)                                             │
│ fecha_valor: Date (NN)                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │ 1:N
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               RENDIMIENTO                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: Long (PK)                                                              │
│ cuenta_ahorro_id: Long (NN, FK)  ──► FK → cuentas_ahorro.id                │
│ periodo_inicio: Date (NN)                                                        │
│ periodo_fin: Date (NN)                                                          │
│ saldo_promedio_periodo: Decimal(19,4)                                        │
│ tasa_aplicada: Decimal(8,6) (NN) ──► 0.0001 - 1.0 (VALIDACIÓN CRÍTICA)      │
│ monto_rendimiento: Decimal(19,4)                                            │
│ tipo: Enum                ──► DIARIO, MENSUAL, ANUAL                        │
│ estado_aplicacion: Enum    ──► CALCULADO, APLICADO, CANCELADO               │
│ fecha_calculo: DateTime                                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Esquema de Tablas

### Table: cuentas_ahorro

```sql
CREATE TABLE cuentas_ahorro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL,
    socio_id BIGINT NOT NULL,
    saldo_actual DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    saldo_retenido DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    tasa_interes DECIMAL(8,6) NOT NULL DEFAULT 0.000000,
    monto_minimo_requerido DECIMAL(19,4) NOT NULL DEFAULT 0.0001,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    tipo_cuenta VARCHAR(20) NOT NULL,
    fecha_apertura DATETIME NOT NULL,
    fecha_ultima_operacion DATETIME NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_saldo_actual CHECK (saldo_actual >= 0),
    CONSTRAINT chk_saldo_retenido CHECK (saldo_retenido >= 0),
    CONSTRAINT chk_tasa_interes CHECK (tasa_interes >= 0 AND tasa_interes <= 1.0),
    CONSTRAINT chk_monto_minimo CHECK (monto_minimo_requerido >= 0.0001),
    CONSTRAINT uk_numero_cuenta UNIQUE (numero_cuenta),
    CONSTRAINT fk_cuentas_ahorro_socios FOREIGN KEY (socio_id) 
        REFERENCES socios(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_cuentas_ahorro_socio_id ON cuentas_ahorro(socio_id);
CREATE INDEX idx_cuentas_ahorro_estado ON cuentas_ahorro(estado);
CREATE INDEX idx_cuentas_ahorro_numero_cuenta ON cuentas_ahorro(numero_cuenta);
```

### Table: movimientos

```sql
CREATE TABLE movimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_operacion VARCHAR(20) NOT NULL,
    cuenta_ahorro_id BIGINT NOT NULL,
    socio_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    monto DECIMAL(19,4) NOT NULL,
    saldo_anterior DECIMAL(19,4) NULL,
    saldo_posterior DECIMAL(19,4) NULL,
    descripcion VARCHAR(500) NULL,
    referencia VARCHAR(100) NULL,
    canal_origen VARCHAR(20) NOT NULL,
    ip_origen VARCHAR(45) NULL,
    session_id VARCHAR(255) NULL,
    request_id VARCHAR(255) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PROCESADO',
    fecha_movimiento DATETIME NOT NULL,
    fecha_valor DATE NOT NULL,
    
    CONSTRAINT chk_monto CHECK (monto >= 0.0001),
    CONSTRAINT uk_numero_operacion UNIQUE (numero_operacion),
    CONSTRAINT fk_movimientos_cuenta FOREIGN KEY (cuenta_ahorro_id) 
        REFERENCES cuentas_ahorro(id) ON DELETE RESTRICT,
    CONSTRAINT fk_movimientos_socios FOREIGN KEY (socio_id) 
        REFERENCES socios(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_movimientos_cuenta_id ON movimientos(cuenta_ahorro_id);
CREATE INDEX idx_movimientos_socio_id ON movimientos(socio_id);
CREATE INDEX idx_movimientos_fecha ON movimientos(fecha_movimiento);
CREATE INDEX idx_movimientos_numero_operacion ON movimientos(numero_operacion);
CREATE INDEX idx_movimientos_estado ON movimientos(estado);
```

### Table: rendimientos

```sql
CREATE TABLE rendimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_ahorro_id BIGINT NOT NULL,
    periodo_inicio DATE NOT NULL,
    periodo_fin DATE NOT NULL,
    saldo_promedio_periodo DECIMAL(19,4) NOT NULL,
    tasa_aplicada DECIMAL(8,6) NOT NULL,
    monto_rendimiento DECIMAL(19,4) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado_aplicacion VARCHAR(20) NOT NULL DEFAULT 'CALCULADO',
    fecha_calculo DATETIME NOT NULL,
    
    CONSTRAINT chk_tasa_aplicada CHECK (tasa_aplicada >= 0.0001 AND tasa_aplicada <= 1.0),
    CONSTRAINT chk_monto_rendimiento CHECK (monto_rendimiento >= 0),
    CONSTRAINT fk_rendimientos_cuenta FOREIGN KEY (cuenta_ahorro_id) 
        REFERENCES cuentas_ahorro(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_rendimientos_cuenta_id ON rendimientos(cuenta_ahorro_id);
CREATE INDEX idx_rendimientos_periodo ON rendimientos(periodo_inicio, periodo_fin);
CREATE INDEX idx_rendimientos_estado ON rendimientos(estado_aplicacion);
```

---

## Relaciones

### Cardinalidad

| Relación | Tipo | Descripción |
|----------|------|-------------|
| CuentaAhorro → Socio | N:1 | Una cuenta pertenece a un socio |
| CuentaAhorro → Movimiento | 1:N | Una cuenta tiene muchos movimientos |
| CuentaAhorro → Rendimiento | 1:N | Una cuenta tiene muchos rendimientos |

### Integridad Referencial

| Relación | Acción en DELETE | Verificación |
|----------|------------------|--------------|
| cuentas_ahorro.socio_id → socios.id | RESTRICT | No permite eliminar socio con cuentas |
| movimientos.cuenta_ahorro_id → cuentas_ahorro.id | RESTRICT | No permite eliminar cuenta con movimientos |
| movimientos.socio_id → socios.id | RESTRICT | No permite eliminar socio con movimientos |
| rendimientos.cuenta_ahorro_id → cuentas_ahorro.id | CASCADE | Al cerrar cuenta, se eliminan rendimientos |

---

## Reglas de Negocio - Modelo de Datos

### RN-001: Un socio, una cuenta por tipo

```sql
-- Constraint para evitar duplicados
ALTER TABLE cuentas_ahorro
ADD CONSTRAINT uk_socio_tipo_cuenta 
UNIQUE (socio_id, tipo_cuenta);
```

### RN-002: Formato de número de cuenta

```
Pattern: AHO-{YYYY}-{XXXXXX}
- AHO: Prefijo fijo
- YYYY: Año actual (4 dígitos)
- XXXXXX: Secuencial de 6 dígitos (zeropadded)
```

### RN-003: Formato de número de operación

```
Pattern: MOV-{YYYY}-{XXXXXX}
- MOV: Prefijo fijo
- YYYY: Año de la operación (4 dígitos)
- XXXXXX: Secuencial de 6 dígitos (zeropadded)
```

### RN-004: Saldo Disponible

```sql
-- El saldo disponible se calcula, no se almacena
saldo_disponible = saldo_actual - saldo_retenido
```

### RN-005: Immutabilidad de Movimientos

> **Decisión de Diseño:** Una vez creado un movimiento, NO se modifica ni elimina. Cualquier corrección se hace mediante un movimiento de ajuste (tipo=AJUSTE).

---

## Índices y Performance

### Índices Primarios

| Tabla | Índice | Columnas | Tipo | Uso |
|-------|--------|----------|------|-----|
| cuentas_ahorro | PRIMARY | id | BTREE | PK |
| movimientos | PRIMARY | id | BTREE | PK |
| rendimientos | PRIMARY | id | BTREE | PK |

### Índices de Búsqueda

| Tabla | Índice | Columnas | Unique | Uso |
|-------|--------|----------|--------|-----|
| cuentas_ahorro | uk_numero_cuenta | numero_cuenta | YES | Búsqueda por número |
| cuentas_ahorro | idx_socio_id | socio_id | NO | Listado por socio |
| movimientos | uk_numero_operacion | numero_operacion | YES | Búsqueda por operación |
| movimientos | idx_cuenta_fecha | cuenta_ahorro_id, fecha_movimiento | NO | Historial de cuenta |
| rendimientos | idx_cuenta_periodo | cuenta_ahorro_id, periodo_inicio | NO | Búsqueda por periodo |

### Recomendaciones de Partitioning

Para tablas con alto volumen (movimientos):
- **Partitioning por fecha:** Mensual o trimestral
- **Partitioning por socio_id:** Para distribución horizontal

---

## Constraints de Validación

### Validaciones en Base de Datos

| Constraint | Tabla | Condición | Mensaje |
|------------|-------|-----------|---------|
| chk_saldo_actual | cuentas_ahorro | saldo_actual >= 0 | El saldo no puede ser negativo |
| chk_saldo_retenido | cuentas_ahorro | saldo_retenido >= 0 | El saldo retenido no puede ser negativo |
| chk_tasa_interes | cuentas_ahorro | tasa_interes BETWEEN 0 AND 1.0 | La tasa debe estar entre 0% y 100% |
| chk_monto_minimo | cuentas_ahorro | monto_minimo_requerido >= 0.0001 | El monto mínimo debe ser >= 0.0001 |
| chk_monto | movimientos | monto >= 0.0001 | El monto debe ser >= 0.0001 |
| chk_tasa_aplicada | rendimientos | tasa_aplicada BETWEEN 0.0001 AND 1.0 | **CRÍTICO:** La tasa aplicada debe estar en rango válido |

---

## Notas de Auditoría de Seguridad

### V-I1: Validación de Inputs en DTOs

> **Hallazgo:** Faltaban anotaciones de validación en los DTOs. 
> **Corrección aplicada:** Se agregaron `@DecimalMin`, `@DecimalMax`, `@Size`, `@Pattern` en todos los DTOs del módulo.

### V-I2: Rate Limiting

> **Hallazgo:** Solo se limitaba por endpoint, no por cuenta ni por IP.
> **Corrección aplicada:** Implementar limitadores por triple clave: IP + cuenta + endpoint.

### V-I3: Auditoría de Eventos

> **Hallazgo:** Faltaban eventos críticos como `INTENTO_RETIRO_RECHAZADO` y `ACCESO_CUENTA_AJENA`.
> **Corrección aplicada:** Agregar estos eventos al catálogo de auditoría.

### V-I4: Overflow Numérico en Rendimientos

> **Hallazgo:** Sin validación de rango para `tasaAplicada`.
> **Corrección aplicada:** Constraint `chk_tasa_aplicada` y validación en DTO.

### V-I5: Validación IDOR

> **Hallazgo:** No se especificaba cómo verificar acceso a cuenta.
> **Corrección aplicada:** En todos los endpoints, verificar que `socioId` del token coincida con el socio propietario de la cuenta.

---

## Value Objects Recomendados

### NumeroCuenta (Value Object)

```java
@Value
@EqualsAndHashCode
public class NumeroCuenta {
    private static final Pattern PATTERN = Pattern.compile("^AHO-\\d{4}-\\d{6}$");
    
    String valor;
    
    private NumeroCuenta(String valor) {
        this.valor = valor;
    }
    
    public static NumeroCuenta crear(String valor) {
        if (!PATTERN.matcher(valor).matches()) {
            throw new IllegalArgumentException("Formato inválido de número de cuenta");
        }
        return new NumeroCuenta(valor);
    }
    
    public static NumeroCuenta generar() {
        int year = Year.now().getValue();
        String secuencial = String.format("%06d", 
            ThreadLocalRandom.current().nextInt(1, 999999));
        return new NumeroCuenta(String.format("AHO-%d-%s", year, secuencial));
    }
}
```

### Saldo (Value Object)

```java
@Value
public class Saldo {
    BigDecimal disponible;
    BigDecimal retenido;
    BigDecimal actual;  // disponible + retenido
    
    public BigDecimal getDisponible() {
        return disponible.subtract(retenido);
    }
}
```

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0.0 | 2026-04-14 | @documentador | Creación inicial basada en spec PM |
| 1.0.1 | 2026-04-14 | @documentador | Integración hallazgos auditoría seguridad |
| 1.0.2 | 2026-04-14 | @documentador | Agregada documentación de constraints de seguridad |

---

## Referencias

- Especificación técnica: `/docs/modulos/ahorros/SPEC.md`
- Referencia API: `/docs/modulos/ahorros/API.md`
- Auditoría de seguridad: `/docs/auditorias/seguridad_YYYYMMDD.md`