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
import com.cloudbees.sdk.cli.AbstractCommand;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.ACommand;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Help command.
 * 
 * @author Kohsuke Kawaguchi
 */
@BeesCommand(group="SDK")
@CLICommand("help")
public class Help extends AbstractCommand {
    @Inject
    CommandServiceImpl commandService;

    /**
     * Sub-command to retrieve the help
     */
    @Argument(metaVar="CMD")
    public String subCommand;

    @Option(name="-all",usage="Show all commands, including those that are experimental")
    public boolean all;

    @Override
    public int main() throws Exception {
        ACommand cmd = null;

        if (subCommand!=null)
            cmd = commandService.getCommand(subCommand);
        if (cmd!=null) {
            List<String> args = new ArrayList<String>();
            args.add(subCommand);
            if (all)
                args.add("-all");
            cmd.printHelp(args);
        } else
            printHelp(all);

        return 1;
    }

    public void printHelp(boolean all) {
        System.out.println(commandService.getHelp(getClass().getClassLoader().getResource(help_file_name), "subcommands:", all));
    }

    private final static String help_file_name = "beesHelp.txt";
}
