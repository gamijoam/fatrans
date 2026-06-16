package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Genera el Estado de Resultados (sub-issue #271).
 *
 * <p>Muestra ingresos (cuentas tipo 4) y egresos (cuentas tipo 5) del
 * período, con jerarquía Rubro → Grupo → Cuenta hoja. El excedente
 * (o déficit) del período es {@code Σ ingresos − Σ egresos}.</p>
 *
 * <p>El saldo de cada cuenta hoja se calcula como movimientos del período
 * (excluyendo ANULADOS): saldo del período = saldo al {@code hasta} − saldo al {@code desde-1}.
 * Esa resta da los movimientos netos del período sin importar saldos previos.</p>
 *
 * <p>Excluye asientos {@link com.tufondo.contabilidad.domain.model.enums.EstadoAsiento#ANULADO}
 * por delegación a {@code calcularSaldoCuentaHasta} (consistente con D-007 del Libro Mayor).</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GenerarEstadoResultadosUseCase {

    private final AsientoContableRepository asientoRepository;
    private final CuentaContableRepository cuentaRepository;
    private final EntidadProperties entidadProperties;

    @Transactional(readOnly = true)
    public EstadoResultadosResponse ejecutar(EstadoResultadosFilter filter, UUID solicitanteId) {
        log.info("Generando Estado de Resultados: desde={} hasta={} incluyeCeros={} solicitante={}",
                filter.desde(), filter.hasta(), filter.incluirCeros(), solicitanteId);

        List<CuentaContable> plan = cuentaRepository.listarTodas();
        // Saldo de período por cuenta hoja: hasta - (desde-1)
        Map<UUID, BigDecimal> saldosHoja = calcularSaldosPeriodoHojas(plan, filter);

        EstadoResultadosResponse.Seccion ingresos = construirSeccion(
                plan, TipoCuentaContable.INGRESO, "INGRESOS", saldosHoja, filter.incluirCeros());
        EstadoResultadosResponse.Seccion egresos = construirSeccion(
                plan, TipoCuentaContable.EGRESO, "EGRESOS", saldosHoja, filter.incluirCeros());

        BigDecimal excedente = ingresos.total().subtract(egresos.total());
        String etiqueta;
        if (excedente.signum() > 0) etiqueta = "EXCEDENTE";
        else if (excedente.signum() < 0) etiqueta = "DÉFICIT";
        else etiqueta = "—";

        return EstadoResultadosResponse.builder()
                .encabezado(EstadoResultadosResponse.Encabezado.builder()
                        .razonSocial(entidadProperties.getRazonSocial())
                        .rif(entidadProperties.getRif())
                        .desde(filter.desde())
                        .hasta(filter.hasta())
                        .generadoEn(Instant.now())
                        .generadoPorUsuarioId(solicitanteId)
                        .incluyeCeros(filter.incluirCeros())
                        .build())
                .ingresos(ingresos)
                .egresos(egresos)
                .excedente(excedente.abs())
                .excedenteEtiqueta(etiqueta)
                .build();
    }

    /**
     * Calcula movimientos del período para cada cuenta hoja de tipos 4 y 5.
     * Formula: {@code saldo_periodo = saldo_hasta - saldo_(desde-1)}
     * — esto da el efecto neto del período sin importar saldos previos.
     */
    private Map<UUID, BigDecimal> calcularSaldosPeriodoHojas(
            List<CuentaContable> plan, EstadoResultadosFilter filter) {
        Map<UUID, BigDecimal> saldos = new HashMap<>();
        for (CuentaContable c : plan) {
            if (!c.isAceptaMovimientos()) continue;
            if (c.getTipo() != TipoCuentaContable.INGRESO
                    && c.getTipo() != TipoCuentaContable.EGRESO) continue;

            SaldoCuenta hasta = asientoRepository.calcularSaldoCuentaHasta(c.getId(), filter.hasta());
            SaldoCuenta antesDesde = asientoRepository.calcularSaldoCuentaHasta(
                    c.getId(), filter.desde().minusDays(1));

            // Diferencia DEBE - HABER del período, ya firmado por naturaleza
            BigDecimal periodoDebe = hasta.totalDebe().subtract(antesDesde.totalDebe());
            BigDecimal periodoHaber = hasta.totalHaber().subtract(antesDesde.totalHaber());
            BigDecimal saldoPeriodo = c.getNaturaleza().calcularSaldo(periodoDebe, periodoHaber);
            saldos.put(c.getId(), saldoPeriodo);
        }
        return saldos;
    }

    /**
     * Construye la sección con árbol jerárquico de cuentas filtradas por tipo.
     * Hace roll-up de saldos: padres son la suma de las hojas descendientes.
     */
    private EstadoResultadosResponse.Seccion construirSeccion(
            List<CuentaContable> plan, TipoCuentaContable tipo, String titulo,
            Map<UUID, BigDecimal> saldosHoja, boolean incluirCeros) {

        // Filtrar por tipo
        List<CuentaContable> cuentas = plan.stream()
                .filter(c -> c.getTipo() == tipo)
                .toList();

        // Index por id
        Map<UUID, CuentaContable> porId = cuentas.stream()
                .collect(Collectors.toMap(CuentaContable::getId, Function.identity()));

        // Encontrar los rubros (nivel 1) y construir sus árboles
        List<EstadoResultadosResponse.NodoCuenta> rubros = cuentas.stream()
                .filter(c -> c.getNivel() == 1)
                .sorted(Comparator.comparing(CuentaContable::getCodigo))
                .map(rubro -> construirNodo(rubro, cuentas, saldosHoja, incluirCeros))
                .filter(n -> n != null) // null = todo cero y no incluirCeros
                .toList();

        BigDecimal total = rubros.stream()
                .map(EstadoResultadosResponse.NodoCuenta::saldoNeto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EstadoResultadosResponse.Seccion.builder()
                .tipo(tipo).titulo(titulo).rubros(rubros).total(total)
                .build();
    }

    /**
     * Construye recursivamente el nodo de una cuenta, sumando hijas.
     * Devuelve null si saldo=0 y no incluirCeros (poda).
     */
    private EstadoResultadosResponse.NodoCuenta construirNodo(
            CuentaContable cuenta, List<CuentaContable> todasDelTipo,
            Map<UUID, BigDecimal> saldosHoja, boolean incluirCeros) {

        List<CuentaContable> hijas = todasDelTipo.stream()
                .filter(c -> cuenta.getId().equals(c.getCuentaPadreId()))
                .sorted(Comparator.comparing(CuentaContable::getCodigo))
                .toList();

        BigDecimal saldoNeto;
        List<EstadoResultadosResponse.NodoCuenta> hijosDto = new ArrayList<>();

        if (cuenta.isAceptaMovimientos()) {
            // Hoja operativa: usa saldo directo del map
            saldoNeto = saldosHoja.getOrDefault(cuenta.getId(), BigDecimal.ZERO);
        } else {
            // Totalizadora: suma hijas recursivamente
            BigDecimal acumulado = BigDecimal.ZERO;
            for (CuentaContable hija : hijas) {
                EstadoResultadosResponse.NodoCuenta nodoHija = construirNodo(
                        hija, todasDelTipo, saldosHoja, incluirCeros);
                if (nodoHija != null) {
                    hijosDto.add(nodoHija);
                    acumulado = acumulado.add(nodoHija.saldoNeto());
                }
            }
            saldoNeto = acumulado;
        }

        // Poda si saldo cero y no se piden ceros
        if (!incluirCeros && saldoNeto.signum() == 0) return null;

        return EstadoResultadosResponse.NodoCuenta.builder()
                .codigo(cuenta.getCodigo())
                .nombre(cuenta.getNombre())
                .nivel(cuenta.getNivel())
                .naturaleza(cuenta.getNaturaleza())
                .saldoNeto(saldoNeto)
                .saldoPresentacion(saldoNeto.abs())
                .hijos(hijosDto)
                .build();
    }
}
