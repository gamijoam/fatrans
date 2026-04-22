// com/tufondo/beneficiarios/domain/model/BeneficiarioTest.java
package com.tufondo.beneficiarios.domain.model;

import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para la entidad Beneficiario.
 * Verifica comportamiento del dominio puro.
 */
@DisplayName("Beneficiario - Entidad de Dominio")
class BeneficiarioTest {

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String NOMBRE = "Juan Pérez";
    private static final String NUMERO_DOCUMENTO = "V-12345678";
    private static final TipoDocumento TIPO_DOCUMENTO = TipoDocumento.CEDULA_IDENTIDAD;
    private static final Parentesco PARENTESCO = Parentesco.HIJO;
    private static final BigDecimal PORCENTAJE = new BigDecimal("50.00");
    private static final String TELEFONO = "04121234567";

    @Nested
    @DisplayName("Crear Beneficiario")
    class CrearBeneficiarioTests {

        @Test
        @DisplayName("crear_beneficiario_valido - debe crear beneficiario con estado activo")
        void crear_beneficiario_valido() {
            // Arrange & Act
            Beneficiario beneficiario = Beneficiario.crear(
                    SOCIO_ID, NOMBRE, NUMERO_DOCUMENTO, TIPO_DOCUMENTO, PARENTESCO, PORCENTAJE, TELEFONO
            );

            // Assert
            assertThat(beneficiario).isNotNull();
            assertThat(beneficiario.getSocioId()).isEqualTo(SOCIO_ID);
            assertThat(beneficiario.getNombreCompleto()).isEqualTo(NOMBRE);
            assertThat(beneficiario.getNumeroDocumento()).isEqualTo(NUMERO_DOCUMENTO);
            assertThat(beneficiario.getTipoDocumento()).isEqualTo(TIPO_DOCUMENTO);
            assertThat(beneficiario.getParentesco()).isEqualTo(PARENTESCO);
            assertThat(beneficiario.getPorcentaje()).isEqualTo(PORCENTAJE);
            assertThat(beneficiario.getTelefono()).isEqualTo(TELEFONO);
            assertThat(beneficiario.isActivo()).isTrue();
            assertThat(beneficiario.getFechaRegistro()).isNotNull();
            assertThat(beneficiario.getFechaActualizacion()).isNotNull();
        }

        @Test
        @DisplayName("crear_beneficiario_con_fechaActualizacion - fechaActualizacion debe ser igual a fechaRegistro al crear")
        void crear_beneficiario_con_fechaActualizacion() {
            // Arrange
            Instant antes = Instant.now().minusSeconds(1);

            // Act
            Beneficiario beneficiario = Beneficiario.crear(
                    SOCIO_ID, NOMBRE, NUMERO_DOCUMENTO, TIPO_DOCUMENTO, PARENTESCO, PORCENTAJE, TELEFONO
            );

            Instant despues = Instant.now().plusSeconds(1);

            // Assert
            assertThat(beneficiario.getFechaRegistro()).isAfterOrEqualTo(antes);
            assertThat(beneficiario.getFechaRegistro()).isBeforeOrEqualTo(despues);
            assertThat(beneficiario.getFechaActualizacion()).isEqualTo(beneficiario.getFechaRegistro());
        }
    }

    @Nested
    @DisplayName("Marcar Inactivo")
    class MarcarInactivoTests {

        @Test
        @DisplayName("marcarInactivo_cambiaEstado - debe cambiar activo a false y actualizar fecha")
        void marcarInactivo_cambiaEstado() {
            // Arrange
            Beneficiario beneficiario = Beneficiario.crear(
                    SOCIO_ID, NOMBRE, NUMERO_DOCUMENTO, TIPO_DOCUMENTO, PARENTESCO, PORCENTAJE, TELEFONO
            );
            Instant fechaActualizacionAntes = beneficiario.getFechaActualizacion();

            // Act
            beneficiario.marcarInactivo();

            // Assert
            assertThat(beneficiario.isActivo()).isFalse();
            assertThat(beneficiario.getFechaActualizacion()).isAfter(fechaActualizacionAntes);
        }
    }

    @Nested
    @DisplayName("Validar Porcentaje")
    class ValidarPorcentajeTests {

        @Test
        @DisplayName("tienePorcentajeValido_acepta_valoresValidos - debe retornar true para valores entre 0.01 y 100.00")
        void tienePorcentajeValido_acepta_valoresValidos() {
            // Arrange
            Beneficiario beneficiario = Beneficiario.crear(
                    SOCIO_ID, NOMBRE, NUMERO_DOCUMENTO, TIPO_DOCUMENTO, PARENTESCO, PORCENTAJE, TELEFONO
            );

            // Act & Assert - boundary cases
            beneficiario.setPorcentaje(new BigDecimal("0.01"));
            assertThat(beneficiario.tienePorcentajeValido()).isTrue();

            beneficiario.setPorcentaje(new BigDecimal("50.00"));
            assertThat(beneficiario.tienePorcentajeValido()).isTrue();

            beneficiario.setPorcentaje(new BigDecimal("100.00"));
            assertThat(beneficiario.tienePorcentajeValido()).isTrue();
        }

        @Test
        @DisplayName("tienePorcentajeValido_rechaza_valoresInvalidos - debe retornar false para valores fuera de rango")
        void tienePorcentajeValido_rechaza_valoresInvalidos() {
            // Arrange
            Beneficiario beneficiario = Beneficiario.crear(
                    SOCIO_ID, NOMBRE, NUMERO_DOCUMENTO, TIPO_DOCUMENTO, PARENTESCO, PORCENTAJE, TELEFONO
            );

            // Act & Assert - valores inválidos
            beneficiario.setPorcentaje(new BigDecimal("0.00"));
            assertThat(beneficiario.tienePorcentajeValido()).isFalse();

            beneficiario.setPorcentaje(new BigDecimal("100.01"));
            assertThat(beneficiario.tienePorcentajeValido()).isFalse();

            beneficiario.setPorcentaje(new BigDecimal("-1.00"));
            assertThat(beneficiario.tienePorcentajeValido()).isFalse();

            beneficiario.setPorcentaje(null);
            assertThat(beneficiario.tienePorcentajeValido()).isFalse();
        }
    }
}
