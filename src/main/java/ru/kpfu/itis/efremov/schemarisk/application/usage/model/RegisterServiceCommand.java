package ru.kpfu.itis.efremov.schemarisk.application.usage.model;

public record RegisterServiceCommand(
        String name,
        boolean critical
) {
}
