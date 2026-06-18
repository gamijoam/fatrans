package com.tufondo.productos.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductoImagenStorageService {

    private static final Set<String> MIME_PERMITIDOS = Set.of("image/jpeg", "image/png");
    private static final int MAX_DIMENSION = 2400;

    private final MinioClient minioClient;

    @Value("${minio.buckets.productos:bucket-productos}")
    private String bucket;

    @Value("${fatrans.productos.imagenes.max-size-bytes:2097152}")
    private long maxSizeBytes;

    public String subirImagen(Long productoId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La imagen del producto es requerida");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("La imagen no puede superar 2 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !MIME_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato no permitido. Usa JPG o PNG");
        }

        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (original == null || original.getWidth() < 1 || original.getHeight() < 1) {
                throw new IllegalArgumentException("El archivo no es una imagen valida");
            }

            byte[] normalizada = normalizarJpeg(original);
            if (normalizada.length > maxSizeBytes) {
                throw new IllegalArgumentException("La imagen normalizada no puede superar 2 MB");
            }
            ensureBucketExists();

            String fecha = LocalDate.now().toString();
            String fileName = generarNombre(productoId);
            String objectPath = "productos/" + fecha + "/" + fileName;
            try (InputStream input = new ByteArrayInputStream(normalizada)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectPath)
                        .stream(input, normalizada.length, -1)
                        .contentType("image/jpeg")
                        .build()
                );
            }

            return "/api/v1/productos/imagenes/" + fecha + "/" + fileName;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo guardar la imagen del producto", ex);
        }
    }

    public ImagenProducto descargar(String fecha, String fileName) {
        String safeFecha = sanitizarFecha(fecha);
        String safeName = sanitizarFileName(fileName);
        String objectPath = "productos/" + safeFecha + "/" + safeName;
        try {
            ensureBucketExists();
            try (InputStream input = minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectPath)
                        .build())) {
                return new ImagenProducto(input.readAllBytes(), "image/jpeg");
            }
        } catch (ImagenNoEncontradaException ex) {
            throw ex;
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())) {
                throw new ImagenNoEncontradaException();
            }
            throw new RuntimeException("No se pudo leer la imagen del producto", ex);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo leer la imagen del producto", ex);
        }
    }

    private byte[] normalizarJpeg(BufferedImage original) throws Exception {
        int width = original.getWidth();
        int height = original.getHeight();
        double scale = Math.min(1.0, (double) MAX_DIMENSION / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        Image scaled = original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, targetWidth, targetHeight);
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(output, "jpg", baos);
        return baos.toByteArray();
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private String generarNombre(Long productoId) {
        byte[] random = new byte[12];
        new SecureRandom().nextBytes(random);
        return "producto-" + productoId + "-" + HexFormat.of().formatHex(random) + ".jpg";
    }

    private String sanitizarFileName(String fileName) {
        if (fileName == null || !fileName.matches("producto-[0-9]+-[a-f0-9]{24}\\.jpg")) {
            throw new IllegalArgumentException("Nombre de imagen invalido");
        }
        return fileName;
    }

    private String sanitizarFecha(String fecha) {
        if (fecha == null || !fecha.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException("Ruta de imagen invalida");
        }
        return fecha;
    }

    public record ImagenProducto(byte[] data, String contentType) {}

    public static class ImagenNoEncontradaException extends RuntimeException {}
}
