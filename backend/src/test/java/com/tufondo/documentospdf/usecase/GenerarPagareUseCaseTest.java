// com.tufondo.documentospdf.usecase.GenerarPagareUseCaseTest
package com.tufondo.documentospdf.usecase;

import com.tufondo.core.port.CreditoQueryPort;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.application.usecase.GenerarPagareUseCase;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.EscaneoMalwareException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import com.tufondo.documentospdf.domain.exception.FirmaDigitalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GenerarPagareUseCase.
 * Los pagarés incluyen firma digital RSA SHA-256.
 */
@ExtendWith(MockitoExtension.class)
class GenerarPagareUseCaseTest {

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
    private GenerarPagareUseCase useCase;

    private UUID creditoId;
    private UUID socioId;
    private byte[] pdfBytesMock;
    private Map<String, Object> datosCreditoMock;
    private Map<String, Object> datosSocioMock;

    @BeforeEach
    void setUp() {
        creditoId = TestDataFactory.CREDITO_ID;
        socioId = TestDataFactory.SOCIO_ID;
        pdfBytesMock = "PDF_PAGARE_MOCK_CONTENT".getBytes();

        Map<String, Object> datosPagare = TestDataFactory.crearDatosPagare();
        datosCreditoMock = (Map<String, Object>) datosPagare.get("credito");
        datosSocioMock = (Map<String, Object>) datosPagare.get("socio");

        ReflectionTestUtils.setField(useCase, "clavePrivadaBase64", null);
    }

    @Test
    @DisplayName("Debería generar pagaré con firma digital real")
    void ejecutar_conKeystoreReal_firmaDigitalValida() {
        // Arrange
        ReflectionTestUtils.setField(useCase, "clavePrivadaBase64", TestDataFactory.TEST_PRIVATE_KEY_BASE64);

        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(TestDataFactory.crearTablaAmortizacionPrueba());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.PAGARE), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/pagare");
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
        DocumentoResponseDTO response = useCase.ejecutar(creditoId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.firmaDigital()).isNotNull();
        assertThat(response.firmaDigital()).startsWith("RSA-SHA256:");
        assertThat(response.firmaDigital().length()).isGreaterThan(50);

        verify(documentoRepository).guardar(any(Documento.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando crédito no existe")
    void ejecutar_conCreditoNoExistente_lanzaExcepcion() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessageContaining("Crédito no encontrado");

        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando malware es detectado")
    void ejecutar_conMalwareDetectado_lanzaEscaneoMalwareException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(TestDataFactory.crearTablaAmortizacionPrueba());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.PAGARE), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultMalicious());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId))
                .isInstanceOf(EscaneoMalwareException.class)
                .hasMessageContaining("malicioso");

        verify(storagePort, never()).upload(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando keystore no está configurado (p2)")
    void ejecutar_sinKeystore_parte2_lanzaFirmaDigitalException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(TestDataFactory.crearTablaAmortizacionPrueba());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.PAGARE), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando keystore no está configurado (p3)")
    void ejecutar_sinKeystore_parte3_lanzaFirmaDigitalException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(TestDataFactory.crearTablaAmortizacionPrueba());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.PAGARE), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }

    @Test
    @DisplayName("Debería lanzar FirmaDigitalException cuando keystore no está configurado (p4)")
    void ejecutar_sinKeystore_parte4_lanzaFirmaDigitalException() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(TestDataFactory.crearTablaAmortizacionPrueba());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.PAGARE), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("DOC_005");
    }
}
