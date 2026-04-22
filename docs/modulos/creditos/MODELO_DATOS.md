# Módulo de Gestión de Créditos - Modelo de Datos

## Resumen

Documentación del modelo de datos del módulo de Gestión de Créditos incluyendo entidades, relaciones, DDL, constraints e índices. El módulo gestiona 5 entidades principales con precisión NUMERIC(15,4) para montos.

---

## Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              TIPO_CREDITO                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: Long (PK)                                                              │
│ codigo: String (UK, NN)      ──► IDENTIFICADOR_UNICO                       │
│ nombre: String (NN)          ──► Nombre del producto                        │
│ tasa_interes_anual: Decimal(8,4) (NN)                                       │
│ plazo_minimo_meses: Integer (NN)                                           │
│ plazo_maximo_meses: Integer (NN)                                           │
│ monto_minimo: Decimal(19,4) (NN)                                           │
│ monto_maximo: Decimal(19,4) (NN)                                           │
│ porcentaje_requerimiento_colateral: Decimal(5,2)                           │
│ comision_apertura: Decimal(5,4)                                            │
│ penalidad_mora_tasa: Decimal(8,4)                                           │
│ dias_gracia: Integer (NN)                                                  │
│ activo: Boolean (NN)                                                       │
│ created_at: DateTime (NN)                                                  │
│ updated_at: DateTime                                                       │
│ version: Long                   ──► Optimistic locking                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ 1:N
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            SOLICITUD_CREDITO                               │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: UUID (PK)                                                              │
│ numero_solicitud: String (UK, NN) ──► SOL-CRED-YYYY-XXXXXX (SecureRandom)   │
│ socio_id: Long (NN, FK)        ──► FK → socios.id                            │
│ tipo_credito_id: Long (NN, FK) ──► FK → tipos_credito.id                    │
│ monto_solicitado: Decimal(19,4) (NN)                                        │
│ plazo_meses: Integer (NN)                                                   │
│ tasa_interes_aplicada: Decimal(8,4)                                        │
│ cuota_mensual_estimada: Decimal(19,4)                                      │
│ estado: Enum (NN)          ──► PENDIENTE, EN_EVALUACION, APROBADA, etc.     │
│ colateral_cuenta_id: UUID     ──► FK → cuentas_ahorro.id (nullable)        │
│ colateral_monto_retenido: Decimal(19,4)                                    │
│ evaluacion_id: UUID           ──► FK → evaluaciones_crediticias.id          │
│ plan_amortizacion_id: UUID   ──► FK → planes_amortizacion.id               │
│ referencia_desembolso: String (100)                                        │
│ cuenta_destino: String (34)   ──► IBAN                                      │
│ created_at: DateTime (NN)                                                     │
│ updated_at: DateTime                                                       │
│ version: Long                   ──► Optimistic locking                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ 1:1
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         EVALUACIONES_CREDITICIAS                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: UUID (PK)                                                              │
│ solicitud_id: UUID (NN, UK) ──► FK → solicitudes_credito.id                │
│ socio_id: Long (NN)                                                        │
│ puntaje_antiguedad: Integer (NN)      ──► 0-30 pts                          │
│ puntaje_historial_ahorro: Integer (NN) ──► 0-30 pts                        │
│ puntaje_capacidad_pago: Integer (NN)   ──► 0-40 pts                        │
│ score_interno: Integer (NN)        ──► 0-100 pts (suma)                   │
│ score_hash: String (64, NN)        ──► SHA-256 del cálculo (SEGURIDAD)     │
│ factores_serializados: Text (NN)   ──► JSON con breakdown                  │
│ firma_verificable: String (128)   ──► RSA signature (SEGURIDAD)            │
│ evaluacion_id_original: UUID      ──► Para detectar modificaciones         │
│ elegible: Boolean (NN)                                                      │
│ nivel_riesgo: String (20)        ──► BAJO, MEDIO, ALTO                      │
│ tasa_interes_final: Decimal(8,4)                                        │
│ mensaje_decision: String (500)                                           │
│ evaluador: String (100)          ──► Admin o SISTEMA                        │
│ created_at: DateTime (NN)                                                     │
│ version: Long                   ──► Optimistic locking                     │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ 1:1 (después de aprobar)
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            PLANES_AMORTIZACION                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: UUID (PK)                                                              │
│ solicitud_id: UUID (NN, UK) ──► FK → solicitudes_credito.id                │
│ monto_principal: Decimal(19,4) (NN)                                        │
│ tasa_interes: Decimal(8,4) (NN)                                           │
│ plazo_meses: Integer (NN)                                                   │
│ frecuencia_pago: String (20) (NN)  ──► MENSUAL, QUINCENAL, SEMANAL        │
│ fecha_inicio: Date (NN)                                                      │
│ fecha_fin: Date                                                            │
│ total_intereses: Decimal(19,4)                                            │
│ total_pagado: Decimal(19,4)                                               │
│ saldo_pendiente: Decimal(19,4)                                           │
│ numero_cuotas: Integer (NN)                                                 │
│ cuota_mensual: Decimal(19,4)                                              │
│ estado: Enum (NN)              ──► ACTIVO, CANCELADO, FINALIZADO, VENCIDO  │
│ created_at: DateTime (NN)                                                     │
│ updated_at: DateTime                                                       │
│ version: Long                   ──► Optimistic locking                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ 1:N
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AMORTIZACIONES                                │
├─────────────────────────────────────────────────────────────────────────────┤
│ id: UUID (PK)                                                              │
│ plan_id: UUID (NN, FK)      ──► FK → planes_amortizacion.id                 │
│ numero_cuota: Integer (NN)                                                │
│ fecha_vencimiento: Date (NN)                                              │
│ fecha_pago: Date                                                           │
│ capital: Decimal(19,4) (NN)                                               │
│ interes: Decimal(19,4) (NN)                                               │
│ seguros: Decimal(19,4)                                                    │
│ comisiones: Decimal(19,4)                                                 │
│ monto_cuota: Decimal(19,4) (NN)                                           │
│ saldo_insoluto: Decimal(19,4)                                            │
│ estado: Enum (NN)       ──► PENDIENTE, PAGADA, VENCIDA, CURSO_MORA, etc.   │
│ dias_mora: Integer                                                     │
│ interes_mora: Decimal(19,4)                                              │
│ monto_pagado: Decimal(19,4)                                              │
│ referencia_pago: String (100) (UK)  ──► Idempotency key (SEGURIDAD)       │
│ colateral_ejecutada: Boolean (NN)  ──► FALSE por defecto                  │
│ created_at: DateTime (NN)                                                     │
│ updated_at: DateTime                                                       │
│ version: Long              ──► Optimistic locking (CRÍTICO: previene double-payment)│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Esquema de Tablas

