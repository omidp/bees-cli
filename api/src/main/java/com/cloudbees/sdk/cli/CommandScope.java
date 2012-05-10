package com.cloudbees.sdk.cli;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Indicates that components are scoped to a single command execution.
 *
 * <p>
 * This is normally used in conjunction with {@link HasOptions}, so that components
 * that define command line options won't end up having multiple instances during
 * a single command invocation.
 *
 * @author Kohsuke Kawaguchi
 */
@Scope
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface CommandScope {
}
