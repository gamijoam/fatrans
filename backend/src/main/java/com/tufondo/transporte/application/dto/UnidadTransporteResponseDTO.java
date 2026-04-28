package com.tufondo.transporte.application.dto;

import com.tufondo.transporte.domain.model.enums.EstadoUnidad;
import com.tufondo.transporte.domain.model.enums.TipoUnidad;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UnidadTransporteResponseDTO {
    private UUID id;
    private UUID socioId;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private TipoUnidad tipoUnidad;
    private Integer capacidadPasajeros;
    private LocalDate soatVencimiento;
    private LocalDate seguroVencimiento;
    private LocalDate revisionTecnicaVencimiento;
    private EstadoUnidad estado;
    private LocalDateTime fechaRegistro;
}
