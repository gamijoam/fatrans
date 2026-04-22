-- =====================================================
-- V1__initial_schema.sql
-- Initial database schema for Fondo Backend
-- Author: Fondo Backend Team
-- Date: 2026-04-15
-- Note: This is a baseline script. Flyway requires a V1
--       to track the initial state before migrations.
--       All tables use UUID as primary key.
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- FLYWAY SCHEMA HISTORY TABLE
-- (Created automatically by Flyway)
-- =====================================================

-- =====================================================
-- TABLES STRUCTURE (UUID-based)
-- Note: These are here for documentation. In dev,
-- Hibernate handles creation via ddl-auto=update.
-- For production, use these scripts with Flyway.
-- =====================================================

-- Example structure (doc reference only):
-- cuentas_ahorro (
--     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
--     numero_cuenta VARCHAR(20) UNIQUE NOT NULL,
--     socio_id UUID NOT NULL,
--     saldo_actual DECIMAL(19,4) NOT NULL DEFAULT 0,
--     saldo_retenido DECIMAL(19,4) NOT NULL DEFAULT 0,
--     tasa_interes DECIMAL(8,6),
--     monto_minimo_requerido DECIMAL(19,4),
--     estado VARCHAR(20) NOT NULL,
--     tipo_cuenta VARCHAR(20) NOT NULL,
--     moneda VARCHAR(10) NOT NULL,
--     fecha_apertura TIMESTAMP NOT NULL,
--     fecha_ultima_operacion TIMESTAMP,
--     version BIGINT DEFAULT 0
-- );

-- =====================================================
-- MIGRATION NOTES
-- =====================================================
-- V2: Migrate cuentas_ahorro from Long to UUID
-- V3: Migrate movimientos from Long to UUID
-- V4: Migrate rendimientos from Long to UUID
-- =====================================================