-- V8__create_parametros_sistema.sql
CREATE TABLE parametros_sistema (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    clave VARCHAR(100) UNIQUE NOT NULL,
    valor TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(50),
    editable BOOLEAN DEFAULT TRUE,
    ultima_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
