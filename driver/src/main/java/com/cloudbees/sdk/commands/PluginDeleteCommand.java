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
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandService;
import com.cloudbees.sdk.utils.Helper;

import javax.inject.Inject;

/**
 * @author Fabian Donze
 */
@CLICommand("plugin:delete")
@BeesCommand(group="SDK", description = "Delete a SDK plugin")
public class PluginDeleteCommand extends Command {
    private Boolean force;

    @Inject
    CommandService commandService;

    public void setForce(Boolean force) {
        this.force = force;
    }

    public PluginDeleteCommand() {
        setArgumentExpected(1);
        setAddDefaultOptions(false);
    }

    @Override
    protected String getUsageMessage() {
        return "PLUGIN_NAME";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "f", "force", false, "force deletion without prompting" );
        addOption("v", "verbose", false, "verbose output");

        return true;
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
        String name = getPluginName();
        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to delete this plugin [" + name + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }
        CommandServiceImpl service = (CommandServiceImpl) commandService;
        GAV gav = service.deletePlugin(name);
        if (gav != null) {
            System.out.println("Plugin deleted: " + gav);
        } else {
            System.out.println("Plugin not found: " + name);
        }
        return true;
    }

}

