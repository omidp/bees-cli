package com.cloudbees.sdk.maven;

import com.cloudbees.sdk.VersionResolverImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
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
    }

    @Provides @Singleton
    public RepositorySystem get() {
        try {
            DefaultPlexusContainer boot = new DefaultPlexusContainer(
                    new DefaultContainerConfiguration(),
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(VersionResolver.class).to(VersionResolverImpl.class);
                        }
                    }
            );
            return boot.lookup(RepositorySystem.class);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Unable to lookup component RepositorySystem, cannot establish Aether dependency resolver.", e);
        } catch (PlexusContainerException e) {
            throw new RuntimeException("Unable to load RepositorySystem component by Plexus, cannot establish Aether dependency resolver.", e);
        }
    }
}
