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

package com.cloudbees.sdk.maven;

import org.sonatype.aether.repository.RemoteRepository;

/**
 * Configures {@link RemoteRepository} before they get used by Aether.
 *
 * @author Kohsuke Kawaguchi
 */
public class RemoteRepositoryDecorator {
    /**
     * The default implementation is no-op. Bind this to your subtype to substitute.
     */
    public RemoteRepository decorate(RemoteRepository repository) {
        return repository;
    }
}
