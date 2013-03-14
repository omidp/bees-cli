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

package com.cloudbees.sdk.utils;

import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.cli.DirectoryStructure;
import com.cloudbees.sdk.maven.RepositoryService;
import com.cloudbees.sdk.maven.ResolvedDependenciesCache;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Injectable component version of {@link ResolvedDependenciesCache}.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class ResolvedDependenciesCacheComponent extends ResolvedDependenciesCache {
    @Inject
    DirectoryStructure structure;

    @Inject
    RepositoryService rs;

    @Override
    protected File getCacheDir() {
        return structure.getLocalRepository();
    }

    @Override
    protected DependencyResult forceResolve(GAV gav) throws DependencyResolutionException {
        return rs.resolveDependencies(gav);
    }

    @Override
    protected File resolveArtifact(Artifact a) throws ArtifactResolutionException {
        return rs.resolveArtifact(a).getArtifact().getFile();
    }
}
