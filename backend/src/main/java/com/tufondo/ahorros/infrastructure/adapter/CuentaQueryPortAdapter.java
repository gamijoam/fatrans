// Adaptador para el puerto CuentaQueryPort usado por el módulo Documentos PDF
package com.tufondo.ahorros.infrastructure.adapter;

import com.tufondo.ahorros.infrastructure.persistence.entity.CuentaAhorroEntity;
import com.tufondo.ahorros.infrastructure.persistence.entity.MovimientoEntity;
import com.tufondo.ahorros.infrastructure.persistence.jpa.CuentaAhorroJpaRepository;
import com.tufondo.ahorros.infrastructure.persistence.jpa.MovimientoJpaRepository;
import com.tufondo.core.port.CuentaQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaQueryPortAdapter implements CuentaQueryPort {

    private final CuentaAhorroJpaRepository cuentaAhorroJpaRepository;
    private final MovimientoJpaRepository movimientoJpaRepository;

    @Override
    public Map<String, Object> obtenerDatosCuenta(UUID cuentaId) {
        log.debug("Obteniendo datos de cuenta: cuentaId={}", cuentaId);

        return cuentaAhorroJpaRepository.findById(cuentaId)
                .map(this::mapCuentaEntityToMap)
                .orElse(null);
    }

    @Override
    public List<Map<String, Object>> obtenerMovimientos(UUID cuentaId, int anio, int mes) {
        log.debug("Obteniendo movimientos para cuenta={}, periodo={}/{}", cuentaId, mes, anio);

        LocalDateTime fechaInicio = LocalDateTime.of(anio, mes, 1, 0, 0, 0);
        LocalDateTime fechaFin = fechaInicio.plusMonths(1).minusSeconds(1);

        Page<MovimientoEntity> movimientosPage = movimientoJpaRepository.findByCuentaYRangoFechas(
                cuentaId, fechaInicio, fechaFin, PageRequest.of(0, 1000));

        return movimientosPage.getContent().stream()
                .map(this::mapMovimientoEntityToMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapCuentaEntityToMap(CuentaAhorroEntity entity) {
        Map<String, Object> datos = new HashMap<>();

        datos.put("id", entity.getId());
        datos.put("numeroCuenta", entity.getNumeroCuenta());
        datos.put("socioId", entity.getSocioId());
        datos.put("saldoActual", entity.getSaldoActual());
        datos.put("saldoRetenido", entity.getSaldoRetenido());
        datos.put("tasaInteres", entity.getTasaInteres());
        datos.put("estado", entity.getEstado() != null ? entity.getEstado().name() : null);
        datos.put("tipoCuenta", entity.getTipoCuenta() != null ? entity.getTipoCuenta().name() : null);
        datos.put("moneda", entity.getMoneda() != null ? entity.getMoneda().name() : null);
        datos.put("fechaApertura", entity.getFechaApertura());
        datos.put("fechaUltimaOperacion", entity.getFechaUltimaOperacion());

        // Saldo disponible = saldoActual - saldoRetenido
        datos.put("saldoDisponible", entity.getSaldoActual().subtract(entity.getSaldoRetenido()));

        return datos;
    }

    private Map<String, Object> mapMovimientoEntityToMap(MovimientoEntity entity) {
        Map<String, Object> datos = new HashMap<>();

        datos.put("id", entity.getId());
        datos.put("numeroOperacion", entity.getNumeroOperacion());
        datos.put("cuentaAhorroId", entity.getCuentaAhorroId());
        datos.put("socioId", entity.getSocioId());
        datos.put("tipo", entity.getTipo() != null ? entity.getTipo().name() : null);
        datos.put("monto", entity.getMonto());
        datos.put("saldoAnterior", entity.getSaldoAnterior());
        datos.put("saldoPosterior", entity.getSaldoPosterior());
        datos.put("descripcion", entity.getDescripcion());
        datos.put("referencia", entity.getReferencia());
        datos.put("canalOrigen", entity.getCanalOrigen() != null ? entity.getCanalOrigen().name() : null);
        datos.put("fechaMovimiento", entity.getFechaMovimiento());
        datos.put("estado", entity.getEstado() != null ? entity.getEstado().name() : null);

        return datos;
    }
}
