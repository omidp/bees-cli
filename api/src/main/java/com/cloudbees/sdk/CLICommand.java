package com.cloudbees.sdk;

import com.google.inject.BindingAnnotation;
import net.java.sezpoz.Indexable;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
@Indexable
@BindingAnnotation
public @interface CLICommand {
    /**
     * Name of the command.
     */
    String value();
}
