-- =====================================================
-- V8__create_password_history.sql
-- Create password history table for preventing password reuse
-- Author: Fondo Backend Team
-- Date: 2026-04-23
-- =====================================================

CREATE TABLE IF NOT EXISTS password_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_history_usuario_id ON password_history(usuario_id);
CREATE INDEX idx_password_history_fecha ON password_history(fecha_creacion);

-- Keep only last 5 passwords per user
CREATE OR REPLACE FUNCTION cleanup_password_history()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM password_history
    WHERE usuario_id = NEW.usuario_id
    AND id NOT IN (
        SELECT id FROM password_history
        WHERE usuario_id = NEW.usuario_id
        ORDER BY fecha_creacion DESC
        LIMIT 5
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;