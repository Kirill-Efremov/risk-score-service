package ru.kpfu.itis.efremov.schemarisk.auth.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("User already exists: " + username);
    }
}
