package com.tufondo.tipocambio.domain.repository;

import com.tufondo.tipocambio.domain.model.TipoCambio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TipoCambioRepository {

    Optional<TipoCambio> buscarPorId(UUID id);

    Optional<TipoCambio> buscarPorFecha(LocalDate fecha);

    Optional<TipoCambio> buscarTasaActual();

    List<TipoCambio> listarTodos();

    List<TipoCambio> listarHistorial(int limit);

    boolean existePorFecha(LocalDate fecha);

    void guardar(TipoCambio tipoCambio);

    void actualizar(TipoCambio tipoCambio);

    void eliminar(UUID id);
}