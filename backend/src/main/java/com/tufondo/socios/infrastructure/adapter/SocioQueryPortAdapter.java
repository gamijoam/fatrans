// Adaptador para el puerto SocioQueryPort usado por el módulo Documentos PDF
package com.tufondo.socios.infrastructure.adapter;

import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.socios.infrastructure.persistence.entity.SocioEntity;
import com.tufondo.socios.infrastructure.persistence.jpa.SocioJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link SocioQueryPort}.
 *
 * <p>Provee datos del módulo Socios al módulo Documentos PDF (constancia
 * de afiliación, contrato de adhesión, etc.) sin que ese módulo dependa
 * del modelo de dominio interno de {@code socios}.</p>
 *
 * <p><strong>Historia (#199):</strong> este adapter tenía un método
 * {@code obtenerSocioIdPorCuenta(UUID)} que era un stub sin callers
 * reales (lanzaba {@code OperacionNoDisponibleException}). Se eliminó
 * junto con la inner exception class — ver JavaDoc del puerto para la
 * justificación arquitectónica.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocioQueryPortAdapter implements SocioQueryPort {

    private final SocioJpaRepository socioJpaRepository;

    @Override
    public boolean existeSocio(UUID socioId) {
        log.debug("Verificando existencia de socio={}", socioId);
        return socioJpaRepository.existsById(socioId);
    }

    @Override
    public Map<String, Object> obtenerDatosSocioParaPdf(UUID socioId) {
        log.debug("Obteniendo datos de socio para PDF: socioId={}", socioId);

        Optional<SocioEntity> socioOpt = socioJpaRepository.findById(socioId);
        if (socioOpt.isEmpty()) {
            log.warn("Socio no encontrado: socioId={}", socioId);
            return null;
        }

        SocioEntity socio = socioOpt.get();
        Map<String, Object> datos = new HashMap<>();

        datos.put("id", socio.getId());
        datos.put("numeroSocio", socio.getNumeroSocio());
        datos.put("primerNombre", socio.getPrimerNombre());
        datos.put("segundoNombre", socio.getSegundoNombre());
        datos.put("primerApellido", socio.getPrimerApellido());
        datos.put("segundoApellido", socio.getSegundoApellido());
        datos.put("nombreCompleto", construirNombreCompleto(socio));
        datos.put("tipoDocumento", socio.getTipoDocumento() != null ? socio.getTipoDocumento().name() : null);
        datos.put("cedula", socio.getNumeroDocumento());
        datos.put("numeroDocumento", socio.getNumeroDocumento());
        datos.put("correoElectronico", socio.getCorreoElectronico());
        datos.put("telefonoPrincipal", socio.getTelefonoPrincipal());
        datos.put("telefonoSecundario", socio.getTelefonoSecundario());

        datos.put("empresa", socio.getEmpresa());
        datos.put("departamento", socio.getDepartamento());
        datos.put("cargo", socio.getCargo());
        datos.put("tipoContrato", socio.getTipoContrato() != null ? socio.getTipoContrato().name() : null);
        datos.put("salario", socio.getSalario());

        datos.put("estado", socio.getEstado() != null ? socio.getEstado().name() : null);
        datos.put("fechaRegistro", socio.getFechaRegistro());
        datos.put("fechaActivacion", socio.getFechaActivacion());

        log.debug("Datos de socio obtenidos exitosamente: {}", datos.keySet());
        return datos;
    }

    private String construirNombreCompleto(SocioEntity socio) {
        StringBuilder sb = new StringBuilder();
        if (socio.getPrimerNombre() != null) {
            sb.append(socio.getPrimerNombre());
        }
        if (socio.getSegundoNombre() != null && !socio.getSegundoNombre().isEmpty()) {
            sb.append(" ").append(socio.getSegundoNombre());
        }
        if (socio.getPrimerApellido() != null) {
            sb.append(" ").append(socio.getPrimerApellido());
        }
        if (socio.getSegundoApellido() != null && !socio.getSegundoApellido().isEmpty()) {
            sb.append(" ").append(socio.getSegundoApellido());
        }
        return sb.toString().trim();
    }
}
