package ru.kpfu.itis.efremov.schemarisk.approval.exception;

public class InvalidApprovalStateException extends RuntimeException {

    public InvalidApprovalStateException(String message) {
        super(message);
    }
}
