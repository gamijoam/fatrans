package com.tufondo.contabilidad.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Partida (renglón) de un asiento contable.
 *
 * <p>Cada partida afecta UNA cuenta del plan, ya sea al DEBE o al HABER —
 * nunca ambos, nunca ninguno. Esta invariante (debe XOR haber) se valida
 * en la construcción y se duplica como CHECK constraint en BD (defensa
 * en profundidad).</p>
 *
 * <p>El objeto es <b>inmutable</b>: una vez creado no se modifica. Para
 * "modificar" un asiento contable existente, se anula y se crea uno nuevo
 * con la reversión + el corregido (sub-issue #273).</p>
 *
 * <p>Acceso recomendado: usar las factories estáticas {@link #alDebe} y
 * {@link #alHaber} que dejan claro el lado del asiento. El constructor
 * vía builder es para reconstruir desde persistencia.</p>
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class PartidaAsiento {

    /**
     * Monto máximo permitido por partida. NUMERIC(18,4) en BD soporta
     * 14 dígitos enteros, así que 9_999_999_999_999.9999 es el límite
     * real. Acá fijamos un techo más bajo (1e13) para detectar errores
     * obvios (alguien ingresó un monto con 3 ceros de más).
     */
    public static final BigDecimal MONTO_MAXIMO = new BigDecimal("10000000000000.0000");

    /** Escala de los montos (4 decimales) — debe coincidir con BD. */
    public static final int ESCALA_MONTO = 4;

    private final UUID id;
    /** ID de la cuenta del plan contable que esta partida afecta. */
    private final UUID cuentaId;
    /** Monto al DEBE. {@code 0} si la partida es al HABER. */
    private final BigDecimal debe;
    /** Monto al HABER. {@code 0} si la partida es al DEBE. */
    private final BigDecimal haber;
    /** Posición de la partida en el asiento (1-based). */
    private final int orden;
    /** Glosa opcional específica de esta partida. */
    private final String glosa;

    private final Instant createdAt;

    @Builder(toBuilder = true)
    private PartidaAsiento(
            UUID id, UUID cuentaId, BigDecimal debe, BigDecimal haber,
            int orden, String glosa, Instant createdAt) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.debe = debe;
        this.haber = haber;
        this.orden = orden;
        this.glosa = glosa;
        this.createdAt = createdAt;
    }

    /**
     * Crea una partida al DEBE (movimiento que aumenta cuentas deudoras o
     * disminuye acreedoras).
     *
     * @param cuentaId cuenta afectada (debe ser hoja activa)
     * @param monto monto positivo (>0)
     * @param orden posición 1-based dentro del asiento
     * @param glosa descripción opcional
     */
    public static PartidaAsiento alDebe(UUID cuentaId, BigDecimal monto, int orden, String glosa) {
        validarMonto(monto, "monto al DEBE");
        Objects.requireNonNull(cuentaId, "cuentaId requerido");
        if (orden < 1) throw new IllegalArgumentException("orden debe ser >= 1, fue " + orden);
        return PartidaAsiento.builder()
                .id(UUID.randomUUID())
                .cuentaId(cuentaId)
                .debe(monto.setScale(ESCALA_MONTO, java.math.RoundingMode.HALF_UP))
                .haber(BigDecimal.ZERO.setScale(ESCALA_MONTO))
                .orden(orden)
                .glosa(glosa)
                .build();
    }

    /**
     * Crea una partida al HABER (movimiento que aumenta cuentas acreedoras
     * o disminuye deudoras).
     */
    public static PartidaAsiento alHaber(UUID cuentaId, BigDecimal monto, int orden, String glosa) {
        validarMonto(monto, "monto al HABER");
        Objects.requireNonNull(cuentaId, "cuentaId requerido");
        if (orden < 1) throw new IllegalArgumentException("orden debe ser >= 1, fue " + orden);
        return PartidaAsiento.builder()
                .id(UUID.randomUUID())
                .cuentaId(cuentaId)
                .debe(BigDecimal.ZERO.setScale(ESCALA_MONTO))
                .haber(monto.setScale(ESCALA_MONTO, java.math.RoundingMode.HALF_UP))
                .orden(orden)
                .glosa(glosa)
                .build();
    }

    /**
     * Reconstruye desde persistencia. Valida invariantes (debe XOR haber,
     * no negativos) — si la BD tiene datos corruptos, falla acá temprano
     * con mensaje claro.
     */
    public static PartidaAsiento reconstruir(
            UUID id, UUID cuentaId, BigDecimal debe, BigDecimal haber,
            int orden, String glosa, Instant createdAt) {
        Objects.requireNonNull(id, "id requerido al reconstruir");
        Objects.requireNonNull(cuentaId, "cuentaId requerido");
        Objects.requireNonNull(debe, "debe no puede ser null");
        Objects.requireNonNull(haber, "haber no puede ser null");
        validarDebeXorHaber(debe, haber);
        return PartidaAsiento.builder()
                .id(id)
                .cuentaId(cuentaId)
                .debe(debe)
                .haber(haber)
                .orden(orden)
                .glosa(glosa)
                .createdAt(createdAt)
                .build();
    }

    /** {@code true} si esta partida es al DEBE (debe > 0). */
    public boolean esDeDebe() {
        return debe.signum() > 0;
    }

    /** {@code true} si esta partida es al HABER (haber > 0). */
    public boolean esDeHaber() {
        return haber.signum() > 0;
    }

    /** Devuelve el monto efectivo (el lado distinto de cero). */
    public BigDecimal monto() {
        return esDeDebe() ? debe : haber;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Validaciones
    // ─────────────────────────────────────────────────────────────────────

    private static void validarMonto(BigDecimal monto, String campo) {
        Objects.requireNonNull(monto, campo + " no puede ser null");
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException(campo + " debe ser positivo (>0), fue " + monto);
        }
        if (monto.compareTo(MONTO_MAXIMO) > 0) {
            throw new IllegalArgumentException(String.format(
                    "%s %s excede el máximo permitido %s (posible error de tipeo?)",
                    campo, monto.toPlainString(), MONTO_MAXIMO.toPlainString()));
        }
    }

    private static void validarDebeXorHaber(BigDecimal debe, BigDecimal haber) {
        if (debe.signum() < 0 || haber.signum() < 0) {
            throw new IllegalArgumentException(
                    "debe y haber no pueden ser negativos: debe=" + debe + " haber=" + haber);
        }
        boolean deDebe = debe.signum() > 0;
        boolean deHaber = haber.signum() > 0;
        if (deDebe == deHaber) {  // ambos true o ambos false → inválido
            throw new IllegalArgumentException(String.format(
                    "una partida debe ser exactamente DEBE o HABER (debe=%s, haber=%s)",
                    debe, haber));
        }
    }
}
