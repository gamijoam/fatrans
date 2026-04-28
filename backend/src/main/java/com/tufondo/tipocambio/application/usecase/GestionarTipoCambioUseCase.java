package com.tufondo.tipocambio.application.usecase;

import com.tufondo.tipocambio.application.dto.TipoCambioRequest;
import com.tufondo.tipocambio.application.dto.TipoCambioResponse;
import com.tufondo.tipocambio.domain.model.TipoCambio;
import com.tufondo.tipocambio.domain.repository.TipoCambioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionarTipoCambioUseCase {

    private final TipoCambioRepository tipoCambioRepository;

    @Transactional
    public TipoCambioResponse crear(TipoCambioRequest request, UUID adminId, String ipAddress) {
        validarTasas(request);

        if (tipoCambioRepository.existePorFecha(request.getFecha())) {
            throw new TipoCambioYaExisteException(request.getFecha());
        }

        TipoCambio tipoCambio = TipoCambio.builder()
                .fecha(request.getFecha())
                .tasaCompra(request.getTasaCompra())
                .tasaVenta(request.getTasaVenta())
                .fuente(request.getFuente())
                .creadoPor(adminId)
                .createdAt(Instant.now())
                .build();

        tipoCambioRepository.guardar(tipoCambio);

        log.info("AUDIT [{}] adminId={} ip={} action=TIPO_CAMBIO_CREADO fecha={} tasaCompra={} tasaVenta={}",
                Instant.now(), adminId, ipAddress, request.getFecha(), request.getTasaCompra(), request.getTasaVenta());

        return toResponse(tipoCambio);
    }

    @Transactional
    public TipoCambioResponse actualizar(UUID id, TipoCambioRequest request, UUID adminId, String ipAddress) {
        TipoCambio existente = tipoCambioRepository.buscarPorId(id)
                .orElseThrow(() -> new TipoCambioNoEncontradoException(id));

        validarTasas(request);

        if (!existente.getFecha().equals(request.getFecha()) &&
            tipoCambioRepository.existePorFecha(request.getFecha())) {
            throw new TipoCambioYaExisteException(request.getFecha());
        }

        TipoCambio actualizado = TipoCambio.builder()
                .id(id)
                .fecha(request.getFecha())
                .tasaCompra(request.getTasaCompra())
                .tasaVenta(request.getTasaVenta())
                .fuente(request.getFuente())
                .creadoPor(existente.getCreadoPor())
                .createdAt(existente.getCreatedAt())
                .build();

        tipoCambioRepository.actualizar(actualizado);

        log.info("AUDIT [{}] adminId={} ip={} action=TIPO_CAMBIO_ACTUALIZADO id={} fecha={}",
                Instant.now(), adminId, ipAddress, id, request.getFecha());

        return toResponse(actualizado);
    }

    @Transactional
    public void eliminar(UUID id, UUID adminId, String ipAddress) {
        if (!tipoCambioRepository.buscarPorId(id).isPresent()) {
            throw new TipoCambioNoEncontradoException(id);
        }

        tipoCambioRepository.eliminar(id);

        log.info("AUDIT [{}] adminId={} ip={} action=TIPO_CAMBIO_ELIMINADO id={}",
                Instant.now(), adminId, ipAddress, id);
    }

    public Optional<TipoCambioResponse> obtenerPorId(UUID id) {
        return tipoCambioRepository.buscarPorId(id).map(this::toResponse);
    }

    private void validarTasas(TipoCambioRequest request) {
        if (request.getTasaCompra().compareTo(request.getTasaVenta()) > 0) {
            throw new TasaInvalidaException("La tasa de compra no puede ser mayor a la tasa de venta");
        }

        if (request.getTasaCompra().compareTo(BigDecimal.ZERO) <= 0 ||
            request.getTasaVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TasaInvalidaException("Las tasas deben ser mayores a cero");
        }
    }

    private TipoCambioResponse toResponse(TipoCambio tipoCambio) {
        return TipoCambioResponse.builder()
                .id(tipoCambio.getId())
                .fecha(tipoCambio.getFecha())
                .tasaCompra(tipoCambio.getTasaCompra())
                .tasaVenta(tipoCambio.getTasaVenta())
                .fuente(tipoCambio.getFuente())
                .creadoPor(tipoCambio.getCreadoPor())
                .createdAt(tipoCambio.getCreatedAt())
                .build();
    }

    public static class TipoCambioNoEncontradoException extends RuntimeException {
        public TipoCambioNoEncontradoException(UUID id) {
            super("Tipo de cambio no encontrado con ID: " + id);
        }
    }

    public static class TipoCambioYaExisteException extends RuntimeException {
        public TipoCambioYaExisteException(LocalDate fecha) {
            super("Ya existe un tipo de cambio para la fecha: " + fecha);
        }
    }

    public static class TasaInvalidaException extends RuntimeException {
        public TasaInvalidaException(String mensaje) {
            super(mensaje);
        }
    }
}