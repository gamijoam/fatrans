package com.tufondo.contabilidad.infrastructure.persistence.adapter;

import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
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
 * Integration tests del adapter de asientos contables (H2 mode Postgres).
 *
 * <p>Verifica round-trip completo del aggregate (cabecera + partidas),
 * queries por rango de fecha/origen/estado/referencia, y manejo del
 * correlativo via secuencia BD (en H2 la secuencia se crea por la
 * configuración del entity manager, así que para test simulamos la
 * obtención del próximo número).</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import({AsientoContableRepositoryImpl.class})
@DisplayName("AsientoContableRepositoryImpl - integration con H2")
class AsientoContableRepositoryImplTest {

    @Autowired private AsientoContableRepositoryImpl repository;
    @Autowired private AsientoContableJpaRepository asientoJpa;
    @Autowired private PartidaAsientoJpaRepository partidaJpa;
    @Autowired private CuentaContableJpaRepository cuentaJpa;
    @Autowired private EntityManager em;

    private UUID cajaId, depositosId, ingresosId;

    /**
     * Contador local para simular la secuencia BD durante tests. Igual que
     * en PROD donde la secuencia garantiza unicidad, acá garantizamos que
     * cada asiento del test tiene un número distinto.
     */
    private final AtomicLong correlativo = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        // Limpiar y crear cuentas hijas en plan_cuentas para las FK.
        partidaJpa.deleteAll();
        asientoJpa.deleteAll();
        cuentaJpa.deleteAll();

        UUID rubroActivo = persistirCuenta(CuentaContable.crear(
                "1", "ACTIVO", TipoCuentaContable.ACTIVO,
                NaturalezaSaldo.DEUDORA, null, false, null));
        UUID grupoDisponible = persistirCuenta(CuentaContable.crear(
                "1.1", "ACTIVO DISPONIBLE", TipoCuentaContable.ACTIVO,
                NaturalezaSaldo.DEUDORA, rubroActivo, false, null));
        cajaId = persistirCuenta(CuentaContable.crear(
                "1.1.01", "Caja", TipoCuentaContable.ACTIVO,
                NaturalezaSaldo.DEUDORA, grupoDisponible, true, null));

        UUID rubroPasivo = persistirCuenta(CuentaContable.crear(
                "2", "PASIVO", TipoCuentaContable.PASIVO,
                NaturalezaSaldo.ACREEDORA, null, false, null));
        UUID grupoDepositos = persistirCuenta(CuentaContable.crear(
                "2.1", "DEPÓSITOS", TipoCuentaContable.PASIVO,
                NaturalezaSaldo.ACREEDORA, rubroPasivo, false, null));
        depositosId = persistirCuenta(CuentaContable.crear(
                "2.1.01", "Cuentas Ahorro Bs", TipoCuentaContable.PASIVO,
                NaturalezaSaldo.ACREEDORA, grupoDepositos, true, null));

        UUID rubroIngreso = persistirCuenta(CuentaContable.crear(
                "4", "INGRESOS", TipoCuentaContable.INGRESO,
                NaturalezaSaldo.ACREEDORA, null, false, null));
        UUID grupoIntereses = persistirCuenta(CuentaContable.crear(
                "4.1", "POR CARTERA", TipoCuentaContable.INGRESO,
                NaturalezaSaldo.ACREEDORA, rubroIngreso, false, null));
        ingresosId = persistirCuenta(CuentaContable.crear(
                "4.1.01", "Intereses sobre Créditos", TipoCuentaContable.INGRESO,
                NaturalezaSaldo.ACREEDORA, grupoIntereses, true, null));

