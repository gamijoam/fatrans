-- =====================================================
-- V4__migrate_rendimientos_to_uuid.sql
-- Migration: Long (IDENTITY) → UUID for rendimientos table
-- Author: UUID Migration Team
-- Date: 2026-04-15
-- =====================================================

-- 1. Add new UUID columns (nullable initially)
ALTER TABLE rendimientos ADD COLUMN id_uuid UUID;
ALTER TABLE rendimientos ADD COLUMN cuenta_ahorro_id_uuid UUID;

-- 2. Populate UUIDs for existing records
UPDATE rendimientos SET id_uuid = uuid_generate_v4() WHERE id_uuid IS NULL;
UPDATE rendimientos SET cuenta_ahorro_id_uuid = (
    SELECT id FROM cuentas_ahorro WHERE id_long_backup = rendimientos.cuenta_ahorro_id
) WHERE cuenta_ahorro_id_uuid IS NULL;

-- 3. Add NOT NULL constraints
ALTER TABLE rendimientos ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE rendimientos ALTER COLUMN cuenta_ahorro_id_uuid SET NOT NULL;

-- 4. Add UNIQUE constraint on id
ALTER TABLE rendimientos ADD CONSTRAINT uk_rendimientos_id_uuid UNIQUE (id_uuid);

-- 5. Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_rendimientos_id_uuid ON rendimientos (id_uuid);
CREATE INDEX IF NOT EXISTS idx_rendimientos_cuenta_ahorro_id_uuid ON rendimientos (cuenta_ahorro_id_uuid);

-- 6. Backup old columns (DO NOT DROP until fully validated)
ALTER TABLE rendimientos RENAME COLUMN id TO id_long_backup;
ALTER TABLE rendimientos RENAME COLUMN cuenta_ahorro_id TO cuenta_ahorro_id_long_backup;

-- 7. Rename new columns to final names
ALTER TABLE rendimientos RENAME COLUMN id_uuid TO id;
ALTER TABLE rendimientos RENAME COLUMN cuenta_ahorro_id_uuid TO cuenta_ahorro_id;

-- 8. Validate: ensure no NULL IDs
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM rendimientos WHERE id IS NULL) THEN
        RAISE EXCEPTION 'Migration failed: NULL IDs found in rendimientos';
    END IF;
    IF EXISTS (SELECT 1 FROM rendimientos WHERE cuenta_ahorro_id IS NULL) THEN
        RAISE EXCEPTION 'Migration failed: NULL cuenta_ahorro_id found in rendimientos';
    END IF;
END $$;

-- =====================================================
-- ROLLBACK SCRIPT (for emergency use only)
-- =====================================================
-- ALTER TABLE rendimientos RENAME COLUMN id TO id_uuid;
-- ALTER TABLE rendimientos RENAME COLUMN cuenta_ahorro_id TO cuenta_ahorro_id_uuid;
-- ALTER TABLE rendimientos RENAME COLUMN id_long_backup TO id;
-- ALTER TABLE rendimientos RENAME COLUMN cuenta_ahorro_id_long_backup TO cuenta_ahorro_id;
-- ALTER TABLE rendimientos DROP COLUMN IF EXISTS id_uuid;
-- ALTER TABLE rendimientos DROP COLUMN IF EXISTS cuenta_ahorro_id_uuid;
-- =====================================================