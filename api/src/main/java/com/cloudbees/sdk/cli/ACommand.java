package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.extensibility.ExtensionPoint;

import java.util.List;

/**
 * A CLI command.
 *
 * <p>
 * This is the contract of CLI commands. All the abstract subtypes are merely convenient
 * partial implementations.
 * <p>
 * A command in CloudBees SDK is usually invoked as a sub-command of the <tt>bees</tt> command.
 * It takes arbitrary numbers of arguments, whose meanings are entirely up to command implmeentations.
 * A command interacts with stdin/stdout/stderr.
 *
 * <p>
 * Commands are instantiated with Guice, so they receive dependency injections.
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
    public abstract int run(List<String> args) throws Exception;

    /**
     * Print out the detailed help of this command.
     *
     * @param args
     *      For backward compatibility, this method receives the full argument list
     *      (where the first token is the command name for which the help is requested.)
     */
    public abstract void printHelp(List<String> args);
}
