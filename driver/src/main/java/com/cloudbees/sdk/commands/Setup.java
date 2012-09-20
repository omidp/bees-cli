package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.UserConfiguration;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="SDK", experimental = true)
@CLICommand("setup")
public class Setup extends Command {

    @Inject
    UserConfiguration config;

    public Setup() {
    }

    @Override
    protected boolean execute() {
        File userConfigFile = new File(getLocalRepository(), "bees.config");
        if (!userConfigFile.exists()) {
            // Setup the bees.config
            Map<String, String> params = beesClientFactory.getParameters();

            config.load(UserConfiguration.EMAIL_CREDENTIALS, params);
        }
        return true;
    }

}
