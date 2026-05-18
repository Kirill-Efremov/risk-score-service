package ru.kpfu.itis.efremov.schemarisk.common.exception;

public class ServiceUsageNotFoundException extends RuntimeException {

    public ServiceUsageNotFoundException(Long usageId) {
        super("Service usage not found: " + usageId);
    }
}
