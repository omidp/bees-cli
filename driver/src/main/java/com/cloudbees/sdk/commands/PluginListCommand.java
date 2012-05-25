package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.CommandServiceImpl;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@CLICommand("plugin:list")
@BeesCommand(group="SDK", description = "List CLI plugins")
public class PluginListCommand extends PluginVersionCommand {
    private Boolean check;

    public PluginListCommand() {
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
            addOption(null, "check", false, "check for newest versions");
            return true;
        }
        return false;
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
        if (check()) {
            for (GAV gav: service.getPlugins()) {
                System.out.println();
                checkVersion(gav);
            }
        }
        return true;
    }

    private String s(String str, int length) {
        return Helper.getPaddedString(str, length);
    }

}

