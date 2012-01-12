package com.cloudbees.sdk.commands.app;


import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:tail")
public class ApplicationTail extends ApplicationBase {

    private String logname;


    public ApplicationTail() {
        setArgumentExpected(0);
    }

    public void setLogname(String logname) {
        this.logname = logname;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "l", "logname", true, "The log name: server, access or error (Default: 'server')" );
            removeOption("o");
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        if (logname == null) logname = "server";

        StaxClient client = getStaxClient();
        client.tailLog(getAppId(), logname, System.out);

        return true;
    }

}
