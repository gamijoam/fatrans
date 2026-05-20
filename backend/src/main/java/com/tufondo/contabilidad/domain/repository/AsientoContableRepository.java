package com.tufondo.contabilidad.domain.repository;

import com.tufondo.contabilidad.domain.model.AsientoContable;
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
}
