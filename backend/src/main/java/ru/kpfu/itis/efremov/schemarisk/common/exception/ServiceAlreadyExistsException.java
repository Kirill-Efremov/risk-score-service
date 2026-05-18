package ru.kpfu.itis.efremov.schemarisk.common.exception;

public class ServiceAlreadyExistsException extends RuntimeException {

    public ServiceAlreadyExistsException(String serviceName) {
        super("Service already exists: " + serviceName);
    }
}
