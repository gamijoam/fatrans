// com/tufondo/creditos/application/dto/DesembolsaRequest.java
package com.tufondo.creditos.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para desembolsar un crédito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesembolsaRequest {
    
    @Size(max = 100, message = "referencia no puede exceder 100 caracteres")
    private String referenciaDesembolso;
    
    private BigDecimal comisionAperturaAplicada;
}
