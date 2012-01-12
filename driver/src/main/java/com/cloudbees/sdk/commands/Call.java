package com.cloudbees.sdk.commands;


import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.cli.Experimental;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("SDK")
@CLICommand("call")
@Experimental
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
        StaxClient client = getStaxClient();

        client.mainCall(otherArgs);

        return true;
    }
}
