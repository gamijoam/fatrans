package com.tufondo.tipocambio.infrastructure.scraper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Parser HTML puro del sitio del BCV (issue #231).
 *
 * <p>Separado de {@link BcvScraperService} para que sea testeable de forma
 * unitaria sin necesidad de red. Los tests le pasan HTML fixture (capturado
 * de bcv.org.ve en un momento conocido) y validan que extrae los valores
 * correctos.</p>
 *
 * <p>Selectores conocidos del sitio actual (Drupal):
 * <ul>
 *   <li>USD: {@code #dolar .centrado strong}</li>
 *   <li>EUR: {@code #euro .centrado strong}</li>
 *   <li>Fecha valor: {@code .date-display-single[content]} → atributo
 *       {@code content} con ISO 8601</li>
 * </ul>
 * Si el BCV cambia su HTML, los métodos lanzan {@link BcvHtmlParseException}
 * con detalle del selector que falló, para que la alerta sea accionable.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BcvHtmlParser {

    /**
     * Parsea la tasa USD del HTML del BCV.
     *
     * @return monto en bolívares por 1 USD (ej. {@code 517.96190000})
     * @throws BcvHtmlParseException si el selector no encuentra el valor o
     *         el número no se puede parsear
     */
    public static BigDecimal parsearTasaUsd(String html) {
        Document doc = Jsoup.parse(html);
        Element strong = doc.selectFirst("#dolar .centrado strong");
        if (strong == null) {
            throw new BcvHtmlParseException(
                    "No se encontró el selector '#dolar .centrado strong' en el HTML del BCV. " +
                    "Posible cambio en la estructura del sitio.");
        }
        String texto = strong.text().trim();
        return parsearNumeroBcv(texto, "USD");
    }

    /**
     * Parsea la fecha valor (Fecha Valor: ...) del HTML.
     *
     * <p>Estructura conocida:
     * <pre>{@code
     *   <span class="date-display-single" content="2026-05-19T00:00:00-04:00">
     *     Martes, 19 Mayo 2026
     *   </span>
     * }</pre>
     * Preferimos el atributo {@code content} porque ya viene en ISO 8601 y
     * no depende del idioma de Drupal.</p>
     *
     * @return la fecha valor publicada por el BCV
     * @throws BcvHtmlParseException si el selector no se encuentra o la
     *         fecha no se puede parsear
     */
    public static LocalDate parsearFechaValor(String html) {
        Document doc = Jsoup.parse(html);
        Element span = doc.selectFirst(".date-display-single[content]");
        if (span == null) {
            throw new BcvHtmlParseException(
                    "No se encontró el selector '.date-display-single[content]' en el HTML del BCV.");
        }
        String iso = span.attr("content").trim();
        if (iso.isEmpty()) {
            throw new BcvHtmlParseException(
                    "El atributo 'content' de '.date-display-single' está vacío.");
        }
        try {
            return OffsetDateTime.parse(iso).toLocalDate();
        } catch (Exception e) {
            throw new BcvHtmlParseException(
                    "No se pudo parsear la fecha BCV '" + iso + "' como ISO 8601.", e);
        }
    }

    /**
     * Parsea un número en formato BCV (coma decimal, sin separador de miles).
     *
     * <p>Ejemplos válidos:
     * <ul>
     *   <li>{@code "517,96190000"} → {@code 517.96190000}</li>
     *   <li>{@code "602,18768455"} → {@code 602.18768455}</li>
     *   <li>{@code "11,37268028"} → {@code 11.37268028}</li>
     * </ul>
     */
    static BigDecimal parsearNumeroBcv(String texto, String contextoLabel) {
        if (texto == null || texto.isEmpty()) {
            throw new BcvHtmlParseException(
                    "Valor de " + contextoLabel + " vacío en el HTML del BCV.");
        }
        // BCV usa coma decimal. Convertimos a punto para BigDecimal.
        // Eliminamos también cualquier whitespace o caracteres no numéricos
        // (excepto la coma decimal) por seguridad.
        String limpio = texto.replace(',', '.').replaceAll("[^0-9.]", "");
        try {
            BigDecimal valor = new BigDecimal(limpio);
            if (valor.signum() <= 0) {
                throw new BcvHtmlParseException(
                        "Tasa " + contextoLabel + " del BCV es <= 0: " + valor);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new BcvHtmlParseException(
                    "No se pudo parsear el número '" + texto + "' (tasa " +
                    contextoLabel + ") del BCV.", e);
        }
    }

    /**
     * Excepción específica del parsing del BCV. Distinta de errores de red
     * o de cert SSL — esta indica que el HTML cambió o trae datos inesperados.
     */
    public static class BcvHtmlParseException extends RuntimeException {
        public BcvHtmlParseException(String mensaje) {
            super(mensaje);
        }
        public BcvHtmlParseException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}
