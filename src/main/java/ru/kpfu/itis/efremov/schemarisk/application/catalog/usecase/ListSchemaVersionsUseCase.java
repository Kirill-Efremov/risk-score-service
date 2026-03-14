package ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.application.port.SchemaCatalogPort;

import java.util.List;

@Service
public class ListSchemaVersionsUseCase {

    private final SchemaCatalogPort schemaCatalogPort;

    public ListSchemaVersionsUseCase(SchemaCatalogPort schemaCatalogPort) {
        this.schemaCatalogPort = schemaCatalogPort;
    }

    public List<SchemaVersionInfo> getVersions(String subject) {
        return schemaCatalogPort.listVersions(subject);
    }
}
