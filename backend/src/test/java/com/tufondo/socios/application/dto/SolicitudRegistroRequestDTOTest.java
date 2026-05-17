package com.tufondo.socios.application.dto;

import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SolicitudRegistroRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SolicitudRegistroRequestDTO createValidRequest() {
        return SolicitudRegistroRequestDTO.builder()
                .nombreCompleto("Juan Pérez García")
                .tipoDocumento(TipoDocumento.CEDULA)
                .cedula("V-12345678")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .genero(Genero.MASCULINO)
                .estadoCivil(EstadoCivil.SOLTERO)
                .correoElectronico("juan@test.com")
                .telefono("04121234567")
                .empresa("Empresa Test C.A.")
                .rifEmpresa("J-123456789-0")
                .departamento("Recursos Humanos")
                .cargo("Analista")
                .salario(new BigDecimal("1500.00"))
                .direccionEstado("Caracas")
                .direccionCiudad("Distrito Capital")
                .direccionMunicipio("Libertador")
                .direccionCalle("Av. Principal #123")
                .emergenciaNombre("María García")
                .emergenciaTelefono("04121234567")
                .emergenciaParentesco("Cónyuge")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .aceptaLocdoft(true)
                .build();
    }

    @Nested
    @DisplayName("Campos Obligatorios")
    class CamposObligatorios {

        @Test
        @DisplayName("Request válido debe pasar todas las validaciones")
        void requestValido_PasaValidaciones() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Nombre vacío falla validación")
        void nombreVacio_FallaValidacion() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setNombreCompleto("");
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("nombreCompleto");
        }

        @Test
        @DisplayName("Cédula vacía falla validación")
        void cedulaVacia_FallaValidacion() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setCedula(null);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("cedula");
        }
    }

    @Nested
    @DisplayName("Validación de Cédula")
    class ValidacionCedula {

        @ParameterizedTest
        @ValueSource(strings = {"V-1234567", "V-12345678", "E-1234567", "E-12345678"})
        @DisplayName("Cédula con formato V- o E- válido debe pasar")
        void cedulaValida_Pasa(String cedula) {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setCedula(cedula);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("cedula");
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345678", "A-12345678", "V12345678", "V-1234567A", "V-123456789"})
        @DisplayName("Cédula con formato inválido debe fallar")
        void cedulaInvalida_Falla(String cedula) {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setCedula(cedula);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("cedula");
        }
    }

    @Nested
    @DisplayName("Validación de RIF")
    class ValidacionRIF {

        @Test
        @DisplayName("RIF vacío es válido (opcional)")
        void rifVacio_EsValido() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setRifEmpresa("");
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("rifEmpresa");
        }

        @Test
        @DisplayName("RIF null es válido (opcional)")
        void rifNull_EsValido() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setRifEmpresa(null);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("rifEmpresa");
        }
    }

    @Nested
    @DisplayName("Validación de Fecha Nacimiento")
    class ValidacionFechaNacimiento {

        @Test
        @DisplayName("Fecha pasada debe pasar")
        void fechaPasada_Pasa() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setFechaNacimiento(LocalDate.now().minusYears(30));
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("fechaNacimiento");
        }

        @Test
        @DisplayName("Issue #204: persona con exactamente 18 años recién cumplidos pasa")
        void edad_18_exacta_Pasa() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setFechaNacimiento(LocalDate.now().minusYears(18));
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("fechaNacimiento");
        }

        @Test
        @DisplayName("Issue #204: persona con 17 años NO debe pasar (bloqueo LOPNNA)")
        void edad_17_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setFechaNacimiento(LocalDate.now().minusYears(17));
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("fechaNacimiento");
        }

        @Test
        @DisplayName("Issue #204: persona con 17 años y 364 días NO debe pasar")
        void edad_17_y_364_dias_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            // Fecha que da exactamente 17 años + 364 días (un día antes del 18 cumpleaños)
            request.setFechaNacimiento(LocalDate.now().minusYears(18).plusDays(1));
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("fechaNacimiento");
        }

        @Test
        @DisplayName("Issue #204: fecha futura falla (cubre tanto @Past como @MayorDeEdad)")
        void fechaFutura_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setFechaNacimiento(LocalDate.now().plusDays(1));
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("fechaNacimiento");
        }
    }

    @Nested
    @DisplayName("Validación de Consentimientos")
    class ValidacionConsentimientos {

        @Test
        @DisplayName("AceptaTerminos false debe fallar")
        void aceptaTerminosFalse_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setAceptaTerminos(false);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("aceptaTerminos");
        }

        @Test
        @DisplayName("AceptaLopdp false debe fallar")
        void aceptaLopdpFalse_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setAceptaLopdp(false);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("aceptaLopdp");
        }

        // Issue #218 PR-B — la declaración LOCDOFT es obligatoria
        @Test
        @DisplayName("AceptaLocdoft false debe fallar")
        void aceptaLocdoftFalse_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setAceptaLocdoft(false);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("aceptaLocdoft");
        }

        @Test
        @DisplayName("AceptaLocdoft null debe fallar")
        void aceptaLocdoftNull_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setAceptaLocdoft(null);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("aceptaLocdoft");
        }

        @Test
        @DisplayName("Consentimientos null deben fallar")
        void consentimientosNull_Fallan() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setAceptaTerminos(null);
            request.setAceptaLopdp(null);
            request.setAceptaLocdoft(null);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("aceptaTerminos", "aceptaLopdp", "aceptaLocdoft");
        }
    }

    @Nested
    @DisplayName("Validación de Email")
    class ValidacionEmail {

        @ParameterizedTest
        @ValueSource(strings = {"test@test.com", "user.name@domain.co.ve"})
        @DisplayName("Email válido debe pasar")
        void emailValido_Pasa(String email) {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setCorreoElectronico(email);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("correoElectronico");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "test@", "@test.com", ""})
        @DisplayName("Email inválido debe fallar")
        void emailInvalido_Falla(String email) {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setCorreoElectronico(email);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("correoElectronico");
        }
    }

    @Nested
    @DisplayName("Validación de Teléfono")
    class ValidacionTelefono {

        @ParameterizedTest
        @ValueSource(strings = {"04121234567", "4121234567", "02121234567"})
        @DisplayName("Teléfono válido debe pasar")
        void telefonoValido_Pasa(String telefono) {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setTelefono(telefono);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("telefono");
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "abc", ""})
        @DisplayName("Teléfono inválido debe fallar")
        void telefonoInvalido_Falla(String telefono) {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setTelefono(telefono);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("telefono");
        }
    }

    @Nested
    @DisplayName("Validación de Salario")
    class ValidacionSalario {

        @Test
        @DisplayName("Salario válido debe pasar")
        void salarioValido_Pasa() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setSalario(new BigDecimal("500.00"));
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).doesNotContain("salario");
        }

        @Test
        @DisplayName("Salario cero debe fallar")
        void salarioCero_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setSalario(BigDecimal.ZERO);
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("salario");
        }

        @Test
        @DisplayName("Salario negativo debe fallar")
        void salarioNegativo_Falla() {
            SolicitudRegistroRequestDTO request = createValidRequest();
            request.setSalario(new BigDecimal("-100.00"));
            Set<String> violations = validator.validate(request).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(violations).contains("salario");
        }
    }
}