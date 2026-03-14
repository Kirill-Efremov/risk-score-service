package ru.kpfu.itis.efremov.schemarisk.support.exception;

public class InvalidSchemaException extends RuntimeException {

    public InvalidSchemaException(String message) {
        super(message);
    }

    public InvalidSchemaException(String message, Throwable cause) {
        super(message, cause);
    }
}
