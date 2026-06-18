CREATE TABLE producto_imagenes (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos_financiables(id) ON DELETE CASCADE,
    imagen_url VARCHAR(500) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    mime_type VARCHAR(60) NOT NULL,
    size_bytes BIGINT NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    orden INTEGER NOT NULL DEFAULT 0,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_producto_imagenes_producto ON producto_imagenes(producto_id);
CREATE INDEX idx_producto_imagenes_producto_activa ON producto_imagenes(producto_id, activa, orden);

INSERT INTO producto_imagenes (
    producto_id,
    imagen_url,
    storage_key,
    mime_type,
    size_bytes,
    width,
    height,
    es_principal,
    activa,
    orden,
    created_at,
    updated_at
)
SELECT
    id,
    imagen_url,
    imagen_url,
    'image/jpeg',
    0,
    1,
    1,
    TRUE,
    TRUE,
    0,
    NOW(),
    NOW()
FROM productos_financiables
WHERE imagen_url IS NOT NULL AND imagen_url <> '';
