// com/tufondo/creditos/infrastructure/persistence/jpa/SolicitudCreditoSpecification.java
package com.tufondo.creditos.infrastructure.persistence.jpa;

import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.infrastructure.persistence.entity.SolicitudCreditoEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Specifications para filtrado dinámico de SolicitudCreditoEntity.
 */
public class SolicitudCreditoSpecification {

    private SolicitudCreditoSpecification() {}

    public static Specification<SolicitudCreditoEntity> conFiltros(
            EstadoSolicitud estado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            BigDecimal montoMin,
            BigDecimal montoMax) {

        return Specification.where(tieneEstado(estado))
                .and(fechaDesdeMayorOIgual(fechaDesde))
                .and(fechaHastaMenorOIgual(fechaHasta))
                .and(montoMayorOIgual(montoMin))
                .and(montoMenorOIgual(montoMax));
    }

    public static Specification<SolicitudCreditoEntity> tieneEstado(EstadoSolicitud estado) {
        return (root, query, criteriaBuilder) -> {
            if (estado == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("estado"), estado);
        };
    }

    public static Specification<SolicitudCreditoEntity> fechaDesdeMayorOIgual(LocalDateTime fecha) {
        return (root, query, criteriaBuilder) -> {
            if (fecha == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fecha);
        };
    }

    public static Specification<SolicitudCreditoEntity> fechaHastaMenorOIgual(LocalDateTime fecha) {
        return (root, query, criteriaBuilder) -> {
            if (fecha == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), fecha);
        };
    }

    public static Specification<SolicitudCreditoEntity> montoMayorOIgual(BigDecimal monto) {
        return (root, query, criteriaBuilder) -> {
            if (monto == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("montoSolicitado"), monto);
        };
    }

    public static Specification<SolicitudCreditoEntity> montoMenorOIgual(BigDecimal monto) {
        return (root, query, criteriaBuilder) -> {
            if (monto == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("montoSolicitado"), monto);
        };
    }
}