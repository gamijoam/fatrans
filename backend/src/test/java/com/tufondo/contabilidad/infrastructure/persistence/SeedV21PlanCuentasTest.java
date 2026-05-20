package com.tufondo.contabilidad.infrastructure.persistence;

import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del seed inicial del plan de cuentas {@code V21__plan_cuentas_ven_nif.sql}.
 *
 * <p>Estos NO ejecutan SQL — leen el archivo de migration como texto, parsean
 * los INSERTs estilo regex, y validan invariantes contables: códigos únicos,
 * prefijo coincide con tipo, padres existen, naturaleza coherente con tipo
 * (con excepciones documentadas para cuentas correctoras y de orden), nivel
 * coincide con el código.</p>
 *
 * <p>Es un test puro Java (sin Spring) — rápido, sirve también como
 * documentación viva del seed: si alguien edita V21 mal, este test marca
 * el bug antes del deploy.</p>
 */
class SeedV21PlanCuentasTest {

    /** Path al archivo de migration, relativo al directorio del módulo backend. */
    private static final Path MIGRATION_PATH = Paths.get(
            "src/main/resources/db/migration/V21__plan_cuentas_ven_nif.sql");

    /**
     * Parser de los INSERTs del SQL. Matchea filas con el formato:
     *   (gen_random_uuid(), 'codigo', 'nombre', 'TIPO', 'NATURALEZA', nivel, ...,
     *    aceptaMov, activa, descripcion)
     *
     * Capturamos: codigo (1), nombre (2), tipo (3), naturaleza (4), nivel (5),
     *             acepta_movimientos (6), activa (7).
     *
     * El padre lo derivamos del código (todo lo que esté antes del último ".")
     * — más robusto que parsear el subselect SQL.
     */
    private static final Pattern PATTERN_FILA = Pattern.compile(
            "\\(gen_random_uuid\\(\\),\\s*" +
                    "'([^']+)',\\s*" +            // 1: codigo
                    "'((?:[^']|'')+)',\\s*" +     // 2: nombre (puede tener apóstrofes escapados '')
                    "'(ACTIVO|PASIVO|PATRIMONIO|INGRESO|EGRESO|CUENTA_ORDEN)',\\s*" + // 3: tipo
                    "'(DEUDORA|ACREEDORA)',\\s*" +// 4: naturaleza
                    "(\\d+),\\s*" +               // 5: nivel
                    "(?:[^,]+),\\s*" +            // padre (subselect o NULL, lo ignoramos)
                    "(TRUE|FALSE),\\s*" +         // 6: acepta_movimientos
                    "(TRUE|FALSE)"                // 7: activa
    );

    private static final Map<Character, TipoCuentaContable> PREFIJO_A_TIPO = Map.of(
            '1', TipoCuentaContable.ACTIVO,
            '2', TipoCuentaContable.PASIVO,
            '3', TipoCuentaContable.PATRIMONIO,
            '4', TipoCuentaContable.INGRESO,
            '5', TipoCuentaContable.EGRESO,
            '6', TipoCuentaContable.CUENTA_ORDEN
    );

    /**
     * Cuentas correctoras conocidas — su naturaleza es opuesta al tipo madre.
     * Ejemplo: 1.3.99 "Provisión Cartera de Créditos" es de tipo ACTIVO pero
     * naturaleza ACREEDORA (resta del activo bruto). Lo declaramos explícito
     * para que el test no falle.
     */
    private static final Set<String> CUENTAS_CORRECTORAS_O_DE_ORDEN_ACREEDORAS = Set.of(
            "1.3.99",  // Provisión Cartera de Créditos
            "1.5.99",  // Depreciación Acumulada
            "6.2",     // Cuentas de Orden Acreedoras (grupo)
            "6.2.01"   // Garantías Otorgadas
    );

    private record CuentaSeed(
            String codigo, String nombre, TipoCuentaContable tipo,
            NaturalezaSaldo naturaleza, int nivel, boolean aceptaMovimientos,
            boolean activa) {}

    /** Parsea el archivo SQL una sola vez por test (no es caro). */
    private List<CuentaSeed> parsearSeed() throws IOException {
        String sql = Files.readString(MIGRATION_PATH);
        Matcher m = PATTERN_FILA.matcher(sql);
        List<CuentaSeed> cuentas = new ArrayList<>();
        while (m.find()) {
            cuentas.add(new CuentaSeed(
                    m.group(1),
                    m.group(2),
                    TipoCuentaContable.valueOf(m.group(3)),
                    NaturalezaSaldo.valueOf(m.group(4)),
                    Integer.parseInt(m.group(5)),
                    Boolean.parseBoolean(m.group(6).toLowerCase()),
                    Boolean.parseBoolean(m.group(7).toLowerCase())
            ));
        }
        return cuentas;
    }

    @Test
    @DisplayName("El archivo V21 existe y se puede leer")
    void archivo_existe() throws IOException {
        assertThat(MIGRATION_PATH).exists();
        assertThat(Files.size(MIGRATION_PATH)).isGreaterThan(1000); // sanity: no es un archivo vacío
    }

