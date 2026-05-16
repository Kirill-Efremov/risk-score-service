package ru.kpfu.itis.efremov.schemarisk.system;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SystemStatusResponse;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class SystemStatusService {

    private final SchemaCatalog schemaCatalog;
    private final DataSource dataSource;

    public SystemStatusService(SchemaCatalog schemaCatalog, DataSource dataSource) {
        this.schemaCatalog = schemaCatalog;
        this.dataSource = dataSource;
    }

    public SystemStatusResponse getStatus() {
        return new SystemStatusResponse("UP", checkSchemaRegistry(), checkDatabase());
    }

    private String checkSchemaRegistry() {
        try {
            schemaCatalog.listSubjects();
            return "UP";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private String checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception exception) {
            return "DOWN";
        }
    }
}
