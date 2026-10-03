package com.carrental.exception;

/** Thrown for bad credentials, duplicate email on register, inactive account, etc. */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
