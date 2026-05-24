package ru.kpfu.itis.efremov.schemarisk.auth.exception;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException() {
        super("Authentication is required");
    }
}
