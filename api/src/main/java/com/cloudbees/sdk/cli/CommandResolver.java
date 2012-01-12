package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.extensibility.ExtensionPoint;

/**
 * Participates in the resolution process of the command name to {@link ICommand} object.
 *
 * To contribute this extension point to bees CLI, you need to be an extension, not just a plugin.
 * 
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public abstract class CommandResolver {
    public abstract ICommand resolve(String commandName);
}