    @Test
    @DisplayName("Parsea al menos 50 cuentas del seed (sanity check)")
    void parsea_cantidad_razonable() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        assertThat(cuentas).hasSizeGreaterThanOrEqualTo(50);
    }

    @Test
    @DisplayName("Códigos son únicos (constraint UNIQUE en BD pero validamos antes)")
    void codigos_unicos() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        Set<String> vistos = new HashSet<>();
        List<String> duplicados = new ArrayList<>();
        for (CuentaSeed c : cuentas) {
            if (!vistos.add(c.codigo())) duplicados.add(c.codigo());
        }
        assertThat(duplicados)
                .as("códigos duplicados encontrados en el seed")
                .isEmpty();
    }

    @Test
    @DisplayName("Existen los 6 rubros raíz (nivel 1)")
    void seis_rubros_nivel_1() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        List<CuentaSeed> rubros = cuentas.stream().filter(c -> c.nivel() == 1).toList();
        assertThat(rubros).hasSize(6);
        assertThat(rubros).extracting(CuentaSeed::codigo)
                .containsExactlyInAnyOrder("1", "2", "3", "4", "5", "6");
    }

    @Test
    @DisplayName("Cada cuenta nivel ≥ 2 tiene un padre (por código) que existe en el seed")
    void padres_existen() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        Set<String> codigosExistentes = new HashSet<>();
        cuentas.forEach(c -> codigosExistentes.add(c.codigo()));

        List<String> huerfanas = new ArrayList<>();
        for (CuentaSeed c : cuentas) {
            if (c.nivel() == 1) continue;
            int idx = c.codigo().lastIndexOf('.');
            String codigoPadre = c.codigo().substring(0, idx);
            if (!codigosExistentes.contains(codigoPadre)) {
                huerfanas.add(c.codigo() + " (esperaba padre " + codigoPadre + ")");
            }
        }
        assertThat(huerfanas)
                .as("cuentas sin padre en el seed")
                .isEmpty();
    }

    @Test
    @DisplayName("Nivel coincide con número de segmentos del código")
    void nivel_coincide_con_codigo() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        List<String> incoherentes = new ArrayList<>();
        for (CuentaSeed c : cuentas) {
            int segmentos = c.codigo().split("\\.").length;
            if (c.nivel() != segmentos) {
                incoherentes.add(String.format(
                        "%s: nivel declarado %d, segmentos del código %d",
                        c.codigo(), c.nivel(), segmentos));
            }
        }
        assertThat(incoherentes).isEmpty();
    }

    @Test
    @DisplayName("Primer dígito del código coincide con el tipo de cuenta")
    void prefijo_coincide_con_tipo() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        List<String> errores = new ArrayList<>();
        for (CuentaSeed c : cuentas) {
            TipoCuentaContable esperado = PREFIJO_A_TIPO.get(c.codigo().charAt(0));
            if (esperado != c.tipo()) {
                errores.add(String.format(
                        "%s: tipo declarado %s, esperado %s",
                        c.codigo(), c.tipo(), esperado));
            }
        }
        assertThat(errores).isEmpty();
    }

    @Test
    @DisplayName("Naturaleza es coherente con el tipo (excepto correctoras conocidas)")
    void naturaleza_coherente() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        List<String> errores = new ArrayList<>();

        for (CuentaSeed c : cuentas) {
            NaturalezaSaldo esperada = c.tipo().naturalezaNatural();
            if (c.naturaleza() != esperada) {
                // Acepto si está en el whitelist de correctoras/cuentas de orden con
                // naturaleza opuesta documentada.
                if (!CUENTAS_CORRECTORAS_O_DE_ORDEN_ACREEDORAS.contains(c.codigo())) {
                    errores.add(String.format(
                            "%s (%s, %s): naturaleza %s no coincide con la natural del tipo (%s)",
                            c.codigo(), c.nombre(), c.tipo(), c.naturaleza(), esperada));
                }
            }
        }
        assertThat(errores)
                .as("cuentas con naturaleza incoherente sin justificación")
                .isEmpty();
    }

    @Test
    @DisplayName("Solo cuentas de nivel 3+ aceptan movimientos (hojas operativas)")
    void solo_hojas_aceptan_movimientos() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        List<String> errores = new ArrayList<>();

        for (CuentaSeed c : cuentas) {
            if (c.nivel() <= 2 && c.aceptaMovimientos()) {
                errores.add(c.codigo() + " (nivel " + c.nivel() + ") no debería aceptar movimientos");
            }
        }
        assertThat(errores).isEmpty();
    }

    @Test
    @DisplayName("Todas las cuentas del seed son creables por CuentaContable.crear() — invariantes del dominio se respetan")
    void todas_pasan_validaciones_del_dominio() throws IOException {
        List<CuentaSeed> cuentas = parsearSeed();
        Map<String, java.util.UUID> codigoToId = new HashMap<>();

        // Ordenar por nivel para crear primero los padres
        List<CuentaSeed> ordenadas = new ArrayList<>(cuentas);
        ordenadas.sort(Comparator.comparingInt(CuentaSeed::nivel));

        for (CuentaSeed c : ordenadas) {
            java.util.UUID padreId = null;
            if (c.nivel() > 1) {
                int idx = c.codigo().lastIndexOf('.');
                String codigoPadre = c.codigo().substring(0, idx);
                padreId = codigoToId.get(codigoPadre);
                assertThat(padreId)
                        .as("padre %s para cuenta %s no fue creado todavía (orden incorrecto?)",
                                codigoPadre, c.codigo())
                        .isNotNull();
            }
            // Si esto lanza, el seed tiene una cuenta que viola las invariantes
            // del dominio. La excepción se propaga y falla el test con el detalle.
            com.tufondo.contabilidad.domain.model.CuentaContable creada =
                    com.tufondo.contabilidad.domain.model.CuentaContable.crear(
                            c.codigo(), c.nombre(), c.tipo(), c.naturaleza(),
                            padreId, c.aceptaMovimientos(), null);
            codigoToId.put(c.codigo(), creada.getId());
        }
    }
}
