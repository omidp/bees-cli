package com.cloudbees.sdk;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.utils.Helper;
import com.cloudbees.sdk.utils.PasswordHelper;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Component that you can inject to your command object to
 * build {@link BeesClient} from a set of command line options.
 * 
 * @author Kohsuke Kawaguchi
 */
public class BeesClientFactory implements HasOptions {
    @Option(name="--server")
    public String server;

    /**
     * Bees api key.
     */
    @Option(name="-k",aliases="--key",usage="CloudBees API Key")
    public String key;

    /**
     * Bees api secret.
     */
    @Option(name="-s",aliases="--secret",usage="CloudBees API secret")
    public String secret;

    @Option(name="--proxyHost")
    public String proxyHost;
    @Option(name="--proxyPort")
    public String proxyPort;
    @Option(name="--proxyUser")
    public String proxyUser;
    @Option(name="--proxyPassword")
    public String proxyPassword;

    public String apiServer = "api.cloudbees.com";

    private Properties properties;

    @Inject
    DirectoryStructure directoryStructure;

    public StaxClient get() throws IOException {
        initCredentials();
        
        Properties properties = getConfigProperties();

        String apiUrl;
        if (server != null)
            apiUrl = server;
        else
            apiUrl = properties.getProperty("bees.api.url", String.format("https://%s/api",apiServer));

        BeesClientConfiguration beesClientConfiguration = new BeesClientConfiguration(apiUrl, key, secret, "xml", "1.0");

        // Set proxy information
        beesClientConfiguration.setProxyHost(properties.getProperty("bees.api.proxy.host", proxyHost));
        if (properties.getProperty("bees.api.proxy.port") != null || proxyPort != null)
            beesClientConfiguration.setProxyPort(Integer.parseInt(properties.getProperty("bees.api.proxy.port", proxyPort)));
        beesClientConfiguration.setProxyUser(properties.getProperty("bees.api.proxy.user", proxyUser));
        beesClientConfiguration.setProxyPassword(properties.getProperty("bees.api.proxy.password", proxyPassword));

        StaxClient staxClient = new StaxClient(beesClientConfiguration);
        staxClient.setVerbose(false);   // TODO
        return  staxClient;
    }

    private void initCredentials() throws IOException
    {
        if (key == null) {
            key = Helper.promptFor("Enter your CloudBees API key: ",true);
        }
        if (secret == null) {
            secret = PasswordHelper.prompt("Enter your CloudBees Api secret: ");
        }
    }

    protected Properties getConfigProperties() {
        if (properties == null) {
            // Read SDK config file
            Map<String, String> params = new HashMap<String, String>();
            if (key != null)
                params.put("key", key);
            if (secret != null)
                params.put("secret", secret);
            if (server != null)
                params.put("server", server);
            if (proxyHost != null)
                params.put("proxy.host", proxyHost);
            if (proxyPort != null)
                params.put("proxy.port", proxyPort);
            if (proxyUser != null)
                params.put("proxy.user", proxyUser);
            if (proxyPassword != null)
                params.put("proxy.password", proxyPassword);
            properties = Helper.initConfigProperties(directoryStructure.localRepository, false, Helper.EMAIL_CREDENTIALS, params, false); // TODO
        }
        return properties;
    }
}
