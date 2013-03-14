package com.cloudbees.sdk.maven;

import org.sonatype.aether.repository.RemoteRepository;

/**
 * Configures {@link RemoteRepository} before they get used by Aether.
 *
 * @author Kohsuke Kawaguchi
 */
public class RemoteRepositoryDecorator {
    /**
     * The default implementation is no-op. Bind this to your subtype to substitute.
     */
    public RemoteRepository decorate(RemoteRepository repository) {
        return repository;
    }
}
