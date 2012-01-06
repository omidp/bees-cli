package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.UserConfiguration;
import com.cloudbees.sdk.utils.Helper;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: Fabian Donze
 */
@CLICommand("init")
public class Init extends Command {
    private String email;
    private String password;
    private String account;
    private Boolean force;
    private Boolean useKeys;

    @Inject
    UserConfiguration config;

    public Init() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Boolean getUseKeys() {
        return useKeys;
    }

    public void setUseKeys(Boolean useKeys) {
        this.useKeys = useKeys;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "e", "email", true, "CloudBees email address" );
        addOption( "p", "password", true, "CloudBees password" );
        addOption( "a", "account", true, "Default CloudBees account name" );
        addOption( "f", "force", false, "force deletion of previous configuration without prompting" );
        addOption( null, "useKeys", false, "use account keys instead of credentials" );
        removeOption("o");

        return true;
    }

    @Override
    protected boolean postParseCommandLine() {
        return true;
    }

    @Override
    protected Properties getConfigProperties() {
        try {
            File userConfigFile = new File(getLocalRepository(), "bees.config");
            if (userConfigFile.exists()) {
                String input = "y";
                if (getForce() == null || !getForce().booleanValue()) {
                    input = Helper.promptFor("Are you sure you want to delete your '" + getLocalRepository() + "' configuration directory?  (y/n) ", true);
                }
                input = input.toLowerCase().trim();
                if (input.startsWith("y")) {
                    Helper.deleteDirectory(getLocalRepository());
                }
            }

            Map<String, String> params = beesClientFactory.getParameters();
            add(params, "email", getEmail());
            add(params, "password", getPassword());
            add(params, "domain", getAccount());

            int credentialType = getUseKeys() == null ? UserConfiguration.EMAIL_CREDENTIALS : UserConfiguration.KEYS_CREDENTIALS;
            return config.create(credentialType, params);
        } catch (Exception e) {
            throw new RuntimeException("Initialization failure: " + e.getMessage(), e);
        }
    }

    private void add(Map<String, String> params, String key, String value) {
        if (value!=null)    params.put(key,value);
    }

    @Override
    protected boolean execute() {
        return true;
    }

    @Override
    protected int getResultCode() {
        return 99;
    }
}
