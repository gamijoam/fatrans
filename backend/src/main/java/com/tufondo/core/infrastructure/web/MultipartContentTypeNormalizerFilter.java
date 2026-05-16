package com.tufondo.core.infrastructure.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Sanea el header {@code Content-Type} en uploads multipart cuando viene con
 * {@code charset=UTF-8} adjunto.
 *
 * Spring Boot 3 (Tomcat embebido) valida estrictamente el Content-Type al
 * resolver multiparts y rechaza con HTTP 500
 * "Content-Type 'multipart/form-data;boundary=...;charset=UTF-8' is not supported".
 *
 * Sin embargo, varios clients HTTP populares adjuntan el charset automáticamente
 * y NO se puede suprimir desde el cliente:
 *  - undici (HTTP client de Node fetch, usado por Next.js BFFs).
 *  - curl con -F (versiones recientes).
 *  - axios cuando recibe FormData.
 *
 * El RFC 7578 §4.7 explícitamente permite el {@code charset} en multipart como
 * default para los parts de texto, así que el rechazo de Spring es demasiado
 * estricto. Este filter se ejecuta antes del MultipartResolver y reescribe el
 * Content-Type quitando el segmento {@code ;charset=...} solo cuando aplica,
 * sin tocar otros casos.
 *
 * Se aplica con orden HIGHEST_PRECEDENCE para garantizar que corra antes de
 * cualquier filter de Spring Security o de Tomcat que parsee el body.
 */
@Slf4j
public class MultipartContentTypeNormalizerFilter implements Filter {

    private static final String MULTIPART_PREFIX = "multipart/form-data";
    private static final String CHARSET_PATTERN = ";\\s*charset=[^;\\s]+";

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) {
        log.info("MultipartContentTypeNormalizerFilter inicializado");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String contentType = httpRequest.getContentType();
        // log.warn temporal para diagnóstico — bajar a debug cuando se confirme.
        if (contentType != null && contentType.startsWith(MULTIPART_PREFIX)) {
            log.warn("[multipart-filter] uri={} Content-Type recibido: {}", httpRequest.getRequestURI(), contentType);
        }
        if (contentType != null
                && contentType.startsWith(MULTIPART_PREFIX)
                && contentType.toLowerCase().contains("charset=")) {
            String saneado = contentType.replaceAll(CHARSET_PATTERN, "");
            log.warn("[multipart-filter] saneando: {} -> {}", contentType, saneado);
            chain.doFilter(new SaneadoContentTypeRequest(httpRequest, saneado),
                    (HttpServletResponse) response);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Wrapper de request que retorna el Content-Type saneado en {@code getContentType()}
     * y {@code getHeader("Content-Type")}. El resto pasa al request original.
     *
     * Spring's StandardServletMultipartResolver consulta {@code getContentType()},
     * pero algunos validators usan {@code getHeader()} — mantenemos ambos sincronizados.
     */
    private static class SaneadoContentTypeRequest extends HttpServletRequestWrapper {
        private final String contentTypeSaneado;

        SaneadoContentTypeRequest(HttpServletRequest original, String contentTypeSaneado) {
            super(original);
            this.contentTypeSaneado = contentTypeSaneado;
        }

        @Override
        public String getContentType() {
            return contentTypeSaneado;
        }

        @Override
        public String getHeader(String name) {
            if ("content-type".equalsIgnoreCase(name)) {
                return contentTypeSaneado;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("content-type".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.singletonList(contentTypeSaneado));
            }
            return super.getHeaders(name);
        }
    }
}
