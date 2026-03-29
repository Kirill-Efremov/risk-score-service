package ru.kpfu.itis.efremov.schemarisk.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConfluentSchemaRegistryProperties.class)
public class SchemaCatalogConfiguration {
}
