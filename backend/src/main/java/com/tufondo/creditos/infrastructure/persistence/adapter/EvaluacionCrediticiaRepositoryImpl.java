// com/tufondo/creditos/infrastructure/persistence/adapter/EvaluacionCrediticiaRepositoryImpl.java
package com.tufondo.creditos.infrastructure.persistence.adapter;

import com.tufondo.creditos.domain.model.EvaluacionCrediticia;
import com.tufondo.creditos.domain.repository.EvaluacionCrediticiaRepository;
import com.tufondo.creditos.infrastructure.persistence.entity.EvaluacionCrediticiaEntity;
import com.tufondo.creditos.infrastructure.persistence.jpa.EvaluacionCrediticiaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de EvaluacionCrediticia.
 */
@Repository
@RequiredArgsConstructor
public class EvaluacionCrediticiaRepositoryImpl implements EvaluacionCrediticiaRepository {

    private final EvaluacionCrediticiaJpaRepository jpaRepository;

    @Override
    public EvaluacionCrediticia guardar(EvaluacionCrediticia evaluacion) {
        EvaluacionCrediticiaEntity entity = toEntity(evaluacion);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<EvaluacionCrediticia> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<EvaluacionCrediticia> buscarPorSolicitudId(UUID solicitudId) {
        return jpaRepository.findBySolicitudId(solicitudId).map(this::toDomain);
    }

    @Override
    public boolean existePorSolicitudId(UUID solicitudId) {
        return jpaRepository.existsBySolicitudId(solicitudId);
    }

    private EvaluacionCrediticia toDomain(EvaluacionCrediticiaEntity entity) {
        return EvaluacionCrediticia.builder()
            .id(entity.getId())
            .solicitudId(entity.getSolicitudId())
            .socioId(entity.getSocioId())
            .puntajeAntiguedad(entity.getPuntajeAntiguedad())
            .puntajeHistorialAhorro(entity.getPuntajeHistorialAhorro())
            .puntajeCapacidadPago(entity.getPuntajeCapacidadPago())
            .scoreInterno(entity.getScoreInterno())
            .scoreHash(entity.getScoreHash())
            .factoresSerializados(entity.getFactoresSerializados())
            .firmaVerificable(entity.getFirmaVerificable())
            .evaluacionIdOriginal(entity.getEvaluacionIdOriginal())
            .elegible(entity.getElegible())
            .nivelRiesgo(entity.getNivelRiesgo())
            .tasaInteresFinal(entity.getTasaInteresFinal())
            .mensajeDecision(entity.getMensajeDecision())
            .evaluador(entity.getEvaluador())
            .createdAt(entity.getCreatedAt())
            .version(entity.getVersion())
            .build();
    }

    private EvaluacionCrediticiaEntity toEntity(EvaluacionCrediticia domain) {
        return EvaluacionCrediticiaEntity.builder()
            .id(domain.getId())
            .solicitudId(domain.getSolicitudId())
            .socioId(domain.getSocioId())
            .puntajeAntiguedad(domain.getPuntajeAntiguedad())
            .puntajeHistorialAhorro(domain.getPuntajeHistorialAhorro())
            .puntajeCapacidadPago(domain.getPuntajeCapacidadPago())
            .scoreInterno(domain.getScoreInterno())
            .scoreHash(domain.getScoreHash())
            .factoresSerializados(domain.getFactoresSerializados())
            .firmaVerificable(domain.getFirmaVerificable())
            .evaluacionIdOriginal(domain.getEvaluacionIdOriginal())
            .elegible(domain.getElegible())
            .nivelRiesgo(domain.getNivelRiesgo())
            .tasaInteresFinal(domain.getTasaInteresFinal())
            .mensajeDecision(domain.getMensajeDecision())
            .evaluador(domain.getEvaluador())
            .createdAt(domain.getCreatedAt())
            .version(domain.getVersion())
            .build();
    }
}
