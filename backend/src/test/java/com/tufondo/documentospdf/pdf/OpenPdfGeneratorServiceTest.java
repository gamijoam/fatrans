// com.tufondo.documentospdf.pdf.OpenPdfGeneratorServiceTest
package com.tufondo.documentospdf.pdf;

import com.tufondo.documentospdf.TestDataFactory;
import com.tufondo.documentospdf.domain.exception.FirmaDigitalException;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.infrastructure.pdf.OpenPdfGeneratorService;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para OpenPdfGeneratorService.
 * Genera PDFs REALES y los guarda en disco para verificación visual.
 *
 * IMPORTANTE: Estos tests generan PDFs válidos que se pueden abrir
 * y verificar visualmente en test-output/pdfs/
 */
class OpenPdfGeneratorServiceTest {

    private OpenPdfGeneratorService pdfGeneratorService;

    private static final String TEST_OUTPUT_DIR = "test-output/pdfs";

    @BeforeEach
    void setUp() {
        pdfGeneratorService = new OpenPdfGeneratorService();
        try {
            Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el directorio de salida", e);
        }
    }

    @Test
    @DisplayName("Debería generar PDF de Estado de Cuenta válido")
    void generarPdf_estadoCuenta_deberiaCrearPdfValido() throws Exception {
        Map<String, Object> datos = TestDataFactory.crearDatosEstadoCuenta();
        TipoDocumento tipo = TipoDocumento.ESTADO_CUENTA;

        byte[] pdfBytes = pdfGeneratorService.generarPdf(tipo, datos);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
        assertThat(pdfBytes.length).isGreaterThan(1000);

        Path pdfFile = Paths.get(TEST_OUTPUT_DIR, "EstadoCuenta-prueba.pdf");
        Files.write(pdfFile, pdfBytes);

        System.out.println("PDF generado: " + pdfFile.toAbsolutePath() + " (" + pdfBytes.length + " bytes)");
    }

    @Test
    @DisplayName("Debería generar PDF de Constancia de Afiliación válido")
    void generarPdf_constanciaAfiliacion_deberiaCrearPdfValido() throws Exception {
        Map<String, Object> datos = TestDataFactory.crearDatosConstanciaAfiliacion();
        TipoDocumento tipo = TipoDocumento.CONSTANCIA_AFILIACION;

        byte[] pdfBytes = pdfGeneratorService.generarPdf(tipo, datos);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
        assertThat(pdfBytes.length).isGreaterThan(1000);

        Path pdfFile = Paths.get(TEST_OUTPUT_DIR, "CONSTANCIA_AFILIACION-prueba.pdf");
        Files.write(pdfFile, pdfBytes);

        System.out.println("PDF generado: " + pdfFile.toAbsolutePath() + " (" + pdfBytes.length + " bytes)");
    }

    @Test
    @DisplayName("Debería generar PDF de Tabla de Amortización válido")
    void generarPdf_tablaAmortizacion_deberiaCrearPdfValido() throws Exception {
        Map<String, Object> datos = TestDataFactory.crearDatosTablaAmortizacion();
        TipoDocumento tipo = TipoDocumento.TABLA_AMORTIZACION;

        byte[] pdfBytes = pdfGeneratorService.generarPdf(tipo, datos);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
        assertThat(pdfBytes.length).isGreaterThan(1000);

        Path pdfFile = Paths.get(TEST_OUTPUT_DIR, "TABLA_AMORTIZACION-prueba.pdf");
        Files.write(pdfFile, pdfBytes);

        System.out.println("PDF generado: " + pdfFile.toAbsolutePath() + " (" + pdfBytes.length + " bytes)");
    }

