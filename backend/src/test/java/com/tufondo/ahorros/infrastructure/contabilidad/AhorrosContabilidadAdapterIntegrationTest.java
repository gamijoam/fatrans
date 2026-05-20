package com.tufondo.ahorros.infrastructure.contabilidad;

import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.Moneda;
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
 * Test de integración E2E del hook contable de Ahorros (sub-issue #267).
 *
 * <p>Arranca el contexto JPA mínimo: el adapter real
 * {@link AhorrosContabilidadAdapter} delega al {@link AsientoContableService}
 * real, que persiste vía {@link AsientoContableRepositoryImpl} contra H2 (modo
 * PostgreSQL).</p>
 *
 * <p><strong>Lo que cubre:</strong></p>
 * <ul>
 *   <li>Depósito Bs end-to-end → asiento persistido con DEBE 1.1.01 / HABER 2.1.01</li>
 *   <li>Retiro Bs end-to-end → asiento persistido con DEBE 2.1.01 / HABER 1.1.01</li>
 *   <li>Depósito USD usa cuentas 1.1.05 / 2.1.02</li>
 *   <li>Cuenta contable inexistente → AsientoContableException (defensa contra
 *       desincronización entre plan y código del adapter)</li>
 * </ul>
 *
 * <p>El seed V21 NO corre porque tests tienen {@code flyway.enabled=false}.
 * Se pre-pueblan manualmente las cuentas necesarias en {@code @BeforeEach}.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import({
        AhorrosContabilidadAdapter.class,
        AsientoContableService.class,
        AsientoContableRepositoryImpl.class,
        CuentaContableRepositoryImpl.class
})
@DisplayName("AhorrosContabilidadAdapter — integración con H2 (BD real)")
class AhorrosContabilidadAdapterIntegrationTest {

    @Autowired private AhorrosContabilidadAdapter adapter;
    @Autowired private AsientoContableRepository asientoRepo;
    @Autowired private AsientoContableJpaRepository asientoJpa;
    @Autowired private PartidaAsientoJpaRepository partidaJpa;
    @Autowired private CuentaContableJpaRepository cuentaJpa;
    @Autowired private CuentaContableRepositoryImpl cuentaRepoAdapter;
    @Autowired private EntityManager em;

    private UUID cajaId, bancoUsdId, depositosBsId, depositosUsdId;

    @BeforeEach
    void setUp() {
        // Limpieza
        partidaJpa.deleteAll();
        asientoJpa.deleteAll();
        cuentaJpa.deleteAll();

        // Crear secuencia que el adapter usa para el correlativo del asiento.
        // En H2 hay que crearla a mano porque flyway está deshabilitado.
        em.createNativeQuery("CREATE SEQUENCE IF NOT EXISTS seq_asiento_numero START WITH 1 INCREMENT BY 1")
                .executeUpdate();

        // Pre-poblar plan de cuentas con SOLO lo que el adapter referencia
        // según los códigos hardcodeados en AhorrosContabilidadAdapter.
        UUID rubroActivo = persistirCuenta("1", "ACTIVO",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, null, false);
        UUID grupoDisponible = persistirCuenta("1.1", "DISPONIBLE",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, rubroActivo, false);
        cajaId = persistirCuenta("1.1.01", "Caja Principal",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, grupoDisponible, true);
        bancoUsdId = persistirCuenta("1.1.05", "Bancos USD",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA, grupoDisponible, true);

        UUID rubroPasivo = persistirCuenta("2", "PASIVO",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA, null, false);
        UUID grupoDepositos = persistirCuenta("2.1", "DEPÓSITOS",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA, rubroPasivo, false);
        depositosBsId = persistirCuenta("2.1.01", "Cuentas de Ahorro Bs",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA, grupoDepositos, true);
        depositosUsdId = persistirCuenta("2.1.02", "Cuentas de Ahorro USD",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA, grupoDepositos, true);
    }

    // ─── Depósito ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("E2E depósito Bs: asiento persistido con DEBE 1.1.01 / HABER 2.1.01")
    void e2e_deposito_bs() {
        CuentaAhorro cuenta = cuentaBs("AHO-2026-000001");
        Movimiento mov = movimientoConMonto("MOV-2026-000001", "1500.00");

        adapter.registrarDeposito(cuenta, mov);

        List<AsientoContable> hits = asientoRepo
                .buscarPorReferenciaExterna("MOV-2026-000001");
        assertThat(hits).as("Asiento debe persistirse").hasSize(1);
        AsientoContable asiento = hits.get(0);

        assertThat(asiento.getOrigen()).isEqualTo(OrigenAsiento.AHORRO_DEPOSITO);
        assertThat(asiento.getEstado()).isEqualTo(EstadoAsiento.REGISTRADO);
        assertThat(asiento.estaBalanceado()).isTrue();
        assertThat(asiento.totalDebe()).isEqualByComparingTo("1500.00");
        assertThat(asiento.getPartidas()).hasSize(2);

        PartidaAsiento debe = asiento.getPartidas().stream()
                .filter(PartidaAsiento::esDeDebe).findFirst().orElseThrow();
        PartidaAsiento haber = asiento.getPartidas().stream()
                .filter(PartidaAsiento::esDeHaber).findFirst().orElseThrow();

        assertThat(debe.getCuentaId()).isEqualTo(cajaId);
        assertThat(debe.getDebe()).isEqualByComparingTo("1500.00");
        assertThat(haber.getCuentaId()).isEqualTo(depositosBsId);
        assertThat(haber.getHaber()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("E2E depósito USD: usa cuentas 1.1.05 / 2.1.02")
    void e2e_deposito_usd() {
        CuentaAhorro cuenta = cuentaUsd("AHO-USD-000001");
        Movimiento mov = movimientoConMonto("MOV-2026-000099", "250.0000");

        adapter.registrarDeposito(cuenta, mov);

        AsientoContable asiento = asientoRepo
                .buscarPorReferenciaExterna("MOV-2026-000099").get(0);
        PartidaAsiento debe = asiento.getPartidas().stream()
                .filter(PartidaAsiento::esDeDebe).findFirst().orElseThrow();
        PartidaAsiento haber = asiento.getPartidas().stream()
                .filter(PartidaAsiento::esDeHaber).findFirst().orElseThrow();
        assertThat(debe.getCuentaId()).isEqualTo(bancoUsdId);
        assertThat(haber.getCuentaId()).isEqualTo(depositosUsdId);
    }

    // ─── Retiro ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("E2E retiro Bs: asiento con DEBE 2.1.01 / HABER 1.1.01 (espejo del depósito)")
    void e2e_retiro_bs() {
        CuentaAhorro cuenta = cuentaBs("AHO-2026-000010");
        Movimiento mov = movimientoConMonto("MOV-2026-000200", "300.00");

        adapter.registrarRetiro(cuenta, mov);

        AsientoContable asiento = asientoRepo
                .buscarPorReferenciaExterna("MOV-2026-000200").get(0);
        assertThat(asiento.getOrigen()).isEqualTo(OrigenAsiento.AHORRO_RETIRO);

        PartidaAsiento debe = asiento.getPartidas().stream()
                .filter(PartidaAsiento::esDeDebe).findFirst().orElseThrow();
        PartidaAsiento haber = asiento.getPartidas().stream()
                .filter(PartidaAsiento::esDeHaber).findFirst().orElseThrow();
        assertThat(debe.getCuentaId()).isEqualTo(depositosBsId);
        assertThat(haber.getCuentaId()).isEqualTo(cajaId);
    }

    // ─── Casos negativos ────────────────────────────────────────────────────

    @Test
    @DisplayName("E2E error: cuenta contable inexistente → AsientoContableException")
    void e2e_error_cuenta_inexistente() {
        // Borramos 1.1.01 para forzar el error
        cuentaJpa.deleteById(cajaId);
        em.flush();

        CuentaAhorro cuenta = cuentaBs("AHO-2026-X");
        Movimiento mov = movimientoConMonto("MOV-FAIL", "100.00");

        assertThatThrownBy(() -> adapter.registrarDeposito(cuenta, mov))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("1.1.01");
    }

    @Test
    @DisplayName("E2E: monto con 4 decimales preservados (BD NUMERIC(18,4))")
    void e2e_monto_con_decimales() {
        CuentaAhorro cuenta = cuentaBs("AHO-2026-DEC");
        Movimiento mov = movimientoConMonto("MOV-DEC-001", "999.9999");

        adapter.registrarDeposito(cuenta, mov);

        AsientoContable asiento = asientoRepo
                .buscarPorReferenciaExterna("MOV-DEC-001").get(0);
        assertThat(asiento.totalDebe()).isEqualByComparingTo("999.9999");
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private UUID persistirCuenta(String codigo, String nombre, TipoCuentaContable tipo,
                                  NaturalezaSaldo naturaleza, UUID padre, boolean aceptaMov) {
        CuentaContable c = CuentaContable.crear(codigo, nombre, tipo, naturaleza, padre, aceptaMov, null);
        cuentaRepoAdapter.guardar(c);
        return c.getId();
    }

    private CuentaAhorro cuentaBs(String numero) {
        return CuentaAhorro.builder()
                .id(UUID.randomUUID())
                .numeroCuenta(numero)
                .socioId(UUID.randomUUID())
                .moneda(Moneda.VES)
                .build();
    }

    private CuentaAhorro cuentaUsd(String numero) {
        return CuentaAhorro.builder()
                .id(UUID.randomUUID())
                .numeroCuenta(numero)
                .socioId(UUID.randomUUID())
                .moneda(Moneda.USD)
                .build();
    }

    private Movimiento movimientoConMonto(String nro, String monto) {
        return Movimiento.builder()
                .id(UUID.randomUUID())
                .numeroOperacion(nro)
                .monto(new BigDecimal(monto))
                .build();
    }
}
