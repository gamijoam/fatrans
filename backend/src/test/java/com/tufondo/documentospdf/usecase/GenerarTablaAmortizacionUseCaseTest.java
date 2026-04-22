// com.tufondo.documentospdf.usecase.GenerarTablaAmortizacionUseCaseTest
package com.tufondo.documentospdf.usecase;

import com.tufondo.core.port.CreditoQueryPort;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.application.usecase.GenerarTablaAmortizacionUseCase;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GenerarTablaAmortizacionUseCase.
 * Verifica la generación de tablas de amortización con validaciones IDOR.
 */
@ExtendWith(MockitoExtension.class)
class GenerarTablaAmortizacionUseCaseTest {

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
    private GenerarTablaAmortizacionUseCase useCase;

    private UUID creditoId;
    private UUID socioId;
    private UUID socioIdToken;
    private byte[] pdfBytesMock;
    private Map<String, Object> datosCreditoMock;
    private Map<String, Object> datosSocioMock;
    private List<Map<String, Object>> tablaAmortizacionMock;

    @BeforeEach
    void setUp() {
        creditoId = TestDataFactory.CREDITO_ID;
        socioId = TestDataFactory.SOCIO_ID;
        socioIdToken = TestDataFactory.SOCIO_ID;
        pdfBytesMock = "PDF_TABLA_AMORTIZACION_MOCK".getBytes();

        Map<String, Object> datosTabla = TestDataFactory.crearDatosTablaAmortizacion();
        datosCreditoMock = (Map<String, Object>) datosTabla.get("credito");
        datosSocioMock = (Map<String, Object>) datosTabla.get("socio");
        tablaAmortizacionMock = (List<Map<String, Object>>) datosTabla.get("tablaAmortizacion");
    }

    @Test
    @DisplayName("Debería generar tabla de amortización exitosamente para socio propio")
    void ejecutar_conCreditoValido_ySocioPropio_retornaDocumentoResponse() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(tablaAmortizacionMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.TABLA_AMORTIZACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/tabla");
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
        DocumentoResponseDTO response = useCase.ejecutar(creditoId, socioIdToken, false);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.documentoId()).isNotNull();
        assertThat(response.socioId()).isEqualTo(socioId);
        assertThat(response.tipo()).isEqualTo(TipoDocumento.TABLA_AMORTIZACION);
        assertThat(response.estado()).isEqualTo(EstadoDocumento.ALMACENADO);
        assertThat(response.clasificacion()).isEqualTo(ClasificacionDocumento.CONFIDENCIAL);
        assertThat(response.nombreArchivo()).contains("TablaAmortizacion");

        // Verify
        verify(creditoQueryPort).obtenerDatosCredito(creditoId);
        verify(creditoQueryPort).obtenerTablaAmortizacion(creditoId);
        verify(pdfGeneratorPort).generarPdf(eq(TipoDocumento.TABLA_AMORTIZACION), anyMap());
        verify(malwareScannerPort).scan(pdfBytesMock);
        verify(storagePort).upload(eq("bucket-creditos"), anyString(), eq(pdfBytesMock), eq("application/pdf"));
    }

    @Test
    @DisplayName("Debería generar tabla de amortización para ADMIN sin validación IDOR")
    void ejecutar_conAdmin_yCreditoDeOtroSocio_retornaDocumento() {
        // Arrange
        UUID adminToken = UUID.randomUUID();
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(tablaAmortizacionMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.TABLA_AMORTIZACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/tabla");
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
        DocumentoResponseDTO response = useCase.ejecutar(creditoId, adminToken, true);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.estado()).isEqualTo(EstadoDocumento.ALMACENADO);
    }

    @Test
    @DisplayName("Debería lanzar excepción IDOR cuando socio intenta acceder a crédito ajeno")
    void ejecutar_conSocioDiferente_yNoAdmin_lanzaExcepcionIDOR() {
        // Arrange
        UUID socioAgresor = TestDataFactory.SOCIO_ID_OTRO;
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId, socioAgresor, false))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("DOC_007");

        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
        verify(storagePort, never()).upload(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando crédito no existe")
    void ejecutar_conCreditoNoExistente_lanzaExcepcion() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId, socioIdToken, false))
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
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(tablaAmortizacionMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.TABLA_AMORTIZACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultMalicious());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(creditoId, socioIdToken, false))
                .isInstanceOf(EscaneoMalwareException.class)
                .hasMessageContaining("malicioso");

        verify(storagePort, never()).upload(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debería usar bucket-creditos para almacenar")
    void ejecutar_deberiaUsarBucketCreditos() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(tablaAmortizacionMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.TABLA_AMORTIZACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        verify(storagePort, never()).upload(eq("bucket-creditos"), anyString(), any(), anyString());

        when(storagePort.upload(eq("bucket-creditos"), anyString(), eq(pdfBytesMock), eq("application/pdf")))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/tabla");
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
        useCase.ejecutar(creditoId, socioIdToken, false);

        // Assert
        verify(storagePort).upload(eq("bucket-creditos"), contains("tablas-amortizacion"), eq(pdfBytesMock), eq("application/pdf"));
    }

    @Test
    @DisplayName("Debería generar tabla con clasificación CONFIDENCIAL")
    void ejecutar_deberiaUsarClasificacionConfidencial() {
        // Arrange
        when(creditoQueryPort.obtenerDatosCredito(creditoId)).thenReturn(datosCreditoMock);
        when(creditoQueryPort.obtenerSocioIdPorCredito(creditoId)).thenReturn(socioId);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId)).thenReturn(datosSocioMock);
        when(creditoQueryPort.obtenerTablaAmortizacion(creditoId)).thenReturn(tablaAmortizacionMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.TABLA_AMORTIZACION), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/tabla");

        when(documentoRepository.guardar(any(Documento.class))).thenAnswer(invocation -> {
            Documento doc = invocation.getArgument(0);
            assertThat(doc.getClasificacion()).isEqualTo(ClasificacionDocumento.CONFIDENCIAL);
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
        useCase.ejecutar(creditoId, socioIdToken, false);

        // Assert - la verificación se hace en el when
        verify(documentoRepository).guardar(any(Documento.class));
    }
}
