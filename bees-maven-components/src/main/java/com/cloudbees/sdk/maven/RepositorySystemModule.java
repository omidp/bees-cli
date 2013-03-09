package com.cloudbees.sdk.maven;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.VersionResolver;

import javax.inject.Singleton;

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
        bind(VersionResolver.class).to(VersionResolverImpl.class);
    }

    @Provides @Aether @Singleton
    public PlexusContainer aetherContainer(final VersionResolver resolver) {
        try {
            return new DefaultPlexusContainer(
                new DefaultContainerConfiguration(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(VersionResolver.class).toInstance(resolver);
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

    protected  <T> T lookup(PlexusContainer aether, Class<T> type) {
        try {
            return aether.lookup(type);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Unable to lookup component RepositorySystem, cannot establish Aether dependency resolver.", e);
        }
    }
}
