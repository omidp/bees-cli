package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.cli.AbstractCommand;
import com.cloudbees.sdk.ArtifactClassLoaderFactory;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandDescription;
import com.cloudbees.sdk.cli.Experimental;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.InstalledExtensionList;
import com.google.inject.Provider;
import org.kohsuke.args4j.Argument;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
@Experimental
@CLICommand("install-extension")
@CommandDescription("Installs a CLI extension")
public class InstallExtensionCommand extends AbstractCommand {
    @Inject
    InstalledExtensionList list;

    @Argument(metaVar="GAV",required=true,usage="groupId:artifact[:version] of the extension to install")
    String gav;

    @Inject
    Provider<ArtifactClassLoaderFactory> artifactClassLoaderFactoryProvider;
    
    @Override
    public int main() throws Exception {
        GAV gav = parseGav();

        // resolve the artifact to make sure it's valid
        try {
            artifactClassLoaderFactoryProvider.get().add(gav);
        } catch (Exception e) {
            throw (IOException)new IOException("Failed to resolve "+gav).initCause(e);
        }

        // record it
        list.put(gav.groupId+'.'+gav.artifactId,gav);

        return 0;
    }

    private GAV parseGav() {
        String[] tokens = gav.split(":");
        if (tokens.length==3)
            return new GAV(gav);
        else
            return new GAV(tokens[0],tokens[1],"LATEST");
    }
}

