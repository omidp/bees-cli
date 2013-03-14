/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

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

        List<File> jars= cache.resolve(new GAV("com.cloudbees.sdk", "bees-driver", "LATEST"));

        File tools = findToolsJar();
        if (tools != null)  jars.add(tools);

        // we don't let this classloader delegate to the 2nd stage boot classloader
        // in this way, the environment that the driver sees can be fully updated (including Guice, Aether, and Plexus)
        // without updating the SDK itself.
        URLClassLoader loader = new URLClassLoader(toURL(jars), null);
        Class<?> beesClass = loader.loadClass("com.cloudbees.sdk.Bees");

        Thread.currentThread().setContextClassLoader(loader);

        Method mainMethod = beesClass.getDeclaredMethod("main", String[].class);
        Object obj = mainMethod.invoke(null, (Object)args);
        if (obj instanceof Integer)
            System.exit((Integer)obj);
    }

    private URL[] toURL(List<File> files) throws MalformedURLException {
        URL[] jars = new URL[files.size()];
        int i=0;
        for (File f : files) {
            jars[i++] = f.toURI().toURL();
        }
        return jars;
    }

    private static void reportTime() {
        String profile = System.getProperty("profile");
        if (profile !=null) {
            System.out.println(BeesLoader.class.getName() + ": " + (System.nanoTime() - Long.valueOf(profile)) / 1000000L + "ms");
        }
    }

    private static File findToolsJar() {
        String javaHome = System.getenv("JAVA_HOME");
        // Try to define it
        if (javaHome == null) {
            String[] paths = System.getProperty("sun.boot.library.path").split(",");
            if (paths != null && paths.length > 0) {
                javaHome = paths[0].trim();
            }
        }
        if (javaHome == null)   return null;

        File dir = new File(javaHome);
        File tools = new File(dir, "lib/tools.jar");
        if (tools.exists())     return tools;

        return null;
    }
}
