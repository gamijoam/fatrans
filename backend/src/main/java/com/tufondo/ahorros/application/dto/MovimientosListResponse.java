// com/tufondo/ahorros/application/dto/MovimientosListResponse.java
package com.tufondo.ahorros.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para listar movimientos con paginación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientosListResponse {
    private String numeroCuenta;
    private int pagina;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
    private List<MovimientoResponse> movimientos;
}