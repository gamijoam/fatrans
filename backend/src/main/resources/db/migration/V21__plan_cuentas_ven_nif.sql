-- ═══════════════════════════════════════════════════════════════════════
--  V21 — Plan de cuentas contable VEN-NIF + seed inicial
-- ═══════════════════════════════════════════════════════════════════════
--
--  Tabla principal: plan_cuentas
--
--  Issue #264 — bloque fundacional del EPIC Contabilidad (#263).
--
--  El plan está estructurado por niveles:
--    Nivel 1 (rubro): "1", "2", "3", "4", "5", "6"
--    Nivel 2 (grupo): "1.1", "1.2", "2.1", ...
--    Nivel 3 (cuenta): "1.1.01", "1.1.02", "2.1.01", ...
--
--  Solo las cuentas de nivel 3 (operativas, hojas) son `acepta_movimientos=TRUE`.
--  Las de nivel 1-2 son totalizadoras (suman los movimientos de sus hijas).
--
--  Referencia: estándar VEN-NIF + plan de cuentas tipo para cajas de ahorro
--  y cooperativas venezolanas (SUDECA/SUNACOOP). Sujeto a validación por
--  contador colegiado antes de uso en producción contable.
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE plan_cuentas (
    id                  UUID PRIMARY KEY,
    codigo              VARCHAR(30)  NOT NULL UNIQUE,
    nombre              VARCHAR(200) NOT NULL,
    tipo                VARCHAR(20)  NOT NULL
        CHECK (tipo IN ('ACTIVO','PASIVO','PATRIMONIO','INGRESO','EGRESO','CUENTA_ORDEN')),
    naturaleza          VARCHAR(15)  NOT NULL
        CHECK (naturaleza IN ('DEUDORA','ACREEDORA')),
    nivel               INTEGER      NOT NULL CHECK (nivel BETWEEN 1 AND 5),
    cuenta_padre_id     UUID,
    acepta_movimientos  BOOLEAN      NOT NULL,
    activa              BOOLEAN      NOT NULL DEFAULT TRUE,
    descripcion         VARCHAR(500),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version             BIGINT       NOT NULL DEFAULT 0,

    -- FK auto-referencial: padre vive en la misma tabla.
    CONSTRAINT fk_plan_cuentas_padre
        FOREIGN KEY (cuenta_padre_id) REFERENCES plan_cuentas(id)
        ON DELETE RESTRICT,

    -- Invariante: nivel=1 NO tiene padre; nivel>1 SÍ.
    CONSTRAINT chk_plan_cuentas_padre_segun_nivel
        CHECK (
            (nivel = 1 AND cuenta_padre_id IS NULL)
            OR (nivel > 1 AND cuenta_padre_id IS NOT NULL)
        ),

    -- Invariante: el formato del código matchea el patrón VEN-NIF.
    -- Acepta hasta nivel 5 ("X.YY.ZZ.WWW.VVV"). El primer dígito 1-6
    -- corresponde al tipo (validado en el dominio Java también).
    CONSTRAINT chk_plan_cuentas_codigo_formato
        CHECK (codigo ~ '^[1-6](\.[0-9]{1,3}){0,4}$')
);

COMMENT ON TABLE plan_cuentas IS
'Plan de cuentas contable según VEN-NIF / SUDECA. Jerárquico por código (1, 1.1, 1.1.01...).';

COMMENT ON COLUMN plan_cuentas.codigo IS
'Código jerárquico VEN-NIF. Primer dígito mapea a tipo (1=Activo, 2=Pasivo, 3=Patrimonio, 4=Ingreso, 5=Egreso, 6=Cuentas Orden).';

COMMENT ON COLUMN plan_cuentas.acepta_movimientos IS
'TRUE solo en cuentas hoja (operativas). Las totalizadoras (rubros/grupos) tienen FALSE y suman los movimientos de sus hijas.';

