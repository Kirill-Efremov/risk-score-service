package ru.kpfu.itis.efremov.schemarisk.catalog.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;
import ru.kpfu.itis.efremov.schemarisk.catalog.exception.SchemaRegistryConflictException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.SchemaRegistryUnavailableException;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.config.ConfluentSchemaRegistryProperties;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "schema-catalog", name = "mode", havingValue = "confluent", matchIfMissing = true)
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
        } catch (RestClientException exception) {
            throw new SchemaRegistryUnavailableException("Schema Registry is unavailable", exception);
        }
    }

    public List<String> listSubjects() {
        try {
            List<String> subjects = restClient.get()
                    .uri("/subjects")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return subjects != null ? subjects : List.of();
        } catch (RestClientException exception) {
            throw new SchemaRegistryUnavailableException("Schema Registry is unavailable", exception);
        }
    }

    private String resolveRegistryMessage(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody != null && !responseBody.isBlank()) {
            return responseBody;
        }
        return exception.getStatusText();
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
        } catch (RestClientException exception) {
            throw new SchemaRegistryUnavailableException("Schema Registry is unavailable", exception);
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
        } catch (RestClientException exception) {
            throw new SchemaRegistryUnavailableException("Schema Registry is unavailable", exception);
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
        } catch (RestClientException exception) {
            throw new SchemaRegistryUnavailableException("Schema Registry is unavailable", exception);
        }
    }

    public ConfluentSchemaRegisterResponse registerVersion(String subject, String schema, SchemaType schemaType) {
        try {
            ConfluentSchemaRegisterResponse response = restClient.post()
                    .uri("/subjects/{subject}/versions", encodeSubject(subject))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ConfluentSchemaRegisterRequest(schemaType != null ? schemaType.name() : "AVRO", schema))
                    .retrieve()
                    .body(ConfluentSchemaRegisterResponse.class);
            if (response == null) {
                throw new IllegalStateException("Confluent Schema Registry returned an empty response");
            }
            return response;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Subject not found in Confluent Schema Registry: " + subject);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new SchemaRegistryConflictException(
                        resolveRegistryMessage(exception),
                        exception.getStatusCode().value(),
                        exception.getResponseBodyAsString()
                );
            }
            throw exception;
        } catch (RestClientException exception) {
            throw new SchemaRegistryUnavailableException("Schema Registry is unavailable", exception);
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

    public record ConfluentSchemaRegisterRequest(
            String schemaType,
            String schema
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfluentSchemaRegisterResponse(
            int id
    ) {
    }
}
