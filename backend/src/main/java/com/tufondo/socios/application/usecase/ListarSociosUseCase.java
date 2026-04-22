package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ListarSociosUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListarSociosUseCase.class);

    private final SocioRepository socioRepository;
    private final SocioDTOMapper dtoMapper;

    @Transactional(readOnly = true)
    public Page<Socio> ejecutar(Pageable pageable) {
        log.debug("Listando socios con paginación: {}", pageable);
        return socioRepository.listar(pageable);
    }
}
