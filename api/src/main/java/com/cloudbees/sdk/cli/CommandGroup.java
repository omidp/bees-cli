package com.cloudbees.sdk.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Puts a command into a group
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface CommandGroup {
    String value();
}
