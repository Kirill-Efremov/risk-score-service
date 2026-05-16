package ru.kpfu.itis.efremov.schemarisk.common.exception;

public class InvalidSchemaException extends RuntimeException {

    public InvalidSchemaException(String message) {
        super(message);
    }

    public InvalidSchemaException(String message, Throwable cause) {
        super(message, cause);
    }
}




