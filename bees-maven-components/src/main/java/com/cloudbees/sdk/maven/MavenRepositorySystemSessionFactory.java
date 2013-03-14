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

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RepositoryPolicy;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Creates new instances of {@link MavenRepositorySystemSession}.
 *
 * <p>
 * This component itself is somewhat configurable, which affects the settings of the newly created
 * repository sessions.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class MavenRepositorySystemSessionFactory implements Provider<MavenRepositorySystemSession> {
    @Inject
    RepositorySystem rs;

    private boolean force;

    @Inject
    private Provider<LocalRepository> localRepository;

    @Override
    public MavenRepositorySystemSession get() {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setLocalRepositoryManager(rs.newLocalRepositoryManager(localRepository.get()));
        if (force) {
            session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
        }
        return session;
    }

    /**
     * Force update from the remote repository.
     */
    public void setForce(boolean force) {
        this.force = force;
    }
}
