package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.exception.MovimientoNoEncontradoException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.CanalOrigen;
import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.domain.exception.GeneracionPDFException;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests del use case que genera comprobantes PDF on-demand (issue #220 PR-B).
 *
 * <p>Cubre: validación IDOR (socio ajeno → AccesoCuentaAjenaException),
 * cuenta inexistente, movimiento inexistente, movimiento que no pertenece
 * a la cuenta (defensa contra enumeración), happy path y construcción
 * correcta del data map para el generador, admin con paso libre, y
 * propagación de GeneracionPDFException.</p>
 */
@ExtendWith(MockitoExtension.class)
class GenerarComprobanteMovimientoUseCaseTest {

    @Mock private CuentaAhorroRepository cuentaRepository;
    @Mock private MovimientoRepository movimientoRepository;
    @Mock private SocioQueryPort socioQueryPort;
    @Mock private PdfGeneratorPort pdfGeneratorPort;

    @InjectMocks
    private GenerarComprobanteMovimientoUseCase useCase;

    private UUID socioOwner;
    private UUID socioAjeno;
    private UUID cuentaId;
    private CuentaAhorro cuenta;
    private Movimiento movimiento;
    private final String numeroCuenta = "AHO-2026-000001";
    private final String numeroOperacion = "MOV-2026-000042";

    @BeforeEach
    void setUp() {
        socioOwner = UUID.randomUUID();
        socioAjeno = UUID.randomUUID();
        cuentaId = UUID.randomUUID();

        cuenta = CuentaAhorro.builder()
                .id(cuentaId)
                .numeroCuenta(numeroCuenta)
                .socioId(socioOwner)
                .tipoCuenta(TipoCuenta.AHORRO)
                .moneda(Moneda.VES)
                .build();

        movimiento = Movimiento.builder()
                .id(UUID.randomUUID())
                .numeroOperacion(numeroOperacion)
                .cuentaAhorroId(cuentaId)
                .socioId(socioOwner)
                .tipo(TipoMovimiento.DEPOSITO)
                .monto(new BigDecimal("1500.50"))
                .saldoAnterior(new BigDecimal("8000.00"))
                .saldoPosterior(new BigDecimal("9500.50"))
                .descripcion("Depósito caja")
                .referencia("REF-XYZ-1")
                .canalOrigen(CanalOrigen.SUCURSAL)
                .estado(EstadoMovimiento.PROCESADO)
                .fechaMovimiento(LocalDateTime.of(2026, 5, 17, 14, 30))
                .fechaValor(LocalDate.of(2026, 5, 17))
                .build();
    }

    @Test
    @DisplayName("IDOR: socio ajeno intenta descargar comprobante → AccesoCuentaAjenaException, NO genera PDF")
    void socio_ajeno_no_puede_descargar() {
        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() ->
                useCase.ejecutar(numeroCuenta, numeroOperacion, socioAjeno, false))
                .isInstanceOf(AccesoCuentaAjenaException.class);

        // CRÍTICO: no se debe llamar al generador ni al repo de movimientos
        verify(movimientoRepository, never()).buscarPorNumeroOperacion(any());
        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
    }

    @Test
    @DisplayName("Cuenta inexistente → CuentaAhorroNoEncontradaException")
    void cuenta_inexistente_lanza() {
        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.ejecutar(numeroCuenta, numeroOperacion, socioOwner, false))
                .isInstanceOf(CuentaAhorroNoEncontradaException.class);

        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
    }

    @Test
    @DisplayName("Movimiento inexistente → MovimientoNoEncontradoException")
    void movimiento_inexistente_lanza() {
        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.buscarPorNumeroOperacion(numeroOperacion))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.ejecutar(numeroCuenta, numeroOperacion, socioOwner, false))
                .isInstanceOf(MovimientoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Defensa contra enumeración: movimiento existe pero NO pertenece a la cuenta → MovimientoNoEncontradoException")
    void movimiento_de_otra_cuenta_lanza_no_encontrado() {
        // Movimiento de OTRA cuenta — no debe filtrar info al usuario
        Movimiento movDeOtraCuenta = Movimiento.builder()
                .id(UUID.randomUUID())
                .numeroOperacion(numeroOperacion)
                .cuentaAhorroId(UUID.randomUUID())  // ← cuenta distinta
                .socioId(socioOwner)
                .tipo(TipoMovimiento.DEPOSITO)
                .monto(BigDecimal.TEN)
                .saldoAnterior(BigDecimal.ZERO)
                .saldoPosterior(BigDecimal.TEN)
                .estado(EstadoMovimiento.PROCESADO)
                .fechaMovimiento(LocalDateTime.now())
                .build();

        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.buscarPorNumeroOperacion(numeroOperacion))
                .thenReturn(Optional.of(movDeOtraCuenta));

        assertThatThrownBy(() ->
                useCase.ejecutar(numeroCuenta, numeroOperacion, socioOwner, false))
                .isInstanceOf(MovimientoNoEncontradoException.class);

        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
    }

    @Test
    @DisplayName("Happy path: socio dueño descarga su propio comprobante — devuelve bytes del PDF")
    void happy_path_socio_propietario() {
        byte[] pdfFalso = "%PDF-FALSO-1.4".getBytes();
        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.buscarPorNumeroOperacion(numeroOperacion))
                .thenReturn(Optional.of(movimiento));
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioOwner)).thenReturn(datosSocioMock());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.COMPROBANTE_MOVIMIENTO), any()))
                .thenReturn(pdfFalso);

        byte[] resultado = useCase.ejecutar(numeroCuenta, numeroOperacion, socioOwner, false);

        assertThat(resultado).isSameAs(pdfFalso);
    }

    @Test
    @DisplayName("Admin puede descargar comprobante de cualquier socio (paso libre por rol)")
    void admin_puede_descargar_de_otro_socio() {
        byte[] pdfFalso = "%PDF-ADMIN".getBytes();
        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.buscarPorNumeroOperacion(numeroOperacion))
                .thenReturn(Optional.of(movimiento));
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioOwner)).thenReturn(datosSocioMock());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.COMPROBANTE_MOVIMIENTO), any()))
                .thenReturn(pdfFalso);

        // socioAjeno (admin) descarga comprobante de cuenta de socioOwner — debe pasar
        byte[] resultado = useCase.ejecutar(numeroCuenta, numeroOperacion, socioAjeno, true);

        assertThat(resultado).isSameAs(pdfFalso);
    }

    @Test
    @DisplayName("El data map enviado al generador contiene socio, cuenta, movimiento y fechaEmision")
    void data_map_contiene_secciones_esperadas() {
        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.buscarPorNumeroOperacion(numeroOperacion))
                .thenReturn(Optional.of(movimiento));
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioOwner)).thenReturn(datosSocioMock());
        when(pdfGeneratorPort.generarPdf(any(), any())).thenReturn(new byte[]{0x01});

        useCase.ejecutar(numeroCuenta, numeroOperacion, socioOwner, false);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(pdfGeneratorPort).generarPdf(eq(TipoDocumento.COMPROBANTE_MOVIMIENTO), captor.capture());
        Map<String, Object> datos = captor.getValue();

        assertThat(datos).containsKeys("socio", "cuenta", "movimiento", "fechaEmision");

        @SuppressWarnings("unchecked")
        Map<String, Object> cuentaMap = (Map<String, Object>) datos.get("cuenta");
        assertThat(cuentaMap.get("numeroCuenta")).isEqualTo(numeroCuenta);
        assertThat(cuentaMap.get("tipoCuenta")).isEqualTo("AHORRO");
        assertThat(cuentaMap.get("moneda")).isEqualTo("VES");

        @SuppressWarnings("unchecked")
        Map<String, Object> movMap = (Map<String, Object>) datos.get("movimiento");
        assertThat(movMap.get("numeroOperacion")).isEqualTo(numeroOperacion);
        assertThat(movMap.get("tipo")).isEqualTo("DEPOSITO");
        assertThat(movMap.get("canalOrigen")).isEqualTo("SUCURSAL");
        assertThat(movMap.get("estado")).isEqualTo("PROCESADO");
        assertThat(movMap.get("monto")).isEqualTo(new BigDecimal("1500.50"));
        assertThat(movMap.get("saldoAnterior")).isEqualTo(new BigDecimal("8000.00"));
        assertThat(movMap.get("saldoPosterior")).isEqualTo(new BigDecimal("9500.50"));
    }

    @Test
    @DisplayName("Si el generador lanza GeneracionPDFException, se propaga tal cual")
    void propaga_excepcion_del_generador() {
        when(cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.buscarPorNumeroOperacion(numeroOperacion))
                .thenReturn(Optional.of(movimiento));
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioOwner)).thenReturn(datosSocioMock());
        when(pdfGeneratorPort.generarPdf(any(), any()))
                .thenThrow(new GeneracionPDFException("fallo de openpdf"));

        assertThatThrownBy(() ->
                useCase.ejecutar(numeroCuenta, numeroOperacion, socioOwner, false))
                .isInstanceOf(GeneracionPDFException.class)
                .hasMessageContaining("fallo de openpdf");
    }

    private Map<String, Object> datosSocioMock() {
        Map<String, Object> m = new HashMap<>();
        m.put("nombreCompleto", "Juan Pérez");
        m.put("cedula", "V-12345678");
        return m;
    }
}
