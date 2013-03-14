package com.cloudbees.sdk.boot2;

import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.boot.Launcher;
import com.cloudbees.sdk.maven.RepositoryService;
import com.cloudbees.sdk.maven.RepositorySystemModule;
import com.cloudbees.sdk.maven.ResolvedDependenciesCache;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * 2nd-level bootstrapping.
 *
 * @author Kohsuke Kawaguchi
 */
public class BeesLoader {

    public static void main(String[] args) throws Exception {
        reportTime();

        new BeesLoader().run(args);
    }

    private Injector injector;
    private RepositoryService rs;

    private void run(String[] args) throws Exception {
        ResolvedDependenciesCache cache = new ResolvedDependenciesCache() {
            private RepositoryService getRepositoryService() {
                if (injector==null)
                    injector = Guice.createInjector(new RepositorySystemModule());
                if (rs==null)
                    rs = injector.getInstance(RepositoryService.class);
                return rs;
            }

            @Override
            protected File getCacheDir() {
                return Launcher.DEFAULT_REPO_DIR;
            }

            @Override
            protected DependencyResult forceResolve(GAV gav) throws DependencyResolutionException {
                RepositoryService rs = getRepositoryService();
                return rs.resolveDependencies(gav);
            }

            @Override
            protected File resolveArtifact(Artifact a) throws ArtifactResolutionException {
                return getRepositoryService().resolveArtifact(a).getArtifact().getFile();
            }
        };

        GAV main = new GAV("com.cloudbees.sdk", "bees-driver", "LATEST");
        URLClassLoader loader = new URLClassLoader(cache.resolveToURLs(main), null);
        Class<?> beesClass = loader.loadClass("com.cloudbees.sdk.Bees");

        Thread.currentThread().setContextClassLoader(loader);

        Method mainMethod = beesClass.getDeclaredMethod("main", String[].class);
        Object obj = mainMethod.invoke(null, (Object)args);
        if (obj instanceof Integer)
            System.exit((Integer)obj);
    }

    private static void reportTime() {
        String profile = System.getProperty("profile");
        if (profile !=null) {
            System.out.println(BeesLoader.class.getName() + ": " + (System.nanoTime() - Long.valueOf(profile)) / 1000000L + "ms");
        }
    }
}
