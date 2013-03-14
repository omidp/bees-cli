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

/**
 * Throw this exception when you are handling an user error gracefully.
 *
 * <p>
 * When this exception is caught, only the exception message is rendered and not its stack trace.
 * This is useful for "expected" problems where you do error checks on what the user did, and when
 * the user can correct the problem without knowing where the problem came from.
 *
 * <p>
 * This is NOT for the situation where the error is due to some underlying failure or a failure in
 * the external system. In such case, use other exception types so that the CLI will report the stack
 * trace. Often those errors cannot be diagnosed by the user himself, and the support staff needs
 * to see the stack trace to understand the structure of the problem.
 *
 * @author Kohsuke Kawaguchi
 */
public class AbortException extends RuntimeException {
    public AbortException() {
    }

    public AbortException(String message) {
        super(message);
    }
}
