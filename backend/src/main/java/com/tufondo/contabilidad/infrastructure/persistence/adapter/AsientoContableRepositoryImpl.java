package com.tufondo.contabilidad.infrastructure.persistence.adapter;

import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.SaldoCuenta;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.domain.repository.AsientoContableRepository;
import com.tufondo.contabilidad.infrastructure.persistence.entity.AsientoContableEntity;
import com.tufondo.contabilidad.infrastructure.persistence.entity.PartidaAsientoEntity;
import com.tufondo.contabilidad.infrastructure.persistence.jpa.AsientoContableJpaRepository;
import com.tufondo.contabilidad.infrastructure.persistence.jpa.PartidaAsientoJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter del puerto {@link AsientoContableRepository}.
 *
 * <p>Maneja la persistencia atómica del aggregate (cabecera + partidas)
 * dentro de una sola transacción. Si el insert de las partidas falla, la
 * cabecera se rolea atrás — preservando la invariante "asiento bien
 * formado en BD = asiento bien formado en memoria".</p>
 */
@Component
@RequiredArgsConstructor
public class AsientoContableRepositoryImpl implements AsientoContableRepository {

    private final AsientoContableJpaRepository asientoJpa;
    private final PartidaAsientoJpaRepository partidaJpa;

    /** EntityManager para las queries nativas del Libro Mayor (#270). */
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public AsientoContable guardar(AsientoContable asiento) {
        // 1. Asignar correlativo si el asiento es nuevo (numero null).
        AsientoContable conNumero = asiento;
        if (asiento.getNumero() == null) {
            Long siguiente = asientoJpa.siguienteNumeroAsiento();
            conNumero = asiento.conNumero(siguiente);
        }

        // 2. Persistir cabecera.
        AsientoContableEntity savedCabecera =
                asientoJpa.save(AsientoContableEntity.fromDomain(conNumero));

        // 3. Persistir partidas. Hacemos saveAll y dejamos que Hibernate
        // optimice el batch insert.
        List<PartidaAsientoEntity> partidas = conNumero.getPartidas().stream()
                .map(p -> PartidaAsientoEntity.fromDomain(p, savedCabecera.getId()))
                .toList();
        partidaJpa.saveAll(partidas);

        // 4. Reconstruir el agregado leyendo lo que quedó en BD (incluye
        // version actualizado, timestamps, etc.).
        return savedCabecera.toDomain(partidas);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsientoContable> buscarPorId(UUID id) {
        return asientoJpa.findById(id)
                .map(c -> c.toDomain(partidaJpa.findByAsientoIdOrderByOrdenAsc(c.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsientoContable> buscarPorNumero(long numero) {
        return asientoJpa.findByNumero(numero)
                .map(c -> c.toDomain(partidaJpa.findByAsientoIdOrderByOrdenAsc(c.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoContable> listarPorRangoFecha(LocalDate desde, LocalDate hasta) {
        List<AsientoContableEntity> cabeceras =
                asientoJpa.findByFechaContableBetweenOrderByNumeroAsc(desde, hasta);
        return hidratarConPartidas(cabeceras);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoContable> listarPorOrigen(OrigenAsiento origen, LocalDate desde, LocalDate hasta) {
        List<AsientoContableEntity> cabeceras = asientoJpa
                .findByOrigenAndFechaContableBetweenOrderByFechaContableAscNumeroAsc(origen, desde, hasta);
        return hidratarConPartidas(cabeceras);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoContable> listarPorEstado(EstadoAsiento estado) {
        return hidratarConPartidas(asientoJpa.findByEstadoOrderByNumeroAsc(estado));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoContable> buscarPorReferenciaExterna(String referenciaExterna) {
        return hidratarConPartidas(
                asientoJpa.findByReferenciaExternaOrderByNumeroAsc(referenciaExterna));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoContable> buscarReversionesDe(UUID asientoOriginalId) {
        return hidratarConPartidas(
                asientoJpa.findByAsientoReversaIdOrderByNumeroAsc(asientoOriginalId));
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return asientoJpa.count();
    }

    // ─── Queries para el Libro Mayor (#270) ────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SaldoCuenta calcularSaldoCuentaHasta(UUID cuentaId, LocalDate fechaCorte) {
        // Native SQL: SUM agregado en una sola query. Excluye ANULADOS.
        // COALESCE para devolver 0 si no hay filas (cuenta sin movimientos).
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT COALESCE(SUM(p.debe), 0) AS total_debe, " +
                "       COALESCE(SUM(p.haber), 0) AS total_haber " +
                "FROM partidas_asientos p " +
                "JOIN asientos_contables a ON a.id = p.asiento_id " +
                "WHERE p.cuenta_id = :cuentaId " +
                "  AND a.fecha_contable <= :fechaCorte " +
                "  AND a.estado = 'REGISTRADO'")
                .setParameter("cuentaId", cuentaId)
                .setParameter("fechaCorte", fechaCorte)
                .getSingleResult();

        BigDecimal totalDebe  = toBigDecimal(row[0]);
        BigDecimal totalHaber = toBigDecimal(row[1]);
        return new SaldoCuenta(totalDebe, totalHaber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoContable> listarAsientosDeCuentaEnRango(
            UUID cuentaId, LocalDate desde, LocalDate hasta) {

        // 1. Identificar IDs de los asientos REGISTRADOS que tocaron esa cuenta.
        //    Native SQL con DISTINCT para evitar duplicados si la cuenta aparece
        //    más de una vez en el asiento (cosa rara pero posible si está en
        //    ambos lados — lo que el dominio permite).
        @SuppressWarnings("unchecked")
        List<UUID> asientoIds = em.createNativeQuery(
                "SELECT DISTINCT a.id " +
                "FROM asientos_contables a " +
                "JOIN partidas_asientos p ON p.asiento_id = a.id " +
                "WHERE p.cuenta_id = :cuentaId " +
                "  AND a.fecha_contable BETWEEN :desde AND :hasta " +
                "  AND a.estado = 'REGISTRADO' " +
                "ORDER BY a.id")
                .setParameter("cuentaId", cuentaId)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        if (asientoIds.isEmpty()) return List.of();

        // 2. Cargar las cabeceras de esos asientos en una sola query JPA
        //    + hidratar con todas sus partidas (batch).
        List<AsientoContableEntity> cabeceras = asientoJpa.findAllById(asientoIds);
        // Ordenar por fechaContable asc, numero asc (no se garantiza por findAllById)
        cabeceras.sort((a, b) -> {
            int byFecha = a.getFechaContable().compareTo(b.getFechaContable());
            if (byFecha != 0) return byFecha;
            return Long.compare(
                    a.getNumero() == null ? 0L : a.getNumero(),
                    b.getNumero() == null ? 0L : b.getNumero());
        });
        return hidratarConPartidas(cabeceras);
    }

    /** Convierte el valor crudo de la BD (puede venir Number o BigDecimal) a BigDecimal. */
    private static BigDecimal toBigDecimal(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        if (raw instanceof BigDecimal bd) return bd;
        if (raw instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(raw.toString());
    }

    /**
     * Carga partidas de TODOS los asientos en UNA sola query y las une por
     * asientoId. Evita el N+1 que tendríamos si hacemos
     * {@code findByAsientoIdOrderByOrdenAsc} por cada cabecera.
     */
    private List<AsientoContable> hidratarConPartidas(List<AsientoContableEntity> cabeceras) {
        if (cabeceras.isEmpty()) return List.of();
        Collection<UUID> ids = cabeceras.stream()
                .map(AsientoContableEntity::getId)
                .toList();
        Map<UUID, List<PartidaAsientoEntity>> partidasPorAsiento =
                partidaJpa.findByAsientoIdInOrderByAsientoIdAscOrdenAsc(ids).stream()
                        .collect(Collectors.groupingBy(PartidaAsientoEntity::getAsientoId,
                                HashMap::new, Collectors.toList()));
        return cabeceras.stream()
                .map(c -> c.toDomain(partidasPorAsiento.getOrDefault(c.getId(), List.of())))
                .toList();
    }
}
