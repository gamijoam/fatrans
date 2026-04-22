// com/tufondo/ahorros/application/mapper/AhorrosDTOMapper.java
package com.tufondo.ahorros.application.mapper;

import com.tufondo.ahorros.application.dto.*;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.Rendimiento;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre domain models y DTOs.
 */
@Component
public class AhorrosDTOMapper {

    public CuentaAhorroResponse toResponse(CuentaAhorro cuenta) {
        return CuentaAhorroResponse.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .socioId(cuenta.getSocioId())
                .saldoActual(cuenta.getSaldoActual())
                .saldoRetenido(cuenta.getSaldoRetenido())
                .saldoDisponible(cuenta.getSaldoDisponible())
                .tasaInteres(cuenta.getTasaInteres())
                .montoMinimoRequerido(cuenta.getMontoMinimoRequerido())
                .estado(cuenta.getEstado())
                .tipoCuenta(cuenta.getTipoCuenta())
                .moneda(cuenta.getMoneda())
                .fechaApertura(cuenta.getFechaApertura())
                .fechaUltimaOperacion(cuenta.getFechaUltimaOperacion())
                .build();
    }

    public MovimientoResponse toResponse(com.tufondo.ahorros.domain.model.Movimiento movimiento) {
        return MovimientoResponse.builder()
                .id(movimiento.getId())
                .numeroOperacion(movimiento.getNumeroOperacion())
                .cuentaAhorroId(movimiento.getCuentaAhorroId())
                .socioId(movimiento.getSocioId())
                .tipo(movimiento.getTipo())
                .monto(movimiento.getMonto())
                .saldoAnterior(movimiento.getSaldoAnterior())
                .saldoPosterior(movimiento.getSaldoPosterior())
                .descripcion(movimiento.getDescripcion())
                .referencia(movimiento.getReferencia())
                .canalOrigen(movimiento.getCanalOrigen())
                .estado(movimiento.getEstado())
                .fechaMovimiento(movimiento.getFechaMovimiento())
                .fechaValor(movimiento.getFechaValor())
                .build();
    }

    public RendimientoResponse toResponse(Rendimiento rendimiento) {
        return RendimientoResponse.builder()
                .id(rendimiento.getId())
                .cuentaAhorroId(rendimiento.getCuentaAhorroId())
                .periodoInicio(rendimiento.getPeriodoInicio())
                .periodoFin(rendimiento.getPeriodoFin())
                .saldoPromedioPeriodo(rendimiento.getSaldoPromedioPeriodo())
                .tasaAplicada(rendimiento.getTasaAplicada())
                .montoRendimiento(rendimiento.getMontoRendimiento())
                .tipo(rendimiento.getTipo())
                .estadoAplicacion(rendimiento.getEstadoAplicacion())
                .fechaCalculo(rendimiento.getFechaCalculo())
                .build();
    }

    public CuentaAhorro toDomain(CreateCuentaAhorroRequest request, String numeroCuenta) {
        return CuentaAhorro.builder()
                .numeroCuenta(numeroCuenta)
                .socioId(request.getSocioId())
                .saldoActual(java.math.BigDecimal.ZERO)
                .saldoRetenido(java.math.BigDecimal.ZERO)
                .tasaInteres(request.getTasaInteres())
                .montoMinimoRequerido(request.getMontoMinimoRequerido())
                .estado(com.tufondo.ahorros.domain.model.enums.EstadoCuenta.ACTIVA)
                .tipoCuenta(request.getTipoCuenta())
                .moneda(request.getMoneda())
                .fechaApertura(java.time.LocalDateTime.now())
                .build();
    }

    public com.tufondo.ahorros.domain.model.Movimiento toDomain(com.tufondo.ahorros.domain.model.Movimiento movimiento) {
        return movimiento;
    }
}