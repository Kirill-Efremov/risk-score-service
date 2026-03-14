package ru.kpfu.itis.efremov.schemarisk.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckResponse;
import ru.kpfu.itis.efremov.schemarisk.core.engine.SchemaCheckService;
import ru.kpfu.itis.efremov.schemarisk.core.engine.SchemaCheckService.ResultWithAll;

@RestController
@RequestMapping("/api/v1/checks")
public class SchemaCheckController {

    private final SchemaCheckService schemaCheckService;

    public SchemaCheckController(SchemaCheckService schemaCheckService) {
        this.schemaCheckService = schemaCheckService;
    }

    @PostMapping
    public ResponseEntity<SchemaCheckResponse> check(@RequestBody SchemaCheckRequest request) {
        ResultWithAll all = schemaCheckService.checkSchemas(request);

        return ResponseEntity.ok(
                SchemaCheckResponse.fromResult(
                        all.compatibilityResult(),
                        all.diffResult(),
                        all.riskResult(),
                        all.recommendations()
                )
        );
    }
}
