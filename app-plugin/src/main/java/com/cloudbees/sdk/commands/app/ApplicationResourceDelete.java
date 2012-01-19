package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.*;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.utils.Helper;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:resource:delete")
public class ApplicationResourceDelete extends ApplicationResourceBase {
    private Boolean force;

    public ApplicationResourceDelete() {
        setArgumentExpected(1);
    }

    protected boolean forceDelete() {
        return force == null ? false : force.booleanValue();
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    protected String getUsageMessage() {
        return "RESOURCE_NAME";
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( "f", "force", false, "force delete without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String resource = getParameterName();
        String[] parts = resource.split("/");
        if (parts.length == 1)
            resource = getAccount() + "/" + resource;
        if (!forceDelete()) {
            if (!Helper.promptMatches("Are you sure you want to delete this application resource [" + resource + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        StaxClient client = getStaxClient(StaxClient.class);
        ServiceResourceDeleteResponse res = client.serviceResourceDelete(getServiceName(), resource);
        if (isTextOutput()) {
            if(res.isDeleted()) {
                System.out.println(String.format("Application resource %s deleted.", resource));
            } else {
                System.out.println("Application resource could not be deleted");
            }
        } else
            printOutput(res, ServiceResourceDeleteResponse.class);

        return true;
    }

}

