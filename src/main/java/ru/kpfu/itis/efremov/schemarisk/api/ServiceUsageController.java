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
import ru.kpfu.itis.efremov.schemarisk.api.dto.RegisterServiceRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.RegisterServiceUsageRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ServiceResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ServiceUsageResponse;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.service.ServiceUsageService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
public class ServiceUsageController {

    private final ServiceUsageService serviceUsageService;

    public ServiceUsageController(ServiceUsageService serviceUsageService) {
        this.serviceUsageService = serviceUsageService;
    }

    @PostMapping("/services")
    public ResponseEntity<ServiceResponse> registerService(@Valid @RequestBody RegisterServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ServiceResponse.fromInfo(
                        serviceUsageService.registerService(
                                new RegisterServiceCommand(request.getName(), request.isCritical())
                        )
                )
        );
    }

    @PostMapping("/services/{serviceId}/usages")
    public ResponseEntity<ServiceUsageResponse> registerUsage(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @Valid @RequestBody RegisterServiceUsageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ServiceUsageResponse.fromInfo(
                        serviceUsageService.registerUsage(
                                new RegisterServiceUsageCommand(
                                        serviceId,
                                        request.getSubject(),
                                        request.getVersion(),
                                        request.getRole(),
                                        request.getActive()
                                )
                        )
                )
        );
    }

    @GetMapping("/subjects/{subject}/usages")
    public ResponseEntity<List<ServiceUsageResponse>> getUsageBySubject(
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        return ResponseEntity.ok(
                serviceUsageService.getUsageBySubject(subject).stream()
                        .map(ServiceUsageResponse::fromInfo)
                        .toList()
        );
    }
}
