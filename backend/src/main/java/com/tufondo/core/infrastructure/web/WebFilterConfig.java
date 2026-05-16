package com.tufondo.core.infrastructure.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registro explícito de los Servlet Filters de la app.
 *
 * Spring Boot auto-detecta {@code @Component} filters y los registra con
 * orden ambiguo respecto a la cadena de Spring Security. Para garantizar
 * que {@link MultipartContentTypeNormalizerFilter} corra ANTES del
 * MultipartResolver de Spring (que valida el Content-Type), lo registramos
 * con un {@link FilterRegistrationBean} y order = HIGHEST_PRECEDENCE.
 */
@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<MultipartContentTypeNormalizerFilter> multipartContentTypeFilter() {
        FilterRegistrationBean<MultipartContentTypeNormalizerFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new MultipartContentTypeNormalizerFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        reg.setName("multipartContentTypeNormalizer");
        return reg;
    }
}
