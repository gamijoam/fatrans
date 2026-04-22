// com/tufondo/ahorros/application/dto/MovimientoResponse.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.CanalOrigen;
import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para movimiento.
 * NOTA: Campos sensibles ipOrigen, sessionId, requestId NO se exponen por seguridad (OWASP A02).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoResponse {
    private UUID id;
    private String numeroOperacion;
    private UUID cuentaAhorroId;
    private UUID socioId;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoPosterior;
    private String descripcion;
    private String referencia;
    private CanalOrigen canalOrigen;
    private EstadoMovimiento estado;
    private LocalDateTime fechaMovimiento;
    private LocalDate fechaValor;
}