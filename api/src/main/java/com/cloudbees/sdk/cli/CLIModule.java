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

package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionPoint;
import com.google.inject.Module;

/**
 * Marker interface for {@link Module}s defined in CLI command plugins.
 *
 * <p>
 * The <tt>bees</tt> command creates a child classloader and child Guice container for each command plugin.
 * When such a child container is created, any classes that implements this interface and is annotated with
 * {@link Extension} are discovered and added to the Guice container.
 *
 * <p>
 * In other words, this is a way for CLI command plugins to customize the bindings.
 *
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public interface CLIModule extends Module {
}
