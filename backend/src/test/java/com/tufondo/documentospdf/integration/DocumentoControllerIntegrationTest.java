// com.tufondo.documentospdf.integration.DocumentoControllerIntegrationTest
package com.tufondo.documentospdf.integration;

import com.tufondo.core.port.CuentaQueryPort;
import com.tufondo.core.port.CreditoQueryPort;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para DocumentoController.
 * Usa @SpringBootTest con @MockBean para mockear dependencias externas.
 *
 * NOTA: Para ejecutar estos tests se necesita la base de datos de test
 * y configurar las propiedades de test en application-test.yml
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfGeneratorPort pdfGeneratorPort;

    @MockBean
    private StoragePort storagePort;

    @MockBean
    private MalwareScannerPort malwareScannerPort;

    @MockBean
    private SocioQueryPort socioQueryPort;

    @MockBean
    private CuentaQueryPort cuentaQueryPort;

    @MockBean
    private CreditoQueryPort creditoQueryPort;

    @MockBean
    private DocumentoRepository documentoRepository;

    private UUID socioId;
    private UUID cuentaId;
    private byte[] pdfBytesMock;

    @BeforeEach
    void setUp() {
        socioId = TestDataFactory.SOCIO_ID;
        cuentaId = TestDataFactory.CUENTA_ID;
        pdfBytesMock = "PDF_MOCK_CONTENT_FOR_API_TEST".getBytes();

        // Configurar mocks por defecto
        when(storagePort.upload(anyString(), anyString(), any(byte[].class), anyString()))
                .thenReturn(new StoragePort.UploadResult("bucket-documentos", "test/path.pdf", 45000L, "etag123"));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://minio.test/documentos/test.pdf");
        when(malwareScannerPort.scan(any(byte[].class)))
                .thenReturn(TestDataFactory.crearScanResultClean());
    }

    @Test
    @DisplayName("GET /api/v1/documentos/estado-cuenta/{cuentaId} - Debe generar estado de cuenta")
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = "SOCIO")
    void generarEstadoCuenta_conUsuarioSocio_deberiaRetornar200() throws Exception {
        // Arrange
        when(cuentaQueryPort.obtenerDatosCuenta(cuentaId))
                .thenReturn((Map<String, Object>) TestDataFactory.crearDatosEstadoCuenta().get("cuenta"));
        when(cuentaQueryPort.obtenerMovimientos(eq(cuentaId), anyInt(), anyInt()))
                .thenReturn(TestDataFactory.crearMovimientosPrueba());
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.ESTADO_CUENTA), anyMap()))
                .thenReturn(pdfBytesMock);
        when(documentoRepository.guardar(any()))
                .thenAnswer(invocation -> {
                    var doc = invocation.getArgument(0, com.tufondo.documentospdf.domain.model.Documento.class);
                    return com.tufondo.documentospdf.domain.model.Documento.builder()
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

        // Act & Assert
        mockMvc.perform(get("/api/v1/documentos/estado-cuenta/{cuentaId}", cuentaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoId").exists())
                .andExpect(jsonPath("$.socioId").value(socioId.toString()))
                .andExpect(jsonPath("$.tipo").value("ESTADO_CUENTA"))
                .andExpect(jsonPath("$.estado").value("ALMACENADO"))
                .andExpect(jsonPath("$.nombreArchivo").value("EstadoCuenta_2026-04_" + cuentaId + ".pdf"))
                .andExpect(jsonPath("$.preSignedUrl").exists());
    }

    @Test
    @DisplayName("GET /api/v1/documentos/constancia-afiliacion/{socioId} - Debe generar constancia")
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = "SOCIO")
    void generarConstanciaAfiliacion_conUsuarioSocio_deberiaRetornar200() throws Exception {
        // Arrange
        when(socioQueryPort.existeSocio(socioId)).thenReturn(true);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId))
                .thenReturn((Map<String, Object>) TestDataFactory.crearDatosConstanciaAfiliacion().get("socio"));
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap()))
                .thenReturn(pdfBytesMock);
        when(documentoRepository.guardar(any()))
                .thenAnswer(invocation -> {
                    var doc = invocation.getArgument(0, com.tufondo.documentospdf.domain.model.Documento.class);
                    return com.tufondo.documentospdf.domain.model.Documento.builder()
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

        // Act & Assert
        mockMvc.perform(get("/api/v1/documentos/constancia-afiliacion/{socioId}", socioId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoId").exists())
                .andExpect(jsonPath("$.tipo").value("CONSTANCIA_AFILIACION"))
                .andExpect(jsonPath("$.clasificacion").value("PUBLICO"));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/constancia-afiliacion/{socioId} - Admin puede generar para cualquier socio")
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = "ADMIN")
    void generarConstanciaAfiliacion_conUsuarioAdmin_deberiaRetornar200() throws Exception {
        // Arrange
        when(socioQueryPort.existeSocio(socioId)).thenReturn(true);
        when(socioQueryPort.obtenerDatosSocioParaPdf(socioId))
                .thenReturn((Map<String, Object>) TestDataFactory.crearDatosConstanciaAfiliacion().get("socio"));
        when(pdfGeneratorPort.generarPdf(eq(TipoDocumento.CONSTANCIA_AFILIACION), anyMap()))
                .thenReturn(pdfBytesMock);
        when(documentoRepository.guardar(any()))
                .thenAnswer(invocation -> {
                    var doc = invocation.getArgument(0, com.tufondo.documentospdf.domain.model.Documento.class);
                    return com.tufondo.documentospdf.domain.model.Documento.builder()
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

        // Act & Assert
        mockMvc.perform(get("/api/v1/documentos/constancia-afiliacion/{socioId}", socioId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/documentos/{documentoId} - Debe obtener metadata de documento")
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = "SOCIO")
    void obtenerDocumento_existeDocumento_deberiaRetornar200() throws Exception {
        // Arrange
        UUID documentoId = TestDataFactory.DOCUMENTO_ID;
        var documento = com.tufondo.documentospdf.domain.model.Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.ESTADO_CUENTA)
                .estado(EstadoDocumento.ALMACENADO)
                .nombreArchivo("EstadoCuenta_2026-04.pdf")
                .rutaAlmacenamiento("estados-cuenta/" + socioId + "/EstadoCuenta_2026-04.pdf")
                .hashArchivo("SHA-256:test123")
                .tamanoBytes(45000L)
                .fechaGeneracion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusDays(7))
                .generadoPor(socioId.toString())
                .clasificacion(ClasificacionDocumento.CONFIDENCIAL)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(java.util.Optional.of(documento));

        // Act & Assert
        mockMvc.perform(get("/api/v1/documentos/{documentoId}", documentoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoId").value(documentoId.toString()))
                .andExpect(jsonPath("$.tipo").value("ESTADO_CUENTA"))
                .andExpect(jsonPath("$.estado").value("ALMACENADO"))
                .andExpect(jsonPath("$.tamanoBytes").value(45000));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/{documentoId}/descargar - Debe generar pre-signed URL")
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = "SOCIO")
    void descargarDocumento_documentoValido_deberiaRetornarPreSignedUrl() throws Exception {
        // Arrange
        UUID documentoId = TestDataFactory.DOCUMENTO_ID;
        var documento = com.tufondo.documentospdf.domain.model.Documento.builder()
                .id(documentoId)
                .socioId(socioId)
                .tipo(TipoDocumento.ESTADO_CUENTA)
                .estado(EstadoDocumento.ALMACENADO)
                .nombreArchivo("EstadoCuenta_2026-04.pdf")
                .rutaAlmacenamiento("estados-cuenta/" + socioId + "/EstadoCuenta_2026-04.pdf")
                .hashArchivo("SHA-256:test123")
                .tamanoBytes(45000L)
                .fechaGeneracion(LocalDateTime.now())
                .generadoPor(socioId.toString())
                .clasificacion(ClasificacionDocumento.CONFIDENCIAL)
                .build();

        when(documentoRepository.buscarPorId(documentoId)).thenReturn(java.util.Optional.of(documento));
        when(storagePort.generatePresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://minio.test/pre-signed-url?token=abc123");

        // Act & Assert
        mockMvc.perform(get("/api/v1/documentos/{documentoId}/descargar", documentoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoId").value(documentoId.toString()))
                .andExpect(jsonPath("$.preSignedUrl").value("https://minio.test/pre-signed-url?token=abc123"))
                .andExpect(jsonPath("$.urlExpiraEn").value(900));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/estado-cuenta/{cuentaId} - Sin autenticación debe retornar 401")
    void generarEstadoCuenta_sinAutenticacion_deberiaRetornar401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/documentos/estado-cuenta/{cuentaId}", cuentaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
