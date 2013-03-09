/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.cloudbees.sdk.maven;

import hudson.util.VersionNumber;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.RepositoryEvent.EventType;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.RequestTrace;
import org.sonatype.aether.SyncContext;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.MetadataResolver;
import org.sonatype.aether.impl.SyncContextFactory;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.ArtifactRepository;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.resolution.VersionResult;
import org.sonatype.aether.util.DefaultRequestTrace;
import org.sonatype.aether.util.listener.DefaultRepositoryEvent;
import org.sonatype.aether.util.metadata.DefaultMetadata;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom {@link VersionResolver} that handles "LATEST" correctly.
 *
 * <p>
 * The default one doesn't work because it only looks for the &lt;snapshots> tags that Maven doesn't set
 * (and if my reading of Maven is right, the &lt;latest> tag is only created when Maven installs/deploys
 * a Maven plugin &mdash; see VersionsMetadata.createMetadata in Maven.)
 *
 * <p>
 * So in this implementation, we look at /metadata/versioning/versions that do get updated during normal
 * "mvn install". This allows us to find the version whose number is greatest, including SNAPSHOT,
 * among both local and remote repositories.
 * 
 * <p>
 * Unfortunately I had to duplicate a lot of code from {@link DefaultVersionResolver}, but to
 * minimize the behaviour difference, all the resolutions except 'LATEST' is delegated right back to
 * {@link DefaultVersionResolver}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class VersionResolverImpl implements VersionResolver {
    @Inject
    DefaultVersionResolver delegate;

    @Inject
    MetadataResolver metadataResolver;

    @Inject
    private SyncContextFactory syncContextFactory;

    public VersionResult resolveVersion(RepositorySystemSession session, VersionRequest request) throws VersionResolutionException {
        Artifact artifact = request.getArtifact();

        String version = artifact.getVersion();
        if (!LATEST.equals(version))
            return delegate.resolveVersion(session,request);

        RequestTrace trace = DefaultRequestTrace.newChild(request.getTrace(), request);
        VersionResult result = new VersionResult( request );

        Metadata metadata =
            new DefaultMetadata( artifact.getGroupId(), artifact.getArtifactId(), MAVEN_METADATA_XML,
                                 Metadata.Nature.RELEASE_OR_SNAPSHOT );

        List<MetadataRequest> metadataRequests = new ArrayList<MetadataRequest>(request.getRepositories().size());

        metadataRequests.add( new MetadataRequest( metadata, null, request.getRequestContext() ) );

        for ( RemoteRepository repository : request.getRepositories() )
        {
            MetadataRequest metadataRequest =
                new MetadataRequest( metadata, repository, request.getRequestContext() );
            metadataRequest.setDeleteLocalCopyIfMissing( true );
            metadataRequest.setFavorLocalRepository( true );
            metadataRequest.setTrace( trace );
            metadataRequests.add( metadataRequest );
        }

        List<MetadataResult> metadataResults = metadataResolver.resolveMetadata( session, metadataRequests );

        List<VersionInfo> infos = new ArrayList<VersionInfo>();

        for (MetadataResult metadataResult : metadataResults) {
            result.addException(metadataResult.getException());

            ArtifactRepository repository = metadataResult.getRequest().getRepository();
            if (repository == null) {
                repository = session.getLocalRepository();
            }

            Versioning versioning = readVersions(session, trace, metadataResult.getMetadata(), repository, result);
            merge(versioning, repository, infos);
        }

        VersionInfo latest = findLatest(infos);
        if (latest!=null) {
            result.setRepository(latest.repository);
            result.setVersion(latest.version.toString());
        }

        if (latest != null && latest.version.toString().endsWith(SNAPSHOT)) {
            VersionRequest subRequest = new VersionRequest();
            subRequest.setArtifact(artifact.setVersion(result.getVersion()));
            if (result.getRepository() instanceof RemoteRepository) {
                subRequest.setRepositories(Collections.singletonList((RemoteRepository) result.getRepository()));
            } else {
                subRequest.setRepositories(request.getRepositories());
            }
            VersionResult subResult = resolveVersion(session, subRequest);
            result.setVersion(subResult.getVersion());
            result.setRepository(subResult.getRepository());
            for (Exception exception : subResult.getExceptions()) {
                result.addException(exception);
            }
        }

        if ( StringUtils.isEmpty(result.getVersion()) )
        {
            throw new VersionResolutionException( result );
        }

        return result;
    }

    /**
     * List up all the versions found in the metadata.
     */
    private void merge(Versioning versioning, ArtifactRepository from, List<VersionInfo> found) {
        if ( StringUtils.isNotEmpty( versioning.getRelease() ) )
            found.add(new VersionInfo(versioning.getRelease(),from));

        if ( StringUtils.isNotEmpty( versioning.getLatest() ) )
            found.add(new VersionInfo(versioning.getLatest(),from));

        for ( String v : versioning.getVersions() )
            found.add(new VersionInfo(v,from));
    }

    VersionInfo findLatest(List<VersionInfo> infos) {
        Collections.sort(infos);
        if (infos.isEmpty())    return null;
        return infos.get(infos.size()-1);
    }

    private static class VersionInfo implements Comparable<VersionInfo> {
        final VersionNumber version;
        final ArtifactRepository repository;

        public VersionInfo(String version, ArtifactRepository repository) {
            this.version = new VersionNumber(version);
            this.repository = repository;
        }

        public int compareTo(VersionInfo that) {
            return this.version.compareTo(that.version);
        }
    }
    
    private Versioning readVersions( RepositorySystemSession session, RequestTrace trace, Metadata metadata,
                                     ArtifactRepository repository, VersionResult result )
    {
        Versioning versioning = null;

        FileInputStream fis = null;
        try
        {
            if ( metadata != null )
            {
                SyncContext syncContext = syncContextFactory.newInstance( session, true );

                try
                {
                    syncContext.acquire( null, Collections.singleton( metadata ) );

                    if ( metadata.getFile() != null && metadata.getFile().exists() )
                    {
                        fis = new FileInputStream( metadata.getFile() );
                        org.apache.maven.artifact.repository.metadata.Metadata m =
                            new MetadataXpp3Reader().read( fis, false );
                        versioning = m.getVersioning();

                        /*
                         * NOTE: Users occasionally misuse the id "local" for remote repos which screws up the metadata
                         * of the local repository. This is especially troublesome during snapshot resolution so we try
                         * to handle that gracefully.
                         */
                        if ( versioning != null && repository instanceof LocalRepository)
                        {
                            if ( versioning.getSnapshot() != null && versioning.getSnapshot().getBuildNumber() > 0 )
                            {
                                Versioning repaired = new Versioning();
                                repaired.setLastUpdated( versioning.getLastUpdated() );
                                Snapshot snapshot = new Snapshot();
                                snapshot.setLocalCopy( true );
                                repaired.setSnapshot( snapshot );
                                versioning = repaired;

                                throw new IOException( "Snapshot information corrupted with remote repository data"
                                    + ", please verify that no remote repository uses the id '" + repository.getId()
                                    + "'" );
                            }
                        }
                    }
                }
                finally
                {
                    syncContext.release();
                }
            }
        }
        catch ( Exception e )
        {
            invalidMetadata( session, trace, metadata, repository, e );
            result.addException( e );
        }
        finally
        {
            IOUtil.close(fis);
        }

        return ( versioning != null ) ? versioning : new Versioning();
    }

    private void invalidMetadata( RepositorySystemSession session, RequestTrace trace, Metadata metadata,
                                  ArtifactRepository repository, Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( EventType.METADATA_INVALID, session, trace );
            event.setMetadata( metadata );
            event.setException( exception );
            event.setRepository( repository );
            listener.metadataInvalid( event );
        }
    }

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    private static final String LATEST = "LATEST";

    private static final String SNAPSHOT = "SNAPSHOT";
}
