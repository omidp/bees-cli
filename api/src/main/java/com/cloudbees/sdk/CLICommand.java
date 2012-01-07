package com.cloudbees.sdk;

import com.google.inject.BindingAnnotation;
import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Marks a class as a discoverable implementation of {@link ICommand} bound to a particular command name.
 *
 * <p>
 * This is a binding annotation, so additional characteristics about a command needs to be defined
 * as separate annotations instead of additional elements, such as {@link Experimental}.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
@Indexed
@BindingAnnotation
public @interface CLICommand {
    /**
     * Name of the command.
     */
    String value();
}
