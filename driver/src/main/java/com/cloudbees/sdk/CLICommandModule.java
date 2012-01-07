package com.cloudbees.sdk;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.jvnet.hudson.annotation_indexer.Index;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * {@link Module} that discovers implementations of the extension annotated by {@link CLICommand}.
 * 
 * @author Kohsuke Kawaguchi
 */
class CLICommandModule extends AbstractModule {
    private final ClassLoader cl;

    public CLICommandModule(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    protected void configure() {
        try {
            for (Class<?> e : Index.list(CLICommand.class, cl, Class.class)) {
                if (e.getClassLoader()==cl)
                    bind(ICommand.class).annotatedWith(e.getAnnotation(CLICommand.class)).to((Class)e);
            }
        } catch (IOException x) {
            throw new Error(x);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(CommandService.class.getName());
}
