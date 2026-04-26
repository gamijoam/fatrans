package com.tufondo.parametros.infrastructure.persistence.adapter;

import com.tufondo.parametros.domain.model.ParametroSistema;
import com.tufondo.parametros.domain.repository.ParametroRepository;
import com.tufondo.parametros.infrastructure.persistence.entity.ParametroEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class ParametroRepositoryImpl implements ParametroRepository {

    private final ParametroJpaRepository jpaRepository;

    public ParametroRepositoryImpl(ParametroJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ParametroSistema> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(ParametroEntity::aDominio)
                .toList();
    }

    @Override
    public Optional<ParametroSistema> buscarPorKey(String key) {
        return jpaRepository.findById(key)
                .map(ParametroEntity::aDominio);
    }

    @Override
    public List<ParametroSistema> buscarPorCategoria(String categoria) {
        return jpaRepository.findByCategoria(categoria).stream()
                .map(ParametroEntity::aDominio)
                .toList();
    }

    @Override
    @Transactional
    public void guardar(ParametroSistema parametro) {
        jpaRepository.save(ParametroEntity.desdeDominio(parametro));
    }

    @Override
    @Transactional
    public void actualizar(ParametroSistema parametro) {
        jpaRepository.save(ParametroEntity.desdeDominio(parametro));
    }

    @Override
    public boolean existePorKey(String key) {
        return jpaRepository.existsById(key);
    }
}