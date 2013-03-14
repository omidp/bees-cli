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

import org.kohsuke.args4j.Option;

import javax.inject.Singleton;

/**
 * Injectable component that captures the verboseness flag.
 * 
 * This is not a binding annotation but a value holder class because
 * wiring needs to happen before arguments are parsed.
 * 
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class Verbose implements HasOptions {
    @Option(name="-v",aliases="--verbose",usage="Make the command output more verbose")
    private boolean verbose;

    public Verbose() {
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
