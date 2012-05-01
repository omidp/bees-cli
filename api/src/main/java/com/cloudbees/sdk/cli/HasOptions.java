package com.cloudbees.sdk.cli;

/**
 * Marker interface for injectable components indicating that this component will define command line options.
 *
 * <p>
 * Often a command implementation uses various components to delegate some lower level tasks, and these
 * components want to allow users to tweak behaviours through a set of command line options.
 *
 * <p>
 * This marker interface tells {@link AbstractCommand#createParser()} that those components will participate
 * in the command line parsing process. Subtypes can define fields/setters with args4j annotations, and/or refer
 * to other components that has {@link HasOptions} annotation.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HasOptions {
}
