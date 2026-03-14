package ru.kpfu.itis.efremov.schemarisk.core.diff;

import org.apache.avro.Schema;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.schema.avro.AvroParsedSchema;

import java.util.*;


@Service
public class AvroDiffService {

    public DiffResult diff(AvroParsedSchema oldSchema, AvroParsedSchema newSchema) {
        Schema oldRecord = oldSchema.getAvroSchema();
        Schema newRecord = newSchema.getAvroSchema();

        if (oldRecord.getType() != Schema.Type.RECORD || newRecord.getType() != Schema.Type.RECORD) {
            throw new IllegalArgumentException("AvroDiffService supports only record schemas");
        }

        Map<String, Schema.Field> oldFields = toFieldMap(oldRecord.getFields());
        Map<String, Schema.Field> newFields = toFieldMap(newRecord.getFields());

        List<FieldChange> changes = new ArrayList<>();

        for (Map.Entry<String, Schema.Field> entry : oldFields.entrySet()) {
            String name = entry.getKey();
            Schema.Field oldField = entry.getValue();
            Schema.Field newField = newFields.get(name);

            if (newField == null) {
                changes.add(FieldChange.builder()
                        .type(FieldChangeType.REMOVED)
                        .fieldName(name)
                        .oldType(oldField.schema().toString())
                        .newType(null)
                        .oldDefault(defaultToString(oldField.defaultVal()))
                        .newDefault(null)
                        .build());
            } else {
                String oldType = oldField.schema().toString();
                String newType = newField.schema().toString();
                Object oldDef = oldField.defaultVal();
                Object newDef = newField.defaultVal();

                if (!Objects.equals(oldType, newType)) {
                    changes.add(FieldChange.builder()
                            .type(FieldChangeType.TYPE_CHANGED)
                            .fieldName(name)
                            .oldType(oldType)
                            .newType(newType)
                            .oldDefault(defaultToString(oldDef))
                            .newDefault(defaultToString(newDef))
                            .build());
                } else if (!Objects.equals(defaultToString(oldDef), defaultToString(newDef))) {
                    FieldChangeType changeType =
                            oldDef == null && newDef != null ? FieldChangeType.DEFAULT_ADDED :
                                    oldDef != null && newDef == null ? FieldChangeType.DEFAULT_REMOVED :
                                            FieldChangeType.OTHER;

                    changes.add(FieldChange.builder()
                            .type(changeType)
                            .fieldName(name)
                            .oldType(oldType)
                            .newType(newType)
                            .oldDefault(defaultToString(oldDef))
                            .newDefault(defaultToString(newDef))
                            .build());
                }
            }
        }

        for (Map.Entry<String, Schema.Field> entry : newFields.entrySet()) {
            String name = entry.getKey();
            if (!oldFields.containsKey(name)) {
                Schema.Field newField = entry.getValue();
                changes.add(FieldChange.builder()
                        .type(FieldChangeType.ADDED)
                        .fieldName(name)
                        .oldType(null)
                        .newType(newField.schema().toString())
                        .oldDefault(null)
                        .newDefault(defaultToString(newField.defaultVal()))
                        .build());
            }
        }

        return DiffResult.builder()
                .schemaName(newRecord.getFullName())
                .changes(changes)
                .build();
    }

    private Map<String, Schema.Field> toFieldMap(List<Schema.Field> fields) {
        Map<String, Schema.Field> map = new LinkedHashMap<>();
        for (Schema.Field f : fields) {
            map.put(f.name(), f);
        }
        return map;
    }

    private String defaultToString(Object def) {
        return def == null ? null : def.toString();
    }
}
