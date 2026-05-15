// 📁 com/tufondo/socios/application/usecase/AprobarSolicitudUseCase.java
package com.tufondo.socios.application.usecase;

import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import com.tufondo.socios.application.dto.AprobarSolicitudRequestDTO;
import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.exception.SolicitudNoEditableException;
import com.tufondo.socios.domain.exception.SolicitudNoEncontradaException;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import com.tufondo.socios.domain.repository.SocioRepository;
import com.tufondo.socios.domain.repository.SolicitudRegistroRepository;
import com.tufondo.core.port.UsuarioCreatorPort;
import com.tufondo.socios.infrastructure.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AprobarSolicitudUseCase {
    
    private final SolicitudRegistroRepository solicitudRepository;
    private final SocioRepository socioRepository;
    private final UsuarioCreatorPort usuarioCreatorPort;
    private final EmailNotificationService emailNotificationService;
    private final SolicitudRegistroDTOMapper dtoMapper;
    private final VerificacionKYCRepository verificacionKYCRepository;
    
    @Transactional
    public SolicitudRegistroResponseDTO ejecutar(UUID solicitudId, AprobarSolicitudRequestDTO request, String adminId) {
        // 1. Obtener y validar solicitud
        SolicitudRegistro solicitud = solicitudRepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new SolicitudNoEncontradaException(solicitudId));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new SolicitudNoEditableException("La solicitud no está pendiente");
        }

        // 2. Crear el Socio desde los datos de la solicitud
        String nombreCompleto = solicitud.getNombreCompleto();
        String[] partes = nombreCompleto.trim().split("\\s+");

        // Venezuelan name format: [primerNombre] [segundoNombre] [primerApellido] [segundoApellido]
        // Last two parts are always surnames (Venezuelan compound surnames)
        String primerApellido;
        String segundoApellido;
        String primerNombre;
        String segundoNombre;

        if (partes.length >= 2) {
            // Last two parts = surnames
            segundoApellido = partes[partes.length - 1];
            primerApellido = partes[partes.length - 2];
            // Everything before surnames = first name(s)
            if (partes.length == 2) {
                primerNombre = partes[0];
                segundoNombre = null;
            } else if (partes.length == 3) {
                primerNombre = partes[0];
                segundoNombre = null;
            } else {
                // partes.length >= 4: first part is primerNombre, second is segundoNombre
                primerNombre = partes[0];
                segundoNombre = partes[1];
            }
        } else {
            primerNombre = partes.length > 0 ? partes[0] : "";
            segundoNombre = null;
            primerApellido = "";
            segundoApellido = null;
        }

        Socio nuevoSocio = Socio.builder()
                .numeroSocio(generarNumeroSocio())
                .tipoDocumento(solicitud.getTipoDocumento() != null ? solicitud.getTipoDocumento() :
                        (solicitud.getCedula().startsWith("V") ? TipoDocumento.CEDULA : TipoDocumento.PASAPORTE))
                .numeroDocumento(solicitud.getCedula())
                .primerNombre(primerNombre)
                .segundoNombre(segundoNombre)
                .primerApellido(primerApellido)
                .segundoApellido(segundoApellido)
                .correoElectronico(solicitud.getCorreoElectronico())
                .telefonoPrincipal(solicitud.getTelefono())
                .empresa(solicitud.getEmpresa())
                .departamento(solicitud.getDepartamento())
                .cargo(solicitud.getCargo())
                .estado(EstadoSocio.ACTIVO)
                .estadoCivil(solicitud.getEstadoCivil())
                .genero(solicitud.getGenero())
                .fechaNacimiento(solicitud.getFechaNacimiento() != null ? solicitud.getFechaNacimiento() : LocalDate.of(1990, 1, 1))
                .fechaIngreso(LocalDate.now())
                .fechaRegistro(LocalDateTime.now())
                .fechaActivacion(LocalDateTime.now())
                .direccionResidencia(new com.tufondo.socios.domain.model.valueobjects.Direccion(
                        solicitud.getDireccionCalle(), null,
                        solicitud.getDireccionCiudad(), solicitud.getDireccionEstado(),
                        null, "Venezuela"))
                .contactoEmergencia(new com.tufondo.socios.domain.model.valueobjects.ContactoEmergencia(
                        solicitud.getEmergenciaNombre(), solicitud.getEmergenciaTelefono(),
                        solicitud.getEmergenciaParentesco()))
                .build();
        
        Socio socioGuardado = socioRepository.guardar(nuevoSocio);
        
        // 3. Auto-trigger KYC para el nuevo socio
        VerificacionKYC verificacionKYC = VerificacionKYC.builder()
                .socioId(socioGuardado.getId())
                .nivel(NivelVerificacion.BASICO)
                .estado(EstadoVerificacion.PENDIENTE)
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusYears(2))
                .build();
        verificacionKYCRepository.save(verificacionKYC);
        log.info("KYC auto-triggered for socio {}. VerificacionKYC {} created with estado PENDIENTE",
                socioGuardado.getId(), verificacionKYC.getId());
        
        // 4. Generar nombre de usuario y password temporal
        // Uses parsed names (primerNombre + primerApellido) to avoid the
        // off-by-one bug where partes[1] could be a second name instead of the surname.
        String nombreUsuario = generarNombreUsuario(primerNombre, primerApellido);
        String passwordTemporal = generarPasswordTemporal();
        
        // 5. Crear el usuario vinculado usando el puerto
        usuarioCreatorPort.crearUsuarioVinculado(
                socioGuardado.getId(),
                nombreUsuario,
                solicitud.getCorreoElectronico(),
                passwordTemporal
        );
        
        // 6. Actualizar la solicitud
        solicitud.aprobar(adminId, request != null ? request.getComentario() : null);
        SolicitudRegistro solicitudActualizada = solicitudRepository.guardar(solicitud);
        
        // 7. Enviar email con credenciales
        emailNotificationService.enviarCredenciales(
                solicitud.getCorreoElectronico(),
                nombreUsuario,
                passwordTemporal
        );
        
        log.info("Solicitud {} aprobada. Socio {} creado con usuario {}",
                solicitudId, socioGuardado.getId(), nombreUsuario);
        log.info("Credenciales enviadas por email para socioId={}, usuario={}",
                socioGuardado.getId(), nombreUsuario);

        return dtoMapper.toResponseDTO(solicitudActualizada);
    }
    
    private String generarNumeroSocio() {
        String numero;
        do {
            int random = ThreadLocalRandom.current().nextInt(100000, 999999);
            numero = "SA" + random;
        } while (socioRepository.existePorNumeroSocio(numero));
        return numero;
    }
    
    /**
     * Genera el username a partir de los nombres ya parseados.
     * Ejemplos (formato venezolano):
     *   ("María", "González")              → "maria.gonzalez"
     *   ("Juan",  "Pérez")                 → "juan.perez"
     *   ("María José", "González López")   → "maria.gonzalez"  (toma primeros tokens)
     * Si hay colisión, agrega sufijo numérico.
     */
    private String generarNombreUsuario(String primerNombre, String primerApellido) {
        String nombre = sanitizarParaUsername(primerNombre);
        String apellido = sanitizarParaUsername(primerApellido);

        String base = apellido.isEmpty()
                ? (nombre.isEmpty() ? "socio" : nombre)
                : nombre + "." + apellido;

        String nombreUsuario = base;
        int counter = 1;
        while (usuarioCreatorPort.existeNombreUsuario(nombreUsuario)) {
            nombreUsuario = base + counter;
            counter++;
        }

        return nombreUsuario;
    }

    private String sanitizarParaUsername(String parte) {
        if (parte == null) return "";
        // Toma el primer token, baja a minúsculas, remueve acentos y deja solo a-z0-9.
        String primerToken = parte.trim().split("\\s+")[0].toLowerCase();
        String sinAcentos = java.text.Normalizer.normalize(primerToken, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.replaceAll("[^a-z0-9]", "");
    }
    
    /**
     * Genera una contraseña temporal criptográficamente segura.
     * Usa SecureRandom (NO ThreadLocalRandom — éste es predecible).
     * Garantiza al menos 1 mayúscula, 1 minúscula, 1 dígito y 1 caracter especial,
     * con longitud total de 12 caracteres.
     */
    private String generarPasswordTemporal() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnpqrstuvwxyz";
        String numbers = "23456789";
        String special = "@$!%*?&";

        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(numbers.charAt(random.nextInt(numbers.length())));
        sb.append(special.charAt(random.nextInt(special.length())));

        String allChars = upper + lower + numbers + special;
        for (int i = 0; i < 8; i++) {
            sb.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}