package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.apache.avro.SchemaCompatibility.SchemaPairCompatibility;
import org.springframework.stereotype.Component;

import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidRequestException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.UnsupportedSchemaTypeException;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

@Component
public class CompatibilityEngine {

    public CompatibilityResult check(
            ParsedSchema oldSchema,
            ParsedSchema newSchema,
            CompatibilityMode mode
    ) {
        if (oldSchema.getSchemaType() != newSchema.getSchemaType()) {
            throw new InvalidRequestException("Schemas must have same type: old="
                    + oldSchema.getSchemaType() + ", new=" + newSchema.getSchemaType());
        }

        if (mode == CompatibilityMode.NONE) {
            return CompatibilityResult.builder()
                    .compatible(true)
                    .mode(mode)
                    .issues(Collections.emptyList())
                    .build();
        }

        if (oldSchema.getSchemaType() == SchemaType.AVRO) {
            return checkAvro((AvroParsedSchema) oldSchema, (AvroParsedSchema) newSchema, mode);
        }

        throw new UnsupportedSchemaTypeException("Compatibility check not implemented for type: "
                + oldSchema.getSchemaType());
    }

    private CompatibilityResult checkAvro(
            AvroParsedSchema oldSchema,
            AvroParsedSchema newSchema,
            CompatibilityMode mode
    ) {
        Schema oldAvro = oldSchema.getAvroSchema();
        Schema newAvro = newSchema.getAvroSchema();

        List<Issue> issues = new ArrayList<>();
        boolean compatible = true;

        switch (mode) {
            case BACKWARD -> {
                SchemaPairCompatibility pair =
                        SchemaCompatibility.checkReaderWriterCompatibility(newAvro, oldAvro);
                compatible = pair.getType() == SchemaCompatibilityType.COMPATIBLE;
                if (!compatible) {
                    issues.addAll(toIssues(pair, "BACKWARD"));
                }
            }
            case FORWARD -> {
                SchemaPairCompatibility pair =
                        SchemaCompatibility.checkReaderWriterCompatibility(oldAvro, newAvro);
                compatible = pair.getType() == SchemaCompatibilityType.COMPATIBLE;
                if (!compatible) {
                    issues.addAll(toIssues(pair, "FORWARD"));
                }
            }
            case FULL -> {
                SchemaPairCompatibility backward =
                        SchemaCompatibility.checkReaderWriterCompatibility(newAvro, oldAvro);
                SchemaPairCompatibility forward =
                        SchemaCompatibility.checkReaderWriterCompatibility(oldAvro, newAvro);

                boolean backwardOk = backward.getType() == SchemaCompatibilityType.COMPATIBLE;
                boolean forwardOk = forward.getType() == SchemaCompatibilityType.COMPATIBLE;

                compatible = backwardOk && forwardOk;

                if (!backwardOk) {
                    issues.addAll(toIssues(backward, "BACKWARD"));
                }
                if (!forwardOk) {
                    issues.addAll(toIssues(forward, "FORWARD"));
                }
            }
            case NONE -> {
                return CompatibilityResult.builder()
                        .compatible(true)
                        .mode(mode)
                        .issues(List.of())
                        .build();
            }
        }

        return CompatibilityResult.builder()
                .compatible(compatible)
                .mode(mode)
                .issues(issues)
                .build();
    }

    private List<Issue> toIssues(SchemaPairCompatibility pair, String direction) {
        if (pair.getResult() == null || pair.getResult().getIncompatibilities() == null) {
            return Collections.emptyList();
        }

        List<Issue> result = new ArrayList<>();
        pair.getResult().getIncompatibilities().forEach(inc -> {
            String code = "AVRO_" + direction + "_" + inc.getType().name();
            String message = "[" + direction + "] " + inc.getMessage();
            String location = inc.getLocation();

            result.add(Issue.builder()
                    .code(code)
                    .message(message)
                    .severity(IssueSeverity.ERROR)
                    .breakingChange(true)
                    .path(location)
                    .build());
        });
        return result;
    }
}




