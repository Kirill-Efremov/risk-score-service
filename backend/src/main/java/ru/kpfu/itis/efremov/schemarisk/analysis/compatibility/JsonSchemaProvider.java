package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.JsonParsedSchema.CompositionKind;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.JsonParsedSchema.JsonSchemaNode;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidSchemaException;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class JsonSchemaProvider implements SchemaProvider {

    private final ObjectMapper objectMapper;
    private final ChangeSeverityPolicy changeSeverityPolicy;

    public JsonSchemaProvider(ObjectMapper objectMapper, ChangeSeverityPolicy changeSeverityPolicy) {
        this.objectMapper = objectMapper;
        this.changeSeverityPolicy = changeSeverityPolicy;
    }

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.JSON_SCHEMA;
    }

    @Override
    public ParsedSchema parseSchema(String schemaText) {
        if (schemaText == null || schemaText.isBlank()) {
            throw new InvalidSchemaException("JSON Schema text must not be null or blank");
        }

        try {
            JsonNode root = objectMapper.readTree(schemaText);
            validateRoot(root);
            Map<String, JsonSchemaNode> nodes = new LinkedHashMap<>();
            flattenSchema(root, root, "", false, nodes, new LinkedHashSet<>());
            return new JsonParsedSchema(root, schemaText, Map.copyOf(nodes));
        } catch (JsonProcessingException exception) {
            throw new InvalidSchemaException("Invalid JSON Schema: " + exception.getOriginalMessage(), exception);
        }
    }

    @Override
    public CompatibilityResult checkCompatibility(
            ParsedSchema oldSchema,
            ParsedSchema newSchema,
            CompatibilityMode mode
    ) {
        DiffResult diff = diff(oldSchema, newSchema);
        List<Issue> issues = mode == CompatibilityMode.NONE ? List.of() : diff.getChanges().stream()
                .filter(change -> isIncompatible(change.getChangeType(), mode))
                .map(change -> Issue.builder()
                        .code("JSON_" + change.getChangeType().name())
                        .message(buildIssueMessage(change))
                        .severity(changeSeverityPolicy.severityOf(change.getChangeType()))
                        .breakingChange(true)
                        .path(change.getFieldName())
                        .build())
                .toList();

        return CompatibilityResult.builder()
                .compatible(mode == CompatibilityMode.NONE || issues.isEmpty())
                .mode(mode)
                .issues(issues)
                .build();
    }

    @Override
    public DiffResult diff(ParsedSchema oldSchema, ParsedSchema newSchema) {
        JsonParsedSchema oldJson = (JsonParsedSchema) oldSchema;
        JsonParsedSchema newJson = (JsonParsedSchema) newSchema;
        List<FieldChange> changes = new ArrayList<>();

        Set<String> paths = new LinkedHashSet<>();
        paths.addAll(oldJson.nodesByPath().keySet());
        paths.addAll(newJson.nodesByPath().keySet());

        for (String path : paths) {
            JsonSchemaNode oldNode = oldJson.nodesByPath().get(path);
            JsonSchemaNode newNode = newJson.nodesByPath().get(path);

            if (oldNode == null) {
                changes.add(buildChange(
                        newNode.required() ? FieldChangeType.REQUIRED_ADDED : FieldChangeType.OPTIONAL_ADDED,
                        path,
                        null,
                        newNode.typeLabel()
                ));
                continue;
            }
            if (newNode == null) {
                changes.add(buildChange(FieldChangeType.REMOVED, path, oldNode.typeLabel(), null));
                continue;
            }

            compareExistingNode(oldNode, newNode, changes);
        }

        return DiffResult.builder()
                .schemaName(resolveSchemaName(newJson.root()))
                .changes(changes)
                .build();
    }

    private void validateRoot(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new InvalidSchemaException("JSON Schema root must be a JSON object");
        }
        validateSchemaNode(root, "$");
    }

    private void validateSchemaNode(JsonNode node, String path) {
        if (node == null || !node.isObject()) {
            return;
        }
        JsonNode type = node.get("type");
        if (type != null && !type.isTextual() && !type.isArray()) {
            throw new InvalidSchemaException("JSON Schema 'type' must be a string or array at " + path);
        }
        JsonNode properties = node.get("properties");
        if (properties != null && !properties.isObject()) {
            throw new InvalidSchemaException("JSON Schema 'properties' must be an object at " + path);
        }
        JsonNode required = node.get("required");
        if (required != null && !required.isArray()) {
            throw new InvalidSchemaException("JSON Schema 'required' must be an array at " + path);
        }
        if (properties != null) {
            properties.fields().forEachRemaining(entry -> validateSchemaNode(entry.getValue(), path + "." + entry.getKey()));
        }
        JsonNode items = node.get("items");
        if (items != null && items.isObject()) {
            validateSchemaNode(items, path + "[]");
        }
    }

    private void flattenSchema(
            JsonNode root,
            JsonNode node,
            String path,
            boolean required,
            Map<String, JsonSchemaNode> nodes,
            Set<String> refStack
    ) {
        JsonNode resolved = resolveRef(root, node, refStack);
        if (resolved == null || !resolved.isObject()) {
            return;
        }

        Set<String> types = typesOf(resolved);
        JsonSchemaNode current = new JsonSchemaNode(
                path,
                types,
                required,
                enumValuesOf(resolved),
                resolved.get("const"),
                booleanValue(resolved.get("additionalProperties")),
                intValue(resolved.get("minLength")),
                intValue(resolved.get("maxLength")),
                textValue(resolved.get("pattern")),
                decimalValue(resolved.get("minimum")),
                decimalValue(resolved.get("maximum")),
                booleanValue(resolved.get("exclusiveMinimum")),
                booleanValue(resolved.get("exclusiveMaximum")),
                intValue(resolved.get("minItems")),
                intValue(resolved.get("maxItems")),
                booleanValue(resolved.get("uniqueItems")),
                itemTypesOf(root, resolved, refStack),
                compositionKindOf(resolved),
                resolved
        );
        nodes.put(path, current);

        Set<String> requiredProperties = requiredOf(resolved);
        JsonNode properties = resolved.get("properties");
        if (properties != null && properties.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = properties.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> property = iterator.next();
                String childPath = path.isBlank() ? property.getKey() : path + "." + property.getKey();
                flattenSchema(root, property.getValue(), childPath, requiredProperties.contains(property.getKey()), nodes, refStack);
            }
        }

        JsonNode items = resolved.get("items");
        if (items != null && items.isObject()) {
            String itemPath = path.isBlank() ? "[]" : path + "[]";
            flattenSchema(root, items, itemPath, false, nodes, refStack);
        }
    }

    private JsonNode resolveRef(JsonNode root, JsonNode node, Set<String> refStack) {
        JsonNode ref = node == null ? null : node.get("$ref");
        if (ref == null || !ref.isTextual()) {
            return node;
        }
        String pointer = ref.asText();
        if (!pointer.startsWith("#/") || refStack.contains(pointer)) {
            return node;
        }
        JsonNode target = root.at(pointer.substring(1));
        if (target.isMissingNode()) {
            return node;
        }
        refStack.add(pointer);
        JsonNode resolved = resolveRef(root, target, refStack);
        refStack.remove(pointer);
        return resolved;
    }

    private void compareExistingNode(JsonSchemaNode oldNode, JsonSchemaNode newNode, List<FieldChange> changes) {
        if (!Objects.equals(oldNode.types(), newNode.types())) {
            changes.add(buildChange(FieldChangeType.TYPE_CHANGED, newNode.path(), oldNode.typeLabel(), newNode.typeLabel()));
        }
        if (!oldNode.required() && newNode.required()) {
            changes.add(buildChange(FieldChangeType.OPTIONAL_BECAME_REQUIRED, newNode.path(), oldNode.typeLabel(), newNode.typeLabel()));
        } else if (oldNode.required() && !newNode.required()) {
            changes.add(buildChange(FieldChangeType.REQUIRED_BECAME_OPTIONAL, newNode.path(), oldNode.typeLabel(), newNode.typeLabel()));
        }
        compareEnum(oldNode, newNode, changes);
        compareConst(oldNode, newNode, changes);
        compareAdditionalProperties(oldNode, newNode, changes);
        compareStringConstraints(oldNode, newNode, changes);
        compareNumericConstraints(oldNode, newNode, changes);
        compareArrayConstraints(oldNode, newNode, changes);
        if (!Objects.equals(oldNode.compositionKind(), newNode.compositionKind())
                || !Objects.equals(compositionValue(oldNode), compositionValue(newNode))) {
            changes.add(buildChange(FieldChangeType.COMPOSITION_CHANGED, newNode.path(), label(oldNode.compositionKind()), label(newNode.compositionKind())));
        }
    }

    private void compareEnum(JsonSchemaNode oldNode, JsonSchemaNode newNode, List<FieldChange> changes) {
        if (oldNode.enumValues().isEmpty() && newNode.enumValues().isEmpty()) {
            return;
        }
        if (oldNode.enumValues().containsAll(newNode.enumValues()) && !newNode.enumValues().containsAll(oldNode.enumValues())) {
            changes.add(buildChange(FieldChangeType.ENUM_RESTRICTED, newNode.path(), oldNode.enumValues().toString(), newNode.enumValues().toString()));
        } else if (newNode.enumValues().containsAll(oldNode.enumValues()) && !oldNode.enumValues().containsAll(newNode.enumValues())) {
            changes.add(buildChange(FieldChangeType.ENUM_EXPANDED, newNode.path(), oldNode.enumValues().toString(), newNode.enumValues().toString()));
        } else if (!Objects.equals(oldNode.enumValues(), newNode.enumValues())) {
            changes.add(buildChange(FieldChangeType.ENUM_RESTRICTED, newNode.path(), oldNode.enumValues().toString(), newNode.enumValues().toString()));
        }
    }

    private void compareConst(JsonSchemaNode oldNode, JsonSchemaNode newNode, List<FieldChange> changes) {
        if (!Objects.equals(oldNode.constValue(), newNode.constValue())) {
            changes.add(buildChange(FieldChangeType.CONST_CHANGED, newNode.path(), label(oldNode.constValue()), label(newNode.constValue())));
        }
    }

    private void compareAdditionalProperties(JsonSchemaNode oldNode, JsonSchemaNode newNode, List<FieldChange> changes) {
        boolean oldAllowed = oldNode.additionalProperties() == null || oldNode.additionalProperties();
        boolean newAllowed = newNode.additionalProperties() == null || newNode.additionalProperties();
        if (oldAllowed && !newAllowed) {
            changes.add(buildChange(FieldChangeType.ADDITIONAL_PROPERTIES_DISABLED, newNode.path(), "true", "false"));
        } else if (!oldAllowed && newAllowed) {
            changes.add(buildChange(FieldChangeType.ADDITIONAL_PROPERTIES_ENABLED, newNode.path(), "false", "true"));
        }
    }

    private void compareStringConstraints(JsonSchemaNode oldNode, JsonSchemaNode newNode, List<FieldChange> changes) {
        boolean tightened = increased(oldNode.minLength(), newNode.minLength())
                || decreased(oldNode.maxLength(), newNode.maxLength())
                || changedToPresentOrDifferent(oldNode.pattern(), newNode.pattern());
        boolean relaxed = decreased(oldNode.minLength(), newNode.minLength())
                || increased(oldNode.maxLength(), newNode.maxLength())
                || (oldNode.pattern() != null && newNode.pattern() == null);
        if (tightened) {
            changes.add(buildChange(FieldChangeType.STRING_CONSTRAINT_TIGHTENED, newNode.path(), "string constraints", "stricter string constraints"));
        } else if (relaxed) {
            changes.add(buildChange(FieldChangeType.STRING_CONSTRAINT_RELAXED, newNode.path(), "string constraints", "relaxed string constraints"));
        }
    }

    private void compareNumericConstraints(JsonSchemaNode oldNode, JsonSchemaNode newNode, List<FieldChange> changes) {
        boolean tightened = increased(oldNode.minimum(), newNode.minimum())
                || decreased(oldNode.maximum(), newNode.maximum())
                || becameTrue(oldNode.exclusiveMinimum(), newNode.exclusiveMinimum())
                || becameTrue(oldNode.exclusiveMaximum(), newNode.exclusiveMaximum());
        boolean relaxed = decreased(oldNode.minimum(), newNode.minimum())
                || increased(oldNode.maximum(), newNode.maximum())
                || becameFalse(oldNode.exclusiveMinimum(), newNode.exclusiveMinimum())
                || becameFalse(oldNode.exclusiveMaximum(), newNode.exclusiveMaximum());
        if (tightened) {
            changes.add(buildChange(FieldChangeType.NUMERIC_CONSTRAINT_TIGHTENED, newNode.path(), "numeric constraints", "stricter numeric constraints"));
        } else if (relaxed) {
            changes.add(buildChange(FieldChangeType.NUMERIC_CONSTRAINT_RELAXED, newNode.path(), "numeric constraints", "relaxed numeric constraints"));
        }
    }

    private void compareArrayConstraints(JsonSchemaNode oldNode, JsonSchemaNode newNode, List<FieldChange> changes) {
        boolean changed = !Objects.equals(oldNode.minItems(), newNode.minItems())
                || !Objects.equals(oldNode.maxItems(), newNode.maxItems())
                || !Objects.equals(oldNode.uniqueItems(), newNode.uniqueItems())
                || !Objects.equals(oldNode.itemTypes(), newNode.itemTypes());
        if (changed) {
            changes.add(buildChange(FieldChangeType.ARRAY_CONSTRAINT_CHANGED, newNode.path(), oldNode.itemTypes(), newNode.itemTypes()));
        }
    }

    private boolean isIncompatible(FieldChangeType changeType, CompatibilityMode mode) {
        if (mode == CompatibilityMode.NONE) {
            return false;
        }
        return switch (changeType) {
            case REQUIRED_ADDED, OPTIONAL_BECAME_REQUIRED, REMOVED, TYPE_CHANGED, ENUM_RESTRICTED,
                    CONST_CHANGED, ADDITIONAL_PROPERTIES_DISABLED, STRING_CONSTRAINT_TIGHTENED,
                    NUMERIC_CONSTRAINT_TIGHTENED, ARRAY_CONSTRAINT_CHANGED, COMPOSITION_CHANGED -> true;
            default -> false;
        };
    }

    private Set<String> typesOf(JsonNode node) {
        Set<String> types = new LinkedHashSet<>();
        JsonNode type = node.get("type");
        if (type != null && type.isTextual()) {
            types.add(type.asText());
        } else if (type != null && type.isArray()) {
            type.forEach(item -> {
                if (item.isTextual()) {
                    types.add(item.asText());
                }
            });
        }
        if (types.isEmpty()) {
            if (node.has("properties")) {
                types.add("object");
            } else if (node.has("items")) {
                types.add("array");
            } else if (node.has("enum")) {
                types.add("enum");
            } else {
                types.add("unknown");
            }
        }
        return Set.copyOf(types);
    }

    private Set<String> enumValuesOf(JsonNode node) {
        Set<String> values = new LinkedHashSet<>();
        JsonNode enumNode = node.get("enum");
        if (enumNode != null && enumNode.isArray()) {
            enumNode.forEach(value -> values.add(value.toString()));
        }
        return Set.copyOf(values);
    }

    private Set<String> requiredOf(JsonNode root) {
        Set<String> required = new LinkedHashSet<>();
        JsonNode requiredNode = root.get("required");
        if (requiredNode == null || !requiredNode.isArray()) {
            return required;
        }
        requiredNode.forEach(item -> {
            if (item.isTextual()) {
                required.add(item.asText());
            }
        });
        return required;
    }

    private String itemTypesOf(JsonNode root, JsonNode node, Set<String> refStack) {
        JsonNode items = node.get("items");
        if (items == null || !items.isObject()) {
            return null;
        }
        JsonNode resolvedItems = resolveRef(root, items, refStack);
        return String.join("|", typesOf(resolvedItems));
    }

    private CompositionKind compositionKindOf(JsonNode node) {
        if (node.has("oneOf")) {
            return CompositionKind.ONE_OF;
        }
        if (node.has("anyOf")) {
            return CompositionKind.ANY_OF;
        }
        if (node.has("allOf")) {
            return CompositionKind.ALL_OF;
        }
        return null;
    }

    private String compositionValue(JsonSchemaNode node) {
        if (node.compositionKind() == null || node.rawNode() == null) {
            return null;
        }
        return switch (node.compositionKind()) {
            case ONE_OF -> label(node.rawNode().get("oneOf"));
            case ANY_OF -> label(node.rawNode().get("anyOf"));
            case ALL_OF -> label(node.rawNode().get("allOf"));
        };
    }

    private String resolveSchemaName(JsonNode root) {
        JsonNode title = root.get("title");
        if (title != null && title.isTextual()) {
            return title.asText();
        }
        JsonNode id = root.get("$id");
        return id != null && id.isTextual() ? id.asText() : "JSON Schema";
    }

    private FieldChange buildChange(FieldChangeType changeType, String fieldName, String oldType, String newType) {
        return FieldChange.builder()
                .changeType(changeType)
                .fieldName(fieldName == null || fieldName.isBlank() ? "$" : fieldName)
                .oldType(oldType)
                .newType(newType)
                .severity(changeSeverityPolicy.severityOf(changeType))
                .breaking(changeSeverityPolicy.isBreaking(changeType))
                .build();
    }

    private String buildIssueMessage(FieldChange change) {
        return "JSON Schema change is potentially incompatible: "
                + change.getChangeType().name() + " at " + change.getFieldName();
    }

    private Boolean booleanValue(JsonNode node) {
        return node != null && node.isBoolean() ? node.asBoolean() : null;
    }

    private Integer intValue(JsonNode node) {
        return node != null && node.isNumber() ? node.asInt() : null;
    }

    private BigDecimal decimalValue(JsonNode node) {
        return node != null && node.isNumber() ? node.decimalValue() : null;
    }

    private String textValue(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private String label(Object value) {
        return value == null ? null : value.toString();
    }

    private boolean increased(Integer oldValue, Integer newValue) {
        return oldValue != null && newValue != null && newValue > oldValue;
    }

    private boolean decreased(Integer oldValue, Integer newValue) {
        return oldValue != null && newValue != null && newValue < oldValue;
    }

    private boolean increased(BigDecimal oldValue, BigDecimal newValue) {
        return oldValue != null && newValue != null && newValue.compareTo(oldValue) > 0;
    }

    private boolean decreased(BigDecimal oldValue, BigDecimal newValue) {
        return oldValue != null && newValue != null && newValue.compareTo(oldValue) < 0;
    }

    private boolean changedToPresentOrDifferent(String oldValue, String newValue) {
        return newValue != null && !Objects.equals(oldValue, newValue);
    }

    private boolean becameTrue(Boolean oldValue, Boolean newValue) {
        return !Boolean.TRUE.equals(oldValue) && Boolean.TRUE.equals(newValue);
    }

    private boolean becameFalse(Boolean oldValue, Boolean newValue) {
        return Boolean.TRUE.equals(oldValue) && Boolean.FALSE.equals(newValue);
    }
}
