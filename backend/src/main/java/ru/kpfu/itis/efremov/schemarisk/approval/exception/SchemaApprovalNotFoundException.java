package ru.kpfu.itis.efremov.schemarisk.approval.exception;

public class SchemaApprovalNotFoundException extends RuntimeException {

    public SchemaApprovalNotFoundException(Long approvalId) {
        super("Schema approval not found: " + approvalId);
    }
}
