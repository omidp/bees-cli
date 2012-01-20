package com.cloudbees.sdk;

import com.cloudbees.sdk.cli.DirectoryStructure;
import com.cloudbees.sdk.utils.PersistedGAVStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Extensions, which are modules loaded into Guice along with the driver to expand the core functionality.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class InstalledExtensionList extends PersistedGAVStore {
    @Inject
    DirectoryStructure structure;

    @Override
    protected File getStorageDirectory() {
        return new File(structure.localRepository, "extensions");
    }
}
