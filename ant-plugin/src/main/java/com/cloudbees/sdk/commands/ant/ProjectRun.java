package com.cloudbees.sdk.commands.ant;


import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Project")
@CLICommand("run")
public class ProjectRun extends AntTarget {
    private String port;
    private String environment;

    public ProjectRun() {
        super("run");
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getPort() {
        if (port == null)
            port = "8080";
        return port;
    }

    public String getEnvironment() {
        if (environment == null)
            environment = "";
        return environment;
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "p", "port", true, "server listen port (default: 8080)" );
        addOption( "e", "environment", true, "environment configurations to load (default: run)" );

        return super.preParseCommandLine();
    }

    @Override
    protected boolean postParseCommandLine() {
        addAntProperty("run.port", getPort());
        addAntProperty("run.environment", getEnvironment());
        return super.postParseCommandLine();
    }
}
