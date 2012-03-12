package com.cloudbees.sdk.commands.config;


import com.cloudbees.api.BeesClient;
import com.cloudbees.api.ConfigurationParametersResponse;
import com.cloudbees.api.ConfigurationParametersUpdateResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.commands.app.ApplicationBase;
import com.cloudbees.sdk.commands.config.model.ConfigParameters;
import com.cloudbees.sdk.commands.config.model.Environment;
import com.cloudbees.sdk.commands.config.model.ParameterSettings;
import com.cloudbees.sdk.commands.config.model.ResourceSettings;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Configuration")
@CLICommand("config:set")
public class ConfigParametersSet extends ApplicationBase {
    private boolean accountOnly;
    private String environment;
    private String name;

    private String type;

    public ConfigParametersSet() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected String getUsageMessage() {
        return "[name=value]";
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "appid", true, "CloudBees application ID");
        addOption( "ac", "account", true, "CloudBees account name" );
        addOption( "n", "name", true, "Optional resource name to group parameters under one resource", true );
        addOption( "e", "environment", true, "Optional environment scope (only for account parameters)", true);
        addOption( "t", "type", true, "Optional resource type [jndi-env[:java_class_type] | system-property | context-param]", true);
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

        ConfigParameters configParameters = ConfigParameters.parse(res.getConfiguration());

        List<String> otherArgs = getParameters();
        if (getName() != null) {
            ResourceSettings resource = configParameters.getResource(getName());
            if (resource == null) resource = new ResourceSettings(getName(), type);
            for (String str : otherArgs) {
                int idx = isParameter(str);
                if (idx > -1) {
                    resource.setParameter(str.substring(0, idx), str.substring(idx + 1));
                }
            }
            if (resource.getParameters().size() > 0) {
                setResource(configParameters, resource, resourceType);
            }
        } else {
            for (String str : otherArgs) {
                int idx = isParameter(str);
                if (idx > -1) {
                    if (getType() != null) {
                        ResourceSettings resource = new ResourceSettings(str.substring(0, idx), getType(), str.substring(idx + 1));
                        setResource(configParameters, resource, resourceType);
                    } else {
                        ParameterSettings parameter = new ParameterSettings(str.substring(0, idx), str.substring(idx + 1));
                        setParameter(configParameters, parameter, resourceType);
                    }
                }
            }
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

    private void setResource(ConfigParameters configParameters, ResourceSettings resource, String resourceType) {
        if (getEnvironment() != null && !resourceType.equalsIgnoreCase("application")) {
            Environment environment = configParameters.getEnvironment(getEnvironment());
            if (environment == null) {
                environment = new Environment(getEnvironment());
                configParameters.setEnvironment(environment);
            }
            environment.setResource(resource);
        } else {
            configParameters.setResource(resource);
        }
    }

    private void setParameter(ConfigParameters configParameters, ParameterSettings parameter, String resourceType) {
        if (getEnvironment() != null && !resourceType.equalsIgnoreCase("application")) {
            Environment environment = configParameters.getEnvironment(getEnvironment());
            if (environment == null) {
                environment = new Environment(getEnvironment());
                configParameters.setEnvironment(environment);
            }
            environment.setParameter(parameter);
        } else {
            configParameters.setParameter(parameter);
        }
    }
}
