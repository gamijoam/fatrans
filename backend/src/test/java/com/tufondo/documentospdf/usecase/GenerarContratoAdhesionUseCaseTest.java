// com.tufondo.documentospdf.usecase.GenerarContratoAdhesionUseCaseTest
package com.tufondo.documentospdf.usecase;

import com.tufondo.core.port.CreditoQueryPort;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.application.usecase.GenerarContratoAdhesionUseCase;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.EscaneoMalwareException;
import com.tufondo.documentospdf.domain.exception.FirmaDigitalException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GenerarContratoAdhesionUseCase.
 * Los contratos incluyen firma digital RSA SHA-256.
 * Nota: Para tests que requieren firma real, ver DocumentoPdfIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class GenerarContratoAdhesionUseCaseTest {

    @Mock
    private PdfGeneratorPort pdfGeneratorPort;

    @Mock
    private StoragePort storagePort;

    @Mock
    private MalwareScannerPort malwareScannerPort;

    @Mock
    private CreditoQueryPort creditoQueryPort;

    @Mock
    private SocioQueryPort socioQueryPort;

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private GenerarContratoAdhesionUseCase useCase;

    private UUID solicitudId;
    private UUID socioId;
    private byte[] pdfBytesMock;
    private Map<String, Object> datosSolicitudMock;
    private Map<String, Object> datosSocioMock;

    @BeforeEach
    void setUp() throws Exception {
        solicitudId = TestDataFactory.SOLICITUD_ID;
        socioId = TestDataFactory.SOCIO_ID;
        pdfBytesMock = "PDF_CONTRATO_MOCK_CONTENT".getBytes();

        Map<String, Object> datosContrato = TestDataFactory.crearDatosContratoAdhesion();
        datosSolicitudMock = (Map<String, Object>) datosContrato.get("solicitud");
        datosSocioMock = (Map<String, Object>) datosContrato.get("socio");

        ReflectionTestUtils.setField(useCase, "clavePrivadaBase64", null);
    }

    @Test
    @DisplayName("Debería generar contrato con firma digital real")
    void ejecutar_conKeystoreReal_firmaDigitalValida() {
        // Arrange
        ReflectionTestUtils.setField(useCase, "clavePrivadaBase64", TestDataFactory.TEST_PRIVATE_KEY_BASE64);

        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(datosSolicitudMock);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONTRATO_ADHESION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(new StoragePort.UploadResult("bucket-contratos", "contratos/" + socioId + "/Contrato.pdf", 60000L, "etag456"));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/contrato");
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
                    .firmaDigital(doc.getFirmaDigital())
                    .build();
        });

        // Act
        DocumentoResponseDTO response = useCase.ejecutar(solicitudId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.firmaDigital()).isNotNull();
        assertThat(response.firmaDigital()).startsWith("RSA-SHA256:");
        assertThat(response.firmaDigital().length()).isGreaterThan(50);

        // Verify que se intentó guardar
        verify(documentoRepository).guardar(any(Documento.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando solicitud no existe")
    void ejecutar_conSolicitudNoExistente_lanzaExcepcion() {
        // Arrange
        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(solicitudId))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessageContaining("Solicitud no encontrada");

        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando malware es detectado")
    void ejecutar_conMalwareDetectado_lanzaEscaneoMalwareException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(datosSolicitudMock);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONTRATO_ADHESION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultMalicious());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(solicitudId))
                .isInstanceOf(EscaneoMalwareException.class)
                .hasMessageContaining("malicioso");

        verify(storagePort, never()).upload(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando clave no está configurada")
    void ejecutar_sinKeystore_lanzaFirmaDigitalException() {
        // Arrange - clavePrivadaBase64 ya es null en setUp()
        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(datosSolicitudMock);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONTRATO_ADHESION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert - mensaje genérico para no exponer configuración interna
        assertThatThrownBy(() -> useCase.ejecutar(solicitudId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando clave no está configurada (p2)")
    void ejecutar_sinKeystore_parte2_lanzaFirmaDigitalException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(datosSolicitudMock);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONTRATO_ADHESION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(solicitudId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando clave no está configurada (p3)")
    void ejecutar_sinKeystore_parte3_lanzaFirmaDigitalException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(datosSolicitudMock);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONTRATO_ADHESION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(solicitudId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando clave no está configurada (p4)")
    void ejecutar_sinKeystore_parte4_lanzaFirmaDigitalException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(datosSolicitudMock);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONTRATO_ADHESION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(solicitudId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando clave no está configurada (p5)")
    void ejecutar_sinKeystore_parte5_lanzaFirmaDigitalException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosSolicitud(solicitudId)).thenReturn(datosSolicitudMock);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONTRATO_ADHESION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(solicitudId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }
}
