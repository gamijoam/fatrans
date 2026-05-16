// com.tufondo.kyc.domain.model.port.StoragePort
package com.tufondo.kyc.domain.model.port;

/**
 * Puerto para el servicio de almacenamiento de documentos.
 */
public interface StoragePort {

    record UploadResult(
        String urlAlmacenamiento,
        String hashArchivo
    ) {}

    UploadResult upload(String path, byte[] data, String mimeType);

    void delete(String path);

    String generatePresignedUrl(String path, int expirationMinutes);

    boolean exists(String path);

    /**
     * Descarga el contenido del archivo. Usado por el endpoint del backend
     * que sirve documentos al frontend admin sin exponer URLs del MinIO
     * interno (`fatrans-minio:9000` no resuelve desde el browser).
     */
    byte[] download(String path);
}