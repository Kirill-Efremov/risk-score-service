package ru.kpfu.itis.efremov.schemarisk.common.exception;

public class SchemaSubjectNotFoundException extends RuntimeException {

    public SchemaSubjectNotFoundException(String subject) {
        super("Schema subject not found: " + subject);
    }
}
