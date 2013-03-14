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

package com.cloudbees.sdk.maven;

import com.cloudbees.sdk.GAV;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Cache of dependency resolution of a specific artifact.
 *
 * <p>
 * Resolving transitive dependencies of a Maven artifact from Aether is expensive, as it has to
 * scan each POM several dozen times. Coupled with the cost of wiring up Maven project parser
 * components and so on, this can take close to one second.
 *
 * <p>
 * This utility class caches the result of transitive dependency resolution and remembers
 * its result. It does timestamp based up to date check that lets us bypass the expensive computation
 * in most of the cases.
 *
 * <p>
 * One of the key use cases of this component requires lazy-loading Guice, so this class by itself
 * does not depend injection. For most use cases, you should be able to use {@code ResolvedDependenciesCacheComponent}
 * in bees-api.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public abstract class ResolvedDependenciesCache {
    public List<File> resolve(GAV gav) throws IOException, RepositoryException {
        File cacheFile = getCacheFile(gav);
        if (cacheFile.exists()) {
            Properties props = new Properties();
            FileInputStream i = new FileInputStream(cacheFile);
            try {
                props.load(i);
            } finally {
                i.close();
            }

            List<File> files = loadFromCache(props);
            if (files!=null)    return files;
        }

        List<File> files = new ArrayList<File>();
        Properties props = new Properties();
        props.setProperty("timestamp",String.valueOf(System.currentTimeMillis()));
        DependencyResult dependencies = forceResolve(gav);
        int i=0;
        for (ArtifactResult ar : dependencies.getArtifactResults()) {
            Artifact a = ar.getArtifact();
            Artifact pom = new DefaultArtifact(a.getGroupId(),a.getArtifactId(),"pom",a.getVersion());

            props.put("pom."+i, resolveArtifact(pom).getAbsolutePath());
            props.put("jar." + i, a.getFile().getAbsolutePath());
            i++;
            files.add(a.getFile());
        }
        FileOutputStream o = new FileOutputStream(cacheFile);
        try {
            props.store(o,"Resolved dependencies for "+gav);
        } finally {
            o.close();
        }
        return files;
    }

    /**
     * Convenience method around {@link #resolve(com.cloudbees.sdk.GAV)} since most often
     * the result is used to create a classloader, which wants URL[].
     */
    public URL[] resolveToURLs(GAV gav) throws IOException, RepositoryException {
        List<URL> jars = new ArrayList<URL>();
        for (File f : resolve(gav)) {
            jars.add(f.toURI().toURL());
        }
        return jars.toArray(new URL[jars.size()]);
    }

    /**
     * Obtains the directory to store cache.
     */
    protected abstract File getCacheDir();

    /**
     * Obtains a file that stores a cache for a specific artifact.
     */
    protected File getCacheFile(GAV gav) {
        return new File(getCacheDir(),gav.toString().replace(':','.')+".dependencies");
    }

    /**
     * Use Aether to resolve dependencies properly.
     */
    protected abstract DependencyResult forceResolve(GAV gav) throws DependencyResolutionException;

    /**
     * Resolves a specific artifact without its dependencies and returns it.
     */
    protected abstract File resolveArtifact(Artifact a) throws ArtifactResolutionException;


    /**
     * Loads a list of jars from the cache, with up-to-date check.
     *
     * @return null if the data is stale
     */
    private List<File> loadFromCache(Properties props) {
        String ts = props.getProperty("timestamp");
        if (ts==null)   return null;

        long timestamp = Long.valueOf(ts);
        if (isStale(timestamp))
            return null;

        List<File> files = new ArrayList<File>();

        for (Object k : props.keySet()) {
            String key = k.toString();
            if (key.startsWith("pom.")) {
                File f = new File(props.getProperty(key));
                if (!isUpToDate(f,timestamp))
                    return null;
            }
            if (key.startsWith("jar.")) {
                File f = new File(props.getProperty(key));
                if (!isUpToDate(f, timestamp))
                    return null;
                files.add(f);
            }
        }

        return files;
    }

    private boolean isUpToDate(File f, long timestamp) {
        long modified = f.lastModified();
        return modified != 0 && modified < timestamp;
    }

    /**
     * Our up-to-date check based on POM and jars do not account for dependency ranges,
     * labels like LATEST, and ~/.m2/settings.xml changes. So if the timestamp of the cache
     * gets old beyond certain point relative to the current timestamp, we should force
     * a re-resolution.
     */
    protected boolean isStale(long timestamp) {
        return timestamp<System.currentTimeMillis()- TimeUnit.DAYS.toMillis(1);
    }
}
