package ru.kpfu.itis.efremov.schemarisk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schemaRiskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Schema Risk Score Service API")
                        .description("""
                                Сервис анализа эволюции схем.
                                Выполняет проверку совместимости,
                                анализ влияния (impact),
                                расчёт риска и governance-решение.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kirill Efremov")
                                .email("kirillefr45@gmail.com")
                        )
                );
    }
}
