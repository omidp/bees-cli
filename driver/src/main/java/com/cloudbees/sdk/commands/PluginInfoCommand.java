/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.CommandServiceImpl;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.Plugin;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.io.IOException;

/**
 * @author Fabian Donze
 */
@CLICommand("plugin:info")
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
            addOption(null, "check", false, "check for newer version");
            addOption("v", "verbose", false, "verbose output");
            return true;
        }
        return false;
    }
    @Override
    protected String getUsageMessage() {
        return "PLUGIN_NAME";
    }

    private String getPluginName() {
        String name = getParameters().get(0);
        if (name.indexOf(':') > -1) {
            String[] parts = name.split(":");
            name = parts[1];
        }
        return name;
    }

    @Override
    protected boolean execute() throws Exception {
        CommandServiceImpl service = (CommandServiceImpl) commandService;
        String name = getPluginName();
        Plugin plugin = service.getPlugin(name);
        if (plugin != null) {
            System.out.println();
            System.out.println("Plugin: " + plugin.getArtifact());
            String help = service.getHelp(plugin, "subcommands:", true);
            System.out.println(help);
            if (check()) {
                GAV gav = new GAV(plugin.getArtifact());
                return checkVersion(gav);
            }
        } else {
            throw new IOException("Plugin not found: " + name);
        }
        return true;
    }

}

