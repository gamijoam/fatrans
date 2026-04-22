// com/tufondo/beneficiarios/application/usecase/DeleteBeneficiarioUseCaseTest.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.DeleteBeneficiarioResponseDTO;
import com.tufondo.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
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
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DeleteBeneficiarioUseCase.
 * Verifica el soft delete y advertencias de porcentaje.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteBeneficiarioUseCase - Caso de Uso Eliminar Beneficiario")
class DeleteBeneficiarioUseCaseTest {

    @Mock
    private BeneficiarioRepository repository;

    @Mock
    private BeneficiarioAuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private DeleteBeneficiarioUseCase useCase;

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BENEFICIARIO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private Beneficiario beneficiarioExistente;

    @BeforeEach
    void setUp() {
        beneficiarioExistente = Beneficiario.builder()
                .id(BENEFICIARIO_ID)
                .socioId(SOCIO_ID)
                .nombreCompleto("Juan Pérez")
                .numeroDocumento("V-12345678")
                .tipoDocumento(TipoDocumento.CEDULA_IDENTIDAD)
                .parentesco(Parentesco.HIJO)
                .porcentaje(new BigDecimal("50.00"))
                .telefono("04121234567")
                .activo(true)
                .fechaRegistro(Instant.now())
                .fechaActualizacion(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Eliminar Beneficiario Exitoso")
    class EliminarExitosoTests {

        @Test
        @DisplayName("eliminar_beneficiario_exitoso - debe marcar como inactivo y sin warning cuando suma es 100%")
        void eliminar_beneficiario_exitoso() {
            // Arrange
            when(repository.buscarPorId(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiarioExistente));
            when(repository.guardar(any(Beneficiario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(repository.sumarPorcentajesPorSocioId(SOCIO_ID)).thenReturn(new BigDecimal("100.00"));

            // Act
            DeleteBeneficiarioResponseDTO response = useCase.ejecutar(SOCIO_ID, BENEFICIARIO_ID, httpRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(BENEFICIARIO_ID);
            assertThat(response.socioId()).isEqualTo(SOCIO_ID);
            assertThat(response.activo()).isFalse();
            assertThat(response.warning()).isNull();
            assertThat(response.sumaPorcentajesRestantes()).isEqualByComparingTo(new BigDecimal("100.00"));

            verify(auditService).registrarDelete(any(Beneficiario.class), eq(httpRequest));
        }
    }

    @Nested
    @DisplayName("Validaciones de IDOR")
    class ValidacionesIdorTests {

        @Test
        @DisplayName("eliminar_beneficiario_idor_lanzaExcepcion - debe lanzar BeneficiarioNoEncontradoException cuando socioId no coincide")
        void eliminar_beneficiario_idor_lanzaExcepcion() {
            // Arrange
            UUID otroSocioId = UUID.fromString("33333333-3333-3333-3333-333333333333");
            when(repository.buscarPorId(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiarioExistente));

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(otroSocioId, BENEFICIARIO_ID, httpRequest))
                    .isInstanceOf(BeneficiarioNoEncontradoException.class);

            verify(repository, never()).guardar(any());
            verify(auditService, never()).registrarDelete(any(), any());
        }
    }

    @Nested
    @DisplayName("Advertencia de Porcentaje")
    class AdvertenciaPorcentajeTests {

        @Test
        @DisplayName("eliminar_beneficiario_warningPorcentaje - debe retornar warning cuando suma no es 100%")
        void eliminar_beneficiario_warningPorcentaje() {
            // Arrange - después de eliminar, la suma restante es 50%
            when(repository.buscarPorId(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiarioExistente));
            when(repository.guardar(any(Beneficiario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(repository.sumarPorcentajesPorSocioId(SOCIO_ID)).thenReturn(new BigDecimal("50.00"));

            // Act
            DeleteBeneficiarioResponseDTO response = useCase.ejecutar(SOCIO_ID, BENEFICIARIO_ID, httpRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.warning()).isNotNull();
            assertThat(response.warning()).containsPattern("50[,.]00%");
            assertThat(response.sumaPorcentajesRestantes()).isEqualByComparingTo(new BigDecimal("50.00"));
        }
    }
}
