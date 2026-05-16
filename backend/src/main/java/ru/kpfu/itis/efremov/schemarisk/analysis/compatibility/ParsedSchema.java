package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.util.List;

public interface ParsedSchema {

    SchemaType getSchemaType();

    String canonicalString();

    String rawSchema();

    List<SchemaReference> references();
}