### Table: tipos_credito

```sql
CREATE TABLE tipos_credito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    tasa_interes_anual DECIMAL(8,4) NOT NULL,
    plazo_minimo_meses INT NOT NULL,
    plazo_maximo_meses INT NOT NULL,
    monto_minimo DECIMAL(19,4) NOT NULL,
    monto_maximo DECIMAL(19,4) NOT NULL,
    porcentaje_requerimiento_colateral DECIMAL(5,2),
    comision_apertura DECIMAL(5,4),
    penalidad_mora_tasa DECIMAL(8,4),
    dias_gracia INT NOT NULL DEFAULT 5,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_tipo_credito_codigo UNIQUE (codigo),
    CONSTRAINT chk_tasa_interes CHECK (tasa_interes_anual > 0 AND tasa_interes_anual <= 1.0),
    CONSTRAINT chk_monto_min CHECK (monto_minimo >= 0),
    CONSTRAINT chk_monto_max CHECK (monto_maximo >= monto_minimo),
    CONSTRAINT chk_plazos CHECK (plazo_maximo_meses >= plazo_minimo_meses)
);

-- Indexes
CREATE INDEX idx_tipos_credito_activo ON tipos_credito(activo);
CREATE INDEX idx_tipos_credito_codigo ON tipos_credito(codigo);
```

### Table: solicitudes_credito

