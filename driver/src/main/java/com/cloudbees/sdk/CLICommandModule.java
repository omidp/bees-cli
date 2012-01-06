package com.cloudbees.sdk;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

import java.util.logging.Level;
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
        for (IndexItem<CLICommand,ICommand> m : Index.load(CLICommand.class, ICommand.class, cl)) {
            try {
                Class e = (Class) m.element();
                if (e.getClassLoader()==cl)
                    bind(ICommand.class).annotatedWith(m.annotation()).to(e);
            } catch (InstantiationException e) {
                LOGGER.log(Level.WARNING,"Failed to instantiate "+m.className(),e);
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(CommandService.class.getName());
}
