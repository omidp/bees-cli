package com.cloudbees.sdk.commands.app;

import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

import java.io.File;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:router:create")
public class ApplicationRouterCreate extends ApplicationResourceCreate {
    private String certificate;
    private String privateKey;
    private Boolean ssl;

    public ApplicationRouterCreate() {
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( "cert", "certificate", true, "SSL certificate file" );
            addOption( "pk", "privateKey", true, "SSL private key file" );
            addOption( "ssl", false, "SSL router" );

            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        Map<String, String> settings = getSettings();
        if (getCertificate() != null) {
            settings.put("cert", "file://" + getCertificate());
            if (!new File(getCertificate()).exists()) {
                throw new IllegalArgumentException("Certificate file not found: " + getCertificate());
            }
        }
        if (getPrivateKey() != null) {
            settings.put("key", "file://" + getPrivateKey());
            if (!new File(getPrivateKey()).exists()) {
                throw new IllegalArgumentException("Private key file not found: " + getPrivateKey());
            }
        }
        if (getSsl() != null)
            settings.put("ssl", getSsl().toString());
        return super.execute();
    }
}
