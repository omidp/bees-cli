package com.cloudbees.sdk;

/**
 * Throw this exception when you are handling an user error gracefully.
 *
 * <p>
 * When this exception is caught, only the exception message is rendered and not its stack trace.
 * This is useful for "expected" problems where you do error checks on what the user did, and when
 * the user can correct the problem without knowing where the problem came from.
 *
 * <p>
 * This is NOT for the situation where the error is due to some underlying failure or a failure in
 * the external system. In such case, use other exception types so that the CLI will report the stack
 * trace. Often those errors cannot be diagnosed by the user himself, and the support staff needs
 * to see the stack trace to understand the structure of the problem.
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
