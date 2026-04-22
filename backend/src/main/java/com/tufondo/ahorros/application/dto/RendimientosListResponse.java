// com/tufondo/ahorros/application/dto/RendimientosListResponse.java
package com.tufondo.ahorros.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para listar rendimientos con paginación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendimientosListResponse {
    private String numeroCuenta;
    private int pagina;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
    private List<RendimientoResponse> rendimientos;
}