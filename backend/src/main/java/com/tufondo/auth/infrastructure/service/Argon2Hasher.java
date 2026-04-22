package com.tufondo.auth.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class Argon2Hasher {

    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 512;
    private static final int SALT_LENGTH = 32;

    public String hash(String input) {
        try {
            byte[] salt = generateSalt();
            PBEKeySpec spec = new PBEKeySpec(
                    input.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error al hashear con PBKDF2, usando fallback SHA-256", e);
            return hashSha256Fallback(input);
        }
    }

    public boolean matches(String input, String storedHashWithSalt) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHashWithSalt);
            byte[] salt = new byte[SALT_LENGTH];
            byte[] storedHash = new byte[KEY_LENGTH / 8];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, storedHash, 0, storedHash.length);

            PBEKeySpec spec = new PBEKeySpec(
                    input.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] computedHash = factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(computedHash, storedHash);
        } catch (Exception e) {
            log.error("Error al verificar hash", e);
            return false;
        }
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private String hashSha256Fallback(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
