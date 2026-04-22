// com/tufondo/beneficiarios/application/usecase/GetBeneficiariosBySocioUseCaseTest.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.BeneficiarioListResponseDTO;
import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.application.port.SocioQueryPort;
import com.tufondo.beneficiarios.domain.exception.SocioNoEncontradoException;
import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * Tests unitarios para GetBeneficiariosBySocioUseCase.
 * Verifica la obtención de lista de beneficiarios por socio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetBeneficiariosBySocioUseCase - Caso de Uso Listar Beneficiarios")
class GetBeneficiariosBySocioUseCaseTest {

    @Mock
    private BeneficiarioRepository repository;

    @Mock
    private SocioQueryPort socioQueryPort;

    @InjectMocks
    private GetBeneficiariosBySocioUseCase useCase;

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BENEFICIARIO_ID_1 = UUID.fromString("22222222-2222-2222-2222-222222222221");
    private static final UUID BENEFICIARIO_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Nested
    @DisplayName("Listar Beneficiarios Exitoso")
    class ListarExitosoTests {

        @Test
        @DisplayName("listar_beneficiarios_retornaLista - debe retornar lista con beneficiarios cuando existen")
        void listar_beneficiarios_retornaLista() {
            // Arrange
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
            when(repository.listarPorSocioId(SOCIO_ID)).thenReturn(crearListaBeneficiarios());

            // Act
            BeneficiarioListResponseDTO response = useCase.ejecutar(SOCIO_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.total()).isEqualTo(2);
            assertThat(response.beneficiarios()).hasSize(2);
            assertThat(response.sumaPorcentajes()).isEqualByComparingTo(new BigDecimal("100.00"));
            verify(repository).listarPorSocioId(SOCIO_ID);
        }

        private List<Beneficiario> crearListaBeneficiarios() {
            Beneficiario b1 = Beneficiario.builder()
                    .id(BENEFICIARIO_ID_1)
                    .socioId(SOCIO_ID)
                    .nombreCompleto("Juan Pérez")
                    .numeroDocumento("V-12345678")
                    .tipoDocumento(TipoDocumento.CEDULA_IDENTIDAD)
                    .parentesco(Parentesco.HIJO)
                    .porcentaje(new BigDecimal("60.00"))
                    .telefono("04121234567")
                    .activo(true)
                    .build();

            Beneficiario b2 = Beneficiario.builder()
                    .id(BENEFICIARIO_ID_2)
                    .socioId(SOCIO_ID)
                    .nombreCompleto("María Pérez")
                    .numeroDocumento("V-87654321")
                    .tipoDocumento(TipoDocumento.CEDULA_IDENTIDAD)
                    .parentesco(Parentesco.CONYUGE)
                    .porcentaje(new BigDecimal("40.00"))
                    .telefono("04141234567")
                    .activo(true)
                    .build();

            return List.of(b1, b2);
        }
    }

    @Nested
    @DisplayName("Validaciones de Socio")
    class ValidacionesSocioTests {

        @Test
        @DisplayName("listar_beneficiarios_socioNoExiste_lanzaExcepcion - debe lanzar SocioNoEncontradoException")
        void listar_beneficiarios_socioNoExiste_lanzaExcepcion() {
            // Arrange
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(SOCIO_ID))
                    .isInstanceOf(SocioNoEncontradoException.class)
                    .hasMessageContaining(SOCIO_ID.toString());

            verify(repository, never()).listarPorSocioId(SOCIO_ID);
        }
    }

    @Nested
    @DisplayName("Lista Vacía")
    class ListaVaciaTests {

        @Test
        @DisplayName("listar_beneficiarios_listaVacia_retornaCero - debe retornar lista vacía con total 0")
        void listar_beneficiarios_listaVacia_retornaCero() {
            // Arrange
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
            when(repository.listarPorSocioId(SOCIO_ID)).thenReturn(Collections.emptyList());

            // Act
            BeneficiarioListResponseDTO response = useCase.ejecutar(SOCIO_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.total()).isZero();
            assertThat(response.beneficiarios()).isEmpty();
            assertThat(response.sumaPorcentajes()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
