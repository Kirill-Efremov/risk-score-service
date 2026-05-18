package ru.kpfu.itis.efremov.schemarisk.usage.model;

public record RegisterServiceCommand(
        String name,
        boolean critical,
        String owner,
        String description
) {
}




