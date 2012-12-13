package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.AbortException;
import com.cloudbees.sdk.extensibility.ExtensionPoint;

import java.util.List;

/**
 * A command in Bees CLI.
 *
 * This is an extension point, as we allow plugins to implement custom commands.
 *
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public abstract class ACommand {

    /**
     * Executes this command.
     *
     * @param args
     *        Never empty, never null. The first argument is the command name itself,
     *        followed by arguments
     * @return
     *        The exit code of the command. 99 is apparently used for something special that I haven't figured out.
     */
    public int run(List<String> args) throws Exception {
        parse(args);
        return invoke();
    }

    /**
     * Configures this object by parsing command line arguments.
     *
     * @param args
     *        Never empty, never null. The first argument is the command name itself,
     *        followed by arguments
     * @throws AbortException
     *      Indicates a graceful failure. An implementation can report an error in the arguments to stderr
     *      in a human readable fashion, and throw this exception to indicate that the error is already
     *      conveyed to the user. The caller will print {@link AbortException#getMessage()} if that's non-null,
     *      but it shouldn't display the stack trace of the exception.
     * @throws Exception
     *      Any other exception thrown from this method will be considered by the caller as an unexpected error.
     *      The caller is expected to dump the stack trace.
     */
    public abstract void parse(List<String> args) throws Exception;

    /**
     * Executes a configured command.

     * @return
     *        The exit code of the command. 99 is apparently used for something special that I haven't figured out.
     * @throws AbortException
     *      Indicates a graceful failure. An implementation can report an error in the arguments to stderr
     *      in a human readable fashion, and throw this exception to indicate that the error is already
     *      conveyed to the user. The caller will print {@link AbortException#getMessage()} if that's non-null,
     *      but it shouldn't display the stack trace of the exception.
     * @throws Exception
     *      Any other exception thrown from this method will be considered by the caller as an unexpected error.
     *      The caller is expected to dump the stack trace.
     */
    public abstract int invoke() throws Exception;

    /**
     * Print out the detailed help of this command.
     *
     * @param args
     *      For backward compatibility, this method receives the full argument list
     *      (where the first token is the command name for which the help is requested.)
     */
    public abstract void printHelp(List<String> args);
}
