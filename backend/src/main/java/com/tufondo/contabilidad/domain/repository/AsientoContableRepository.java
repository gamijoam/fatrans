package com.tufondo.contabilidad.domain.repository;

import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.SaldoCuenta;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del repositorio de asientos contables.
 *
 * <p>Operaciones de lectura optimizadas para los reportes contables típicos:
 * Libro Diario (por fecha), por origen (todos los depósitos), por
 * referencia (un movimiento específico).</p>
 */
public interface AsientoContableRepository {

    /**
     * Persiste el asiento (cabecera + partidas atómicamente).
     *
     * <p>El correlativo {@code numero} lo asigna la BD vía secuencia. La
     * implementación es responsable de hacer un {@code SELECT nextval()}
     * y setearlo en el asiento antes del INSERT.</p>
     *
     * <p>Devuelve el asiento con el {@code numero}, {@code createdAt},
     * {@code version} ya completados desde BD.</p>
     */
    AsientoContable guardar(AsientoContable asiento);

    Optional<AsientoContable> buscarPorId(UUID id);

    Optional<AsientoContable> buscarPorNumero(long numero);

    /**
     * Asientos registrados en un rango de fechas, ordenados por número
     * correlativo asc. Útil para el Libro Diario.
     */
    List<AsientoContable> listarPorRangoFecha(LocalDate desde, LocalDate hasta);

    /**
     * Asientos por origen de negocio (ej. todos los depósitos del mes).
     * Ordenados por fecha + número.
     */
    List<AsientoContable> listarPorOrigen(OrigenAsiento origen, LocalDate desde, LocalDate hasta);

    /**
     * Asientos por estado (ej. listar todos los ANULADOS para revisión).
     */
    List<AsientoContable> listarPorEstado(EstadoAsiento estado);

    /**
     * Busca asientos que referencien un evento externo (ej. número de
     * operación de movimiento de ahorros). Puede devolver más de uno si
     * el evento generó varios asientos (asiento + reversión).
     */
    List<AsientoContable> buscarPorReferenciaExterna(String referenciaExterna);

    /**
     * Asientos de reversión que apuntan a un asiento original. Útil para
     * detectar si un asiento ya fue reversado.
     */
    List<AsientoContable> buscarReversionesDe(UUID asientoOriginalId);

    long contar();

    // ─── Queries para el Libro Mayor (#270) ────────────────────────────────

    /**
     * Calcula el saldo acumulado de una cuenta a una fecha de corte (inclusive).
     *
     * <p>Suma TODAS las partidas DEBE/HABER que tocaron la cuenta hasta la
     * {@code fechaCorte}, excluyendo asientos {@link EstadoAsiento#ANULADO}.
     * Una sola query SQL agregada — eficiente incluso con miles de partidas.</p>
     *
     * <p>Si la cuenta no tiene movimientos hasta esa fecha, devuelve
     * {@link SaldoCuenta#cero()}.</p>
     */
    SaldoCuenta calcularSaldoCuentaHasta(UUID cuentaId, LocalDate fechaCorte);

    /**
     * Lista los asientos COMPLETOS (con todas sus partidas) que tocaron una
     * cuenta específica en un rango de fechas.
     *
     * <p>Excluye asientos {@link EstadoAsiento#ANULADO} — el Libro Mayor
     * muestra saldos vigentes, no historial. Para historial de anulados ver
     * Libro Diario (#269).</p>
     *
     * <p>El use case del Libro Mayor extrae las partidas de la cuenta y
     * resuelve la contracuenta mirando las otras partidas del mismo asiento
     * (ya cargadas batch — sin N+1).</p>
     *
     * <p>Orden: por fecha contable ascendente, luego por número correlativo.</p>
     */
    List<AsientoContable> listarAsientosDeCuentaEnRango(
            UUID cuentaId, LocalDate desde, LocalDate hasta);
}
