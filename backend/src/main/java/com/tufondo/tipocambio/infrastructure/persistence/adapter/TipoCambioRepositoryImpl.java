package com.tufondo.tipocambio.infrastructure.persistence.adapter;

import com.tufondo.tipocambio.domain.model.TipoCambio;
import com.tufondo.tipocambio.domain.repository.TipoCambioRepository;
import com.tufondo.tipocambio.infrastructure.persistence.entity.TipoCambioEntity;
import com.tufondo.tipocambio.infrastructure.persistence.jpa.TipoCambioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TipoCambioRepositoryImpl implements TipoCambioRepository {

    private final TipoCambioJpaRepository jpaRepository;

    @Override
    public Optional<TipoCambio> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<TipoCambio> buscarPorFecha(LocalDate fecha) {
        return jpaRepository.findByFecha(fecha).map(this::toDomain);
    }

    @Override
    public Optional<TipoCambio> buscarTasaActual() {
        return jpaRepository.findTasaActual().map(this::toDomain);
    }

    @Override
    public List<TipoCambio> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TipoCambio> listarHistorial(int limit) {
        return jpaRepository.findHistorial(limit).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorFecha(LocalDate fecha) {
        return jpaRepository.existsByFecha(fecha);
    }

    @Override
    public void guardar(TipoCambio tipoCambio) {
        TipoCambioEntity entity = toEntity(tipoCambio);
        jpaRepository.save(entity);
    }

    @Override
    public void actualizar(TipoCambio tipoCambio) {
        TipoCambioEntity entity = toEntity(tipoCambio);
        jpaRepository.save(entity);
    }

    @Override
    public void eliminar(UUID id) {
        jpaRepository.deleteById(id);
    }

    private TipoCambio toDomain(TipoCambioEntity entity) {
        return TipoCambio.builder()
                .id(entity.getId())
                .fecha(entity.getFecha())
                .tasaCompra(entity.getTasaCompra())
                .tasaVenta(entity.getTasaVenta())
                .fuente(entity.getFuente())
                .creadoPor(entity.getCreadoPor())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private TipoCambioEntity toEntity(TipoCambio domain) {
        return TipoCambioEntity.builder()
                .id(domain.getId())
                .fecha(domain.getFecha())
                .tasaCompra(domain.getTasaCompra())
                .tasaVenta(domain.getTasaVenta())
                .fuente(domain.getFuente())
                .creadoPor(domain.getCreadoPor())
                .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now())
                .build();
    }
}