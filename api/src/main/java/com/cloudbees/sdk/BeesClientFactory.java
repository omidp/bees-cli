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
    UserConfiguration config;

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
            key = Helper.promptFor("Enter your CloudBees API key: ", true);
        }
        if (secret == null) {
            secret = PasswordHelper.prompt("Enter your CloudBees Api secret: ");
        }
    }

    public Properties getConfigProperties() {
        if (properties == null) {
            // Read SDK config file
            Map<String, String> params = getParameters();
            properties = config.load(UserConfiguration.EMAIL_CREDENTIALS, params, false); // TODO: verbose flag
        }
        return properties;
    }

    /**
     * See {@link UserConfiguration} for parameters vs config properties
     */
    public Map<String, String> getParameters() {
        Map<String, String> params = new HashMap<String, String>();
        add(params, "key", key);
        add(params, "secret", secret);
        add(params, "server", server);
        add(params, "proxy.host", proxyHost);
        add(params, "proxy.port", proxyPort);
        add(params, "proxy.user", proxyUser);
        add(params, "proxy.password", proxyPassword);
        return params;
    }

    private void add(Map<String, String> params, String key, String value) {
        if (value!=null)    params.put(key,value);
    }
}
