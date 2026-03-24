package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.NormalizedField;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.NormalizedSchema;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.SchemaProvider;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidSchemaException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AvroSchemaProvider implements SchemaProvider {

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.AVRO;
    }

    @Override
    public ParsedSchema parseSchema(String schemaText) {
        if (schemaText == null || schemaText.isBlank()) {
            throw new InvalidSchemaException("Schema text must not be null or blank");
        }

        try {
            Schema.Parser parser = new Schema.Parser();
            Schema avroSchema = parser.parse(schemaText);
            return new AvroParsedSchema(avroSchema, schemaText);
        } catch (SchemaParseException e) {
            throw new InvalidSchemaException("Invalid Avro schema: " + e.getMessage(), e);
        }
    }

    public NormalizedSchema normalize(AvroParsedSchema parsedSchema) {
        return normalizeSchema(parsedSchema.getAvroSchema());
    }

    private NormalizedSchema normalizeSchema(Schema schema) {
        if (schema.getType() != Schema.Type.RECORD) {
            throw new InvalidSchemaException("Normalization supports only Avro record schemas");
        }

        Map<String, NormalizedField> fields = new LinkedHashMap<>();
        for (Schema.Field field : schema.getFields()) {
            NormalizedField normalizedField = normalizeField(field);
            fields.put(normalizedField.getName(), normalizedField);
        }

        return new NormalizedSchema(schema.getFullName(), fields);
    }

    private NormalizedField normalizeField(Schema.Field field) {
        SchemaInfo schemaInfo = unwrap(field.schema());
        return NormalizedField.of(
                field.name(),
                schemaInfo.typeName(),
                schemaInfo.nullable(),
                field.hasDefaultValue(),
                defaultToString(field.defaultVal()),
                schemaInfo.nestedSchema()
        );
    }

    private SchemaInfo unwrap(Schema schema) {
        if (schema.getType() != Schema.Type.UNION) {
            return new SchemaInfo(schema.getType().getName(), false, nestedSchemaOf(schema));
        }

        List<Schema> nonNullTypes = schema.getTypes().stream()
                .filter(candidate -> candidate.getType() != Schema.Type.NULL)
                .toList();
        boolean nullable = nonNullTypes.size() != schema.getTypes().size();

        if (nonNullTypes.size() == 1) {
            Schema actualSchema = nonNullTypes.get(0);
            return new SchemaInfo(actualSchema.getType().getName(), nullable, nestedSchemaOf(actualSchema));
        }

        return new SchemaInfo(schema.toString(), nullable, null);
    }

    private NormalizedSchema nestedSchemaOf(Schema schema) {
        if (schema.getType() != Schema.Type.RECORD) {
            return null;
        }
        return normalizeSchema(schema);
    }

    private String defaultToString(Object defaultValue) {
        return defaultValue == null ? null : defaultValue.toString();
    }

    private record SchemaInfo(String typeName, boolean nullable, NormalizedSchema nestedSchema) {
    }
}




