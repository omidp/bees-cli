package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @Author: Fabian Donze
 */
@CLICommand("app:stop")
public class ApplicationStop extends ApplicationBase {
    private Boolean force;

    public ApplicationStop() {
        setArgumentExpected(0);
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force stop without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        String defaultAppDomain = getConfigProperties().getProperty("bees.project.app.domain");
        String[] appIdParts = appid.split("/");
        if (appIdParts.length < 2) {
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                appid = defaultAppDomain + "/" + appid;
            } else {
                throw new RuntimeException("default app account could not be determined, appid needs to be fully-qualified ");
            }
        }

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to stop this application [" + appid + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        StaxClient client = getStaxClient();
        ApplicationStatusResponse res = client.applicationStop(appid);

        if (isTextOutput()) {
            if(res.getStatus().equalsIgnoreCase("stopped"))
                System.out.println("application stopped - " + appid);
            else
                System.out.println("application could not be stopped, current status: " + res.getStatus());
        } else
            printOutput(res, ApplicationStatusResponse.class);

        return true;
    }

}
