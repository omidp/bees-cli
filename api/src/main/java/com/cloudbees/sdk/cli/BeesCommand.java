package com.cloudbees.sdk.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bees command definition.
 *
 * This annotation is used in conjunction with {@link CLICommand} to provide
 * metadata about the command.
 *
 * @author Fabian Donze
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface BeesCommand {
    String group() default "SDK";
    String description() default "";
    int priority() default 1;
    String pattern() default "";
    boolean experimental() default false;
}
