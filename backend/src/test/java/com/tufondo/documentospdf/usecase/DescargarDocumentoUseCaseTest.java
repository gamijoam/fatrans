// com.tufondo.documentospdf.usecase.DescargarDocumentoUseCaseTest
package com.tufondo.documentospdf.usecase;

import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.application.dto.DescargarDocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.application.usecase.DescargarDocumentoUseCase;
import com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.documentospdf.domain.exception.DocumentoExpiradoException;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.DocumentoRevocadoException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DescargarDocumentoUseCase.
 * Verifica la descarga de documentos con validaciones de estado y IDOR.
 */
@ExtendWith(MockitoExtension.class)
class DescargarDocumentoUseCaseTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private DescargarDocumentoUseCase useCase;

    private UUID documentoId;
    private UUID socioId;
    private UUID socioIdToken;
    private Documento documentoMock;

    @BeforeEach
    void setUp() {
        documentoId = TestDataFactory.DOCUMENTO_ID;
        socioId = TestDataFactory.SOCIO_ID;
        socioIdToken = TestDataFactory.SOCIO_ID;

        ReflectionTestUtils.setField(useCase, "presignedUrlExpirationMinutes", 15);

        documentoMock = Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.ESTADO_CUENTA)
                .estado(EstadoDocumento.ALMACENADO)
                .nombreArchivo("EstadoCuenta_2026-04.pdf")
                .rutaAlmacenamiento("estados-cuenta/" + socioId + "/EstadoCuenta_2026-04.pdf")
                .hashArchivo("SHA-256:abc123")
                .tamanoBytes(45872L)
                .fechaGeneracion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusDays(7))
                .generadoPor(socioId.toString())
                .clasificacion(ClasificacionDocumento.CONFIDENCIAL)
                .build();
    }

    @Test
    @DisplayName("Debería descargar documento exitosamente para socio propio")
    void ejecutar_conDocumentoValido_ySocioPropio_retornaPreSignedUrl() {
        // Arrange
        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoMock));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://minio.mock/bucket/estados-cuenta/" + socioId + "/EstadoCuenta_2026-04.pdf");

        // Act
        DescargarDocumentoResponseDTO response = useCase.ejecutar(documentoId, socioIdToken, false);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.documentoId()).isEqualTo(documentoId);
        assertThat(response.preSignedUrl()).isEqualTo("https://minio.mock/bucket/estados-cuenta/" + socioId + "/EstadoCuenta_2026-04.pdf");
        assertThat(response.urlExpiraEn()).isEqualTo(900); // 15 minutos * 60 segundos
        assertThat(response.fechaExpiracion()).isNotNull();

        verify(storagePort).generatePresignedUrl(
                eq("bucket-documentos"),
                contains("estados-cuenta"),
                eq(15)
        );
    }

    @Test
    @DisplayName("Debería descargar documento para ADMIN sin validación IDOR")
    void ejecutar_conAdmin_ySocioDiferente_retornaPreSignedUrl() {
        // Arrange
        UUID adminToken = UUID.randomUUID(); // Token de admin diferente al socio
        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoMock));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://minio.mock/pre-signed-url");

        // Act
        DescargarDocumentoResponseDTO response = useCase.ejecutar(documentoId, adminToken, true);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.preSignedUrl()).isNotNull();
    }

    @Test
    @DisplayName("Debería lanzar excepción IDOR cuando socio intenta descargar documento ajeno")
    void ejecutar_conSocioDiferente_yNoAdmin_lanzaExcepcionIDOR() {
        // Arrange
        UUID socioAgresor = TestDataFactory.SOCIO_ID_OTRO; // Diferente al socio del documento
        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoMock));

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(documentoId, socioAgresor, false))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("DOC_007");

        verify(storagePort, never()).generatePresignedUrl(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando documento no existe")
    void ejecutar_conDocumentoNoExistente_lanzaExcepcion() {
        // Arrange
        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(documentoId, socioIdToken, false))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessageContaining("Documento no encontrado");
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando documento está expirado")
    void ejecutar_conDocumentoExpirado_lanzaExcepcion() {
        // Arrange
        Documento documentoExpirado = Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.ESTADO_CUENTA)
                .estado(EstadoDocumento.EXPIRADO)
                .nombreArchivo("EstadoCuenta_2026-03.pdf")
                .rutaAlmacenamiento("estados-cuenta/" + socioId + "/EstadoCuenta_2026-03.pdf")
                .hashArchivo("SHA-256:xyz789")
                .tamanoBytes(45000L)
                .fechaGeneracion(LocalDateTime.now().minusDays(10))
                .fechaExpiracion(LocalDateTime.now().minusDays(3))
                .generadoPor(socioId.toString())
                .clasificacion(ClasificacionDocumento.CONFIDENCIAL)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoExpirado));

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(documentoId, socioIdToken, false))
                .isInstanceOf(DocumentoExpiradoException.class)
                .hasMessageContaining("DOC_002");

        verify(storagePort, never()).generatePresignedUrl(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando documento está revocado")
    void ejecutar_conDocumentoRevocado_lanzaExcepcion() {
        // Arrange
        Documento documentoRevocado = Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.CONTRATO_ADHESION)
                .estado(EstadoDocumento.REVOCADO)
                .nombreArchivo("Contrato_2026.pdf")
                .rutaAlmacenamiento("contratos/" + socioId + "/Contrato_2026.pdf")
                .hashArchivo("SHA-256:revocado")
                .tamanoBytes(50000L)
                .fechaGeneracion(LocalDateTime.now())
                .generadoPor("ADMIN")
                .clasificacion(ClasificacionDocumento.RESTRINGIDO)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoRevocado));

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(documentoId, socioIdToken, false))
                .isInstanceOf(DocumentoRevocadoException.class)
                .hasMessageContaining("DOC_003");

        verify(storagePort, never()).generatePresignedUrl(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando documento aún está en GENERADO")
    void ejecutar_conDocumentoEnGeneracion_lanzaExcepcion() {
        // Arrange
        Documento documentoEnGeneracion = Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.ESTADO_CUENTA)
                .estado(EstadoDocumento.GENERADO)
                .nombreArchivo("EstadoCuenta_2026-04.pdf")
                .rutaAlmacenamiento("estados-cuenta/" + socioId + "/EstadoCuenta_2026-04.pdf")
                .hashArchivo(null)
                .tamanoBytes(null)
                .fechaGeneracion(LocalDateTime.now())
                .generadoPor(socioId.toString())
                .clasificacion(ClasificacionDocumento.CONFIDENCIAL)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoEnGeneracion));

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(documentoId, socioIdToken, false))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessageContaining("aún en proceso");

        verify(storagePort, never()).generatePresignedUrl(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debería usar bucket correcto según tipo de documento")
    void ejecutar_deberiaUsarBucketCorrectoSegunTipo() {
        // Arrange - Documento de tipo CONTRATO_ADHESION
        Documento documentoContrato = Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.CONTRATO_ADHESION)
                .estado(EstadoDocumento.ALMACENADO)
                .nombreArchivo("Contrato_2026.pdf")
                .rutaAlmacenamiento("contratos/" + socioId + "/Contrato_2026.pdf")
                .hashArchivo("SHA-256:contrato")
                .tamanoBytes(60000L)
                .fechaGeneracion(LocalDateTime.now())
                .generadoPor("ADMIN")
                .clasificacion(ClasificacionDocumento.RESTRINGIDO)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoContrato));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://minio.mock/contrato");

        // Act
        useCase.ejecutar(documentoId, socioIdToken, false);

        // Assert
        verify(storagePort).generatePresignedUrl(
                eq("bucket-contratos"),
                contains("contratos"),
                eq(15)
        );
    }

    @Test
    @DisplayName("Debería usar bucket-pagares para PAGARE")
    void ejecutar_conPagare_deberiaUsarBucketPagares() {
        // Arrange
        Documento documentoPagare = Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.PAGARE)
                .estado(EstadoDocumento.ALMACENADO)
                .nombreArchivo("Pagare_123.pdf")
                .rutaAlmacenamiento("pagares/" + socioId + "/Pagare_123.pdf")
                .hashArchivo("SHA-256:pagare")
                .tamanoBytes(55000L)
                .fechaGeneracion(LocalDateTime.now())
                .generadoPor("SYSTEM")
                .clasificacion(ClasificacionDocumento.RESTRINGIDO)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoPagare));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://minio.mock/pagare");

        // Act
        useCase.ejecutar(documentoId, socioIdToken, false);

        // Assert
        verify(storagePort).generatePresignedUrl(
                eq("bucket-pagares"),
                contains("pagares"),
                eq(15)
        );
    }

    @Test
    @DisplayName("Debería usar bucket-creditos para TABLA_AMORTIZACION")
    void ejecutar_conTablaAmortizacion_deberiaUsarBucketCreditos() {
        // Arrange
        Documento documentoTabla = Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.TABLA_AMORTIZACION)
                .estado(EstadoDocumento.ALMACENADO)
                .nombreArchivo("TablaAmortizacion_456.pdf")
                .rutaAlmacenamiento("tablas-amortizacion/" + socioId + "/TablaAmortizacion_456.pdf")
                .hashArchivo("SHA-256:tabla")
                .tamanoBytes(35000L)
                .fechaGeneracion(LocalDateTime.now())
                .generadoPor(socioId.toString())
                .clasificacion(ClasificacionDocumento.CONFIDENCIAL)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(Optional.of(documentoTabla));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://minio.mock/tabla");

        // Act
        useCase.ejecutar(documentoId, socioIdToken, false);

        // Assert
        verify(storagePort).generatePresignedUrl(
                eq("bucket-creditos"),
                contains("tablas-amortizacion"),
                eq(15)
        );
    }
}
