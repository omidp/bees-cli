package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationScaleResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:scale")
public class ApplicationScale extends ApplicationBase {
    private String up;

    private String down;

    public ApplicationScale() {
        setArgumentExpected(0);
    }

    public void setUp(String up) {
        this.up = up;
    }

    public String getUp() {
        return up;
    }

    public String getDown() {
        return down;
    }

    public void setDown(String down) {
        this.down = down;
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( "up", true, "scale up");
            addOption( "down", true, "scale down" );

            return true;
        }
        return false;
    }

    @Override
    protected boolean postParseCommandLine() {
        if (super.postParseCommandLine()) {
            if (getUp() == null && getDown() == null)
                throw new IllegalArgumentException("Either up or down option needs to be specified");
        }
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        int quantity;
        if (getUp() != null)
            quantity = Integer.parseInt(getUp());
        else
            quantity = -Integer.parseInt(getDown());

        StaxClient client = getStaxClient(StaxClient.class);
        ApplicationScaleResponse res = client.applicationScale(appid, quantity);
        if (isTextOutput()) {
            System.out.println("application - " + appid + ": " + res.getStatus());
        } else
            printOutput(res, ApplicationScaleResponse.class);
        return true;
    }
}
