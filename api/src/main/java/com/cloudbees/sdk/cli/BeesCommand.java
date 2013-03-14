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
