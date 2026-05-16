package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

@Data
@Schema(description = "Запрос анализа версии схемы")
public class VersionedSchemaCheckRequest {

    @Positive(message = "oldVersion must be positive")
    @Schema(description = "Версия старой схемы", example = "1")
    private Integer oldVersion;

    @Positive(message = "newVersion must be positive")
    @Schema(description = "Версия новой схемы", example = "2")
    private Integer newVersion;

    @Schema(
            description = "Новая draft-схема в формате JSON, если анализ выполняется не по newVersion",
            example = "{\"type\":\"record\",\"name\":\"UserCreated\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null}]}"
    )
    private String newSchema;

    @Schema(description = "Тип схемы, обязателен при передаче newSchema", example = "AVRO")
    private SchemaType schemaType;

    @Schema(description = "Режим совместимости", example = "BACKWARD")
    private CompatibilityMode compatibilityMode;

    @AssertTrue(message = "Either newVersion or newSchema must be provided, but not both")
    public boolean isNewSchemaSourceValid() {
        boolean hasNewVersion = newVersion != null;
        boolean hasNewSchema = newSchema != null && !newSchema.isBlank();
        return hasNewVersion ^ hasNewSchema;
    }

    @AssertTrue(message = "schemaType is required when newSchema is provided")
    public boolean isSchemaTypeValid() {
        boolean hasNewSchema = newSchema != null && !newSchema.isBlank();
        return !hasNewSchema || schemaType != null;
    }
}




