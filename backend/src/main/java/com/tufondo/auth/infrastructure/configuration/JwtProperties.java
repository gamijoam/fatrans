// 📁 backend/src/main/java/com/tufondo/auth/infrastructure/configuration/JwtProperties.java
package com.tufondo.auth.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * Configuración de propiedades JWT.
 * Valida en startup que todas las propiedades requeridas estén configuradas correctamente.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        AccessTokenConfig accessToken,
        RefreshTokenConfig refreshToken,
        String issuer
) {
    
    public JwtProperties {
        Objects.requireNonNull(secret, 
            "jwt.secret es requerido. Configure jwt.secret en application.yml");
        Objects.requireNonNull(accessToken, 
            "jwt.accessToken es requerido. Configure jwt.accessToken en application.yml");
        Objects.requireNonNull(refreshToken, 
            "jwt.refreshToken es requerido. Configure jwt.refreshToken.en application.yml");
        Objects.requireNonNull(issuer, 
            "jwt.issuer es requerido. Configure jwt.issuer en application.yml");
        
        // Los nested records ya se validan en sus propios compact constructors
    }
    
    /**
     * Configuración del token de acceso.
     * @param expirationMinutes minutos hasta expiración (debe ser > 0)
     */
    public record AccessTokenConfig(int expirationMinutes) {
        public AccessTokenConfig {
            if (expirationMinutes <= 0) {
                throw new IllegalStateException(
                    "jwt.accessToken.expirationMinutes debe ser un número positivo, recibido: " 
                    + expirationMinutes);
            }
        }
    }
    
    /**
     * Configuración del token de refresco.
     * @param expirationDays días hasta expiración (debe ser > 0)
     */
    public record RefreshTokenConfig(int expirationDays) {
        public RefreshTokenConfig {
            if (expirationDays <= 0) {
                throw new IllegalStateException(
                    "jwt.refreshToken.expirationDays debe ser un número positivo, recibido: " 
                    + expirationDays);
            }
        }
    }
}