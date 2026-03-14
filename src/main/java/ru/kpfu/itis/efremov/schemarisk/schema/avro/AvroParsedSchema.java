package ru.kpfu.itis.efremov.schemarisk.schema.avro;

import org.apache.avro.Schema;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.schema.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.schema.SchemaReference;

import java.util.Collections;
import java.util.List;

public class AvroParsedSchema implements ParsedSchema {

    private final Schema avroSchema;
    private final String rawSchema;
    private final List<SchemaReference> references;

    public AvroParsedSchema(Schema avroSchema, String rawSchema, List<SchemaReference> references) {
        this.avroSchema = avroSchema;
        this.rawSchema = rawSchema;
        this.references = references != null ? references : Collections.emptyList();
    }

    public AvroParsedSchema(Schema avroSchema, String rawSchema) {
        this(avroSchema, rawSchema, Collections.emptyList());
    }

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.AVRO;
    }

    @Override
    public String canonicalString() {
        return avroSchema.toString();
    }

    @Override
    public String rawSchema() {
        return rawSchema;
    }

    @Override
    public List<SchemaReference> references() {
        return references;
    }

    public Schema getAvroSchema() {
        return avroSchema;
    }
}
