package ru.kpfu.itis.efremov.schemarisk.auth.exception;

public class LastAdminCannotBeDisabledException extends RuntimeException {

    public LastAdminCannotBeDisabledException() {
        super("Last active admin cannot be disabled or downgraded");
    }
}
