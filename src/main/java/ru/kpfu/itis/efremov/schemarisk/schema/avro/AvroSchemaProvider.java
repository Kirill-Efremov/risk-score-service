package ru.kpfu.itis.efremov.schemarisk.schema.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.schema.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.schema.SchemaProvider;

@Component
public class AvroSchemaProvider implements SchemaProvider {

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.AVRO;
    }

    @Override
    public ParsedSchema parseSchema(String schemaText) {
        if (schemaText == null || schemaText.isBlank()) {
            throw new IllegalArgumentException("Schema text must not be null or blank");
        }

        try {
            Schema.Parser parser = new Schema.Parser();
            Schema avroSchema = parser.parse(schemaText);
            return new AvroParsedSchema(avroSchema, schemaText);
        } catch (SchemaParseException e) {
            throw new IllegalArgumentException("Invalid Avro schema: " + e.getMessage(), e);
        }
    }
}
