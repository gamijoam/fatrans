package com.tufondo.auth.infrastructure.service;

import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.infrastructure.configuration.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generarAccessToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(
                jwtProperties.accessToken().expirationMinutes() * 60L
        );

        return Jwts.builder()
                .subject(usuario.id().toString())
                .claim("nombre_usuario", usuario.nombreUsuario())
                .claim("correo", usuario.correoElectronico())
                .claim("rol", usuario.rol().name())
                .claim("tipo_token", "ACCESS")
                .claim("socio_id", usuario.socioId() != null ? usuario.socioId().toString() : null)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(secretKey)
                .compact();
    }

    public String generarRefreshToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(
                jwtProperties.refreshToken().expirationDays() * 24 * 60 * 60L
        );

        return Jwts.builder()
                .subject(usuario.id().toString())
                .claim("tipo_token", "REFRESH")
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(secretKey)
                .compact();
    }

    public Claims validarToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            throw e;
        }
    }

    public boolean esAccessTokenValido(String token) {
        try {
            Claims claims = validarToken(token);
            return "ACCESS".equals(claims.get("tipo_token", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean esRefreshTokenValido(String token) {
        try {
            Claims claims = validarToken(token);
            return "REFRESH".equals(claims.get("tipo_token", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public UUID extraerUsuarioId(String token) {
        Claims claims = validarToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extraerRol(String token) {
        Claims claims = validarToken(token);
        return claims.get("rol", String.class);
    }

    public UUID extraerSocioId(String token) {
        Claims claims = validarToken(token);
        String socioIdStr = claims.get("socio_id", String.class);
        return socioIdStr != null ? UUID.fromString(socioIdStr) : null;
    }

    public Instant extraerExpiracionAccessToken(String token) {
        Claims claims = validarToken(token);
        return claims.getExpiration().toInstant();
    }
}
