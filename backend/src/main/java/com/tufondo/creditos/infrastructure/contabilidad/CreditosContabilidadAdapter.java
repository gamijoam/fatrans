package com.tufondo.creditos.infrastructure.contabilidad;

import com.tufondo.contabilidad.application.dto.RegistrarAsientoCommand;
import com.tufondo.contabilidad.application.usecase.AsientoContableService;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.creditos.application.port.output.CreditosContabilidadPort;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter que materializa cada operación de Créditos como un asiento contable
 * de partida doble. Implementación de {@link CreditosContabilidadPort}.
 *
 * <p>Vive en {@code infrastructure/contabilidad} porque conoce los códigos
 * VEN-NIF concretos del plan de cuentas — esa configuración no debe
 * contaminar el dominio de Créditos.</p>
 *
 * <h2>Códigos VEN-NIF usados (V21)</h2>
 *
 * <table>
 *   <tr><th>Cuenta</th><th>Naturaleza</th><th>Uso</th></tr>
 *   <tr><td>1.3.01 Créditos Personales por Cobrar</td><td>ACTIVO (deudora)</td><td>Cartera vigente</td></tr>
 *   <tr><td>1.1.03 Bancos Cta Corriente Bs</td><td>ACTIVO (deudora)</td><td>Plata real del fondo</td></tr>
 *   <tr><td>4.1.01 Intereses sobre Créditos</td><td>INGRESO (acreedora)</td><td>Ingreso normal de la cartera</td></tr>
 *   <tr><td>4.1.02 Comisiones por Otorgamiento</td><td>INGRESO (acreedora)</td><td>Comisión apertura crédito</td></tr>
 *   <tr><td>4.1.03 Intereses Moratorios</td><td>INGRESO (acreedora)</td><td>Mora cobrada (diferenciada)</td></tr>
 * </table>
 *
 * <h2>Asientos generados</h2>
 *
 * <h3>Desembolso</h3>
 * <pre>
 * DEBE  1.3.01  [monto bruto = monto solicitado]
 * HABER 1.1.03  [monto neto = bruto - comisión]
 * HABER 4.1.02  [comisión apertura, omitido si 0]
 * </pre>
 *
 * <h3>Pago de cuota</h3>
 * <pre>
 * DEBE  1.1.03  [monto total cobrado]
 * HABER 1.3.01  [capital]
 * HABER 4.1.01  [intereses normales]
 * HABER 4.1.03  [intereses moratorios, omitido si 0]
 * </pre>
 *
 * <p>Razón regulatoria de los mappings: ver
 * {@code docs/modulos/contabilidad/_decisiones-contables.md} D-003 y D-004.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CreditosContabilidadAdapter implements CreditosContabilidadPort {

    // ─── Códigos del plan de cuentas (V21) ─────────────────────────────────
    static final String CUENTA_CARTERA          = "1.3.01";
    static final String CUENTA_BANCO_BS         = "1.1.03";
    static final String CUENTA_INTERESES        = "4.1.01";
    static final String CUENTA_COMISION_APERTURA = "4.1.02";
    static final String CUENTA_INTERESES_MORA   = "4.1.03";

    private final AsientoContableService asientoContableService;

    @Override
    public void registrarDesembolso(SolicitudCredito solicitud,
                                     BigDecimal montoNeto,
                                     BigDecimal comisionApertura) {
        BigDecimal montoBruto = solicitud.getMontoSolicitado();
        BigDecimal comision   = comisionApertura == null ? BigDecimal.ZERO : comisionApertura;

        // Validación temprana: el bruto debe = neto + comisión, sino el asiento
        // no cuadra y AsientoContableService lo rechazará después con un mensaje
        // genérico. Detectamos acá para mensaje claro de qué está mal en Créditos.
        BigDecimal recomposicion = montoNeto.add(comision);
        if (montoBruto.compareTo(recomposicion) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Asiento desbalanceado en desembolso %s: bruto=%s ≠ neto(%s)+comisión(%s)",
                    solicitud.getNumeroSolicitud(), montoBruto, montoNeto, comision));
        }

        List<RegistrarAsientoCommand.Partida> partidas = new ArrayList<>();
        // DEBE: sube la cartera por el monto bruto (lo que el socio debe al fondo)
        partidas.add(RegistrarAsientoCommand.Partida.builder()
                .codigoCuenta(CUENTA_CARTERA)
                .debe(montoBruto)
                .haber(null)
                .glosa("Cartera por cobrar — desembolso " + solicitud.getNumeroSolicitud())
                .build());
        // HABER: sale plata del banco del fondo al banco externo del socio
        partidas.add(RegistrarAsientoCommand.Partida.builder()
                .codigoCuenta(CUENTA_BANCO_BS)
                .debe(null)
                .haber(montoNeto)
                .glosa("Transferencia al socio — " + nullableTrim(solicitud.getCuentaDestino(), 60))
                .build());
        // HABER (solo si hay comisión): ingreso por comisión apertura
        if (comision.signum() > 0) {
            partidas.add(RegistrarAsientoCommand.Partida.builder()
                    .codigoCuenta(CUENTA_COMISION_APERTURA)
                    .debe(null)
                    .haber(comision)
                    .glosa("Comisión por otorgamiento de crédito")
                    .build());
        }

        RegistrarAsientoCommand cmd = RegistrarAsientoCommand.builder()
                .fechaContable(LocalDate.now())
                .glosa(String.format("Desembolso crédito %s — bruto %s",
                        solicitud.getNumeroSolicitud(), montoBruto.toPlainString()))
                .origen(OrigenAsiento.CREDITO_DESEMBOLSO)
                .referenciaExterna(solicitud.getNumeroSolicitud())
                .creadoPorUsuarioId(null)
                .asientoReversaId(null)
                .partidas(partidas)
                .build();

        asientoContableService.registrar(cmd);
        log.debug("Asiento de desembolso generado: solicitud={} bruto={} neto={} comisión={}",
                solicitud.getNumeroSolicitud(), montoBruto, montoNeto, comision);
    }

    @Override
    public void registrarPagoCuota(SolicitudCredito solicitud,
                                    Amortizacion cuota,
                                    BigDecimal montoCobrado,
                                    String referenciaPago) {
        BigDecimal capital   = cuota.getCapital() == null ? BigDecimal.ZERO : cuota.getCapital();
        BigDecimal interes   = cuota.getInteres() == null ? BigDecimal.ZERO : cuota.getInteres();
        BigDecimal mora      = cuota.getInteresMora() == null ? BigDecimal.ZERO : cuota.getInteresMora();

        // Validación: capital + interés + mora debe = montoCobrado.
        // Si difiere, indica que la cuota tiene seguros o comisiones intra-cuota
        // no contemplados todavía (campos existen en Amortizacion pero estamos
        // ignorándolos por D-004 — están en 0 en flujos actuales).
        BigDecimal sumaConocida = capital.add(interes).add(mora);
        if (sumaConocida.compareTo(montoCobrado) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Asiento desbalanceado en pago cuota %s (crédito %s): cobrado=%s ≠ capital(%s)+interés(%s)+mora(%s). " +
                            "Si la cuota incluye seguros/comisiones intra-cuota, hay que extender el adapter " +
                            "(ver _pendientes-criticos.md sección 'Desglose completo').",
                    cuota.getNumeroCuota(), solicitud.getNumeroSolicitud(),
                    montoCobrado, capital, interes, mora));
        }

        List<RegistrarAsientoCommand.Partida> partidas = new ArrayList<>();
        // DEBE: entra plata al banco del fondo
        partidas.add(RegistrarAsientoCommand.Partida.builder()
                .codigoCuenta(CUENTA_BANCO_BS)
                .debe(montoCobrado)
                .haber(null)
                .glosa("Cobro cuota " + cuota.getNumeroCuota() + " — ref " + safeRef(referenciaPago))
                .build());
        // HABER: baja la cartera por el capital amortizado
        if (capital.signum() > 0) {
            partidas.add(RegistrarAsientoCommand.Partida.builder()
                    .codigoCuenta(CUENTA_CARTERA)
                    .debe(null)
                    .haber(capital)
                    .glosa("Amortización capital cuota " + cuota.getNumeroCuota())
                    .build());
        }
        // HABER: ingreso por intereses normales
        if (interes.signum() > 0) {
            partidas.add(RegistrarAsientoCommand.Partida.builder()
                    .codigoCuenta(CUENTA_INTERESES)
                    .debe(null)
                    .haber(interes)
                    .glosa("Intereses cuota " + cuota.getNumeroCuota())
                    .build());
        }
        // HABER: ingreso por mora (SOLO si hay)
        if (mora.signum() > 0) {
            partidas.add(RegistrarAsientoCommand.Partida.builder()
                    .codigoCuenta(CUENTA_INTERESES_MORA)
                    .debe(null)
                    .haber(mora)
                    .glosa("Intereses moratorios cuota " + cuota.getNumeroCuota())
                    .build());
        }

        RegistrarAsientoCommand cmd = RegistrarAsientoCommand.builder()
                .fechaContable(LocalDate.now())
                .glosa(String.format("Pago cuota %d crédito %s — %s",
                        cuota.getNumeroCuota(), solicitud.getNumeroSolicitud(),
                        montoCobrado.toPlainString()))
                .origen(OrigenAsiento.CREDITO_COBRO)
                .referenciaExterna(referenciaPago != null && !referenciaPago.isBlank()
                        ? referenciaPago
                        : "CUOTA-" + cuota.getId())
                .creadoPorUsuarioId(null)
                .asientoReversaId(null)
                .partidas(partidas)
                .build();

        asientoContableService.registrar(cmd);
        log.debug("Asiento de pago cuota generado: cuota={} crédito={} capital={} interés={} mora={}",
                cuota.getNumeroCuota(), solicitud.getNumeroSolicitud(), capital, interes, mora);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    /** Trunca strings para no exceder el límite de glosa de la BD. */
    private static String nullableTrim(String s, int max) {
        if (s == null) return "(sin cuenta destino)";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private static String safeRef(String r) {
        return r == null || r.isBlank() ? "(sin referencia)" : r;
    }
}
