package ru.kpfu.itis.efremov.schemarisk.schema;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

public interface SchemaProvider {

    SchemaType getSchemaType();

    ParsedSchema parseSchema(String schemaText);
}
