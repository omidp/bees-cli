package com.cloudbees.sdk;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import com.google.inject.Module;

/**
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public interface CLIModule extends Module {
}
