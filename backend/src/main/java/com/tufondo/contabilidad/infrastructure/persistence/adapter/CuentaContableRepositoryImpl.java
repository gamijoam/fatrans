package com.tufondo.contabilidad.infrastructure.persistence.adapter;

import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import com.tufondo.contabilidad.domain.repository.CuentaContableRepository;
import com.tufondo.contabilidad.infrastructure.persistence.entity.CuentaContableEntity;
import com.tufondo.contabilidad.infrastructure.persistence.jpa.CuentaContableJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter del puerto {@link CuentaContableRepository}.
 *
 * <p>Las operaciones de lectura van con {@code readOnly=true} para que
 * Hibernate no genere dirty-checking innecesario (el plan de cuentas se
 * lee mucho más de lo que se escribe).</p>
 */
@Component
@RequiredArgsConstructor
public class CuentaContableRepositoryImpl implements CuentaContableRepository {

    private final CuentaContableJpaRepository jpaRepository;

    @Override
    @Transactional
    public CuentaContable guardar(CuentaContable cuenta) {
        CuentaContableEntity saved = jpaRepository.save(CuentaContableEntity.fromDomain(cuenta));
        return saved.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaContable> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(CuentaContableEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaContable> buscarPorCodigo(String codigo) {
        return jpaRepository.findByCodigo(codigo).map(CuentaContableEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        return jpaRepository.existsByCodigo(codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaContable> listarTodas() {
        return jpaRepository.findAllByOrderByCodigoAsc().stream()
                .map(CuentaContableEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaContable> listarPorTipo(TipoCuentaContable tipo) {
        return jpaRepository.findByTipoOrderByCodigoAsc(tipo).stream()
                .map(CuentaContableEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaContable> listarConMovimientos() {
        return jpaRepository.findByAceptaMovimientosTrueAndActivaTrueOrderByCodigoAsc().stream()
                .map(CuentaContableEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaContable> listarHijasDirectas(UUID cuentaPadreId) {
        return jpaRepository.findByCuentaPadreIdOrderByCodigoAsc(cuentaPadreId).stream()
                .map(CuentaContableEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return jpaRepository.count();
    }
}
