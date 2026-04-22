// com.tufondo.documentospdf.infrastructure.storage.MinIODocumentStorageService
package com.tufondo.documentospdf.infrastructure.storage;

import com.tufondo.documentospdf.application.port.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de almacenamiento en MinIO.
 * Implementa el puerto StoragePort.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinIODocumentStorageService implements StoragePort {

    private final MinioClient minioClient;

    @Value("${minio.buckets.documentos:bucket-documentos}")
    private String bucketDocumentos;

    @Value("${minio.buckets.contratos:bucket-contratos}")
    private String bucketContratos;

    @Value("${minio.buckets.pagares:bucket-pagares}")
    private String bucketPagares;

    @Value("${minio.buckets.creditos:bucket-creditos}")
    private String bucketCreditos;

    @Value("${minio.buckets.temporal:bucket-temporal}")
    private String bucketTemporal;

    @Override
    public UploadResult upload(String bucket, String path, byte[] data, String mimeType) {
        try {
            log.debug("Subiendo archivo a MinIO: bucket={}, path={}, size={}", bucket, path, data.length);

            // Asegurar que el bucket existe
            ensureBucketExists(bucket);

            // Subir archivo
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(bais, data.length, -1)
                            .contentType(mimeType)
                            .build()
            );

            // Obtener ETag
            StatObjectArgs statArgs = StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build();
            StatObjectResponse stat = minioClient.statObject(statArgs);

            log.info("Archivo subido exitosamente: bucket={}, path={}, etag={}", bucket, path, stat.etag());

            return new UploadResult(bucket, path, data.length, stat.etag());
        } catch (Exception e) {
            log.error("Error al subir archivo a MinIO: bucket={}, path={}", bucket, path, e);
            throw new RuntimeException("Error al subir archivo a MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucket, String path, int expirationMinutes) {
        try {
            log.debug("Generando pre-signed URL: bucket={}, path={}, expiration={}min",
                    bucket, path, expirationMinutes);

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(path)
                            .expiry(expirationMinutes, TimeUnit.MINUTES)
                            .build()
            );

            log.debug("Pre-signed URL generada: {}...", url.substring(0, Math.min(50, url.length())));

            return url;
        } catch (Exception e) {
            log.error("Error al generar pre-signed URL: bucket={}, path={}", bucket, path, e);
            throw new RuntimeException("Error al generar pre-signed URL: " + e.getMessage(), e);
        }
    }

    /**
     * Asegura que el bucket existe, créalo si no existe.
     */
    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                log.info("Bucket no existe, creando: {}", bucket);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Bucket creado: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Error al verificar/crear bucket: {}", bucket, e);
            throw new RuntimeException("Error al verificar bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el nombre del bucket según el tipo de documento.
     */
    public String getBucketPorTipo(String tipo) {
        return switch (tipo) {
            case "ESTADO_CUENTA", "CONSTANCIA_AFILIACION", "CARTA_BENEFICIARIOS" -> bucketDocumentos;
            case "CONTRATO_ADHESION" -> bucketContratos;
            case "PAGARE" -> bucketPagares;
            case "TABLA_AMORTIZACION" -> bucketCreditos;
            default -> bucketTemporal;
        };
    }
}
