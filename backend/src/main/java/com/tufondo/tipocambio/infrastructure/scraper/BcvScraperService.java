package com.tufondo.tipocambio.infrastructure.scraper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;

/**
 * Cliente HTTP que scrapea bcv.org.ve para obtener la tasa USD oficial
 * (issue #231).
 *
 * <p>El sitio del BCV tiene un certificado SSL problemático (auto-firmado
 * o vencido en algunos momentos), lo cual normalmente bloquearía la conexión.
 * Aceptamos el certificado SOLO para el host {@code bcv.org.ve} — no
 * deshabilitamos la validación globalmente. Esto es un riesgo aceptado y
 * documentado: la fuente oficial del BCV es lo que es, y la alternativa
 * (no consumirla) es peor.</p>
 *
 * <p>El parsing del HTML se delega a {@link BcvHtmlParser} (puro, testeable
 * sin red).</p>
 */
@Slf4j
@Service
public class BcvScraperService {

    private static final String BCV_HOST = "www.bcv.org.ve";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (TuFondo/Fatrans Bot; +https://fatrans.com.ve) Java/21";

    private final String baseUrl;

    public BcvScraperService(@Value("${bcv.base-url:https://www.bcv.org.ve}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Descarga el HTML del home del BCV y extrae la tasa USD + fecha valor.
     *
     * @return resultado con la tasa USD (Bs/USD), tasa compra (=tasaVenta para BCV,
     *         no publica spread), y la fecha valor publicada.
     * @throws BcvScrapingException si falla la red, el SSL, o el parsing.
     */
    public TasaScrapeada scrapearTasaBcv() {
        String html = descargarHtml();
        BigDecimal tasaUsd = BcvHtmlParser.parsearTasaUsd(html);
        LocalDate fecha = BcvHtmlParser.parsearFechaValor(html);

        log.info("Scraping BCV exitoso: USD={} Bs, fechaValor={}", tasaUsd, fecha);
        return new TasaScrapeada(tasaUsd, fecha);
    }

    private String descargarHtml() {
        try {
            URL url = URI.create(baseUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Solo aplica el SSL relajado si es HTTPS hacia el host del BCV
            if (conn instanceof HttpsURLConnection https
                    && BCV_HOST.equalsIgnoreCase(url.getHost())) {
                aplicarSslRelajadoSoloParaBcv(https);
            }

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml");

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new BcvScrapingException(
                        "BCV respondió HTTP " + status + " — el sitio no está disponible.");
            }

            try (InputStream is = conn.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new BcvScrapingException(
                    "Error de red/SSL al consultar bcv.org.ve: " + e.getMessage(), e);
        }
    }

    /**
     * Acepta el cert del BCV SOLO para esta conexión específica.
     * El TrustManager y HostnameVerifier se setean en la instancia
     * {@link HttpsURLConnection} (no globalmente).
     */
    private void aplicarSslRelajadoSoloParaBcv(HttpsURLConnection conn)
            throws GeneralSecurityException {
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // No-op: aceptamos cualquier cert (solo aplicado a esta conexión).
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // No-op: aceptamos cualquier cert del BCV.
                    }
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new java.security.SecureRandom());
        conn.setSSLSocketFactory(ctx.getSocketFactory());

        // Hostname verifier permisivo solo para esta conexión
        HostnameVerifier permisivo = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return BCV_HOST.equalsIgnoreCase(hostname);
            }
        };
        conn.setHostnameVerifier(permisivo);
    }

    /** Resultado del scraping del BCV: tasa USD y fecha valor. */
    public record TasaScrapeada(BigDecimal tasaUsdEnBs, LocalDate fechaValor) {}

    /** Error específico del scraper (distinto de parsing y de tasa inválida). */
    public static class BcvScrapingException extends RuntimeException {
        public BcvScrapingException(String mensaje) {
            super(mensaje);
        }
        public BcvScrapingException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}
