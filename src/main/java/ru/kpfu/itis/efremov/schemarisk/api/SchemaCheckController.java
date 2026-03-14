package ru.kpfu.itis.efremov.schemarisk.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckResponse;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeResult;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeUseCase;

@RestController
@RequestMapping("/api/v1/checks")
public class SchemaCheckController {

    private final AnalyzeSchemaChangeUseCase analyzeSchemaChangeUseCase;

    public SchemaCheckController(AnalyzeSchemaChangeUseCase analyzeSchemaChangeUseCase) {
        this.analyzeSchemaChangeUseCase = analyzeSchemaChangeUseCase;
    }

    @PostMapping
    public ResponseEntity<SchemaCheckResponse> check(@Valid @RequestBody SchemaCheckRequest request) {
        AnalyzeSchemaChangeResult result = analyzeSchemaChangeUseCase.analyze(
                new AnalyzeSchemaChangeCommand(
                        request.getSchemaType(),
                        request.getCompatibilityMode(),
                        request.getOldSchema(),
                        request.getNewSchema()
                )
        );

        return ResponseEntity.ok(
                SchemaCheckResponse.fromResult(
                        result.compatibilityResult(),
                        result.diffResult(),
                        result.riskResult(),
                        result.recommendations()
                )
        );
    }
}
