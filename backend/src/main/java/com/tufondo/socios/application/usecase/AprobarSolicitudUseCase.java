// 📁 com/tufondo/socios/application/usecase/AprobarSolicitudUseCase.java
package com.tufondo.socios.application.usecase;

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
        String[] partes = nombreCompleto.split(" ");
        
        String primerNombre = partes.length > 0 ? partes[0] : "";
        String segundoNombre = partes.length > 1 ? partes[1] : "";
        String primerApellido = partes.length > 2 ? partes[2] : "";
        String segundoApellido = partes.length > 3 ? partes[3] : "";
        
        Socio nuevoSocio = Socio.builder()
                .numeroSocio(generarNumeroSocio())
                .tipoDocumento(TipoDocumento.valueOf(solicitud.getCedula().startsWith("V") ? "V" : "E"))
                .numeroDocumento(solicitud.getCedula())
                .primerNombre(primerNombre)
                .segundoNombre(segundoNombre)
                .primerApellido(primerApellido)
                .segundoApellido(segundoApellido)
                .correoElectronico(solicitud.getCorreoElectronico())
                .telefonoPrincipal(solicitud.getTelefono())
                .empresa(solicitud.getEmpresa())
                .estado(EstadoSocio.ACTIVO)
                .fechaIngreso(LocalDate.now())
                .fechaRegistro(LocalDateTime.now())
                .fechaActivacion(LocalDateTime.now())
                .build();
        
        Socio socioGuardado = socioRepository.guardar(nuevoSocio);
        
        // 3. Generar nombre de usuario y password temporal
        String nombreUsuario = generarNombreUsuario(nombreCompleto);
        String passwordTemporal = generarPasswordTemporal();
        
        // 4. Crear el usuario vinculado usando el puerto
        usuarioCreatorPort.crearUsuarioVinculado(
                socioGuardado.getId(),
                nombreUsuario,
                solicitud.getCorreoElectronico(),
                passwordTemporal
        );
        
        // 5. Actualizar la solicitud
        solicitud.aprobar(adminId, request != null ? request.getComentario() : null);
        SolicitudRegistro solicitudActualizada = solicitudRepository.guardar(solicitud);
        
        // 6. Enviar email con credenciales
        emailNotificationService.enviarCredenciales(
                solicitud.getCorreoElectronico(),
                nombreUsuario,
                passwordTemporal
        );
        
        log.info("Solicitud {} aprobada. Socio {} creado con usuario {}",
                solicitudId, socioGuardado.getId(), nombreUsuario);
        
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
    
    private String generarNombreUsuario(String nombreCompleto) {
        // Juan Pérez García → juan.perez
        String[] partes = nombreCompleto.toLowerCase().split(" ");
        String nombre = partes[0];
        String apellido = partes.length > 1 ? partes[1].replaceAll("[^a-z]", "") : "";
        
        String base = nombre + "." + apellido;
        String nombreUsuario = base;
        
        int counter = 1;
        while (usuarioCreatorPort.existeNombreUsuario(nombreUsuario)) {
            nombreUsuario = base + counter;
            counter++;
        }
        
        return nombreUsuario;
    }
    
    private String generarPasswordTemporal() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}