```sql
CREATE TABLE solicitudes_credito (
    id CHAR(36) PRIMARY KEY,
    numero_solicitud VARCHAR(25) NOT NULL,
    socio_id BIGINT NOT NULL,
    tipo_credito_id BIGINT NOT NULL,
    monto_solicitado DECIMAL(19,4) NOT NULL,
    plazo_meses INT NOT NULL,
    tasa_interes_aplicada DECIMAL(8,4),
    cuota_mensual_estimada DECIMAL(19,4),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    colateral_cuenta_id CHAR(36) NULL,
    colateral_monto_retenido DECIMAL(19,4),
    evaluacion_id CHAR(36) NULL,
    plan_amortizacion_id CHAR(36) NULL,
    referencia_desembolso VARCHAR(100) NULL,
    cuenta_destino VARCHAR(34) NULL,
    destino_credito VARCHAR(500) NULL,
    notas TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_numero_solicitud UNIQUE (numero_solicitud),
    CONSTRAINT chk_monto_solicitado CHECK (monto_solicitado > 0),
    CONSTRAINT chk_plazo_meses CHECK (plazo_meses > 0),
    CONSTRAINT chk_estado_valido CHECK (estado IN ('PENDIENTE', 'EN_EVALUACION', 'APROBADA', 'RECHAZADA', 'CANCELADA', 'DESEMBOLSADO')),
    CONSTRAINT fk_solicitud_socio FOREIGN KEY (socio_id) REFERENCES socios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_tipo FOREIGN KEY (tipo_credito_id) REFERENCES tipos_credito(id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_colateral FOREIGN KEY (colateral_cuenta_id) REFERENCES cuentas_ahorro(id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_evaluacion FOREIGN KEY (evaluacion_id) REFERENCES evaluaciones_crediticias(id) ON DELETE SET NULL,
    CONSTRAINT fk_solicitud_plan FOREIGN KEY (plan_amortizacion_id) REFERENCES planes_amortizacion(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_solicitudes_socio_id ON solicitudes_credito(socio_id);
CREATE INDEX idx_solicitudes_estado ON solicitudes_credito(estado);
CREATE INDEX idx_solicitudes_numero ON solicitudes_credito(numero_solicitud);
CREATE INDEX idx_solicitudes_fecha ON solicitudes_credito(created_at);
CREATE INDEX idx_solicitudes_tipo_credito ON solicitudes_credito(tipo_credito_id);
```

### Table: evaluaciones_crediticias

```sql
CREATE TABLE evaluaciones_crediticias (
    id CHAR(36) PRIMARY KEY,
    solicitud_id CHAR(36) NOT NULL,
    socio_id BIGINT NOT NULL,
    puntaje_antiguedad INT NOT NULL,
    puntaje_historial_ahorro INT NOT NULL,
    puntaje_capacidad_pago INT NOT NULL,
    score_interno INT NOT NULL,
    score_hash VARCHAR(64) NOT NULL,           -- SHA-256 para auditoría
    factores_serializados TEXT NOT NULL,      -- JSON del breakdown
    firma_verificable VARCHAR(128) NULL,      -- RSA signature
    evaluacion_id_original CHAR(36) NULL,     -- Para detección de modificaciones
    elegible BOOLEAN NOT NULL,
    nivel_riesgo VARCHAR(20),
    tasa_interes_final DECIMAL(8,4),
    mensaje_decision VARCHAR(500),
    evaluador VARCHAR(100),
    created_at DATETIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_evaluacion_solicitud UNIQUE (solicitud_id),
    CONSTRAINT chk_score_interno CHECK (score_interno >= 0 AND score_interno <= 100),
    CONSTRAINT chk_puntaje_antiguedad CHECK (puntaje_antiguedad >= 0 AND puntaje_antiguedad <= 30),
    CONSTRAINT chk_puntaje_historial CHECK (puntaje_historial_ahorro >= 0 AND puntaje_historial_ahorro <= 30),
    CONSTRAINT chk_puntaje_capacidad CHECK (puntaje_capacidad_pago >= 0 AND puntaje_capacidad_pago <= 40),
    CONSTRAINT chk_score_hash CHECK (LENGTH(score_hash) = 64),
    CONSTRAINT fk_evaluacion_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes_credito(id) ON DELETE RESTRICT,
    CONSTRAINT fk_evaluacion_socio FOREIGN KEY (socio_id) REFERENCES socios(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_evaluaciones_solicitud_id ON evaluaciones_crediticias(solicitud_id);
CREATE INDEX idx_evaluaciones_socio_id ON evaluaciones_crediticias(socio_id);
CREATE INDEX idx_evaluaciones_elegible ON evaluaciones_crediticias(elegible);
CREATE INDEX idx_evaluaciones_score ON evaluaciones_crediticias(score_interno);
```

### Table: planes_amortizacion

