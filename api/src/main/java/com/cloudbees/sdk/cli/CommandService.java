package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.AbortException;

import java.io.IOException;

/**
 * Resolves command names to their implementations.
 * 
 * <p>
 * This interface isn't intended to be implemented by plugins. It is rather a representation of the contract
 * from the CLI environment to plugins. You can inject this component to your code by using this interface.
 * 
 * <p>
 * Bees CLI implements {@link CommandService} to encapsulate automatic plugin discovery logic.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface CommandService {
    /**
     * Resolves a command to its implementation.
     *
     * @param name
     *      Name of the command, such as "app:deploy", "help", or "foo:bar".
     * @return
     *      null if the command of the said name is not found.
     * @throws IOException
     *      If a fatal error occurs during the retrieval of the command implementation.
     * @throws AbortException
     *      If an anticipated problem happens, this exception is thrown. The caller must
     *      handle this exception without reporting a stack trace.
     */
    ICommand getCommand(String name) throws IOException, AbortException;

}
