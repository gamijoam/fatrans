package com.tufondo.admin.application.usecase;

import com.tufondo.admin.application.dto.DashboardEstadisticasResponse;
import com.tufondo.admin.application.dto.DashboardEstadisticasResponse.ActividadRecienteResponse;
import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.repository.AmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObtenerDashboardEstadisticasUseCase {

    private final SocioRepository socioRepository;
    private final CuentaAhorroRepository cuentaAhorroRepository;
    private final MovimientoRepository movimientoRepository;
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final AmortizacionRepository amortizacionRepository;

    public DashboardEstadisticasResponse ejecutar() {
        log.info("Obteniendo estadísticas del dashboard admin");

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioMes = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime hace30Dias = ahora.minus(30, ChronoUnit.DAYS);

        long totalSocios = socioRepository.count();
        long sociosActivos = socioRepository.countByEstado(EstadoSocio.ACTIVO);
        long sociosInactivos = socioRepository.countByEstado(EstadoSocio.INACTIVO);
        long sociosPendientes = socioRepository.countByEstado(EstadoSocio.PENDIENTE_APROBACION);

        BigDecimal totalAportaciones = cuentaAhorroRepository.sumSaldoActualCuentasActivas();
        if (totalAportaciones == null) totalAportaciones = BigDecimal.ZERO;

        long totalCuentas = cuentaAhorroRepository.count();
        long cuentasActivas = cuentaAhorroRepository.countByEstado(EstadoCuenta.ACTIVA);
        long cuentasSuspendidas = cuentaAhorroRepository.countByEstado(EstadoCuenta.SUSPENDIDA);

        BigDecimal depositosMes = movimientoRepository.sumDepositosMes(inicioMes);
        if (depositosMes == null) depositosMes = BigDecimal.ZERO;

        BigDecimal retirosMes = movimientoRepository.sumRetirosMes(inicioMes);
        if (retirosMes == null) retirosMes = BigDecimal.ZERO;

        long prestamosActivos = solicitudCreditoRepository.countByEstado(EstadoSolicitud.DESEMBOLSADO);
        long solicitudesPendientes = solicitudCreditoRepository.countByEstado(EstadoSolicitud.PENDIENTE);
        long solicitudesAprobadas = solicitudCreditoRepository.countByEstado(EstadoSolicitud.APROBADA);
        long solicitudesRechazadas = solicitudCreditoRepository.countByEstado(EstadoSolicitud.RECHAZADA);

        BigDecimal capitalDesembolsado = solicitudCreditoRepository.sumMontoSolicitadoByEstado(EstadoSolicitud.DESEMBOLSADO);
        if (capitalDesembolsado == null) capitalDesembolsado = BigDecimal.ZERO;

        long cuotasVencidas = amortizacionRepository.countByEstado(EstadoAmortizacion.VENCIDA);
        long cuotasEnMora = amortizacionRepository.countByEstado(EstadoAmortizacion.CURSO_MORA);
        long cuotasPagadas = amortizacionRepository.countByEstado(EstadoAmortizacion.PAGADA);

        BigDecimal interesesMora = amortizacionRepository.sumInteresesMoraPendientes();
        if (interesesMora == null) interesesMora = BigDecimal.ZERO;

        double tasaCumplimiento = totalSocios > 0
            ? (double) sociosActivos / totalSocios : 0.0;

        double tasaMora = prestamosActivos > 0
            ? (double) (cuotasVencidas + cuotasEnMora) / prestamosActivos : 0.0;

        ActividadRecienteResponse actividadReciente = ActividadRecienteResponse.builder()
            .nuevosSociosMes(socioRepository.countByFechaRegistroBetween(inicioMes, ahora))
            .depositosMes(movimientoRepository.countByTipoAndFechaAfter(TipoMovimiento.DEPOSITO, hace30Dias))
            .retirosMes(movimientoRepository.countByTipoAndFechaAfter(TipoMovimiento.RETIRO, hace30Dias))
            .prestamosAprobadosMes(solicitudCreditoRepository.countByEstadoAndCreatedAtAfter(EstadoSolicitud.APROBADA, hace30Dias))
            .prestamosDesembolsadosMes(solicitudCreditoRepository.countByEstadoAndCreatedAtAfter(EstadoSolicitud.DESEMBOLSADO, hace30Dias))
            .montoDepositadoMes(depositosMes)
            .montoRetiradoMes(retirosMes)
            .build();

        return DashboardEstadisticasResponse.builder()
            .totalSocios(totalSocios)
            .sociosActivos(sociosActivos)
            .sociosInactivos(sociosInactivos)
            .sociosPendientes(sociosPendientes)
            .totalAportaciones(totalAportaciones.setScale(2, RoundingMode.HALF_UP))
            .totalCuentasAhorro(totalCuentas)
            .cuentasActivas(cuentasActivas)
            .cuentasSuspendidas(cuentasSuspendidas)
            .depositosMes(depositosMes.setScale(2, RoundingMode.HALF_UP))
            .retirosMes(retirosMes.setScale(2, RoundingMode.HALF_UP))
            .prestamosActivos(prestamosActivos)
            .solicitudesPendientes(solicitudesPendientes)
            .solicitudesAprobadas(solicitudesAprobadas)
            .solicitudesRechazadas(solicitudesRechazadas)
            .capitalDesembolsado(capitalDesembolsado.setScale(2, RoundingMode.HALF_UP))
            .carteraVencida(interesesMora.setScale(2, RoundingMode.HALF_UP))
            .cuotasVencidas(cuotasVencidas)
            .cuotasEnMora(cuotasEnMora)
            .cuotasPagadas(cuotasPagadas)
            .interesesMoraGenerados(interesesMora.setScale(2, RoundingMode.HALF_UP))
            .tasaCumplimiento(Math.round(tasaCumplimiento * 100.0) / 100.0)
            .tasaMora(Math.round(tasaMora * 100.0) / 100.0)
            .actividadReciente(actividadReciente)
            .build();
    }
}