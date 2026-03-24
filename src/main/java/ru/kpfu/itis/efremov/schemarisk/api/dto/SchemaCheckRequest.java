package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

@Data
@Schema(description = "Запрос на анализ изменения схемы")
public class SchemaCheckRequest {

    @NotNull(message = "schemaType is required")
    @Schema(description = "Тип схемы (например, AVRO)", example = "AVRO")
    private SchemaType schemaType;

    @Schema(description = "Режим совместимости", example = "BACKWARD")
    private CompatibilityMode compatibilityMode;

    @NotBlank(message = "oldSchema must not be blank")
    @Schema(
            description = "Старая схема в формате JSON",
            example = "{\"type\":\"record\",\"name\":\"UserCreated\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"}]}"
    )
    private String oldSchema;

    @NotBlank(message = "newSchema must not be blank")
    @Schema(
            description = "Новая схема в формате JSON",
            example = "{\"type\":\"record\",\"name\":\"UserCreated\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null}]}"
    )
    private String newSchema;
}




