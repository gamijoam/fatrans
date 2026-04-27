package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.TipoCreditoRequest;
import com.tufondo.creditos.application.dto.TipoCreditoResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import com.tufondo.creditos.infrastructure.security.XssSanitizer;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionarTipoCreditoUseCase {

    private final TipoCreditoRepository tipoCreditoRepository;
    private final CreditosDTOMapper mapper;
    private final XssSanitizer xssSanitizer;
    private final SecurityAuditService auditService;

    @Transactional(readOnly = true)
    public List<TipoCreditoResponse> listarTodos() {
        return tipoCreditoRepository.listarTodos().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoCreditoResponse obtenerPorId(Long id) {
        TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
                .orElseThrow(() -> new TipoCreditoNoEncontradoException(id));
        return mapper.toResponse(tipo);
    }

    @Transactional
    public TipoCreditoResponse crear(TipoCreditoRequest request, UUID adminId, String ipAddress) {
        String codigoNormalizado = request.getCodigo().toUpperCase().trim();

        if (tipoCreditoRepository.existePorCodigo(codigoNormalizado)) {
            throw new CodigoTipoCreditoYaExisteException(codigoNormalizado);
        }

        if (request.getPlazoMaximoMeses() < request.getPlazoMinimoMeses()) {
            throw new PlazoInvalidoException();
        }

        if (request.getMontoMaximo().compareTo(request.getMontoMinimo()) < 0) {
            throw new MontoInvalidoException();
        }

        TipoCredito tipo = TipoCredito.builder()
                .codigo(codigoNormalizado)
                .nombre(xssSanitizer.sanitize(request.getNombre()))
                .descripcion(xssSanitizer.sanitizeDescription(request.getDescripcion()))
                .tasaInteresAnual(request.getTasaInteresAnual())
                .plazoMinimoMeses(request.getPlazoMinimoMeses())
                .plazoMaximoMeses(request.getPlazoMaximoMeses())
                .montoMinimo(request.getMontoMinimo())
                .montoMaximo(request.getMontoMaximo())
                .porcentajeRequerimientoColateral(request.getPorcentajeRequerimientoColateral())
                .comisionApertura(request.getComisionApertura())
                .penalidadMoraTasa(request.getPenalidadMoraTasa())
                .diasGracia(request.getDiasGracia())
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        TipoCredito guardado = tipoCreditoRepository.guardar(tipo);

        auditService.logEntityEvent("TIPO_CREDITO_CREADO", adminId, ipAddress, "TIPO_CREDITO",
                String.valueOf(guardado.getId()), "TIPO_CREDITO_CREADO", "Tipo crédito creado: " + guardado.getCodigo());

        return mapper.toResponse(guardado);
    }

    @Transactional
    public TipoCreditoResponse actualizar(Long id, TipoCreditoRequest request, UUID adminId, String ipAddress) {
        TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
                .orElseThrow(() -> new TipoCreditoNoEncontradoException(id));

        String codigoNormalizado = request.getCodigo().toUpperCase().trim();

        if (!tipo.getCodigo().equals(codigoNormalizado) &&
            tipoCreditoRepository.existePorCodigo(codigoNormalizado)) {
            throw new CodigoTipoCreditoYaExisteException(codigoNormalizado);
        }

        if (request.getPlazoMaximoMeses() < request.getPlazoMinimoMeses()) {
            throw new PlazoInvalidoException();
        }

        if (request.getMontoMaximo().compareTo(request.getMontoMinimo()) < 0) {
            throw new MontoInvalidoException();
        }

        tipo.setCodigo(codigoNormalizado);
        tipo.setNombre(xssSanitizer.sanitize(request.getNombre()));
        tipo.setDescripcion(xssSanitizer.sanitizeDescription(request.getDescripcion()));
        tipo.setTasaInteresAnual(request.getTasaInteresAnual());
        tipo.setPlazoMinimoMeses(request.getPlazoMinimoMeses());
        tipo.setPlazoMaximoMeses(request.getPlazoMaximoMeses());
        tipo.setMontoMinimo(request.getMontoMinimo());
        tipo.setMontoMaximo(request.getMontoMaximo());
        tipo.setPorcentajeRequerimientoColateral(request.getPorcentajeRequerimientoColateral());
        tipo.setComisionApertura(request.getComisionApertura());
        tipo.setPenalidadMoraTasa(request.getPenalidadMoraTasa());
        tipo.setDiasGracia(request.getDiasGracia());
        tipo.setUpdatedAt(LocalDateTime.now());

        TipoCredito actualizado = tipoCreditoRepository.guardar(tipo);

        auditService.logEntityEvent("TIPO_CREDITO_ACTUALIZADO", adminId, ipAddress, "TIPO_CREDITO",
                String.valueOf(actualizado.getId()), "TIPO_CREDITO_ACTUALIZADO", "Tipo crédito actualizado: " + actualizado.getCodigo());

        return mapper.toResponse(actualizado);
    }

    @Transactional
    public TipoCreditoResponse activar(Long id, UUID adminId, String ipAddress) {
        TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
                .orElseThrow(() -> new TipoCreditoNoEncontradoException(id));

        tipo.setActivo(true);
        tipo.setUpdatedAt(LocalDateTime.now());
        TipoCredito actualizado = tipoCreditoRepository.guardar(tipo);

        auditService.logEntityEvent("TIPO_CREDITO_ACTIVADO", adminId, ipAddress, "TIPO_CREDITO",
                String.valueOf(actualizado.getId()), "TIPO_CREDITO_ACTIVADO", "Tipo crédito activado: " + actualizado.getCodigo());

        return mapper.toResponse(actualizado);
    }

    @Transactional
    public TipoCreditoResponse desactivar(Long id, UUID adminId, String ipAddress) {
        TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
                .orElseThrow(() -> new TipoCreditoNoEncontradoException(id));

        tipo.setActivo(false);
        tipo.setUpdatedAt(LocalDateTime.now());
        TipoCredito actualizado = tipoCreditoRepository.guardar(tipo);

        auditService.logEntityEvent("TIPO_CREDITO_DESACTIVADO", adminId, ipAddress, "TIPO_CREDITO",
                String.valueOf(actualizado.getId()), "TIPO_CREDITO_DESACTIVADO", "Tipo crédito desactivado: " + actualizado.getCodigo());

        return mapper.toResponse(actualizado);
    }

    public static class TipoCreditoNoEncontradoException extends RuntimeException {
        public TipoCreditoNoEncontradoException(Long id) {
            super("Tipo de crédito no encontrado con ID: " + id);
        }
    }

    public static class CodigoTipoCreditoYaExisteException extends RuntimeException {
        public CodigoTipoCreditoYaExisteException(String codigo) {
            super("Ya existe un tipo de crédito con código: " + codigo);
        }
    }

    public static class PlazoInvalidoException extends RuntimeException {
        public PlazoInvalidoException() {
            super("El plazo máximo debe ser mayor o igual al plazo mínimo");
        }
    }

    public static class MontoInvalidoException extends RuntimeException {
        public MontoInvalidoException() {
            super("El monto máximo debe ser mayor o igual al monto mínimo");
        }
    }
}