package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SystemStatusResponse;
import ru.kpfu.itis.efremov.schemarisk.system.SystemStatusService;

@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "System Status", description = "Статус backend и ключевых зависимостей")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping
    @Operation(summary = "Получить статус backend, Schema Registry и базы данных")
    public SystemStatusResponse getStatus() {
        return systemStatusService.getStatus();
    }
}
