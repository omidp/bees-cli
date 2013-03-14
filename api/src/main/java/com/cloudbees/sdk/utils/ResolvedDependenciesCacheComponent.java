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
