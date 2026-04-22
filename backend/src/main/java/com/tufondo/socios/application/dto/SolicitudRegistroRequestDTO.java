// 📁 com/tufondo/socios/application/dto/SolicitudRegistroRequestDTO.java
package com.tufondo.socios.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de registro de nuevo socio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRegistroRequestDTO {
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreCompleto;
    
    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[VE]-[0-9]{7,10}$", message = "Formato de cédula inválido. Use V-12345678 o E-12345678")
    private String cedula;
    
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    private String correoElectronico;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "El teléfono debe tener 10 o 11 dígitos")
    private String telefono;
    
    @NotBlank(message = "La empresa es obligatoria")
    @Size(min = 2, max = 200, message = "El nombre de la empresa debe tener entre 2 y 200 caracteres")
    private String empresa;
}