package com.cloudbees.sdk.commonds.debugger;

import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.sdk.cli.BeesClientFactory;
import com.cloudbees.sdk.cli.HasOptions;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * Injectable component that connects to SSH server.
 *
 * @author Kohsuke Kawaguchi
 */
public class SshConnectionFactory implements HasOptions {
    // this is where we get proxy configuration from
    @Inject
    BeesClientFactory bees;

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

        // TODO: figure out the user name
        if (!connection.authenticateWithPublicKey("kohsuke", new File("/home/kohsuke/.ssh/id_rsa"), null)) {
            throw new IOException("Public key authentication with the server failed");
        }

        return connection;
    }
}
