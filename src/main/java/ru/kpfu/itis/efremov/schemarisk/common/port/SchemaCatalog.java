package ru.kpfu.itis.efremov.schemarisk.common.port;

import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;

import java.util.List;

public interface SchemaCatalog {

    SchemaVersionInfo getLatestVersion(String subject);

    SchemaVersionInfo getVersion(String subject, int version);

    List<SchemaVersionInfo> listVersions(String subject);

    SchemaVersionInfo registerSchemaVersion(RegisterSchemaVersionCommand command);
}




