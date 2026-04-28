-- V9__seed_test_data.sql
-- Datos de prueba para desarrollo: Socio, Unidad, Cuentas y Créditos

-- 1. Insertar un Socio de Prueba
INSERT INTO socios (id, numero_socio, primer_nombre, primer_apellido, fecha_nacimiento, genero, estado_civil, tipo_documento, numero_documento, correo_electronico, telefono_principal, direccion_residencia, estado, fecha_registro)
VALUES (
    'e08ef6d3-4dd0-4648-8268-c94271742488', 
    'SOC-2026-000001', 
    'Carlos', 
    'Perez', 
    '1985-05-20', 
    'MASCULINO', 
    'CASADO', 
    'CEDULA_IDENTIDAD', 
    '20123456', 
    'carlos.perez@test.com', 
    '04121234567', 
    'Caracas, Av. Francisco de Miranda', 
    'ACTIVO', 
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- 2. Crear el Usuario para ese Socio
INSERT INTO usuarios (id, nombre_usuario, correo_electronico, password_hash, nombre_completo, rol, socio_id, cuenta_activa, fecha_creacion, ultima_modificacion)
VALUES (
    'b2222222-2222-2222-2222-222222222222',
    'socio_prueba',
    'carlos.perez@test.com',
    '$2a$12$JhUnj2YEo9eQF0vgthnDteTu0RHCdmQPcoRn6ZAD3Lkq2/z6igj..', -- Password: Admin123! (por simplicidad el mismo)
    'Carlos Perez (Socio Prueba)',
    'SOCIO',
    'e08ef6d3-4dd0-4648-8268-c94271742488',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- 3. Insertar Unidad de Transporte
INSERT INTO unidades_transporte (id, socio_id, placa, marca, modelo, ano_vehiculo, tipo_unidad, estado, soat_vencimiento)
VALUES (
    gen_random_uuid(),
    'e08ef6d3-4dd0-4648-8268-c94271742488',
    '20A11BB',
    'Encava',
    'Ent-610',
    2015,
    'BUSETA',
    'ACTIVA',
    CURRENT_DATE + INTERVAL '15 days'
) ON CONFLICT DO NOTHING;

-- 4. Crear Cuentas de Ahorro
INSERT INTO cuentas_ahorro (id, numero_cuenta, socio_id, saldo_actual, moneda, tipo_cuenta, estado, fecha_apertura)
VALUES 
(gen_random_uuid(), '01340001000000005678', 'e08ef6d3-4dd0-4648-8268-c94271742488', 12450.50, 'VES', 'CORRIENTE', 'ACTIVA', CURRENT_TIMESTAMP),
(gen_random_uuid(), '01340001000000009999', 'e08ef6d3-4dd0-4648-8268-c94271742488', 500.00, 'USD', 'CORRIENTE', 'ACTIVA', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 5. Insertar Tipos de Crédito base si no existen
INSERT INTO tipos_credito (id, codigo, nombre, tasa_interes_anual, plazo_maximo_meses, monto_maximo, activo)
VALUES (gen_random_uuid(), 'EXP_REPUESTOS', 'Crédito Express Repuestos', 24.00, 6, 500.00, true)
ON CONFLICT (codigo) DO NOTHING;

-- 6. Crear un Crédito de Prueba ya otorgado
INSERT INTO solicitudes_credito (id, numero_solicitud, socio_id, tipo_credito_id, monto_solicitado, plazo_solicitado, estado, fecha_solicitud)
SELECT 
    gen_random_uuid(), 
    'SOL-CRED-2026-0001', 
    'e08ef6d3-4dd0-4648-8268-c94271742488', 
    id, 
    200.00, 
    4, 
    'DESEMBOLSADO', 
    CURRENT_TIMESTAMP
FROM tipos_credito WHERE codigo = 'EXP_REPUESTOS' LIMIT 1
ON CONFLICT DO NOTHING;
