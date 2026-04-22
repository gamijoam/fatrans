package com.tufondo;

import com.tufondo.auth.infrastructure.configuration.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class FondoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FondoApplication.class, args);
    }
}
