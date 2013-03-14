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

package com.cloudbees.sdk.cli;

import java.io.File;

/**
 * Injectable component that represents the directory structure.
 *
 * @author Kohsuke Kawaguchi
 */
public class DirectoryStructure {
    public final File sdkRepository;
    public final File localRepository;
    public final String pluginExtension = ".bees";

    public DirectoryStructure(File sdkRepository, File localRepository) {
        this.sdkRepository = sdkRepository;
        this.localRepository = localRepository;
    }

    public DirectoryStructure() {
        this.sdkRepository = new File(System.getProperty("bees.home"), "conf");
        this.localRepository = calcLocalRepository();
    }

    /**
     * This directory contains additional jar files to be made available to the bees SDK's main classloader.
     */
    public File getLibDir() {
        return new File(localRepository,"lib1");
    }

    public File getPluginDir() {
        return new File(localRepository,"plugins");
    }

    public File getLocalRepository() {
        return localRepository;
    }

    private File calcLocalRepository() {
        if(System.getenv("BEES_REPO") != null)
            return new File(System.getenv("BEES_REPO"));
        else if(System.getProperty("bees.repo") != null)
            return new File(System.getProperty("bees.repo"));
        else
            return new File(System.getProperty("user.home"), ".bees");
    }

}
