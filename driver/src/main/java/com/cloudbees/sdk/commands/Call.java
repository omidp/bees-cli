package com.cloudbees.sdk.commands;


import com.cloudbees.api.StaxClient;

/**
 * @Author: Fabian Donze
 */
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
