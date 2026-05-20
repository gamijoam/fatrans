package com.tufondo.contabilidad.domain.repository;

import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del repositorio del plan de cuentas. Define solo las operaciones
 * que el dominio necesita; el adapter JPA en {@code infrastructure} las
 * implementa.
 *
 * <p>El plan de cuentas es un dataset relativamente pequeño (decenas a pocos
 * cientos de cuentas) y cambia raramente, así que muchos consumidores van a
 * cachear el resultado de {@link #listarTodas()} en memoria. NO hace falta
 * paginar.</p>
 */
public interface CuentaContableRepository {

    /**
     * Persiste una cuenta nueva o existente. Si {@code cuenta.getId()} ya
     * existe en BD, actualiza; si no, inserta.
     */
    CuentaContable guardar(CuentaContable cuenta);

    /** Busca por UUID. {@code Optional.empty()} si no existe. */
    Optional<CuentaContable> buscarPorId(UUID id);

    /**
     * Busca por código contable (ej. "1.1.01"). Los códigos son únicos en
     * el plan, así que devuelve a lo sumo una cuenta.
     */
    Optional<CuentaContable> buscarPorCodigo(String codigo);

    /** {@code true} si existe alguna cuenta con ese código. */
    boolean existePorCodigo(String codigo);

    /**
     * Devuelve todas las cuentas del plan, ordenadas por código ascendente.
     * Útil para mostrar el plan completo en UI o para construir un index en
     * memoria.
     */
    List<CuentaContable> listarTodas();

    /** Filtra por tipo. Ordenadas por código ascendente. */
    List<CuentaContable> listarPorTipo(TipoCuentaContable tipo);

    /**
     * Devuelve solo las cuentas que aceptan movimientos (hojas operativas
     * del plan). Útil para UIs de creación de asientos — solo se puede
     * referenciar hojas.
     */
    List<CuentaContable> listarConMovimientos();

    /** Hijas directas de una cuenta (nivel padre + 1). */
    List<CuentaContable> listarHijasDirectas(UUID cuentaPadreId);

    /** Cantidad total de cuentas registradas. */
    long contar();
}
