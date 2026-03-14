package ru.kpfu.itis.efremov.schemarisk.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.RegisterSchemaVersionRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaVersionResponse;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase.GetSchemaVersionUseCase;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase.ListSchemaVersionsUseCase;
import ru.kpfu.itis.efremov.schemarisk.application.catalog.usecase.RegisterSchemaVersionUseCase;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/subjects")
public class SchemaCatalogController {

    private final RegisterSchemaVersionUseCase registerSchemaVersionUseCase;
    private final ListSchemaVersionsUseCase listSchemaVersionsUseCase;
    private final GetSchemaVersionUseCase getSchemaVersionUseCase;

    public SchemaCatalogController(
            RegisterSchemaVersionUseCase registerSchemaVersionUseCase,
            ListSchemaVersionsUseCase listSchemaVersionsUseCase,
            GetSchemaVersionUseCase getSchemaVersionUseCase
    ) {
        this.registerSchemaVersionUseCase = registerSchemaVersionUseCase;
        this.listSchemaVersionsUseCase = listSchemaVersionsUseCase;
        this.getSchemaVersionUseCase = getSchemaVersionUseCase;
    }

    @PostMapping("/{subject}/versions")
    public ResponseEntity<SchemaVersionResponse> registerVersion(
            @PathVariable @NotBlank(message = "subject must not be blank") String subject,
            @Valid @RequestBody RegisterSchemaVersionRequest request
    ) {
        SchemaVersionResponse response = SchemaVersionResponse.fromInfo(
                registerSchemaVersionUseCase.register(
                        new RegisterSchemaVersionCommand(
                                subject,
                                request.getSchemaType(),
                                request.getDefaultCompatibilityMode(),
                                request.getDescription(),
                                request.getSchemaText(),
                                request.getStatus(),
                                request.getSourceType(),
                                request.getExternalSchemaId()
                        )
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{subject}/versions")
    public ResponseEntity<List<SchemaVersionResponse>> listVersions(
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        List<SchemaVersionResponse> response = listSchemaVersionsUseCase.getVersions(subject)
                .stream()
                .map(SchemaVersionResponse::fromInfo)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{subject}/versions/{version}")
    public ResponseEntity<SchemaVersionResponse> getVersion(
            @PathVariable @NotBlank(message = "subject must not be blank") String subject,
            @PathVariable @Positive(message = "version must be positive") int version
    ) {
        SchemaVersionResponse response = SchemaVersionResponse.fromInfo(
                getSchemaVersionUseCase.getVersion(subject, version)
        );
        return ResponseEntity.ok(response);
    }
}
