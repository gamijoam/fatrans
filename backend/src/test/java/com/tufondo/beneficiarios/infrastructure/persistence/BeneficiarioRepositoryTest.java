// com/tufondo/beneficiarios.infrastructure.persistence.BeneficiarioRepositoryTest.java
package com.tufondo.beneficiarios.infrastructure.persistence;

import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para BeneficiarioRepository.
 * Verifica operaciones de persistencia con PostgreSQL real.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BeneficiarioRepository - Tests de Integración")
class BeneficiarioRepositoryTest {

    @Autowired
    private BeneficiarioRepository repository;

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String NUMERO_DOCUMENTO = "V-12345678";

    @BeforeEach
    void setUp() {
        // Limpiar datos existentes del socio
        List<Beneficiario> existentes = repository.listarPorSocioId(SOCIO_ID);
        for (Beneficiario b : existentes) {
            b.marcarInactivo();
            repository.guardar(b);
        }
    }

    @AfterEach
    void tearDown() {
        // Limpiar después de cada test
        List<Beneficiario> existentes = repository.listarPorSocioId(SOCIO_ID);
        for (Beneficiario b : existentes) {
            b.marcarInactivo();
            repository.guardar(b);
        }
    }

    private Beneficiario crearBeneficiario(String numeroDocumento, BigDecimal porcentaje) {
        return Beneficiario.crear(
                SOCIO_ID,
                "Juan Pérez " + numeroDocumento,
                numeroDocumento,
                TipoDocumento.CEDULA_IDENTIDAD,
                Parentesco.HIJO,
                porcentaje,
                "04121234567"
        );
    }

    @Nested
    @DisplayName("Guardar Beneficiario")
    class GuardarTests {

        @Test
        @DisplayName("guardar_beneficiario_persiste - debe persistir beneficiario y retornarlo con ID")
        void guardar_beneficiario_persiste() {
            // Arrange
            Beneficiario beneficiario = crearBeneficiario("V-11111111", new BigDecimal("50.00"));

            // Act
            Beneficiario saved = repository.guardar(beneficiario);

            // Assert
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFechaRegistro()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Buscar Por ID")
    class BuscarPorIdTests {

        @Test
        @DisplayName("buscarPorId_retornaBeneficiario - debe retornar beneficiario activo encontrado")
        void buscarPorId_retornaBeneficiario() {
            // Arrange
            Beneficiario saved = repository.guardar(crearBeneficiario("V-22222222", new BigDecimal("30.00")));

            // Act
            Optional<Beneficiario> found = repository.buscarPorId(saved.getId());

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getNumeroDocumento()).isEqualTo("V-22222222");
        }

        @Test
        @DisplayName("buscarPorId_inactivo_retornaVacio - no debe retornar beneficiario inactivo")
        void buscarPorId_inactivo_retornaVacio() {
            // Arrange
            Beneficiario saved = repository.guardar(crearBeneficiario("V-33333333", new BigDecimal("25.00")));
            saved.marcarInactivo();
            repository.guardar(saved);

            // Act
            Optional<Beneficiario> found = repository.buscarPorId(saved.getId());

            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Listar Por Socio ID")
    class ListarPorSocioIdTests {

        @Test
        @DisplayName("listarPorSocioId_retornaSoloActivos - debe retornar solo beneficiarios activos")
        void listarPorSocioId_retornaSoloActivos() {
            // Arrange
            Beneficiario activo1 = repository.guardar(crearBeneficiario("V-44444441", new BigDecimal("20.00")));
            Beneficiario activo2 = repository.guardar(crearBeneficiario("V-44444442", new BigDecimal("30.00")));
            Beneficiario inactivo = crearBeneficiario("V-44444443", new BigDecimal("50.00"));
            inactivo.marcarInactivo();
            repository.guardar(inactivo);

            // Act
            List<Beneficiario> activos = repository.listarPorSocioId(SOCIO_ID);

            // Assert
            assertThat(activos).hasSize(2);
            assertThat(activos).extracting(Beneficiario::getNumeroDocumento)
                    .containsExactlyInAnyOrder("V-44444441", "V-44444442");
        }
    }

    @Nested
    @DisplayName("Count Activos Por Socio ID")
    class CountActivosTests {

        @Test
        @DisplayName("countActivosPorSocioId_cuentaCorrectamente - debe contar solo beneficiarios activos")
        void countActivosPorSocioId_cuentaCorrectamente() {
            // Arrange
            repository.guardar(crearBeneficiario("V-55555551", new BigDecimal("25.00")));
            repository.guardar(crearBeneficiario("V-55555552", new BigDecimal("25.00")));
            Beneficiario inactivo = crearBeneficiario("V-55555553", new BigDecimal("50.00"));
            inactivo.marcarInactivo();
            repository.guardar(inactivo);

            // Act
            int count = repository.countActivosPorSocioId(SOCIO_ID);

            // Assert
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Existe Por Documento")
    class ExistePorDocumentoTests {

        @Test
        @DisplayName("existePorDocumento_detectaDuplicado - debe detectar documento duplicado activo")
        void existePorDocumento_detectaDuplicado() {
            // Arrange
            String documentoDuplicado = "V-66666666";
            repository.guardar(crearBeneficiario(documentoDuplicado, new BigDecimal("50.00")));

            // Act
            boolean existe = repository.existePorDocumento(
                    SOCIO_ID,
                    TipoDocumento.CEDULA_IDENTIDAD,
                    documentoDuplicado,
                    null
            );

            // Assert
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("existePorDocumento_noDetectaInactivo - no debe detectar documento de beneficiario inactivo")
        void existePorDocumento_noDetectaInactivo() {
            // Arrange
            String documentoUnico = "V-77777777";
            Beneficiario inactivo = crearBeneficiario(documentoUnico, new BigDecimal("50.00"));
            inactivo.marcarInactivo();
            repository.guardar(inactivo);

            // Act
            boolean existe = repository.existePorDocumento(
                    SOCIO_ID,
                    TipoDocumento.CEDULA_IDENTIDAD,
                    documentoUnico,
                    null
            );

            // Assert
            assertThat(existe).isFalse();
        }

        @Test
        @DisplayName("existePorDocumento_excluyePropioId - no debe detectar duplicado excluyendo el ID propio (para updates)")
        void existePorDocumento_excluyePropioId() {
            // Arrange
            String documento = "V-88888888";
            Beneficiario existente = repository.guardar(crearBeneficiario(documento, new BigDecimal("50.00")));

            // Act
            boolean existe = repository.existePorDocumento(
                    SOCIO_ID,
                    TipoDocumento.CEDULA_IDENTIDAD,
                    documento,
                    existente.getId()
            );

            // Assert
            assertThat(existe).isFalse();
        }
    }
}