```sql
CREATE TABLE planes_amortizacion (
    id CHAR(36) PRIMARY KEY,
    solicitud_id CHAR(36) NOT NULL,
    monto_principal DECIMAL(19,4) NOT NULL,
    tasa_interes DECIMAL(8,4) NOT NULL,
    plazo_meses INT NOT NULL,
    frecuencia_pago VARCHAR(20) NOT NULL DEFAULT 'MENSUAL',
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    total_intereses DECIMAL(19,4),
    total_pagado DECIMAL(19,4) DEFAULT 0,
    saldo_pendiente DECIMAL(19,4),
    numero_cuotas INT NOT NULL,
    cuota_mensual DECIMAL(19,4),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_plan_solicitud UNIQUE (solicitud_id),
    CONSTRAINT chk_tasa_interes_plan CHECK (tasa_interes > 0 AND tasa_interes <= 1.0),
    CONSTRAINT chk_plazo_plan CHECK (plazo_meses > 0),
    CONSTRAINT chk_frecuencia CHECK (frecuencia_pago IN ('SEMANAL', 'QUINCENAL', 'MENSUAL')),
    CONSTRAINT chk_estado_plan CHECK (estado IN ('ACTIVO', 'CANCELADO', 'FINALIZADO', 'VENCIDO')),
    CONSTRAINT fk_plan_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes_credito(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_plan_solicitud_id ON planes_amortizacion(solicitud_id);
CREATE INDEX idx_plan_estado ON planes_amortizacion(estado);
CREATE INDEX idx_plan_fecha_inicio ON planes_amortizacion(fecha_inicio);
```

### Table: amortizaciones

```sql
CREATE TABLE amortizaciones (
    id CHAR(36) PRIMARY KEY,
    plan_id CHAR(36) NOT NULL,
    numero_cuota INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago DATE NULL,
    capital DECIMAL(19,4) NOT NULL,
    interes DECIMAL(19,4) NOT NULL,
    seguros DECIMAL(19,4) DEFAULT 0,
    comisiones DECIMAL(19,4) DEFAULT 0,
    monto_cuota DECIMAL(19,4) NOT NULL,
    saldo_insoluto DECIMAL(19,4),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    dias_mora INT DEFAULT 0,
    interes_mora DECIMAL(19,4) DEFAULT 0,
    monto_pagado DECIMAL(19,4) DEFAULT 0,
    referencia_pago VARCHAR(100) NULL,
    colateral_ejecutada BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    version BIGINT NOT NULL DEFAULT 0,         -- CRÍTICO: previene double-payment
    
    CONSTRAINT uk_referencia_pago UNIQUE (referencia_pago),
    CONSTRAINT chk_numero_cuota CHECK (numero_cuota > 0),
    CONSTRAINT chk_estado_amort CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'CURSO_MORA', 'CANCELADA', 'EJECUTADA')),
    CONSTRAINT chk_capital CHECK (capital >= 0),
    CONSTRAINT chk_interes CHECK (interes >= 0),
    CONSTRAINT chk_monto_cuota CHECK (monto_cuota > 0),
    CONSTRAINT chk_dias_mora CHECK (dias_mora >= 0),
    CONSTRAINT fk_amortizacion_plan FOREIGN KEY (plan_id) REFERENCES planes_amortizacion(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_amortizacion_plan_id ON amortizaciones(plan_id);
CREATE INDEX idx_amortizacion_numero_cuota ON amortizaciones(plan_id, numero_cuota);
CREATE INDEX idx_amortizacion_estado ON amortizaciones(estado);
CREATE INDEX idx_amortizacion_fecha_venc ON amortizaciones(fecha_vencimiento);
CREATE INDEX idx_amortizacion_referencia ON amortizaciones(referencia_pago);
CREATE INDEX idx_amortizacion_saldo_insoluto ON amortizaciones(saldo_insoluto);
```

---

## Relaciones

### Cardinalidad

| Relación | Tipo | Descripción |
|----------|------|-------------|
| TipoCredito → SolicitudCredito | 1:N | Un tipo de crédito puede tener muchas solicitudes |
| Socio → SolicitudCredito | 1:N | Un socio puede tener múltiples solicitudes |
| SolicitudCredito → EvaluacionCrediticia | 1:1 | Una solicitud tiene una evaluación |
| SolicitudCredito → PlanAmortizacion | 1:1 | Una solicitud aprobada tiene un plan |
| SolicitudCredito → CuentaAhorro (colateral) | N:1 | Colateral de la cuenta de ahorro |
| PlanAmortizacion → Amortizacion | 1:N | Un plan tiene múltiples cuotas |

### Integridad Referencial

