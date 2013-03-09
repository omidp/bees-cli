package com.cloudbees.sdk.maven;

import com.cloudbees.sdk.GAV;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.SyncContext;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeployResult;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallResult;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.*;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Convenient wrapper methods around {@link RepositorySystem}.
 *
 * <p>
 * {@link RepositorySystem} is written to achieve maximum forward compatibility, and as a result
 * it's not very client-friendly interface. This component adds convenience methods around them
 * to make it more bearable.
 *
 * <p>
 * Feel free to add more methods here.
 *
 * @author Kohsuke Kawaguchi
 */
public class RepositoryService {
    @Inject
    MavenRepositorySystemSession session;

    @Inject
    RepositorySystem repository;

    @Inject
    List<RemoteRepository> remoteRepositories;

    /**
     * Provides access to the underlying session.
     */
    public MavenRepositorySystemSession getSession() {
        return session;
    }

    /**
     * Provides access to the underlying repository system.
     */
    public RepositorySystem getRepository() {
        return repository;
    }

    public List<RemoteRepository> getRemoteRepositories() {
        return remoteRepositories;
    }

    /**
     * Expands a version range to a list of matching versions, in ascending order. For example, resolves "[3.8,4.0)" to
     * ["3.8", "3.8.1", "3.8.2"].
     *
     * @param request The version range request, must not be {@code null}
     * @return The version range result, never {@code null}.
     * @throws org.sonatype.aether.resolution.VersionRangeResolutionException If the requested range could not be parsed. Note that an empty range does
     *             not raise an exception.
     */
    public VersionRangeResult resolveVersionRange( VersionRangeRequest request ) throws VersionRangeResolutionException {
        return repository.resolveVersionRange(session, request);
    }

    /**
     * Takes "groupId:artifactId" and resolves available versions.
     */
    public VersionRangeResult resolveVersionRange(GAV gav) throws VersionRangeResolutionException {
        VersionRangeRequest rangeRequest = new VersionRangeRequest()
            .setArtifact(new DefaultArtifact( gav.ga()+":[0,)" ))
            .setRepositories(remoteRepositories);

        return resolveVersionRange(rangeRequest);
    }

    /**
     * Resolves an artifact's meta version (if any) to a concrete version. For example, resolves "1.0-SNAPSHOT" to
     * "1.0-20090208.132618-23".
     *
     * @param request The version request, must not be {@code null}
     * @return The version result, never {@code null}.
     * @throws org.sonatype.aether.resolution.VersionResolutionException If the metaversion could not be resolved.
     */
    public VersionResult resolveVersion( VersionRequest request ) throws VersionResolutionException {
        return repository.resolveVersion(session, request);
    }

    /**
     * Gets information about an artifact like its direct dependencies and potential relocations.
     *
     * @param request The descriptor request, must not be {@code null}
     * @return The descriptor result, never {@code null}.
     * @throws ArtifactDescriptorException If the artifact descriptor could not be read.
     * @see RepositorySystemSession#isIgnoreInvalidArtifactDescriptor()
     * @see RepositorySystemSession#isIgnoreMissingArtifactDescriptor()
     */
    public ArtifactDescriptorResult readArtifactDescriptor( ArtifactDescriptorRequest request ) throws ArtifactDescriptorException {
        return repository.readArtifactDescriptor(session, request);
    }

    /**
     * Collects the transitive dependencies of an artifact and builds a dependency graph. Note that this operation is
     * only concerned about determining the coordinates of the transitive dependencies. To also resolve the actual
     * artifact files, use {@link #resolveDependencies(DependencyRequest)}.
     *
     * @param request The collection request, must not be {@code null}
     * @return The collection result, never {@code null}.
     * @throws org.sonatype.aether.collection.DependencyCollectionException If the dependency tree could not be built.
     * @see RepositorySystemSession#getDependencyTraverser()
     * @see RepositorySystemSession#getDependencyManager()
     * @see RepositorySystemSession#getDependencySelector()
     * @see RepositorySystemSession#getDependencyGraphTransformer()
     */
    public CollectResult collectDependencies( CollectRequest request ) throws DependencyCollectionException {
        return repository.collectDependencies(session, request);
    }

    /**
     * Collects and resolves the transitive dependencies of an artifact. This operation is essentially a combination of
     * {@link #collectDependencies(CollectRequest)} and
     * {@link #resolveArtifacts(java.util.Collection)}.
     *
     * @param request The dependency request, must not be {@code null}
     * @return The dependency result, never {@code null}.
     * @throws DependencyResolutionException If the dependency tree could not be built or any dependency artifact could
     *             not be resolved.
     */
    public DependencyResult resolveDependencies( DependencyRequest request ) throws DependencyResolutionException {
        return repository.resolveDependencies(session, request);
    }

