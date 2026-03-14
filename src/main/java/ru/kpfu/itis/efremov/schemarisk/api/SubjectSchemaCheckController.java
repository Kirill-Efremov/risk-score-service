package ru.kpfu.itis.efremov.schemarisk.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.VersionedSchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeResult;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeVersionedSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeVersionedSchemaChangeUseCase;

@Validated
@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectSchemaCheckController {

    private final AnalyzeVersionedSchemaChangeUseCase analyzeVersionedSchemaChangeUseCase;

    public SubjectSchemaCheckController(AnalyzeVersionedSchemaChangeUseCase analyzeVersionedSchemaChangeUseCase) {
        this.analyzeVersionedSchemaChangeUseCase = analyzeVersionedSchemaChangeUseCase;
    }

    @PostMapping("/{subject}/checks")
    public ResponseEntity<SchemaCheckResponse> check(
            @PathVariable @NotBlank(message = "subject must not be blank") String subject,
            @Valid @RequestBody VersionedSchemaCheckRequest request
    ) {
        AnalyzeSchemaChangeResult result = analyzeVersionedSchemaChangeUseCase.analyze(
                new AnalyzeVersionedSchemaChangeCommand(
                        subject,
                        request.getOldVersion(),
                        request.getNewVersion(),
                        request.getNewSchema(),
                        request.getSchemaType(),
                        request.getCompatibilityMode()
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
