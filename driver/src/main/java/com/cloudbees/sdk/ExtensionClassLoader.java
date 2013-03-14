/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
