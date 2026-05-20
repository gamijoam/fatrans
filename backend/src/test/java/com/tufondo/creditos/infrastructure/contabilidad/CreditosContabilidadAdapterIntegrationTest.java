package com.tufondo.creditos.infrastructure.contabilidad;

import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.contabilidad.application.usecase.AsientoContableService;
import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import com.tufondo.contabilidad.domain.repository.AsientoContableRepository;
import com.tufondo.contabilidad.infrastructure.persistence.adapter.AsientoContableRepositoryImpl;
import com.tufondo.contabilidad.infrastructure.persistence.adapter.CuentaContableRepositoryImpl;
import com.tufondo.contabilidad.infrastructure.persistence.jpa.AsientoContableJpaRepository;
import com.tufondo.contabilidad.infrastructure.persistence.jpa.CuentaContableJpaRepository;
import com.tufondo.contabilidad.infrastructure.persistence.jpa.PartidaAsientoJpaRepository;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración E2E del hook contable de Créditos (sub-issue #268).
 *
 * <p>Arranca el contexto JPA mínimo: el adapter real
 * {@link CreditosContabilidadAdapter} delega al {@link AsientoContableService}
 * real, que persiste vía {@link AsientoContableRepositoryImpl} contra H2.</p>
 *
 * <p>El seed V21 NO corre porque tests tienen {@code flyway.enabled=false}.
 * Se pre-pueblan manualmente las cuentas mínimas que el adapter referencia.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import({
        CreditosContabilidadAdapter.class,
        AsientoContableService.class,
        AsientoContableRepositoryImpl.class,
        CuentaContableRepositoryImpl.class
})
@DisplayName("CreditosContabilidadAdapter — integración con H2 (BD real)")
class CreditosContabilidadAdapterIntegrationTest {

    @Autowired private CreditosContabilidadAdapter adapter;
    @Autowired private AsientoContableRepository asientoRepo;
    @Autowired private AsientoContableJpaRepository asientoJpa;
    @Autowired private PartidaAsientoJpaRepository partidaJpa;
    @Autowired private CuentaContableJpaRepository cuentaJpa;
    @Autowired private CuentaContableRepositoryImpl cuentaRepoAdapter;
    @Autowired private EntityManager em;

    private UUID carteraId, bancoBsId, ingresoInteresesId, ingresoComisionId, ingresoMoraId;

    @BeforeEach
    void setUp() {
        partidaJpa.deleteAll();
        asientoJpa.deleteAll();
        cuentaJpa.deleteAll();

        em.createNativeQuery("CREATE SEQUENCE IF NOT EXISTS seq_asiento_numero START WITH 1 INCREMENT BY 1")
                .executeUpdate();

        // Activo
        UUID rubroAct = persistirCuenta("1", "ACTIVO",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, null, false);
        UUID grupoDisp = persistirCuenta("1.1", "DISPONIBLE",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, rubroAct, false);
        bancoBsId = persistirCuenta("1.1.03", "Bancos Cta Corriente Bs",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, grupoDisp, true);
        UUID grupoCart = persistirCuenta("1.3", "CARTERA DE CRÉDITOS",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, rubroAct, false);
        carteraId = persistirCuenta("1.3.01", "Créditos Personales por Cobrar",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, grupoCart, true);

        // Ingresos
        UUID rubroIng = persistirCuenta("4", "INGRESOS",
                TipoCuentaContable.INGRESO, NaturalezaSaldo.ACREEDORA, null, false);
        UUID grupoIngCart = persistirCuenta("4.1", "INGRESOS POR CARTERA",
                TipoCuentaContable.INGRESO, NaturalezaSaldo.ACREEDORA, rubroIng, false);
        ingresoInteresesId = persistirCuenta("4.1.01", "Intereses sobre Créditos",
                TipoCuentaContable.INGRESO, NaturalezaSaldo.ACREEDORA, grupoIngCart, true);
        ingresoComisionId = persistirCuenta("4.1.02", "Comisiones por Otorgamiento",
                TipoCuentaContable.INGRESO, NaturalezaSaldo.ACREEDORA, grupoIngCart, true);
        ingresoMoraId = persistirCuenta("4.1.03", "Intereses Moratorios",
                TipoCuentaContable.INGRESO, NaturalezaSaldo.ACREEDORA, grupoIngCart, true);
    }

    // ─── Desembolso ────────────────────────────────────────────────────────

    @Test
    @DisplayName("E2E desembolso SIN comisión: asiento DEBE 1.3.01 / HABER 1.1.03")
    void e2e_desembolso_sin_comision() {
        SolicitudCredito sol = solicitudConMonto("SOL-CRED-2026-001", "10000.00");

        adapter.registrarDesembolso(sol, new BigDecimal("10000.00"), BigDecimal.ZERO);

        AsientoContable asiento = asientoUnico("SOL-CRED-2026-001");
        assertThat(asiento.getOrigen()).isEqualTo(OrigenAsiento.CREDITO_DESEMBOLSO);
        assertThat(asiento.getEstado()).isEqualTo(EstadoAsiento.REGISTRADO);
        assertThat(asiento.estaBalanceado()).isTrue();
        assertThat(asiento.totalDebe()).isEqualByComparingTo("10000.00");
        assertThat(asiento.getPartidas()).hasSize(2);

        PartidaAsiento debe = partidaDebe(asiento, carteraId);
        PartidaAsiento haber = partidaHaber(asiento, bancoBsId);
        assertThat(debe.getDebe()).isEqualByComparingTo("10000.00");
        assertThat(haber.getHaber()).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("E2E desembolso CON comisión: 3 partidas, suma cuadra")
    void e2e_desembolso_con_comision() {
        SolicitudCredito sol = solicitudConMonto("SOL-CRED-2026-002", "10000.00");

        adapter.registrarDesembolso(sol, new BigDecimal("9500.00"), new BigDecimal("500.00"));

        AsientoContable asiento = asientoUnico("SOL-CRED-2026-002");
        assertThat(asiento.getPartidas()).hasSize(3);
        assertThat(asiento.estaBalanceado()).isTrue();

        assertThat(partidaDebe(asiento, carteraId).getDebe()).isEqualByComparingTo("10000.00");
        assertThat(partidaHaber(asiento, bancoBsId).getHaber()).isEqualByComparingTo("9500.00");
        assertThat(partidaHaber(asiento, ingresoComisionId).getHaber()).isEqualByComparingTo("500.00");
    }

    // ─── Pago de cuota ─────────────────────────────────────────────────────

    @Test
    @DisplayName("E2E pago cuota SIN mora: 3 partidas — DEBE 1.1.03 / HABER 1.3.01 + 4.1.01")
    void e2e_pago_cuota_sin_mora() {
        SolicitudCredito sol = solicitudConMonto("SOL-CRED-2026-010", "10000.00");
        Amortizacion cuota = cuotaConDesglose(5, "400.00", "100.00", "0.00", "500.00");

        adapter.registrarPagoCuota(sol, cuota, new BigDecimal("500.00"), "REF-PAGO-A1");

        AsientoContable asiento = asientoUnico("REF-PAGO-A1");
        assertThat(asiento.getOrigen()).isEqualTo(OrigenAsiento.CREDITO_COBRO);
        assertThat(asiento.estaBalanceado()).isTrue();
        assertThat(asiento.getPartidas()).hasSize(3);

        assertThat(partidaDebe(asiento, bancoBsId).getDebe()).isEqualByComparingTo("500.00");
        assertThat(partidaHaber(asiento, carteraId).getHaber()).isEqualByComparingTo("400.00");
        assertThat(partidaHaber(asiento, ingresoInteresesId).getHaber()).isEqualByComparingTo("100.00");

        // NO debe haber partida de mora
        assertThat(asiento.getPartidas())
                .extracting(PartidaAsiento::getCuentaId)
                .doesNotContain(ingresoMoraId);
    }

    @Test
    @DisplayName("E2E pago cuota CON mora: 4 partidas (la 4ta = 4.1.03 mora)")
    void e2e_pago_cuota_con_mora() {
        SolicitudCredito sol = solicitudConMonto("SOL-CRED-2026-011", "10000.00");
        Amortizacion cuota = cuotaConDesglose(8, "400.00", "100.00", "30.00", "500.00");

        adapter.registrarPagoCuota(sol, cuota, new BigDecimal("530.00"), "REF-PAGO-MORA1");

        AsientoContable asiento = asientoUnico("REF-PAGO-MORA1");
        assertThat(asiento.getPartidas()).hasSize(4);
        assertThat(asiento.estaBalanceado()).isTrue();

        assertThat(partidaDebe(asiento, bancoBsId).getDebe()).isEqualByComparingTo("530.00");
        assertThat(partidaHaber(asiento, ingresoMoraId).getHaber()).isEqualByComparingTo("30.00");
    }

    // ─── Casos negativos ────────────────────────────────────────────────────

    @Test
    @DisplayName("E2E: cuenta 1.3.01 inexistente en plan → AsientoContableException")
    void e2e_error_cuenta_cartera_inexistente() {
        // Borrar 1.3.01 del plan para simular plan mal configurado
        cuentaJpa.deleteById(carteraId);
        em.flush();

        SolicitudCredito sol = solicitudConMonto("SOL-FAIL", "100.00");

        assertThatThrownBy(() -> adapter.registrarDesembolso(
                sol, new BigDecimal("100.00"), BigDecimal.ZERO))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("1.3.01");
    }

    @Test
    @DisplayName("E2E: decimales NUMERIC(18,4) se preservan sin truncado")
    void e2e_decimales_preservados() {
        SolicitudCredito sol = solicitudConMonto("SOL-DEC", "999.9999");

        adapter.registrarDesembolso(sol, new BigDecimal("999.9999"), BigDecimal.ZERO);

        AsientoContable a = asientoUnico("SOL-DEC");
        assertThat(a.totalDebe()).isEqualByComparingTo("999.9999");
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private UUID persistirCuenta(String codigo, String nombre, TipoCuentaContable tipo,
                                  NaturalezaSaldo naturaleza, UUID padre, boolean aceptaMov) {
        CuentaContable c = CuentaContable.crear(codigo, nombre, tipo, naturaleza, padre, aceptaMov, null);
        cuentaRepoAdapter.guardar(c);
        return c.getId();
    }

    private SolicitudCredito solicitudConMonto(String numero, String monto) {
        return SolicitudCredito.builder()
                .id(UUID.randomUUID())
                .numeroSolicitud(numero)
                .socioId(UUID.randomUUID())
                .montoSolicitado(new BigDecimal(monto))
                .cuentaDestino("0134-XXXX")
                .build();
    }

    private Amortizacion cuotaConDesglose(int nro, String capital, String interes,
                                           String mora, String montoCuota) {
        return Amortizacion.builder()
                .id(UUID.randomUUID())
                .numeroCuota(nro)
                .capital(new BigDecimal(capital))
                .interes(new BigDecimal(interes))
                .interesMora(new BigDecimal(mora))
                .montoCuota(new BigDecimal(montoCuota))
                .build();
    }

    private AsientoContable asientoUnico(String referencia) {
        List<AsientoContable> hits = asientoRepo.buscarPorReferenciaExterna(referencia);
        assertThat(hits).as("Asiento ref=%s debe existir y ser único", referencia).hasSize(1);
        return hits.get(0);
    }

    private PartidaAsiento partidaDebe(AsientoContable a, UUID cuentaId) {
        return a.getPartidas().stream()
                .filter(p -> p.esDeDebe() && p.getCuentaId().equals(cuentaId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No hay partida DEBE para cuenta " + cuentaId));
    }

    private PartidaAsiento partidaHaber(AsientoContable a, UUID cuentaId) {
        return a.getPartidas().stream()
                .filter(p -> p.esDeHaber() && p.getCuentaId().equals(cuentaId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No hay partida HABER para cuenta " + cuentaId));
    }
}
