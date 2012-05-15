package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.CommandServiceImpl;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandService;
import com.cloudbees.sdk.utils.Helper;

import javax.inject.Inject;

/**
 * @author Fabian Donze
 */
@CLICommand("sdk:plugin:list")
@BeesCommand(group="SDK", description = "List CLI plugins")
public class PluginListCommand extends Command {
    @Inject
    CommandService commandService;

    public PluginListCommand() {
    }

    @Override
    protected boolean execute() throws Exception {
        CommandServiceImpl service = (CommandServiceImpl) commandService;
        System.out.println("Name               GroupId                             Version");
        System.out.println();
        for (GAV gav: service.getPlugins()) {
            String msg = s(gav.artifactId, 18)+ " " + s(gav.groupId, 36) + gav.version;
            System.out.println(msg);
        }
        return true;
    }

    private String s(String str, int length) {
        return Helper.getPaddedString(str, length);
    }

}

