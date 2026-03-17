package ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.application.port.SchemaCatalog;

import java.util.List;

@Service
public class ListSchemaVersionsService {

    private final SchemaCatalog schemaCatalog;

    public ListSchemaVersionsService(SchemaCatalog schemaCatalog) {
        this.schemaCatalog = schemaCatalog;
    }

    public List<SchemaVersionInfo> getVersions(String subject) {
        return schemaCatalog.listVersions(subject);
    }
}
