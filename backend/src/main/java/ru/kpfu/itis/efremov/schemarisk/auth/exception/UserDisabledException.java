package ru.kpfu.itis.efremov.schemarisk.auth.exception;

public class UserDisabledException extends RuntimeException {

    public UserDisabledException(String username) {
        super("User is disabled: " + username);
    }
}
