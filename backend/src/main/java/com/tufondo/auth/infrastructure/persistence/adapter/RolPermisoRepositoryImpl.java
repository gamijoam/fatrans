package com.tufondo.auth.infrastructure.persistence.adapter;

import com.tufondo.auth.domain.model.enums.Permiso;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.RolPermisoRepository;
import com.tufondo.auth.infrastructure.persistence.entity.RolPermisoEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.RolPermisoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RolPermisoRepositoryImpl implements RolPermisoRepository {

    private final RolPermisoJpaRepository jpaRepository;

    private static final List<Permiso> PERMISOS_SOCIO = List.of(
            Permiso.INICIAR_KYC, Permiso.VER_KYC, Permiso.SUBIR_DOCUMENTOS_KYC,
            Permiso.VER_HISTORIAL_KYC, Permiso.REVOCAR_CONSENTIMIENTO,
            Permiso.VER_CUENTAS, Permiso.REGISTRAR_DEPOSITOS, Permiso.REGISTRAR_RETIROS,
            Permiso.VER_MOVIMIENTOS, Permiso.VER_RENDIMIENTOS,
            Permiso.SOLICITAR_CREDITO, Permiso.SIMULAR_CREDITO,
            Permiso.GESTIONAR_BENEFICIARIOS, Permiso.VER_DOCUMENTOS, Permiso.DESCARGAR_DOCUMENTOS
    );

    private static final List<Permiso> PERMISOS_ADMIN = List.of(
            Permiso.VER_DASHBOARD, Permiso.GESTIONAR_SOCIOS, Permiso.GESTIONAR_SOLICITUDES,
            Permiso.APROBAR_SOLICITUDES, Permiso.RECHAZAR_SOLICITUDES,
            Permiso.GESTIONAR_CREDITOS, Permiso.EVALUAR_CREDITO, Permiso.APROBAR_CREDITO,
            Permiso.RECHAZAR_CREDITO, Permiso.DESEMBOLSAR_CREDITO,
            Permiso.GESTIONAR_KYC, Permiso.VER_KYC_ESTADISTICAS,
            Permiso.VER_CUENTAS, Permiso.VER_MOVIMIENTOS, Permiso.VER_RENDIMIENTOS,
            Permiso.GESTIONAR_BENEFICIARIOS, Permiso.GESTIONAR_DOCUMENTOS,
            Permiso.GESTIONAR_TIPOS_CREDITO, Permiso.GESTIONAR_TIPOS_CAMBIO,
            Permiso.GESTIONAR_USUARIOS, Permiso.CREAR_USUARIOS, Permiso.ACTIVAR_USUARIOS,
            Permiso.DESACTIVAR_USUARIOS, Permiso.VER_AUDITORIA,
            Permiso.REGISTRAR_PAGOS, Permiso.GESTIONAR_CUENTAS,
            Permiso.VER_DOCUMENTOS, Permiso.DESCARGAR_DOCUMENTOS,
            Permiso.EXPORTAR_ESTADO_CUENTA, Permiso.EXPORTAR_CONTRATO,
            Permiso.EXPORTAR_PAGARE, Permiso.EXPORTAR_TABLA_AMORTIZACION,
            Permiso.GENERAR_DOCUMENTOS
    );

    private static final List<Permiso> PERMISOS_CAJERO = List.of(
            Permiso.VER_DASHBOARD, Permiso.REGISTRAR_PAGOS, Permiso.VER_CUENTAS,
            Permiso.VER_MOVIMIENTOS
    );

    private static final List<Permiso> PERMISOS_ANALISTA_KYC = List.of(
            Permiso.VER_DASHBOARD, Permiso.REVISAR_KYC, Permiso.GESTIONAR_KYC,
            Permiso.VER_KYC_ESTADISTICAS, Permiso.VER_AUDITORIA
    );

    private static final List<Permiso> PERMISOS_SISTEMA = List.of(
            Permiso.GESTIONAR_CREDITOS, Permiso.EVALUAR_CREDITO, Permiso.APROBAR_CREDITO,
            Permiso.RECHAZAR_CREDITO, Permiso.DESEMBOLSAR_CREDITO,
            Permiso.GESTIONAR_CUENTAS, Permiso.REGISTRAR_DEPOSITOS, Permiso.REGISTRAR_RETIROS,
            Permiso.VER_CUENTAS, Permiso.VER_MOVIMIENTOS,
            Permiso.EXPORTAR_TABLA_AMORTIZACION, Permiso.GENERAR_DOCUMENTOS
    );

    private static final List<Permiso> PERMISOS_SUPER_ADMIN = List.of(
            Permiso.VER_DASHBOARD, Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS,
            Permiso.GESTIONAR_SOCIOS, Permiso.GESTIONAR_CREDITOS, Permiso.GESTIONAR_TIPOS_CREDITO,
            Permiso.GESTIONAR_TIPOS_CAMBIO, Permiso.GESTIONAR_PARAMETROS,
            Permiso.GESTIONAR_KYC, Permiso.REVISAR_KYC, Permiso.VER_KYC_ESTADISTICAS,
            Permiso.REGISTRAR_PAGOS, Permiso.GESTIONAR_CUENTAS, Permiso.VER_CUENTAS,
            Permiso.GESTIONAR_BENEFICIARIOS, Permiso.GESTIONAR_DOCUMENTOS,
            Permiso.GESTIONAR_SOLICITUDES, Permiso.APROBAR_SOLICITUDES, Permiso.RECHAZAR_SOLICITUDES,
            Permiso.CREAR_USUARIOS, Permiso.ACTIVAR_USUARIOS, Permiso.DESACTIVAR_USUARIOS,
            Permiso.INICIAR_KYC, Permiso.VER_KYC, Permiso.SUBIR_DOCUMENTOS_KYC,
            Permiso.VER_HISTORIAL_KYC, Permiso.REVOCAR_CONSENTIMIENTO,
            Permiso.REGISTRAR_DEPOSITOS, Permiso.REGISTRAR_RETIROS, Permiso.VER_MOVIMIENTOS,
            Permiso.SOLICITAR_CREDITO, Permiso.EVALUAR_CREDITO, Permiso.APROBAR_CREDITO,
            Permiso.RECHAZAR_CREDITO, Permiso.DESEMBOLSAR_CREDITO, Permiso.SIMULAR_CREDITO,
            Permiso.VER_RENDIMIENTOS, Permiso.EXPORTAR_ESTADO_CUENTA,
            Permiso.EXPORTAR_CONTRATO, Permiso.EXPORTAR_PAGARE,
            Permiso.EXPORTAR_TABLA_AMORTIZACION, Permiso.GENERAR_DOCUMENTOS,
            Permiso.VER_DOCUMENTOS, Permiso.DESCARGAR_DOCUMENTOS
    );

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorRol(Rol rol) {
        List<RolPermisoEntity> entities = jpaRepository.findByRol(rol);
        if (entities.isEmpty()) {
            return getPermisosDefault(rol);
        }
        return entities.stream().map(RolPermisoEntity::getPermiso).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorRoles(List<Rol> roles) {
        Set<Permiso> permisos = new HashSet<>();
        for (Rol rol : roles) {
            permisos.addAll(obtenerPermisosPorRol(rol));
        }
        return new ArrayList<>(permisos);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePermiso(Rol rol, Permiso permiso) {
        return obtenerPermisosPorRol(rol).contains(permiso);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneAlgunPermiso(Rol rol, List<Permiso> permisos) {
        List<Permiso> permisosDelRol = obtenerPermisosPorRol(rol);
        return permisos.stream().anyMatch(permisosDelRol::contains);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneTodosLosPermisos(Rol rol, List<Permiso> permisos) {
        List<Permiso> permisosDelRol = obtenerPermisosPorRol(rol);
        return new HashSet<>(permisosDelRol).containsAll(permisos);
    }

    @Override
    @Transactional
    public void asignarPermiso(Rol rol, Permiso permiso) {
        if (!jpaRepository.existsByRolAndPermiso(rol, permiso)) {
            jpaRepository.save(new RolPermisoEntity(rol, permiso));
            log.info("Permiso {} asignado al rol {}", permiso, rol);
        }
    }

    @Override
    @Transactional
    public void quitarPermiso(Rol rol, Permiso permiso) {
        List<RolPermisoEntity> entities = jpaRepository.findByRol(rol);
        entities.stream()
                .filter(e -> e.getPermiso() == permiso)
                .findFirst()
                .ifPresent(e -> jpaRepository.delete(e));
        log.info("Permiso {} removido del rol {}", permiso, rol);
    }

    @Override
    @Transactional
    public void inicializarPermisosDefault() {
        inicializarRol(Rol.SOCIO, PERMISOS_SOCIO);
        inicializarRol(Rol.ADMIN, PERMISOS_ADMIN);
        inicializarRol(Rol.CAJERO, PERMISOS_CAJERO);
        inicializarRol(Rol.ANALISTA_KYC, PERMISOS_ANALISTA_KYC);
        inicializarRol(Rol.SISTEMA, PERMISOS_SISTEMA);
        inicializarRol(Rol.SUPER_ADMIN, PERMISOS_SUPER_ADMIN);
        log.info("Permisos default inicializados para todos los roles");
    }

    private void inicializarRol(Rol rol, List<Permiso> permisos) {
        for (Permiso permiso : permisos) {
            if (!jpaRepository.existsByRolAndPermiso(rol, permiso)) {
                jpaRepository.save(new RolPermisoEntity(rol, permiso));
            }
        }
    }

    private List<Permiso> getPermisosDefault(Rol rol) {
        return switch (rol) {
            case SOCIO -> PERMISOS_SOCIO;
            case ADMIN -> PERMISOS_ADMIN;
            case CAJERO -> PERMISOS_CAJERO;
            case ANALISTA_KYC -> PERMISOS_ANALISTA_KYC;
            case SISTEMA -> PERMISOS_SISTEMA;
            case SUPER_ADMIN -> PERMISOS_SUPER_ADMIN;
        };
    }
}