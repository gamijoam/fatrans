package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
import com.tufondo.contabilidad.application.dto.BalanceGeneralFilter;
import com.tufondo.contabilidad.application.dto.BalanceGeneralResponse;
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
 * Tests del {@link GenerarBalanceGeneralUseCase} (sub-issue #271).
 *
 * <p>Cobertura: roll-up jerarquía A/P/Patrim, cuentas correctoras restan,
 * excedente del ejercicio integrado, validación de cuadre, poda de ceros.</p>
 */
@ExtendWith(MockitoExtension.class)
class GenerarBalanceGeneralUseCaseTest {

    @Mock private AsientoContableRepository asientoRepo;
    @Mock private CuentaContableRepository cuentaRepo;
    @Mock private GenerarEstadoResultadosUseCase estadoResultadosUseCase;

    private GenerarBalanceGeneralUseCase useCase;

    // Plan: Activo (1.1.03 Bancos + 1.3.01 Cartera + 1.3.99 Provisión correctora),
    //       Pasivo (2.1.01 Ahorros), Patrimonio (3.1.01 Aportes)
    private CuentaContable rubroAct, grupoDisp, banco;
    private CuentaContable grupoCart, cartera, provisionCartera;
    private CuentaContable rubroPas, grupoDep, ahorros;
    private CuentaContable rubroPat, grupoAp, aportes;

    private final LocalDate fecha = LocalDate.of(2026, 5, 31);

    @BeforeEach
    void setUp() {
        EntidadProperties props = new EntidadProperties();
        props.setRazonSocial("Fatrans Test");
        props.setRif("J-TEST-0");
        useCase = new GenerarBalanceGeneralUseCase(asientoRepo, cuentaRepo, props, estadoResultadosUseCase);

        // ACTIVO
        rubroAct = CuentaContable.crear("1", "ACTIVO",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, null, false, null);
        grupoDisp = CuentaContable.crear("1.1", "Disponible",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, rubroAct.getId(), false, null);
        banco = CuentaContable.crear("1.1.03", "Bancos Bs",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, grupoDisp.getId(), true, null);
        grupoCart = CuentaContable.crear("1.3", "Cartera Créditos",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, rubroAct.getId(), false, null);
        cartera = CuentaContable.crear("1.3.01", "Créditos por Cobrar",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, grupoCart.getId(), true, null);
        // Correctora: ACTIVO con naturaleza ACREEDORA
        provisionCartera = CuentaContable.crear("1.3.99", "Provisión Cartera (CR)",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.ACREEDORA, grupoCart.getId(), true, null);

        // PASIVO
        rubroPas = CuentaContable.crear("2", "PASIVO",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA, null, false, null);
        grupoDep = CuentaContable.crear("2.1", "Depósitos Asociados",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA, rubroPas.getId(), false, null);
        ahorros = CuentaContable.crear("2.1.01", "Cuentas de Ahorro Bs",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA, grupoDep.getId(), true, null);

        // PATRIMONIO
        rubroPat = CuentaContable.crear("3", "PATRIMONIO",
                TipoCuentaContable.PATRIMONIO, NaturalezaSaldo.ACREEDORA, null, false, null);
        grupoAp = CuentaContable.crear("3.1", "Aportes",
                TipoCuentaContable.PATRIMONIO, NaturalezaSaldo.ACREEDORA, rubroPat.getId(), false, null);
        aportes = CuentaContable.crear("3.1.01", "Aportes Sociales",
                TipoCuentaContable.PATRIMONIO, NaturalezaSaldo.ACREEDORA, grupoAp.getId(), true, null);

        lenient().when(cuentaRepo.listarTodas()).thenReturn(List.of(
                rubroAct, grupoDisp, banco, grupoCart, cartera, provisionCartera,
                rubroPas, grupoDep, ahorros,
                rubroPat, grupoAp, aportes));

        // Excedente del ER default = cero
        lenient().when(estadoResultadosUseCase.ejecutar(any(), any()))
                .thenReturn(estadoResultadosResponseConExcedente(BigDecimal.ZERO, "—"));
    }

    @Test
    @DisplayName("balance vacío (sin saldos, sin excedente) → balanceado en cero")
    void balance_vacio() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(fecha), UUID.randomUUID());

        assertThat(resp.totales().totalActivo()).isEqualByComparingTo("0");
        assertThat(resp.totales().totalPasivo()).isEqualByComparingTo("0");
        assertThat(resp.totales().totalPatrimonio()).isEqualByComparingTo("0");
        assertThat(resp.totales().balanceado()).isTrue();
    }

    @Test
    @DisplayName("balance simple cuadrado: A=15000 / (P=5000 + Pat=10000)")
    void balance_simple_cuadra() {
        // Activo: bancos 15000 D
        when(asientoRepo.calcularSaldoCuentaHasta(eq(banco.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(new BigDecimal("15000"), BigDecimal.ZERO));
        // Pasivo: ahorros 5000 A
        when(asientoRepo.calcularSaldoCuentaHasta(eq(ahorros.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("5000")));
        // Patrimonio: aportes 10000 A
        when(asientoRepo.calcularSaldoCuentaHasta(eq(aportes.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("10000")));
        // Resto de cuentas hoja: cero
        when(asientoRepo.calcularSaldoCuentaHasta(eq(cartera.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(provisionCartera.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(fecha), UUID.randomUUID());

        assertThat(resp.totales().totalActivo()).isEqualByComparingTo("15000");
        assertThat(resp.totales().totalPasivo()).isEqualByComparingTo("5000");
        assertThat(resp.totales().totalPatrimonio()).isEqualByComparingTo("10000");
        assertThat(resp.totales().balanceado()).isTrue();
    }

    @Test
    @DisplayName("cuenta correctora 1.3.99 con saldo resta del rubro padre")
    void correctora_resta() {
        // Cartera bruta: 10000
        when(asientoRepo.calcularSaldoCuentaHasta(eq(cartera.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(new BigDecimal("10000"), BigDecimal.ZERO));
        // Provisión cartera (CR, naturaleza acreedora dentro de activo): 500
        when(asientoRepo.calcularSaldoCuentaHasta(eq(provisionCartera.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("500")));
        // Resto cero
        when(asientoRepo.calcularSaldoCuentaHasta(eq(banco.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(ahorros.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(aportes.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(fecha), UUID.randomUUID());

        // Activo total = 10000 (cartera) − 500 (provisión) = 9500
        assertThat(resp.totales().totalActivo()).isEqualByComparingTo("9500");

        // Verificar que la provisión aparece marcada como correctora
        BalanceGeneralResponse.NodoCuenta rubro1 = resp.activo().rubros().get(0);
        BalanceGeneralResponse.NodoCuenta grupo13 = rubro1.hijos().stream()
                .filter(g -> g.codigo().equals("1.3"))
                .findFirst().orElseThrow();
        BalanceGeneralResponse.NodoCuenta provision = grupo13.hijos().stream()
                .filter(c -> c.codigo().equals("1.3.99"))
                .findFirst().orElseThrow();
        assertThat(provision.esCorrectora()).isTrue();
    }

    @Test
    @DisplayName("excedente del ER se suma al patrimonio para cuadrar")
    void excedente_se_integra() {
        // Resto cero por default
        lenient().when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        // Activo bancos: 1000 D
        when(asientoRepo.calcularSaldoCuentaHasta(eq(banco.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(new BigDecimal("1000"), BigDecimal.ZERO));
        // Patrimonio aportes: 700 A
        when(asientoRepo.calcularSaldoCuentaHasta(eq(aportes.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("700")));

        when(estadoResultadosUseCase.ejecutar(any(), any()))
                .thenReturn(estadoResultadosResponseConExcedente(new BigDecimal("300"), "EXCEDENTE"));

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(fecha), UUID.randomUUID());

        assertThat(resp.totales().totalActivo()).isEqualByComparingTo("1000");
        assertThat(resp.totales().totalPasivo()).isEqualByComparingTo("0");
        assertThat(resp.totales().totalPatrimonio()).isEqualByComparingTo("700");
        assertThat(resp.totales().excedenteEjercicio()).isEqualByComparingTo("300");
        assertThat(resp.totales().totalPasivoMasPatrimonio()).isEqualByComparingTo("1000");
        assertThat(resp.totales().balanceado()).isTrue();
        assertThat(resp.excedenteEtiqueta()).isEqualTo("EXCEDENTE");
    }

    @Test
    @DisplayName("déficit del ER se resta del patrimonio")
    void deficit_se_resta() {
        // Activo 500, Patrimonio aportes 800, Déficit 300 → cuadrar
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(banco.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(new BigDecimal("500"), BigDecimal.ZERO));
        when(asientoRepo.calcularSaldoCuentaHasta(eq(aportes.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("800")));

        when(estadoResultadosUseCase.ejecutar(any(), any()))
                .thenReturn(estadoResultadosResponseConExcedente(new BigDecimal("300"), "DÉFICIT"));

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(fecha), UUID.randomUUID());

        assertThat(resp.totales().excedenteEjercicio()).isEqualByComparingTo("-300");
        // Activo 500 = 0 (pasivo) + 800 (patrimonio) − 300 (déficit) = 500 ✓
        assertThat(resp.totales().balanceado()).isTrue();
        assertThat(resp.excedenteEtiqueta()).isEqualTo("DÉFICIT");
    }

    @Test
    @DisplayName("incluirCeros=false poda cuentas con saldo cero")
    void poda_de_ceros_default() {
        when(asientoRepo.calcularSaldoCuentaHasta(eq(banco.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(new BigDecimal("100"), BigDecimal.ZERO));
        // Resto cero
        when(asientoRepo.calcularSaldoCuentaHasta(eq(cartera.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(provisionCartera.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(ahorros.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(aportes.getId()), eq(fecha)))
                .thenReturn(SaldoCuenta.cero());

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(fecha), UUID.randomUUID());

        // Solo rubro 1 con grupo 1.1 con cuenta 1.1.03 aparece
        BalanceGeneralResponse.NodoCuenta rubro = resp.activo().rubros().get(0);
        // Grupo 1.3 NO aparece (cartera + provisión ambos cero)
        boolean tieneGrupo13 = rubro.hijos().stream().anyMatch(g -> g.codigo().equals("1.3"));
        assertThat(tieneGrupo13).isFalse();
        // Sí tiene 1.1
        boolean tieneGrupo11 = rubro.hijos().stream().anyMatch(g -> g.codigo().equals("1.1"));
        assertThat(tieneGrupo11).isTrue();
    }

    @Test
    @DisplayName("incluirCeros=true muestra árbol completo")
    void incluir_ceros_muestra_todo() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());

        BalanceGeneralResponse resp = useCase.ejecutar(
                new BalanceGeneralFilter(fecha, null, true), UUID.randomUUID());

        // 3 rubros visibles aunque cero (1, 2, 3)
        assertThat(resp.activo().rubros()).hasSize(1);
        assertThat(resp.pasivo().rubros()).hasSize(1);
        assertThat(resp.patrimonio().rubros()).hasSize(1);
    }

    @Test
    @DisplayName("encabezado completo con fechaCorte e inicioEjercicio resuelto")
    void encabezado_completo() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(LocalDate.of(2026, 6, 30)), UUID.randomUUID());

        assertThat(resp.encabezado().razonSocial()).isEqualTo("Fatrans Test");
        assertThat(resp.encabezado().fechaCorte()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(resp.encabezado().inicioEjercicio()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("balance desbalanceado: diferencia visible y flag false (defensivo)")
    void desbalance_se_reporta() {
        // Activo: 1000, Pasivo+Patrim: 600 → diferencia 400
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.calcularSaldoCuentaHasta(eq(banco.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(new BigDecimal("1000"), BigDecimal.ZERO));
        when(asientoRepo.calcularSaldoCuentaHasta(eq(ahorros.getId()), eq(fecha)))
                .thenReturn(new SaldoCuenta(BigDecimal.ZERO, new BigDecimal("600")));

        BalanceGeneralResponse resp = useCase.ejecutar(
                BalanceGeneralFilter.al(fecha), UUID.randomUUID());

        assertThat(resp.totales().diferencia()).isEqualByComparingTo("400");
        assertThat(resp.totales().balanceado()).isFalse();
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private EstadoResultadosResponse estadoResultadosResponseConExcedente(
            BigDecimal excedente, String etiqueta) {
        return EstadoResultadosResponse.builder()
                .encabezado(EstadoResultadosResponse.Encabezado.builder()
                        .razonSocial("Test").rif("J-T")
                        .desde(LocalDate.now()).hasta(LocalDate.now())
                        .build())
                .ingresos(EstadoResultadosResponse.Seccion.builder()
                        .tipo(TipoCuentaContable.INGRESO).titulo("INGRESOS")
                        .rubros(List.of()).total(BigDecimal.ZERO).build())
                .egresos(EstadoResultadosResponse.Seccion.builder()
                        .tipo(TipoCuentaContable.EGRESO).titulo("EGRESOS")
                        .rubros(List.of()).total(BigDecimal.ZERO).build())
                .excedente(excedente)
                .excedenteEtiqueta(etiqueta)
                .build();
    }
}
