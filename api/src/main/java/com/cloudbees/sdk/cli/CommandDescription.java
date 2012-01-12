package com.cloudbees.sdk.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface CommandDescription {
    String value();
}
