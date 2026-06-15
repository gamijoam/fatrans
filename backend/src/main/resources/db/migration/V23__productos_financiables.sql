CREATE TABLE productos_financiables (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(1000),
    categoria VARCHAR(40) NOT NULL,
    proveedor VARCHAR(120),
    precio NUMERIC(19,4) NOT NULL,
    moneda VARCHAR(10) NOT NULL DEFAULT 'VES',
    imagen_url VARCHAR(500),
    tipo_credito_id INTEGER NOT NULL REFERENCES tipos_credito(id),
    plazo_minimo_meses INTEGER NOT NULL,
    plazo_maximo_meses INTEGER NOT NULL,
    porcentaje_colateral NUMERIC(5,2) NOT NULL,
    requiere_aprobacion_manual BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    visible_desde TIMESTAMP,
    visible_hasta TIMESTAMP,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_productos_financiables_codigo UNIQUE (codigo),
    CONSTRAINT uk_productos_financiables_slug UNIQUE (slug),
    CONSTRAINT ck_productos_financiables_precio CHECK (precio > 0),
    CONSTRAINT ck_productos_financiables_plazo CHECK (plazo_minimo_meses >= 1 AND plazo_maximo_meses >= plazo_minimo_meses),
    CONSTRAINT ck_productos_financiables_colateral CHECK (porcentaje_colateral >= 0 AND porcentaje_colateral <= 100),
    CONSTRAINT ck_productos_financiables_estado CHECK (estado IN ('BORRADOR', 'PUBLICADO', 'PAUSADO', 'ARCHIVADO'))
);

CREATE INDEX idx_productos_financiables_estado ON productos_financiables(estado);
CREATE INDEX idx_productos_financiables_categoria ON productos_financiables(categoria);
CREATE INDEX idx_productos_financiables_tipo_credito ON productos_financiables(tipo_credito_id);

ALTER TABLE solicitudes_credito
    ADD COLUMN producto_financiable_id BIGINT REFERENCES productos_financiables(id),
    ADD COLUMN producto_nombre_snapshot VARCHAR(120),
    ADD COLUMN producto_precio_snapshot NUMERIC(19,4),
    ADD COLUMN producto_moneda_snapshot VARCHAR(10),
    ADD COLUMN producto_colateral_requerido_snapshot NUMERIC(19,4);

CREATE INDEX idx_solicitudes_producto_financiable ON solicitudes_credito(producto_financiable_id);
