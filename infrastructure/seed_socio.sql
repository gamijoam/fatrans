-- =====================================================
-- Script para crear SOCIO de prueba
-- Credenciales: socio1 / Socio123!
-- =====================================================

-- Limpiar datos previos si existen
DELETE FROM usuarios WHERE nombre_usuario = 'socio1';
DELETE FROM cuentas_ahorro WHERE numero_cuenta = 'AHO-2026-000001';
DELETE FROM socios WHERE numero_socio = 'SOC-2026-000001';

-- Crear SOCIO
INSERT INTO socios (
    id, numero_socio, tipo_documento, numero_documento,
    primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
    nombre, fecha_nacimiento, genero, estado_civil,
    correo_electronico, telefono_principal, telefono_secundario,
    empresa, departamento, cargo, tipo_contrato,
    salario, monto_ahorro, numero_cuenta_nomina, banco_nomina,
    estado, fecha_ingreso, fecha_registro, fecha_actualizacion,
    fecha_activacion
) VALUES (
    'b2222222-2222-2222-2222-222222222222',
    'SOC-2026-000001',
    'CEDULA',
    'V-30123456',
    'Juan',
    'Carlos',
    'Perez',
    'Rodriguez',
    'Juan Carlos Perez Rodriguez',
    '1990-05-15',
    'MASCULINO',
    'SOLTERO',
    'juan.perez@email.com',
    '0412-1234567',
    '0414-7654321',
    'Empresa XYZ',
    'Recursos Humanos',
    'Analista',
    'INDEFINIDO',
    1500.00,
    0.00,
    '0123-4567-89-0123456789',
    'Banco de Venezuela',
    'ACTIVO',
    '2020-01-15',
    NOW(),
    NOW(),
    NOW()
);

-- Crear USUARIO vinculado al socio
INSERT INTO usuarios (
    id, nombre_usuario, correo_electronico,
    password_hash, nombre_completo, rol, socio_id,
    cuenta_activa, fecha_creacion, ultima_modificacion,
    intentos_fallidos, fecha_bloqueo
) VALUES (
    'u2222222-2222-2222-2222-222222222222',
    'socio1',
    'socio1@fondoAhorro.test',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4pWGj7iJlEQo.7e',
    'Juan Carlos Perez Rodriguez',
    'SOCIO',
    'b2222222-2222-2222-2222-222222222222',
    true,
    NOW(),
    NOW(),
    0,
    NULL
);

-- Crear CUENTA DE AHORRO
INSERT INTO cuentas_ahorro (
    id, numero_cuenta, socio_id,
    saldo_actual, saldo_retenido,
    tasa_interes, monto_minimo_requerido,
    estado, tipo_cuenta, moneda,
    fecha_apertura, fecha_ultima_operacion,
    version
) VALUES (
    'c2222222-2222-2222-2222-222222222222',
    'AHO-2026-000001',
    'b2222222-2222-2222-2222-222222222222',
    5000.00,
    0.00,
    0.0150,
    100.00,
    'ACTIVA',
    'AHORRO',
    'VES',
    NOW(),
    NOW(),
    0
);

-- Verificar
SELECT '=== SOCIOS ===' as info;
SELECT id, numero_socio, nombre, estado FROM socios WHERE numero_socio = 'SOC-2026-000001';

SELECT '=== USUARIOS ===' as info;
SELECT id, nombre_usuario, correo_electronico, rol FROM usuarios WHERE nombre_usuario = 'socio1';

SELECT '=== CUENTAS ===' as info;
SELECT id, numero_cuenta, saldo_actual, estado FROM cuentas_ahorro WHERE numero_cuenta = 'AHO-2026-000001';
