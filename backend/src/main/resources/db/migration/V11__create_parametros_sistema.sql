-- V11__create_parametros_sistema.sql
-- Tabla de parámetros configurables del sistema

CREATE TABLE parametros_sistema (
    param_key VARCHAR(100) PRIMARY KEY,
    valor VARCHAR(500) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('STRING', 'NUMERIC', 'BOOLEAN', 'DATE', 'PERCENTAGE', 'CURRENCY')),
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL,
    editable BOOLEAN DEFAULT true,
    fecha_actualizacion TIMESTAMP,
    actualizado_por UUID
);

-- Índices para búsqueda eficiente
CREATE INDEX idx_parametros_categoria ON parametros_sistema(categoria);
CREATE INDEX idx_parametros_editable ON parametros_sistema(editable);

-- Comentario de tabla
COMMENT ON TABLE parametros_sistema IS 'Parámetros configurables del sistema (tasas, límites, comisiones)';

-- Parámetros iniciales - TASA (Tasas de interés)
INSERT INTO parametros_sistema (param_key, valor, tipo, descripcion, categoria, editable) VALUES
('TASA_INTERES_AHORRO', '0.05', 'PERCENTAGE', 'Tasa de interés anual para cuentas de ahorro', 'TASA', true),
('TASA_INTERES_MORA', '0.024', 'PERCENTAGE', 'Tasa de mora mensual para créditos', 'TASA', true),
('TASA_COMISION_ADMIN', '0.01', 'PERCENTAGE', 'Comisión administrativa sobre monto de crédito', 'TASA', true);

-- Parámetros iniciales - LIMITE (Límites de operaciones)
INSERT INTO parametros_sistema (param_key, valor, tipo, descripcion, categoria, editable) VALUES
('LIMITE_RETIRO_DIARIO', '5000000', 'CURRENCY', 'Límite máximo de retiro diario en VES', 'LIMITE', true),
('LIMITE_DEPOSITO_DIARIO', '10000000', 'CURRENCY', 'Límite máximo de depósito diario en VES', 'LIMITE', true),
('LIMITE_TRANSFERENCIA', '5000000', 'CURRENCY', 'Límite máximo de transferencia en VES', 'LIMITE', true),
('LIMITE_MONTO_CREDITO_MIN', '1000000', 'CURRENCY', 'Monto mínimo para solicitud de crédito', 'LIMITE', true),
('LIMITE_MONTO_CREDITO_MAX', '100000000', 'CURRENCY', 'Monto máximo para solicitud de crédito', 'LIMITE', true);

-- Parámetros iniciales - COMISION (Comisiones)
INSERT INTO parametros_sistema (param_key, valor, tipo, descripcion, categoria, editable) VALUES
('COMISION_RETIRO', '1000', 'CURRENCY', 'Comisión fija por retiro en VES', 'COMISION', true),
('COMISION_TRANSFERENCIA', '500', 'CURRENCY', 'Comisión por transferencia en VES', 'COMISION', true),
('COMISION_ATM_OTRO_BANCO', '2000', 'CURRENCY', 'Comisión por uso de ATM otro banco en VES', 'COMISION', true);

-- Parámetros iniciales - CUENTA (Configuración de cuentas)
INSERT INTO parametros_sistema (param_key, valor, tipo, descripcion, categoria, editable) VALUES
('MONTO_MINIMO_APERTURA', '50000', 'CURRENCY', 'Monto mínimo para apertura de cuenta de ahorro', 'CUENTA', true),
('SALDO_MINIMO_REQUERIDO', '10000', 'CURRENCY', 'Saldo mínimo requerido en cuenta', 'CUENTA', true),
('DIAS_INACTIVIDAD_BLOQUEO', '90', 'NUMERIC', 'Días de inactividad antes de bloquear cuenta', 'CUENTA', true);

-- Parámetros iniciales - KYC (Verificación de identidad)
INSERT INTO parametros_sistema (param_key, valor, tipo, descripcion, categoria, editable) VALUES
('KYC_NIVEL_BASICO_MAX_MONTO', '5000000', 'CURRENCY', 'Monto máximo de operaciones para KYC nivel básico', 'KYC', true),
('KYC_NIVEL_INTERMEDIO_MAX_MONTO', '25000000', 'CURRENCY', 'Monto máximo de operaciones para KYC nivel intermedio', 'KYC', true),
('KYC_DIAS_EXPIRACION', '365', 'NUMERIC', 'Días hasta expiración de verificación KYC', 'KYC', true);

-- Parámetros iniciales - SISTEMA (Configuración general)
INSERT INTO parametros_sistema (param_key, valor, tipo, descripcion, categoria, editable) VALUES
('NOMBRE_EMPRESA', 'Fondo de Ahorro TuFondo', 'STRING', 'Nombre de la empresa/institución', 'SISTEMA', false),
('MONEDA_PRINCIPAL', 'VES', 'STRING', 'Moneda principal del sistema', 'SISTEMA', false),
('MONEDA_SECUNDARIA', 'USD', 'STRING', 'Moneda secundaria del sistema', 'SISTEMA', false),
('TASA_CAMBIO_USD', '50.00', 'CURRENCY', 'Tasa de cambio USD a VES', 'SISTEMA', true),
('PAIS_OPERACION', 'VE', 'STRING', 'Código de país ISO 3166-1 alpha-2', 'SISTEMA', false);

-- Parámetros no editables del sistema
INSERT INTO parametros_sistema (param_key, valor, tipo, descripcion, categoria, editable) VALUES
('VERSION_SISTEMA', '1.0.0', 'STRING', 'Versión actual del sistema', 'SISTEMA', false),
('MAX_SESIONES_POR_USUARIO', '5', 'NUMERIC', 'Número máximo de sesiones activas por usuario', 'SISTEMA', false);