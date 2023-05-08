package com.epam.esm.epammodule4.exception;

public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String token, String message) {
        super(message);
    }
}
