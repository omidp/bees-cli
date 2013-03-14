package com.cloudbees.sdk.boot2;

import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.maven.RepositoryService;
import com.cloudbees.sdk.maven.RepositorySystemModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyResult;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * 2nd-level bootstrapping.
 *
 * @author Kohsuke Kawaguchi
 */
public class BeesLoader {
    public static void main(String[] args) throws Exception {
        reportTime();

        //  container that includes all the things that make a bees CLI.
        Injector injector = Guice.createInjector(new RepositorySystemModule());

        BeesLoader loader = new BeesLoader();
        injector.injectMembers(loader);
        loader.run(args);
    }

    @Inject
    public RepositoryService rs;

    private void run(String[] args) throws Exception {
        DependencyResult r = rs.resolveDependencies(new GAV("com.cloudbees.sdk", "bees-driver", "LATEST"));

        List<URL> jars = new ArrayList<URL>();
        for (ArtifactResult a : r.getArtifactResults()) {
            jars.add(a.getArtifact().getFile().toURI().toURL());
        }

        URLClassLoader loader = new URLClassLoader(jars.toArray(new URL[jars.size()]), null);
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
            System.out.println(BeesLoader.class.getName()+": "+(System.nanoTime()-Long.valueOf(profile))/1000000L+"ms");
        }
    }
}
