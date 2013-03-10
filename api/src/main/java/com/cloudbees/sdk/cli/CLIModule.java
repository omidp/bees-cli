package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionPoint;
import com.google.inject.Module;

/**
 * Marker interface for {@link Module}s defined in CLI command plugins.
 *
 * <p>
 * The <tt>bees</tt> command creates a child classloader and child Guice container for each command plugin.
 * When such a child container is created, any classes that implements this interface and is annotated with
 * {@link Extension} are discovered and added to the Guice container.
 *
 * <p>
 * In other words, this is a way for CLI command plugins to customize the bindings.
 *
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public abstract class CLIModule implements Module {
}
