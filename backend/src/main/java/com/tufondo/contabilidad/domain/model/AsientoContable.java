package com.tufondo.contabilidad.domain.model;

import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Asiento contable — la unidad atómica de registro contable de partida doble.
 *
 * <p>Es un <b>aggregate root</b> que contiene una cabecera (fecha, glosa,
 * origen, estado) y una lista de {@link PartidaAsiento} (renglones). El
 * objeto valida en construcción TODAS las invariantes contables, de modo
 * que es imposible tener un {@code AsientoContable} mal formado en memoria:</p>
 *
 * <ol>
 *   <li><b>Tiene al menos 2 partidas</b> (uno al DEBE, otro al HABER).</li>
 *   <li><b>Suma DEBE = Suma HABER</b> (asiento balanceado — la regla de oro
 *       de la partida doble).</li>
 *   <li><b>Cada partida es DEBE XOR HABER</b> (validado por PartidaAsiento).</li>
 *   <li><b>No hay cuentas duplicadas en la misma posición</b> — si la misma
 *       cuenta aparece dos veces al DEBE, deben consolidarse.</li>
 * </ol>
 *
 * <p>El número correlativo ({@code numero}) lo asigna la BD vía secuencia
 * — el dominio acepta null al crear y se completa al persistir.</p>
 *
 * <p><b>Inmutabilidad</b>: los asientos NUNCA se modifican una vez registrados
 * (auditoría SUDECA). El método {@link #anular} devuelve una NUEVA instancia
 * con estado ANULADO; la "edición" de un asiento se hace via reversión más
 * un nuevo asiento corregido (sub-issue #273).</p>
 */
@Getter
@ToString(exclude = "partidas")  // partidas son ruidosas en logs
@EqualsAndHashCode(of = "id")
public final class AsientoContable {

    /** Mínimo de partidas en un asiento (partida doble). */
    public static final int MIN_PARTIDAS = 2;

    /** Máximo de glosa (debe coincidir con BD). */
    public static final int MAX_GLOSA = 500;

    private final UUID id;
    /**
     * Correlativo único. Lo asigna la secuencia {@code seq_asiento_numero}
     * al persistir. Puede ser {@code null} en asientos recién creados que
     * todavía no se persistieron.
     */
    private final Long numero;
    private final LocalDate fechaContable;
    private final String glosa;
    private final OrigenAsiento origen;
    private final String referenciaExterna;
    private final EstadoAsiento estado;
    private final UUID creadoPorUsuarioId;
    private final String motivoAnulacion;
    /**
     * Si este asiento es una REVERSIÓN, apunta al asiento original que está
     * reversando. {@code null} en asientos normales.
     */
    private final UUID asientoReversaId;

    /**
     * Lista inmutable de partidas. Ordenadas por {@link PartidaAsiento#getOrden}.
     */
    private final List<PartidaAsiento> partidas;

    private final Instant createdAt;
    private final Instant updatedAt;
    private final Long version;

    @Builder(toBuilder = true)
    private AsientoContable(
            UUID id, Long numero, LocalDate fechaContable, String glosa,
            OrigenAsiento origen, String referenciaExterna, EstadoAsiento estado,
            UUID creadoPorUsuarioId, String motivoAnulacion, UUID asientoReversaId,
            List<PartidaAsiento> partidas,
            Instant createdAt, Instant updatedAt, Long version) {
        this.id = id;
        this.numero = numero;
        this.fechaContable = fechaContable;
        this.glosa = glosa;
        this.origen = origen;
        this.referenciaExterna = referenciaExterna;
        this.estado = estado;
        this.creadoPorUsuarioId = creadoPorUsuarioId;
        this.motivoAnulacion = motivoAnulacion;
        this.asientoReversaId = asientoReversaId;
        this.partidas = partidas == null ? List.of() : Collections.unmodifiableList(partidas);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /**
     * Crea un asiento nuevo. Valida TODAS las invariantes contables — si
     * algo no cuadra, lanza {@link IllegalArgumentException} con detalle.
     *
     * <p>El {@code numero} se asigna en la BD al persistir, así que acá va
     * en {@code null}. El estado inicial es {@link EstadoAsiento#REGISTRADO}.</p>
     */
    public static AsientoContable crear(
            LocalDate fechaContable, String glosa, OrigenAsiento origen,
            String referenciaExterna, UUID creadoPorUsuarioId,
            UUID asientoReversaId, List<PartidaAsiento> partidas) {
        validarInvariantes(fechaContable, glosa, origen, partidas);
        return AsientoContable.builder()
                .id(UUID.randomUUID())
                .numero(null)  // BD asignará el correlativo
                .fechaContable(fechaContable)
                .glosa(glosa.trim())
                .origen(origen)
                .referenciaExterna(referenciaExterna)
                .estado(EstadoAsiento.REGISTRADO)
                .creadoPorUsuarioId(creadoPorUsuarioId)
                .motivoAnulacion(null)
                .asientoReversaId(asientoReversaId)
                .partidas(List.copyOf(partidas))
                .build();
    }

    /**
     * Reconstruye desde persistencia. Valida las mismas invariantes — si la
     * BD tiene datos inconsistentes (asiento desbalanceado, partidas
     * faltantes), explota acá con un mensaje claro.
     */
    public static AsientoContable reconstruir(
            UUID id, Long numero, LocalDate fechaContable, String glosa,
            OrigenAsiento origen, String referenciaExterna, EstadoAsiento estado,
            UUID creadoPorUsuarioId, String motivoAnulacion, UUID asientoReversaId,
            List<PartidaAsiento> partidas,
            Instant createdAt, Instant updatedAt, Long version) {
        Objects.requireNonNull(id, "id requerido al reconstruir");
        Objects.requireNonNull(estado, "estado requerido");
        validarInvariantes(fechaContable, glosa, origen, partidas);
        return AsientoContable.builder()
                .id(id)
                .numero(numero)
                .fechaContable(fechaContable)
                .glosa(glosa)
                .origen(origen)
                .referenciaExterna(referenciaExterna)
                .estado(estado)
                .creadoPorUsuarioId(creadoPorUsuarioId)
                .motivoAnulacion(motivoAnulacion)
                .asientoReversaId(asientoReversaId)
                .partidas(List.copyOf(partidas))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .version(version)
                .build();
    }

    /**
     * Anula este asiento. Devuelve una NUEVA instancia con
     * {@link EstadoAsiento#ANULADO} y el motivo. La anulación NO genera
     * el asiento de reversión automáticamente — eso lo hace el caller
     * (típicamente {@code AsientoContableService.anular}).
     *
     * @throws IllegalStateException si el asiento ya está anulado
     */
    public AsientoContable anular(String motivo) {
        if (estado == EstadoAsiento.ANULADO) {
            throw new IllegalStateException("asiento " + numero + " ya está anulado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("motivo de anulación es obligatorio");
        }
        if (motivo.length() > 500) {
            throw new IllegalArgumentException("motivo no puede exceder 500 caracteres");
        }
        return this.toBuilder()
                .estado(EstadoAsiento.ANULADO)
                .motivoAnulacion(motivo.trim())
                .build();
    }

    /**
     * Asigna el número correlativo (que viene de la secuencia BD).
     * Devuelve una nueva instancia con el {@code numero} seteado.
     */
    public AsientoContable conNumero(long numero) {
        if (this.numero != null) {
            throw new IllegalStateException(
                    "asiento ya tiene número asignado: " + this.numero);
        }
        return this.toBuilder().numero(numero).build();
    }

    /** Suma de los DEBE de todas las partidas. */
    public BigDecimal totalDebe() {
        return partidas.stream()
                .map(PartidaAsiento::getDebe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Suma de los HABER de todas las partidas. */
    public BigDecimal totalHaber() {
        return partidas.stream()
                .map(PartidaAsiento::getHaber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * {@code true} si {@code totalDebe == totalHaber}. La construcción ya
     * lo valida, así que en condiciones normales esto siempre es true para
     * un asiento bien formado.
     */
    public boolean estaBalanceado() {
        return totalDebe().compareTo(totalHaber()) == 0;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Invariantes (corren en crear y reconstruir)
    // ─────────────────────────────────────────────────────────────────────

    private static void validarInvariantes(
            LocalDate fechaContable, String glosa, OrigenAsiento origen,
            List<PartidaAsiento> partidas) {

        Objects.requireNonNull(fechaContable, "fechaContable requerida");
        Objects.requireNonNull(origen, "origen requerido");

        if (glosa == null || glosa.isBlank()) {
            throw new IllegalArgumentException("glosa es requerida");
        }
        if (glosa.length() > MAX_GLOSA) {
            throw new IllegalArgumentException(
                    "glosa no puede exceder " + MAX_GLOSA + " caracteres");
        }

        Objects.requireNonNull(partidas, "partidas requeridas");
        if (partidas.size() < MIN_PARTIDAS) {
            throw new IllegalArgumentException(String.format(
                    "un asiento debe tener al menos %d partidas (partida doble), tiene %d",
                    MIN_PARTIDAS, partidas.size()));
        }

        // Cada partida ya valida debe XOR haber en su propia construcción
        // (PartidaAsiento.alDebe / alHaber / reconstruir).

        // INVARIANTE FUNDAMENTAL: suma del DEBE = suma del HABER
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;
        for (PartidaAsiento p : partidas) {
            totalDebe = totalDebe.add(p.getDebe());
            totalHaber = totalHaber.add(p.getHaber());
        }
        if (totalDebe.compareTo(totalHaber) != 0) {
            throw new IllegalArgumentException(String.format(
                    "asiento desbalanceado: totalDebe=%s totalHaber=%s diferencia=%s",
                    totalDebe.toPlainString(),
                    totalHaber.toPlainString(),
                    totalDebe.subtract(totalHaber).toPlainString()));
        }

        // El asiento debe tener al menos una partida al DEBE y una al HABER
        // (si no, está balanceado en 0 pero no es un asiento real).
        boolean tieneDebe = partidas.stream().anyMatch(PartidaAsiento::esDeDebe);
        boolean tieneHaber = partidas.stream().anyMatch(PartidaAsiento::esDeHaber);
        if (!tieneDebe || !tieneHaber) {
            throw new IllegalArgumentException(
                    "un asiento debe tener al menos una partida al DEBE y otra al HABER");
        }

        // Detectar cuentas duplicadas en el mismo lado (DEBE o HABER). Si
        // alguien intenta poner la misma cuenta dos veces al DEBE, lo más
        // probable es un error — debería consolidarse en una sola partida.
        // (Una cuenta SÍ puede aparecer dos veces si una al DEBE y otra al
        // HABER, eso es legítimo en algunos casos.)
        Set<UUID> cuentasAlDebe = new HashSet<>();
        Set<UUID> cuentasAlHaber = new HashSet<>();
        for (PartidaAsiento p : partidas) {
            if (p.esDeDebe() && !cuentasAlDebe.add(p.getCuentaId())) {
                throw new IllegalArgumentException(
                        "cuenta duplicada al DEBE en el asiento: " + p.getCuentaId() +
                                " (consolidar en una sola partida)");
            }
            if (p.esDeHaber() && !cuentasAlHaber.add(p.getCuentaId())) {
                throw new IllegalArgumentException(
                        "cuenta duplicada al HABER en el asiento: " + p.getCuentaId() +
                                " (consolidar en una sola partida)");
            }
        }
    }
}
