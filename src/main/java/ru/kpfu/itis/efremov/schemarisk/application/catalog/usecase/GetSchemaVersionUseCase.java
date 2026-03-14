package ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.application.port.SchemaCatalogPort;

@Service
public class GetSchemaVersionUseCase {

    private final SchemaCatalogPort schemaCatalogPort;

    public GetSchemaVersionUseCase(SchemaCatalogPort schemaCatalogPort) {
        this.schemaCatalogPort = schemaCatalogPort;
    }

    public SchemaVersionInfo getVersion(String subject, int version) {
        return schemaCatalogPort.getVersion(subject, version);
    }
}
