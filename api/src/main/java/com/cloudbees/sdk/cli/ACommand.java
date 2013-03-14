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

import com.cloudbees.sdk.extensibility.ExtensionPoint;

import java.util.List;

/**
 * A CLI command.
 *
 * <p>
 * This is the contract of CLI commands. All the abstract subtypes are merely convenient
 * partial implementations.
 * <p>
 * A command in CloudBees SDK is usually invoked as a sub-command of the <tt>bees</tt> command.
 * It takes arbitrary numbers of arguments, whose meanings are entirely up to command implmeentations.
 * A command interacts with stdin/stdout/stderr.
 *
 * <p>
 * Commands are instantiated with Guice, so they receive dependency injections.
 *
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public abstract class ACommand {

    /**
     * Executes this command.
     *
     * @param args
     *        Never empty, never null. The first argument is the command name itself,
     *        followed by arguments
     * @return
     *        The exit code of the command. 99 is apparently used for something special that I haven't figured out.
     */
    public abstract int run(List<String> args) throws Exception;

    /**
     * Print out the detailed help of this command.
     *
     * @param args
     *      For backward compatibility, this method receives the full argument list
     *      (where the first token is the command name for which the help is requested.)
     */
    public abstract void printHelp(List<String> args);
}
