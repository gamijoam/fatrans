package com.tufondo.productos.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "producto_historial_cambios")
@Getter
@Setter
public class ProductoHistorialCambioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "tipo_evento", nullable = false, length = 40)
    private String tipoEvento;

    @Column(name = "campo", length = 80)
    private String campo;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    @Column(name = "estado_producto", length = 20)
    private String estadoProducto;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
