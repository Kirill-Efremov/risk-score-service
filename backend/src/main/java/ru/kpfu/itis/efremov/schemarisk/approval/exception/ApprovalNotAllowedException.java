package ru.kpfu.itis.efremov.schemarisk.approval.exception;

public class ApprovalNotAllowedException extends RuntimeException {

    public ApprovalNotAllowedException(String message) {
        super(message);
    }
}
