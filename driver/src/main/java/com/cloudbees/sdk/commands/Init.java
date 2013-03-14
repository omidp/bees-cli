/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.UserConfiguration;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.repository.LocalRepository;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="SDK")
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
        return new Properties();
    }

    private void add(Map<String, String> params, String key, String value) {
        if (value!=null)    params.put(key,value);
    }

    @Override
    protected boolean execute() {
        try {
            File userConfigFile = new File(getLocalRepository(), "bees.config");
            if (userConfigFile.exists()) {
                String input = "y";
                if (getForce() == null || !getForce().booleanValue()) {
                    System.out.println("WARNING: This command will delete your current configuration directory, including all installed plugins. " +
                            "Answering \"no\" will only re-initialize your bees.config file and leave your installed plugins intact.");
                    input = Helper.promptFor("Are you sure you want to delete your '" + getLocalRepository() + "' configuration directory?  (y/n) ", true);
                }
                input = input.toLowerCase().trim();
                if (input.startsWith("y")) {
                    Helper.deleteDirectory(getLocalRepository());
                } else {
                    userConfigFile.delete();
                }
            }

            // Reset the version check
            LocalRepository localRepository = new LocalRepository();
            String beesRepoPath = localRepository.getRepositoryPath();
            File lastCheckFile = new File(beesRepoPath, "sdk/check.dat");
            if (lastCheckFile.exists()) lastCheckFile.delete();

            // Reset the bees.config
            Map<String, String> params = beesClientFactory.getParameters();
            add(params, "email", getEmail());
            add(params, "password", getPassword());
            add(params, "domain", getAccount());

            int credentialType = getUseKeys() == null ? UserConfiguration.EMAIL_CREDENTIALS : UserConfiguration.KEYS_CREDENTIALS;
            config.load(credentialType, params);
        } catch (Exception e) {
            throw new RuntimeException("Initialization failure: " + e.getMessage(), e);
        }
        return true;
    }

    @Override
    protected int getResultCode() {
        return 99;
    }
}
