package ru.kpfu.itis.efremov.schemarisk.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

@Data
public class VersionedSchemaCheckRequest {

    @Positive(message = "oldVersion must be positive")
    private Integer oldVersion;

    @Positive(message = "newVersion must be positive")
    private Integer newVersion;

    private String newSchema;

    private SchemaType schemaType;

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
