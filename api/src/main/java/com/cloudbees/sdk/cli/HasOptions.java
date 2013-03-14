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

/**
 * Marker interface for injectable components indicating that this component will define command line options.
 *
 * <p>
 * Often a command implementation uses various components to delegate some lower level tasks, and these
 * components want to allow users to tweak behaviours through a set of command line options.
 *
 * <p>
 * This marker interface tells {@link AbstractCommand#createParser()} that those components will participate
 * in the command line parsing process. Subtypes can define fields/setters with args4j annotations, and/or refer
 * to other components that has {@link HasOptions} annotation.
 *
 * <p>
 * Components that have this interface normally needs to be {@linkplain CommandScope scoped to command} to
 * ensure there's only one instance of it per a single command invocation.
 *
 * @author Kohsuke Kawaguchi
 * @see CommandScope
 */
public interface HasOptions {
}
