package com.cloudbees.sdk;

import com.cloudbees.sdk.cli.DirectoryStructure;
import com.cloudbees.sdk.utils.PersistedGAVStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * @author Fabian Donze
 */
@Singleton
public class PluginsToInstallList extends PersistedGAVStore {
    @Inject
    DirectoryStructure structure;

    protected File getStorageDirectory() {
        return new File(structure.sdkRepository.getParent(), "plugins");
    }
}
