package ru.kpfu.itis.efremov.schemarisk.catalog.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;

@Service
public class GetSchemaVersionService {

    private final SchemaCatalog schemaCatalog;

    public GetSchemaVersionService(SchemaCatalog schemaCatalog) {
        this.schemaCatalog = schemaCatalog;
    }

    public SchemaVersionInfo getVersion(String subject, int version) {
        return schemaCatalog.getVersion(subject, version);
    }
}




