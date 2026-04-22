package com.tufondo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Fondo de Ahorro")
                        .version("1.0.0")
                        .description("API RESTful para gestión de cuentas de ahorro, movimientos y rendimientos")
                        .contact(new Contact()
                                .name("Tufondo Team")
                                .email("soporte@tufondo.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://tufondo.com/licenses")));
    }
}
