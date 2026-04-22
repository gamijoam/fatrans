// 📁 com/tufondo/socios/application/usecase/ActualizarSocioUseCase.java
// 🔧 SECURITY FIX: Eliminada lógica de cambio de estado/roles via DTO (previene Mass Assignment)
// La gestión de estado se realiza exclusivamente via use cases dedicados (Activar/Desactivar/Eliminar)
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.ActualizarSocioDTO;
import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.application.validation.ValidadorCorreoElectronico;
import com.tufondo.socios.application.validation.ValidadorTelefono;
import com.tufondo.socios.domain.exception.*;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ActualizarSocioUseCase {

    private final SocioRepository socioRepository;
    private final ValidadorCorreoElectronico validadorCorreo;
    private final ValidadorTelefono validadorTelefono;
    private final SocioDTOMapper dtoMapper;

    @Transactional
    public SocioResponseDTO ejecutar(java.util.UUID id, ActualizarSocioDTO dto) {

        Socio socio = socioRepository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id.toString()));

        // ── 1. Validar reglas de negocio de actualización ──
        validarReglasDeNegocio(socio, dto);

        // ── 2. Aplicar cambios al dominio ──
        aplicarCambios(socio, dto);

        // ── 3. Persistir y responder ──
        Socio actualizado = socioRepository.guardar(socio);
        return dtoMapper.toResponseDTO(actualizado);
    }

    private void validarReglasDeNegocio(Socio socio, ActualizarSocioDTO dto) {

        // ── Correo: verificar que no esté en uso por otro socio ──
        if (dto.getCorreoElectronico() != null && !dto.getCorreoElectronico().isBlank()) {
            String correoNormalizado = validadorCorreo.validarYNormalizar(dto.getCorreoElectronico());
            socioRepository.buscarPorCorreo(correoNormalizado)
                    .filter(s -> !s.getId().equals(socio.getId()))
                    .ifPresent(s -> { throw new CorreoYaRegistradoException(correoNormalizado); });
        }

        // 🔧 BUG-04 FIX: Validar cambio de numeroDocumento
        // Si se proporciona un nuevo numeroDocumento, verificar que no exista
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().isBlank()) {
            String nuevoNumero = dto.getNumeroDocumento().trim();
            if (!nuevoNumero.equals(socio.getNumeroDocumento())) {
                if (socioRepository.existePorNumeroDocumento(nuevoNumero)) {
                    throw new NumeroDocumentoYaRegistradoException(nuevoNumero);
                }
            }
        }

        // 🔧 BUG-05 FIX: Validar consistencia tipoDocumento + numeroDocumento
        // Si se cambia solo tipoDocumento (sin cambiar numeroDocumento), es inconsistente
        if (dto.getTipoDocumento() != null && dto.getNumeroDocumento() == null) {
            // Usuario quiere cambiar tipoDocumento pero NO proporciona numeroDocumento
            if (!dto.getTipoDocumento().equals(socio.getTipoDocumento())) {
                throw new IllegalArgumentException(
                        "Para cambiar el tipo de documento debe proporcionar también el nuevo número de documento");
            }
        }
        // Si se cambia solo numeroDocumento (sin cambiar tipoDocumento), OK
        // Si se cambian ambos, OK
        // Si se cambian tipoDocumento y numeroDocumento juntos, OK

        // ── Validar fecha de nacimiento no sea futura ──
        if (dto.getFechaNacimiento() != null
                && dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new FechaNacimientoInvalidaException();
        }

        // ── Teléfono principal ──
        if (dto.getTelefonoPrincipal() != null && !dto.getTelefonoPrincipal().isBlank()) {
            validadorTelefono.validarYNormalizar(dto.getTelefonoPrincipal());
        }

        // ── Teléfono secundario ──
        if (dto.getTelefonoSecundario() != null && !dto.getTelefonoSecundario().isBlank()) {
            validadorTelefono.validarYNormalizar(dto.getTelefonoSecundario());
        }
    }

    private void aplicarCambios(Socio socio, ActualizarSocioDTO dto) {

        if (dto.getPrimerNombre() != null) socio.setPrimerNombre(dto.getPrimerNombre().trim());
        if (dto.getSegundoNombre() != null)
            socio.setSegundoNombre(dto.getSegundoNombre().isBlank() ? null : dto.getSegundoNombre().trim());
        if (dto.getPrimerApellido() != null) socio.setPrimerApellido(dto.getPrimerApellido().trim());
        if (dto.getSegundoApellido() != null)
            socio.setSegundoApellido(dto.getSegundoApellido().isBlank() ? null : dto.getSegundoApellido().trim());

        // 🔧 BUG-05 FIX: Actualizar tipo y número de documento juntos
        if (dto.getTipoDocumento() != null) socio.setTipoDocumento(dto.getTipoDocumento());
        if (dto.getNumeroDocumento() != null) {
            socio.setNumeroDocumento(dto.getNumeroDocumento().trim());
        }

        if (dto.getGenero() != null) socio.setGenero(dto.getGenero());
        if (dto.getFechaNacimiento() != null) socio.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getEstadoCivil() != null) socio.setEstadoCivil(dto.getEstadoCivil());

        if (dto.getCorreoElectronico() != null) {
            socio.setCorreoElectronico(validadorCorreo.validarYNormalizar(dto.getCorreoElectronico()));
        }
        if (dto.getTelefonoPrincipal() != null) {
            socio.setTelefonoPrincipal(validadorTelefono.validarYNormalizar(dto.getTelefonoPrincipal()));
        }
        if (dto.getTelefonoSecundario() != null && !dto.getTelefonoSecundario().isBlank()) {
            socio.setTelefonoSecundario(validadorTelefono.validarYNormalizar(dto.getTelefonoSecundario()));
        } else if (dto.getTelefonoSecundario() != null) {
            socio.setTelefonoSecundario(null);
        }

        if (dto.getDireccionResidencia() != null) socio.setDireccionResidencia(dto.getDireccionResidencia());
        if (dto.getDireccionLaboral() != null) socio.setDireccionLaboral(dto.getDireccionLaboral());
        if (dto.getEmpresa() != null) socio.setEmpresa(dto.getEmpresa().trim());
        if (dto.getDepartamento() != null) socio.setDepartamento(dto.getDepartamento().trim());
        if (dto.getCargo() != null) socio.setCargo(dto.getCargo().trim());
        if (dto.getTipoContrato() != null) socio.setTipoContrato(dto.getTipoContrato());
        if (dto.getNumeroCuentaNomina() != null) socio.setNumeroCuentaNomina(dto.getNumeroCuentaNomina());
        if (dto.getBancoNomina() != null) socio.setBancoNomina(dto.getBancoNomina());
        if (dto.getContactoEmergencia() != null) socio.setContactoEmergencia(dto.getContactoEmergencia());

        // SECURITY FIX: No se permite cambiar estado o roles via DTO
        // Estas operaciones están reservadas a use cases dedicados (Activar/Desactivar/Eliminar)
    }
}
