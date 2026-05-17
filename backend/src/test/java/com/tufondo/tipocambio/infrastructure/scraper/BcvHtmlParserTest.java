package com.tufondo.tipocambio.infrastructure.scraper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests para issue #231: parser HTML del BCV.
 *
 * <p>Usa un fixture en {@code src/test/resources/bcv/bcv-home-fixture.html}
 * con HTML real capturado de bcv.org.ve. Si el BCV cambia su HTML, este
 * fixture y el parser deben actualizarse juntos — el fallo en este test es
 * la alerta temprana.</p>
 */
class BcvHtmlParserTest {

    private static String htmlFixture;

    @BeforeAll
    static void loadFixture() throws IOException {
        try (InputStream is = BcvHtmlParserTest.class.getResourceAsStream(
                "/bcv/bcv-home-fixture.html")) {
            if (is == null) {
                throw new IOException("Fixture bcv-home-fixture.html no encontrado en classpath");
            }
            htmlFixture = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("Issue #231: parsea tasa USD del HTML real del BCV (517,96190000 → 517.96190000)")
    void parseaTasaUsd_desdeHtmlRealBcv() {
        BigDecimal tasa = BcvHtmlParser.parsearTasaUsd(htmlFixture);

        // El valor exacto del fixture: 517,96190000
        assertThat(tasa).isEqualByComparingTo(new BigDecimal("517.96190000"));
    }

    @Test
    @DisplayName("Issue #231: parsea Fecha Valor del atributo content (ISO 8601 robusto)")
    void parseaFechaValor_desdeAtributoContent() {
        LocalDate fecha = BcvHtmlParser.parsearFechaValor(htmlFixture);

        // El fixture tiene content="2026-05-19T00:00:00-04:00"
        assertThat(fecha).isEqualTo(LocalDate.of(2026, 5, 19));
    }

    @Test
    @DisplayName("Issue #231: parsearNumeroBcv convierte coma decimal a punto correctamente")
    void parsearNumeroBcv_convierteComa() {
        assertThat(BcvHtmlParser.parsearNumeroBcv("517,96190000", "USD"))
                .isEqualByComparingTo(new BigDecimal("517.96190000"));
        assertThat(BcvHtmlParser.parsearNumeroBcv("76,07", "CNY"))
                .isEqualByComparingTo(new BigDecimal("76.07"));
    }

    @Test
    @DisplayName("Issue #231: parsearNumeroBcv rechaza valores <= 0 (defensive)")
    void parsearNumeroBcv_rechazaNoPositivos() {
        assertThatThrownBy(() -> BcvHtmlParser.parsearNumeroBcv("0,00", "USD"))
                .isInstanceOf(BcvHtmlParser.BcvHtmlParseException.class)
                .hasMessageContaining("<= 0");
    }

    @Test
    @DisplayName("Issue #231: parsearTasaUsd lanza error claro si el selector no existe")
    void parsearTasaUsd_selectorAusente() {
        String htmlSinDolar = "<html><body><div>algo distinto</div></body></html>";

        assertThatThrownBy(() -> BcvHtmlParser.parsearTasaUsd(htmlSinDolar))
                .isInstanceOf(BcvHtmlParser.BcvHtmlParseException.class)
                .hasMessageContaining("#dolar .centrado strong")
                .hasMessageContaining("Posible cambio");
    }

    @Test
    @DisplayName("Issue #231: parsearFechaValor lanza error claro si el span no existe")
    void parsearFechaValor_selectorAusente() {
        String htmlSinFecha = "<html><body></body></html>";

        assertThatThrownBy(() -> BcvHtmlParser.parsearFechaValor(htmlSinFecha))
                .isInstanceOf(BcvHtmlParser.BcvHtmlParseException.class)
                .hasMessageContaining(".date-display-single");
    }

    @Test
    @DisplayName("Issue #231: parsearNumeroBcv rechaza texto vacío")
    void parsearNumeroBcv_textoVacio() {
        assertThatThrownBy(() -> BcvHtmlParser.parsearNumeroBcv("", "USD"))
                .isInstanceOf(BcvHtmlParser.BcvHtmlParseException.class);
        assertThatThrownBy(() -> BcvHtmlParser.parsearNumeroBcv(null, "USD"))
                .isInstanceOf(BcvHtmlParser.BcvHtmlParseException.class);
    }

    @Test
    @DisplayName("Issue #231: parsearNumeroBcv rechaza basura no-numérica")
    void parsearNumeroBcv_basura() {
        assertThatThrownBy(() -> BcvHtmlParser.parsearNumeroBcv("hola mundo", "USD"))
                .isInstanceOf(BcvHtmlParser.BcvHtmlParseException.class);
    }
}
