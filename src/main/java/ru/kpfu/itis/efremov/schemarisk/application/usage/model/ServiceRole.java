package ru.kpfu.itis.efremov.schemarisk.application.usage.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роль сервиса относительно схемы")
public enum ServiceRole {
    PRODUCER,
    CONSUMER
}
