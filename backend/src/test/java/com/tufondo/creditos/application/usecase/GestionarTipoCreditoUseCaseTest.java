package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.TipoCreditoRequest;
import com.tufondo.creditos.application.dto.TipoCreditoResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import com.tufondo.creditos.infrastructure.security.XssSanitizer;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionarTipoCreditoUseCase - Tests")
class GestionarTipoCreditoUseCaseTest {

    @Mock
    private TipoCreditoRepository repository;

    @Mock
    private CreditosDTOMapper mapper;

    @Mock
    private XssSanitizer xssSanitizer;

    @Mock
    private SecurityAuditService auditService;

    private GestionarTipoCreditoUseCase useCase;

    private TipoCredito tipoCreditoMock;
    private TipoCreditoRequest requestMock;
    private TipoCreditoResponse responseMock;
    private UUID adminId;
    private String ipAddress;

    @BeforeEach
    void setUp() {
        useCase = new GestionarTipoCreditoUseCase(repository, mapper, xssSanitizer, auditService);

        adminId = UUID.randomUUID();
        ipAddress = "192.168.1.1";

        tipoCreditoMock = TipoCredito.builder()
                .id(1L)
                .codigo("MICRO_CREDITO")
                .nombre("Micro crédito")
                .descripcion("Crédito pequeño para emprendedores")
                .tasaInteresAnual(new BigDecimal("0.24"))
                .plazoMinimoMeses(3)
                .plazoMaximoMeses(12)
                .montoMinimo(new BigDecimal("100"))
                .montoMaximo(new BigDecimal("5000"))
                .porcentajeRequerimientoColateral(new BigDecimal("0.10"))
                .comisionApertura(new BigDecimal("0.01"))
                .penalidadMoraTasa(new BigDecimal("0.02"))
                .diasGracia(5)
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        requestMock = TipoCreditoRequest.builder()
                .codigo("MICRO_CREDITO")
                .nombre("Micro crédito")
                .descripcion("Crédito pequeño para emprendedores")
                .tasaInteresAnual(new BigDecimal("0.24"))
                .plazoMinimoMeses(3)
                .plazoMaximoMeses(12)
                .montoMinimo(new BigDecimal("100"))
                .montoMaximo(new BigDecimal("5000"))
                .porcentajeRequerimientoColateral(new BigDecimal("0.10"))
                .comisionApertura(new BigDecimal("0.01"))
                .penalidadMoraTasa(new BigDecimal("0.02"))
                .diasGracia(5)
                .activo(true)
                .build();

        responseMock = TipoCreditoResponse.builder()
                .id(1L)
                .codigo("MICRO_CREDITO")
                .nombre("Micro crédito")
                .descripcion("Crédito pequeño para emprendedores")
                .tasaInteresAnual(new BigDecimal("0.24"))
                .plazoMinimoMeses(3)
                .plazoMaximoMeses(12)
                .montoMinimo(new BigDecimal("100"))
                .montoMaximo(new BigDecimal("5000"))
                .porcentajeRequerimientoColateral(new BigDecimal("0.10"))
                .comisionApertura(new BigDecimal("0.01"))
                .penalidadMoraTasa(new BigDecimal("0.02"))
                .diasGracia(5)
                .activo(true)
                .build();
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTests {

        @Test
        @DisplayName("Lista todos los tipos de crédito")
        void lista_todos_los_tipos() {
            when(repository.listarTodos()).thenReturn(List.of(tipoCreditoMock));
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);

            List<TipoCreditoResponse> result = useCase.listarTodos();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCodigo()).isEqualTo("MICRO_CREDITO");
            verify(repository).listarTodos();
            verify(mapper).toResponse(tipoCreditoMock);
        }

        @Test
        @DisplayName("Lista tipos vacíos")
        void lista_tipos_vacios() {
            when(repository.listarTodos()).thenReturn(List.of());

            List<TipoCreditoResponse> result = useCase.listarTodos();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Obtiene tipo de crédito por ID")
        void obtiene_por_id() {
            when(repository.buscarPorId(1L)).thenReturn(Optional.of(tipoCreditoMock));
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);

            TipoCreditoResponse result = useCase.obtenerPorId(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCodigo()).isEqualTo("MICRO_CREDITO");
        }

