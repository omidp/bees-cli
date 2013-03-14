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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenDependencyResolverSettings;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Module that adds {@link RepositorySystem} to Guice.
 *
 * <p>
 * This makes it easy for plugins to interact with Maven repository, most typically to download
 * artifacts.
 *
 * @author Kohsuke Kawaguchi
 */
public class RepositorySystemModule extends AbstractModule {
    @Override
    protected void configure() {
        // NettyAsyncHttpProvider prints some INFO-level messages. suppress them
        Logger.getLogger("com.ning.http.client.providers.netty.NettyAsyncHttpProvider").setLevel(Level.WARNING);
        LoggerFactory.getLogger(NettyAsyncHttpProvider.class);

        bind(LocalRepository.class).toProvider(LocalRepositorySetting.class);
        bind(MavenRepositorySystemSession.class).toProvider(MavenRepositorySystemSessionFactory.class);
    }

    @Provides @Aether @Singleton
    public PlexusContainer aetherContainer() {
        try {
            return new DefaultPlexusContainer(
                new DefaultContainerConfiguration(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(VersionResolver.class).to(VersionResolverImpl.class);
                    }
                }
            );
        } catch (PlexusContainerException e) {
            throw new RuntimeException("Unable to load RepositorySystem component by Plexus, cannot establish Aether dependency resolver.", e);
        }
    }

    @Provides @Singleton
    public RepositorySystem get(@Aether PlexusContainer aether) {
        return lookup(aether, RepositorySystem.class);
    }

    /**
     * List of remote repositories to resolve artifacts from.
     */
    @Provides @Singleton
    public List<RemoteRepository> getRemoteRepositories(RemoteRepositoryDecorator decorator) {
        List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
        MavenDependencyResolverSettings resolverSettings = new MavenDependencyResolverSettings();
        resolverSettings.setUseMavenCentral(true);
        List<RemoteRepository> repos = resolverSettings.getRemoteRepositories();
        for (RemoteRepository remoteRepository : repos) {
            Server server = resolverSettings.getSettings().getServer(remoteRepository.getId());
            if (server != null) {
                remoteRepository.setAuthentication(new Authentication(server.getUsername(), server.getPassword(), server.getPrivateKey(), server.getPassphrase()));
            }
            repositories.add(decorator.decorate(remoteRepository));
        }
        RemoteRepository r = new RemoteRepository("cloudbees-public-release", "default", "https://repository-cloudbees.forge.cloudbees.com/public-release/");
        repositories.add(decorator.decorate(r));
        return repositories;
    }

    protected  <T> T lookup(PlexusContainer aether, Class<T> type) {
        try {
            return aether.lookup(type);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Unable to lookup component RepositorySystem, cannot establish Aether dependency resolver.", e);
        }
    }
}
