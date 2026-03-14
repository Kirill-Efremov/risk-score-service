package ru.kpfu.itis.efremov.schemarisk.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<SchemaCheckResponse> check(@Valid @RequestBody SchemaCheckRequest request) {
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
