// com.tufondo.documentospdf.application.port.StoragePort
package com.tufondo.documentospdf.application.port;

/**
 * Puerto para almacenamiento de documentos en MinIO.
 */
public interface StoragePort {

    /**
     * Sube un archivo a MinIO.
     *
     * @param bucket nombre del bucket
     * @param path ruta dentro del bucket
     * @param data bytes del archivo
     * @param mimeType tipo MIME del archivo
     * @return UploadResult con información del upload
     */
    UploadResult upload(String bucket, String path, byte[] data, String mimeType);

    /**
     * Genera una URL pre-firmada para descarga.
     *
     * @param bucket nombre del bucket
     * @param path ruta dentro del bucket
     * @param expirationMinutes minutos hasta expiración
     * @return URL pre-firmada
     */
    String generatePresignedUrl(String bucket, String path, int expirationMinutes);

    /**
     * Resultado de una operación de upload.
     */
    record UploadResult(
        String bucket,
        String path,
        long tamanoBytes,
        String etag
    ) {}
}
