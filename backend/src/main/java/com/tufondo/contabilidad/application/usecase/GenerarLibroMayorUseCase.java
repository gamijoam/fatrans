package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
import com.tufondo.contabilidad.application.dto.LibroMayorFilter;
import com.tufondo.contabilidad.application.dto.LibroMayorResponse;
import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.SaldoCuenta;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Genera el Libro Mayor (sub-issue #270): agrupa los movimientos contables
 * por cuenta, calcula saldo inicial al comienzo del período, lista todos
 * los movimientos del rango con su contracuenta resuelta, y calcula saldo
 * final.
 *
 * <p>Decisiones formalizadas en {@code D-007} (ver
 * {@code docs/modulos/contabilidad/_decisiones-contables.md}):</p>
 * <ul>
 *   <li>Saldo inicial real (sumando todo lo previo a {@code desde-1}), no asume cero.</li>
 *   <li>Solo cuentas hoja por default (las que aceptan movimientos).</li>
 *   <li>Cuentas sin movimientos NO se incluyen por default.</li>
 *   <li>Asientos ANULADOS se excluyen del Mayor (es saldo vigente, no historial).</li>
 *   <li>Saldos en formato absoluto + tag (D/A), no firmado.</li>
 *   <li>Contracuenta: la cuenta del lado opuesto con mayor monto (o "múltiple").</li>
 * </ul>
 *
 * <h2>Performance</h2>
 * <ul>
 *   <li>1 query: plan de cuentas completo.</li>
 *   <li>Por cuenta: 1 query saldo inicial (SUM) + 1 query asientos del período.</li>
 *   <li>Filtro de 1 cuenta: solo 2 queries totales.</li>
 *   <li>Sin filtro: ~2 × N queries donde N es la cantidad de cuentas hoja del plan
 *       (típicamente 50-100). Si crece mucho, cachear saldos finales en una tabla
 *       de "estado de cuenta" pre-calculado por el cierre mensual (#272).</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GenerarLibroMayorUseCase {

    private final AsientoContableRepository asientoRepository;
    private final CuentaContableRepository cuentaRepository;
    private final EntidadProperties entidadProperties;

    @Transactional(readOnly = true)
    public LibroMayorResponse ejecutar(LibroMayorFilter filter, UUID solicitanteId) {
        log.info("Generando Libro Mayor: desde={} hasta={} cuenta={} solicitante={}",
                filter.desde(), filter.hasta(), filter.codigoCuenta(), solicitanteId);

        // 1. Determinar el set de cuentas a procesar
        List<CuentaContable> cuentasObjetivo = seleccionarCuentas(filter);

        // 2. Index UUID → CuentaContable (para resolver contracuentas)
        Map<UUID, CuentaContable> indexCuentas = cuentaRepository.listarTodas().stream()
                .collect(Collectors.toMap(CuentaContable::getId, Function.identity()));

        // 3. Procesar cada cuenta: saldo inicial + movimientos + saldo final
        List<LibroMayorResponse.CuentaConMovimientos> dtoCuentas = new ArrayList<>();
        BigDecimal totalDebeAcumulado = BigDecimal.ZERO;
        BigDecimal totalHaberAcumulado = BigDecimal.ZERO;
        int totalMovimientosAcumulado = 0;

        for (CuentaContable cuenta : cuentasObjetivo) {
            LibroMayorResponse.CuentaConMovimientos dto = procesarCuenta(cuenta, filter, indexCuentas);
            // Filtrar cuentas sin movimientos si el filtro lo pide
            if (!filter.incluirSinMovimientos() && dto.cantidadMovimientos() == 0) {
                continue;
            }
            dtoCuentas.add(dto);
            totalDebeAcumulado = totalDebeAcumulado.add(dto.totalDebePeriodo());
            totalHaberAcumulado = totalHaberAcumulado.add(dto.totalHaberPeriodo());
            totalMovimientosAcumulado += dto.cantidadMovimientos();
        }

        LibroMayorResponse.Totales totales = LibroMayorResponse.Totales.builder()
                .cantidadCuentas(dtoCuentas.size())
                .cantidadMovimientos(totalMovimientosAcumulado)
                .totalDebe(totalDebeAcumulado)
                .totalHaber(totalHaberAcumulado)
                .balanceado(totalDebeAcumulado.compareTo(totalHaberAcumulado) == 0)
                .build();

        LibroMayorResponse.Encabezado encabezado = LibroMayorResponse.Encabezado.builder()
                .razonSocial(entidadProperties.getRazonSocial())
                .rif(entidadProperties.getRif())
                .desde(filter.desde())
                .hasta(filter.hasta())
                .generadoEn(Instant.now())
                .generadoPorUsuarioId(solicitanteId)
                .filtroCuenta(filter.codigoCuenta())
                .incluyeSinMovimientos(filter.incluirSinMovimientos())
                .incluyeTotalizadoras(filter.incluirTotalizadoras())
                .build();

        return LibroMayorResponse.builder()
                .encabezado(encabezado)
                .cuentas(dtoCuentas)
                .totales(totales)
                .build();
    }

    // ─── Selección de cuentas ──────────────────────────────────────────────

    private List<CuentaContable> seleccionarCuentas(LibroMayorFilter filter) {
        if (filter.filtraPorCuenta()) {
            CuentaContable c = cuentaRepository.buscarPorCodigo(filter.codigoCuenta())
                    .orElseThrow(() -> new AsientoContableException(
                            "cuenta no encontrada en el plan: " + filter.codigoCuenta()));
            return List.of(c);
        }
        // Sin filtro de código: todas las hojas, o todas si totalizadoras también.
        List<CuentaContable> todas = cuentaRepository.listarTodas();
        return todas.stream()
                .filter(c -> filter.incluirTotalizadoras() || c.isAceptaMovimientos())
                .sorted(Comparator.comparing(CuentaContable::getCodigo))
                .toList();
    }

    // ─── Procesamiento de una cuenta ───────────────────────────────────────

    private LibroMayorResponse.CuentaConMovimientos procesarCuenta(
            CuentaContable cuenta, LibroMayorFilter filter,
            Map<UUID, CuentaContable> indexCuentas) {

        NaturalezaSaldo naturaleza = cuenta.getNaturaleza();

        // a) Saldo inicial = SUM de partidas hasta (desde - 1)
        SaldoCuenta saldoInicial = asientoRepository
                .calcularSaldoCuentaHasta(cuenta.getId(), filter.desde().minusDays(1));

        // b) Listar asientos del período que tocaron esta cuenta
        List<AsientoContable> asientosPeriodo = asientoRepository
                .listarAsientosDeCuentaEnRango(cuenta.getId(), filter.desde(), filter.hasta());

        // c) Iterar asientos → para cada partida DE ESTA CUENTA, construir un MovimientoMayor
        //    calculando el saldo acumulado paso a paso.
        List<LibroMayorResponse.MovimientoMayor> movs = new ArrayList<>();
        SaldoCuenta acumulador = saldoInicial;
        BigDecimal totalDebePeriodo = BigDecimal.ZERO;
        BigDecimal totalHaberPeriodo = BigDecimal.ZERO;

        for (AsientoContable a : asientosPeriodo) {
            // Una cuenta puede aparecer más de una vez en el mismo asiento si está
            // en ambos lados (dominio lo permite). Procesamos todas las partidas
            // que matchean esta cuenta.
            List<PartidaAsiento> partidasDeEstaCuenta = a.getPartidas().stream()
                    .filter(p -> p.getCuentaId().equals(cuenta.getId()))
                    .sorted(Comparator.comparingInt(PartidaAsiento::getOrden))
                    .toList();

            // Las "partidas opuestas" (lado contrario) son las que NO matchean esta cuenta.
            List<PartidaAsiento> opuestas = a.getPartidas().stream()
                    .filter(p -> !p.getCuentaId().equals(cuenta.getId()))
                    .toList();

            for (PartidaAsiento p : partidasDeEstaCuenta) {
                // Acumular saldo
                acumulador = acumulador.mas(new SaldoCuenta(p.getDebe(), p.getHaber()));
                totalDebePeriodo = totalDebePeriodo.add(p.getDebe());
                totalHaberPeriodo = totalHaberPeriodo.add(p.getHaber());

                // Resolver contracuenta: si p es DEBE, la opuesta es del HABER (y viceversa).
                List<PartidaAsiento> opuestasFiltradas = opuestas.stream()
                        .filter(op -> p.esDeDebe() ? op.esDeHaber() : op.esDeDebe())
                        .toList();
                ContracuentaInfo cc = resolverContracuenta(opuestasFiltradas, indexCuentas);

                movs.add(LibroMayorResponse.MovimientoMayor.builder()
                        .fechaContable(a.getFechaContable())
                        .numeroAsiento(a.getNumero() == null ? 0L : a.getNumero())
                        .numeroAsientoFormateado(formatearNumero(a))
                        .origen(a.getOrigen())
                        .glosaAsiento(a.getGlosa())
                        .referenciaExterna(a.getReferenciaExterna())
                        .contracuentaCodigo(cc.codigo)
                        .contracuentaNombre(cc.nombre)
                        .contracuentaResumen(cc.resumen)
                        .debe(p.getDebe())
                        .haber(p.getHaber())
                        .saldoAcumulado(acumulador.saldoNeto(naturaleza))
                        .build());
            }
        }

        // d) Construir DTO con etiquetas D/A según naturaleza y signo
        BigDecimal saldoIniNeto = saldoInicial.saldoNeto(naturaleza);
        BigDecimal saldoFinNeto = acumulador.saldoNeto(naturaleza);

        return LibroMayorResponse.CuentaConMovimientos.builder()
                .codigo(cuenta.getCodigo())
                .nombre(cuenta.getNombre())
                .tipo(cuenta.getTipo())
                .naturaleza(naturaleza)
                .saldoInicialDebe(saldoInicial.totalDebe())
                .saldoInicialHaber(saldoInicial.totalHaber())
                .saldoInicialNeto(saldoIniNeto.abs())
                .saldoInicialEtiqueta(etiquetaSaldo(saldoIniNeto, naturaleza))
                .movimientos(movs)
                .totalDebePeriodo(totalDebePeriodo)
                .totalHaberPeriodo(totalHaberPeriodo)
                .cantidadMovimientos(movs.size())
                .saldoFinalDebe(acumulador.totalDebe())
                .saldoFinalHaber(acumulador.totalHaber())
                .saldoFinalNeto(saldoFinNeto.abs())
                .saldoFinalEtiqueta(etiquetaSaldo(saldoFinNeto, naturaleza))
                .build();
    }

    /**
     * Etiqueta del saldo: "D" si quedó deudor (positivo en cuenta deudora o
     * negativo en acreedora), "A" si quedó acreedor (positivo en acreedora o
     * negativo en deudora), "—" si saldo cero.
     *
     * <p>El saldo neto pre-firmado por {@link NaturalezaSaldo#calcularSaldo}
     * ya es positivo cuando "está del lado de su naturaleza". Si es negativo,
     * la cuenta tiene saldo del lado opuesto (poco usual pero válido).</p>
     */
    static String etiquetaSaldo(BigDecimal saldoNeto, NaturalezaSaldo naturaleza) {
        int signo = saldoNeto.signum();
        if (signo == 0) return "—";
        if (signo > 0) {
            return naturaleza == NaturalezaSaldo.DEUDORA ? "D" : "A";
        } else {
            return naturaleza == NaturalezaSaldo.DEUDORA ? "A" : "D";
        }
    }

    // ─── Contracuenta ──────────────────────────────────────────────────────

    /** Info resumida de la contracuenta principal del movimiento. */
    private record ContracuentaInfo(String codigo, String nombre, String resumen) {}

    /**
     * Resuelve la contracuenta a mostrar. Reglas:
     * <ul>
     *   <li>Si hay exactamente 1 partida opuesta → esa cuenta es la contracuenta.</li>
     *   <li>Si hay > 1 → tomamos la de mayor monto absoluto como "principal" y
     *       marcamos {@code resumen="múltiple"}.</li>
     *   <li>Si por alguna razón no hay opuestas (no debería pasar por invariante
     *       de partida doble) → "(sin contracuenta)".</li>
     * </ul>
     */
    private ContracuentaInfo resolverContracuenta(
            List<PartidaAsiento> opuestas, Map<UUID, CuentaContable> indexCuentas) {
        if (opuestas.isEmpty()) {
            return new ContracuentaInfo("—", "(sin contracuenta)", null);
        }
        // Ordenamos por monto descendente y tomamos la primera como principal
        PartidaAsiento principal = opuestas.stream()
                .max(Comparator.comparing(PartidaAsiento::monto))
                .orElse(opuestas.get(0));
        CuentaContable cuenta = indexCuentas.get(principal.getCuentaId());
        String codigo = cuenta != null ? cuenta.getCodigo() : "???";
        String nombre = cuenta != null ? cuenta.getNombre() : "[desconocida " + principal.getCuentaId() + "]";
        String resumen = opuestas.size() > 1 ? "(múltiple)" : null;
        return new ContracuentaInfo(codigo, nombre, resumen);
    }

    /** Mismo formato visual del Libro Diario: "AÑO-NNNNNN". */
    static String formatearNumero(AsientoContable a) {
        int anio = a.getFechaContable().getYear();
        return String.format("%d-%06d", anio,
                a.getNumero() == null ? 0L : a.getNumero());
    }
}
