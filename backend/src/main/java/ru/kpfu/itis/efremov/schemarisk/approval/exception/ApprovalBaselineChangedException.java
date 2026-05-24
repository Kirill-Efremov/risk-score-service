package ru.kpfu.itis.efremov.schemarisk.approval.exception;

public class ApprovalBaselineChangedException extends RuntimeException {

    public ApprovalBaselineChangedException() {
        super("Latest schema version has changed since approval request was created");
    }
}
