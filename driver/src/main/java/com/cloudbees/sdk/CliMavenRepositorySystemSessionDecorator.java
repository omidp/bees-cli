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

import com.cloudbees.sdk.cli.Verbose;
import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.maven.ConsoleRepositoryListener;
import com.cloudbees.sdk.maven.ConsoleTransferListener;
import com.cloudbees.sdk.maven.MavenRepositorySystemSessionDecorator;
import com.cloudbees.sdk.maven.MavenRepositorySystemSessionFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.repository.RepositoryPolicy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link MavenRepositorySystemSessionFactory} that honors the verbose flag.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class CliMavenRepositorySystemSessionDecorator extends MavenRepositorySystemSessionDecorator {
    @Inject
    Verbose verbose;

    private boolean force;

    @Override
    public MavenRepositorySystemSession decorate(MavenRepositorySystemSession session) {
        if (force) {
            session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
        }
        if (verbose.isVerbose()) {
            session.setTransferListener(new ConsoleTransferListener());
            session.setRepositoryListener(new ConsoleRepositoryListener());
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
