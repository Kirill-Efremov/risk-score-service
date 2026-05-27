package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.common.exception.UnsupportedSchemaTypeException;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;

public interface SchemaProvider {

    SchemaType getSchemaType();

    ParsedSchema parseSchema(String schemaText);

    default CompatibilityResult checkCompatibility(
            ParsedSchema oldSchema,
            ParsedSchema newSchema,
            CompatibilityMode mode
    ) {
        throw new UnsupportedSchemaTypeException("Compatibility check not implemented for type: " + getSchemaType());
    }

    default DiffResult diff(ParsedSchema oldSchema, ParsedSchema newSchema) {
        throw new UnsupportedSchemaTypeException("Diff not implemented for type: " + getSchemaType());
    }
}