-- ═══════════════════════════════════════════════════════════════════════
--  SEED INICIAL — plan de cuentas para Fondo de Ahorro y Crédito
-- ═══════════════════════════════════════════════════════════════════════
--
--  Generamos los UUIDs con `gen_random_uuid()` (extensión pgcrypto, ya
--  habilitada en V1). Usamos CTEs para resolver las FKs entre niveles —
--  los padres se insertan primero y sus IDs se referencian en las hijas.
--
--  ⚠️  Si el contador valida cambios al plan, hacer una migration V22+
--      con los UPDATEs/INSERTs adicionales — NUNCA editar este seed in-place
--      en producción.
-- ═══════════════════════════════════════════════════════════════════════

-- ─── Nivel 1: RUBROS (6) ────────────────────────────────────────────────
WITH rubros AS (
    INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion)
    VALUES
        (gen_random_uuid(), '1', 'ACTIVO',       'ACTIVO',       'DEUDORA',   1, NULL, FALSE, TRUE, 'Bienes y derechos de la entidad.'),
        (gen_random_uuid(), '2', 'PASIVO',       'PASIVO',       'ACREEDORA', 1, NULL, FALSE, TRUE, 'Obligaciones con terceros.'),
        (gen_random_uuid(), '3', 'PATRIMONIO',   'PATRIMONIO',   'ACREEDORA', 1, NULL, FALSE, TRUE, 'Recursos propios: aportes de socios, reservas, resultados.'),
        (gen_random_uuid(), '4', 'INGRESOS',     'INGRESO',      'ACREEDORA', 1, NULL, FALSE, TRUE, 'Ingresos del ejercicio.'),
        (gen_random_uuid(), '5', 'EGRESOS',      'EGRESO',       'DEUDORA',   1, NULL, FALSE, TRUE, 'Egresos / gastos del ejercicio.'),
        (gen_random_uuid(), '6', 'CUENTAS DE ORDEN', 'CUENTA_ORDEN', 'DEUDORA', 1, NULL, FALSE, TRUE, 'Cuentas de orden (garantías, contingencias).')
    RETURNING id, codigo
)
SELECT 1; -- la INSERT-RETURNING dentro de CTE necesita un SELECT final

-- A partir de acá, los siguientes niveles usan subselects para localizar
-- el padre por código. Es más legible que cargar todo en CTEs anidadas y
-- el seed corre solo una vez al deploy inicial.

