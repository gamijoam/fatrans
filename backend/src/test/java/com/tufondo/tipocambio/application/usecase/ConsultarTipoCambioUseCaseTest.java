package com.tufondo.tipocambio.application.usecase;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultarTipoCambioUseCase - Tests Completos")
class ConsultarTipoCambioUseCaseTest {

    @Mock
    private TipoCambioRepository tipoCambioRepository;

    private ConsultarTipoCambioUseCase useCase;

    private TipoCambio tasaHoy;
    private TipoCambio tasaAyer;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarTipoCambioUseCase(tipoCambioRepository);

        tasaHoy = TipoCambio.builder()
                .id(UUID.randomUUID())
                .fecha(LocalDate.of(2026, 4, 26))
                .tasaCompra(new BigDecimal("45.000000"))
                .tasaVenta(new BigDecimal("45.500000"))
                .fuente("BCV")
                .creadoPor(UUID.randomUUID())
                .createdAt(Instant.now())
                .build();

        tasaAyer = TipoCambio.builder()
                .id(UUID.randomUUID())
                .fecha(LocalDate.of(2026, 4, 25))
                .tasaCompra(new BigDecimal("44.000000"))
                .tasaVenta(new BigDecimal("44.500000"))
                .fuente("BCV")
                .creadoPor(UUID.randomUUID())
                .createdAt(Instant.now().minusSeconds(86400))
                .build();
    }

    @Nested
    @DisplayName("obtenerTasaActual")
    class ObtenerTasaActualTests {

        @Test
        @DisplayName("Retorna tasa actual con variación calculada")
        void retorna_tasa_actual_con_variacion() {
            when(tipoCambioRepository.buscarTasaActual()).thenReturn(Optional.of(tasaHoy));
            when(tipoCambioRepository.listarHistorial(2)).thenReturn(Arrays.asList(tasaHoy, tasaAyer));

            Optional<TipoCambioResponse> result = useCase.obtenerTasaActual();

            assertThat(result).isPresent();
            assertThat(result.get().getFecha()).isEqualTo(LocalDate.of(2026, 4, 26));
            assertThat(result.get().getVariacionPorcentual()).isNotNull();
        }

        @Test
        @DisplayName("Retorna vacío cuando no hay tasas registradas")
        void retorna_vacio_sin_tasas() {
            when(tipoCambioRepository.buscarTasaActual()).thenReturn(Optional.empty());

            Optional<TipoCambioResponse> result = useCase.obtenerTasaActual();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Calcula variación positiva correctamente")
        void calcula_variacion_positiva() {
            when(tipoCambioRepository.buscarTasaActual()).thenReturn(Optional.of(tasaHoy));
            when(tipoCambioRepository.listarHistorial(2)).thenReturn(Arrays.asList(tasaHoy, tasaAyer));

            Optional<TipoCambioResponse> result = useCase.obtenerTasaActual();

            assertThat(result.get().getVariacionPorcentual()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("obtenerPorFecha")
    class ObtenerPorFechaTests {

        @Test
        @DisplayName("Obtiene tasa por fecha específica")
        void obtiene_por_fecha() {
            LocalDate fecha = LocalDate.of(2026, 4, 25);
            when(tipoCambioRepository.buscarPorFecha(fecha)).thenReturn(Optional.of(tasaAyer));
            when(tipoCambioRepository.listarHistorial(2)).thenReturn(Arrays.asList(tasaHoy, tasaAyer));

            Optional<TipoCambioResponse> result = useCase.obtenerPorFecha(fecha);

            assertThat(result).isPresent();
            assertThat(result.get().getFecha()).isEqualTo(fecha);
        }

        @Test
        @DisplayName("Retorna vacío para fecha sin tasa")
        void retorna_vacio_fecha_sin_tasa() {
            LocalDate fecha = LocalDate.of(2020, 1, 1);
            when(tipoCambioRepository.buscarPorFecha(fecha)).thenReturn(Optional.empty());
            when(tipoCambioRepository.listarHistorial(2)).thenReturn(Arrays.asList(tasaHoy, tasaAyer));

            Optional<TipoCambioResponse> result = useCase.obtenerPorFecha(fecha);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarHistorial")
    class ListarHistorialTests {

        @Test
        @DisplayName("Lista historial con límite por defecto de 30")
        void lista_historial_default() {
            List<TipoCambio> historial = Arrays.asList(tasaHoy, tasaAyer);
            when(tipoCambioRepository.listarHistorial(30)).thenReturn(historial);

            List<TipoCambioResponse> result = useCase.listarHistorial(0);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Lista historial con límite personalizado")
        void lista_historial_limite_personalizado() {
            when(tipoCambioRepository.listarHistorial(10)).thenReturn(Arrays.asList(tasaHoy));

            List<TipoCambioResponse> result = useCase.listarHistorial(10);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Limita a máximo 100")
        void limita_maximo_100() {
            when(tipoCambioRepository.listarHistorial(30)).thenReturn(Arrays.asList(tasaHoy));

            useCase.listarHistorial(500);

            verify(tipoCambioRepository).listarHistorial(30);
        }

        @Test
        @DisplayName("Retorna vacío cuando no hay historial")
        void retorna_vacio_sin_historial() {
            when(tipoCambioRepository.listarHistorial(anyInt())).thenReturn(List.of());

            List<TipoCambioResponse> result = useCase.listarHistorial(30);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Incluye todos los campos en respuesta")
        void incluye_todos_los_campos() {
            when(tipoCambioRepository.listarHistorial(1)).thenReturn(Arrays.asList(tasaHoy));

            List<TipoCambioResponse> result = useCase.listarHistorial(1);

            assertThat(result.get(0).getId()).isNotNull();
            assertThat(result.get(0).getFecha()).isNotNull();
            assertThat(result.get(0).getTasaCompra()).isNotNull();
            assertThat(result.get(0).getTasaVenta()).isNotNull();
            assertThat(result.get(0).getFuente()).isNotNull();
        }
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTests {

        @Test
        @DisplayName("Lista todas las tasas")
        void lista_todas() {
            List<TipoCambio> todas = Arrays.asList(tasaHoy, tasaAyer);
            when(tipoCambioRepository.listarTodos()).thenReturn(todas);

            List<TipoCambioResponse> result = useCase.listarTodos();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Retorna vacío cuando no hay tasas")
        void retorna_vacio_sin_tasas() {
            when(tipoCambioRepository.listarTodos()).thenReturn(List.of());

            List<TipoCambioResponse> result = useCase.listarTodos();

            assertThat(result).isEmpty();
        }
    }
}