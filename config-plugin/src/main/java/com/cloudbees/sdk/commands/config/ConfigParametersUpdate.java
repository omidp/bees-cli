package com.cloudbees.sdk.commands.config;


import com.cloudbees.api.BeesClient;
import com.cloudbees.api.ConfigurationParametersUpdateResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.commands.app.ApplicationBase;
import com.cloudbees.sdk.utils.Helper;

import java.io.File;
import java.io.IOException;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Configuration")
@CLICommand("config:update")
public class ConfigParametersUpdate extends ApplicationBase {
    private String file;
    private boolean accountOnly;

    public ConfigParametersUpdate() {
        setArgumentExpected(1);
    }

    @Override
    public void setAccount(String account) {
        super.setAccount(account);
        accountOnly = true;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFile() throws IOException {
        if (file == null) file = getParameters().get(0);
        if (file == null) file = Helper.promptFor("Config file name: ", true);
        return file;
    }

    @Override
    protected String getUsageMessage() {
        return "CONFIG_FILE";
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "appid", true, "CloudBees application ID");
        addOption( "ac", "account", true, "CloudBees account name" );
        addOption( "fi", "file", true, "Config file name" );
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        if (getFile() == null) {
            throw new IllegalArgumentException("Config file not specified");
        }
        File resourceFile = new File(getFile());
        if (!resourceFile.exists()) {
            throw new IllegalArgumentException("Config file not found: " + getFile());
        }

        String resourceId;
        String resourceType;
        if (accountOnly) {
            resourceId = getAccount();
            resourceType = "global";
        } else {
            resourceId = getAppId();
            resourceType = "application";
        }

        BeesClient client = getBeesClient();
        ConfigurationParametersUpdateResponse res = client.configurationParametersUpdate(resourceId, resourceType, resourceFile);
        if (isTextOutput()) {
            System.out.println(resourceType + " config parameters for " + resourceId + ": " + res.getStatus());
        } else {
            printOutput(res, ConfigurationParametersUpdateResponse.class);
        }
        return true;
    }
}
