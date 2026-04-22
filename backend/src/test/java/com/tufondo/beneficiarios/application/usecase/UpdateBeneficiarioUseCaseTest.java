// com/tufondo/beneficiarios/application/usecase/UpdateBeneficiarioUseCaseTest.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.application.dto.UpdateBeneficiarioRequestDTO;
import com.tufondo.beneficiarios.application.port.SocioQueryPort;
import com.tufondo.beneficiarios.domain.exception.*;
import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import com.tufondo.beneficiarios.infrastructure.persistence.adapter.BeneficiarioAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UpdateBeneficiarioUseCase.
 * Verifica todas las reglas de negocio al actualizar un beneficiario.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBeneficiarioUseCase - Caso de Uso Actualizar Beneficiario")
class UpdateBeneficiarioUseCaseTest {

    @Mock
    private BeneficiarioRepository repository;

    @Mock
    private SocioQueryPort socioQueryPort;

    @Mock
    private BeneficiarioAuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private UpdateBeneficiarioUseCase useCase;

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BENEFICIARIO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String NUMERO_DOCUMENTO_SOCIO = "V-87654321";
    private static final String NUMERO_DOCUMENTO_BENEFICIARIO = "V-12345678";

    private Beneficiario beneficiarioExistente;
    private UpdateBeneficiarioRequestDTO updateRequest;

    @BeforeEach
    void setUp() {
        beneficiarioExistente = Beneficiario.builder()
                .id(BENEFICIARIO_ID)
                .socioId(SOCIO_ID)
                .nombreCompleto("Juan Pérez")
                .numeroDocumento(NUMERO_DOCUMENTO_BENEFICIARIO)
                .tipoDocumento(TipoDocumento.CEDULA_IDENTIDAD)
                .parentesco(Parentesco.HIJO)
                .porcentaje(new BigDecimal("50.00"))
                .telefono("04121234567")
                .activo(true)
                .fechaRegistro(Instant.now())
                .fechaActualizacion(Instant.now())
                .build();

        updateRequest = new UpdateBeneficiarioRequestDTO(
                "Juan Pérez Actualizado",
                NUMERO_DOCUMENTO_BENEFICIARIO,
                TipoDocumento.CEDULA_IDENTIDAD,
                Parentesco.HIJO,
                new BigDecimal("60.00"),
                "04121234567"
        );
    }

    @Nested
    @DisplayName("Actualizar Beneficiario Exitoso")
    class ActualizarExitosoTests {

        @Test
        @DisplayName("actualizar_beneficiario_exitoso - debe actualizar cuando todas las validaciones pasan")
        void actualizar_beneficiario_exitoso() {
            // Arrange
            when(repository.buscarPorId(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiarioExistente));
            when(repository.sumarPorcentajesPorSocioId(SOCIO_ID)).thenReturn(new BigDecimal("50.00"));
            when(repository.guardar(any(Beneficiario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BeneficiarioResponseDTO response = useCase.ejecutar(SOCIO_ID, BENEFICIARIO_ID, updateRequest, httpRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.nombreCompleto()).isEqualTo("Juan Pérez Actualizado");
            verify(repository).guardar(any(Beneficiario.class));
            verify(auditService).registrarUpdate(any(Beneficiario.class), any(Beneficiario.class), eq(httpRequest));
        }
    }

    @Nested
    @DisplayName("Validaciones de IDOR")
    class ValidacionesIdorTests {

        @Test
        @DisplayName("actualizar_beneficiario_idor_lanzaExcepcion - debe lanzar BeneficiarioNoEncontradoException cuando socioId no coincide")
        void actualizar_beneficiario_idor_lanzaExcepcion() {
            // Arrange
            UUID otroSocioId = UUID.fromString("33333333-3333-3333-3333-333333333333");
            when(repository.buscarPorId(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiarioExistente));

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(otroSocioId, BENEFICIARIO_ID, updateRequest, httpRequest))
                    .isInstanceOf(BeneficiarioNoEncontradoException.class);

            verify(repository, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("Validaciones de Beneficiario No Encontrado")
    class ValidacionesNoEncontradoTests {

        @Test
        @DisplayName("actualizar_beneficiario_noEncontrado_lanzaExcepcion - debe lanzar BeneficiarioNoEncontradoException")
        void actualizar_beneficiario_noEncontrado_lanzaExcepcion() {
            // Arrange
            when(repository.buscarPorId(BENEFICIARIO_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(SOCIO_ID, BENEFICIARIO_ID, updateRequest, httpRequest))
                    .isInstanceOf(BeneficiarioNoEncontradoException.class);

            verify(repository, never()).guardar(any());
        }
    }
}
