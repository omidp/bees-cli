package com.cloudbees.sdk.commonds.debugger;

import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.sdk.cli.BeesClientFactory;
import com.cloudbees.sdk.cli.CommandScope;
import com.cloudbees.sdk.cli.HasOptions;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Injectable component that connects to SSH server.
 *
 * @author Kohsuke Kawaguchi
 */
@CommandScope
public class SshConnectionFactory implements HasOptions {
    // this is where we get proxy configuration from
    @Inject
    BeesClientFactory bees;

    @Option(name="--privateKey",usage="SSH private key(s) for authentication with the backend")
    List<File> privateKeys = new ArrayList<File>();

    /**
     * Connects and authenticates to the server, returning the resulting connection.
     */
    public Connection connect(String host, int port) throws IOException {
        // the trick is to run the server on port 443 so that we can tunnel
        // this over HTTP proxy by pretending to be HTTPS
        //        Connection connection = new Connection("debugger.cloudbees.com", 443);
        Connection connection = new Connection(host, port);

        BeesClientConfiguration config = bees.createConfigurations();
        if (config.getProxyHost()!=null) {
            // tunnel this over HTTP proxy
            connection.setProxyData(new HTTPProxyData(config.getProxyHost(),config.getProxyPort(),config.getProxyUser(),config.getProxyPassword()));
        }

        connection.connect(); // TODO: verify the host key

        if (privateKeys.isEmpty())
            inferDefaultPrivateKeys();

        // TODO: figure out the user name, not that it really matters
        for (File privateKey : privateKeys) {
            if (connection.authenticateWithPublicKey("kohsuke", privateKey, null)) {
                break;
            }
        }

        if (!connection.isAuthenticationComplete())
            throw new IOException("Public key authentication with the server failed");

        return connection;
    }

    /**
     * Picks up the default SSH private keys.
     */
    private void inferDefaultPrivateKeys() {
        File home = new File(System.getProperty("user.home"));
        for (String name : new String[]{".ssh/id_rsa", ".ssh/id_dsa"}) {
            File key = new File(home, name);
            if (key.exists()) privateKeys.add(key);
        }
    }
}
