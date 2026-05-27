package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProtobufParsedSchema implements ParsedSchema {

    private final String rawSchema;
    private final String syntax;
    private final String packageName;
    private final Map<String, ProtoMessage> messages;
    private final Map<String, ProtoEnum> enums;

    public ProtobufParsedSchema(
            String rawSchema,
            String syntax,
            String packageName,
            Map<String, ProtoMessage> messages,
            Map<String, ProtoEnum> enums
    ) {
        this.rawSchema = rawSchema;
        this.syntax = syntax;
        this.packageName = packageName;
        this.messages = messages;
        this.enums = enums;
    }

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.PROTOBUF;
    }

    @Override
    public String canonicalString() {
        return rawSchema;
    }

    @Override
    public String rawSchema() {
        return rawSchema;
    }

    @Override
    public List<SchemaReference> references() {
        return List.of();
    }

    public Map<String, ProtoMessage> messages() {
        return messages;
    }

    public String syntax() {
        return syntax;
    }

    public String packageName() {
        return packageName;
    }

    public Map<String, ProtoEnum> enums() {
        return enums;
    }

    public record ProtoMessage(
            String name,
            String fullName,
            Map<Integer, ProtoField> fieldsByNumber,
            Map<String, ProtoField> fieldsByName,
            Map<String, ProtoOneof> oneofs,
            Set<Integer> reservedNumbers,
            Set<String> reservedNames
    ) {
    }

    public record ProtoField(
            String messageName,
            String type,
            String name,
            int number,
            String label,
            boolean repeated,
            boolean optional,
            boolean required,
            boolean map,
            String mapKeyType,
            String mapValueType,
            String oneofName
    ) {
    }

    public record ProtoOneof(String name, Map<Integer, ProtoField> fieldsByNumber) {
    }

    public record ProtoEnum(
            String name,
            String fullName,
            Map<Integer, ProtoEnumValue> valuesByNumber,
            Map<String, ProtoEnumValue> valuesByName,
            Set<Integer> reservedNumbers,
            Set<String> reservedNames
    ) {
    }

    public record ProtoEnumValue(String name, int number) {
    }
}
