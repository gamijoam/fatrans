package com.tufondo.tipocambio.application.usecase;

import com.tufondo.tipocambio.application.dto.TipoCambioRequest;
import com.tufondo.tipocambio.application.dto.TipoCambioResponse;
import com.tufondo.tipocambio.domain.model.TipoCambio;
import com.tufondo.tipocambio.domain.repository.TipoCambioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionarTipoCambioUseCase - Tests Completos")
class GestionarTipoCambioUseCaseTest {

    @Mock
    private TipoCambioRepository tipoCambioRepository;

    private GestionarTipoCambioUseCase useCase;

    private UUID adminId;
    private UUID tipoCambioId;
    private String ipAddress;
    private TipoCambio tipoCambioExistente;

    @BeforeEach
    void setUp() {
        useCase = new GestionarTipoCambioUseCase(tipoCambioRepository);
        adminId = UUID.randomUUID();
        tipoCambioId = UUID.randomUUID();
        ipAddress = "192.168.1.1";

        tipoCambioExistente = TipoCambio.builder()
                .id(tipoCambioId)
                .fecha(LocalDate.of(2026, 4, 26))
                .tasaCompra(new BigDecimal("45.000000"))
                .tasaVenta(new BigDecimal("45.500000"))
                .fuente("BCV")
                .creadoPor(adminId)
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("crear")
    class CrearTests {

        @Test
        @DisplayName("Crea tipo de cambio exitosamente")
        void crea_tipo_cambio_exitosamente() {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 27))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .fuente("BCV")
                    .build();

            when(tipoCambioRepository.existePorFecha(request.getFecha())).thenReturn(false);
            doNothing().when(tipoCambioRepository).guardar(any(TipoCambio.class));

            TipoCambioResponse result = useCase.crear(request, adminId, ipAddress);

            assertThat(result.getFecha()).isEqualTo(request.getFecha());
            assertThat(result.getTasaCompra()).isEqualByComparingTo(request.getTasaCompra());
            assertThat(result.getTasaVenta()).isEqualByComparingTo(request.getTasaVenta());
            verify(tipoCambioRepository).guardar(any(TipoCambio.class));
        }

