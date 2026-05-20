package com.tufondo.contabilidad.domain.model;

import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaContableTest {

    private static final UUID PADRE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("crear() — happy paths")
    class HappyPaths {

        @Test
        @DisplayName("crea cuenta nivel 1 (rubro) sin padre")
        void rubro_nivel_1() {
            CuentaContable c = CuentaContable.crear(
                    "1", "ACTIVO", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    null, false, "Bienes y derechos");

            assertThat(c.getId()).isNotNull();
            assertThat(c.getCodigo()).isEqualTo("1");
            assertThat(c.getNivel()).isEqualTo(1);
            assertThat(c.getCuentaPadreId()).isNull();
            assertThat(c.isAceptaMovimientos()).isFalse();
            assertThat(c.isActiva()).isTrue();
            assertThat(c.getTipo()).isEqualTo(TipoCuentaContable.ACTIVO);
        }

        @Test
        @DisplayName("crea cuenta nivel 2 (grupo) con padre")
        void grupo_nivel_2() {
            CuentaContable c = CuentaContable.crear(
                    "1.1", "ACTIVO DISPONIBLE", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, PADRE_ID, false, null);

            assertThat(c.getNivel()).isEqualTo(2);
            assertThat(c.getCuentaPadreId()).isEqualTo(PADRE_ID);
        }

        @Test
        @DisplayName("crea cuenta nivel 3 (cuenta operativa) acepta movimientos")
        void cuenta_nivel_3() {
            CuentaContable c = CuentaContable.crear(
                    "1.1.01", "Caja Principal", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, PADRE_ID, true, "Caja chica de oficina");

            assertThat(c.getNivel()).isEqualTo(3);
            assertThat(c.isAceptaMovimientos()).isTrue();
            assertThat(c.getDescripcion()).isEqualTo("Caja chica de oficina");
        }

        @Test
        @DisplayName("acepta cuenta de orden con naturaleza configurable (acreedora)")
        void cuenta_orden_acreedora() {
            CuentaContable c = CuentaContable.crear(
                    "6.2", "CUENTAS DE ORDEN ACREEDORAS", TipoCuentaContable.CUENTA_ORDEN,
                    NaturalezaSaldo.ACREEDORA, PADRE_ID, false, null);
            assertThat(c.getNaturaleza()).isEqualTo(NaturalezaSaldo.ACREEDORA);
        }
    }

    @Nested
    @DisplayName("crear() — validaciones de código")
    class CodigoInvalido {

        @Test
        @DisplayName("código null → IllegalArgumentException")
        void codigo_null() {
            assertThatThrownBy(() -> CuentaContable.crear(
                    null, "X", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    null, false, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("codigo");
        }

        @Test
        @DisplayName("código vacío → IllegalArgumentException")
        void codigo_vacio() {
            assertThatThrownBy(() -> CuentaContable.crear(
                    "  ", "X", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    null, false, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "0",            // primer dígito 0 inválido
                "7",            // primer dígito 7 inválido
                "1.",           // segmento vacío
                ".1",           // empieza con punto
                "1..1",         // doble punto
                "1.abc",        // letras
                "1.1.1.1.1.1",  // demasiados niveles (6)
                "11",           // sin separador
                "1-1-01",       // separador incorrecto
        })
        @DisplayName("códigos malformados → IllegalArgumentException")
        void codigo_malformado(String codigo) {
            assertThatThrownBy(() -> CuentaContable.crear(
                    codigo, "X", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    PADRE_ID, true, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("crear() — invariantes semánticas")
    class Invariantes {

        @Test
        @DisplayName("prefijo del código DEBE coincidir con tipo")
        void prefijo_no_coincide_con_tipo() {
            assertThatThrownBy(() -> CuentaContable.crear(
                    "2.1.01", "X", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    PADRE_ID, true, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no coincide con prefijo");
        }

        @Test
        @DisplayName("nivel 1 NO debe tener padre")
        void rubro_con_padre_rechazado() {
            assertThatThrownBy(() -> CuentaContable.crear(
                    "1", "ACTIVO", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    PADRE_ID, false, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nivel 1");
        }

        @Test
        @DisplayName("nivel > 1 DEBE tener padre")
        void cuenta_sin_padre_rechazada() {
            assertThatThrownBy(() -> CuentaContable.crear(
                    "1.1", "ACTIVO DISPONIBLE", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, null, false, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debe tener cuenta padre");
        }

        @Test
        @DisplayName("nombre vacío → rechazado")
        void nombre_vacio() {
            assertThatThrownBy(() -> CuentaContable.crear(
                    "1", " ", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    null, false, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nombre > 200 chars → rechazado")
        void nombre_muy_largo() {
            String largo = "a".repeat(201);
            assertThatThrownBy(() -> CuentaContable.crear(
                    "1", largo, TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    null, false, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reconstruir() — datos desde BD")
    class Reconstruccion {

        @Test
        @DisplayName("reconstruye sin generar ID nuevo")
        void preserva_id_y_timestamps() {
            UUID id = UUID.randomUUID();
            CuentaContable c = CuentaContable.reconstruir(
                    id, "1.1.01", "Caja", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, 3, PADRE_ID, true, true,
                    null, java.time.Instant.parse("2026-01-01T00:00:00Z"),
                    java.time.Instant.parse("2026-01-02T00:00:00Z"), 1L);

            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getVersion()).isEqualTo(1L);
            assertThat(c.getCreatedAt()).isEqualTo("2026-01-01T00:00:00Z");
        }

        @Test
        @DisplayName("reconstruir falla si la BD tiene nivel incoherente con el código")
        void rechaza_nivel_incoherente() {
            assertThatThrownBy(() -> CuentaContable.reconstruir(
                    UUID.randomUUID(), "1.1.01", "Caja", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, /*nivel=*/ 2 /*WRONG*/, PADRE_ID, true, true,
                    null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nivel");
        }
    }

    @Nested
    @DisplayName("Comportamiento del modelo")
    class Comportamiento {

        @Test
        @DisplayName("desactivar() devuelve nueva instancia con activa=false")
        void desactivar() {
            CuentaContable c = CuentaContable.crear(
                    "1", "ACTIVO", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    null, false, null);
            CuentaContable desactivada = c.desactivar();

            assertThat(desactivada.isActiva()).isFalse();
            assertThat(c.isActiva()).isTrue(); // inmutabilidad
            assertThat(desactivada.getId()).isEqualTo(c.getId()); // mismo ID
        }

        @Test
        @DisplayName("esDescendienteDe() reconoce ancestros directos e indirectos")
        void es_descendiente() {
            CuentaContable cuenta = CuentaContable.crear(
                    "1.1.01", "Caja", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    PADRE_ID, true, null);

            assertThat(cuenta.esDescendienteDe("1")).isTrue();
            assertThat(cuenta.esDescendienteDe("1.1")).isTrue();
            assertThat(cuenta.esDescendienteDe("1.1.01")).isFalse(); // ella misma no es descendiente
            assertThat(cuenta.esDescendienteDe("2")).isFalse();
            assertThat(cuenta.esDescendienteDe("1.2")).isFalse(); // hermano distinto
            // Edge: "1.1" no es descendiente de "1.10" — startsWith debe ir con "."
            assertThat(cuenta.esDescendienteDe("1.10")).isFalse();
        }

        @Test
        @DisplayName("codigoPadre() extrae el código del padre desde el código de la cuenta")
        void codigo_padre() {
            CuentaContable c = CuentaContable.crear(
                    "1.1.01", "Caja", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    PADRE_ID, true, null);
            assertThat(c.codigoPadre()).isEqualTo("1.1");
        }

        @Test
        @DisplayName("codigoPadre() devuelve null para nivel 1")
        void codigo_padre_rubro() {
            CuentaContable c = CuentaContable.crear(
                    "1", "ACTIVO", TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                    null, false, null);
            assertThat(c.codigoPadre()).isNull();
        }

        @Test
        @DisplayName("igualdad se basa solo en ID")
        void equals_por_id() {
            UUID id = UUID.randomUUID();
            CuentaContable c1 = CuentaContable.reconstruir(
                    id, "1.1.01", "Caja", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, 3, PADRE_ID, true, true, null, null, null, 1L);
            CuentaContable c2 = CuentaContable.reconstruir(
                    id, "1.1.02", "Otra cuenta", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, 3, PADRE_ID, true, true, null, null, null, 1L);
            assertThat(c1).isEqualTo(c2); // mismo ID = misma entidad
        }
    }
}
