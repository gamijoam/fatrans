package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.config.EntidadProperties;
import com.tufondo.contabilidad.application.dto.LibroDiarioFilter;
import com.tufondo.contabilidad.application.dto.LibroDiarioResponse;
import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.repository.AsientoContableRepository;
import com.tufondo.contabilidad.domain.repository.CuentaContableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Genera el Libro Diario (sub-issue #269) — reporte secuencial de todos los
 * asientos contables del período, ordenados por número correlativo.
 *
 * <p>Es el primer reporte exigido por SUDECA para auditoría. Incluye todos
 * los asientos REGISTRADOS y opcionalmente los ANULADOS (con marca visual),
 * para que la auditoría externa pueda ver el flujo completo del período sin
 * censura.</p>
 *
 * <h2>Performance</h2>
 * <ul>
 *   <li>Lectura: 1 query a {@code listarPorRangoFecha} (ya batch-loaded las partidas).</li>
 *   <li>Lookup de nombres: 1 query a {@code listarTodas()} del plan + index en memoria.
 *       Costo O(n) ~ decenas-cientos de cuentas, despreciable.</li>
 *   <li>No paginar — el Libro Diario es por naturaleza secuencial y completo.
 *       La validación del rango (≤ 1 año) en {@link LibroDiarioFilter} acota el tamaño.</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GenerarLibroDiarioUseCase {

    private final AsientoContableRepository asientoRepository;
    private final CuentaContableRepository cuentaRepository;
    private final EntidadProperties entidadProperties;

    @Transactional(readOnly = true)
    public LibroDiarioResponse ejecutar(LibroDiarioFilter filter, UUID solicitanteId) {
        log.info("Generando Libro Diario: desde={} hasta={} incluyeAnulados={} solicitante={}",
                filter.desde(), filter.hasta(), filter.incluirAnulados(), solicitanteId);

        // 1. Cargar todos los asientos del período (orden por número correlativo)
        List<AsientoContable> asientosRaw = asientoRepository
                .listarPorRangoFecha(filter.desde(), filter.hasta());

        // 2. Filtrar por anulados si el filtro lo pide
        List<AsientoContable> asientos = filter.incluirAnulados()
                ? asientosRaw
                : asientosRaw.stream()
                    .filter(a -> a.getEstado() != EstadoAsiento.ANULADO)
                    .toList();

        // 3. Cargar plan de cuentas e indexar por UUID para lookup de nombres
        Map<UUID, CuentaContable> indexPorId = cuentaRepository.listarTodas().stream()
                .collect(Collectors.toMap(CuentaContable::getId, Function.identity()));

        // 4. Mapear cada asiento al DTO con sus partidas resueltas
        List<LibroDiarioResponse.AsientoDiario> dtoAsientos = asientos.stream()
                .map(a -> mapearAsiento(a, indexPorId))
                .toList();

        // 5. Calcular totales del período
        int cantidadAnulados = (int) asientos.stream()
                .filter(a -> a.getEstado() == EstadoAsiento.ANULADO).count();
        BigDecimal totalDebe = asientos.stream()
                .map(AsientoContable::totalDebe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalHaber = asientos.stream()
                .map(AsientoContable::totalHaber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LibroDiarioResponse.Totales totales = LibroDiarioResponse.Totales.builder()
                .cantidadAsientos(asientos.size())
                .cantidadAnulados(cantidadAnulados)
                .totalDebe(totalDebe)
                .totalHaber(totalHaber)
                .balanceado(totalDebe.compareTo(totalHaber) == 0)
                .build();

        // 6. Armar encabezado con properties + auditoría
        LibroDiarioResponse.Encabezado encabezado = LibroDiarioResponse.Encabezado.builder()
                .razonSocial(entidadProperties.getRazonSocial())
                .rif(entidadProperties.getRif())
                .desde(filter.desde())
                .hasta(filter.hasta())
                .generadoEn(Instant.now())
                .generadoPorUsuarioId(solicitanteId)
                .incluyeAnulados(filter.incluirAnulados())
                .build();

        return LibroDiarioResponse.builder()
                .encabezado(encabezado)
                .asientos(dtoAsientos)
                .totales(totales)
                .build();
    }

    private LibroDiarioResponse.AsientoDiario mapearAsiento(
            AsientoContable a, Map<UUID, CuentaContable> indexCuentas) {

        // Partidas ordenadas por su campo `orden`
        List<LibroDiarioResponse.PartidaDiario> partidasDto = a.getPartidas().stream()
                .sorted(Comparator.comparingInt(PartidaAsiento::getOrden))
                .map(p -> mapearPartida(p, indexCuentas))
                .toList();

        return LibroDiarioResponse.AsientoDiario.builder()
                .numero(a.getNumero())
                .numeroFormateado(formatearNumero(a))
                .fechaContable(a.getFechaContable())
                .origen(a.getOrigen())
                .estado(a.getEstado())
                .glosa(a.getGlosa())
                .referenciaExterna(a.getReferenciaExterna())
                .motivoAnulacion(a.getMotivoAnulacion())
                .partidas(partidasDto)
                .totalDebe(a.totalDebe())
                .totalHaber(a.totalHaber())
                .build();
    }

    private LibroDiarioResponse.PartidaDiario mapearPartida(
            PartidaAsiento p, Map<UUID, CuentaContable> indexCuentas) {
        CuentaContable cuenta = indexCuentas.get(p.getCuentaId());
        // Defensa: si por alguna razón la cuenta no está en el index (ej. fue
        // borrada — aunque las FK ON DELETE RESTRICT lo impiden), mostramos
        // el UUID como nombre para que el reporte no rompa.
        String nombre = cuenta != null ? cuenta.getNombre() : "[CUENTA DESCONOCIDA " + p.getCuentaId() + "]";
        String codigo = cuenta != null ? cuenta.getCodigo() : "???";

        return LibroDiarioResponse.PartidaDiario.builder()
                .codigoCuenta(codigo)
                .nombreCuenta(nombre)
                .debe(p.getDebe())
                .haber(p.getHaber())
                .glosa(p.getGlosa())
                .orden(p.getOrden())
                .build();
    }

    /**
     * Formatea el número del asiento como {@code AÑO-NNNNNN} (ej. "2026-000123").
     * El año se toma de la fecha contable, no del momento de inserción.
     *
     * <p>NOTA: actualmente la SEQUENCE BD es continua y nunca se resetea.
     * Este formato presenta el número de forma "anual" sin requerir cambio
     * de modelo. SUDECA exige reset anual real — ver pendiente
     * P1 "Reset anual del correlativo" en {@code _pendientes-criticos.md}.
     * Sub-issue dedicado antes del primer cierre fiscal.</p>
     */
    static String formatearNumero(AsientoContable a) {
        int anio = a.getFechaContable().getYear();
        return String.format("%d-%06d", anio, a.getNumero());
    }
}