        @Test
        @DisplayName("Lanza excepción por fecha duplicada")
        void lanza_excepcion_fecha_duplicada() {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 26))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .build();

            when(tipoCambioRepository.existePorFecha(request.getFecha())).thenReturn(true);

            assertThatThrownBy(() -> useCase.crear(request, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCambioUseCase.TipoCambioYaExisteException.class)
                    .hasMessageContaining("2026-04-26");
        }

        @Test
        @DisplayName("Lanza excepción cuando tasa compra mayor a venta")
        void lanza_excepcion_tasa_compra_mayor() {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 27))
                    .tasaCompra(new BigDecimal("46.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .build();

            assertThatThrownBy(() -> useCase.crear(request, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCambioUseCase.TasaInvalidaException.class)
                    .hasMessageContaining("compra no puede ser mayor");
        }

        @Test
        @DisplayName("Lanza excepción cuando tasas son cero o negativas")
        void lanza_excepcion_tasas_cero() {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 27))
                    .tasaCompra(new BigDecimal("0"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .build();

            assertThatThrownBy(() -> useCase.crear(request, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCambioUseCase.TasaInvalidaException.class);
        }

        @Test
        @DisplayName("Crea con fuente null")
        void crea_sin_fuente() {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 27))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .fuente(null)
                    .build();

            when(tipoCambioRepository.existePorFecha(any())).thenReturn(false);
            doNothing().when(tipoCambioRepository).guardar(any(TipoCambio.class));

            TipoCambioResponse result = useCase.crear(request, adminId, ipAddress);

            assertThat(result.getFuente()).isNull();
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("Actualiza tipo de cambio exitosamente")
        void actualiza_tipo_cambio_exitosamente() {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 27))
                    .tasaCompra(new BigDecimal("46.000000"))
                    .tasaVenta(new BigDecimal("46.500000"))
                    .fuente("BCV Actualizado")
                    .build();

            when(tipoCambioRepository.buscarPorId(tipoCambioId)).thenReturn(Optional.of(tipoCambioExistente));
            when(tipoCambioRepository.existePorFecha(LocalDate.of(2026, 4, 27))).thenReturn(false);
            doNothing().when(tipoCambioRepository).actualizar(any(TipoCambio.class));

            TipoCambioResponse result = useCase.actualizar(tipoCambioId, request, adminId, ipAddress);

            assertThat(result.getTasaCompra()).isEqualByComparingTo(new BigDecimal("46.000000"));
            assertThat(result.getTasaVenta()).isEqualByComparingTo(new BigDecimal("46.500000"));
            verify(tipoCambioRepository).actualizar(any(TipoCambio.class));
        }

        @Test
        @DisplayName("Lanza excepción al actualizar no existente")
        void lanza_excepcion_no_existente() {
            UUID nonExistentId = UUID.randomUUID();
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 26))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .build();

            when(tipoCambioRepository.buscarPorId(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.actualizar(nonExistentId, request, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCambioUseCase.TipoCambioNoEncontradoException.class);
        }

        @Test
        @DisplayName("Lanza excepción por cambio de fecha duplicada")
        void lanza_excepcion_cambio_fecha_duplicada() {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 25))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .build();

            when(tipoCambioRepository.buscarPorId(tipoCambioId)).thenReturn(Optional.of(tipoCambioExistente));
            when(tipoCambioRepository.existePorFecha(LocalDate.of(2026, 4, 25))).thenReturn(true);

            assertThatThrownBy(() -> useCase.actualizar(tipoCambioId, request, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCambioUseCase.TipoCambioYaExisteException.class);
        }

        @Test
        @DisplayName("Mantiene creador original al actualizar")
        void mantiene_creador_original() {
            UUID creadorOriginal = UUID.randomUUID();
            tipoCambioExistente = TipoCambio.builder()
                    .id(tipoCambioId)
                    .fecha(LocalDate.of(2026, 4, 26))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .fuente("BCV")
                    .creadoPor(creadorOriginal)
                    .createdAt(Instant.now().minusSeconds(3600))
                    .build();

            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 26))
                    .tasaCompra(new BigDecimal("46.000000"))
                    .tasaVenta(new BigDecimal("46.500000"))
                    .fuente("BCV")
                    .build();

            when(tipoCambioRepository.buscarPorId(tipoCambioId)).thenReturn(Optional.of(tipoCambioExistente));
            doNothing().when(tipoCambioRepository).actualizar(any(TipoCambio.class));

            useCase.actualizar(tipoCambioId, request, adminId, ipAddress);

            verify(tipoCambioRepository).actualizar(argThat(tc ->
                tc.getCreadoPor().equals(creadorOriginal)
            ));
        }
    }

    @Nested
    @DisplayName("eliminar")
    class EliminarTests {

        @Test
        @DisplayName("Elimina tipo de cambio exitosamente")
        void elimina_tipo_cambio_exitosamente() {
            when(tipoCambioRepository.buscarPorId(tipoCambioId)).thenReturn(Optional.of(tipoCambioExistente));
            doNothing().when(tipoCambioRepository).eliminar(tipoCambioId);

            useCase.eliminar(tipoCambioId, adminId, ipAddress);

            verify(tipoCambioRepository).eliminar(tipoCambioId);
        }

        @Test
        @DisplayName("Lanza excepción al eliminar no existente")
        void lanza_excepcion_eliminar_no_existente() {
            UUID nonExistentId = UUID.randomUUID();
            when(tipoCambioRepository.buscarPorId(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.eliminar(nonExistentId, adminId, ipAddress))
                    .isInstanceOf(GestionarTipoCambioUseCase.TipoCambioNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Obtiene tipo de cambio por ID")
        void obtiene_por_id() {
            when(tipoCambioRepository.buscarPorId(tipoCambioId)).thenReturn(Optional.of(tipoCambioExistente));

            Optional<TipoCambioResponse> result = useCase.obtenerPorId(tipoCambioId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(tipoCambioId);
            assertThat(result.get().getFecha()).isEqualTo(LocalDate.of(2026, 4, 26));
        }

        @Test
        @DisplayName("Retorna vacío para ID no existente")
        void retorna_vacio_no_existente() {
            UUID nonExistentId = UUID.randomUUID();
            when(tipoCambioRepository.buscarPorId(nonExistentId)).thenReturn(Optional.empty());

            Optional<TipoCambioResponse> result = useCase.obtenerPorId(nonExistentId);

            assertThat(result).isEmpty();
        }
    }
}