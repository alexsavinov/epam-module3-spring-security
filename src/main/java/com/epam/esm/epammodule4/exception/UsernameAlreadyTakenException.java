package com.epam.esm.epammodule4.exception;

public class UsernameAlreadyTakenException extends RuntimeException {

    public UsernameAlreadyTakenException(String message) {
        super(message);
    }
}
