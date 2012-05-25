package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.ArtifactInstallFactory;
import com.cloudbees.sdk.Bees;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.cli.CommandService;
import com.cloudbees.sdk.cli.ICommand;
import com.cloudbees.sdk.utils.Helper;
import com.google.inject.Provider;
import hudson.util.VersionNumber;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.version.Version;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Fabian Donze
 */
public abstract class PluginVersionCommand extends Command {

    private Boolean force;

    @Inject
    CommandService commandService;

    String localrepo;

    @Inject
    Provider<ArtifactInstallFactory> artifactInstallFactoryProvider;

    public PluginVersionCommand() {
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public void setLocalrepo(String localrepo) {
        this.localrepo = localrepo;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption("f", "force", false, "force update to newest version without prompting");
        addOption("l", "localrepo", true, "the maven local repo", true);

        return true;
    }

    protected boolean checkVersion(GAV gav) throws Exception {
        try {
            System.out.println(String.format("Checking [%s] for newer version...", gav));
            ArtifactInstallFactory installFactory = artifactInstallFactoryProvider.get();
            if (localrepo != null)
                installFactory.setLocalRepository(localrepo);
            installFactory.setBeesClientConfiguration(getBeesClient().getBeesClientConfiguration());

            VersionRangeResult rangeResult = installFactory.findVersions(gav);
            Version newestVersion = rangeResult.getHighestVersion();

            VersionNumber currentVersion = new VersionNumber(gav.version);
            VersionNumber availableVersion = new VersionNumber(newestVersion.toString());

            System.out.println();
            if (currentVersion.compareTo(availableVersion) < 0) {
                System.out.println("A newest version [" + newestVersion + "] exists from repository: "
                        + rangeResult.getRepository(newestVersion));

                boolean install = true;
                if (force == null || !force.booleanValue()) {
                    System.out.println();
                    install = Helper.promptMatches("Do you want to install the latest version [" + newestVersion + "]: (y/n) ", "[yY].*");
                }
                System.out.println();
                GAV newGAV = new GAV(gav.groupId, gav.artifactId, newestVersion.toString());
                if (install) {
                    ICommand installPluginCmd = commandService.getCommand(Bees.SDK_PLUGIN_INSTALL);
                    System.out.println("Installing plugin: " + newGAV);
                    installPluginCmd.run(Arrays.asList(Bees.SDK_PLUGIN_INSTALL, newGAV.toString()));
                } else {
                    System.out.println("You can install the latest version with:");
                    System.out.println("> bees " + Bees.SDK_PLUGIN_INSTALL + " " + newGAV.toString());
                }
            } else {
                System.out.println("The latest version is already installed");
            }
        } catch (Exception e) {
            throw new IOException(gav.toString(), e);
        }
        return true;
    }

}

