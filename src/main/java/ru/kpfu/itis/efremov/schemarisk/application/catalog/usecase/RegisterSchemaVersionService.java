package ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.application.port.SchemaCatalog;

@Service
public class RegisterSchemaVersionService {

    private final SchemaCatalog schemaCatalog;

    public RegisterSchemaVersionService(SchemaCatalog schemaCatalog) {
        this.schemaCatalog = schemaCatalog;
    }

    public SchemaVersionInfo register(RegisterSchemaVersionCommand command) {
        return schemaCatalog.registerSchemaVersion(command);
    }
}