        @Test
        @DisplayName("Lanza excepción para ID no existente")
        void lanza_excepcion_id_no_existente() {
            when(repository.buscarPorId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.obtenerPorId(999L))
                    .isInstanceOf(GestionarTipoCreditoUseCase.TipoCreditoNoEncontradoException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("crear")
    class CrearTests {

        @Test
        @DisplayName("Crea tipo de crédito exitosamente")
        void crea_tipo_exitosamente() {
            when(repository.existePorCodigo("MICRO_CREDITO")).thenReturn(false);
            when(repository.guardar(any(TipoCredito.class))).thenReturn(tipoCreditoMock);
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);
            when(xssSanitizer.sanitize(requestMock.getNombre())).thenReturn(requestMock.getNombre());
            when(xssSanitizer.sanitizeDescription(requestMock.getDescripcion())).thenReturn(requestMock.getDescripcion());

            TipoCreditoResponse result = useCase.crear(requestMock, adminId, ipAddress);

            assertThat(result.getCodigo()).isEqualTo("MICRO_CREDITO");
            verify(repository).guardar(any(TipoCredito.class));
        }

        @Test
        @DisplayName("Lanza excepción por código duplicado")
        void lanza_excepcion_codigo_duplicado() {
            when(repository.existePorCodigo("MICRO_CREDITO")).thenReturn(true);

            assertThatThrownBy(() -> useCase.crear(requestMock, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCreditoUseCase.CodigoTipoCreditoYaExisteException.class)
                    .hasMessageContaining("MICRO_CREDITO");
        }

        @Test
        @DisplayName("Lanza excepción por plazo inválido")
        void lanza_excepcion_plazo_invalido() {
            TipoCreditoRequest invalidRequest = TipoCreditoRequest.builder()
                    .codigo("OTRO")
                    .nombre("Otro")
                    .tasaInteresAnual(new BigDecimal("0.24"))
                    .plazoMinimoMeses(12)
                    .plazoMaximoMeses(6)
                    .montoMinimo(new BigDecimal("100"))
                    .montoMaximo(new BigDecimal("5000"))
                    .build();

            assertThatThrownBy(() -> useCase.crear(invalidRequest, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCreditoUseCase.PlazoInvalidoException.class);
        }

        @Test
        @DisplayName("Lanza excepción por monto inválido")
        void lanza_excepcion_monto_invalido() {
            TipoCreditoRequest invalidRequest = TipoCreditoRequest.builder()
                    .codigo("OTRO")
                    .nombre("Otro")
                    .tasaInteresAnual(new BigDecimal("0.24"))
                    .plazoMinimoMeses(3)
                    .plazoMaximoMeses(12)
                    .montoMinimo(new BigDecimal("5000"))
                    .montoMaximo(new BigDecimal("100"))
                    .build();

            assertThatThrownBy(() -> useCase.crear(invalidRequest, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCreditoUseCase.MontoInvalidoException.class);
        }

        @Test
        @DisplayName("Normaliza código a uppercase")
        void normaliza_codigo_a_uppercase() {
            TipoCreditoRequest lowerRequest = TipoCreditoRequest.builder()
                    .codigo("micro_credito")
                    .nombre("Micro crédito")
                    .tasaInteresAnual(new BigDecimal("0.24"))
                    .plazoMinimoMeses(3)
                    .plazoMaximoMeses(12)
                    .montoMinimo(new BigDecimal("100"))
                    .montoMaximo(new BigDecimal("5000"))
                    .build();

            when(repository.existePorCodigo("MICRO_CREDITO")).thenReturn(false);
            when(repository.guardar(any(TipoCredito.class))).thenReturn(tipoCreditoMock);
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);
            when(xssSanitizer.sanitize(any())).thenReturn("Micro crédito");

            useCase.crear(lowerRequest, adminId, ipAddress);

            verify(repository).existePorCodigo(eq("MICRO_CREDITO"));
        }

        @Test
        @DisplayName("Sanitiza nombre y descripción")
        void sanitiza_nombre_y_descripcion() {
            when(repository.existePorCodigo("MICRO_CREDITO")).thenReturn(false);
            when(repository.guardar(any(TipoCredito.class))).thenReturn(tipoCreditoMock);
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);
            when(xssSanitizer.sanitize(requestMock.getNombre())).thenReturn("Micro credito sanitizado");
            when(xssSanitizer.sanitizeDescription(requestMock.getDescripcion())).thenReturn("Desc sanitizada");

            useCase.crear(requestMock, adminId, ipAddress);

            verify(xssSanitizer).sanitize(requestMock.getNombre());
            verify(xssSanitizer).sanitizeDescription(requestMock.getDescripcion());
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("Actualiza tipo de crédito exitosamente")
        void actualiza_tipo_exitosamente() {
            when(repository.buscarPorId(1L)).thenReturn(Optional.of(tipoCreditoMock));
            when(repository.guardar(any(TipoCredito.class))).thenReturn(tipoCreditoMock);
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);
            when(xssSanitizer.sanitize(requestMock.getNombre())).thenReturn(requestMock.getNombre());
            when(xssSanitizer.sanitizeDescription(requestMock.getDescripcion())).thenReturn(requestMock.getDescripcion());

            TipoCreditoResponse result = useCase.actualizar(1L, requestMock, adminId, ipAddress);

            assertThat(result).isNotNull();
            verify(repository).guardar(any(TipoCredito.class));
        }

        @Test
        @DisplayName("Lanza excepción al actualizar no existente")
        void lanza_excepcion_no_existente() {
            when(repository.buscarPorId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.actualizar(999L, requestMock, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCreditoUseCase.TipoCreditoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("activar/desactivar")
    class ActivarDesactivarTests {

        @Test
        @DisplayName("Activa tipo de crédito")
        void activa_tipo() {
            tipoCreditoMock.setActivo(false);
            when(repository.buscarPorId(1L)).thenReturn(Optional.of(tipoCreditoMock));
            when(repository.guardar(any(TipoCredito.class))).thenReturn(tipoCreditoMock);
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);

            TipoCreditoResponse result = useCase.activar(1L, adminId, ipAddress);

            assertThat(tipoCreditoMock.getActivo()).isTrue();
            verify(repository).guardar(tipoCreditoMock);
        }

        @Test
        @DisplayName("Desactiva tipo de crédito")
        void desactiva_tipo() {
            tipoCreditoMock.setActivo(true);
            when(repository.buscarPorId(1L)).thenReturn(Optional.of(tipoCreditoMock));
            when(repository.guardar(any(TipoCredito.class))).thenReturn(tipoCreditoMock);
            when(mapper.toResponse(tipoCreditoMock)).thenReturn(responseMock);

            TipoCreditoResponse result = useCase.desactivar(1L, adminId, ipAddress);

            assertThat(tipoCreditoMock.getActivo()).isFalse();
            verify(repository).guardar(tipoCreditoMock);
        }

        @Test
        @DisplayName("Lanza excepción al activar no existente")
        void lanza_excepcion_activar_no_existente() {
            when(repository.buscarPorId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.activar(999L, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCreditoUseCase.TipoCreditoNoEncontradoException.class);
        }
    }
}