package com.cloudbees.sdk;

/**
 * @author Fabian Donze
 */
public class BeesSecurityException extends RuntimeException {
    public BeesSecurityException() {
    }

    public BeesSecurityException(String message) {
        super(message);
    }

    public BeesSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeesSecurityException(Throwable cause) {
        super(cause);
    }
}
