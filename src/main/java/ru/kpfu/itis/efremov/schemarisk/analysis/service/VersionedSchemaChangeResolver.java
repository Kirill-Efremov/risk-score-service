package ru.kpfu.itis.efremov.schemarisk.analysis.service;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeVersionedSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.ResolvedVersionedSchemaChange;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionStatus;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidRequestException;

@Component
public class VersionedSchemaChangeResolver {

    private final SchemaCatalog schemaCatalog;

    public VersionedSchemaChangeResolver(SchemaCatalog schemaCatalog) {
        this.schemaCatalog = schemaCatalog;
    }

    public ResolvedVersionedSchemaChange resolve(AnalyzeVersionedSchemaChangeCommand command) {
        SchemaVersionInfo oldSchemaVersion = resolveOldSchema(command);
        SchemaVersionInfo newSchemaVersion = resolveNewSchema(command, oldSchemaVersion);
        SchemaType schemaType = resolveSchemaType(command, oldSchemaVersion, newSchemaVersion);

        return new ResolvedVersionedSchemaChange(
                command.subject(),
                schemaType,
                oldSchemaVersion.schemaText(),
                newSchemaVersion.schemaText(),
                oldSchemaVersion,
                newSchemaVersion
        );
    }

    private SchemaVersionInfo resolveOldSchema(AnalyzeVersionedSchemaChangeCommand command) {
        if (command.oldVersion() != null) {
            return schemaCatalog.getVersion(command.subject(), command.oldVersion());
        }
        return schemaCatalog.getLatestVersion(command.subject());
    }

    private SchemaVersionInfo resolveNewSchema(
            AnalyzeVersionedSchemaChangeCommand command,
            SchemaVersionInfo oldSchemaVersion
    ) {
        if (command.newVersion() != null) {
            return schemaCatalog.getVersion(command.subject(), command.newVersion());
        }

        if (command.newSchema() == null || command.newSchema().isBlank()) {
            throw new InvalidRequestException("Either newVersion or newSchema must be provided");
        }

        return schemaCatalog.registerSchemaVersion(
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
        SchemaType subjectType = oldSchemaVersion.schemaType();
        if (newSchemaVersion != null && newSchemaVersion.schemaType() != subjectType) {
            throw new InvalidRequestException("Schema type mismatch between old and new versions");
        }

        if (command.schemaType() != null && command.schemaType() != subjectType) {
            throw new InvalidRequestException("Provided schemaType does not match subject schema type");
        }

        return subjectType;
    }
}

