package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionStatus;
import ru.kpfu.itis.efremov.schemarisk.application.port.SchemaCatalogPort;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.support.exception.InvalidRequestException;

@Component
public class VersionedSchemaChangeResolver {

    private final SchemaCatalogPort schemaCatalogPort;

    public VersionedSchemaChangeResolver(SchemaCatalogPort schemaCatalogPort) {
        this.schemaCatalogPort = schemaCatalogPort;
    }

    public ResolvedVersionedSchemaChange resolve(AnalyzeVersionedSchemaChangeCommand command) {
        SchemaVersionInfo oldSchemaVersion = resolveOldSchema(command);
        SchemaVersionInfo newSchemaVersion = resolveNewSchema(command, oldSchemaVersion);
        SchemaType schemaType = resolveSchemaType(command, oldSchemaVersion, newSchemaVersion);

        return new ResolvedVersionedSchemaChange(
                schemaType,
                oldSchemaVersion.schemaText(),
                newSchemaVersion.schemaText(),
                oldSchemaVersion,
                newSchemaVersion
        );
    }

    private SchemaVersionInfo resolveOldSchema(AnalyzeVersionedSchemaChangeCommand command) {
        if (command.oldVersion() != null) {
            return schemaCatalogPort.getVersion(command.subject(), command.oldVersion());
        }
        return schemaCatalogPort.getLatestVersion(command.subject());
    }

    private SchemaVersionInfo resolveNewSchema(
            AnalyzeVersionedSchemaChangeCommand command,
            SchemaVersionInfo oldSchemaVersion
    ) {
        if (command.newVersion() != null) {
            return schemaCatalogPort.getVersion(command.subject(), command.newVersion());
        }

        if (command.newSchema() == null || command.newSchema().isBlank()) {
            throw new InvalidRequestException("Either newVersion or newSchema must be provided");
        }

        return schemaCatalogPort.registerSchemaVersion(
                new RegisterSchemaVersionCommand(
                        command.subject(),
                        resolveSchemaType(command, oldSchemaVersion, null),
                        oldSchemaVersion.subject().defaultCompatibilityMode(),
                        oldSchemaVersion.subject().description(),
                        command.newSchema(),
                        SchemaVersionStatus.DRAFT,
                        SchemaSourceType.LOCAL,
                        null
                )
        );
    }

    private SchemaType resolveSchemaType(
            AnalyzeVersionedSchemaChangeCommand command,
            SchemaVersionInfo oldSchemaVersion,
            SchemaVersionInfo newSchemaVersion
    ) {
        SchemaType subjectType = oldSchemaVersion.subject().schemaType();
        if (newSchemaVersion != null && newSchemaVersion.subject().schemaType() != subjectType) {
            throw new InvalidRequestException("Schema type mismatch between old and new versions");
        }

        if (command.schemaType() != null && command.schemaType() != subjectType) {
            throw new InvalidRequestException("Provided schemaType does not match subject schema type");
        }

        return subjectType;
    }
}
