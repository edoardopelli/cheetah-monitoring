package org.cheetah.monitoring.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cheetahMonitoringOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Cheetah Monitoring API")
                .version("1.0.0")
                .description("REST endpoints for agent registration, metrics, thresholds, and alerts")
                .contact(new Contact()
                    .name("Edoardo Pelli")
                    .url("https://github.com/edoardopelli")
                )
            );
    }
}