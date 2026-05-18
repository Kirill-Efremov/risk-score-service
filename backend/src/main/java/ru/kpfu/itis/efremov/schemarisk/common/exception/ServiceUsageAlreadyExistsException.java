package ru.kpfu.itis.efremov.schemarisk.common.exception;

public class ServiceUsageAlreadyExistsException extends RuntimeException {

    public ServiceUsageAlreadyExistsException(
            Long serviceId,
            String subject,
            Integer version,
            String role
    ) {
        super("Active service usage already exists: serviceId="
                + serviceId
                + ", subject="
                + subject
                + ", version="
                + version
                + ", role="
                + role);
    }
}
