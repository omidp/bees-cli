package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.extensibility.ExtensionPoint;

/**
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public abstract class CommandResolver {
    public abstract ICommand resolve(String commandName);
}
