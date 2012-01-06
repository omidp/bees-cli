package com.cloudbees.sdk;

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
        this.localRepository = new File(getLocalRepository(), "lib");
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
