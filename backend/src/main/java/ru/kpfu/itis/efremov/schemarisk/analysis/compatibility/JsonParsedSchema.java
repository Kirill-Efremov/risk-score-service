package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import com.fasterxml.jackson.databind.JsonNode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonParsedSchema implements ParsedSchema {

    private final JsonNode root;
    private final String rawSchema;
    private final Map<String, JsonSchemaNode> nodesByPath;

    public JsonParsedSchema(JsonNode root, String rawSchema, Map<String, JsonSchemaNode> nodesByPath) {
        this.root = root;
        this.rawSchema = rawSchema;
        this.nodesByPath = nodesByPath;
    }

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.JSON_SCHEMA;
    }

    @Override
    public String canonicalString() {
        return root.toString();
    }

    @Override
    public String rawSchema() {
        return rawSchema;
    }

    @Override
    public List<SchemaReference> references() {
        return List.of();
    }

    public JsonNode root() {
        return root;
    }

    public Map<String, JsonSchemaNode> nodesByPath() {
        return nodesByPath;
    }

    public enum CompositionKind {
        ONE_OF,
        ANY_OF,
        ALL_OF
    }

    public record JsonSchemaNode(
            String path,
            Set<String> types,
            boolean required,
            Set<String> enumValues,
            JsonNode constValue,
            Boolean additionalProperties,
            Integer minLength,
            Integer maxLength,
            String pattern,
            BigDecimal minimum,
            BigDecimal maximum,
            Boolean exclusiveMinimum,
            Boolean exclusiveMaximum,
            Integer minItems,
            Integer maxItems,
            Boolean uniqueItems,
            String itemTypes,
            CompositionKind compositionKind,
            JsonNode rawNode
    ) {
        public String typeLabel() {
            if (types == null || types.isEmpty()) {
                return "unknown";
            }
            return String.join("|", types);
        }
    }
}
