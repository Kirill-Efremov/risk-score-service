package ru.kpfu.itis.efremov.schemarisk.analysis.diff;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.avro.Schema;
import org.springframework.stereotype.Service;

import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.AvroParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.AvroSchemaProvider;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ChangeSeverityPolicy;

@Service
public class AvroDiffService {

    private final AvroSchemaProvider avroSchemaProvider;
    private final ChangeSeverityPolicy changeSeverityPolicy;

    public AvroDiffService(
            AvroSchemaProvider avroSchemaProvider,
            ChangeSeverityPolicy changeSeverityPolicy
    ) {
        this.avroSchemaProvider = avroSchemaProvider;
        this.changeSeverityPolicy = changeSeverityPolicy;
    }

    public DiffResult diff(AvroParsedSchema oldSchema, AvroParsedSchema newSchema) {
        Schema oldRecord = oldSchema.getAvroSchema();
        Schema newRecord = newSchema.getAvroSchema();

        if (oldRecord.getType() != Schema.Type.RECORD || newRecord.getType() != Schema.Type.RECORD) {
            throw new IllegalArgumentException("AvroDiffService supports only record schemas");
        }

        NormalizedSchema oldNormalized = avroSchemaProvider.normalize(oldSchema);
        NormalizedSchema newNormalized = avroSchemaProvider.normalize(newSchema);

        return DiffResult.builder()
                .schemaName(newRecord.getFullName())
                .changes(diffFields(oldNormalized, newNormalized, ""))
                .build();
    }

    private List<FieldChange> diffFields(NormalizedSchema oldSchema, NormalizedSchema newSchema, String prefix) {
        List<FieldChange> changes = new ArrayList<>();
        Set<String> fieldNames = new LinkedHashSet<>();
        fieldNames.addAll(oldSchema.getFields().keySet());
        fieldNames.addAll(newSchema.getFields().keySet());

        for (String fieldName : fieldNames) {
            String path = prefix.isBlank() ? fieldName : prefix + "." + fieldName;
            NormalizedField oldField = oldSchema.getFields().get(fieldName);
            NormalizedField newField = newSchema.getFields().get(fieldName);

            if (oldField == null) {
                changes.add(buildChange(
                        newField.isRequired() ? FieldChangeType.REQUIRED_ADDED : FieldChangeType.OPTIONAL_ADDED,
                        path,
                        null,
                        newField.getType(),
                        null,
                        newField.getDefaultValue()
                ));
                continue;
            }

            if (newField == null) {
                changes.add(buildChange(
                        FieldChangeType.REMOVED,
                        path,
                        oldField.getType(),
                        null,
                        oldField.getDefaultValue(),
                        null
                ));
                continue;
            }

            if (!Objects.equals(oldField.getType(), newField.getType())) {
                changes.add(buildChange(
                        FieldChangeType.TYPE_CHANGED,
                        path,
                        oldField.getType(),
                        newField.getType(),
                        oldField.getDefaultValue(),
                        newField.getDefaultValue()
                ));
                continue;
            }

            if (oldField.isNullable() != newField.isNullable()) {
                changes.add(buildChange(
                        FieldChangeType.NULLABILITY_CHANGED,
                        path,
                        oldField.getType(),
                        newField.getType(),
                        oldField.getDefaultValue(),
                        newField.getDefaultValue()
                ));
            }

            if (oldField.isRequired() && !newField.isRequired()) {
                changes.add(buildChange(
                        FieldChangeType.REQUIRED_BECAME_OPTIONAL,
                        path,
                        oldField.getType(),
                        newField.getType(),
                        oldField.getDefaultValue(),
                        newField.getDefaultValue()
                ));
            } else if (!oldField.isRequired() && newField.isRequired()) {
                changes.add(buildChange(
                        FieldChangeType.OPTIONAL_BECAME_REQUIRED,
                        path,
                        oldField.getType(),
                        newField.getType(),
                        oldField.getDefaultValue(),
                        newField.getDefaultValue()
                ));
            }

            if (!oldField.hasDefault() && newField.hasDefault()) {
                changes.add(buildChange(
                        FieldChangeType.DEFAULT_ADDED,
                        path,
                        oldField.getType(),
                        newField.getType(),
                        oldField.getDefaultValue(),
                        newField.getDefaultValue()
                ));
            } else if (oldField.hasDefault() && !newField.hasDefault()) {
                changes.add(buildChange(
                        FieldChangeType.DEFAULT_REMOVED,
                        path,
                        oldField.getType(),
                        newField.getType(),
                        oldField.getDefaultValue(),
                        newField.getDefaultValue()
                ));
            } else if (oldField.hasDefault()
                    && newField.hasDefault()
                    && !Objects.equals(oldField.getDefaultValue(), newField.getDefaultValue())) {
                changes.add(buildChange(
                        FieldChangeType.DEFAULT_CHANGED,
                        path,
                        oldField.getType(),
                        newField.getType(),
                        oldField.getDefaultValue(),
                        newField.getDefaultValue()
                ));
            }

            if ("record".equals(oldField.getType())
                    && "record".equals(newField.getType())
                    && oldField.getNestedSchema() != null
                    && newField.getNestedSchema() != null) {
                List<FieldChange> nestedChanges = diffFields(oldField.getNestedSchema(), newField.getNestedSchema(), path);
                if (!nestedChanges.isEmpty()) {
                    changes.add(buildChange(
                            FieldChangeType.NESTED_CHANGED,
                            path,
                            oldField.getType(),
                            newField.getType(),
                            oldField.getDefaultValue(),
                            newField.getDefaultValue()
                    ));
                }
            }
        }

        return changes;
    }

    private FieldChange buildChange(
            FieldChangeType changeType,
            String fieldName,
            String oldType,
            String newType,
            String oldDefault,
            String newDefault
    ) {
        return FieldChange.builder()
                .changeType(changeType)
                .fieldName(fieldName)
                .oldType(oldType)
                .newType(newType)
                .oldDefault(oldDefault)
                .newDefault(newDefault)
                .severity(changeSeverityPolicy.severityOf(changeType))
                .breaking(changeSeverityPolicy.isBreaking(changeType))
                .build();
    }
}




