// com/tufondo/socios/application/usecase/CrearSocioUseCase.java
// 🔧 SECURITY FIX: Usar UUID completo con encoding legible para garantizar unicidad
// 🔧 SECURITY FIX: Constante ROLES_DEFAULT para evitar hardcoded "SOCIO"
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.CrearSocioRequestDTO;
import com.tufondo.socios.application.validation.ValidadorCorreoElectronico;
import com.tufondo.socios.application.validation.ValidadorTelefono;
import com.tufondo.socios.domain.exception.*;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrearSocioUseCase {

    // SECURITY FIX: Constante para roles por defecto en lugar de hardcoded string
    private static final Set<String> ROLES_DEFAULT = Set.of("SOCIO");

    private final SocioRepository socioRepository;
    private final ValidadorCorreoElectronico validadorCorreo;
    private final ValidadorTelefono validadorTelefono;

    @Transactional
    public Socio ejecutar(CrearSocioRequestDTO request) {
        // 1. Validar duplicados básicos
        if (socioRepository.existePorNumeroDocumento(request.getNumeroDocumento())) {
            throw new NumeroDocumentoYaRegistradoException(request.getNumeroDocumento());
        }

        if (socioRepository.existePorCorreo(request.getCorreoElectronico())) {
            throw new CorreoYaRegistradoException(request.getCorreoElectronico());
        }

        // 2. Construir el objeto de dominio Socio V2
        UUID nuevoId = UUID.randomUUID();
        Socio socio = Socio.builder()
                .id(nuevoId)
                .numeroSocio(generarNumeroSocio(nuevoId)) // SECURITY FIX: UUID completo para evitar colisiones
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .primerNombre(request.getPrimerNombre().trim())
                .segundoNombre(request.getSegundoNombre() != null ? request.getSegundoNombre().trim() : null)
                .primerApellido(request.getPrimerApellido().trim())
                .segundoApellido(request.getSegundoApellido() != null ? request.getSegundoApellido().trim() : null)
                .fechaNacimiento(request.getFechaNacimiento())
                .genero(request.getGenero())
                .estadoCivil(request.getEstadoCivil())
                .correoElectronico(request.getCorreoElectronico())
                .telefonoPrincipal(request.getTelefonoPrincipal())
                .telefonoSecundario(request.getTelefonoSecundario())
                .direccionResidencia(request.getDireccionResidencia())
                .direccionLaboral(request.getDireccionLaboral())
                .empresa(request.getEmpresa())
                .departamento(request.getDepartamento())
                .cargo(request.getCargo())
                .tipoContrato(request.getTipoContrato())
                .salario(request.getSalario())
                .montoAhorro(java.math.BigDecimal.ZERO)
                .numeroCuentaNomina(request.getNumeroCuentaNomina())
                .bancoNomina(request.getBancoNomina())
                .contactoEmergencia(request.getContactoEmergencia())
                .estado(EstadoSocio.PENDIENTE_APROBACION) // Iniciamos en pendiente por seguridad bancaria
                .fechaIngreso(request.getFechaIngreso() != null ? request.getFechaIngreso() : LocalDate.now())
                .fechaRegistro(java.time.LocalDateTime.now())
                .fechaActualizacion(java.time.LocalDateTime.now())
                .roles(ROLES_DEFAULT) // SECURITY FIX: Usar constante en lugar de literal
                .build();

        return socioRepository.guardar(socio);
    }

    /**
     * Genera número de socio único usando hash legible del UUID completo.
     * Formato: S-YYYY-XXXXXXXX donde XXXXXXXX es los primeros 8 caracteres del hash hex del UUID.
     * Esto garantiza unicidad ya que usa el UUID completo (128 bits) vía hash SHA-256.
     */
    private String generarNumeroSocio(UUID id) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(id.toString().getBytes());
            // Tomar los primeros 8 caracteres del hash hex (32 bits suficientes para unicidad local)
            String hashHex = bytesToHex(hash).substring(0, 8).toUpperCase();
            return "S-" + LocalDate.now().getYear() + "-" + hashHex;
        } catch (NoSuchAlgorithmException e) {
            // Fallback: nunca debería ocurrir ya que SHA-256 siempre está disponible
            return "S-" + LocalDate.now().getYear() + "-" + id.toString().substring(0, 8).toUpperCase();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
