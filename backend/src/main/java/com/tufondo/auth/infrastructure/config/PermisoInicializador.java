package com.tufondo.auth.infrastructure.config;

import com.tufondo.auth.domain.repository.RolPermisoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermisoInicializador implements ApplicationRunner {

    private final RolPermisoRepository rolPermisoRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Inicializando permisos default del sistema...");
        rolPermisoRepository.inicializarPermisosDefault();
        log.info("Permisos default inicializados correctamente");
    }
}