    /**
     * Resolves dependencies transitively from the given jar artifact with the runtime scope.
     *
     * Since bees extensibility is used primarily at the runtime of the given artifact,
     * runtime resolution should be the preferred scope of the dependency resolution.
     */
    public DependencyResult resolveDependencies(GAV a) throws DependencyResolutionException {
        return resolveDependencies(a,JavaScopes.RUNTIME);
    }

    /**
     * Resolves dependencies transitively from the given jar artifact, with the specified Maven scope
     * (compile, runtime, and so on.)
     */
    public DependencyResult resolveDependencies(GAV a, String scope) throws DependencyResolutionException {
        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(scope);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(new DefaultArtifact(a.toString()), JavaScopes.COMPILE));
        collectRequest.setRepositories(remoteRepositories);

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

        return resolveDependencies(dependencyRequest);
    }

    /**
     * Resolves the paths for an artifact. The artifact will be downloaded if necessary. An artifacts that is already
     * resolved will be skipped and is not re-resolved. Note that this method assumes that any relocations have already
     * been processed.
     *
     * @param request The resolution request, must not be {@code null}
     * @return The resolution result, never {@code null}.
     * @throws ArtifactResolutionException If the artifact could not be resolved.
     * @see org.sonatype.aether.artifact.Artifact#getFile()
     */
    public ArtifactResult resolveArtifact( ArtifactRequest request ) throws ArtifactResolutionException {
        return repository.resolveArtifact(session, request);
    }

    /**
     * Resolves a single jar artifact of the given coordinate and returns it.
     */
    public ArtifactResult resolveArtifact( GAV gav ) throws ArtifactResolutionException {
        return resolveArtifact(new DefaultArtifact(gav.toString()));
    }

    /**
     * Resolves a single artifact and returns it.
     */
    public ArtifactResult resolveArtifact( Artifact artifact ) throws ArtifactResolutionException {
        return resolveArtifact(new ArtifactRequest(artifact,remoteRepositories,null));
    }

    /**
     * Resolves the paths for a collection of artifacts. Artifacts will be downloaded if necessary. Artifacts that are
     * already resolved will be skipped and are not re-resolved. Note that this method assumes that any relocations have
     * already been processed.
     *
     * @param requests The resolution requests, must not be {@code null}
     * @return The resolution results (in request order), never {@code null}.
     * @throws ArtifactResolutionException If any artifact could not be resolved.
     * @see org.sonatype.aether.artifact.Artifact#getFile()
     */
    public List<ArtifactResult> resolveArtifacts( Collection<? extends ArtifactRequest> requests ) throws ArtifactResolutionException {
        return repository.resolveArtifacts(session, requests);
    }

    /**
     * Resolves the paths for a collection of metadata. Metadata will be downloaded if necessary.
     *
     * @param requests The resolution requests, must not be {@code null}
     * @return The resolution results (in request order), never {@code null}.
     * @see org.sonatype.aether.metadata.Metadata#getFile()
     */
    public List<MetadataResult> resolveMetadata( Collection<? extends MetadataRequest> requests ) {
        return repository.resolveMetadata(session, requests);
    }

    /**
     * Creates a new synchronization context.
     *
     * @param shared A flag indicating whether access to the artifacts/metadata associated with the new context can be
     *            shared among concurrent readers or whether access needs to be exclusive to the calling thread.
     * @return The synchronization context, never {@code null}.
     */
    public SyncContext newSyncContext(boolean shared) {
        return repository.newSyncContext(session, shared);
    }

    /**
     * Uploads a collection of artifacts and their accompanying metadata to a remote repository.
     *
     * @param request The deployment request, must not be {@code null}.
     * @return The deployment result, never {@code null}.
     * @throws DeploymentException If any artifact/metadata from the request could not be deployed.
     */
    public DeployResult deploy(DeployRequest request) throws DeploymentException {
        return repository.deploy(session, request);
    }

    /**
     * Installs a collection of artifacts and their accompanying metadata to the local repository.
     *
     * @param request The installation request, must not be {@code null}.
     * @return The installation result, never {@code null}.
     * @throws InstallationException If any artifact/metadata from the request could not be installed.
     */
    public InstallResult install(InstallRequest request) throws InstallationException {
        return repository.install(session, request);
    }
}
