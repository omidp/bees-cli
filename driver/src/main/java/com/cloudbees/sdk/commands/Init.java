package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.utils.Helper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: Fabian Donze
 */
public class Init extends Command {
    private String email;
    private String password;
    private String account;
    private Boolean force;
    private Boolean useKeys;

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

            Map<String, String> params = new HashMap<String, String>();
            if (getEmail() != null)
                params.put("email", getEmail());
            if (getPassword() != null)
                params.put("password", getPassword());
            if (getAccount() != null)
                params.put("domain", getAccount());
            if (getKey() != null)
                params.put("key", getKey());
            if (getSecret() != null)
                params.put("secret", getSecret());
            if (getServer() != null)
                params.put("server", getServer());
            if (getProxyHost() != null)
                params.put("proxy.host", getProxyHost());
            if (getProxyPort() != null)
                params.put("proxy.port", getProxyPort());
            if (getProxyUser() != null)
                params.put("proxy.user", getProxyUser());
            if (getProxyPassword() != null)
                params.put("proxy.password", getProxyPassword());
            int credentialType = getUseKeys() == null ? Helper.EMAIL_CREDENTIALS : Helper.KEYS_CREDENTIALS;
            return Helper.initConfigProperties(getLocalRepository(), true, credentialType, params, isVerbose());
        } catch (Exception e) {
            throw new RuntimeException("Initialization failure: " + e.getMessage(), e);
        }
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
