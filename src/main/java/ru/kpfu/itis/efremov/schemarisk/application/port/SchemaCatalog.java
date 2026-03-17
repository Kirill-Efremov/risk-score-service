package ru.kpfu.itis.efremov.schemarisk.application.port;

import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;

import java.util.List;

public interface SchemaCatalog {

    SchemaVersionInfo getLatestVersion(String subject);

    SchemaVersionInfo getVersion(String subject, int version);

    List<SchemaVersionInfo> listVersions(String subject);

    SchemaVersionInfo registerSchemaVersion(RegisterSchemaVersionCommand command);
}
