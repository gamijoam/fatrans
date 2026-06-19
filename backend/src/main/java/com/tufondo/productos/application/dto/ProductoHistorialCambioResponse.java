package com.tufondo.productos.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProductoHistorialCambioResponse {

    private Long id;
    private Long productoId;
    private String tipoEvento;
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String estadoProducto;
    private UUID actorId;
    private LocalDateTime createdAt;
}
