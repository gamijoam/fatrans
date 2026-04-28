package com.tufondo.transporte.application.dto;

import com.tufondo.transporte.domain.model.enums.TipoUnidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistrarUnidadRequestDTO {

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z0-9-]{4,10}$", message = "Formato de placa inválido")
    private String placa;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotNull(message = "El año del vehículo es obligatorio")
    private Integer ano;

    @NotNull(message = "El tipo de unidad es obligatorio")
    private TipoUnidad tipoUnidad;

    private Integer capacidadPasajeros;
    private LocalDate soatVencimiento;
    private LocalDate seguroVencimiento;
    private LocalDate revisionTecnicaVencimiento;
}
