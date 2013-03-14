package com.cloudbees.sdk;

import com.cloudbees.sdk.cli.Verbose;
import com.cloudbees.sdk.maven.ConsoleRepositoryListener;
import com.cloudbees.sdk.maven.ConsoleTransferListener;
import com.cloudbees.sdk.maven.MavenRepositorySystemSessionFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link MavenRepositorySystemSessionFactory} that honors the verbose flag.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class CliMavenRepositorySystemSessionFactory extends MavenRepositorySystemSessionFactory {
    @Inject
    Verbose verbose;

    @Override
    public MavenRepositorySystemSession get() {
        MavenRepositorySystemSession session = super.get();
        if (verbose.isVerbose()) {
            session.setTransferListener(new ConsoleTransferListener());
            session.setRepositoryListener(new ConsoleRepositoryListener());
        }
        return session;
    }

}
