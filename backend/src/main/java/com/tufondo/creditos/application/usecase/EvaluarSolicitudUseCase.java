// com/tufondo/creditos/application/usecase/EvaluarSolicitudUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.EvaluarSolicitudRequest;
import com.tufondo.creditos.application.dto.EvaluacionResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.exception.EstadoCreditoInvalidoException;
import com.tufondo.creditos.domain.model.EvaluacionCrediticia;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.model.enums.NivelRiesgo;
import com.tufondo.creditos.domain.repository.EvaluacionCrediticiaRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para evaluar una solicitud de crédito.
 * UC-CRE-02: Evaluar Solicitud de Crédito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluarSolicitudUseCase {

    private final SolicitudCreditoRepository solicitudRepository;
    private final EvaluacionCrediticiaRepository evaluacionRepository;
    private final TipoCreditoRepository tipoCreditoRepository;
    private final CreditosDTOMapper mapper;

    @Transactional
    public EvaluacionResponse ejecutar(String numeroSolicitud, EvaluarSolicitudRequest request, String evaluador) {
        return ejecutar(numeroSolicitud, 
            request.getPuntajeAntiguedad(),
            request.getPuntajeHistorialAhorro(),
            request.getPuntajeCapacidadPago(),
            request.getSalarioEstimado(),
            evaluador);
    }
    
    @Transactional
    public EvaluacionResponse ejecutar(String numeroSolicitud, Integer puntajeAntiguedad, 
            Integer puntajeHistorialAhorro, Integer puntajeCapacidadPago, 
            BigDecimal salarioEstimado, String evaluador) {

        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validar estado
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new EstadoCreditoInvalidoException(numeroSolicitud, solicitud.getEstado(), "evaluación");
        }

        // Verificar que no existe evaluación previa
        if (evaluacionRepository.existePorSolicitudId(solicitud.getId())) {
            throw new IllegalStateException("La solicitud ya tiene una evaluación");
        }

        // Obtener tipo de crédito para tasa base
        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(solicitud.getTipoCreditoId())
            .orElseThrow(() -> new CreditoNoEncontradoException(solicitud.getTipoCreditoId()));

        // Calcular score
        int score = Math.min(30, puntajeAntiguedad) + 
                    Math.min(30, puntajeHistorialAhorro) + 
                    Math.min(40, puntajeCapacidadPago);

        // Crear evaluación (tasaInteresFinal se calcula después)
        EvaluacionCrediticia evaluacion = EvaluacionCrediticia.builder()
            .id(UUID.randomUUID())
            .solicitudId(solicitud.getId())
            .socioId(solicitud.getSocioId())
            .puntajeAntiguedad(puntajeAntiguedad)
            .puntajeHistorialAhorro(puntajeHistorialAhorro)
            .puntajeCapacidadPago(puntajeCapacidadPago)
            .scoreInterno(score)
            .elegible(score >= 50)
            .nivelRiesgo(score >= 70 ? NivelRiesgo.BAJO : (score >= 50 ? NivelRiesgo.MEDIO : NivelRiesgo.ALTO))
            .tasaInteresFinal(null)  // se calcula con el método del objeto
            .evaluador(evaluador)
            .createdAt(LocalDateTime.now())
            .build();

        // Calcular tasa de interés usando el método del objeto
        evaluacion.setTasaInteresFinal(evaluacion.calcularTasaInteres(tipoCredito.getTasaInteresAnual()));

        // Calcular hash de auditoría
        evaluacion.calcularHash();

        // Mensaje de decisión
        if (score >= 50) {
            evaluacion.setMensajeDecision("Solicitud elegible con tasa " + 
                (score >= 80 ? "preferencial (15% descuento)" : 
                 score >= 70 ? "reducida (5% descuento)" : "estándar"));
        } else {
            evaluacion.setMensajeDecision("Score insuficiente: " + score + " (mínimo: 50)");
        }

        evaluacion = evaluacionRepository.guardar(evaluacion);

        // Actualizar solicitud
        solicitud.setEvaluacionId(evaluacion.getId());
        solicitud.setTasaInteresAplicada(evaluacion.getTasaInteresFinal());
        solicitud.transicionarA(EstadoSolicitud.EN_EVALUACION);
        solicitudRepository.guardar(solicitud);

        log.info("Evaluación creada: {} para solicitud {} - Score: {}", 
            evaluacion.getId(), numeroSolicitud, score);

        return mapper.toResponse(evaluacion);
    }
}
