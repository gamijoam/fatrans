// com.tufondo.kyc.infrastructure.validator.ClamAVAdapter
package com.tufondo.kyc.infrastructure.validator;

import com.tufondo.kyc.domain.exception.DocumentoMaliciosoException;
import com.tufondo.kyc.domain.model.port.MalwareScannerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Adapter para ClamAV antivirus.
 * Se conecta via TCP socket al daemon de ClamAV (typically localhost:3310).
 */
@Slf4j
@Component
public class ClamAVAdapter implements MalwareScannerPort {

    @Value("${clamav.host:localhost}")
    private String host;

    @Value("${clamav.port:3310}")
    private int port;

    @Value("${clamav.timeout-ms:60000}")
    private int timeoutMs;

    @Value("${clamav.enabled:true}")
    private boolean enabled;

    @Override
    public EscaneoResult escanear(byte[] datos, String nombreArchivo) {
        if (!enabled) {
            log.debug("ClamAV deshabilitado, saltando escaneo");
            return new EscaneoResult(true, null, null);
        }

        if (!estaDisponible()) {
            log.warn("ClamAV no esta disponible, se permitira upload con revision manual");
            return new EscaneoResult(true, null, "Antivirus no disponible");
        }

        try {
            boolean malicioso = escanearConClamAV(datos);
            if (malicioso) {
                log.warn("Malware detectado en archivo: {}", nombreArchivo);
                return new EscaneoResult(false, "MALWARE_DETECTADO",
                    "El archivo contiene malware y fue rechazado");
            }
            return new EscaneoResult(true, null, null);
        } catch (IOException e) {
            log.error("Error de IO al escanear con ClamAV: {}", e.getMessage());
            return new EscaneoResult(true, null, "Error en escaneo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al escanear: {}", e.getMessage(), e);
            return new EscaneoResult(true, null, "Error inesperado: " + e.getMessage());
        }
    }

    @Override
    public boolean estaDisponible() {
        if (!enabled) return false;

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(5000);
            return socket.isConnected();
        } catch (Exception e) {
            log.debug("ClamAV no disponible en {}:{}: {}", host, port, e.getMessage());
            return false;
        }
    }

    private boolean escanearConClamAV(byte[] datosArchivo) throws IOException {
        log.debug("Escaneando {} bytes con ClamAV en {}:{}", datosArchivo.length, host, port);

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(timeoutMs);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write("zINSTREAM\0".getBytes());
            out.flush();

            enviarDatos(out, datosArchivo);

            out.write(4);
            out.flush();

            StringBuilder respuesta = new StringBuilder();
            int b;
            while ((b = in.read()) != -1) {
                if (b == 0) break;
                respuesta.append((char) b);
            }

            String resultado = respuesta.toString().trim();
            log.debug("Respuesta ClamAV: {}", resultado);

            return resultado.contains("FOUND") || resultado.contains("Infected");
        }
    }

    private void enviarDatos(OutputStream out, byte[] datos) throws IOException {
        int offset = 0;

        while (offset < datos.length) {
            int length = Math.min(1024 * 1024, datos.length - offset);
            byte[] chunk = Arrays.copyOfRange(datos, offset, offset + length);

            ByteBuffer buffer = ByteBuffer.allocate(4 + length);
            buffer.putInt(length);
            buffer.put(chunk);

            out.write(buffer.array());
            out.flush();

            offset += length;
        }
    }
}