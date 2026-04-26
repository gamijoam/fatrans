package com.tufondo.parametros.application.usecase;

import com.tufondo.parametros.application.dto.ActualizarParametroRequest;
import com.tufondo.parametros.application.dto.ParametroResponse;
import com.tufondo.parametros.domain.model.ParametroSistema;
import com.tufondo.parametros.domain.repository.ParametroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParametroService - Tests")
class ParametroServiceTest {

    @Mock
    private ParametroRepository repository;

    private ParametroService service;

    private ParametroSistema parametroEditable;
    private ParametroSistema parametroNoEditable;

    @BeforeEach
    void setUp() {
        service = new ParametroService(repository);

        parametroEditable = ParametroSistema.desdeParametros(
                "TASA_INTERES_AHORRO",
                "0.05",
                ParametroSistema.TipoParametro.PERCENTAGE,
                "Tasa de interés anual",
                "TASA",
                true,
                null,
                null
        );

        parametroNoEditable = ParametroSistema.desdeParametros(
                "NOMBRE_EMPRESA",
                "Fondo TuFondo",
                ParametroSistema.TipoParametro.STRING,
                "Nombre de la empresa",
                "SISTEMA",
                false,
                null,
                null
        );
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTests {

        @Test
        @DisplayName("Lista todos los parámetros")
        void lista_todos_los_parametros() {
            when(repository.listarTodos()).thenReturn(List.of(parametroEditable, parametroNoEditable));

            List<ParametroResponse> result = service.listarTodos();

            assertThat(result).hasSize(2);
            verify(repository).listarTodos();
        }

        @Test
        @DisplayName("Lista parámetros vacíos")
        void lista_parametros_vacios() {
            when(repository.listarTodos()).thenReturn(List.of());

            List<ParametroResponse> result = service.listarTodos();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorKey")
    class BuscarPorKeyTests {

        @Test
        @DisplayName("Busca parámetro existente")
        void busca_parametro_existente() {
            when(repository.buscarPorKey("TASA_INTERES_AHORRO")).thenReturn(Optional.of(parametroEditable));

            ParametroResponse result = service.buscarPorKey("TASA_INTERES_AHORRO");

            assertThat(result.key()).isEqualTo("TASA_INTERES_AHORRO");
            assertThat(result.valor()).isEqualTo("0.05");
        }

        @Test
        @DisplayName("Lanza excepción para parámetro no existente")
        void lanza_excepcion_parametro_no_existente() {
            when(repository.buscarPorKey("NO_EXISTE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorKey("NO_EXISTE"))
                    .isInstanceOf(ParametroService.ParametroNoEncontradoException.class)
                    .hasMessageContaining("NO_EXISTE");
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("Actualiza valor de parámetro editable")
        void actualiza_parametro_editable() {
            UUID adminId = UUID.randomUUID();
            when(repository.buscarPorKey("TASA_INTERES_AHORRO")).thenReturn(Optional.of(parametroEditable));
            doNothing().when(repository).actualizar(any());

            ParametroResponse result = service.actualizar(
                    "TASA_INTERES_AHORRO",
                    new ActualizarParametroRequest("0.06"),
                    adminId
            );

            assertThat(result.valor()).isEqualTo("0.06");
            assertThat(result.actualizadoPor()).isEqualTo(adminId);
            verify(repository).actualizar(any());
        }

        @Test
        @DisplayName("Lanza excepción al actualizar parámetro no editable")
        void lanza_excepcion_parametro_no_editable() {
            when(repository.buscarPorKey("NOMBRE_EMPRESA")).thenReturn(Optional.of(parametroNoEditable));

            assertThatThrownBy(() -> service.actualizar(
                    "NOMBRE_EMPRESA",
                    new ActualizarParametroRequest("Nuevo Nombre"),
                    UUID.randomUUID()
            ))
                    .isInstanceOf(ParametroService.ParametroNoEditableException.class)
                    .hasMessageContaining("NOMBRE_EMPRESA");
        }

        @Test
        @DisplayName("Lanza excepción para valor numérico inválido")
        void lanza_excepcion_valor_numerico_invalido() {
            when(repository.buscarPorKey("TASA_INTERES_AHORRO")).thenReturn(Optional.of(parametroEditable));

            assertThatThrownBy(() -> service.actualizar(
                    "TASA_INTERES_AHORRO",
                    new ActualizarParametroRequest("no-es-numero"),
                    UUID.randomUUID()
            ))
                    .isInstanceOf(ParametroService.ValorInvalidoException.class);
        }

        @Test
        @DisplayName("Lanza excepción para porcentaje fuera de rango")
        void lanza_excepcion_porcentaje_fuera_rango() {
            when(repository.buscarPorKey("TASA_INTERES_AHORRO")).thenReturn(Optional.of(parametroEditable));

            assertThatThrownBy(() -> service.actualizar(
                    "TASA_INTERES_AHORRO",
                    new ActualizarParametroRequest("1.5"),
                    UUID.randomUUID()
            ))
                    .isInstanceOf(ParametroService.ValorInvalidoException.class);
        }
    }
}