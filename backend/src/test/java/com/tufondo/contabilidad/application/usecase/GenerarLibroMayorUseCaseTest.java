package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
import com.tufondo.contabilidad.application.dto.LibroMayorFilter;
import com.tufondo.contabilidad.application.dto.LibroMayorResponse;
import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.SaldoCuenta;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests del {@link GenerarLibroMayorUseCase} (sub-issue #270).
 *
 * <p>Cobertura: filtros por cuenta, saldo inicial real, contracuenta resuelta,
 * cuentas sin movimientos (incluir/no incluir), totalizadoras, etiqueta D/A,
 * saldo acumulado, asientos ANULADOS excluidos por repo.</p>
 */
@ExtendWith(MockitoExtension.class)
class GenerarLibroMayorUseCaseTest {

    @Mock private AsientoContableRepository asientoRepo;
    @Mock private CuentaContableRepository cuentaRepo;

    @InjectMocks private GenerarLibroMayorUseCase useCase;

    private CuentaContable bancos;    // 1.1.03 ACTIVO/DEUDORA  (hoja)
    private CuentaContable ahorros;   // 2.1.01 PASIVO/ACREEDORA (hoja)
    private CuentaContable totalizadora;  // 1.1 (no acepta movimientos)

    private final LocalDate desde = LocalDate.of(2026, 5, 1);
    private final LocalDate hasta = LocalDate.of(2026, 5, 31);

    @BeforeEach
    void setUp() {
        EntidadProperties props = new EntidadProperties();
        props.setRazonSocial("Fatrans Test");
        props.setRif("J-TEST-0");
        useCase = new GenerarLibroMayorUseCase(asientoRepo, cuentaRepo, props);

        // Cuenta hoja activa (DEUDORA)
        bancos = CuentaContable.crear(
                "1.1.03", "Bancos Cta Corriente Bs",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                UUID.randomUUID(), true, null);

        // Cuenta hoja pasiva (ACREEDORA)
        ahorros = CuentaContable.crear(
                "2.1.01", "Cuentas de Ahorro Bs",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA,
                UUID.randomUUID(), true, null);

        // Cuenta totalizadora (no acepta movimientos)
        totalizadora = CuentaContable.crear(
                "1.1", "ACTIVO DISPONIBLE",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                UUID.randomUUID(), false, null);

        // Index global para resolver contracuentas
        lenient().when(cuentaRepo.listarTodas()).thenReturn(List.of(bancos, ahorros, totalizadora));
    }

    // ─── Filtros ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("sin filtro: incluye todas las cuentas hoja (excluye totalizadora)")
    void sin_filtro_solo_hojas() {
        // Simular ningún movimiento — cada cuenta hoja: saldo cero, sin movimientos
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.listarAsientosDeCuentaEnRango(any(), any(), any())).thenReturn(List.of());

        LibroMayorResponse resp = useCase.ejecutar(
                LibroMayorFilter.completo(desde, hasta), UUID.randomUUID());

        // No incluyeSinMovimientos default → cuentas vacías filtradas
        assertThat(resp.cuentas()).isEmpty();
        assertThat(resp.totales().cantidadCuentas()).isZero();
    }

    @Test
    @DisplayName("incluirSinMovimientos=true: muestra hojas aunque no tengan movimientos")
    void incluir_sin_movimientos_muestra_hojas_vacias() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.listarAsientosDeCuentaEnRango(any(), any(), any())).thenReturn(List.of());

        LibroMayorResponse resp = useCase.ejecutar(
                new LibroMayorFilter(desde, hasta, null, true, false), UUID.randomUUID());

        // 2 cuentas hoja sin la totalizadora
        assertThat(resp.cuentas()).hasSize(2);
        assertThat(resp.cuentas()).extracting(LibroMayorResponse.CuentaConMovimientos::codigo)
                .containsExactly("1.1.03", "2.1.01"); // ordenadas por código
    }

    @Test
    @DisplayName("incluirTotalizadoras=true: muestra cuentas no-hoja también")
    void incluir_totalizadoras() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.listarAsientosDeCuentaEnRango(any(), any(), any())).thenReturn(List.of());

        LibroMayorResponse resp = useCase.ejecutar(
                new LibroMayorFilter(desde, hasta, null, true, true), UUID.randomUUID());

        // 3 cuentas (incluida la totalizadora "1.1")
        assertThat(resp.cuentas()).hasSize(3);
        assertThat(resp.cuentas()).extracting(LibroMayorResponse.CuentaConMovimientos::codigo)
                .containsExactly("1.1", "1.1.03", "2.1.01");
    }

    @Test
    @DisplayName("filtro por código de cuenta: solo esa cuenta procesada")
    void filtro_por_codigo() {
        when(cuentaRepo.buscarPorCodigo("1.1.03")).thenReturn(Optional.of(bancos));
        when(asientoRepo.calcularSaldoCuentaHasta(eq(bancos.getId()), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.listarAsientosDeCuentaEnRango(eq(bancos.getId()), any(), any()))
                .thenReturn(List.of());

        LibroMayorResponse resp = useCase.ejecutar(
                LibroMayorFilter.deCuenta(desde, hasta, "1.1.03"), UUID.randomUUID());

        assertThat(resp.cuentas()).hasSize(1);
        assertThat(resp.cuentas().get(0).codigo()).isEqualTo("1.1.03");
    }

    @Test
    @DisplayName("filtro por código inexistente → AsientoContableException")
    void filtro_codigo_inexistente() {
        when(cuentaRepo.buscarPorCodigo("9.9.99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(
                LibroMayorFilter.deCuenta(desde, hasta, "9.9.99"), UUID.randomUUID()))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("9.9.99");
    }

    // ─── Saldo inicial real ────────────────────────────────────────────────

    @Test
    @DisplayName("saldo inicial se calcula con calcularSaldoCuentaHasta(desde-1)")
    void saldo_inicial_pre_periodo() {
        // Banco arranca con saldo inicial 1000 D (totalDebe=1000, totalHaber=0)
        SaldoCuenta sIni = new SaldoCuenta(new BigDecimal("1000.00"), BigDecimal.ZERO);
        when(cuentaRepo.buscarPorCodigo("1.1.03")).thenReturn(Optional.of(bancos));
        when(asientoRepo.calcularSaldoCuentaHasta(bancos.getId(), desde.minusDays(1)))
                .thenReturn(sIni);
        when(asientoRepo.listarAsientosDeCuentaEnRango(bancos.getId(), desde, hasta))
                .thenReturn(List.of());

        LibroMayorResponse resp = useCase.ejecutar(
                LibroMayorFilter.deCuenta(desde, hasta, "1.1.03"), UUID.randomUUID());

        var c = resp.cuentas().get(0);
        assertThat(c.saldoInicialDebe()).isEqualByComparingTo("1000.00");
        assertThat(c.saldoInicialHaber()).isEqualByComparingTo("0");
        assertThat(c.saldoInicialNeto()).isEqualByComparingTo("1000.00");
        assertThat(c.saldoInicialEtiqueta()).isEqualTo("D");
    }

    @Test
    @DisplayName("etiqueta saldo: cero → '—', positivo en deudora → 'D', acreedora → 'A'")
    void etiqueta_saldo_segun_naturaleza() {
        assertThat(GenerarLibroMayorUseCase.etiquetaSaldo(BigDecimal.ZERO, NaturalezaSaldo.DEUDORA))
                .isEqualTo("—");
        assertThat(GenerarLibroMayorUseCase.etiquetaSaldo(new BigDecimal("100"), NaturalezaSaldo.DEUDORA))
                .isEqualTo("D");
        assertThat(GenerarLibroMayorUseCase.etiquetaSaldo(new BigDecimal("100"), NaturalezaSaldo.ACREEDORA))
                .isEqualTo("A");
        // Casos atípicos: saldo opuesto al esperado por su naturaleza
        assertThat(GenerarLibroMayorUseCase.etiquetaSaldo(new BigDecimal("-50"), NaturalezaSaldo.DEUDORA))
                .isEqualTo("A");
        assertThat(GenerarLibroMayorUseCase.etiquetaSaldo(new BigDecimal("-50"), NaturalezaSaldo.ACREEDORA))
                .isEqualTo("D");
    }

    // ─── Movimientos y contracuenta ────────────────────────────────────────

    @Test
    @DisplayName("asiento con 2 partidas: contracuenta = la otra cuenta del asiento")
    void contracuenta_dos_partidas() {
        // Asiento: DEBE bancos 500 / HABER ahorros 500 (depósito típico)
        AsientoContable a = asientoCon2Partidas(1L,
                bancos.getId(), new BigDecimal("500.00"),
                ahorros.getId(), new BigDecimal("500.00"));

        when(cuentaRepo.buscarPorCodigo("1.1.03")).thenReturn(Optional.of(bancos));
        when(asientoRepo.calcularSaldoCuentaHasta(bancos.getId(), desde.minusDays(1)))
                .thenReturn(SaldoCuenta.cero());
        when(asientoRepo.listarAsientosDeCuentaEnRango(bancos.getId(), desde, hasta))
                .thenReturn(List.of(a));

        LibroMayorResponse resp = useCase.ejecutar(
                LibroMayorFilter.deCuenta(desde, hasta, "1.1.03"), UUID.randomUUID());

        var movs = resp.cuentas().get(0).movimientos();
        assertThat(movs).hasSize(1);
        var m = movs.get(0);
        assertThat(m.debe()).isEqualByComparingTo("500.00");
        assertThat(m.haber()).isEqualByComparingTo("0");
        assertThat(m.contracuentaCodigo()).isEqualTo("2.1.01");
        assertThat(m.contracuentaNombre()).isEqualTo("Cuentas de Ahorro Bs");
        assertThat(m.contracuentaResumen()).isNull(); // no es "múltiple"
    }

    @Test
    @DisplayName("saldo acumulado se calcula correctamente movimiento a movimiento")
    void saldo_acumulado_progresivo() {
        // Banco: saldo inicial = 100 D
        // Mov 1: DEBE 50 → saldo 150 D
        // Mov 2: HABER 30 → saldo 120 D
        AsientoContable a1 = asientoCon2Partidas(1L,
                bancos.getId(), new BigDecimal("50.00"),
                ahorros.getId(), new BigDecimal("50.00"));
        AsientoContable a2 = asientoCon2Partidas(2L,
                ahorros.getId(), new BigDecimal("30.00"),  // banco va al HABER
                bancos.getId(), new BigDecimal("30.00"));

        when(cuentaRepo.buscarPorCodigo("1.1.03")).thenReturn(Optional.of(bancos));
        when(asientoRepo.calcularSaldoCuentaHasta(bancos.getId(), desde.minusDays(1)))
                .thenReturn(new SaldoCuenta(new BigDecimal("100.00"), BigDecimal.ZERO));
        when(asientoRepo.listarAsientosDeCuentaEnRango(bancos.getId(), desde, hasta))
                .thenReturn(List.of(a1, a2));

        LibroMayorResponse resp = useCase.ejecutar(
                LibroMayorFilter.deCuenta(desde, hasta, "1.1.03"), UUID.randomUUID());

        var c = resp.cuentas().get(0);
        var movs = c.movimientos();
        assertThat(movs).hasSize(2);
        // Saldo acumulado después de cada movimiento (firmado por naturaleza)
        assertThat(movs.get(0).saldoAcumulado()).isEqualByComparingTo("150.00");
        assertThat(movs.get(1).saldoAcumulado()).isEqualByComparingTo("120.00");
        // Saldo final
        assertThat(c.saldoFinalNeto()).isEqualByComparingTo("120.00");
        assertThat(c.saldoFinalEtiqueta()).isEqualTo("D");
        // Totales del período
        assertThat(c.totalDebePeriodo()).isEqualByComparingTo("50.00");
        assertThat(c.totalHaberPeriodo()).isEqualByComparingTo("30.00");
        assertThat(c.cantidadMovimientos()).isEqualTo(2);
    }

    @Test
    @DisplayName("encabezado completo con razón social, RIF, filtros aplicados")
    void encabezado_refleja_filtros() {
        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.listarAsientosDeCuentaEnRango(any(), any(), any())).thenReturn(List.of());

        UUID solicitante = UUID.randomUUID();
        LibroMayorResponse resp = useCase.ejecutar(
                new LibroMayorFilter(desde, hasta, null, true, false), solicitante);

        assertThat(resp.encabezado().razonSocial()).isEqualTo("Fatrans Test");
        assertThat(resp.encabezado().rif()).isEqualTo("J-TEST-0");
        assertThat(resp.encabezado().desde()).isEqualTo(desde);
        assertThat(resp.encabezado().hasta()).isEqualTo(hasta);
        assertThat(resp.encabezado().generadoPorUsuarioId()).isEqualTo(solicitante);
        assertThat(resp.encabezado().incluyeSinMovimientos()).isTrue();
        assertThat(resp.encabezado().incluyeTotalizadoras()).isFalse();
        assertThat(resp.encabezado().filtroCuenta()).isNull();
    }

    @Test
    @DisplayName("totales generales suman cuentas procesadas")
    void totales_generales_balanceados() {
        AsientoContable a = asientoCon2Partidas(1L,
                bancos.getId(), new BigDecimal("500.00"),
                ahorros.getId(), new BigDecimal("500.00"));

        when(asientoRepo.calcularSaldoCuentaHasta(any(), any())).thenReturn(SaldoCuenta.cero());
        when(asientoRepo.listarAsientosDeCuentaEnRango(eq(bancos.getId()), any(), any()))
                .thenReturn(List.of(a));
        when(asientoRepo.listarAsientosDeCuentaEnRango(eq(ahorros.getId()), any(), any()))
                .thenReturn(List.of(a));

        LibroMayorResponse resp = useCase.ejecutar(
                LibroMayorFilter.completo(desde, hasta), UUID.randomUUID());

        // El asiento aparece en ambas cuentas → totales contabilizan ambas vistas
        assertThat(resp.totales().cantidadCuentas()).isEqualTo(2);
        assertThat(resp.totales().cantidadMovimientos()).isEqualTo(2); // 1 en cada cuenta
        // bancos: DEBE 500 / ahorros: HABER 500
        assertThat(resp.totales().totalDebe()).isEqualByComparingTo("500.00");
        assertThat(resp.totales().totalHaber()).isEqualByComparingTo("500.00");
        assertThat(resp.totales().balanceado()).isTrue();
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private AsientoContable asientoCon2Partidas(
            long numero,
            UUID cuentaDebeId, BigDecimal montoDebe,
            UUID cuentaHaberId, BigDecimal montoHaber) {
        return AsientoContable.reconstruir(
                UUID.randomUUID(), numero,
                LocalDate.of(2026, 5, 10), "Asiento test " + numero,
                OrigenAsiento.MANUAL, "REF-" + numero,
                EstadoAsiento.REGISTRADO, null, null, null,
                List.of(
                        PartidaAsiento.alDebe(cuentaDebeId, montoDebe, 1, null),
                        PartidaAsiento.alHaber(cuentaHaberId, montoHaber, 2, null)
                ),
                null, null, 0L);
    }
}
