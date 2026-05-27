package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ProtobufParsedSchema.ProtoEnum;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ProtobufParsedSchema.ProtoEnumValue;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ProtobufParsedSchema.ProtoField;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ProtobufParsedSchema.ProtoMessage;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ProtobufParsedSchema.ProtoOneof;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidSchemaException;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProtobufSchemaProvider implements SchemaProvider {

    private static final Pattern SYNTAX_PATTERN = Pattern.compile("syntax\\s*=\\s*\"(proto2|proto3)\"\\s*;");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w.]+)\\s*;");
    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "(?m)^\\s*(?:(optional|required|repeated)\\s+)?([\\w.]+)\\s+(\\w+)\\s*=\\s*(\\d+)\\s*(?:\\[[^]]*])?\\s*;"
    );
    private static final Pattern MAP_FIELD_PATTERN = Pattern.compile(
            "(?m)^\\s*map\\s*<\\s*([\\w.]+)\\s*,\\s*([\\w.]+)\\s*>\\s+(\\w+)\\s*=\\s*(\\d+)\\s*(?:\\[[^]]*])?\\s*;"
    );
    private static final Pattern ENUM_VALUE_PATTERN = Pattern.compile("(?m)^\\s*(\\w+)\\s*=\\s*(-?\\d+)\\s*(?:\\[[^]]*])?\\s*;");
    private static final Pattern RESERVED_PATTERN = Pattern.compile("reserved\\s+([^;]+);");

    private final ChangeSeverityPolicy changeSeverityPolicy;

    public ProtobufSchemaProvider(ChangeSeverityPolicy changeSeverityPolicy) {
        this.changeSeverityPolicy = changeSeverityPolicy;
    }

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.PROTOBUF;
    }

    @Override
    public ParsedSchema parseSchema(String schemaText) {
        if (schemaText == null || schemaText.isBlank()) {
            throw new InvalidSchemaException("Protobuf schema text must not be null or blank");
        }

        String sanitized = stripComments(schemaText);
        String syntax = matchFirst(SYNTAX_PATTERN, sanitized);
        if (syntax == null) {
            syntax = "proto3";
        }
        String packageName = matchFirst(PACKAGE_PATTERN, sanitized);
        Map<String, ProtoMessage> messages = new LinkedHashMap<>();
        Map<String, ProtoEnum> enums = new LinkedHashMap<>();
        parseTopLevel(sanitized, packageName, messages, enums);

        if (messages.isEmpty()) {
            throw new InvalidSchemaException("Protobuf schema must contain at least one message definition");
        }
        return new ProtobufParsedSchema(schemaText, syntax, packageName, Map.copyOf(messages), Map.copyOf(enums));
    }

    @Override
    public CompatibilityResult checkCompatibility(
            ParsedSchema oldSchema,
            ParsedSchema newSchema,
            CompatibilityMode mode
    ) {
        DiffResult diff = diff(oldSchema, newSchema);
        List<Issue> issues = mode == CompatibilityMode.NONE ? List.of() : diff.getChanges().stream()
                .filter(change -> isIncompatible(change.getChangeType()))
                .map(change -> Issue.builder()
                        .code("PROTO_" + change.getChangeType().name())
                        .message("Protobuf change is potentially incompatible: "
                                + change.getChangeType().name() + " at " + change.getFieldName())
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
        ProtobufParsedSchema oldProto = (ProtobufParsedSchema) oldSchema;
        ProtobufParsedSchema newProto = (ProtobufParsedSchema) newSchema;
        List<FieldChange> changes = new ArrayList<>();

        Set<String> messageNames = new LinkedHashSet<>();
        messageNames.addAll(oldProto.messages().keySet());
        messageNames.addAll(newProto.messages().keySet());

        for (String messageName : messageNames) {
            ProtoMessage oldMessage = oldProto.messages().get(messageName);
            ProtoMessage newMessage = newProto.messages().get(messageName);
            if (oldMessage == null || newMessage == null) {
                changes.add(buildChange(
                        oldMessage == null ? FieldChangeType.OPTIONAL_ADDED : FieldChangeType.REMOVED,
                        messageName,
                        oldMessage == null ? null : "message",
                        newMessage == null ? null : "message"
                ));
                continue;
            }
            diffFields(oldMessage, newMessage, changes);
            diffReserved(oldMessage.fullName(), oldMessage.reservedNumbers(), newMessage.reservedNumbers(), changes);
            diffOneofs(oldMessage, newMessage, changes);
        }

        diffEnums(oldProto.enums(), newProto.enums(), changes);

        return DiffResult.builder()
                .schemaName(messageNames.stream().findFirst().orElse("Protobuf"))
                .changes(changes)
                .build();
    }

    private void parseTopLevel(
            String schemaText,
            String packageName,
            Map<String, ProtoMessage> messages,
            Map<String, ProtoEnum> enums
    ) {
        for (Block block : findBlocks(schemaText, "message")) {
            parseMessage(block.name(), qualify(packageName, block.name()), block.body(), messages, enums);
        }
        String schemaWithoutMessages = blankBlocks(schemaText, Set.of("message"));
        for (Block block : findBlocks(schemaWithoutMessages, "enum")) {
            ProtoEnum protoEnum = parseEnum(block.name(), qualify(packageName, block.name()), block.body());
            enums.put(protoEnum.fullName(), protoEnum);
        }
    }

    private ProtoMessage parseMessage(
            String name,
            String fullName,
            String body,
            Map<String, ProtoMessage> messages,
            Map<String, ProtoEnum> enums
    ) {
        Map<Integer, ProtoField> fieldsByNumber = new LinkedHashMap<>();
        Map<String, ProtoField> fieldsByName = new LinkedHashMap<>();
        Map<String, ProtoOneof> oneofs = new LinkedHashMap<>();

        String bodyWithoutNestedTypes = blankBlocks(body, Set.of("message", "enum"));
        String bodyWithoutNestedMessages = blankBlocks(body, Set.of("message"));
        for (Block oneofBlock : findBlocks(bodyWithoutNestedTypes, "oneof")) {
            Map<Integer, ProtoField> oneofFields = parseFields(fullName, oneofBlock.body(), oneofBlock.name());
            oneofFields.values().forEach(field -> addField(fullName, fieldsByNumber, fieldsByName, field));
            oneofs.put(oneofBlock.name(), new ProtoOneof(oneofBlock.name(), Map.copyOf(oneofFields)));
        }

        String shallowBody = blankBlocks(body, Set.of("message", "enum", "oneof"));
        parseFields(fullName, shallowBody, null).values()
                .forEach(field -> addField(fullName, fieldsByNumber, fieldsByName, field));

        ReservedValues reserved = parseReserved(shallowBody);
        ProtoMessage message = new ProtoMessage(
                name,
                fullName,
                Map.copyOf(fieldsByNumber),
                Map.copyOf(fieldsByName),
                Map.copyOf(oneofs),
                reserved.numbers(),
                reserved.names()
        );
        messages.put(fullName, message);

        for (Block nestedMessage : findBlocks(body, "message")) {
            parseMessage(
                    nestedMessage.name(),
                    fullName + "." + nestedMessage.name(),
                    nestedMessage.body(),
                    messages,
                    enums
            );
        }
        for (Block nestedEnum : findBlocks(bodyWithoutNestedMessages, "enum")) {
            ProtoEnum protoEnum = parseEnum(nestedEnum.name(), fullName + "." + nestedEnum.name(), nestedEnum.body());
            enums.put(protoEnum.fullName(), protoEnum);
        }

        return message;
    }

    private Map<Integer, ProtoField> parseFields(String messageName, String body, String oneofName) {
        Map<Integer, ProtoField> fields = new LinkedHashMap<>();

        Matcher mapMatcher = MAP_FIELD_PATTERN.matcher(body);
        while (mapMatcher.find()) {
            String keyType = mapMatcher.group(1);
            String valueType = mapMatcher.group(2);
            String fieldName = mapMatcher.group(3);
            int number = parsePositiveNumber(messageName, fieldName, mapMatcher.group(4));
            fields.put(number, new ProtoField(
                    messageName,
                    "map<" + keyType + "," + valueType + ">",
                    fieldName,
                    number,
                    "repeated",
                    true,
                    false,
                    false,
                    true,
                    keyType,
                    valueType,
                    oneofName
            ));
        }

        Matcher fieldMatcher = FIELD_PATTERN.matcher(body);
        while (fieldMatcher.find()) {
            if (isInsideMapDeclaration(body, fieldMatcher.start())) {
                continue;
            }
            String label = fieldMatcher.group(1);
            String fieldType = fieldMatcher.group(2);
            String fieldName = fieldMatcher.group(3);
            int number = parsePositiveNumber(messageName, fieldName, fieldMatcher.group(4));
            fields.put(number, new ProtoField(
                    messageName,
                    fieldType,
                    fieldName,
                    number,
                    label,
                    "repeated".equals(label),
                    "optional".equals(label),
                    "required".equals(label),
                    false,
                    null,
                    null,
                    oneofName
            ));
        }
        return fields;
    }

    private ProtoEnum parseEnum(String name, String fullName, String body) {
        Map<Integer, ProtoEnumValue> byNumber = new LinkedHashMap<>();
        Map<String, ProtoEnumValue> byName = new LinkedHashMap<>();
        Matcher matcher = ENUM_VALUE_PATTERN.matcher(blankBlocks(body, Set.of("message", "enum", "oneof")));
        while (matcher.find()) {
            String valueName = matcher.group(1);
            int number = Integer.parseInt(matcher.group(2));
            ProtoEnumValue value = new ProtoEnumValue(valueName, number);
            byNumber.put(number, value);
            byName.put(valueName, value);
        }
        ReservedValues reserved = parseReserved(body);
        return new ProtoEnum(name, fullName, Map.copyOf(byNumber), Map.copyOf(byName), reserved.numbers(), reserved.names());
    }

    private void addField(
            String messageName,
            Map<Integer, ProtoField> fieldsByNumber,
            Map<String, ProtoField> fieldsByName,
            ProtoField field
    ) {
        if (fieldsByNumber.containsKey(field.number())) {
            throw new InvalidSchemaException("Duplicate Protobuf field number: " + messageName + "." + field.number());
        }
        fieldsByNumber.put(field.number(), field);
        fieldsByName.put(field.name(), field);
    }

    private void diffFields(ProtoMessage oldMessage, ProtoMessage newMessage, List<FieldChange> changes) {
        Set<Integer> fieldNumbers = new LinkedHashSet<>();
        fieldNumbers.addAll(oldMessage.fieldsByNumber().keySet());
        fieldNumbers.addAll(newMessage.fieldsByNumber().keySet());

        for (Integer number : fieldNumbers) {
            ProtoField oldField = oldMessage.fieldsByNumber().get(number);
            ProtoField newField = newMessage.fieldsByNumber().get(number);
            if (oldField == null) {
                changes.add(buildChange(
                        newField.required() ? FieldChangeType.REQUIRED_ADDED : FieldChangeType.OPTIONAL_ADDED,
                        fieldPath(newField),
                        null,
                        newField.type()
                ));
                continue;
            }
            if (newField == null) {
                FieldChangeType type = newMessage.reservedNumbers().contains(number)
                        ? FieldChangeType.REMOVED
                        : FieldChangeType.FIELD_REMOVED_WITHOUT_RESERVED;
                changes.add(buildChange(type, fieldPath(oldField), oldField.type(), null));
                continue;
            }

            compareField(oldField, newField, changes);
        }
    }

    private void compareField(ProtoField oldField, ProtoField newField, List<FieldChange> changes) {
        boolean nameChanged = !Objects.equals(oldField.name(), newField.name());
        boolean typeChanged = !Objects.equals(oldField.type(), newField.type());
        if (nameChanged && typeChanged) {
            changes.add(buildChange(FieldChangeType.FIELD_NUMBER_REUSED, fieldPath(newField), oldField.type(), newField.type()));
            return;
        }
        if (typeChanged) {
            FieldChangeType changeType = Objects.equals(wireType(oldField.type()), wireType(newField.type()))
                    ? FieldChangeType.TYPE_CHANGED
                    : FieldChangeType.WIRE_TYPE_CHANGED;
            changes.add(buildChange(changeType, fieldPath(newField), oldField.type(), newField.type()));
        }
        if (nameChanged) {
            changes.add(buildChange(FieldChangeType.FIELD_NAME_CHANGED, fieldPath(newField), oldField.name(), newField.name()));
        }
        if (!Objects.equals(oldField.label(), newField.label())) {
            changes.add(buildChange(FieldChangeType.FIELD_LABEL_CHANGED, fieldPath(newField), oldField.label(), newField.label()));
        }
        if (oldField.map() && newField.map()
                && (!Objects.equals(oldField.mapKeyType(), newField.mapKeyType())
                || !Objects.equals(oldField.mapValueType(), newField.mapValueType()))) {
            changes.add(buildChange(FieldChangeType.MAP_TYPE_CHANGED, fieldPath(newField), oldField.type(), newField.type()));
        }
        if (oldField.oneofName() == null && newField.oneofName() != null) {
            changes.add(buildChange(FieldChangeType.FIELD_MOVED_TO_ONEOF, fieldPath(newField), null, newField.oneofName()));
        } else if (oldField.oneofName() != null && newField.oneofName() == null) {
            changes.add(buildChange(FieldChangeType.FIELD_REMOVED_FROM_ONEOF, fieldPath(newField), oldField.oneofName(), null));
        } else if (!Objects.equals(oldField.oneofName(), newField.oneofName())) {
            changes.add(buildChange(FieldChangeType.FIELD_MOVED_TO_ONEOF, fieldPath(newField), oldField.oneofName(), newField.oneofName()));
        }
    }

    private void diffReserved(String owner, Set<Integer> oldNumbers, Set<Integer> newNumbers, List<FieldChange> changes) {
        for (Integer number : newNumbers) {
            if (!oldNumbers.contains(number)) {
                changes.add(buildChange(FieldChangeType.RESERVED_NUMBER_ADDED, owner + ".reserved." + number, null, String.valueOf(number)));
            }
        }
        for (Integer number : oldNumbers) {
            if (!newNumbers.contains(number)) {
                changes.add(buildChange(FieldChangeType.RESERVED_NUMBER_REMOVED, owner + ".reserved." + number, String.valueOf(number), null));
            }
        }
    }

    private void diffOneofs(ProtoMessage oldMessage, ProtoMessage newMessage, List<FieldChange> changes) {
        Set<String> names = new LinkedHashSet<>();
        names.addAll(oldMessage.oneofs().keySet());
        names.addAll(newMessage.oneofs().keySet());
        for (String name : names) {
            if (!oldMessage.oneofs().containsKey(name)) {
                changes.add(buildChange(FieldChangeType.ONEOF_ADDED, newMessage.fullName() + ".oneof." + name, null, "oneof"));
            } else if (!newMessage.oneofs().containsKey(name)) {
                changes.add(buildChange(FieldChangeType.ONEOF_REMOVED, oldMessage.fullName() + ".oneof." + name, "oneof", null));
            }
        }
    }

    private void diffEnums(Map<String, ProtoEnum> oldEnums, Map<String, ProtoEnum> newEnums, List<FieldChange> changes) {
        Set<String> enumNames = new LinkedHashSet<>();
        enumNames.addAll(oldEnums.keySet());
        enumNames.addAll(newEnums.keySet());
        for (String enumName : enumNames) {
            ProtoEnum oldEnum = oldEnums.get(enumName);
            ProtoEnum newEnum = newEnums.get(enumName);
            if (oldEnum == null || newEnum == null) {
                changes.add(buildChange(oldEnum == null ? FieldChangeType.OPTIONAL_ADDED : FieldChangeType.REMOVED,
                        enumName, oldEnum == null ? null : "enum", newEnum == null ? null : "enum"));
                continue;
            }
            Set<Integer> numbers = new LinkedHashSet<>();
            numbers.addAll(oldEnum.valuesByNumber().keySet());
            numbers.addAll(newEnum.valuesByNumber().keySet());
            for (Integer number : numbers) {
                ProtoEnumValue oldValue = oldEnum.valuesByNumber().get(number);
                ProtoEnumValue newValue = newEnum.valuesByNumber().get(number);
                if (oldValue == null) {
                    changes.add(buildChange(FieldChangeType.ENUM_VALUE_ADDED, enumName + "." + newValue.name(), null, String.valueOf(number)));
                } else if (newValue == null) {
                    changes.add(buildChange(FieldChangeType.ENUM_VALUE_REMOVED, enumName + "." + oldValue.name(), String.valueOf(number), null));
                } else if (!Objects.equals(oldValue.name(), newValue.name())) {
                    changes.add(buildChange(FieldChangeType.ENUM_VALUE_NUMBER_REUSED, enumName + "." + newValue.name(), oldValue.name(), newValue.name()));
                }
            }
            diffReserved(enumName, oldEnum.reservedNumbers(), newEnum.reservedNumbers(), changes);
        }
    }

    private boolean isIncompatible(FieldChangeType changeType) {
        return switch (changeType) {
            case FIELD_NUMBER_REUSED, WIRE_TYPE_CHANGED, FIELD_REMOVED_WITHOUT_RESERVED, REQUIRED_ADDED,
                    FIELD_LABEL_CHANGED, FIELD_MOVED_TO_ONEOF, FIELD_REMOVED_FROM_ONEOF, ONEOF_REMOVED,
                    ENUM_VALUE_NUMBER_REUSED, ENUM_VALUE_REMOVED, MAP_TYPE_CHANGED -> true;
            default -> false;
        };
    }

    private List<Block> findBlocks(String text, String keyword) {
        List<Block> blocks = new ArrayList<>();
        Pattern startPattern = Pattern.compile("\\b" + keyword + "\\s+(\\w+)\\s*\\{");
        Matcher matcher = startPattern.matcher(text);
        while (matcher.find()) {
            int openBrace = text.indexOf('{', matcher.start());
            int closeBrace = findMatchingBrace(text, openBrace);
            if (closeBrace < 0) {
                throw new InvalidSchemaException("Invalid Protobuf " + keyword + " block: " + matcher.group(1));
            }
            blocks.add(new Block(matcher.group(1), text.substring(openBrace + 1, closeBrace), matcher.start(), closeBrace + 1));
            matcher.region(closeBrace + 1, text.length());
        }
        return blocks;
    }

    private String blankBlocks(String text, Set<String> keywords) {
        char[] chars = text.toCharArray();
        for (String keyword : keywords) {
            for (Block block : findBlocks(text, keyword)) {
                for (int index = block.start(); index < block.end(); index++) {
                    chars[index] = ' ';
                }
            }
        }
        return new String(chars);
    }

    private int findMatchingBrace(String text, int openBrace) {
        int depth = 0;
        for (int index = openBrace; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    private ReservedValues parseReserved(String body) {
        Set<Integer> numbers = new LinkedHashSet<>();
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = RESERVED_PATTERN.matcher(body);
        while (matcher.find()) {
            String value = matcher.group(1);
            if (value.contains("\"")) {
                Matcher nameMatcher = Pattern.compile("\"([^\"]+)\"").matcher(value);
                while (nameMatcher.find()) {
                    names.add(nameMatcher.group(1));
                }
                continue;
            }
            for (String part : value.split(",")) {
                String token = part.trim();
                if (token.contains("to")) {
                    String[] range = token.split("\\s+to\\s+");
                    int start = Integer.parseInt(range[0].trim());
                    int end = "max".equals(range[1].trim()) ? start : Integer.parseInt(range[1].trim());
                    for (int number = start; number <= end; number++) {
                        numbers.add(number);
                    }
                } else if (!token.isBlank()) {
                    numbers.add(Integer.parseInt(token));
                }
            }
        }
        return new ReservedValues(Set.copyOf(numbers), Set.copyOf(names));
    }

    private String stripComments(String schemaText) {
        return schemaText
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)//.*$", "");
    }

    private String matchFirst(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private int parsePositiveNumber(String messageName, String fieldName, String rawNumber) {
        int fieldNumber = Integer.parseInt(rawNumber);
        if (fieldNumber <= 0) {
            throw new InvalidSchemaException("Protobuf field number must be positive: " + messageName + "." + fieldName);
        }
        return fieldNumber;
    }

    private boolean isInsideMapDeclaration(String body, int start) {
        int lineStart = body.lastIndexOf('\n', start);
        String linePrefix = body.substring(lineStart + 1, start);
        return linePrefix.contains("map<");
    }

    private String qualify(String packageName, String name) {
        return packageName == null || packageName.isBlank() ? name : packageName + "." + name;
    }

    private String fieldPath(ProtoField field) {
        if (field.oneofName() == null) {
            return field.messageName() + "." + field.name();
        }
        return field.messageName() + ".oneof." + field.oneofName() + "." + field.name();
    }

    private String wireType(String type) {
        if (type == null) {
            return "unknown";
        }
        if (Set.of("int32", "int64", "uint32", "uint64", "sint32", "sint64", "bool", "enum").contains(type)) {
            return "varint";
        }
        if (Set.of("fixed64", "sfixed64", "double").contains(type)) {
            return "fixed64";
        }
        if (Set.of("string", "bytes").contains(type) || type.startsWith("map<") || Character.isUpperCase(type.charAt(0))) {
            return "length-delimited";
        }
        if (Set.of("fixed32", "sfixed32", "float").contains(type)) {
            return "fixed32";
        }
        return "message";
    }

    private FieldChange buildChange(FieldChangeType changeType, String fieldName, String oldType, String newType) {
        return FieldChange.builder()
                .changeType(changeType)
                .fieldName(fieldName)
                .oldType(oldType)
                .newType(newType)
                .severity(changeSeverityPolicy.severityOf(changeType))
                .breaking(changeSeverityPolicy.isBreaking(changeType))
                .build();
    }

    private record Block(String name, String body, int start, int end) {
    }

    private record ReservedValues(Set<Integer> numbers, Set<String> names) {
    }
}
