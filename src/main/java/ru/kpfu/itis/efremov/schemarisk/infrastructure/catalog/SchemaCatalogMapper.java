package ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaSubjectInfo;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaSubjectEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaVersionEntity;

@Component
public class SchemaCatalogMapper {

    public SchemaSubjectInfo toSubjectInfo(SchemaSubjectEntity entity) {
        return new SchemaSubjectInfo(
                entity.getId(),
                entity.getName(),
                entity.getSchemaType(),
                entity.getDefaultCompatibilityMode(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }

    public SchemaVersionInfo toVersionInfo(SchemaVersionEntity entity) {
        return new SchemaVersionInfo(
                entity.getId(),
                toSubjectInfo(entity.getSubject()),
                entity.getVersion(),
                entity.getSchemaText(),
                entity.getSchemaHash(),
                entity.getStatus(),
                entity.getSourceType(),
                entity.getExternalSchemaId(),
                entity.getCreatedAt()
        );
    }
}
