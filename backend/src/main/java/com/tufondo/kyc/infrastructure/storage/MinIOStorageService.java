// com.tufondo.kyc.infrastructure.storage.MinIOStorageService
package com.tufondo.kyc.infrastructure.storage;

import com.tufondo.kyc.domain.model.port.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementacion del servicio de almacenamiento usando MinIO/S3.
 * Implementa seguridad: pre-signed URLs, encriptacion, sanitizacion de paths.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOStorageService implements StoragePort {

    private final MinioClient minioClient;

    @Value("${storage.bucket.kyc:bucket-kyc}")
    private String bucketName;

    @Override
    public UploadResult upload(String path, byte[] data, String mimeType) {
        try {
            // Sanitizar path y validar
            String normalizedPath = normalizePath(path);
            if (normalizedPath.contains("..")) {
                throw new SecurityException("Path traversal attempt detected");
            }

            // Generar nombre de archivo aleatorio (no usar nombre original)
            String extension = getExtensionFromMimeType(mimeType);
            String fileName = UUID.randomUUID() + extension;
            String fullPath = normalizedPath + "/" + fileName;

            // Subir archivo
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .stream(bais, data.length, -1)
                    .contentType(mimeType)
                    .build()
            );

            // Calcular hash SHA-256
            String hash = calculateHash(data);

            String url = bucketName + "/" + fullPath;

            return new UploadResult(url, hash);

        } catch (Exception e) {
            log.error("Error uploading file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            String normalizedPath = normalizePath(path);
            if (normalizedPath.contains("..")) {
                throw new SecurityException("Path traversal attempt detected");
            }

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String path, int expirationMinutes) {
        try {
            String normalizedPath = normalizePath(path);
            if (normalizedPath.contains("..")) {
                throw new SecurityException("Path traversal attempt detected");
            }

            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .expiry(expirationMinutes, TimeUnit.MINUTES)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating presigned URL: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] download(String path) {
        try {
            // El path persistido en BD viene con prefijo "{bucket}/" porque
            // el upload lo guarda así. Lo removemos antes de pedir el objeto.
            String objectPath = path.startsWith(bucketName + "/")
                    ? path.substring(bucketName.length() + 1)
                    : path;
            String normalizedPath = normalizePath(objectPath);
            if (normalizedPath.contains("..")) {
                throw new SecurityException("Path traversal attempt detected");
            }
            try (var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(normalizedPath)
                            .build())) {
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            log.error("Error downloading file from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Error downloading file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            String normalizedPath = normalizePath(path);
            if (normalizedPath.contains("..")) {
                throw new SecurityException("Path traversal attempt detected");
            }

            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizePath(String path) {
        // Normalizar y validar el path
        return path.replaceAll("[/]+", "/")
            .replaceAll("^/", "")
            .replaceAll("/$", "");
    }

    private String getExtensionFromMimeType(String mimeType) {
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> ".bin";
        };
    }

    private String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}