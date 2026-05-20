package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.dto.RegistrarAsientoCommand;
import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsientoContableServiceTest {

    @Mock private AsientoContableRepository asientoRepo;
    @Mock private CuentaContableRepository cuentaRepo;

    @InjectMocks private AsientoContableService service;

    private CuentaContable caja, depositos, totalizadora, inactiva;

    @BeforeEach
    void setUp() {
        // Cuenta operativa válida (hoja, activa)
        caja = CuentaContable.crear("1.1.01", "Caja Principal",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                UUID.randomUUID(), true, null);
        depositos = CuentaContable.crear("2.1.01", "Cuentas de Ahorro Bs",
                TipoCuentaContable.PASIVO, NaturalezaSaldo.ACREEDORA,
                UUID.randomUUID(), true, null);
        // Cuenta totalizadora (no acepta movimientos)
        totalizadora = CuentaContable.crear("1.1", "ACTIVO DISPONIBLE",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                UUID.randomUUID(), false, null);
        // Cuenta inactiva
        inactiva = CuentaContable.crear("1.1.99", "Cuenta retirada",
                TipoCuentaContable.ACTIVO, NaturalezaSaldo.DEUDORA,
                UUID.randomUUID(), true, null).desactivar();
    }

    private RegistrarAsientoCommand.Partida partidaDebe(String codigo, String monto) {
        return new RegistrarAsientoCommand.Partida(codigo, new BigDecimal(monto), null, null);
    }

    private RegistrarAsientoCommand.Partida partidaHaber(String codigo, String monto) {
        return new RegistrarAsientoCommand.Partida(codigo, null, new BigDecimal(monto), null);
    }

    private RegistrarAsientoCommand cmdDeposito(List<RegistrarAsientoCommand.Partida> partidas) {
        return RegistrarAsientoCommand.builder()
                .fechaContable(LocalDate.now())
                .glosa("Depósito de socio")
                .origen(OrigenAsiento.AHORRO_DEPOSITO)
                .referenciaExterna("OP-001")
                .creadoPorUsuarioId(null)
                .partidas(partidas)
                .build();
    }

    // ─── Happy path ─────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar asiento de depósito válido — pasa todas las validaciones y persiste")
    void registrar_deposito_ok() {
        when(cuentaRepo.buscarPorCodigo("1.1.01")).thenReturn(Optional.of(caja));
        when(cuentaRepo.buscarPorCodigo("2.1.01")).thenReturn(Optional.of(depositos));
        when(asientoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        AsientoContable result = service.registrar(cmdDeposito(List.of(
                partidaDebe("1.1.01", "1000.00"),
                partidaHaber("2.1.01", "1000.00")
        )));

        assertThat(result).isNotNull();
        assertThat(result.totalDebe()).isEqualByComparingTo("1000.00");
        assertThat(result.estaBalanceado()).isTrue();
        verify(asientoRepo).guardar(any(AsientoContable.class));
    }

    // ─── Validaciones de cuentas ────────────────────────────────────────

    @Test
    @DisplayName("cuenta inexistente → AsientoContableException sin persistir")
    void cuenta_no_existe() {
        when(cuentaRepo.buscarPorCodigo("1.1.01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(cmdDeposito(List.of(
                partidaDebe("1.1.01", "100"),
                partidaHaber("2.1.01", "100")
        ))))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("no encontrada");
        verify(asientoRepo, never()).guardar(any());
    }

    @Test
    @DisplayName("cuenta totalizadora (acepta_movimientos=false) → rechazado")
    void cuenta_totalizadora_rechazada() {
        when(cuentaRepo.buscarPorCodigo("1.1")).thenReturn(Optional.of(totalizadora));
        when(cuentaRepo.buscarPorCodigo("2.1.01")).thenReturn(Optional.of(depositos));

        assertThatThrownBy(() -> service.registrar(cmdDeposito(List.of(
                partidaDebe("1.1", "100"),
                partidaHaber("2.1.01", "100")
        ))))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("totalizadora");
        verify(asientoRepo, never()).guardar(any());
    }

    @Test
    @DisplayName("cuenta inactiva → rechazado")
    void cuenta_inactiva_rechazada() {
        when(cuentaRepo.buscarPorCodigo("1.1.99")).thenReturn(Optional.of(inactiva));
        when(cuentaRepo.buscarPorCodigo("2.1.01")).thenReturn(Optional.of(depositos));

        assertThatThrownBy(() -> service.registrar(cmdDeposito(List.of(
                partidaDebe("1.1.99", "100"),
                partidaHaber("2.1.01", "100")
        ))))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("inactiva");
        verify(asientoRepo, never()).guardar(any());
    }

    // ─── Validaciones de balance (delegadas al dominio) ────────────────

    @Test
    @DisplayName("asiento desbalanceado → propagado como AsientoContableException")
    void desbalanceado_rechazado() {
        when(cuentaRepo.buscarPorCodigo("1.1.01")).thenReturn(Optional.of(caja));
        when(cuentaRepo.buscarPorCodigo("2.1.01")).thenReturn(Optional.of(depositos));

        assertThatThrownBy(() -> service.registrar(cmdDeposito(List.of(
                partidaDebe("1.1.01", "100"),
                partidaHaber("2.1.01", "99.99")  // 1 centavo diferencia
        ))))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("desbalanceado");
        verify(asientoRepo, never()).guardar(any());
    }

    @Test
    @DisplayName("partida con DEBE y HABER simultáneos → rechazado por service")
    void debe_y_haber_simultaneos_rechazado_por_service() {
        when(cuentaRepo.buscarPorCodigo("1.1.01")).thenReturn(Optional.of(caja));
        when(cuentaRepo.buscarPorCodigo("2.1.01")).thenReturn(Optional.of(depositos));

        var partidaInvalida = new RegistrarAsientoCommand.Partida(
                "1.1.01", new BigDecimal("100"), new BigDecimal("100"), null);

        assertThatThrownBy(() -> service.registrar(cmdDeposito(List.of(
                partidaInvalida,
                partidaHaber("2.1.01", "100")
        ))))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("DEBE y HABER simultáneamente");
    }

    @Test
    @DisplayName("partida sin DEBE ni HABER → rechazado")
    void sin_debe_ni_haber_rechazado() {
        when(cuentaRepo.buscarPorCodigo("1.1.01")).thenReturn(Optional.of(caja));
        when(cuentaRepo.buscarPorCodigo("2.1.01")).thenReturn(Optional.of(depositos));

        var partidaInvalida = new RegistrarAsientoCommand.Partida(
                "1.1.01", BigDecimal.ZERO, BigDecimal.ZERO, null);

        assertThatThrownBy(() -> service.registrar(cmdDeposito(List.of(
                partidaInvalida,
                partidaHaber("2.1.01", "100")
        ))))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("DEBE o HABER positivo");
    }

    // ─── Anular ────────────────────────────────────────────────────────

    @Test
    @DisplayName("anular asiento existente — marca estado ANULADO + persiste")
    void anular_ok() {
        AsientoContable original = AsientoContable.crear(
                LocalDate.now(), "test", OrigenAsiento.MANUAL, null, null, null,
                List.of(
                        com.tufondo.contabilidad.domain.model.PartidaAsiento
                                .alDebe(caja.getId(), new BigDecimal("10"), 1, null),
                        com.tufondo.contabilidad.domain.model.PartidaAsiento
                                .alHaber(depositos.getId(), new BigDecimal("10"), 2, null)
                ));
        when(asientoRepo.buscarPorId(original.getId())).thenReturn(Optional.of(original));
        when(asientoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        AsientoContable result = service.anular(original.getId(), "error en monto");

        assertThat(result.getEstado())
                .isEqualTo(com.tufondo.contabilidad.domain.model.enums.EstadoAsiento.ANULADO);
        assertThat(result.getMotivoAnulacion()).isEqualTo("error en monto");
    }

    @Test
    @DisplayName("anular asiento inexistente → AsientoContableException")
    void anular_no_existe() {
        UUID id = UUID.randomUUID();
        when(asientoRepo.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.anular(id, "motivo"))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("command sin partidas → rechazado")
    void command_vacio() {
        assertThatThrownBy(() -> service.registrar(cmdDeposito(List.of())))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("vacía");
    }
}