-- ─── Nivel 2: GRUPOS de ACTIVO ──────────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '1.1', 'ACTIVO DISPONIBLE',    'ACTIVO', 'DEUDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='1'), FALSE, TRUE, 'Caja y bancos. Liquidez inmediata.'),
    (gen_random_uuid(), '1.2', 'INVERSIONES TEMPORALES','ACTIVO','DEUDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='1'), FALSE, TRUE, 'Títulos valores de corto plazo.'),
    (gen_random_uuid(), '1.3', 'CARTERA DE CRÉDITOS',  'ACTIVO', 'DEUDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='1'), FALSE, TRUE, 'Créditos otorgados a asociados.'),
    (gen_random_uuid(), '1.4', 'CUENTAS POR COBRAR',   'ACTIVO', 'DEUDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='1'), FALSE, TRUE, 'Otras cuentas por cobrar.'),
    (gen_random_uuid(), '1.5', 'BIENES DE USO',        'ACTIVO', 'DEUDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='1'), FALSE, TRUE, 'Activos fijos (mobiliario, equipo, inmuebles).'),
    (gen_random_uuid(), '1.6', 'OTROS ACTIVOS',        'ACTIVO', 'DEUDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='1'), FALSE, TRUE, 'Activos diferidos y otros.');

-- ─── Nivel 3: CUENTAS de ACTIVO ─────────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    -- 1.1 Activo Disponible
    (gen_random_uuid(), '1.1.01', 'Caja Principal',             'ACTIVO', 'DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.1'), TRUE, TRUE, 'Efectivo en caja principal.'),
    (gen_random_uuid(), '1.1.02', 'Caja Chica',                 'ACTIVO', 'DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.1'), TRUE, TRUE, 'Efectivo para gastos menores.'),
    (gen_random_uuid(), '1.1.03', 'Bancos Cuenta Corriente Bs', 'ACTIVO', 'DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.1'), TRUE, TRUE, 'Saldos en cuentas corrientes en bolívares.'),
    (gen_random_uuid(), '1.1.04', 'Bancos Cuenta de Ahorro Bs', 'ACTIVO', 'DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.1'), TRUE, TRUE, 'Saldos en cuentas de ahorro en bolívares.'),
    (gen_random_uuid(), '1.1.05', 'Bancos Cuentas USD',         'ACTIVO', 'DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.1'), TRUE, TRUE, 'Saldos en cuentas en dólares (USD).'),
    -- 1.2 Inversiones
    (gen_random_uuid(), '1.2.01', 'Inversiones en Títulos Valores','ACTIVO','DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.2'), TRUE, TRUE, 'Bonos, certificados, otros títulos.'),
    -- 1.3 Cartera de Créditos
    (gen_random_uuid(), '1.3.01', 'Créditos Personales por Cobrar',  'ACTIVO','DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.3'), TRUE, TRUE, 'Saldos vigentes de créditos personales.'),
    (gen_random_uuid(), '1.3.02', 'Créditos Hipotecarios por Cobrar','ACTIVO','DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.3'), TRUE, TRUE, 'Saldos vigentes de créditos con garantía hipotecaria.'),
    (gen_random_uuid(), '1.3.03', 'Intereses por Cobrar sobre Créditos','ACTIVO','DEUDORA', 3, (SELECT id FROM plan_cuentas WHERE codigo='1.3'), TRUE, TRUE, 'Intereses devengados no cobrados.'),
    (gen_random_uuid(), '1.3.99', 'Provisión Cartera de Créditos (CR)','ACTIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='1.3'), TRUE, TRUE, 'Cuenta correctora — provisión para incobrables. Naturaleza acreedora pese a ser ACTIVO.'),
    -- 1.4 Cuentas por Cobrar
    (gen_random_uuid(), '1.4.01', 'Cuentas por Cobrar Asociados','ACTIVO','DEUDORA',  3, (SELECT id FROM plan_cuentas WHERE codigo='1.4'), TRUE, TRUE, 'Otras CxC de asociados (no créditos).'),
    (gen_random_uuid(), '1.4.02', 'Cuentas por Cobrar Personal','ACTIVO','DEUDORA',  3, (SELECT id FROM plan_cuentas WHERE codigo='1.4'), TRUE, TRUE, 'Anticipos y otros saldos del personal.'),
    -- 1.5 Bienes de Uso
    (gen_random_uuid(), '1.5.01', 'Mobiliario y Equipo',        'ACTIVO','DEUDORA',  3, (SELECT id FROM plan_cuentas WHERE codigo='1.5'), TRUE, TRUE, 'Mobiliario de oficina al costo.'),
    (gen_random_uuid(), '1.5.02', 'Equipo de Computación',      'ACTIVO','DEUDORA',  3, (SELECT id FROM plan_cuentas WHERE codigo='1.5'), TRUE, TRUE, 'Computadores, servidores, redes.'),
    (gen_random_uuid(), '1.5.03', 'Vehículos',                  'ACTIVO','DEUDORA',  3, (SELECT id FROM plan_cuentas WHERE codigo='1.5'), TRUE, TRUE, 'Flota vehicular.'),
    (gen_random_uuid(), '1.5.99', 'Depreciación Acumulada (CR)','ACTIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='1.5'), TRUE, TRUE, 'Cuenta correctora — depreciación acumulada de bienes de uso.'),
    -- 1.6 Otros Activos
    (gen_random_uuid(), '1.6.01', 'Gastos Pagados por Anticipado','ACTIVO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='1.6'), TRUE, TRUE, 'Pagos anticipados (seguros, alquileres).');

-- ─── Nivel 2: GRUPOS de PASIVO ──────────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '2.1', 'DEPÓSITOS DE ASOCIADOS',  'PASIVO','ACREEDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='2'), FALSE, TRUE, 'Captaciones: ahorros, plazos fijos.'),
    (gen_random_uuid(), '2.2', 'OBLIGACIONES FINANCIERAS','PASIVO','ACREEDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='2'), FALSE, TRUE, 'Préstamos con bancos u otras instituciones.'),
    (gen_random_uuid(), '2.3', 'CUENTAS POR PAGAR',       'PASIVO','ACREEDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='2'), FALSE, TRUE, 'Proveedores, sueldos, impuestos por pagar.'),
    (gen_random_uuid(), '2.4', 'PROVISIONES Y ACUMULACIONES','PASIVO','ACREEDORA', 2, (SELECT id FROM plan_cuentas WHERE codigo='2'), FALSE, TRUE, 'Prestaciones sociales, otras provisiones.');

-- ─── Nivel 3: CUENTAS de PASIVO ─────────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    -- 2.1 Depósitos
    (gen_random_uuid(), '2.1.01', 'Cuentas de Ahorro Bs',      'PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.1'), TRUE, TRUE, 'Saldos de cuentas de ahorro en bolívares de los socios.'),
    (gen_random_uuid(), '2.1.02', 'Cuentas de Ahorro USD',     'PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.1'), TRUE, TRUE, 'Saldos de cuentas de ahorro en dólares de los socios.'),
    (gen_random_uuid(), '2.1.03', 'Depósitos a Plazo Fijo Bs', 'PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.1'), TRUE, TRUE, 'Plazos fijos en bolívares.'),
    (gen_random_uuid(), '2.1.04', 'Intereses por Pagar Captaciones','PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.1'), TRUE, TRUE, 'Intereses devengados a favor de los depositantes, no pagados.'),
    -- 2.2 Obligaciones
    (gen_random_uuid(), '2.2.01', 'Préstamos por Pagar Instituciones','PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.2'), TRUE, TRUE, 'Préstamos con la banca u otras instituciones financieras.'),
    -- 2.3 CxP
    (gen_random_uuid(), '2.3.01', 'Cuentas por Pagar Proveedores','PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.3'), TRUE, TRUE, 'Facturas pendientes a proveedores.'),
    (gen_random_uuid(), '2.3.02', 'Sueldos y Salarios por Pagar','PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.3'), TRUE, TRUE, 'Nómina pendiente de pago.'),
    (gen_random_uuid(), '2.3.03', 'Aportes Patronales por Pagar','PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.3'), TRUE, TRUE, 'IVSS, INCES, paro forzoso, etc.'),
    (gen_random_uuid(), '2.3.04', 'Impuestos por Pagar',        'PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.3'), TRUE, TRUE, 'ISLR, IVA, otros impuestos.'),
    -- 2.4 Provisiones
    (gen_random_uuid(), '2.4.01', 'Provisión Prestaciones Sociales','PASIVO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='2.4'), TRUE, TRUE, 'Prestaciones acumuladas según LOTTT.');

-- ─── Nivel 2: GRUPOS de PATRIMONIO ──────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '3.1', 'APORTES DE LOS ASOCIADOS','PATRIMONIO','ACREEDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='3'), FALSE, TRUE, 'Capital aportado por los socios.'),
    (gen_random_uuid(), '3.2', 'RESERVAS',               'PATRIMONIO','ACREEDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='3'), FALSE, TRUE, 'Reservas legales y voluntarias.'),
    (gen_random_uuid(), '3.3', 'RESULTADOS',             'PATRIMONIO','ACREEDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='3'), FALSE, TRUE, 'Excedentes acumulados y del ejercicio.');

-- ─── Nivel 3: CUENTAS de PATRIMONIO ─────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '3.1.01', 'Aportes Sociales',      'PATRIMONIO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='3.1'), TRUE, TRUE, 'Aportes obligatorios de los asociados.'),
    (gen_random_uuid(), '3.1.02', 'Aportes Extraordinarios','PATRIMONIO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='3.1'), TRUE, TRUE, 'Aportes voluntarios o de capitalización.'),
    (gen_random_uuid(), '3.2.01', 'Reserva Legal',          'PATRIMONIO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='3.2'), TRUE, TRUE, 'Reserva obligatoria por ley.'),
    (gen_random_uuid(), '3.2.02', 'Reserva de Educación',   'PATRIMONIO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='3.2'), TRUE, TRUE, 'Para programas educativos de socios.'),
    (gen_random_uuid(), '3.2.03', 'Reserva de Solidaridad', 'PATRIMONIO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='3.2'), TRUE, TRUE, 'Para apoyo en situaciones de emergencia de socios.'),
    (gen_random_uuid(), '3.3.01', 'Excedentes Acumulados',  'PATRIMONIO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='3.3'), TRUE, TRUE, 'Excedentes de ejercicios anteriores no distribuidos.'),
    (gen_random_uuid(), '3.3.02', 'Excedente del Ejercicio','PATRIMONIO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='3.3'), TRUE, TRUE, 'Resultado neto del ejercicio en curso (cierra a 3.3.01 al fin del período).');

-- ─── Nivel 2: GRUPOS de INGRESOS ────────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '4.1', 'INGRESOS POR CARTERA DE CRÉDITOS','INGRESO','ACREEDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='4'), FALSE, TRUE, 'Intereses y comisiones de la cartera.'),
    (gen_random_uuid(), '4.2', 'INGRESOS POR INVERSIONES',        'INGRESO','ACREEDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='4'), FALSE, TRUE, 'Rendimientos sobre inversiones.'),
    (gen_random_uuid(), '4.3', 'OTROS INGRESOS',                  'INGRESO','ACREEDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='4'), FALSE, TRUE, 'Aportes especiales, donaciones, otros.');

-- ─── Nivel 3: CUENTAS de INGRESOS ───────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '4.1.01', 'Intereses sobre Créditos',  'INGRESO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='4.1'), TRUE, TRUE, 'Intereses cobrados a deudores de créditos.'),
    (gen_random_uuid(), '4.1.02', 'Comisiones por Otorgamiento','INGRESO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='4.1'), TRUE, TRUE, 'Comisiones de apertura de créditos.'),
    (gen_random_uuid(), '4.1.03', 'Intereses Moratorios',      'INGRESO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='4.1'), TRUE, TRUE, 'Mora cobrada por atrasos en pagos.'),
    (gen_random_uuid(), '4.2.01', 'Rendimientos sobre Inversiones','INGRESO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='4.2'), TRUE, TRUE, 'Intereses y dividendos de inversiones.'),
    (gen_random_uuid(), '4.3.01', 'Aportes Especiales',        'INGRESO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='4.3'), TRUE, TRUE, 'Cuotas y aportes no recurrentes de socios.'),
    (gen_random_uuid(), '4.3.02', 'Otros Ingresos Operativos', 'INGRESO','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='4.3'), TRUE, TRUE, 'Ingresos diversos no clasificados en categorías anteriores.');

-- ─── Nivel 2: GRUPOS de EGRESOS ─────────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '5.1', 'EGRESOS POR CAPTACIONES',  'EGRESO','DEUDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='5'), FALSE, TRUE, 'Intereses pagados a depositantes.'),
    (gen_random_uuid(), '5.2', 'EGRESOS OPERATIVOS',       'EGRESO','DEUDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='5'), FALSE, TRUE, 'Sueldos, servicios, depreciación.'),
    (gen_random_uuid(), '5.3', 'EGRESOS FINANCIEROS',      'EGRESO','DEUDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='5'), FALSE, TRUE, 'Intereses pagados sobre préstamos.'),
    (gen_random_uuid(), '5.4', 'IMPUESTOS Y TASAS',        'EGRESO','DEUDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='5'), FALSE, TRUE, 'ISLR, impuestos municipales, otros.');

-- ─── Nivel 3: CUENTAS de EGRESOS ────────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    -- 5.1 Captaciones
    (gen_random_uuid(), '5.1.01', 'Intereses sobre Cuentas de Ahorro','EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.1'), TRUE, TRUE, 'Intereses devengados/pagados a cuenta-ahorristas.'),
    (gen_random_uuid(), '5.1.02', 'Intereses sobre Plazo Fijo',       'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.1'), TRUE, TRUE, 'Intereses devengados/pagados sobre depósitos a plazo.'),
    -- 5.2 Operativos
    (gen_random_uuid(), '5.2.01', 'Sueldos y Salarios',                'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'Remuneración del personal.'),
    (gen_random_uuid(), '5.2.02', 'Beneficios al Personal',            'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'Bonos, cesta ticket, otros beneficios.'),
    (gen_random_uuid(), '5.2.03', 'Aportes Patronales',                'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'IVSS, INCES, paro forzoso (parte patronal).'),
    (gen_random_uuid(), '5.2.04', 'Servicios Básicos',                 'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'Luz, agua, internet, teléfono.'),
    (gen_random_uuid(), '5.2.05', 'Alquiler de Inmueble',              'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'Arrendamiento de oficinas.'),
    (gen_random_uuid(), '5.2.06', 'Materiales y Suministros',          'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'Papelería, insumos varios.'),
    (gen_random_uuid(), '5.2.07', 'Depreciación',                      'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'Gasto por depreciación de bienes de uso.'),
    (gen_random_uuid(), '5.2.08', 'Otros Gastos Operativos',           'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.2'), TRUE, TRUE, 'Gastos diversos no clasificados.'),
    -- 5.3 Financieros
    (gen_random_uuid(), '5.3.01', 'Intereses sobre Préstamos',         'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.3'), TRUE, TRUE, 'Intereses pagados sobre obligaciones financieras.'),
    -- 5.4 Impuestos
    (gen_random_uuid(), '5.4.01', 'Impuesto sobre la Renta',           'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.4'), TRUE, TRUE, 'ISLR del ejercicio.'),
    (gen_random_uuid(), '5.4.02', 'Otros Impuestos y Tasas',           'EGRESO','DEUDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='5.4'), TRUE, TRUE, 'Impuestos municipales, tasas administrativas.');

-- ─── Nivel 2 + 3: CUENTAS DE ORDEN ──────────────────────────────────────
INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '6.1', 'CUENTAS DE ORDEN DEUDORAS',  'CUENTA_ORDEN','DEUDORA',  2, (SELECT id FROM plan_cuentas WHERE codigo='6'), FALSE, TRUE, 'Garantías recibidas y otros activos contingentes.'),
    (gen_random_uuid(), '6.2', 'CUENTAS DE ORDEN ACREEDORAS','CUENTA_ORDEN','ACREEDORA',2, (SELECT id FROM plan_cuentas WHERE codigo='6'), FALSE, TRUE, 'Garantías otorgadas y pasivos contingentes.');

INSERT INTO plan_cuentas (id, codigo, nombre, tipo, naturaleza, nivel, cuenta_padre_id, acepta_movimientos, activa, descripcion) VALUES
    (gen_random_uuid(), '6.1.01', 'Garantías Recibidas',          'CUENTA_ORDEN','DEUDORA',  3, (SELECT id FROM plan_cuentas WHERE codigo='6.1'), TRUE, TRUE, 'Valor de las garantías recibidas de deudores.'),
    (gen_random_uuid(), '6.2.01', 'Garantías Otorgadas',          'CUENTA_ORDEN','ACREEDORA',3, (SELECT id FROM plan_cuentas WHERE codigo='6.2'), TRUE, TRUE, 'Avales o garantías otorgadas a terceros.');
