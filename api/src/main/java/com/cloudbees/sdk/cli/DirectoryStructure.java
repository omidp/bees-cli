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

    public DirectoryStructure(File sdkRepository, File localRepository) {
        this.sdkRepository = sdkRepository;
        this.localRepository = localRepository;
    }

    public DirectoryStructure() {
        this.sdkRepository = new File(System.getProperty("bees.home"), "conf");
        this.localRepository = getLocalRepository();
    }

    /**
     * This directory contains additional jar files to be made available to the bees SDK's main classloader.
     */
    public File getLibDir() {
        return new File(localRepository,"lib");
    }

    private File getLocalRepository() {
        if(System.getenv("BEES_REPO") != null)
            return new File(System.getenv("BEES_REPO"));
        else if(System.getProperty("bees.repo") != null)
            return new File(System.getProperty("bees.repo"));
        else
            return new File(System.getProperty("user.home"), ".bees");
    }

}
