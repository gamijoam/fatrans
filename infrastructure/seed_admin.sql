-- =====================================================
-- Script para crear usuario ADMIN de prueba
-- Uso: Ejecutar después de que la DB esté corriendo
-- =====================================================

-- Credenciales del admin de prueba:
-- Nombre usuario: admin
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
    intentos_fallidos,
    fecha_bloqueo
) VALUES (
    'a1111111-1111-1111-1111-111111111111',
    'admin',
    'admin@fondoAhorro.test',
    -- Password: Admin123! (bcrypt hash generado con bcryptjs o similar)
    -- Hash generado con: bcrypt.hashSync('Admin123!', 12)
    '$2a$12$JhUnj2YEo9eQF0vgthnDteTu0RHCdmQPcoRn6ZAD3Lkq2/z6igj..',
    'Administrador del Sistema',
    'ADMIN',
    NULL,
    true,
    NOW(),
    NOW(),
    0,
    NULL
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- Verificar que se creó
SELECT id, nombre_usuario, correo_electronico, rol, cuenta_activa
FROM usuarios
WHERE nombre_usuario = 'admin';
