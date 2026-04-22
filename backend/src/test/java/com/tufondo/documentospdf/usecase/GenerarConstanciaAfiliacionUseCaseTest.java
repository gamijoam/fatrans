// com.tufondo.documentospdf.usecase.GenerarConstanciaAfiliacionUseCaseTest
package com.tufondo.documentospdf.usecase;

import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.application.usecase.GenerarConstanciaAfiliacionUseCase;
import com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.EscaneoMalwareException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GenerarConstanciaAfiliacionUseCase.
 * Verifica la generación de constancias con validaciones IDOR.
 */
@ExtendWith(MockitoExtension.class)
class GenerarConstanciaAfiliacionUseCaseTest {

    @Mock
    private PdfGeneratorPort pdfGeneratorPort;

    @Mock
    private StoragePort storagePort;

    @Mock
    private MalwareScannerPort malwareScannerPort;

    @Mock
    private SocioQueryPort socioQueryPort;

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private GenerarConstanciaAfiliacionUseCase useCase;

    private UUID socioId;
    private UUID socioIdToken;
    private byte[] pdfBytesMock;
    private Map<String, Object> datosSocioMock;

    @BeforeEach
    void setUp() {
        socioId = TestDataFactory.SOCIO_ID;
        socioIdToken = TestDataFactory.SOCIO_ID;
        pdfBytesMock = "PDF_CONSTANCIA_MOCK".getBytes();
        datosSocioMock = (Map<String, Object>) TestDataFactory.crearDatosConstanciaAfiliacion().get("socio");
    }

    @Test
    @DisplayName("Debería generar constancia de afiliación exitosamente para socio propio")
    void ejecutar_conSocioValido_retornaDocumentoResponse() {
        // Arrange
        when(socioQueryPort.existeSocio(socioId)).thenReturn(true);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/constancia");
        when(documentoRepository.guardar(any(Documento.class))).thenAnswer(invocation -> {
            Documento doc = invocation.getArgument(0);
            return Documento.builder()
                    .id(UUID.randomUUID())
                    .socioId(doc.getSocioId())
                    .tipo(doc.getTipo())
                    .estado(doc.getEstado())
                    .nombreArchivo(doc.getNombreArchivo())
                    .rutaAlmacenamiento(doc.getRutaAlmacenamiento())
                    .hashArchivo(doc.getHashArchivo())
                    .tamanoBytes(doc.getTamanoBytes())
                    .fechaGeneracion(doc.getFechaGeneracion())
                    .fechaExpiracion(doc.getFechaExpiracion())
                    .generadoPor(doc.getGeneradoPor())
                    .clasificacion(doc.getClasificacion())
                    .build();
        });

        // Act
        DocumentoResponseDTO response = useCase.ejecutar(socioId, socioIdToken, false);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.documentoId()).isNotNull();
        assertThat(response.socioId()).isEqualTo(socioId);
        assertThat(response.tipo()).isEqualTo(TipoDocumento.CONSTANCIA_AFILIACION);
        assertThat(response.estado()).isEqualTo(EstadoDocumento.ALMACENADO);
        assertThat(response.clasificacion()).isEqualTo(ClasificacionDocumento.PUBLICO);
        assertThat(response.nombreArchivo()).contains("ConstanciaAfiliacion");

