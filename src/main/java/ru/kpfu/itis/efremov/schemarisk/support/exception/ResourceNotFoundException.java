package ru.kpfu.itis.efremov.schemarisk.support.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
