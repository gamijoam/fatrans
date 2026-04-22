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
}