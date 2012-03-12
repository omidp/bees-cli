package com.cloudbees.sdk.commands.config;


import com.cloudbees.api.BeesClient;
import com.cloudbees.api.ConfigurationParametersDeleteResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.commands.app.ApplicationBase;
import com.cloudbees.sdk.utils.Helper;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Configuration")
@CLICommand("config:delete")
public class ConfigParametersDelete extends ApplicationBase {
    private boolean accountOnly;
    private Boolean force;

    public ConfigParametersDelete() {
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    public void setAccount(String account) {
        super.setAccount(account);
        accountOnly = true;
    }

    @Override
    protected String getUsageMessage() {
        return "";
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "appid", true, "CloudBees application ID");
        addOption( "ac", "account", true, "CloudBees account name" );
        addOption( "f", "force", false, "force delete without prompting" );
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        String resourceId;
        String resourceType;
        if (accountOnly) {
            resourceId = getAccount();
            resourceType = "global";
        } else {
            resourceId = getAppId();
            resourceType = "application";
        }

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to delete the " + resourceType + " configuration parameters for [" + resourceId + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        BeesClient client = getStaxClient();
        ConfigurationParametersDeleteResponse res = client.configurationParametersDelete(resourceId, resourceType);
        if (isTextOutput()) {
            System.out.println(resourceType + " config parameters for " + resourceId + ": " + res.getStatus());
        } else {
            printOutput(res, ConfigurationParametersDeleteResponse.class);
        }
        return true;
    }
    }
