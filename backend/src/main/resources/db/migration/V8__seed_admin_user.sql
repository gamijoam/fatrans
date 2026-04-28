-- V8__seed_admin_user.sql
-- Creación del usuario ADMINISTRADOR inicial por defecto

-- Credenciales:
-- Usuario: admin
-- Password: Admin123!
-- Rol: ADMIN

INSERT INTO usuarios (
    id,
    nombre_usuario,
    correo_electronico,
    password_hash,
    nombre_completo,
    rol,
    socio_id,
    cuenta_activa,
    fecha_creacion,
    ultima_modificacion,
    intentos_fallidos
) VALUES (
    'a1111111-1111-1111-1111-111111111111',
    'admin',
    'admin@tufondo.com.ve',
    '$2a$12$JhUnj2YEo9eQF0vgthnDteTu0RHCdmQPcoRn6ZAD3Lkq2/z6igj..',
    'Administrador TuFondo',
    'SUPER_ADMIN',
    NULL,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
) ON CONFLICT (nombre_usuario) DO NOTHING;
