package com.yowyob.feedback.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI and Swagger UI documentation.
 * Defines the security schemes and global requirements for the API.
 * * @author Thomas Djotio Ndié
 * @since 30.09.25
 * @version 0.1
 */
@Configuration
public class OpenApiConfig {

    /**
     * Customizes the OpenAPI definition to include JWT security.
     * * @return the configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String security_scheme_name = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Yowyob Feedback API")
                        .version("1.0")
                        .description("Documentation of APIs with JWT protection"))
                .addSecurityItem(new SecurityRequirement()
                        .addList(security_scheme_name))
                .components(new Components()
                        .addSecuritySchemes(security_scheme_name,
                                new SecurityScheme()
                                        .name(security_scheme_name)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
