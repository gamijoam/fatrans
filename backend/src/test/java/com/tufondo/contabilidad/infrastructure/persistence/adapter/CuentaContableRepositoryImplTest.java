package com.tufondo.contabilidad.infrastructure.persistence.adapter;

import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import com.tufondo.contabilidad.infrastructure.persistence.jpa.CuentaContableJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración del adapter del plan de cuentas con H2 (modo Postgres).
 *
 * <p>Verifican el round-trip domain → entity → BD → entity → domain, queries
 * derivadas (por código, por tipo, hijas), y que las invariantes del modelo
 * se respeten al re-leer desde BD.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import(CuentaContableRepositoryImpl.class)
@DisplayName("CuentaContableRepositoryImpl - integration con H2")
class CuentaContableRepositoryImplTest {

    @Autowired
    private CuentaContableRepositoryImpl repository;

    @Autowired
    private CuentaContableJpaRepository jpaRepository;

    @BeforeEach
    void limpiarBd() {
        jpaRepository.deleteAll();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private CuentaContable insertarRubro(String codigo, String nombre, TipoCuentaContable tipo) {
        return repository.guardar(CuentaContable.crear(
                codigo, nombre, tipo, tipo.naturalezaNatural(),
                null, false, null));
    }

    private CuentaContable insertarGrupo(String codigo, String nombre, TipoCuentaContable tipo, UUID padre) {
        return repository.guardar(CuentaContable.crear(
                codigo, nombre, tipo, tipo.naturalezaNatural(),
                padre, false, null));
    }

    private CuentaContable insertarCuenta(String codigo, String nombre, TipoCuentaContable tipo, UUID padre) {
        return repository.guardar(CuentaContable.crear(
                codigo, nombre, tipo, tipo.naturalezaNatural(),
                padre, true, null));
    }

    // ─── CRUD básico ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("CRUD básico")
    class Crud {

        @Test
        @DisplayName("guardar() + buscarPorId() — round-trip preserva todos los campos")
        void round_trip_completo() {
            UUID padreId = insertarRubro("1", "ACTIVO", TipoCuentaContable.ACTIVO).getId();
            CuentaContable original = CuentaContable.crear(
                    "1.1.01", "Caja Principal", TipoCuentaContable.ACTIVO,
                    NaturalezaSaldo.DEUDORA, padreId, true, "Caja del local principal");
            CuentaContable saved = repository.guardar(original);

            CuentaContable leida = repository.buscarPorId(saved.getId()).orElseThrow();

            assertThat(leida.getId()).isEqualTo(saved.getId());
            assertThat(leida.getCodigo()).isEqualTo("1.1.01");
            assertThat(leida.getNombre()).isEqualTo("Caja Principal");
            assertThat(leida.getTipo()).isEqualTo(TipoCuentaContable.ACTIVO);
            assertThat(leida.getNaturaleza()).isEqualTo(NaturalezaSaldo.DEUDORA);
            assertThat(leida.getNivel()).isEqualTo(3);
            assertThat(leida.getCuentaPadreId()).isEqualTo(padreId);
            assertThat(leida.isAceptaMovimientos()).isTrue();
            assertThat(leida.isActiva()).isTrue();
            assertThat(leida.getDescripcion()).isEqualTo("Caja del local principal");
            assertThat(leida.getCreatedAt()).isNotNull();
            assertThat(leida.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("buscarPorId() inexistente devuelve Optional.empty()")
        void buscar_inexistente() {
            assertThat(repository.buscarPorId(UUID.randomUUID())).isEmpty();
        }

        @Test
        @DisplayName("buscarPorCodigo() encuentra cuenta existente")
        void buscar_por_codigo() {
            insertarRubro("2", "PASIVO", TipoCuentaContable.PASIVO);
            assertThat(repository.buscarPorCodigo("2")).isPresent();
            assertThat(repository.buscarPorCodigo("99.9")).isEmpty();
        }

        @Test
        @DisplayName("existePorCodigo() detecta cuentas creadas")
        void existe_por_codigo() {
            insertarRubro("3", "PATRIMONIO", TipoCuentaContable.PATRIMONIO);
            assertThat(repository.existePorCodigo("3")).isTrue();
            assertThat(repository.existePorCodigo("4")).isFalse();
        }

        @Test
        @DisplayName("desactivar() persiste el cambio")
        void desactivar_persiste() {
            CuentaContable c = insertarRubro("4", "INGRESOS", TipoCuentaContable.INGRESO);
            repository.guardar(c.desactivar());

            assertThat(repository.buscarPorId(c.getId()).orElseThrow().isActiva()).isFalse();
        }
    }

    // ─── Queries jerárquicas ─────────────────────────────────────────────

    @Nested
    @DisplayName("Queries jerárquicas")
    class Jerarquia {

        @Test
        @DisplayName("listarTodas() devuelve ordenado por código asc")
        void listar_todas_ordenadas() {
            UUID activo = insertarRubro("1", "ACTIVO", TipoCuentaContable.ACTIVO).getId();
            UUID pasivo = insertarRubro("2", "PASIVO", TipoCuentaContable.PASIVO).getId();
            insertarGrupo("1.1", "DISPONIBLE", TipoCuentaContable.ACTIVO, activo);
            insertarGrupo("2.1", "DEPÓSITOS", TipoCuentaContable.PASIVO, pasivo);

            List<CuentaContable> todas = repository.listarTodas();
            assertThat(todas).extracting(CuentaContable::getCodigo)
                    .containsExactly("1", "1.1", "2", "2.1");
        }

        @Test
        @DisplayName("listarPorTipo() filtra correctamente")
        void listar_por_tipo() {
            UUID activo = insertarRubro("1", "ACTIVO", TipoCuentaContable.ACTIVO).getId();
            UUID pasivo = insertarRubro("2", "PASIVO", TipoCuentaContable.PASIVO).getId();
            insertarGrupo("1.1", "DISPONIBLE", TipoCuentaContable.ACTIVO, activo);
            insertarGrupo("2.1", "DEPÓSITOS", TipoCuentaContable.PASIVO, pasivo);

            assertThat(repository.listarPorTipo(TipoCuentaContable.ACTIVO))
                    .extracting(CuentaContable::getCodigo)
                    .containsExactly("1", "1.1");
            assertThat(repository.listarPorTipo(TipoCuentaContable.PASIVO))
                    .extracting(CuentaContable::getCodigo)
                    .containsExactly("2", "2.1");
        }

        @Test
        @DisplayName("listarConMovimientos() solo trae hojas activas")
        void listar_movimientos() {
            UUID activo = insertarRubro("1", "ACTIVO", TipoCuentaContable.ACTIVO).getId();
            UUID disponible = insertarGrupo("1.1", "DISPONIBLE", TipoCuentaContable.ACTIVO, activo).getId();
            CuentaContable caja = insertarCuenta("1.1.01", "Caja", TipoCuentaContable.ACTIVO, disponible);
            CuentaContable banco = insertarCuenta("1.1.02", "Banco", TipoCuentaContable.ACTIVO, disponible);
            // Desactivar Banco — no debería aparecer
            repository.guardar(banco.desactivar());

            List<CuentaContable> hojas = repository.listarConMovimientos();
            assertThat(hojas).extracting(CuentaContable::getCodigo).containsExactly("1.1.01");
            // El rubro y grupo (no aceptan movimientos) no aparecen
            assertThat(hojas).noneMatch(c -> c.getCodigo().equals("1"));
            assertThat(hojas).noneMatch(c -> c.getCodigo().equals("1.1"));
        }

        @Test
        @DisplayName("listarHijasDirectas() devuelve solo hijas directas, no nietas")
        void listar_hijas_directas() {
            UUID activo = insertarRubro("1", "ACTIVO", TipoCuentaContable.ACTIVO).getId();
            CuentaContable disp = insertarGrupo("1.1", "DISPONIBLE", TipoCuentaContable.ACTIVO, activo);
            CuentaContable inv = insertarGrupo("1.2", "INVERSIONES", TipoCuentaContable.ACTIVO, activo);
            insertarCuenta("1.1.01", "Caja", TipoCuentaContable.ACTIVO, disp.getId());
            insertarCuenta("1.1.02", "Banco", TipoCuentaContable.ACTIVO, disp.getId());

            List<CuentaContable> hijasDeActivo = repository.listarHijasDirectas(activo);
            assertThat(hijasDeActivo).extracting(CuentaContable::getCodigo)
                    .containsExactly("1.1", "1.2"); // grupos, no las cuentas
        }

        @Test
        @DisplayName("contar() refleja inserts")
        void contar() {
            assertThat(repository.contar()).isZero();
            insertarRubro("1", "ACTIVO", TipoCuentaContable.ACTIVO);
            insertarRubro("2", "PASIVO", TipoCuentaContable.PASIVO);
            assertThat(repository.contar()).isEqualTo(2);
        }
    }

    // ─── Optimistic locking ──────────────────────────────────────────────

    @Test
    @DisplayName("@Version incrementa en actualizaciones")
    void version_se_incrementa() {
        CuentaContable c = insertarRubro("1", "ACTIVO", TipoCuentaContable.ACTIVO);
        Long version1 = repository.buscarPorId(c.getId()).orElseThrow().getVersion();

        repository.guardar(c.toBuilder().nombre("ACTIVO REVISADO").build());
        Long version2 = repository.buscarPorId(c.getId()).orElseThrow().getVersion();

        assertThat(version2).isGreaterThan(version1);
    }
}
