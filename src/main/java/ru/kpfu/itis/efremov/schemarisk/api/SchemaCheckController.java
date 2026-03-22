package ru.kpfu.itis.efremov.schemarisk.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.AnalysisRecordResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckResponse;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.usecase.GetAnalysisByIdService;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeResult;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeService;

@Validated
@RestController
@RequestMapping("/api/v1/checks")
public class SchemaCheckController {

    private final AnalyzeSchemaChangeService analyzeSchemaChangeService;
    private final GetAnalysisByIdService getAnalysisByIdService;

    public SchemaCheckController(
            AnalyzeSchemaChangeService analyzeSchemaChangeService,
            GetAnalysisByIdService getAnalysisByIdService
    ) {
        this.analyzeSchemaChangeService = analyzeSchemaChangeService;
        this.getAnalysisByIdService = getAnalysisByIdService;
    }

    @PostMapping
    public ResponseEntity<SchemaCheckResponse> check(@Valid @RequestBody SchemaCheckRequest request) {
        AnalyzeSchemaChangeResult result = analyzeSchemaChangeService.analyze(
                new AnalyzeSchemaChangeCommand(
                        request.getSchemaType(),
                        request.getCompatibilityMode(),
                        request.getOldSchema(),
                        request.getNewSchema()
                )
        );

        return ResponseEntity.ok(
                SchemaCheckResponse.fromResult(result)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisRecordResponse> getById(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(
                AnalysisRecordResponse.fromRecord(getAnalysisByIdService.getById(id))
        );
    }
}
