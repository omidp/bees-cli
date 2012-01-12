package com.cloudbees.sdk;

import com.google.inject.Provider;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.filter.ScopeDependencyFilter;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Component that talks to {@link MavenRepositorySystemSession}, resolve artifacts,
 * and create {@link ClassLoader} from them.
 *
 * <p>
 * This component is stateful.
 *
 * @author Kohsuke Kawaguchi
 */
public class ArtifactClassLoaderFactory {
    @Inject
    Provider<MavenRepositorySystemSession> sessionFactory;

    @Inject
    RepositorySystem rs;
    
    /**
     * We accumulate resolved jar files here.
     */
    private List<URL> urls = new ArrayList<URL>();

    /**
     * Sets up repositories we use to resolve artifacts.
     */
    private void configureRepositories(CollectRequest req) {
        // TODO: we want to hit our repository, too
        req.addRepository(new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/"));
    }

    /**
     * Adds the given artifact and all its transitive dependencies to the classpath.
     */
    public void add(GAV gav) throws DependencyCollectionException, DependencyResolutionException, IOException {
        add(toArtifact(gav));
    }
    
    /**
     * Adds the given artifact and all its transitive dependencies to the classpath.
     */
    public void add(Artifact a) throws DependencyCollectionException, DependencyResolutionException, IOException {
        MavenRepositorySystemSession session = sessionFactory.get();
        Dependency dependency = new Dependency(a, "compile");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        configureRepositories(collectRequest);
        DependencyNode node = rs.collectDependencies(session, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest(node,new ScopeDependencyFilter("provided"));

        add(rs.resolveDependencies(session, dependencyRequest).getRoot());
    }

    /**
     * Adds the given node and all its transitive dependencies to the classpath.
     */
    public void add(DependencyNode root) throws IOException {
        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        root.accept(nlg);

        for (File jar : nlg.getFiles()) {
            urls.add(jar.toURI().toURL());
        }
        LOGGER.fine("Resolved: "+urls);
    }

    /**
     * Creates a classloader from all the artifacts resolved thus far.
     */
    public ClassLoader createClassLoader(ClassLoader parent) {
        if (urls.isEmpty()) return parent;  // nothing to load
        return new URLClassLoader(urls.toArray(new URL[urls.size()]),getClass().getClassLoader());
    }

    private Artifact toArtifact(GAV gav) {
        return new DefaultArtifact(gav.groupId,gav.artifactId,"jar",gav.version);
    }

    private static final Logger LOGGER = Logger.getLogger(ArtifactClassLoaderFactory.class.getName());
}
