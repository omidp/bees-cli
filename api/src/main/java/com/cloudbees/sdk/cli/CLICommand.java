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

import com.cloudbees.sdk.extensibility.ExtensionImplementation;
import com.google.inject.BindingAnnotation;
import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Marks a class as a discoverable implementation of {@link ACommand} bound to a particular command name.
 *
 * <p>
 * This is a binding annotation, so additional characteristics about a command needs to be defined
 * as separate annotations instead of additional elements, such as {@link BeesCommand}.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
@Indexed
@BindingAnnotation
@ExtensionImplementation
public @interface CLICommand {
    /**
     * Name of the command.
     */
    String value();
}
