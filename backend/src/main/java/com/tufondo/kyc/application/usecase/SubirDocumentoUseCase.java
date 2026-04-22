// com.tufondo.kyc.application.usecase.SubirDocumentoUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.request.SubirDocumentoRequest;
import com.tufondo.kyc.application.dto.response.SubirDocumentoResponse;
import com.tufondo.kyc.domain.exception.DocumentoMaliciosoException;
import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import com.tufondo.kyc.domain.model.port.MalwareScannerPort;
import com.tufondo.kyc.domain.model.port.StoragePort;
import com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Use case para subir un documento.
 * Implementa validaciones de seguridad: magic number, tamano, formato, malware.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubirDocumentoUseCase {

    private final DocumentoIdentidadRepository documentoRepository;
    private final VerificacionKYCRepository verificacionRepository;
    private final StoragePort storagePort;
    private final MalwareScannerPort malwareScannerPort;

    private static final Set<String> FORMATOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final long MAX_TAMANO_BYTES = 10 * 1024 * 1024; // 10MB
    private static final Set<String> MAGIC_NUMBERS_VALIDOS = Set.of("FFD8FF", "89504E47", "25504446");

    public SubirDocumentoResponse ejecutar(SubirDocumentoRequest request, UUID socioId) {

        // 1. Validar tamano
        if (request.getTamanoBytes() > MAX_TAMANO_BYTES) {
            throw new com.tufondo.kyc.domain.exception.DocumentoExcedeTamanoException(
                "El archivo excede el tamano maximo de 10MB");
        }

        // 2. Validar formato MIME
        if (!FORMATOS_PERMITIDOS.contains(request.getMimeType().toLowerCase())) {
            throw new com.tufondo.kyc.domain.exception.DocumentoFormatoInvalidoException(
                "Formato no permitido. Use JPEG, PNG o PDF.");
        }

        // 3. Decodificar Base64 y validar magic number
        byte[] archivoBytes;
        try {
            archivoBytes = java.util.Base64.getDecoder().decode(request.getArchivoBase64());
        } catch (IllegalArgumentException e) {
            throw new com.tufondo.kyc.domain.exception.DocumentoFormatoInvalidoException("Base64 invalido");
        }

        // 4. Validar que el tamano decodificado coincida
        if (archivoBytes.length != request.getTamanoBytes().intValue()) {
            throw new com.tufondo.kyc.domain.exception.DocumentoExcedeTamanoException(
                "El tamano declarado no coincide con el archivo");
        }

        // 5. Validar tamano decodificado
        if (archivoBytes.length > MAX_TAMANO_BYTES) {
            throw new com.tufondo.kyc.domain.exception.DocumentoExcedeTamanoException(
                "El archivo decodificado excede 10MB");
        }

        // 6. Validar magic number
        String magicNumber = getMagicNumber(archivoBytes);
        if (!MAGIC_NUMBERS_VALIDOS.contains(magicNumber)) {
            throw new com.tufondo.kyc.domain.exception.DocumentoFormatoInvalidoException(
                "El archivo no es un JPEG, PNG o PDF valido");
        }

        // 6.5. Escanear malware con ClamAV
        MalwareScannerPort.EscaneoResult resultadoEscaneo = malwareScannerPort.escanear(
            archivoBytes, request.getNombreOriginal());
        if (!resultadoEscaneo.limpio()) {
            log.warn("Malware detectado en archivo {}: {}", request.getNombreOriginal(), resultadoEscaneo.amenazaDetectada());
            throw new DocumentoMaliciosoException(resultadoEscaneo.mensajeError());
        }

        // 7. Validar verificacion existe y es editable
        VerificacionKYC verificacion = verificacionRepository.findById(request.getVerificacionId())
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(request.getVerificacionId()));

        if (!verificacion.getSocioId().equals(socioId)) {
            throw new com.tufondo.kyc.domain.exception.AccesoNoAutorizadoException("No tiene acceso a esta verificacion");
        }

        if (!verificacion.esEditable()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no esta en un estado editable");
        }

        // 8. Validar tipo de documento segun nivel
        validarTipoDocumentoPermitido(verificacion.getNivel(), request.getTipoDocumento());

        // 9. Verificar que no existe documento del mismo tipo
        boolean yaExiste = documentoRepository.existsByVerificacionIdAndTipo(
            request.getVerificacionId(), request.getTipoDocumento());
        if (yaExiste) {
            throw new com.tufondo.kyc.domain.exception.DocumentoDuplicadoException(
                "Ya existe un documento de tipo " + request.getTipoDocumento());
        }

        // 10. Subir archivo a storage
        String pathArchivo = String.format("kyc/%s/%s/%s",
            socioId,
            request.getVerificacionId(),
            UUID.randomUUID());

        StoragePort.UploadResult uploadResult = storagePort.upload(pathArchivo, archivoBytes, request.getMimeType());

        // 11. Crear documento
        DocumentoIdentidad documento = DocumentoIdentidad.builder()
            .verificacionId(request.getVerificacionId())
            .socioId(socioId)
            .tipoDocumento(request.getTipoDocumento())
            .urlAlmacenamiento(uploadResult.urlAlmacenamiento())
            .nombreOriginal(request.getNombreOriginal())
            .tamanoBytes(request.getTamanoBytes())
            .mimeType(request.getMimeType())
            .hashArchivo(uploadResult.hashArchivo())
            .fechaSubida(LocalDateTime.now())
            .fechaExpiracionDocumento(request.getFechaExpiracionDocumento())
            .estado(EstadoDocumento.PENDIENTE)
            .build();

        documento = documentoRepository.save(documento);

        return SubirDocumentoResponse.builder()
            .documentoId(documento.getId())
            .tipoDocumento(documento.getTipoDocumento())
            .nombreOriginal(documento.getNombreOriginal())
            .estado(documento.getEstado())
            .mensaje("Documento subido exitosamente")
            .build();
    }

    private String getMagicNumber(byte[] bytes) {
        if (bytes.length < 4) return "";
        return String.format("%02X%02X%02X", bytes[0], bytes[1], bytes[2]);
    }

    private void validarTipoDocumentoPermitido(NivelVerificacion nivel, TipoDocumentoKYC tipo) {
        List<TipoDocumentoKYC> permitidos = switch (nivel) {
            case BASICO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO
            );
            case MEDIO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO,
                TipoDocumentoKYC.RIF_NIT,
                TipoDocumentoKYC.CONSTANCIA_TRABAJO
            );
            case COMPLETO -> List.of(TipoDocumentoKYC.values());
        };

        if (!permitidos.contains(tipo)) {
            throw new com.tufondo.kyc.domain.exception.TipoDocumentoNoPermitidoException(
                "El tipo de documento " + tipo + " no esta permitido para KYC " + nivel);
        }
    }
}