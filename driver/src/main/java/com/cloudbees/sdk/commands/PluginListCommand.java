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
            addOption(null, "check", false, "check for newer versions");
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

