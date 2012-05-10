package com.cloudbees.sdk.commands.config;


import com.cloudbees.api.BeesClient;
import com.cloudbees.api.ConfigurationParametersResponse;
import com.cloudbees.api.ConfigurationParametersUpdateResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.commands.app.ApplicationBase;
import com.cloudbees.sdk.commands.config.model.ConfigParameters;
import com.cloudbees.sdk.commands.config.model.Environment;
import com.cloudbees.sdk.commands.config.model.ResourceSettings;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Configuration")
@CLICommand("config:unset")
public class ConfigParametersUnSet extends ApplicationBase {
    private boolean accountOnly;
    private String environment;
    private String name;

    public ConfigParametersUnSet() {
    }

    @Override
    public void setAccount(String account) {
        super.setAccount(account);
        accountOnly = true;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected String getUsageMessage() {
        return "[name]";
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "appid", true, "CloudBees application ID");
        addOption( "ac", "account", true, "CloudBees account name" );
        addOption( "e", "environment", true, "Optional environment scope (only for account parameters)", true);
        addOption( "n", "name", true, "Optional resource name to unset a specific resource parameter", true);
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getBeesClient();

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

        ConfigParameters configParameters = ConfigParameters.parse(res.getConfiguration());

        List<String> otherArgs = getParameters();

        if (otherArgs.size() > 0) {
            for (String name : otherArgs) {
                if (getEnvironment() != null) {
                    Environment environment = configParameters.getEnvironment(getEnvironment());
                    deleteParameter(configParameters, name, getName(), environment);
                } else {
                    deleteParameter(configParameters, name, getName(), null);
                }
            }
        } else if (getName() != null) {
            if (getEnvironment() != null) {
                Environment environment = configParameters.getEnvironment(getEnvironment());
                deleteParameter(configParameters, getName(), null, environment);
            } else {
                deleteParameter(configParameters, getName(), null, null);
            }
        } else if (getEnvironment() != null) {
            configParameters.deleteEnvironment(getEnvironment());
        }


        File xmlFile = File.createTempFile("conf", "xml");
        xmlFile.deleteOnExit();
        FileWriter fos = null;
        try {
            fos = new FileWriter(xmlFile);
            fos.write(configParameters.toXML());
        } finally {
            if (fos != null)
                fos.close();
        }

        ConfigurationParametersUpdateResponse res2 = client.configurationParametersUpdate(resourceId, resourceType, xmlFile);
        if (isTextOutput()) {
            System.out.println(resourceType + " config parameters for " + resourceId + ": updated");
        } else {
            printOutput(res2, ConfigurationParametersUpdateResponse.class);
        }

        System.out.println(configParameters.toXML());

        return true;
    }

    private void deleteParameter(ConfigParameters configParameters, String parameterName, String resourceName, Environment environment) {
        if (resourceName != null) {
            ResourceSettings resource;
            if (environment != null) {
                resource = environment.getResource(resourceName);
            } else {
                resource = configParameters.getResource(resourceName);
            }
            if (resource != null) {
                resource.deleteParameter(parameterName);
            }
        } else {
            if (environment != null) {
                environment.deleteResource(parameterName);
                environment.deleteParameter(parameterName);
            } else {
                configParameters.deleteResource(parameterName);
                configParameters.deleteParameter(parameterName);
            }
        }
    }

}
