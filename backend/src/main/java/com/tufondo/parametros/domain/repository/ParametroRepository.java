package com.tufondo.parametros.domain.repository;

import com.tufondo.parametros.domain.model.ParametroSistema;
import java.util.List;
import java.util.Optional;

public interface ParametroRepository {

    List<ParametroSistema> listarTodos();

    Optional<ParametroSistema> buscarPorKey(String key);

    List<ParametroSistema> buscarPorCategoria(String categoria);

    void guardar(ParametroSistema parametro);

    void actualizar(ParametroSistema parametro);

    boolean existePorKey(String key);
}