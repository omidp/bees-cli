package com.cloudbees.sdk.maven;

import com.cloudbees.sdk.cli.Verbose;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RepositoryPolicy;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Creates new instances of {@link MavenRepositorySystemSession}.
 *
 * <p>
 * This component itself is somewhat configurable, which affects the settings of the newly created
 * repository sessions.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class MavenRepositorySystemSessionFactory implements Provider<MavenRepositorySystemSession> {
    @Inject
    RepositorySystem rs;

    @Inject
    Verbose verbose;

    private boolean force;

    @Inject
    private Provider<LocalRepository> localRepository;

    @Override
    public MavenRepositorySystemSession get() {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setLocalRepositoryManager(rs.newLocalRepositoryManager(localRepository.get()));
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
