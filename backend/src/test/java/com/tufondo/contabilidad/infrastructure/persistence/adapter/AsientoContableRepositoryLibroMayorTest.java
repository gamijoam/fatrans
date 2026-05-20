package com.tufondo.contabilidad.infrastructure.persistence.adapter;

import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.SaldoCuenta;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import com.tufondo.contabilidad.infrastructure.persistence.entity.AsientoContableEntity;
import com.tufondo.contabilidad.infrastructure.persistence.entity.PartidaAsientoEntity;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests del adapter — queries nuevas del Libro Mayor (#270).
 *
 * <p>Verifica con BD H2 real (modo Postgres) que las queries nativas
 * calcularSaldoCuentaHasta() y listarAsientosDeCuentaEnRango() devuelven
 * los datos correctos, especialmente:</p>
 * <ul>
 *   <li>SUM de DEBE/HABER excluyendo asientos ANULADOS.</li>
 *   <li>Rango de fechas inclusive en ambos extremos.</li>
 *   <li>Filtrado solo por la cuenta solicitada (no devuelve otras).</li>
 *   <li>Cuenta sin movimientos → SaldoCuenta.cero(), lista vacía.</li>
 * </ul>
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import(AsientoContableRepositoryImpl.class)
@DisplayName("AsientoContableRepositoryImpl - queries del Libro Mayor")
class AsientoContableRepositoryLibroMayorTest {

    @Autowired private AsientoContableRepositoryImpl repo;
    @Autowired private AsientoContableJpaRepository asientoJpa;
    @Autowired private PartidaAsientoJpaRepository partidaJpa;
    @Autowired private CuentaContableJpaRepository cuentaJpa;
    @Autowired private EntityManager em;

    private UUID bancoId, ahorrosId;
    private final AtomicLong correlativo = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        partidaJpa.deleteAll();
        asientoJpa.deleteAll();
        cuentaJpa.deleteAll();

        UUID rubroA = persistirCuenta("1", "ACTIVO", TipoCuentaContable.ACTIVO,
                NaturalezaSaldo.DEUDORA, null, false);
        UUID grupoA = persistirCuenta("1.1", "DISPONIBLE", TipoCuentaContable.ACTIVO,
                NaturalezaSaldo.DEUDORA, rubroA, false);
        bancoId = persistirCuenta("1.1.03", "Bancos Bs", TipoCuentaContable.ACTIVO,
                NaturalezaSaldo.DEUDORA, grupoA, true);

        UUID rubroP = persistirCuenta("2", "PASIVO", TipoCuentaContable.PASIVO,
                NaturalezaSaldo.ACREEDORA, null, false);
        UUID grupoP = persistirCuenta("2.1", "DEPÓSITOS", TipoCuentaContable.PASIVO,
                NaturalezaSaldo.ACREEDORA, rubroP, false);
        ahorrosId = persistirCuenta("2.1.01", "Ahorros Bs", TipoCuentaContable.PASIVO,
                NaturalezaSaldo.ACREEDORA, grupoP, true);
    }

    // ─── calcularSaldoCuentaHasta ──────────────────────────────────────────

    @Test
    @DisplayName("cuenta sin movimientos → saldo cero")
    void saldo_cuenta_sin_movimientos() {
        SaldoCuenta s = repo.calcularSaldoCuentaHasta(bancoId, LocalDate.now());
        assertThat(s.totalDebe()).isEqualByComparingTo("0");
        assertThat(s.totalHaber()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("SUM acumulado hasta fecha de corte")
    void saldo_suma_hasta_corte() {
        // 3 asientos: 2 dentro del corte, 1 después
        persistirAsiento(LocalDate.of(2026, 5, 1), bancoId, ahorrosId, "100.00", EstadoAsiento.REGISTRADO);
        persistirAsiento(LocalDate.of(2026, 5, 10), bancoId, ahorrosId, "200.00", EstadoAsiento.REGISTRADO);
        persistirAsiento(LocalDate.of(2026, 5, 20), bancoId, ahorrosId, "50.00", EstadoAsiento.REGISTRADO);

        // Corte al 2026-05-15 → debe sumar solo los dos primeros (100 + 200)
        SaldoCuenta s = repo.calcularSaldoCuentaHasta(bancoId, LocalDate.of(2026, 5, 15));
        assertThat(s.totalDebe()).isEqualByComparingTo("300.00");
        assertThat(s.totalHaber()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("excluye asientos ANULADOS")
    void saldo_excluye_anulados() {
        persistirAsiento(LocalDate.of(2026, 5, 1), bancoId, ahorrosId, "100.00", EstadoAsiento.REGISTRADO);
        persistirAsiento(LocalDate.of(2026, 5, 2), bancoId, ahorrosId, "500.00", EstadoAsiento.ANULADO);  // no debe contar
        persistirAsiento(LocalDate.of(2026, 5, 3), bancoId, ahorrosId, "200.00", EstadoAsiento.REGISTRADO);

        SaldoCuenta s = repo.calcularSaldoCuentaHasta(bancoId, LocalDate.of(2026, 5, 31));
        assertThat(s.totalDebe()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("la cuenta puede aparecer en HABER también (suma a totalHaber)")
    void saldo_acumula_haber_si_cuenta_en_lado_acreedor() {
        // Asiento 1: banco al DEBE 100
        persistirAsiento(LocalDate.of(2026, 5, 1), bancoId, ahorrosId, "100.00", EstadoAsiento.REGISTRADO);
        // Asiento 2: banco al HABER 30 (retiro)
        persistirAsiento(LocalDate.of(2026, 5, 5), ahorrosId, bancoId, "30.00", EstadoAsiento.REGISTRADO);

        SaldoCuenta s = repo.calcularSaldoCuentaHasta(bancoId, LocalDate.of(2026, 5, 31));
        assertThat(s.totalDebe()).isEqualByComparingTo("100.00");
        assertThat(s.totalHaber()).isEqualByComparingTo("30.00");
        // Saldo neto deudor = 100 - 30 = 70
        assertThat(s.saldoNeto(NaturalezaSaldo.DEUDORA)).isEqualByComparingTo("70.00");
    }

    // ─── listarAsientosDeCuentaEnRango ─────────────────────────────────────

    @Test
    @DisplayName("rango sin movimientos → lista vacía")
    void rango_vacio() {
        List<AsientoContable> r = repo.listarAsientosDeCuentaEnRango(
                bancoId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertThat(r).isEmpty();
    }

    @Test
    @DisplayName("solo asientos REGISTRADOS dentro del rango (excluye ANULADOS y fuera de rango)")
    void rango_filtra_correctamente() {
        persistirAsiento(LocalDate.of(2026, 4, 30), bancoId, ahorrosId, "10.00", EstadoAsiento.REGISTRADO); // antes
        persistirAsiento(LocalDate.of(2026, 5, 5), bancoId, ahorrosId, "20.00", EstadoAsiento.REGISTRADO);  // ok
        persistirAsiento(LocalDate.of(2026, 5, 10), bancoId, ahorrosId, "30.00", EstadoAsiento.ANULADO);    // anulado
        persistirAsiento(LocalDate.of(2026, 5, 20), bancoId, ahorrosId, "40.00", EstadoAsiento.REGISTRADO); // ok
        persistirAsiento(LocalDate.of(2026, 6, 1), bancoId, ahorrosId, "50.00", EstadoAsiento.REGISTRADO);  // después

        List<AsientoContable> r = repo.listarAsientosDeCuentaEnRango(
                bancoId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        // Solo los 2 REGISTRADOS dentro del rango
        assertThat(r).hasSize(2);
        // Ordenados por fecha asc
        assertThat(r.get(0).getFechaContable()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(r.get(1).getFechaContable()).isEqualTo(LocalDate.of(2026, 5, 20));
        // Las partidas están hidratadas (no N+1)
        assertThat(r.get(0).getPartidas()).hasSize(2);
        assertThat(r.get(1).getPartidas()).hasSize(2);
    }

    @Test
    @DisplayName("solo asientos donde la cuenta tiene partida — no devuelve asientos de otras cuentas")
    void filtro_solo_cuenta_solicitada() {
        // Asiento de bancos
        persistirAsiento(LocalDate.of(2026, 5, 1), bancoId, ahorrosId, "100.00", EstadoAsiento.REGISTRADO);
        // Asiento que NO toca bancos (solo entre ahorros y otra)
        UUID otraCuenta = persistirCuenta("2.1.02", "USD",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA,
                cuentaJpa.findAll().stream()
                        .filter(c -> c.getCodigo().equals("2.1"))
                        .findFirst().get().getId(),
                true);
        persistirAsiento(LocalDate.of(2026, 5, 5), ahorrosId, otraCuenta, "50.00", EstadoAsiento.REGISTRADO);

        // Solo debe devolver el primero (el que toca bancos)
        List<AsientoContable> r = repo.listarAsientosDeCuentaEnRango(
                bancoId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertThat(r).hasSize(1);
        assertThat(r.get(0).getFechaContable()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private UUID persistirCuenta(String codigo, String nombre, TipoCuentaContable tipo,
                                  NaturalezaSaldo naturaleza, UUID padre, boolean aceptaMov) {
        CuentaContable c = CuentaContable.crear(codigo, nombre, tipo, naturaleza, padre, aceptaMov, null);
        cuentaJpa.save(com.tufondo.contabilidad.infrastructure.persistence.entity
                .CuentaContableEntity.fromDomain(c));
        return c.getId();
    }

    /**
     * Persiste un asiento mínimo con 2 partidas (DEBE / HABER del mismo monto).
     * @param estado REGISTRADO o ANULADO
     */
    private void persistirAsiento(LocalDate fecha, UUID cuentaDebeId, UUID cuentaHaberId,
                                   String monto, EstadoAsiento estado) {
        BigDecimal m = new BigDecimal(monto);
        AsientoContable a = AsientoContable.reconstruir(
                UUID.randomUUID(), correlativo.getAndIncrement(),
                fecha, "test", OrigenAsiento.MANUAL, "REF-" + monto,
                estado, null,
                estado == EstadoAsiento.ANULADO ? "test" : null, null,
                List.of(
                        PartidaAsiento.alDebe(cuentaDebeId, m, 1, null),
                        PartidaAsiento.alHaber(cuentaHaberId, m, 2, null)),
                null, null, 0L);
        AsientoContableEntity savedCab = asientoJpa.save(AsientoContableEntity.fromDomain(a));
        List<PartidaAsientoEntity> partidas = a.getPartidas().stream()
                .map(p -> PartidaAsientoEntity.fromDomain(p, savedCab.getId()))
                .toList();
        partidaJpa.saveAll(partidas);
        em.flush();
    }
}
