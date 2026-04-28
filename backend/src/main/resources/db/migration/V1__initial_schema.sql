-- V1__initial_schema.sql
-- ESQUEMA CORE INTEGRADO TUFONDO

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. TABLA SOCIOS
CREATE TABLE socios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_socio VARCHAR(50) UNIQUE NOT NULL,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR(20) NOT NULL,
    estado_civil VARCHAR(30) NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    numero_documento VARCHAR(50) UNIQUE NOT NULL,
    correo_electronico VARCHAR(255) UNIQUE NOT NULL,
    telefono_principal VARCHAR(20),
    telefono_secundario VARCHAR(20),
    residencia_calle TEXT,
    residencia_numero VARCHAR(20),
    residencia_colonia VARCHAR(100),
    residencia_ciudad VARCHAR(100),
    residencia_estado VARCHAR(100),
    residencia_cp VARCHAR(20),
    residencia_pais VARCHAR(100),
    laboral_calle TEXT,
    laboral_numero VARCHAR(20),
    laboral_colonia VARCHAR(100),
    laboral_ciudad VARCHAR(100),
    laboral_estado VARCHAR(100),
    laboral_cp VARCHAR(20),
    laboral_pais VARCHAR(100),
    empresa VARCHAR(100),
    departamento VARCHAR(100),
    cargo VARCHAR(100),
    tipo_contrato VARCHAR(30),
    salario DECIMAL(19,4),
    monto_ahorro DECIMAL(19,4),
    numero_cuenta_nomina VARCHAR(50),
    banco_nomina VARCHAR(100),
    contacto_emergencia_nombre VARCHAR(200),
    contacto_emergencia_parentesco VARCHAR(50),
    contacto_emergencia_telefono VARCHAR(20),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_ingreso DATE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    fecha_activacion TIMESTAMP,
    fecha_desactivacion TIMESTAMP,
    motivo_desactivacion TEXT
);

-- 2. TABLA USUARIOS
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    correo_electronico VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    socio_id UUID REFERENCES socios(id),
    cuenta_activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_modificacion TIMESTAMP,
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    fecha_bloqueo TIMESTAMP,
    debe_cambiar_password BOOLEAN DEFAULT FALSE
);

-- 3. TABLA CUENTAS AHORRO
CREATE TABLE cuentas_ahorro (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_cuenta VARCHAR(25) UNIQUE NOT NULL,
    socio_id UUID NOT NULL REFERENCES socios(id),
    saldo_actual DECIMAL(19,4) NOT NULL DEFAULT 0,
    saldo_retenido DECIMAL(19,4) NOT NULL DEFAULT 0,
    tasa_interes DECIMAL(8,6),
    monto_minimo_requerido DECIMAL(19,4),
    estado VARCHAR(20) NOT NULL,
    tipo_cuenta VARCHAR(20) NOT NULL,
    moneda VARCHAR(10) NOT NULL,
    fecha_apertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_operacion TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- 4. TABLA MOVIMIENTOS
CREATE TABLE movimientos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_operacion VARCHAR(20) UNIQUE NOT NULL,
    cuenta_ahorro_id UUID NOT NULL REFERENCES cuentas_ahorro(id),
    socio_id UUID NOT NULL REFERENCES socios(id),
    tipo VARCHAR(30) NOT NULL,
    monto DECIMAL(19,4) NOT NULL,
    saldo_anterior DECIMAL(19,4),
    saldo_posterior DECIMAL(19,4),
    descripcion VARCHAR(500),
    referencia VARCHAR(100),
    canal_origen VARCHAR(20) NOT NULL,
    ip_origen VARCHAR(45),
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    estado VARCHAR(20) NOT NULL,
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_valor DATE NOT NULL
);