    @Test
    @DisplayName("Debería generar PDF de Carta de Beneficiarios válido")
    void generarPdf_cartaBeneficiarios_deberiaCrearPdfValido() throws Exception {
        Map<String, Object> datos = TestDataFactory.crearDatosCartaBeneficiarios();
        TipoDocumento tipo = TipoDocumento.CARTA_BENEFICIARIOS;

        byte[] pdfBytes = pdfGeneratorService.generarPdf(tipo, datos);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
        assertThat(pdfBytes.length).isGreaterThan(1000);

        Path pdfFile = Paths.get(TEST_OUTPUT_DIR, "CARTA_BENEFICIARIOS-prueba.pdf");
        Files.write(pdfFile, pdfBytes);

        System.out.println("PDF generado: " + pdfFile.toAbsolutePath() + " (" + pdfBytes.length + " bytes)");
    }

    @Test
    @DisplayName("Debería lanzar excepción al generar CONTRATO sin firma digital configurada")
    void generarPdf_contratoSinFirmaDigital_lanzaExcepcion() {
        Map<String, Object> datos = TestDataFactory.crearDatosContratoAdhesion();
        datos.put("firmaDigitalHabilitada", false);
        TipoDocumento tipo = TipoDocumento.CONTRATO_ADHESION;

        assertThatThrownBy(() -> pdfGeneratorService.generarPdf(tipo, datos))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("Firma digital no configurada");
    }

    @Test
    @DisplayName("Debería lanzar excepción al generar PAGARE sin firma digital configurada")
    void generarPdf_pagareSinFirmaDigital_lanzaExcepcion() {
        Map<String, Object> datos = TestDataFactory.crearDatosPagare();
        TipoDocumento tipo = TipoDocumento.PAGARE;

        assertThatThrownBy(() -> pdfGeneratorService.generarPdf(tipo, datos))
                .isInstanceOf(FirmaDigitalException.class)
                .hasMessageContaining("Firma digital no configurada");
    }

    @Nested
    @DisplayName("Tests CONTRATO y PAGARE con Firma Digital Real")
    class ContratoYPagareConFirmaTests {

        private static final String TEST_KEYSTORE_PATH = "/home/gamijoam/Documentos/fondoAhorro/fondo-ahorro-platform/backend/test-resources/firma/test-keystore.p12";
        private static final String TEST_KEYSTORE_PASSWORD = "test123";
        private static final String TEST_KEY_ALIAS = "test-documents";

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(pdfGeneratorService, "firmaDigitalEnabled", true);
            ReflectionTestUtils.setField(pdfGeneratorService, "keystorePath", TEST_KEYSTORE_PATH);
            ReflectionTestUtils.setField(pdfGeneratorService, "keystorePassword", TEST_KEYSTORE_PASSWORD);
            ReflectionTestUtils.setField(pdfGeneratorService, "keyAlias", TEST_KEY_ALIAS);
        }

        @Test
        @DisplayName("Debería generar PDF de CONTRATO_ADHESION con firma digital")
        void generarPdf_contratoConFirma_deberiaCrearPdfValido() throws Exception {
            Map<String, Object> datos = TestDataFactory.crearDatosContratoAdhesion();
            TipoDocumento tipo = TipoDocumento.CONTRATO_ADHESION;

            byte[] pdfBytes = pdfGeneratorService.generarPdf(tipo, datos);

            assertThat(pdfBytes).isNotEmpty();
            assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
            assertThat(pdfBytes.length).isGreaterThan(1500);

            Path pdfFile = Paths.get(TEST_OUTPUT_DIR, "CONTRATO_ADHESION_firmado.pdf");
            Files.write(pdfFile, pdfBytes);

            System.out.println("CONTRATO PDF generado con firma: " + pdfFile.toAbsolutePath() + " (" + pdfBytes.length + " bytes)");
        }

        @Test
        @DisplayName("Debería generar PDF de PAGARE con firma digital")
        void generarPdf_pagareConFirma_deberiaCrearPdfValido() throws Exception {
            Map<String, Object> datos = TestDataFactory.crearDatosPagare();
            TipoDocumento tipo = TipoDocumento.PAGARE;

            byte[] pdfBytes = pdfGeneratorService.generarPdf(tipo, datos);

            assertThat(pdfBytes).isNotEmpty();
            assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
            assertThat(pdfBytes.length).isGreaterThan(1500);

            Path pdfFile = Paths.get(TEST_OUTPUT_DIR, "PAGARE_firmado.pdf");
            Files.write(pdfFile, pdfBytes);

            System.out.println("PAGARE PDF generado con firma: " + pdfFile.toAbsolutePath() + " (" + pdfBytes.length + " bytes)");
        }

