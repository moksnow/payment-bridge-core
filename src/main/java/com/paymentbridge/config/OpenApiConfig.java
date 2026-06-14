package com.paymentbridge.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentBridgeOpenAPI() {
        return new OpenAPI();
    }
}
