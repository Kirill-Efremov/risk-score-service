package ru.kpfu.itis.efremov.schemarisk.usage.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус использования схемы сервисом")
public enum UsageStatus {
    ACTIVE,
    MIGRATING,
    DEPRECATED
}




