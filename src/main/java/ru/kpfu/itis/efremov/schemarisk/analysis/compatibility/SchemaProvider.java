package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

public interface SchemaProvider {

    SchemaType getSchemaType();

    ParsedSchema parseSchema(String schemaText);
}




