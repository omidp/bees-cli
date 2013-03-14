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

import com.cloudbees.sdk.AbortException;

import java.io.IOException;

/**
 * Resolves command names to their implementations.
 * 
 * <p>
 * This interface isn't intended to be implemented by plugins. It is rather a representation of the contract
 * from the CLI environment to plugins. You can inject this component to your code by using this interface.
 * 
 * <p>
 * Bees CLI implements {@link CommandService} to encapsulate automatic plugin discovery logic.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface CommandService {
    /**
     * Resolves a command to its implementation.
     *
     * @param name
     *      Name of the command, such as "app:deploy", "help", or "foo:bar".
     * @return
     *      null if the command of the said name is not found.
     * @throws IOException
     *      If a fatal error occurs during the retrieval of the command implementation.
     * @throws AbortException
     *      If an anticipated problem happens, this exception is thrown. The caller must
     *      handle this exception without reporting a stack trace.
     */
    ACommand getCommand(String name) throws IOException, AbortException;

}
