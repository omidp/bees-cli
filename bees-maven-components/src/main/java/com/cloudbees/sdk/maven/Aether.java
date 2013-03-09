package com.cloudbees.sdk.maven;

import com.google.inject.BindingAnnotation;
import org.codehaus.plexus.PlexusContainer;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates {@link PlexusContainer} for loading Aether.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({TYPE, PARAMETER, METHOD})
@BindingAnnotation
public @interface Aether {
}
