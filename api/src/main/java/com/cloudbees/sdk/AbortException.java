package com.cloudbees.sdk;

/**
 * Throw this exception when you are handling an user error gracefully.
 *
 * When this exception is caught, only the exception message is rendered and not its stack trace.
 * This is useful for "expected" problems where you do error checks on what the user did, and when
 * the user can correct the problem without knowing where the problem came from.
 *
 * @author Kohsuke Kawaguchi
 */
public class AbortException extends RuntimeException {
    public AbortException() {
    }

    public AbortException(String message) {
        super(message);
    }
}
