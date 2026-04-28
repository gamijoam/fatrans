-- =====================================================
-- V7__add_debe_cambiar_password_column.sql
-- Add debe_cambiar_password column to usuarios table
-- Author: Fondo Backend Team
-- Date: 2026-04-23
-- =====================================================

ALTER TABLE usuarios ADD COLUMN debe_cambiar_password BOOLEAN NOT NULL DEFAULT FALSE;