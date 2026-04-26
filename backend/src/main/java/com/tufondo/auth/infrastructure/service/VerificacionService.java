package com.tufondo.auth.infrastructure.service;

import com.tufondo.auth.infrastructure.persistence.entity.VerificacionTokenEntity;
import com.tufondo.auth.infrastructure.persistence.repository.VerificacionTokenRepository;
import com.tufondo.socios.domain.model.enums.TipoVerificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificacionService {

    private static final int TTL_MINUTES = 5;
    private static final int MAX_INTENTOS = 3;
    private static final String CARACTERES_CODIGO = "0123456789";

    private final VerificacionTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecurityAuditService auditService;

    public boolean verificarPasswordUsuario(UUID usuarioId, String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }

    @Transactional
    public String generarTokenVerificacion(UUID usuarioId, String ipAddress, String userAgent) {
        String token = UUID.randomUUID().toString();

        VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                .token(token)
                .usuarioId(usuarioId)
                .tipo(TipoVerificacion.EMAIL)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .intentos(0)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        tokenRepository.save(entity);

        auditService.registrarIntentoVerificacion(usuarioId, "PASSWORD_VERIFIED", true, ipAddress);

        return token;
    }

    @Transactional
    public String generarYCEnviarCodigo(UUID usuarioId, TipoVerificacion tipo, String valor,
                                         String ipAddress, String userAgent, String emailDestino) {
        String codigo = generarCodigo(6);

        Optional<VerificacionTokenEntity> existente = tokenRepository
                .findByUsuarioIdAndTipoAndUsedFalseAndExpiresAtAfter(usuarioId, tipo, Instant.now());

        existente.ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();

        VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                .token(token)
                .usuarioId(usuarioId)
                .tipo(tipo)
                .valor(valor)
                .codigo(codigo)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .intentos(0)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        tokenRepository.save(entity);

        if (tipo == TipoVerificacion.EMAIL) {
            emailService.enviarCodigoVerificacion(emailDestino, codigo);
        } else if (tipo == TipoVerificacion.SMS) {
            log.info("========= SMS MOCK: CÓDIGO DE VERIFICACIÓN =========");
            log.info("Para: {}", valor);
            log.info("Código: {}", codigo);
            log.info("===================================================");
        }

        auditService.registrarIntentoVerificacion(usuarioId, "CODIGO_ENVIADO_" + tipo.name(), true, ipAddress);

        return token;
    }

    @Transactional
    public boolean confirmarCodigo(UUID usuarioId, String token, String codigo,
                                   String ipAddress, String userAgent) {
        Optional<VerificacionTokenEntity> entityOpt = tokenRepository.findByTokenAndUsedFalse(token);

        if (entityOpt.isEmpty()) {
            auditService.registrarIntentoVerificacion(usuarioId, "CODIGO_CONFIRM_FAIL_NO_TOKEN", false, ipAddress);
            return false;
        }

        VerificacionTokenEntity entity = entityOpt.get();

        if (!entity.getUsuarioId().equals(usuarioId)) {
            auditService.registrarIntentoVerificacion(usuarioId, "CODIGO_CONFIRM_FAIL_USER_MISMATCH", false, ipAddress);
            return false;
        }

        if (entity.getExpiresAt().isBefore(Instant.now())) {
            auditService.registrarIntentoVerificacion(usuarioId, "CODIGO_CONFIRM_FAIL_EXPIRED", false, ipAddress);
            return false;
        }

        if (entity.getIntentos() >= MAX_INTENTOS) {
            auditService.registrarIntentoVerificacion(usuarioId, "CODIGO_CONFIRM_FAIL_MAX_INTENTOS", false, ipAddress);
            throw new ExcesoIntentosException("Superaste el número máximo de intentos");
        }

        if (!entity.getCodigo().equals(codigo)) {
            entity.setIntentos(entity.getIntentos() + 1);
            tokenRepository.save(entity);
            auditService.registrarIntentoVerificacion(usuarioId, "CODIGO_CONFIRM_FAIL_WRONG_CODE", false, ipAddress);
            return false;
        }

        entity.setUsed(true);
        tokenRepository.save(entity);

        auditService.registrarIntentoVerificacion(usuarioId, "CODIGO_CONFIRMED", true, ipAddress);

        return true;
    }

    public boolean validarTokenVerificacion(UUID usuarioId, String token) {
        Optional<VerificacionTokenEntity> entityOpt = tokenRepository.findByTokenAndUsedFalse(token);

        if (entityOpt.isEmpty()) {
            return false;
        }

        VerificacionTokenEntity entity = entityOpt.get();

        if (!entity.getUsuarioId().equals(usuarioId)) {
            return false;
        }

        if (entity.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        return true;
    }

    @Transactional
    public void invalidarToken(UUID usuarioId, String token) {
        Optional<VerificacionTokenEntity> entityOpt = tokenRepository.findByTokenAndUsedFalse(token);

        entityOpt.ifPresent(entity -> {
            entity.setUsed(true);
            tokenRepository.save(entity);
        });
    }

    private String generarCodigo(int longitud) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(CARACTERES_CODIGO.charAt(random.nextInt(CARACTERES_CODIGO.length())));
        }
        return sb.toString();
    }

    public static class ExcesoIntentosException extends RuntimeException {
        public ExcesoIntentosException(String message) {
            super(message);
        }
    }
}