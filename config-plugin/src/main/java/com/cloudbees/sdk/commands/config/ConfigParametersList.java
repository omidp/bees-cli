package com.cloudbees.sdk.commands.config;


import com.cloudbees.api.BeesClient;
import com.cloudbees.api.ConfigurationParametersResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.commands.app.ApplicationBase;

import java.io.File;
import java.io.FileWriter;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Configuration")
@CLICommand("config:list")
public class ConfigParametersList extends ApplicationBase {
    private String file;
    private boolean accountOnly;

    public ConfigParametersList() {
    }

    @Override
    public void setAccount(String account) {
        super.setAccount(account);
        accountOnly = true;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    protected String getUsageMessage() {
        return "";
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "appid", true, "CloudBees application ID");
        addOption( "ac", "account", true, "CloudBees account name" );
        addOption( "fi", "file", true, "Output file name" );
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getStaxClient();

        String resourceId;
        String resourceType;
        if (accountOnly) {
            resourceId = getAccount();
            resourceType = "global";
        } else {
            resourceId = getAppId();
            resourceType = "application";
        }
        ConfigurationParametersResponse res = client.configurationParameters(resourceId, resourceType);

        if (res.getConfiguration() != null && file != null) {
            File xmlFile = new File(file);
            FileWriter fos = null;
            try {
                fos = new FileWriter(xmlFile);
                fos.write(res.getConfiguration());
            } finally {
                if (fos != null)
                    fos.close();
            }

        } else {
            if (isTextOutput()) {
                if (res.getConfiguration() != null)
                    System.out.println(res.getConfiguration());
            } else {
                printOutput(res, ConfigurationParametersResponse.class);
            }
        }
        return true;
    }

}
