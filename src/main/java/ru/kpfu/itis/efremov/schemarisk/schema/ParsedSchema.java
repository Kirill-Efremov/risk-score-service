package ru.kpfu.itis.efremov.schemarisk.schema;

import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

import java.util.List;


public interface ParsedSchema {

    SchemaType getSchemaType();

    String canonicalString();

    String rawSchema();

    List<SchemaReference> references();
}
