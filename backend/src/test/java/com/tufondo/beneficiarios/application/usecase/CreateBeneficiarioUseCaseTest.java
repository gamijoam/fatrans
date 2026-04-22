// com/tufondo/beneficiarios/application/usecase/CreateBeneficiarioUseCaseTest.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.application.dto.CreateBeneficiarioRequestDTO;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CreateBeneficiarioUseCase.
 * Verifica todas las reglas de negocio al crear un beneficiario.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBeneficiarioUseCase - Caso de Uso Crear Beneficiario")
class CreateBeneficiarioUseCaseTest {

    @Mock
    private BeneficiarioRepository repository;

    @Mock
    private SocioQueryPort socioQueryPort;

    @Mock
    private BeneficiarioAuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private CreateBeneficiarioUseCase useCase;

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BENEFICIARIO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String NUMERO_DOCUMENTO_SOCIO = "V-87654321";
    private static final String NUMERO_DOCUMENTO_BENEFICIARIO = "V-12345678";

    private CreateBeneficiarioRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateBeneficiarioRequestDTO(
                "Juan Pérez",
                NUMERO_DOCUMENTO_BENEFICIARIO,
                TipoDocumento.CEDULA_IDENTIDAD,
                Parentesco.HIJO,
                new BigDecimal("50.00"),
                "04121234567"
        );
    }

    private Beneficiario crearBeneficiarioMock() {
        return Beneficiario.builder()
                .id(BENEFICIARIO_ID)
                .socioId(SOCIO_ID)
                .nombreCompleto(createRequest.nombreCompleto())
                .numeroDocumento(createRequest.numeroDocumento())
                .tipoDocumento(createRequest.tipoDocumento())
                .parentesco(createRequest.parentesco())
                .porcentaje(createRequest.porcentaje())
                .telefono(createRequest.telefono())
                .activo(true)
                .build();
    }

    @Nested
    @DisplayName("Crear Beneficiario Exitoso")
    class CrearExitosoTests {

        @Test
        @DisplayName("crear_beneficiario_exitoso - debe crear beneficiario cuando todas las validaciones pasan")
        void crear_beneficiario_exitoso() {
            // Arrange
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
            when(socioQueryPort.getNumeroDocumentoById(SOCIO_ID)).thenReturn(NUMERO_DOCUMENTO_SOCIO);
            when(repository.existePorDocumento(eq(SOCIO_ID), any(TipoDocumento.class), anyString(), any())).thenReturn(false);
            when(repository.countActivosPorSocioId(SOCIO_ID)).thenReturn(0);
            when(repository.sumarPorcentajesPorSocioId(SOCIO_ID)).thenReturn(BigDecimal.ZERO);
            when(repository.guardar(any(Beneficiario.class))).thenAnswer(invocation -> {
                Beneficiario b = invocation.getArgument(0);
                b.setId(BENEFICIARIO_ID);
                return b;
            });

            // Act
            BeneficiarioResponseDTO response = useCase.ejecutar(SOCIO_ID, createRequest, httpRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(BENEFICIARIO_ID);
            assertThat(response.socioId()).isEqualTo(SOCIO_ID);
            verify(repository).guardar(any(Beneficiario.class));
            verify(auditService).registrarCreate(any(Beneficiario.class), eq(httpRequest));
        }
    }

    @Nested
    @DisplayName("Validaciones de Socio")
    class ValidacionesSocioTests {

        @Test
        @DisplayName("crear_beneficiario_socioNoExiste_lanzaExcepcion - debe lanzar SocioNoEncontradoException")
        void crear_beneficiario_socioNoExiste_lanzaExcepcion() {
            // Arrange
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(SOCIO_ID, createRequest, httpRequest))
                    .isInstanceOf(SocioNoEncontradoException.class)
                    .hasMessageContaining(SOCIO_ID.toString());

            verify(repository, never()).guardar(any());
            verify(auditService, never()).registrarCreate(any(), any());
        }
    }

    @Nested
    @DisplayName("Validaciones de Documento")
    class ValidacionesDocumentoTests {

        @Test
        @DisplayName("crear_beneficiario_documentoIgualAlTitular_lanzaExcepcion - debe lanzar DocumentoIgualAlTitularException")
        void crear_beneficiario_documentoIgualAlTitular_lanzaExcepcion() {
            // Arrange - beneficiario con mismo documento que el socio titular
            CreateBeneficiarioRequestDTO requestConDocumentoIgual = new CreateBeneficiarioRequestDTO(
                    "Juan Pérez",
                    NUMERO_DOCUMENTO_SOCIO, // mismo documento que socio
                    TipoDocumento.CEDULA_IDENTIDAD,
                    Parentesco.HIJO,
                    new BigDecimal("50.00"),
                    "04121234567"
            );

            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
            when(socioQueryPort.getNumeroDocumentoById(SOCIO_ID)).thenReturn(NUMERO_DOCUMENTO_SOCIO);

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(SOCIO_ID, requestConDocumentoIgual, httpRequest))
                    .isInstanceOf(DocumentoIgualAlTitularException.class);

            verify(repository, never()).guardar(any());
        }

        @Test
        @DisplayName("crear_beneficiario_duplicado_lanzaExcepcion - debe lanzar BeneficiarioDuplicadoException")
        void crear_beneficiario_duplicado_lanzaExcepcion() {
            // Arrange
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
            when(socioQueryPort.getNumeroDocumentoById(SOCIO_ID)).thenReturn(NUMERO_DOCUMENTO_SOCIO);
            when(repository.existePorDocumento(eq(SOCIO_ID), any(TipoDocumento.class), eq(NUMERO_DOCUMENTO_BENEFICIARIO), any()))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(SOCIO_ID, createRequest, httpRequest))
                    .isInstanceOf(BeneficiarioDuplicadoException.class)
                    .hasMessageContaining(NUMERO_DOCUMENTO_BENEFICIARIO);

            verify(repository, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("Validaciones de Límites")
    class ValidacionesLimitesTests {

        @Test
        @DisplayName("crear_beneficiario_maximoExcedido_lanzaExcepcion - debe lanzar MaximoBeneficiariosExcedidoException")
        void crear_beneficiario_maximoExcedido_lanzaExcepcion() {
            // Arrange - socio ya tiene 5 beneficiarios activos
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
            when(socioQueryPort.getNumeroDocumentoById(SOCIO_ID)).thenReturn(NUMERO_DOCUMENTO_SOCIO);
            when(repository.existePorDocumento(eq(SOCIO_ID), any(TipoDocumento.class), anyString(), any())).thenReturn(false);
            when(repository.countActivosPorSocioId(SOCIO_ID)).thenReturn(5); // máximo alcanzado

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(SOCIO_ID, createRequest, httpRequest))
                    .isInstanceOf(MaximoBeneficiariosExcedidoException.class);

            verify(repository, never()).guardar(any());
        }

        @Test
        @DisplayName("crear_beneficiario_porcentajeSumExcedido_lanzaExcepcion - debe lanzar PorcentajeSumExcedidoException")
        void crear_beneficiario_porcentajeSumExcedido_lanzaExcepcion() {
            // Arrange - suma actual es 80%, al agregar 30% excede 100%
            when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
            when(socioQueryPort.getNumeroDocumentoById(SOCIO_ID)).thenReturn(NUMERO_DOCUMENTO_SOCIO);
            when(repository.existePorDocumento(eq(SOCIO_ID), any(TipoDocumento.class), anyString(), any())).thenReturn(false);
            when(repository.countActivosPorSocioId(SOCIO_ID)).thenReturn(1);
            when(repository.sumarPorcentajesPorSocioId(SOCIO_ID)).thenReturn(new BigDecimal("80.00"));

            CreateBeneficiarioRequestDTO requestConPorcentajeAlto = new CreateBeneficiarioRequestDTO(
                    "Juan Pérez",
                    NUMERO_DOCUMENTO_BENEFICIARIO,
                    TipoDocumento.CEDULA_IDENTIDAD,
                    Parentesco.HIJO,
                    new BigDecimal("30.00"), // 80 + 30 = 110% > 100%
                    "04121234567"
            );

            // Act & Assert
            assertThatThrownBy(() -> useCase.ejecutar(SOCIO_ID, requestConPorcentajeAlto, httpRequest))
                    .isInstanceOf(PorcentajeSumExcedidoException.class);

            verify(repository, never()).guardar(any());
        }
    }
}