        @Test
        @DisplayName("Debería generar CONTRATO y PAGARE consecutivamente")
        void generarPdf_contratoYPagare_consecutivos() throws Exception {
            Map<String, Object> datosContrato = TestDataFactory.crearDatosContratoAdhesion();
            Map<String, Object> datosPagare = TestDataFactory.crearDatosPagare();

            byte[] pdfContrato = pdfGeneratorService.generarPdf(TipoDocumento.CONTRATO_ADHESION, datosContrato);
            byte[] pdfPagare = pdfGeneratorService.generarPdf(TipoDocumento.PAGARE, datosPagare);

            assertThat(pdfContrato).isNotEmpty();
            assertThat(new String(pdfContrato, 0, 4)).isEqualTo("%PDF");
            assertThat(pdfPagare).isNotEmpty();
            assertThat(new String(pdfPagare, 0, 4)).isEqualTo("%PDF");

            Files.write(Paths.get(TEST_OUTPUT_DIR, "CONTRATO_firmado_test.pdf"), pdfContrato);
            Files.write(Paths.get(TEST_OUTPUT_DIR, "PAGARE_firmado_test.pdf"), pdfPagare);

            System.out.println("CONTRATO: " + pdfContrato.length + " bytes, PAGARE: " + pdfPagare.length + " bytes");
        }
    }

    @Nested
    @DisplayName("Casos Extremos")
    class CasosExtremos {

        @Test
        @DisplayName("Debería generar PDF con movimientos vacíos")
        void generarPdf_movimientosVacios_deberiaCrearPdf() throws Exception {
            Map<String, Object> datos = new java.util.HashMap<>();
            datos.put("cuenta", java.util.Map.of(
                    "numeroCuenta", "TEST-EMPTY",
                    "nombreCompleto", "Test User Empty",
                    "saldoAnterior", new java.math.BigDecimal("0.00"),
                    "totalAbonos", new java.math.BigDecimal("0.00"),
                    "totalCargos", new java.math.BigDecimal("0.00"),
                    "saldoActual", new java.math.BigDecimal("0.00")
            ));
            datos.put("movimientos", java.util.Collections.emptyList());
            datos.put("periodo", "Mayo 2026");
            datos.put("fechaGeneracion", "19/04/2026");
            datos.put("socioId", "test-empty");
            datos.put("generadoPor", "test-system");

            byte[] pdfBytes = pdfGeneratorService.generarPdf(TipoDocumento.ESTADO_CUENTA, datos);

            assertThat(pdfBytes).isNotEmpty();
            assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("Debería generar PDF con caracteres especiales en nombres")
        void generarPdf_caracteresEspeciales_deberiaCrearPdf() throws Exception {
            Map<String, Object> datos = new java.util.HashMap<>();
            datos.put("cuenta", java.util.Map.of(
                    "numeroCuenta", "TEST-CHARS",
                    "nombreCompleto", "José María Niño García del Valle & Compañía",
                    "saldoAnterior", new java.math.BigDecimal("10000.50"),
                    "totalAbonos", new java.math.BigDecimal("5000.00"),
                    "totalCargos", new java.math.BigDecimal("2500.50"),
                    "saldoActual", new java.math.BigDecimal("12500.00")
            ));
            datos.put("movimientos", java.util.Collections.emptyList());
            datos.put("periodo", "Abril 2026");
            datos.put("fechaGeneracion", "19/04/2026");
            datos.put("socioId", "test-chars");
            datos.put("generadoPor", "test-system");

            byte[] pdfBytes = pdfGeneratorService.generarPdf(TipoDocumento.ESTADO_CUENTA, datos);

            assertThat(pdfBytes).isNotEmpty();
            assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
        }
    }
}