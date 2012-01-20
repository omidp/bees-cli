package com.cloudbees.sdk;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.repository.LocalRepository;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;

/**
 * Creates a configured {@link MavenRepositorySystemSession}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class RepositorySessionProvider implements Provider<MavenRepositorySystemSession> {
    @Inject
    RepositorySystem rs;

    /**
     * Creates a new session.
     */
    public MavenRepositorySystemSession get() {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        LocalRepository localRepo = new LocalRepository( new File(new File(System.getProperty("user.home")),".m2/repository"));
        session.setLocalRepositoryManager(rs.newLocalRepositoryManager(localRepo));
        return session;
    }
}