| Relación | Acción en DELETE | Verificación |
|----------|------------------|--------------|
| solicitudes_credito.socio_id → socios.id | RESTRICT | No permite eliminar socio con solicitudes |
| solicitudes_credito.tipo_credito_id → tipos_credito.id | RESTRICT | No permite eliminar tipo con solicitudes |
| solicitudes_credito.colateral_cuenta_id → cuentas_ahorro.id | SET NULL | Si se elimina cuenta, colateral se desvincula |
| evaluaciones_crediticias.solicitud_id → solicitudes_credito.id | CASCADE | Si se elimina solicitud, se elimina evaluación |
| planes_amortizacion.solicitud_id → solicitudes_credito.id | CASCADE | Si se elimina solicitud, se elimina plan |
| amortizaciones.plan_id → planes_amortizacion.id | CASCADE | Si se elimina plan, se eliminan cuotas |

---

## Constraints de Validación

### Validaciones en Base de Datos

| Constraint | Tabla | Condición | Mensaje |
|------------|-------|-----------|---------|
| chk_tasa_interes | tipos_credito | tasa_interes_anual BETWEEN 0 AND 1.0 | La tasa debe estar entre 0% y 100% |
| chk_monto_max | tipos_credito | monto_maximo >= monto_minimo | Monto máximo debe ser >= monto mínimo |
| chk_plazos | tipos_credito | plazo_maximo_meses >= plazo_minimo_meses | Plazo máximo debe ser >= mínimo |
| chk_monto_solicitado | solicitudes_credito | monto_solicitado > 0 | El monto solicitado debe ser mayor a 0 |
| chk_plazo_meses | solicitudes_credito | plazo_meses > 0 | El plazo debe ser mayor a 0 |
| chk_score_interno | evaluaciones_crediticias | score_interno BETWEEN 0 AND 100 | Score debe estar entre 0 y 100 |
| chk_puntaje_antiguedad | evaluaciones_crediticias | puntaje_antiguedad BETWEEN 0 AND 30 | Puntaje antigüedad debe estar entre 0-30 |
| chk_puntaje_historial | evaluaciones_crediticias | puntaje_historial_ahorro BETWEEN 0 AND 30 | Puntaje historial debe estar entre 0-30 |
| chk_puntaje_capacidad | evaluaciones_crediticias | puntaje_capacidad_pago BETWEEN 0 AND 40 | Puntaje capacidad debe estar entre 0-40 |
| chk_score_hash | evaluaciones_crediticias | LENGTH(score_hash) = 64 | Hash de score debe ser 64 caracteres (SHA-256) |
| chk_capital | amortizaciones | capital >= 0 | Capital no puede ser negativo |
| chk_monto_cuota | amortizaciones | monto_cuota > 0 | Monto de cuota debe ser mayor a 0 |
| chk_dias_mora | amortizaciones | dias_mora >= 0 | Días de mora no puede ser negativo |

---

## Índices y Performance

### Índices Primarios

| Tabla | Índice | Columnas | Tipo | Uso |
|-------|--------|----------|------|-----|
| tipos_credito | PRIMARY | id | BTREE | PK |
| solicitudes_credito | PRIMARY | id | BTREE | PK |
| evaluaciones_crediticias | PRIMARY | id | BTREE | PK |
| planes_amortizacion | PRIMARY | id | BTREE | PK |
| amortizaciones | PRIMARY | id | BTREE | PK |

### Índices de Búsqueda

| Tabla | Índice | Columnas | Unique | Uso |
|-------|--------|----------|--------|-----|
| tipos_credito | uk_codigo | codigo | YES | Lookup por código |
| solicitudes_credito | uk_numero_solicitud | numero_solicitud | YES | Lookup por número |
| solicitudes_credito | idx_socio_id | socio_id | NO | Listado por socio |
| solicitudes_credito | idx_estado | estado | NO | Filtrado por estado |
| evaluaciones_crediticias | uk_solicitud | solicitud_id | YES | Relación 1:1 |
| evaluaciones_crediticias | idx_score | score_interno | NO | Búsqueda por score |
| planes_amortizacion | uk_solicitud_plan | solicitud_id | YES | Relación 1:1 |
| planes_amortizacion | idx_estado_plan | estado | NO | Filtrado por estado |
| amortizaciones | idx_cuota | plan_id, numero_cuota | NO | Lookup de cuota específica |
| amortizaciones | idx_vencimiento | fecha_vencimiento | NO | Búsqueda por fecha |
| amortizaciones | uk_referencia_pago | referencia_pago | YES | Idempotencia de pago |

### Recomendaciones de Partitioning

