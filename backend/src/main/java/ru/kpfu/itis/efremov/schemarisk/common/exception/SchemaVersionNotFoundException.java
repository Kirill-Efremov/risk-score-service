package ru.kpfu.itis.efremov.schemarisk.common.exception;

public class SchemaVersionNotFoundException extends RuntimeException {

    public SchemaVersionNotFoundException(String subject, Integer version) {
        super("Schema version not found: subject=" + subject + ", version=" + version);
    }
}
