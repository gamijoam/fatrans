package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
import com.tufondo.contabilidad.application.dto.EstadoResultadosFilter;
import com.tufondo.contabilidad.application.dto.EstadoResultadosResponse;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.SaldoCuenta;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests del {@link GenerarEstadoResultadosUseCase} (sub-issue #271).
 *
 * <p>Cobertura: roll-up jerarquía, excedente vs déficit, poda de ceros,
 * exclusión de tipos que no son ingresos/egresos.</p>
 */
@ExtendWith(MockitoExtension.class)
class GenerarEstadoResultadosUseCaseTest {

    @Mock private AsientoContableRepository asientoRepo;
    @Mock private CuentaContableRepository cuentaRepo;

    @InjectMocks private GenerarEstadoResultadosUseCase useCase;

    // Plan mínimo: 4 → 4.1 → 4.1.01 (Intereses), 5 → 5.2 → 5.2.01 (Sueldos)
    private CuentaContable rubroIngresos, grupoIngresoCart, ingresoIntereses;
    private CuentaContable rubroEgresos, grupoEgresoOper, egresoSueldos;
    private CuentaContable activo;  // tipo 1 — NO debe aparecer

    private final LocalDate desde = LocalDate.of(2026, 5, 1);
    private final LocalDate hasta = LocalDate.of(2026, 5, 31);

    @BeforeEach
    void setUp() {
        EntidadProperties props = new EntidadProperties();
        props.setRazonSocial("Fatrans Test");
        props.setRif("J-TEST-0");
        useCase = new GenerarEstadoResultadosUseCase(asientoRepo, cuentaRepo, props);

        rubroIngresos = CuentaContable.crear(
                "4", "INGRESOS", TipoCuentaContable.INGRESO,
                NaturalezaSaldo.ACREEDORA, null, false, null);
        grupoIngresoCart = CuentaContable.crear(
                "4.1", "Ingresos Cartera", TipoCuentaContable.INGRESO,
                NaturalezaSaldo.ACREEDORA, rubroIngresos.getId(), false, null);
        ingresoIntereses = CuentaContable.crear(
                "4.1.01", "Intereses sobre Créditos", TipoCuentaContable.INGRESO,
                NaturalezaSaldo.ACREEDORA, grupoIngresoCart.getId(), true, null);

        rubroEgresos = CuentaContable.crear(
                "5", "EGRESOS", TipoCuentaContable.EGRESO,
                NaturalezaSaldo.DEUDORA, null, false, null);
        grupoEgresoOper = CuentaContable.crear(
                "5.2", "Egresos Operativos", TipoCuentaContable.EGRESO,
                NaturalezaSaldo.DEUDORA, rubroEgresos.getId(), false, null);
        egresoSueldos = CuentaContable.crear(
                "5.2.01", "Sueldos", TipoCuentaContable.EGRESO,
                NaturalezaSaldo.DEUDORA, grupoEgresoOper.getId(), true, null);

        // Cuenta de tipo 1 que NO debe aparecer en el ER
        activo = CuentaContable.crear(
                "1.1.03", "Bancos", TipoCuentaContable.ACTIVO,
                NaturalezaSaldo.DEUDORA, UUID.randomUUID(), true, null);

        lenient().when(cuentaRepo.listarTodas()).thenReturn(List.of(
                rubroIngresos, grupoIngresoCart, ingresoIntereses,
                rubroEgresos, grupoEgresoOper, egresoSueldos, activo));
    }

    @Test
    @DisplayName("período sin movimientos → ingresos=0, egresos=0, excedente=0 (poda total)")
    void periodo_vacio() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());

        EstadoResultadosResponse resp = useCase.ejecutar(
                EstadoResultadosFilter.de(desde, hasta), UUID.randomUUID());

        assertThat(resp.ingresos().total()).isEqualByComparingTo("0");
        assertThat(resp.egresos().total()).isEqualByComparingTo("0");
        assertThat(resp.excedente()).isEqualByComparingTo("0");
        assertThat(resp.excedenteEtiqueta()).isEqualTo("—");
        // poda: rubros con saldo cero no aparecen
        assertThat(resp.ingresos().rubros()).isEmpty();
        assertThat(resp.egresos().rubros()).isEmpty();
    }

    @Test
    @DisplayName("ingresos > egresos → EXCEDENTE")
    void excedente_positivo() {
        // Intereses: saldo período = 1000 (acreedor)
        // Sueldos: saldo período = 400 (deudor)
        cuandoCalcularSaldoDevolver(ingresoIntereses.getId(), "0", "1000", "0", "0");
        cuandoCalcularSaldoDevolver(egresoSueldos.getId(), "400", "0", "0", "0");

        EstadoResultadosResponse resp = useCase.ejecutar(
                EstadoResultadosFilter.de(desde, hasta), UUID.randomUUID());

        assertThat(resp.ingresos().total()).isEqualByComparingTo("1000");
        assertThat(resp.egresos().total()).isEqualByComparingTo("400");
        assertThat(resp.excedente()).isEqualByComparingTo("600");
        assertThat(resp.excedenteEtiqueta()).isEqualTo("EXCEDENTE");
    }

    @Test
    @DisplayName("egresos > ingresos → DÉFICIT")
    void deficit() {
        cuandoCalcularSaldoDevolver(ingresoIntereses.getId(), "0", "100", "0", "0");
        cuandoCalcularSaldoDevolver(egresoSueldos.getId(), "500", "0", "0", "0");

        EstadoResultadosResponse resp = useCase.ejecutar(
                EstadoResultadosFilter.de(desde, hasta), UUID.randomUUID());

        assertThat(resp.excedente()).isEqualByComparingTo("400");
        assertThat(resp.excedenteEtiqueta()).isEqualTo("DÉFICIT");
    }

    @Test
    @DisplayName("solo cuentas de tipo INGRESO/EGRESO aparecen — activo (1.1.03) NO")
    void excluye_otros_tipos() {
        // El activo NO debe ser consultado por el use case (se filtra por tipo).
        // Si fuera consultado, devolvemos saldo grande para que sea evidente
        // si el filtro fallara (lenient porque no debería invocarse).
        lenient().when(asientoRepo.calcularSaldoCuentaHasta(eq(activo.getId()), any()))
                .thenReturn(new SaldoCuenta(new BigDecimal("9999"), BigDecimal.ZERO));
        cuandoCalcularSaldoDevolver(ingresoIntereses.getId(), "0", "100", "0", "0");
        when(asientoRepo.calcularSaldoCuentaHasta(eq(egresoSueldos.getId()), any()))
                .thenReturn(SaldoCuenta.cero());

        EstadoResultadosResponse resp = useCase.ejecutar(
                EstadoResultadosFilter.de(desde, hasta), UUID.randomUUID());

        // El activo no debe aparecer en ninguna sección
        boolean activoAparece = resp.ingresos().rubros().stream()
                .anyMatch(r -> r.codigo().equals("1.1.03"))
                || resp.egresos().rubros().stream()
                .anyMatch(r -> r.codigo().equals("1.1.03"));
        assertThat(activoAparece).isFalse();
    }

    @Test
    @DisplayName("roll-up: rubro 4 = grupo 4.1 = hoja 4.1.01")
    void rollup_funciona() {
        cuandoCalcularSaldoDevolver(ingresoIntereses.getId(), "0", "750", "0", "0");
        when(asientoRepo.calcularSaldoCuentaHasta(eq(egresoSueldos.getId()), any()))
                .thenReturn(SaldoCuenta.cero());

        EstadoResultadosResponse resp = useCase.ejecutar(
                EstadoResultadosFilter.de(desde, hasta), UUID.randomUUID());

        assertThat(resp.ingresos().rubros()).hasSize(1);
        var rubro4 = resp.ingresos().rubros().get(0);
        assertThat(rubro4.codigo()).isEqualTo("4");
        assertThat(rubro4.saldoNeto()).isEqualByComparingTo("750");
        // grupo 4.1
        assertThat(rubro4.hijos()).hasSize(1);
        var grupo = rubro4.hijos().get(0);
        assertThat(grupo.codigo()).isEqualTo("4.1");
        assertThat(grupo.saldoNeto()).isEqualByComparingTo("750");
        // hoja 4.1.01
        assertThat(grupo.hijos()).hasSize(1);
        assertThat(grupo.hijos().get(0).codigo()).isEqualTo("4.1.01");
        assertThat(grupo.hijos().get(0).saldoNeto()).isEqualByComparingTo("750");
    }

    @Test
    @DisplayName("incluirCeros=true: muestra árbol completo aunque saldos sean cero")
    void incluir_ceros_muestra_todo() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());

        EstadoResultadosResponse resp = useCase.ejecutar(
                new EstadoResultadosFilter(desde, hasta, true), UUID.randomUUID());

        // Con incluirCeros=true, los rubros aparecen aunque vacíos
        assertThat(resp.ingresos().rubros()).hasSize(1);
        assertThat(resp.egresos().rubros()).hasSize(1);
    }

    @Test
    @DisplayName("período calculado correctamente: saldo_hasta − saldo_antesDesde")
    void calcula_saldo_periodo_diferencial() {
        // Saldo acumulado hasta hasta: 1500 acreedor
        // Saldo acumulado hasta desde-1: 500 acreedor
        // → saldo del período: 1000
        when(asientoRepo.calcularSaldoCuentaHasta(eq(ingresoIntereses.getId()), eq(hasta)))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("1500")));
        when(asientoRepo.calcularSaldoCuentaHasta(eq(ingresoIntereses.getId()), eq(desde.minusDays(1))))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("500")));
        when(asientoRepo.calcularSaldoCuentaHasta(eq(egresoSueldos.getId()), any()))
                .thenReturn(SaldoCuenta.cero());

        EstadoResultadosResponse resp = useCase.ejecutar(
                EstadoResultadosFilter.de(desde, hasta), UUID.randomUUID());

        assertThat(resp.ingresos().total()).isEqualByComparingTo("1000");
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    /** Mock cómodo: setea saldo hasta(hasta) y hasta(desde-1) con valores. */
    private void cuandoCalcularSaldoDevolver(UUID cuentaId,
                                              String debeHasta, String haberHasta,
                                              String debeAntes, String haberAntes) {
        when(asientoRepo.calcularSaldoCuentaHasta(eq(cuentaId), eq(hasta)))
                .thenReturn(new SaldoCuenta(new BigDecimal(debeHasta), new BigDecimal(haberHasta)));
        when(asientoRepo.calcularSaldoCuentaHasta(eq(cuentaId), eq(desde.minusDays(1))))
                .thenReturn(new SaldoCuenta(new BigDecimal(debeAntes), new BigDecimal(haberAntes)));
    }
}
