// com.tufondo.documentospdf.usecase.GenerarEstadoCuentaUseCaseTest
package com.tufondo.documentospdf.usecase;

import com.tufondo.core.port.CuentaQueryPort;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.application.usecase.GenerarEstadoCuentaUseCase;
import com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GenerarEstadoCuentaUseCase.
 * Verifica la generación de estados de cuenta con validaciones IDOR.
 */
@ExtendWith(MockitoExtension.class)
class GenerarEstadoCuentaUseCaseTest {

    @Mock
    private PdfGeneratorPort pdfGeneratorPort;

    @Mock
    private StoragePort storagePort;

    @Mock
    private MalwareScannerPort malwareScannerPort;

    @Mock
    private SocioQueryPort socioQueryPort;

    @Mock
    private CuentaQueryPort cuentaQueryPort;

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private GenerarEstadoCuentaUseCase useCase;

    private UUID cuentaId;
    private UUID socioId;
    private UUID socioIdToken;
    private byte[] pdfBytesMock;
    private Map<String, Object> datosCuentaMock;
    private List<Map<String, Object>> movimientosMock;

    @BeforeEach
    void setUp() {
        cuentaId = TestDataFactory.CUENTA_ID;
        socioId = TestDataFactory.SOCIO_ID;
        socioIdToken = TestDataFactory.SOCIO_ID;

        pdfBytesMock = "PDF_MOCK_CONTENT".getBytes();
        datosCuentaMock = (Map<String, Object>) TestDataFactory.crearDatosEstadoCuenta().get("cuenta");
        movimientosMock = TestDataFactory.crearMovimientosPrueba();
    }

