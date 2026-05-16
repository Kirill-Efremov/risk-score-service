package ru.kpfu.itis.efremov.schemarisk.catalog.exception;

public class SchemaRegistryConflictException extends RuntimeException {

    private final int registryStatus;
    private final String registryResponseBody;

    public SchemaRegistryConflictException(String message, int registryStatus, String registryResponseBody) {
        super(message);
        this.registryStatus = registryStatus;
        this.registryResponseBody = registryResponseBody;
    }

    public int getRegistryStatus() {
        return registryStatus;
    }

    public String getRegistryResponseBody() {
        return registryResponseBody;
    }
}
