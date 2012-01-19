package com.cloudbees.sdk.commands.ant;

import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CLIModule;
import com.cloudbees.sdk.cli.ICommand;
import com.cloudbees.sdk.extensibility.AnnotationLiteral;
import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;

/**
 * Registers CLI commands implemented via {@link AntTarget}.
 * 
 * @author Kohsuke Kawaguchi
 */
@Extension
public class AntTargetCommandsModule extends AbstractModule implements CLIModule {
    @Override
    protected void configure() {
        target("clean");
        target("compile");
        target("dist");
    }
    
    private void target(final String antTaget) {
        bind(ICommand.class).annotatedWith(AnnotationLiteral.of(CLICommand.class, "ant:"+antTaget))
            .toProvider(new Provider<ICommand>() {
                public ICommand get() {
                    return new AntTarget(antTaget);
                }
            });
    }
}