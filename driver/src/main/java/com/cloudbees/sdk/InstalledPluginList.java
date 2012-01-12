package com.cloudbees.sdk;

import com.cloudbees.sdk.cli.DirectoryStructure;
import com.cloudbees.sdk.utils.PersistedGAVStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Encapsulates access to the persisted installed plugins list (~/.bees/plugins/)
 *
 * This table is a look up table from command prefix (such as 'app' or 'db') to its implementation.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class InstalledPluginList extends PersistedGAVStore {
    @Inject
    DirectoryStructure structure;

    @Override
    protected File getStorageDirectory() {
        return new File(structure.localRepository, "plugins");
    }
}
