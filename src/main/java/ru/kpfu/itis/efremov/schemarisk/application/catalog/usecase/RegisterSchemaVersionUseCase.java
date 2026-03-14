package ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.application.port.SchemaCatalogPort;

@Service
public class RegisterSchemaVersionUseCase {

    private final SchemaCatalogPort schemaCatalogPort;

    public RegisterSchemaVersionUseCase(SchemaCatalogPort schemaCatalogPort) {
        this.schemaCatalogPort = schemaCatalogPort;
    }

    public SchemaVersionInfo register(RegisterSchemaVersionCommand command) {
        return schemaCatalogPort.registerSchemaVersion(command);
    }
}
