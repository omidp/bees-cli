package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationDeleteResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application")
@CLICommand("app:delete")
public class ApplicationDelete extends ApplicationBase {
    private Boolean force;


    public ApplicationDelete() {
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force delete without prompting" );
            return true;
        }

        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to delete this application [" + appid + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        BeesClient client = getBeesClient();
        ApplicationDeleteResponse res = client.applicationDelete(appid);

        if (isTextOutput()) {
            if(res.isDeleted())
                System.out.println("application deleted - " + appid);
            else
                System.out.println("application could not be deleted");
        } else
            printOutput(res, ApplicationDeleteResponse.class);

        return true;
    }

}
