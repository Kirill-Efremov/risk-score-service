package ru.kpfu.itis.efremov.schemarisk.common.exception;

public class ServiceNotFoundException extends RuntimeException {

    public ServiceNotFoundException(Long serviceId) {
        super("Service not found: " + serviceId);
    }
}
