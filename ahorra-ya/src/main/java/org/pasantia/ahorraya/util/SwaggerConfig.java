package org.pasantia.ahorraya.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that provides the OpenAPI (Swagger) documentation configuration for the application.
 *
 * <p>Exposes an {@link OpenAPI} bean with application title, description and version used by Swagger UI
 * and OpenAPI tooling.</p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Create and return the application's OpenAPI definition.
     *
     * @return configured {@link OpenAPI} instance with title, description and version
     */
    @Bean
    public OpenAPI ahorrayaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ahorraya API")
                        .description("""
                                REST API for the Ahorraya application.
                                Enables management of users, saving accounts, transactions, and saving movements.
                                """)
                        .version("1.0.0"));
    }
}