-- =====================================================
-- V2__migrate_cuentas_ahorro_to_uuid.sql
-- Migration: Long (IDENTITY) → UUID for cuentas_ahorro table
-- Author: UUID Migration Team
-- Date: 2026-04-15
-- =====================================================

-- 1. Add new UUID column (nullable initially)
ALTER TABLE cuentas_ahorro ADD COLUMN id_uuid UUID;

-- 2. Enable UUID extension if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 3. Populate UUIDs for existing records
UPDATE cuentas_ahorro SET id_uuid = uuid_generate_v4() WHERE id_uuid IS NULL;

-- 4. If there are NO existing records, generate a UUID for any NULL values
-- (This handles edge case where table is empty)
INSERT INTO cuentas_ahorro (id_uuid)
SELECT uuid_generate_v4()
WHERE NOT EXISTS (SELECT 1 FROM cuentas_ahorro WHERE id_uuid IS NULL)
AND NOT EXISTS (SELECT 1 FROM cuentas_ahorro);

-- 5. Add NOT NULL constraint
ALTER TABLE cuentas_ahorro ALTER COLUMN id_uuid SET NOT NULL;

-- 6. Add UNIQUE constraint
ALTER TABLE cuentas_ahorro ADD CONSTRAINT uk_cuentas_ahorro_id_uuid UNIQUE (id_uuid);

-- 7. Create index for performance
CREATE INDEX IF NOT EXISTS idx_cuentas_ahorro_id_uuid ON cuentas_ahorro (id_uuid);

-- 8. Backup old column (DO NOT DROP until fully validated)
ALTER TABLE cuentas_ahorro RENAME COLUMN id TO id_long_backup;

-- 9. Rename new column to final name
ALTER TABLE cuentas_ahorro RENAME COLUMN id_uuid TO id;

-- 10. Validate: ensure no NULL IDs
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM cuentas_ahorro WHERE id IS NULL) THEN
        RAISE EXCEPTION 'Migration failed: NULL IDs found in cuentas_ahorro';
    END IF;
END $$;

-- =====================================================
-- ROLLBACK SCRIPT (for emergency use only)
-- =====================================================
-- ALTER TABLE cuentas_ahorro RENAME COLUMN id TO id_uuid;
-- ALTER TABLE cuentas_ahorro RENAME COLUMN id_long_backup TO id;
-- ALTER TABLE cuentas_ahorro DROP COLUMN IF EXISTS id_uuid;
-- =====================================================