-- 5. TABLA RENDIMIENTOS
CREATE TABLE rendimientos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cuenta_ahorro_id UUID NOT NULL REFERENCES cuentas_ahorro(id),
    periodo_inicio DATE NOT NULL,
    periodo_fin DATE NOT NULL,
    saldo_promedio_periodo DECIMAL(19,4),
    tasa_applied DECIMAL(8,6) NOT NULL,
    monto_rendimiento DECIMAL(19,4),
    tipo VARCHAR(20) NOT NULL,
    estado_aplicacion VARCHAR(20) NOT NULL,
    fecha_calculo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. TABLA TIPOS CREDITO
CREATE TABLE tipos_credito (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    tasa_interes_anual DECIMAL(8,4) NOT NULL,
    plazo_minimo_meses INTEGER,
    plazo_maximo_meses INTEGER,
    monto_minimo DECIMAL(19,4),
    monto_maximo DECIMAL(19,4),
    porcentaje_requerimiento_colateral DECIMAL(8,4),
    comision_apertura DECIMAL(19,4),
    penalidad_mora_tasa DECIMAL(8,4),
    dias_gracia INTEGER,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- 7. TABLA SOLICITUDES CREDITO
CREATE TABLE solicitudes_credito (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_solicitud VARCHAR(25) UNIQUE NOT NULL,
    socio_id UUID NOT NULL REFERENCES socios(id),
    tipo_credito_id INTEGER NOT NULL REFERENCES tipos_credito(id),
    monto_solicitado DECIMAL(19,4) NOT NULL,
    plazo_meses INTEGER NOT NULL,
    tasa_interes_applied DECIMAL(8,4),
    cuota_mensual_estimada DECIMAL(19,4),
    estado VARCHAR(20) NOT NULL,
    colateral_cuenta_id UUID REFERENCES cuentas_ahorro(id),
    colateral_monto_retenido DECIMAL(19,4),
    destino_credito VARCHAR(500),
    evaluacion_id UUID,
    plan_amortizacion_id UUID,
    referencia_desembolso VARCHAR(100),
    cuenta_destino VARCHAR(34),
    notas TEXT,
    fecha_aprobacion TIMESTAMP,
    fecha_rechazo TIMESTAMP,
    fecha_desembolso TIMESTAMP,
    motivo_rechazo VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- 8. TABLA PLANES AMORTIZACION
CREATE TABLE planes_amortizacion (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    solicitud_id UUID NOT NULL UNIQUE REFERENCES solicitudes_credito(id),
    monto_principal DECIMAL(19,4) NOT NULL,
    tasa_interes DECIMAL(8,4) NOT NULL,
    plazo_meses INTEGER NOT NULL,
    frecuencia_pago VARCHAR(20) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    total_intereses DECIMAL(19,4),
    total_pagado DECIMAL(19,4),
    saldo_pendiente DECIMAL(19,4),
    numero_cuotas INTEGER NOT NULL,
    cuota_mensual DECIMAL(19,4),
    estado VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- 9. TABLA AMORTIZACIONES (CUOTAS)
CREATE TABLE amortizaciones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_id UUID NOT NULL REFERENCES planes_amortizacion(id),
    numero_cuota INTEGER NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago DATE,
    capital DECIMAL(19,4) NOT NULL,
    interes DECIMAL(19,4) NOT NULL,
    seguros DECIMAL(19,4),
    comisiones DECIMAL(19,4),
    monto_cuota DECIMAL(19,4) NOT NULL,
    saldo_insoluto DECIMAL(19,4),
    estado VARCHAR(20) NOT NULL,
    dias_mora INTEGER,
    interes_mora DECIMAL(19,4),
    monto_pagado DECIMAL(19,4),
    referencia_pago VARCHAR(100) UNIQUE,
    colateral_ejecutada BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- INDICES PARA PERFORMANCE
CREATE INDEX idx_socios_documento ON socios(numero_documento);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_cuentas_socio ON cuentas_ahorro(socio_id);
CREATE INDEX idx_movimientos_cuenta ON movimientos(cuenta_ahorro_id);
CREATE INDEX idx_solicitudes_socio ON solicitudes_credito(socio_id);
CREATE INDEX idx_amortizaciones_plan ON amortizaciones(plan_id);
