package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
import com.tufondo.contabilidad.application.dto.BalanceGeneralFilter;
import com.tufondo.contabilidad.application.dto.BalanceGeneralResponse;
import com.tufondo.contabilidad.application.dto.EstadoResultadosFilter;
import com.tufondo.contabilidad.application.dto.EstadoResultadosResponse;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.SaldoCuenta;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import com.tufondo.contabilidad.domain.repository.AsientoContableRepository;
import com.tufondo.contabilidad.domain.repository.CuentaContableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Genera el Balance General (sub-issue #271).
 *
 * <p>Reporte VEN-NIF de la situación patrimonial a una fecha. Estructura:</p>
 * <pre>
 *  ACTIVO              |  PASIVO
 *    rubros / grupos   |    rubros / grupos
 *                      |  PATRIMONIO
 *                      |    rubros / grupos
 *                      |    Excedente del Ejercicio (calculado)
 *
 *  Σ Activo  ==  Σ Pasivo + Σ Patrimonio + Excedente   ← debe cuadrar
 * </pre>
 *
 * <h2>Decisiones formales (D-008)</h2>
 * <ul>
 *   <li><strong>Roll-up por jerarquía</strong>: rubros = suma de grupos, grupos = suma de hojas.</li>
 *   <li><strong>Cuentas correctoras</strong> (ej. 1.3.99 Provisión, 1.5.99 Depreciación):
 *       saldo acreedor dentro de un rubro deudor. Se muestran restando.</li>
 *   <li><strong>Excedente calculado on-the-fly</strong>: invoca al use case del Estado de
 *       Resultados del año fiscal y suma el excedente al patrimonio. Cuando #272 (Cierre)
 *       se implemente y persista el excedente en {@code 3.3.02}, este cálculo se
 *       reemplaza por lectura del saldo persistido.</li>
 *   <li><strong>Asientos ANULADOS excluidos</strong> (mismo criterio que Libro Mayor).</li>
 *   <li><strong>Poda de ceros</strong>: cuentas con saldo cero NO se muestran salvo {@code incluirCeros=true}.</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GenerarBalanceGeneralUseCase {

    private final AsientoContableRepository asientoRepository;
    private final CuentaContableRepository cuentaRepository;
    private final EntidadProperties entidadProperties;
    private final GenerarEstadoResultadosUseCase estadoResultadosUseCase;

    @Transactional(readOnly = true)
    public BalanceGeneralResponse ejecutar(BalanceGeneralFilter filter, UUID solicitanteId) {
        log.info("Generando Balance General: fechaCorte={} inicioEjercicio={} incluyeCeros={} solicitante={}",
                filter.fechaCorte(), filter.inicioEjercicioResuelto(),
                filter.incluirCeros(), solicitanteId);

        List<CuentaContable> plan = cuentaRepository.listarTodas();
        Map<UUID, BigDecimal> saldosHoja = calcularSaldosHojas(plan, filter);

        BalanceGeneralResponse.Seccion activo = construirSeccion(
                plan, TipoCuentaContable.ACTIVO, "ACTIVO", saldosHoja, filter.incluirCeros());
        BalanceGeneralResponse.Seccion pasivo = construirSeccion(
                plan, TipoCuentaContable.PASIVO, "PASIVO", saldosHoja, filter.incluirCeros());
        BalanceGeneralResponse.Seccion patrimonio = construirSeccion(
                plan, TipoCuentaContable.PATRIMONIO, "PATRIMONIO", saldosHoja, filter.incluirCeros());

        // Excedente del ejercicio: invoca al use case del Estado de Resultados
        BigDecimal excedente = calcularExcedenteEjercicio(filter, solicitanteId);
        String etiqueta;
        if (excedente.signum() > 0) etiqueta = "EXCEDENTE";
        else if (excedente.signum() < 0) etiqueta = "DÉFICIT";
        else etiqueta = "—";

        // Validación crítica de cuadre
        BigDecimal totalPasivoPatExc = pasivo.total().add(patrimonio.total()).add(excedente);
        BigDecimal diferencia = activo.total().subtract(totalPasivoPatExc);
        boolean balanceado = diferencia.signum() == 0;

        if (!balanceado) {
            log.error("⚠ Balance General DESBALANCEADO al {}: " +
                    "activo={} pasivo+patrimonio+excedente={} diferencia={}",
                    filter.fechaCorte(), activo.total(), totalPasivoPatExc, diferencia);
        }

        return BalanceGeneralResponse.builder()
                .encabezado(BalanceGeneralResponse.Encabezado.builder()
                        .razonSocial(entidadProperties.getRazonSocial())
                        .rif(entidadProperties.getRif())
                        .fechaCorte(filter.fechaCorte())
                        .inicioEjercicio(filter.inicioEjercicioResuelto())
                        .generadoEn(Instant.now())
                        .generadoPorUsuarioId(solicitanteId)
                        .incluyeCeros(filter.incluirCeros())
                        .build())
                .activo(activo)
                .pasivo(pasivo)
                .patrimonio(patrimonio)
                .excedenteEjercicio(excedente.abs())
                .excedenteEtiqueta(etiqueta)
                .totales(BalanceGeneralResponse.Totales.builder()
                        .totalActivo(activo.total())
                        .totalPasivo(pasivo.total())
                        .totalPatrimonio(patrimonio.total())
                        .excedenteEjercicio(excedente)
                        .totalPasivoMasPatrimonio(totalPasivoPatExc)
                        .diferencia(diferencia)
                        .balanceado(balanceado)
                        .build())
                .build();
    }

    /**
     * Saldo acumulado a la fecha de corte para cada cuenta hoja de tipos
     * 1/2/3 (Activo/Pasivo/Patrimonio). Excluye ANULADOS por delegación
     * al repositorio.
     */
    private Map<UUID, BigDecimal> calcularSaldosHojas(
            List<CuentaContable> plan, BalanceGeneralFilter filter) {
        Map<UUID, BigDecimal> saldos = new HashMap<>();
        for (CuentaContable c : plan) {
            if (!c.isAceptaMovimientos()) continue;
            if (c.getTipo() != TipoCuentaContable.ACTIVO
                    && c.getTipo() != TipoCuentaContable.PASIVO
                    && c.getTipo() != TipoCuentaContable.PATRIMONIO) continue;

            SaldoCuenta saldoCuenta = asientoRepository.calcularSaldoCuentaHasta(
                    c.getId(), filter.fechaCorte());
            // IMPORTANTE: usamos la naturaleza del TIPO de cuenta (no de la cuenta
            // individual) para que el saldo se integre correctamente al rubro padre.
            // Una cuenta correctora (ej. 1.3.99 Provisión Cartera: tipo ACTIVO con
            // naturaleza ACREEDORA) debe contribuir con saldo NEGATIVO al rubro
            // ACTIVO, no positivo. Si usáramos c.getNaturaleza() la provisión
            // sumaría en lugar de restar — D-008.2.
            BigDecimal saldoNeto = c.getTipo().naturalezaNatural().calcularSaldo(
                    saldoCuenta.totalDebe(), saldoCuenta.totalHaber());
            saldos.put(c.getId(), saldoNeto);
        }
        return saldos;
    }

    /**
     * Invoca {@link GenerarEstadoResultadosUseCase} para el rango
     * {@code inicioEjercicio → fechaCorte} y extrae el excedente.
     */
    private BigDecimal calcularExcedenteEjercicio(BalanceGeneralFilter filter, UUID solicitanteId) {
        EstadoResultadosFilter erFilter = new EstadoResultadosFilter(
                filter.inicioEjercicioResuelto(), filter.fechaCorte(), false);
        EstadoResultadosResponse er = estadoResultadosUseCase.ejecutar(erFilter, solicitanteId);
        // er.excedente() es valor absoluto; recuperamos el signo
        BigDecimal valor = er.excedente();
        if ("DÉFICIT".equals(er.excedenteEtiqueta())) valor = valor.negate();
        return valor;
    }

    /**
     * Construye la sección con árbol jerárquico. Reglas:
     * <ul>
     *   <li>Si cuenta es totalizadora: suma recursivamente sus hijas.</li>
     *   <li>Si cuenta es hoja: usa saldo del map.</li>
     *   <li>Cuenta correctora (naturaleza opuesta al tipo): se marca y se considera
     *       en el roll-up tal cual su saldo firmado (que será negativo para el padre).</li>
     *   <li>Poda: si saldo es cero y no incluirCeros, no se incluye.</li>
     * </ul>
     */
    private BalanceGeneralResponse.Seccion construirSeccion(
            List<CuentaContable> plan, TipoCuentaContable tipo, String titulo,
            Map<UUID, BigDecimal> saldosHoja, boolean incluirCeros) {

        List<CuentaContable> cuentas = plan.stream()
                .filter(c -> c.getTipo() == tipo)
                .toList();

        List<BalanceGeneralResponse.NodoCuenta> rubros = cuentas.stream()
                .filter(c -> c.getNivel() == 1)
                .sorted(Comparator.comparing(CuentaContable::getCodigo))
                .map(rubro -> construirNodo(rubro, cuentas, saldosHoja, incluirCeros, tipo))
                .filter(n -> n != null)
                .toList();

        BigDecimal total = rubros.stream()
                .map(BalanceGeneralResponse.NodoCuenta::saldoNeto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BalanceGeneralResponse.Seccion.builder()
                .tipo(tipo).titulo(titulo).rubros(rubros).total(total)
                .build();
    }

    private BalanceGeneralResponse.NodoCuenta construirNodo(
            CuentaContable cuenta, List<CuentaContable> todasDelTipo,
            Map<UUID, BigDecimal> saldosHoja, boolean incluirCeros, TipoCuentaContable tipoRubro) {

        List<CuentaContable> hijas = todasDelTipo.stream()
                .filter(c -> cuenta.getId().equals(c.getCuentaPadreId()))
                .sorted(Comparator.comparing(CuentaContable::getCodigo))
                .toList();

        BigDecimal saldoNeto;
        List<BalanceGeneralResponse.NodoCuenta> hijosDto = new ArrayList<>();

        if (cuenta.isAceptaMovimientos()) {
            saldoNeto = saldosHoja.getOrDefault(cuenta.getId(), BigDecimal.ZERO);
        } else {
            BigDecimal acumulado = BigDecimal.ZERO;
            for (CuentaContable hija : hijas) {
                BalanceGeneralResponse.NodoCuenta nodoHija = construirNodo(
                        hija, todasDelTipo, saldosHoja, incluirCeros, tipoRubro);
                if (nodoHija != null) {
                    hijosDto.add(nodoHija);
                    acumulado = acumulado.add(nodoHija.saldoNeto());
                }
            }
            saldoNeto = acumulado;
        }

        if (!incluirCeros && saldoNeto.signum() == 0) return null;

        // Detectar cuenta correctora: naturaleza opuesta al tipo natural del rubro
        boolean esCorrectora = cuenta.getNaturaleza() != tipoRubro.naturalezaNatural();

        return BalanceGeneralResponse.NodoCuenta.builder()
                .codigo(cuenta.getCodigo())
                .nombre(cuenta.getNombre())
                .nivel(cuenta.getNivel())
                .naturaleza(cuenta.getNaturaleza())
                .esCorrectora(esCorrectora)
                .saldoNeto(saldoNeto)
                .saldoPresentacion(saldoNeto.abs())
                .hijos(hijosDto)
                .build();
    }
}
