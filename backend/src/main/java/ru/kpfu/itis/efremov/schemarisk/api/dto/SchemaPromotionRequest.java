package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

@Data
@Schema(description = "Запрос на контролируемую публикацию схемы")
public class SchemaPromotionRequest {

    @NotNull(message = "schemaType is required")
    @Schema(description = "Тип схемы", example = "AVRO")
    private SchemaType schemaType;

    @Schema(
            description = "Режим совместимости для анализа. Если не указан, используется политика subject по умолчанию.",
            example = "BACKWARD"
    )
    private CompatibilityMode compatibilityMode;

    @NotBlank(message = "schemaText must not be blank")
    @Schema(description = "Текст новой схемы, которую нужно проанализировать и при необходимости зарегистрировать")
    private String schemaText;

    @Schema(description = "Опциональное описание версии схемы")
    private String description;

    @Schema(description = "Кто инициировал controlled promotion flow")
    private String createdBy;
}
