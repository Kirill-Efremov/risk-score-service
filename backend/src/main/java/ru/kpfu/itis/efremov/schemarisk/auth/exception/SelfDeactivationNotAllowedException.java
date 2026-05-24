package ru.kpfu.itis.efremov.schemarisk.auth.exception;

public class SelfDeactivationNotAllowedException extends RuntimeException {

    public SelfDeactivationNotAllowedException() {
        super("Self deactivation is not allowed");
    }
}
