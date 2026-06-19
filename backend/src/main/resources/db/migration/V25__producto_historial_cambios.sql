CREATE TABLE producto_historial_cambios (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos_financiables(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(40) NOT NULL,
    campo VARCHAR(80),
    valor_anterior TEXT,
    valor_nuevo TEXT,
    estado_producto VARCHAR(20),
    actor_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_producto_historial_producto_fecha
    ON producto_historial_cambios(producto_id, created_at DESC);
