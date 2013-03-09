package com.cloudbees.sdk.maven;

import org.jboss.shrinkwrap.resolver.impl.maven.MavenDependencyResolverSettings;
import org.sonatype.aether.repository.LocalRepository;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;

/**
 * Component that holds on to the configured {@link LocalRepository}.
 *
 * {@link LocalRepository} is a representation of a local file system directory
 * where Maven stores artifacts and metadata that it has downloaded from elsewhere.
 * You typically need this when you resolve artifacts.
 *
 * This indirection allows a manual insertion of the local repository that
 * ignores the default location.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class LocalRepositorySetting implements Provider<LocalRepository> {
    private LocalRepository localRepository;

    public LocalRepository get() {
        if (localRepository == null) {
            // Try to get local repository from settings.xml
            MavenDependencyResolverSettings resolverSettings = new MavenDependencyResolverSettings();
            String localRepositoryName = resolverSettings.getSettings().getLocalRepository();
            if (localRepositoryName != null)
                localRepository = new LocalRepository(new File(localRepositoryName));
            else
                localRepository = new LocalRepository(new File(new File(System.getProperty("user.home")), ".m2/repository"));
        }
        return localRepository;
    }

    public void set(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }
}
