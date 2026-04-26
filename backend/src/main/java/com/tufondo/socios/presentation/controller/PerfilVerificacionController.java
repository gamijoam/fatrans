package com.tufondo.socios.presentation.controller;

import com.tufondo.socios.application.dto.ConfirmarCodigoRequestDTO;
import com.tufondo.socios.application.dto.ConfirmarCodigoResponseDTO;
import com.tufondo.socios.application.dto.EnviarCodigoRequestDTO;
import com.tufondo.socios.application.dto.VerificarPasswordRequestDTO;
import com.tufondo.socios.application.dto.VerificarPasswordResponseDTO;
import com.tufondo.socios.application.usecase.VerificacionUseCase;
import com.tufondo.auth.domain.exception.CredencialesInvalidasException;
import com.tufondo.auth.infrastructure.service.VerificacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
public class PerfilVerificacionController {

    private final VerificacionUseCase verificacionUseCase;

    @PostMapping("/verificar-password")
    public ResponseEntity<VerificarPasswordResponseDTO> verificarPassword(
            @Valid @RequestBody VerificarPasswordRequestDTO request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        UUID usuarioId = getUsuarioId(authentication);

        try {
            VerificarPasswordResponseDTO response = verificacionUseCase.verificarPassword(
                    usuarioId, request, httpRequest);
            return ResponseEntity.ok(response);
        } catch (CredencialesInvalidasException e) {
            throw e;
        }
    }

    @PostMapping("/enviar-codigo")
    public ResponseEntity<Map<String, String>> enviarCodigo(
            @Valid @RequestBody EnviarCodigoRequestDTO request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        UUID usuarioId = getUsuarioId(authentication);

        String token = verificacionUseCase.enviarCodigo(usuarioId, request, httpRequest);

        return ResponseEntity.ok(Map.of(
                "token" , token,
                "mensaje", "Código enviado exitosamente"
        ));
    }

    @PostMapping("/confirmar-codigo")
    public ResponseEntity<ConfirmarCodigoResponseDTO> confirmarCodigo(
            @Valid @RequestBody ConfirmarCodigoRequestDTO request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        UUID usuarioId = getUsuarioId(authentication);

        try {
            ConfirmarCodigoResponseDTO response = verificacionUseCase.confirmarCodigo(
                    usuarioId, request, httpRequest);
            return ResponseEntity.ok(response);
        } catch (VerificacionUseCase.CodigoInvalidoException e) {
            throw new CredencialesInvalidasException(e.getMessage());
        } catch (VerificacionService.ExcesoIntentosException e) {
            return ResponseEntity.badRequest().body(
                    ConfirmarCodigoResponseDTO.builder()
                            .valido(false)
                            .tokenVerificacion(null)
                            .build()
            );
        }
    }

    @PostMapping("/validar-token")
    public ResponseEntity<Map<String, Boolean>> validarToken(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        UUID usuarioId = getUsuarioId(authentication);
        String token = body.get("token");

        boolean valido = verificacionUseCase.validarToken(usuarioId, token);

        return ResponseEntity.ok(Map.of("valido", valido));
    }

    private UUID getUsuarioId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}