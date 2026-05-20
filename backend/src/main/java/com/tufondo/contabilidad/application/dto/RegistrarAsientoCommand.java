package com.tufondo.contabilidad.application.dto;

import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Command para registrar un asiento contable.
 *
 * <p>Es el input del {@code AsientoContableService.registrar}. Define cada
 * partida por código de cuenta (no por UUID) para que los callers no tengan
 * que resolver el UUID antes — el service hace ese lookup vs. el plan.</p>
 */
@Builder
public record RegistrarAsientoCommand(
        LocalDate fechaContable,
        String glosa,
        OrigenAsiento origen,
        String referenciaExterna,
        UUID creadoPorUsuarioId,
        /** Si este asiento es una reversión, el ID del asiento original. */
        UUID asientoReversaId,
        List<Partida> partidas
) {

    /**
     * Partida del comando, con cuenta referenciada por código del plan.
     * El service resuelve el UUID antes de crear el dominio.
     *
     * @param codigoCuenta código jerárquico VEN-NIF (ej. "1.1.01")
     * @param debe monto al DEBE; si > 0, haber debe ser 0
     * @param haber monto al HABER; si > 0, debe debe ser 0
     * @param glosa descripción opcional de la partida
     */
    @Builder
    public record Partida(
            String codigoCuenta,
            BigDecimal debe,
            BigDecimal haber,
            String glosa
    ) {}
}