        // Verify
        verify(socioQueryPort).existeSocio(socioId);
        verify(socioQueryPort).obtenerDatosSocioParaPdf(socioId);
        verify(pdfGeneratorPort).generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap());
        verify(malwareScannerPort).scan(pdfBytesMock);
        verify(storagePort).upload(anyString(), anyString(), eq(pdfBytesMock), eq("application/pdf"));
    }

    @Test
    @DisplayName("Debería generar constancia para ADMIN sin validación IDOR")
    void ejecutar_conAdmin_ySocioDiferente_retornaDocumento() {
        // Arrange
        UUID adminToken = UUID.randomUUID();
        when(socioQueryPort.existeSocio(socioId)).thenReturn(true);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/constancia");
        when(documentoRepository.guardar(any(Documento.class))).thenAnswer(invocation -> {
            Documento doc = invocation.getArgument(0);
            return Documento.builder()
                    .id(UUID.randomUUID())
                    .socioId(doc.getSocioId())
                    .tipo(doc.getTipo())
                    .estado(doc.getEstado())
                    .nombreArchivo(doc.getNombreArchivo())
                    .rutaAlmacenamiento(doc.getRutaAlmacenamiento())
                    .hashArchivo(doc.getHashArchivo())
                    .tamanoBytes(doc.getTamanoBytes())
                    .fechaGeneracion(doc.getFechaGeneracion())
                    .fechaExpiracion(doc.getFechaExpiracion())
                    .generadoPor(doc.getGeneradoPor())
                    .clasificacion(doc.getClasificacion())
                    .build();
        });

        // Act
        DocumentoResponseDTO response = useCase.ejecutar(socioId, adminToken, true);

        // Assert
        assertThat(response).isNotNull();
        verify(socioQueryPort).existeSocio(socioId);
    }

    @Test
    @DisplayName("Debería lanzar excepción IDOR cuando socio intenta generar constancia ajena")
    void ejecutar_conSocioDiferente_yNoAdmin_lanzaExcepcionIDOR() {
        // Arrange
        UUID socioAgresor = TestDataFactory.SOCIO_ID_OTRO;

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(socioId, socioAgresor, false))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("DOC_007");

        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
        verify(storagePort, never()).upload(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando socio no existe")
    void ejecutar_conSocioNoExistente_lanzaExcepcion() {
        // Arrange
        when(socioQueryPort.existeSocio(socioId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(socioId, socioIdToken, false))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessageContaining("Socio no encontrado");

        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando malware es detectado")
    void ejecutar_conMalwareDetectado_lanzaEscaneoMalwareException() {
        // Arrange
        when(socioQueryPort.existeSocio(socioId)).thenReturn(true);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultMalicious());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(socioId, socioIdToken, false))
                .isInstanceOf(EscaneoMalwareException.class);

        verify(storagePort, never()).upload(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debería usar clasificación PUBLICO para constancia")
    void ejecutar_deberiaUsarClasificacionPublico() {
        // Arrange
        when(socioQueryPort.existeSocio(socioId)).thenReturn(true);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        ArgumentCaptor<Documento> documentoCaptor = ArgumentCaptor.forClass(Documento.class);
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/constancia");
        when(documentoRepository.guardar(documentoCaptor.capture())).thenAnswer(invocation -> {
            Documento doc = invocation.getArgument(0);
            return Documento.builder()
                    .id(UUID.randomUUID())
                    .socioId(doc.getSocioId())
                    .tipo(doc.getTipo())
                    .estado(doc.getEstado())
                    .nombreArchivo(doc.getNombreArchivo())
                    .rutaAlmacenamiento(doc.getRutaAlmacenamiento())
                    .hashArchivo(doc.getHashArchivo())
                    .tamanoBytes(doc.getTamanoBytes())
                    .fechaGeneracion(doc.getFechaGeneracion())
                    .fechaExpiracion(doc.getFechaExpiracion())
                    .generadoPor(doc.getGeneradoPor())
                    .clasificacion(doc.getClasificacion())
                    .build();
        });

        // Act
        useCase.ejecutar(socioId, socioIdToken, false);

        // Assert
        Documento savedDoc = documentoCaptor.getValue();
        assertThat(savedDoc.getClasificacion()).isEqualTo(ClasificacionDocumento.PUBLICO);
    }

    @Test
    @DisplayName("Debería guardar documento con ruta correcta en bucket-documentos")
    void ejecutar_deberiaGuardarConRutaCorrecta() {
        // Arrange
        when(socioQueryPort.existeSocio(socioId)).thenReturn(true);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        when(storagePort.upload(eq("bucket-documentos"), pathCaptor.capture(), eq(pdfBytesMock), eq("application/pdf")))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/constancia");
        when(documentoRepository.guardar(any(Documento.class))).thenAnswer(invocation -> {
            Documento doc = invocation.getArgument(0);
            return Documento.builder()
                    .id(UUID.randomUUID())
                    .socioId(doc.getSocioId())
                    .tipo(doc.getTipo())
                    .estado(doc.getEstado())
                    .nombreArchivo(doc.getNombreArchivo())
                    .rutaAlmacenamiento(doc.getRutaAlmacenamiento())
                    .hashArchivo(doc.getHashArchivo())
                    .tamanoBytes(doc.getTamanoBytes())
                    .fechaGeneracion(doc.getFechaGeneracion())
                    .fechaExpiracion(doc.getFechaExpiracion())
                    .generadoPor(doc.getGeneradoPor())
                    .clasificacion(doc.getClasificacion())
                    .build();
        });

        // Act
        useCase.ejecutar(socioId, socioIdToken, false);

        // Assert
        String path = pathCaptor.getValue();
        assertThat(path).contains("constancias");
        assertThat(path).contains(socioId.toString());
        assertThat(path).endsWith(".pdf");
    }
}
