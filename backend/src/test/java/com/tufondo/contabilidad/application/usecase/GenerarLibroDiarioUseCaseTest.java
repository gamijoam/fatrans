package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
import com.tufondo.contabilidad.application.dto.LibroDiarioFilter;
import com.tufondo.contabilidad.application.dto.LibroDiarioResponse;
import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import com.tufondo.contabilidad.domain.repository.AsientoContableRepository;
import com.tufondo.contabilidad.domain.repository.CuentaContableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests del {@link GenerarLibroDiarioUseCase} (sub-issue #269).
 *
 * <p>Foco: filtros funcionan, totales correctos, asientos anulados se manejan
 * según parámetro, plan de cuentas se resuelve para mostrar nombres, formato
 * de número correcto.</p>
 */
@ExtendWith(MockitoExtension.class)
class GenerarLibroDiarioUseCaseTest {

    @Mock private AsientoContableRepository asientoRepo;
    @Mock private CuentaContableRepository cuentaRepo;

    @InjectMocks private GenerarLibroDiarioUseCase useCase;

    private CuentaContable caja;
    private CuentaContable depositos;
    private AsientoContable asientoRegistrado;
    private AsientoContable asientoAnulado;
    private LocalDate desde = LocalDate.of(2026, 5, 1);
    private LocalDate hasta = LocalDate.of(2026, 5, 31);

    @BeforeEach
    void setUp() {
        // Inyecto EntidadProperties manualmente porque @InjectMocks no lo crea
        // (no es mock, es un POJO de propiedades). Reemplazo el constructor.
        EntidadProperties props = new EntidadProperties();
        props.setRazonSocial("Fatrans Test");
        props.setRif("J-TEST-0");
        useCase = new GenerarLibroDiarioUseCase(asientoRepo, cuentaRepo, props);

        caja = CuentaContable.crear(
                "1.1.03", "Bancos Cta Corriente Bs",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                UUID.randomUUID(), true, null);
        depositos = CuentaContable.crear(
                "2.1.01", "Cuentas de Ahorro Bs",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA,
                UUID.randomUUID(), true, null);

        // Asiento REGISTRADO con número 1 → "2026-000001"
        asientoRegistrado = AsientoContable.reconstruir(
                UUID.randomUUID(), 1L,
                LocalDate.of(2026, 5, 10), "Depósito test",
                OrigenAsiento.AHORRO_DEPOSITO, "MOV-001",
                EstadoAsiento.REGISTRADO, null, null, null,
                List.of(
                        PartidaAsiento.alDebe(caja.getId(), new BigDecimal("100.00"), 1, "DEBE"),
                        PartidaAsiento.alHaber(depositos.getId(), new BigDecimal("100.00"), 2, "HABER")
                ),
                null, null, 0L);

        // Asiento ANULADO con número 2 → "2026-000002"
        asientoAnulado = AsientoContable.reconstruir(
                UUID.randomUUID(), 2L,
                LocalDate.of(2026, 5, 15), "Depósito que se anuló",
                OrigenAsiento.AHORRO_DEPOSITO, "MOV-002",
                EstadoAsiento.ANULADO, null, "Error en monto", null,
                List.of(
                        PartidaAsiento.alDebe(caja.getId(), new BigDecimal("200.00"), 1, null),
                        PartidaAsiento.alHaber(depositos.getId(), new BigDecimal("200.00"), 2, null)
                ),
                null, null, 0L);

        when(cuentaRepo.listarTodas()).thenReturn(List.of(caja, depositos));
    }

    // ─── Happy path ────────────────────────────────────────────────────────

    @Test
    @DisplayName("período con 1 asiento → totales y partidas correctos")
    void happy_path_un_asiento() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta))
                .thenReturn(List.of(asientoRegistrado));

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), UUID.randomUUID());

        assertThat(resp.asientos()).hasSize(1);
        assertThat(resp.totales().cantidadAsientos()).isEqualTo(1);
        assertThat(resp.totales().cantidadAnulados()).isZero();
        assertThat(resp.totales().totalDebe()).isEqualByComparingTo("100.00");
        assertThat(resp.totales().totalHaber()).isEqualByComparingTo("100.00");
        assertThat(resp.totales().balanceado()).isTrue();
    }

    @Test
    @DisplayName("encabezado incluye razón social, RIF y rango")
    void encabezado_completo() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta)).thenReturn(List.of());

        UUID solicitante = UUID.randomUUID();
        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), solicitante);

        assertThat(resp.encabezado().razonSocial()).isEqualTo("Fatrans Test");
        assertThat(resp.encabezado().rif()).isEqualTo("J-TEST-0");
        assertThat(resp.encabezado().desde()).isEqualTo(desde);
        assertThat(resp.encabezado().hasta()).isEqualTo(hasta);
        assertThat(resp.encabezado().generadoPorUsuarioId()).isEqualTo(solicitante);
        assertThat(resp.encabezado().incluyeAnulados()).isTrue();
        assertThat(resp.encabezado().generadoEn()).isNotNull();
    }

    @Test
    @DisplayName("partidas se renderizan con código + nombre de cuenta resuelto")
    void partidas_resuelven_nombre_cuenta() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta))
                .thenReturn(List.of(asientoRegistrado));

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), UUID.randomUUID());

        LibroDiarioResponse.AsientoDiario a = resp.asientos().get(0);
        assertThat(a.partidas()).hasSize(2);
        // DEBE
        assertThat(a.partidas().get(0).codigoCuenta()).isEqualTo("1.1.03");
        assertThat(a.partidas().get(0).nombreCuenta()).isEqualTo("Bancos Cta Corriente Bs");
        assertThat(a.partidas().get(0).debe()).isEqualByComparingTo("100.00");
        // HABER
        assertThat(a.partidas().get(1).codigoCuenta()).isEqualTo("2.1.01");
        assertThat(a.partidas().get(1).nombreCuenta()).isEqualTo("Cuentas de Ahorro Bs");
        assertThat(a.partidas().get(1).haber()).isEqualByComparingTo("100.00");
    }

    // ─── Anulados ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("incluirAnulados=true → incluye ambos asientos y cuenta los anulados")
    void incluye_anulados_true() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta))
                .thenReturn(List.of(asientoRegistrado, asientoAnulado));

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), UUID.randomUUID());

        assertThat(resp.asientos()).hasSize(2);
        assertThat(resp.totales().cantidadAsientos()).isEqualTo(2);
        assertThat(resp.totales().cantidadAnulados()).isEqualTo(1);
        assertThat(resp.totales().totalDebe()).isEqualByComparingTo("300.00");
        assertThat(resp.totales().totalHaber()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("incluirAnulados=false → filtra los ANULADOS")
    void incluye_anulados_false() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta))
                .thenReturn(List.of(asientoRegistrado, asientoAnulado));

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, false), UUID.randomUUID());

        assertThat(resp.asientos()).hasSize(1);
        assertThat(resp.totales().cantidadAnulados()).isZero();
        assertThat(resp.totales().totalDebe()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("asiento anulado mantiene motivoAnulacion visible")
    void asiento_anulado_expone_motivo() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta))
                .thenReturn(List.of(asientoAnulado));

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), UUID.randomUUID());

        LibroDiarioResponse.AsientoDiario a = resp.asientos().get(0);
        assertThat(a.estado()).isEqualTo(EstadoAsiento.ANULADO);
        assertThat(a.motivoAnulacion()).isEqualTo("Error en monto");
    }

    // ─── Formato del número correlativo ────────────────────────────────────

    @Test
    @DisplayName("numeroFormateado usa formato AÑO-NNNNNN")
    void numero_formateado_anio_y_seis_digitos() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta))
                .thenReturn(List.of(asientoRegistrado));

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), UUID.randomUUID());

        assertThat(resp.asientos().get(0).numeroFormateado()).isEqualTo("2026-000001");
    }

    // ─── Casos borde ───────────────────────────────────────────────────────

    @Test
    @DisplayName("período sin asientos → response vacío balanceado en cero")
    void periodo_vacio() {
        when(asientoRepo.listarPorRangoFecha(desde, hasta)).thenReturn(List.of());

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), UUID.randomUUID());

        assertThat(resp.asientos()).isEmpty();
        assertThat(resp.totales().cantidadAsientos()).isZero();
        assertThat(resp.totales().totalDebe()).isEqualByComparingTo("0");
        assertThat(resp.totales().totalHaber()).isEqualByComparingTo("0");
        assertThat(resp.totales().balanceado()).isTrue();  // 0 = 0
    }

    @Test
    @DisplayName("cuenta no encontrada en plan → fallback al UUID (no crashea)")
    void cuenta_faltante_no_rompe() {
        // Devolver plan vacío para forzar el fallback
        when(cuentaRepo.listarTodas()).thenReturn(List.of());
        when(asientoRepo.listarPorRangoFecha(desde, hasta))
                .thenReturn(List.of(asientoRegistrado));

        LibroDiarioResponse resp = useCase.ejecutar(
                new LibroDiarioFilter(desde, hasta, true), UUID.randomUUID());

        // El reporte se genera, pero las partidas tienen el nombre fallback
        LibroDiarioResponse.PartidaDiario p = resp.asientos().get(0).partidas().get(0);
        assertThat(p.nombreCuenta()).contains("CUENTA DESCONOCIDA");
        assertThat(p.codigoCuenta()).isEqualTo("???");
    }
}
