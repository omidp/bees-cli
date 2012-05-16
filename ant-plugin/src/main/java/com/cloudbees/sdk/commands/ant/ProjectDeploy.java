package com.cloudbees.sdk.commands.ant;


import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Project")
@CLICommand("deploy")
public class ProjectDeploy extends AntTarget {
    private String message;
    private String environment;
    private String appid;
    private String delta;
    private Boolean deploySource;
    private String type;

    public ProjectDeploy() {
        super("deploy");
        setAddDefaultOptions(true);
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid == null ? "" : appid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public String getEnvironment() {
        return environment == null ? "" : environment;
    }

    public String getDelta() {
        return delta == null ? "true" : delta;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public boolean deploySource() {
        return deploySource == null ? false : deploySource;
    }

    public void setDeploySource(Boolean deploySource) {
        this.deploySource = deploySource;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "appid", true, "CloudBees application ID" );
        addOption( "m", "message", true, "Message describing the deployment" );
        addOption( "e", "environment", true, "Environment configurations to deploy" );
        addOption( "d", "delta", true, "true to enable, false to disable delta upload (default: true)" );
        addOption( "ds", "deploySource", false, "To deploy source package" , true);
        addOption("t", "type", true, "deployment container type");
        removeOption("o");

        return super.preParseCommandLine();
    }

    @Override
    protected boolean postParseCommandLine() {
      // Old properties, might still be used in old build.xml
        addAntProperty("stax.appid", getAppid());
        addAntProperty("stax.message", getMessage());
        addAntProperty("stax.environment", getEnvironment());

        addAntProperty("bees.appid", getAppid());
        addAntProperty("bees.message", getMessage());
        addAntProperty("bees.environment", getEnvironment());
        addAntProperty("bees.delta", getDelta());
        addAntProperty("bees.verbose", ""+isVerbose());
        addAntProperty("bees.deploySource", ""+deploySource());
        addAntProperty("bees.api.url", getApiUrl());
        if (getServer() != null)
            addAntProperty("bees.server", getServer());
        if (getType() != null)
            addAntProperty("bees.containerType", getType());

        //Warning: these properties are no longer used, but we need to set them since old
        //beta projects still check that they exist in the stax-build.xml file
        addAntProperty("stax.username", getKey() == null ? "" : getKey());
        addAntProperty("stax.password", getSecret() == null ? "" : getSecret());

        return super.postParseCommandLine();
    }
}
