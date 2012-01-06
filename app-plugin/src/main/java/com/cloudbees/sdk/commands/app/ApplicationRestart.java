package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationRestartResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @Author: Fabian Donze
 */
@CLICommand("app:restart")
public class ApplicationRestart extends ApplicationBase {
    private Boolean force;

    public ApplicationRestart() {
        setArgumentExpected(0);
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force restart without prompting" );
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
            if (!Helper.promptMatches("Are you sure you want to restart this application [" + appid + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        StaxClient client = getStaxClient();
        ApplicationRestartResponse res = client.applicationRestart(appid);

        if (isTextOutput()) {
            if(res.isRestarted())
                System.out.println("application restarted - " + appid);
            else
                System.out.println("application could not be restarted");
        } else
            printOutput(res, ApplicationRestartResponse.class);

        return true;
    }

}
