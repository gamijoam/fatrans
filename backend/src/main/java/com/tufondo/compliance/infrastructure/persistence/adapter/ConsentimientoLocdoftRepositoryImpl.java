package com.tufondo.compliance.infrastructure.persistence.adapter;

import com.tufondo.compliance.domain.model.ConsentimientoLocdoftOperacion;
import com.tufondo.compliance.domain.repository.ConsentimientoLocdoftRepository;
import com.tufondo.compliance.infrastructure.persistence.entity.ConsentimientoLocdoftEntity;
import com.tufondo.compliance.infrastructure.persistence.jpa.ConsentimientoLocdoftJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConsentimientoLocdoftRepositoryImpl implements ConsentimientoLocdoftRepository {

    private final ConsentimientoLocdoftJpaRepository jpa;

    @Override
    @Transactional
    public ConsentimientoLocdoftOperacion guardar(ConsentimientoLocdoftOperacion c) {
        ConsentimientoLocdoftEntity e = new ConsentimientoLocdoftEntity();
        e.setId(c.getId() != null ? c.getId() : UUID.randomUUID());
        e.setSocioId(c.getSocioId());
        e.setCuentaAhorroId(c.getCuentaAhorroId());
        e.setMovimientoId(c.getMovimientoId());
        e.setTipoOperacion(c.getTipoOperacion().name());
        e.setMonto(c.getMonto());
        e.setMoneda(c.getMoneda());
        e.setUmbralAplicado(c.getUmbralAplicado());
        e.setAceptaOrigenLicito(c.isAceptaOrigenLicito());
        e.setOrigenFondos(c.getOrigenFondos());
        e.setIpOrigen(c.getIpOrigen());
        e.setUserAgent(c.getUserAgent());
        e.setSessionId(c.getSessionId());
        e.setRequestId(c.getRequestId());
        e.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt() : Instant.now());

        ConsentimientoLocdoftEntity saved = jpa.save(e);

        return ConsentimientoLocdoftOperacion.builder()
                .id(saved.getId())
                .socioId(saved.getSocioId())
                .cuentaAhorroId(saved.getCuentaAhorroId())
                .movimientoId(saved.getMovimientoId())
                .tipoOperacion(ConsentimientoLocdoftOperacion.TipoOperacion.valueOf(saved.getTipoOperacion()))
                .monto(saved.getMonto())
                .moneda(saved.getMoneda())
                .umbralAplicado(saved.getUmbralAplicado())
                .aceptaOrigenLicito(saved.isAceptaOrigenLicito())
                .origenFondos(saved.getOrigenFondos())
                .ipOrigen(saved.getIpOrigen())
                .userAgent(saved.getUserAgent())
                .sessionId(saved.getSessionId())
                .requestId(saved.getRequestId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void asociarConMovimiento(UUID consentimientoId, UUID movimientoId) {
        jpa.actualizarMovimientoId(consentimientoId, movimientoId);
    }
}
