package ru.kpfu.itis.efremov.schemarisk.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Режим проверки совместимости")
public enum CompatibilityMode {
    NONE,
    BACKWARD,
    FORWARD,
    FULL
}





