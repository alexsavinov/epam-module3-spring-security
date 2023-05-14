package com.epam.esm.epammodule4.exception;

public class UserCannotDeleteException extends RuntimeException {

    public UserCannotDeleteException(String message) {
        super(message);
    }
}
