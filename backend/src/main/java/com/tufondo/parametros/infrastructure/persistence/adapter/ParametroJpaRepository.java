package com.tufondo.parametros.infrastructure.persistence.adapter;

import com.tufondo.parametros.infrastructure.persistence.entity.ParametroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametroJpaRepository extends JpaRepository<ParametroEntity, String> {
    List<ParametroEntity> findByCategoria(String categoria);
}