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

import com.cloudbees.sdk.ArtifactInstallFactory;
import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;
import com.google.inject.Provider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * @author Fabian Donze
 */
@CLICommand("plugin:install")
@BeesCommand(group="SDK", description = "Installs a CLI plugin")
public class PluginInstallCommand extends Command {

    String artifact;
    String localrepo;
    File jar;
    File pom;
    Boolean force;

    @Inject
    Provider<ArtifactInstallFactory> artifactInstallFactoryProvider;

    public PluginInstallCommand() {
        setAddDefaultOptions(false);
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getArtifact() throws IOException {
        if (artifact == null) artifact = Helper.promptFor("Artifact: ", true);
        return artifact;
    }

    public void setJar(String jar) {
        this.jar = new File(jar);
    }

    public void setPom(String pom) {
        this.pom = new File(pom);
    }

    public void setLocalrepo(String localrepo) {
        this.localrepo = localrepo;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    private boolean forceInstall() {
        return force != null ? force : false;
    }

    @Override
    protected String getUsageMessage() {
        return "ARTIFACT (groupId:name[:version])";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption("j", "jar", true, "the plugin jar file to install", true);
        addOption("p", "pom", true, "the plugin pom.xml file to install", true);
        addOption(null, "localrepo", true, "the maven local repo", true);
        addOption("v", "verbose", false, "verbose output");
        addOption("f", "force", false, "force install");
        return true;
    }

    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setArtifact(getParameters().get(0));
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        // install the artifact
        try {
            ArtifactInstallFactory installFactory = artifactInstallFactoryProvider.get();
            if (forceInstall())
                installFactory.setForceInstall(true);
            if (localrepo != null)
                installFactory.setLocalRepository(localrepo);
            installFactory.setBeesClientConfiguration(getBeesClientBase().getBeesClientConfiguration());
            GAV gav = new GAV(getArtifact());
            if (pom != null && jar != null) {
                gav = installFactory.install(gav, jar, pom);
            } else
                gav = installFactory.install(gav);
            System.out.println("Plugin installed: " + gav);
        } catch (Exception e) {
            throw (IOException) new IOException("Failed to install " + getArtifact()).initCause(e);
        }
        return true;
    }

}