Para tablas con alto volumen (amortizaciones):
- **Partitioning por estado:** Para optimizar queries de cuotas pendientes vs pagadas
- **Partitioning por fecha_vencimiento:** Para archival de cuotas antiguas

---

## Notas de Auditoría de Seguridad

### V-C1: Score Hash Inmutable

> **Hallazgo:** El score interno no tenía verificación de integridad.
> **Corrección aplicada:** Campo `score_hash` con SHA-256 y `firma_verificable` con RSA para garantizar que el cálculo no puede ser alterado post-creación.

### V-C2: Double-Payment Prevention

> **Hallazgo:** Posible doble cobro en pagos concurrentes de cuotas.
> **Corrección aplicada:** 
> - `@Version` en `Amortizacion` para optimistic locking
> - `referencia_pago` como UNIQUE para idempotencia
> - Verificación de estado atómica con `findByIdWithLock`

### V-C3: Número de Solicitud NoEnumerable

> **Hallazgo:** Formato `SOL-CRED-YYYY-XXXXXX` era secuencial y predecible.
> **Corrección aplicada:** Uso de `SecureRandom` para generar el secuencial, haciendo enumeration imposible.

### V-C4: Validación IDOR en Consultas

> **Hallazgo:** Socio podía ver solicitudes de otros socios.
> **Corrección aplicada:** En `GET /creditos/{numeroSolicitud}` y `GET /solicitudes/{numero}`, verificar que `socioId` del token coincida con el de la solicitud.

### V-C5: Rate Limiting en Simulador

> **Hallazgo:** Endpoint público sin protección contra abuse.
> **Corrección aplicada:** Rate limiting de 10 requests/min por IP + logging de auditoría.

### V-C6: Colateral con Estado EJECUTADA

> **Hallazgo:** No había estado para colateral ejecutada en mora.
> **Corrección aplicada:** Nuevo estado `EJECUTADA` en `EstadoAmortizacion` + campo `colateral_ejecutada`.

---

## Value Objects Recomendados

### NumeroSolicitud (Value Object)

```java
@Value
@EqualsAndHashCode
public class NumeroSolicitud {
    private static final Pattern PATTERN = Pattern.compile("^SOL-CRED-\\d{4}-\\d{6}$");
    
    String valor;
    
    private NumeroSolicitud(String valor) {
        this.valor = valor;
    }
    
    public static NumeroSolicitud crear(String valor) {
        if (!PATTERN.matcher(valor).matches()) {
            throw new IllegalArgumentException("Formato inválido de número de solicitud");
        }
        return new NumeroSolicitud(valor);
    }
    
    public static NumeroSolicitud generar() {
        int year = Year.now().getValue();
        // SecureRandom para evitar enumeration
        int secuencial = new SecureRandom().nextInt(999999);
        return new NumeroSolicitud(String.format("SOL-CRED-%d-%06d", year, secuencial));
    }
}
```

### ScoreCredito (Value Object)

```java
@Value
public class ScoreCredito {
    int puntajeAntiguedad;      // 0-30
    int puntajeHistorialAhorro;  // 0-30
    int puntajeCapacidadPago;    // 0-40
    int scoreTotal;              // 0-100
    
    public static ScoreCredito calcular(int antiguedad, int historial, int capacidad) {
        int total = Math.min(30, antiguedad) + Math.min(30, historial) + Math.min(40, capacidad);
        return new ScoreCredito(
            Math.min(30, antiguedad),
            Math.min(30, historial),
            Math.min(40, capacidad),
            total
        );
    }
    
    public String toHash() {
        // Genera SHA-256 del score
        String data = String.format("%d|%d|%d|%d", puntajeAntiguedad, puntajeHistorialAhorro, puntajeCapacidadPago, scoreTotal);
        return SHA256(data);
    }
}
```

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0.0 | 2026-04-14 | @documentador | Creación inicial basada en spec PM |
| 1.0.1 | 2026-04-14 | @documentador | Integración correcciones auditoría seguridad |
| 1.0.2 | 2026-04-14 | @documentador | Agregados campos de auditoría criptográfica |
| 1.0.3 | 2026-04-14 | @documentador | Documentados constraints de seguridad |

---

## Referencias

- Especificación técnica: `/docs/modulos/creditos/SPEC.md`
- Referencia API: `/docs/modulos/creditos/API.md`
- Sistema de pagos: `/docs/modulos/creditos/PAGOS.md`
- Auditoría de seguridad: `/docs/auditorias/ULTIMA_AUDITORIA.md`