-- Seed data for tipos_credito (issue #61)
-- Run this script to populate the catalog

INSERT INTO tipos_credito (codigo, nombre, descripcion, tasa_interes_anual, plazo_minimo_meses, plazo_maximo_meses, monto_minimo, monto_maximo, porcentaje_requerimiento_colateral, comision_apertura, penalidad_mora_tasa, dias_gracia, activo, created_at, updated_at)
VALUES
('MICRO_CRED', 'Micro Crédito', 'Crédito para pequeñas necesidades financieras. Ideal para emergencias y gastos imprevistos. Sin garantías inmobiliarias.', 0.2400, 1, 12, 1000.0000, 50000.0000, 0.10, 0.0050, 0.0010, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('CRED_PERSONAL', 'Crédito Personal', 'Crédito de consumo personal para cualquier destino. Plazos flexibles y tasas competitivas.', 0.1450, 6, 48, 5000.0000, 200000.0000, 0.15, 0.0030, 0.0005, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('CRED_VEHICULO', 'Crédito Vehículo', 'Financiamiento para adquisición de vehículos nuevos o usados. Tasa preferencial con garantía del vehículo.', 0.1050, 12, 72, 50000.0000, 1000000.0000, 0.20, 0.0020, 0.0005, 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('CRED_HIPOTECARIO', 'Crédito Hipotecario', 'Financiamiento para compra, construcción o mejora de vivienda. Plazos largos con garantía hipotecaria.', 0.0850, 24, 240, 100000.0000, 5000000.0000, 0.30, 0.0010, 0.0002, 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('CRED_EDUCATIVO', 'Crédito Educativo', 'Financiamiento para gastos de educación superior, posgrados y cursos de especialización.', 0.0950, 6, 60, 10000.0000, 500000.0000, 0.10, 0.0025, 0.0003, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('CRED_EMPRENDEDOR', 'Crédito Emprendedor', 'Financiamiento para microempresarios y pequeños negocios. Apoyo para capital de trabajo e inversión.', 0.1800, 3, 36, 5000.0000, 150000.0000, 0.15, 0.0040, 0.0008, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('TARJETA_CREDITO', 'Tarjeta de Crédito', 'Línea de crédito rotativa con tasa competitiva y gracia de 45 días para compras.', 0.2200, 0, 0, 5000.0000, 100000.0000, 0.00, 0.0200, 0.0050, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);