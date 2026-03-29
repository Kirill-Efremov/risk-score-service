package ru.kpfu.itis.efremov.schemarisk.catalog.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.config.ConfluentSchemaRegistryProperties;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "schema-catalog", name = "mode", havingValue = "confluent")
public class ConfluentSchemaRegistryClient {

    private final RestClient restClient;

    public ConfluentSchemaRegistryClient(
            RestClient.Builder restClientBuilder,
            ConfluentSchemaRegistryProperties properties
    ) {
        RestClient.Builder builder = restClientBuilder
                .baseUrl(normalizeBaseUrl(properties.getUrl()))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);

        Credentials credentials = resolveCredentials(properties);
        if (credentials.username() != null && credentials.password() != null) {
            builder = builder.defaultHeaders(headers -> headers.setBasicAuth(
                    credentials.username(),
                    credentials.password()
            ));
        }

        this.restClient = builder.build();
    }

    public ConfluentSchemaVersionResponse getLatestVersion(String subject) {
        try {
            return restClient.get()
                    .uri("/subjects/{subject}/versions/latest", encodeSubject(subject))
                    .retrieve()
                    .body(ConfluentSchemaVersionResponse.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Subject not found in Confluent Schema Registry: " + subject);
        }
    }

    public ConfluentSchemaVersionResponse getVersion(String subject, int version) {
        try {
            return restClient.get()
                    .uri("/subjects/{subject}/versions/{version}", encodeSubject(subject), version)
                    .retrieve()
                    .body(ConfluentSchemaVersionResponse.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException(
                    "Schema version not found in Confluent Schema Registry: subject=" + subject + ", version=" + version
            );
        }
    }

    public List<Integer> listVersions(String subject) {
        try {
            List<Integer> versions = restClient.get()
                    .uri("/subjects/{subject}/versions", encodeSubject(subject))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (versions == null || versions.isEmpty()) {
                throw new ResourceNotFoundException("Subject not found in Confluent Schema Registry: " + subject);
            }
            return versions;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Subject not found in Confluent Schema Registry: " + subject);
        }
    }

    public ConfluentSchemaConfigResponse getSubjectConfig(String subject) {
        try {
            return restClient.get()
                    .uri("/config/{subject}", encodeSubject(subject))
                    .retrieve()
                    .body(ConfluentSchemaConfigResponse.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        }
    }

    private String encodeSubject(String subject) {
        return UriUtils.encodePathSegment(subject, StandardCharsets.UTF_8);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("confluent.schema-registry.url must be configured in CONFLUENT mode");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private Credentials resolveCredentials(ConfluentSchemaRegistryProperties properties) {
        if (hasText(properties.getUsername()) && hasText(properties.getPassword())) {
            return new Credentials(properties.getUsername(), properties.getPassword());
        }
        if (hasText(properties.getApiKey()) && hasText(properties.getApiSecret())) {
            return new Credentials(properties.getApiKey(), properties.getApiSecret());
        }
        return new Credentials(null, null);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record Credentials(String username, String password) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfluentSchemaVersionResponse(
            int id,
            int version,
            String subject,
            String schema
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfluentSchemaConfigResponse(
            String compatibilityLevel
    ) {
    }
}
