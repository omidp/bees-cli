package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ServiceResourceUnBindResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.commands.services.ServiceBase;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:unbind")
public class ApplicationUnBind extends ServiceBase {
    /**
     * The id of the application.
     */
    private String appid;

    public ApplicationUnBind() {
        setArgumentExpected(1);
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "appid", true, "CloudBees application ID" );

        return true;
    }

    @Override
    protected String getUsageMessage() {
        return "BINDING_ALIAS";
    }

    @Override
    protected boolean execute() throws Exception {
        initAppId();

        StaxClient client = getStaxClient(StaxClient.class);
        ServiceResourceUnBindResponse res = client.resourceUnBind("cb-app", getAppid(), getAlias());
        if (isTextOutput()) {
//            System.out.println("Message: " + res.getMessage());
            System.out.println("application - " + getAppid() + " binding " + getAlias() + " removed");
        } else
            printOutput(res, ServiceResourceUnBindResponse.class);
        return true;
    }

    private String getAlias() {
        return getParameters().get(0);
    }

    private void initAppId() throws IOException
    {
        if (appid == null || appid.equals("")) {
            appid = AppHelper.getArchiveApplicationId();
        }

        if (appid == null || appid.equals(""))
            appid = Helper.promptForAppId();

        if (appid == null || appid.equals(""))
            throw new IllegalArgumentException("No application id specified");

        String[] parts = appid.split("/");
        if (parts.length < 2)
            appid = getAccount() + "/" + appid;
    }


}
