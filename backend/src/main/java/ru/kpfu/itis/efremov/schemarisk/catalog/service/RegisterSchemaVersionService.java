package ru.kpfu.itis.efremov.schemarisk.catalog.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;

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




