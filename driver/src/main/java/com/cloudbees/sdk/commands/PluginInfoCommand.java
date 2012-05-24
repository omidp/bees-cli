package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.ArtifactInstallFactory;
import com.cloudbees.sdk.CommandServiceImpl;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.Plugin;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
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
@CLICommand("sdk:plugin:info")
@BeesCommand(group="SDK", description = "CLI plugin info")
public class PluginInfoCommand extends PluginVersionCommand {
    private Boolean check;

    public PluginInfoCommand() {
        setArgumentExpected(1);
    }

    public boolean check() {
        return check == null ? false : check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption(null, "check", false, "check for newest version");
            return true;
        }
        return false;
    }
    @Override
    protected String getUsageMessage() {
        return "PLUGIN_NAME";
    }

    @Override
    protected boolean execute() throws Exception {
        CommandServiceImpl service = (CommandServiceImpl) commandService;
        String name = getParameters().get(0);
        Plugin plugin = service.getPlugin(name);
        if (plugin != null) {
            System.out.println();
            System.out.println("Plugin: " + plugin.getArtifact());
            String help = service.getHelp(plugin, "subcommands:", true);
            System.out.println(help);
            GAV gav = new GAV(plugin.getArtifact());
            if (check()) {
                return checkVersion(gav);
            }
        } else {
            throw new IOException("Plugin not found: " + name);
        }
        return true;
    }

}