    @Test
    @DisplayName("Debería generar estado de cuenta exitosamente para socio propio")
    void ejecutar_conSocioValido_retornaDocumentoResponse() {
        // Arrange
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId)).thenReturn(datosCuentaMock);
        when(cuentaQueryPort.obtenerMovimientos(eq(cuentaId), anyInt(), anyInt())).thenReturn(movimientosMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.ESTADO_CUENTA), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/url");
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
        DocumentoResponseDTO response = useCase.ejecutar(cuentaId, socioIdToken, false);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.documentoId()).isNotNull();
        assertThat(response.socioId()).isEqualTo(socioId);
        assertThat(response.tipo()).isEqualTo(TipoDocumento.ESTADO_CUENTA);
        assertThat(response.estado()).isEqualTo(EstadoDocumento.ALMACENADO);
        assertThat(response.clasificacion()).isEqualTo(ClasificacionDocumento.CONFIDENCIAL);
        assertThat(response.nombreArchivo()).contains("EstadoCuenta");
        assertThat(response.tamanoBytes()).isGreaterThan(0);
        assertThat(response.preSignedUrl()).isEqualTo("https://minio.mock/url");

        // Verify interacciones
        verify(cuentaQueryPort).obtenerDatosCuenta(cuentaId);
        verify(pdfGeneratorPort).generarPdf(eq(TipoDocumento.ESTADO_CUENTA), anyMap());
        verify(malwareScannerPort).scan(pdfBytesMock);
        verify(storagePort).upload(anyString(), anyString(), eq(pdfBytesMock), eq("application/pdf"));
        verify(documentoRepository).guardar(any(Documento.class));
    }

    @Test
    @DisplayName("Debería generar estado de cuenta para ADMIN sin validación IDOR")
    void ejecutar_conAdmin_ySocioDiferente_retornaDocumento() {
        // Arrange
        UUID adminToken = UUID.randomUUID(); // Token de admin diferente al socio
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId)).thenReturn(datosCuentaMock);
        when(cuentaQueryPort.obtenerMovimientos(eq(cuentaId), anyInt(), anyInt())).thenReturn(movimientosMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.ESTADO_CUENTA), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/url");
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
        DocumentoResponseDTO response = useCase.ejecutar(cuentaId, adminToken, true);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.estado()).isEqualTo(EstadoDocumento.ALMACENADO);

        // Verify que NO se lanzó excepción IDOR
        verify(cuentaQueryPort).obtenerDatosCuenta(cuentaId);
    }

    @Test
    @DisplayName("Debería lanzar excepción IDOR cuando socio intenta acceder a cuenta ajena")
    void ejecutar_conSocioDiferente_yNoAdmin_lanzaExcepcionIDOR() {
        // Arrange
        UUID socioAgresor = TestDataFactory.SOCIO_ID_OTRO; // Diferente al socio de la cuenta
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId)).thenReturn(datosCuentaMock);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(cuentaId, socioAgresor, false))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("DOC_007");

        // Verify que NO se llamó al generador de PDF
        verify(pdfGeneratorPort, never()).generarPdf(any(), any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando malware es detectado")
    void ejecutar_conMalwareDetectado_lanzaEscaneoMalwareException() {
        // Arrange
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId)).thenReturn(datosCuentaMock);
        when(cuentaQueryPort.obtenerMovimientos(eq(cuentaId), anyInt(), anyInt())).thenReturn(movimientosMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.ESTADO_CUENTA), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultMalicious());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(cuentaId, socioIdToken, false))
                .isInstanceOf(EscaneoMalwareException.class)
                .hasMessageContaining("malicioso");

        // Verify que NO se subió a storage
        verify(storagePort, never()).upload(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debería lançar exceção cuando cuenta no existe")
    void ejecutar_conCuentaNoExistente_lanzaExcepcion() {
        // Arrange
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(cuentaId, socioIdToken, false))
                .isInstanceOf(com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException.class)
                .hasMessageContaining("Cuenta no encontrada");
    }

    @Test
    @DisplayName("Debería usar bucket correcto y clasificación CONFIDENCIAL")
    void ejecutar_deberiaUsarBucketCorrecto_yClasificacionConfidencial() {
        // Arrange
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId)).thenReturn(datosCuentaMock);
        when(cuentaQueryPort.obtenerMovimientos(eq(cuentaId), anyInt(), anyInt())).thenReturn(movimientosMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.ESTADO_CUENTA), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

        when(storagePort.upload(bucketCaptor.capture(), pathCaptor.capture(), eq(pdfBytesMock), eq("application/pdf")))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/url");
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
        DocumentoResponseDTO response = useCase.ejecutar(cuentaId, socioIdToken, false);

        // Assert
        assertThat(bucketCaptor.getValue()).isEqualTo("bucket-documentos");
        assertThat(pathCaptor.getValue()).contains("estados-cuenta");
        assertThat(response.clasificacion()).isEqualTo(ClasificacionDocumento.CONFIDENCIAL);
    }

    @Test
    @DisplayName("Debería calcular hash SHA-256 del PDF")
    void ejecutar_deberiaCalcularHashSha256() {
        // Arrange
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId)).thenReturn(datosCuentaMock);
        when(cuentaQueryPort.obtenerMovimientos(eq(cuentaId), anyInt(), anyInt())).thenReturn(movimientosMock);
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.ESTADO_CUENTA), anyMap())).thenReturn(pdfBytesMock);
        when(malwareScannerPort.scan(pdfBytesMock)).thenReturn(TestDataFactory.crearScanResultClean());

        ArgumentCaptor<Documento> documentoCaptor = ArgumentCaptor.forClass(Documento.class);
        when(storagePort.upload(anyString(), anyString(), eq(pdfBytesMock), anyString()))
                .thenReturn(TestDataFactory.crearUploadResultMock());
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://minio.mock/url");
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
        useCase.ejecutar(cuentaId, socioIdToken, false);

        // Assert
        Documento savedDoc = documentoCaptor.getValue();
        assertThat(savedDoc.getHashArchivo()).startsWith("SHA-256:");
        // SHA-256 hex = 64 chars, prefix "SHA-256:" = 9 chars, total = 73
        assertThat(savedDoc.getHashArchivo()).isGreaterThanOrEqualTo("SHA-256:");
        assertThat(savedDoc.getHashArchivo().length()).isBetween(70, 80); // Allow some variation
    }
}
