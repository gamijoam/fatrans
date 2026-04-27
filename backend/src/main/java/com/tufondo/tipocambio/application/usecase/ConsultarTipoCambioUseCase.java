package com.tufondo.tipocambio.application.usecase;

import com.tufondo.tipocambio.application.dto.TipoCambioResponse;
import com.tufondo.tipocambio.domain.model.TipoCambio;
import com.tufondo.tipocambio.domain.repository.TipoCambioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultarTipoCambioUseCase {

    private final TipoCambioRepository tipoCambioRepository;

    public Optional<TipoCambioResponse> obtenerTasaActual() {
        Optional<TipoCambio> tasaActual = tipoCambioRepository.buscarTasaActual();
        Optional<TipoCambio> tasaAnterior = tipoCambioRepository.listarHistorial(2).stream()
                .skip(1)
                .findFirst();

        return tasaActual.map(tc -> toResponse(tc, tasaAnterior.orElse(null)));
    }

    public Optional<TipoCambioResponse> obtenerPorFecha(LocalDate fecha) {
        Optional<TipoCambio> tasa = tipoCambioRepository.buscarPorFecha(fecha);
        Optional<TipoCambio> tasaAnterior = tipoCambioRepository.listarHistorial(2).stream()
                .skip(1)
                .findFirst();

        return tasa.map(tc -> toResponse(tc, tasaAnterior.orElse(null)));
    }

    public List<TipoCambioResponse> listarHistorial(int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 30;
        }
        List<TipoCambio> historial = tipoCambioRepository.listarHistorial(limit);
        return historial.stream()
                .map(this::toResponseSimple)
                .collect(Collectors.toList());
    }

    public List<TipoCambioResponse> listarTodos() {
        return tipoCambioRepository.listarTodos().stream()
                .map(this::toResponseSimple)
                .collect(Collectors.toList());
    }

    private TipoCambioResponse toResponse(TipoCambio tasa, TipoCambio tasaAnterior) {
        BigDecimal variacion = null;
        if (tasaAnterior != null && tasaAnterior.getTasaVenta().compareTo(BigDecimal.ZERO) > 0) {
            variacion = tasa.getTasaVenta()
                    .subtract(tasaAnterior.getTasaVenta())
                    .divide(tasaAnterior.getTasaVenta(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return TipoCambioResponse.builder()
                .id(tasa.getId())
                .fecha(tasa.getFecha())
                .tasaCompra(tasa.getTasaCompra())
                .tasaVenta(tasa.getTasaVenta())
                .fuente(tasa.getFuente())
                .creadoPor(tasa.getCreadoPor())
                .createdAt(tasa.getCreatedAt())
                .variacionPorcentual(variacion)
                .build();
    }

    private TipoCambioResponse toResponseSimple(TipoCambio tasa) {
        return TipoCambioResponse.builder()
                .id(tasa.getId())
                .fecha(tasa.getFecha())
                .tasaCompra(tasa.getTasaCompra())
                .tasaVenta(tasa.getTasaVenta())
                .fuente(tasa.getFuente())
                .creadoPor(tasa.getCreadoPor())
                .createdAt(tasa.getCreatedAt())
                .build();
    }
}