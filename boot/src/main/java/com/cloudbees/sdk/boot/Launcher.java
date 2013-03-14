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

package com.cloudbees.sdk.boot;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * @author Fabian Donze
 */
public class Launcher {
    public final static String BEES_CLASS = "com.cloudbees.sdk.boot2.BeesLoader";
    public final static File DEFAULT_REPO_DIR = new File(System.getProperty("user.home"), ".bees");

    public static void main(String[] args) {
        if (System.getProperty("profile")!=null) {
            System.out.println("Starting");
            System.setProperty("profile",String.valueOf(System.nanoTime()));
        }

        try {
            URL[] urls = getJarUrls();
            ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader loader;
            if (parentLoader != null)
                loader = new URLClassLoader(urls, parentLoader);
            else
                loader = new URLClassLoader(urls);

            Class beesClass = loader.loadClass(BEES_CLASS);
            Thread.currentThread().setContextClassLoader(loader);

            Method mainMethod = beesClass.getDeclaredMethod("main", String[].class);
            Object obj = mainMethod.invoke(null, new Object[]{args});
            int returnValue = 0;
            if (obj instanceof Integer) {
                returnValue = ((Integer)obj).intValue();
            }
            System.exit(returnValue);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static File getLocalRepository() {
        File repo_dir;
        if(System.getenv("BEES_REPO") != null)
            repo_dir = new File(System.getenv("BEES_REPO"));
        else if(System.getProperty("bees.repo") != null)
            repo_dir = new File(System.getProperty("bees.repo"));
        else
            repo_dir = DEFAULT_REPO_DIR;
        return repo_dir;
    }

    private static URL[] getJarUrls() throws MalformedURLException
    {
        String resourcePath = Launcher.class.getName().replace(".", "/") + ".class";
        URL baseUrl = Launcher.class.getClassLoader().getResource(resourcePath);
        if (baseUrl == null)
            throw new IllegalStateException(Launcher.class.getName());

        // trim off the jar protocol
        String filePart = baseUrl.getFile();

        // trim off the ! part
        String[] parts = filePart.split("!");
        String fileUrl = parts[0];

        // trim off the file protocol
        String filePath = new URL(fileUrl).getFile();
        File parentFile = new File(URLDecoder.decode(filePath)).getParentFile();

        ArrayList<URL> list = new ArrayList<URL>();

        // Add jars from $BEES.REPO/lib1 default: [${user.home}/.bees/lib1]
        addJars(new File(getLocalRepository(), "lib1"), list);

        // Add jars from where the Launcher jar came from
        if (parentFile.isDirectory()) {
            addJars(parentFile, list);
        }

        return list.toArray(new URL[list.size()]);

    }

    private static void addJars(File dir, ArrayList<URL> list) throws MalformedURLException {
        String [] files = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jar"));
            }
        });
        if (files != null) {
            for (int i = 0, n = files.length; i < n; i++) {
                URL u = new File(dir, files[i]).toURI().toURL();
                list.add(u);
            }
        }
    }
}