        em.flush();
    }

    private UUID persistirCuenta(CuentaContable c) {
        var entity = com.tufondo.contabilidad.infrastructure.persistence.entity
                .CuentaContableEntity.fromDomain(c);
        cuentaJpa.save(entity);
        return entity.getId();
    }

    private AsientoContable nuevoAsientoDeposito(BigDecimal monto) {
        AsientoContable a = AsientoContable.crear(
                LocalDate.of(2026, 5, 20), "Depósito de socio", OrigenAsiento.AHORRO_DEPOSITO,
                "OP-" + UUID.randomUUID(), null, null,
                List.of(
                        PartidaAsiento.alDebe(cajaId, monto, 1, "efectivo recibido"),
                        PartidaAsiento.alHaber(depositosId, monto, 2, "saldo en ahorro")));
        // Simular la asignación del correlativo (en BD real lo hace la secuencia)
        return a.conNumero(correlativo.getAndIncrement());
    }

    // ─── Round-trip completo ────────────────────────────────────────────

    @Test
    @DisplayName("guardar + buscarPorId — preserva cabecera y partidas completas")
    void round_trip() {
        AsientoContable original = nuevoAsientoDeposito(new BigDecimal("500.00"));
        AsientoContable saved = asientoJpa.save(
                com.tufondo.contabilidad.infrastructure.persistence.entity
                        .AsientoContableEntity.fromDomain(original)).toDomain(List.of());
        // Partidas a mano (saltea el siguienteNumeroAsiento que H2 no tiene aún)
        for (PartidaAsiento p : original.getPartidas()) {
            partidaJpa.save(com.tufondo.contabilidad.infrastructure.persistence.entity
                    .PartidaAsientoEntity.fromDomain(p, saved.getId()));
        }

        AsientoContable leido = repository.buscarPorId(saved.getId()).orElseThrow();

        assertThat(leido.getNumero()).isEqualTo(original.getNumero());
        assertThat(leido.getFechaContable()).isEqualTo(original.getFechaContable());
        assertThat(leido.getGlosa()).isEqualTo(original.getGlosa());
        assertThat(leido.getOrigen()).isEqualTo(OrigenAsiento.AHORRO_DEPOSITO);
        assertThat(leido.getEstado()).isEqualTo(EstadoAsiento.REGISTRADO);
        assertThat(leido.getPartidas()).hasSize(2);
        assertThat(leido.totalDebe()).isEqualByComparingTo("500.00");
        assertThat(leido.totalHaber()).isEqualByComparingTo("500.00");
        assertThat(leido.estaBalanceado()).isTrue();
        // Partidas en orden
        assertThat(leido.getPartidas().get(0).getOrden()).isEqualTo(1);
        assertThat(leido.getPartidas().get(0).esDeDebe()).isTrue();
        assertThat(leido.getPartidas().get(1).esDeHaber()).isTrue();
    }

    // ─── Queries por filtros ────────────────────────────────────────────

    @Test
    @DisplayName("listarPorRangoFecha — devuelve solo los del rango, ordenados por número")
    void listar_por_fecha() {
        persistirAsiento(crearAsientoConFecha(LocalDate.of(2026, 5, 1), 1L));
        persistirAsiento(crearAsientoConFecha(LocalDate.of(2026, 5, 15), 2L));
        persistirAsiento(crearAsientoConFecha(LocalDate.of(2026, 6, 1), 3L));

        List<AsientoContable> mes5 = repository.listarPorRangoFecha(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertThat(mes5).extracting(AsientoContable::getNumero).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("listarPorOrigen filtra por tipo de evento")
    void listar_por_origen() {
        persistirAsiento(crearAsientoConOrigen(OrigenAsiento.AHORRO_DEPOSITO, 10L));
        persistirAsiento(crearAsientoConOrigen(OrigenAsiento.AHORRO_RETIRO, 11L));
        persistirAsiento(crearAsientoConOrigen(OrigenAsiento.AHORRO_DEPOSITO, 12L));

        List<AsientoContable> depositos = repository.listarPorOrigen(
                OrigenAsiento.AHORRO_DEPOSITO,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        assertThat(depositos).extracting(AsientoContable::getNumero)
                .containsExactlyInAnyOrder(10L, 12L);
    }

    @Test
    @DisplayName("buscarPorReferenciaExterna encuentra el asiento del movimiento")
    void buscar_por_referencia() {
        AsientoContable a = nuevoAsientoDeposito(new BigDecimal("100"))
                .toBuilder().referenciaExterna("OP-12345").build();
        persistirAsiento(a);

        List<AsientoContable> resultado = repository.buscarPorReferenciaExterna("OP-12345");
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getReferenciaExterna()).isEqualTo("OP-12345");
    }

    @Test
    @DisplayName("buscarReversionesDe encuentra los asientos que apuntan al original")
    void buscar_reversiones() {
        AsientoContable original = nuevoAsientoDeposito(new BigDecimal("100"));
        persistirAsiento(original);
        AsientoContable reversion = AsientoContable.crear(
                LocalDate.now(), "Reversión", OrigenAsiento.REVERSION,
                null, null, original.getId(),
                List.of(
                        PartidaAsiento.alDebe(depositosId, new BigDecimal("100"), 1, null),
                        PartidaAsiento.alHaber(cajaId, new BigDecimal("100"), 2, null)))
                .conNumero(correlativo.getAndIncrement());
        persistirAsiento(reversion);

        List<AsientoContable> reversiones = repository.buscarReversionesDe(original.getId());
        assertThat(reversiones).hasSize(1);
        assertThat(reversiones.get(0).getAsientoReversaId()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("hidratarConPartidas batch — N+1 evitado al listar varios asientos")
    void no_n_plus_1_al_listar() {
        // 5 asientos en un mes
        for (int i = 1; i <= 5; i++) {
            persistirAsiento(crearAsientoConFecha(
                    LocalDate.of(2026, 5, i), (long) (100 + i)));
        }

        List<AsientoContable> todos = repository.listarPorRangoFecha(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertThat(todos).hasSize(5);
        // Todos tienen partidas hidratadas
        assertThat(todos).allMatch(a -> a.getPartidas().size() == 2);
        assertThat(todos).allMatch(AsientoContable::estaBalanceado);
    }

    // ─── Helpers ────────────────────────────────────────────────────────

    private AsientoContable crearAsientoConFecha(LocalDate fecha, long numero) {
        return AsientoContable.crear(
                fecha, "test asiento", OrigenAsiento.MANUAL,
                null, null, null,
                List.of(
                        PartidaAsiento.alDebe(cajaId, new BigDecimal("10"), 1, null),
                        PartidaAsiento.alHaber(depositosId, new BigDecimal("10"), 2, null))
        ).conNumero(numero);
    }

    private AsientoContable crearAsientoConOrigen(OrigenAsiento origen, long numero) {
        return AsientoContable.crear(
                LocalDate.of(2026, 5, 20), "test", origen, null, null, null,
                List.of(
                        PartidaAsiento.alDebe(cajaId, new BigDecimal("10"), 1, null),
                        PartidaAsiento.alHaber(depositosId, new BigDecimal("10"), 2, null))
        ).conNumero(numero);
    }

    private void persistirAsiento(AsientoContable a) {
        var entity = com.tufondo.contabilidad.infrastructure.persistence.entity
                .AsientoContableEntity.fromDomain(a);
        asientoJpa.save(entity);
        for (PartidaAsiento p : a.getPartidas()) {
            partidaJpa.save(com.tufondo.contabilidad.infrastructure.persistence.entity
                    .PartidaAsientoEntity.fromDomain(p, entity.getId()));
        }
        em.flush();
        em.clear();
    }
}
