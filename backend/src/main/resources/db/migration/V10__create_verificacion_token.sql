-- V10__create_verificacion_token.sql
-- Tabla para tokens de verificación de cambios sensibles

CREATE TABLE verificacion_token (
    id UUID PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    usuario_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor VARCHAR(255),
    codigo VARCHAR(10),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    intentos INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_verificacion_token ON verificacion_token(token);
CREATE INDEX idx_verificacion_usuario ON verificacion_token(usuario_id);
CREATE INDEX idx_verificacion_expira ON verificacion_token(expires_at);