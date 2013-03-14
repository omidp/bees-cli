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


import com.cloudbees.sdk.api.BeesAPIClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="SDK", experimental = true)
@CLICommand("call")
public class Call extends Command {
    String[] otherArgs;

    @Override
    protected String getUsageMessage() {
        return "parameters";
    }

    @Override
    protected boolean preParseCommandLine() {
        return true;
    }

    @Override
    protected boolean postParseCommandLine() {
        otherArgs = getCommandLine().getArgs();
        if (otherArgs == null || otherArgs.length == 0)
            return false;

        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        // call the application to the server
        BeesAPIClient client = getBeesClient(BeesAPIClient.class);

        client.mainCall(otherArgs);

        return true;
    }
}
