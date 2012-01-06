package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.ICommand;
import com.cloudbees.sdk.annotations.CLICommandImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;

/**
 * Registers CLI commands implemented via {@link AntTarget}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class AntTargetCommandsModule extends AbstractModule {
    @Override
    protected void configure() {
        target("clean");
        target("compile");
        target("dist");
    }
    
    private void target(final String antTaget) {
        bind(ICommand.class).annotatedWith(new CLICommandImpl(antTaget))
            .toProvider(new Provider<ICommand>() {
                public ICommand get() {
                    return new AntTarget(antTaget);
                }
            });
    }
}