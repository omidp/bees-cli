package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.CommandServiceImpl;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandService;

import javax.inject.Inject;

/**
 * @author Fabian Donze
 */
@CLICommand("sdk:plugin:list")
@BeesCommand(group="SDK", description = "List CLI plugins")
public class PluginDeleteCommand extends Command {
    @Inject
    CommandService commandService;

    public PluginDeleteCommand() {
        setArgumentExpected(1);
    }

    @Override
    protected String getUsageMessage() {
        return "PLUGIN_NAME";
    }

    @Override
    protected boolean execute() throws Exception {
        CommandServiceImpl service = (CommandServiceImpl) commandService;
        String name = getParameters().get(0);
        GAV gav = service.deletePlugin(name);
        if (gav != null) {
            System.out.println("Plugin deleted: " + gav);
        } else {
            System.out.println("Plugin not found: " + name);
        }
        return true;
    }

}

