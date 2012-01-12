package com.cloudbees.sdk;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link BindingAnnotation} for injecting extension class loader.
 *
 * <p>
 * This is the {@link ClassLoader} that includes all the bees CLI components + extensions.
 * Any additional code that we load should use this as the parent. This is a singleton instance
 * you can inject into your component as follows:
 *
 * <pre>
 * &#64;ExtensionClassLoader
 * ClassLoader extensionClassLoader;
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface ExtensionClassLoader {
}
