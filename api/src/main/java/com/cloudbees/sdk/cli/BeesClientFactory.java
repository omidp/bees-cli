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

package com.cloudbees.sdk.cli;

import com.cloudbees.api.BeesClientBase;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.sdk.UserConfiguration;
import com.cloudbees.sdk.utils.Helper;
import com.cloudbees.sdk.utils.PasswordHelper;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Component that you can inject to your command object to
 * build {@link com.cloudbees.sdk.api.BeesAPIClient} from a set of command line options.
 * 
 * @author Kohsuke Kawaguchi
 */
@CommandScope
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

    @Option(name="--proxyHost",usage="HTTP proxy host to route traffic through")
    public String proxyHost;
    @Option(name="--proxyPort",usage="HTTP proxy port. Must be specified if --proxyHost is used")
    public String proxyPort;
    @Option(name="--proxyUser",usage="User name for HTTP proxy if it  requires authentication")
    public String proxyUser;
    @Option(name="--proxyPassword",usage="Password for HTTP proxy. Must be specified if --proxyUser is used")
    public String proxyPassword;
    @Option(name="-ep",aliases="--endPoint", usage="CloudBes API end point [us | eu]")
    public String endPoint;

    private Properties properties;

    @Inject
    UserConfiguration config;

    @Inject
    Verbose verbose;

    public BeesClientBase get() throws IOException {
        return get(BeesClientBase.class);
    }
    
    public <T extends BeesClientBase> T get(Class<T> clientType) throws IOException {
        BeesClientConfiguration beesClientConfiguration = createConfigurations();

        try {
            T client = clientType.getConstructor(BeesClientConfiguration.class).newInstance(beesClientConfiguration);
            client.setVerbose(verbose.isVerbose());
            return  client;
        } catch (InstantiationException e) {
            throw (Error)new InstantiationError().initCause(e);
        } catch (IllegalAccessException e) {
            throw (Error)new IllegalAccessError().initCause(e);
        } catch (InvocationTargetException e) {
            throw (Error)new InstantiationError().initCause(e);
        } catch (NoSuchMethodException e) {
            throw (Error)new InstantiationError().initCause(e);
        }
    }

    /**
     * Creates a fully populated {@link BeesClientConfiguration} based on the current setting.
     */
    public BeesClientConfiguration createConfigurations() throws IOException {
        Properties properties = getConfigProperties();

        BeesClientConfiguration beesClientConfiguration = new BeesClientConfiguration(getApiUrl());

        String token = properties.getProperty("bees.api.oauth_token");
        if (token!=null) {
            // use OAuth token for authentication
            beesClientConfiguration.withOAuthToken(token);
        } else {
            // fallback to API key and secret
            if (key==null)      key = properties.getProperty("bees.api.key");
            if (secret==null)   secret = properties.getProperty("bees.api.secret");
            initCredentials();
            beesClientConfiguration.withApiKeyAndSecret(key,secret);
        }


        // Set proxy information
        beesClientConfiguration.setProxyHost(properties.getProperty("bees.api.proxy.host", proxyHost));
        if (properties.getProperty("bees.api.proxy.port") != null || proxyPort != null)
            beesClientConfiguration.setProxyPort(Integer.parseInt(properties.getProperty("bees.api.proxy.port", proxyPort)));
        beesClientConfiguration.setProxyUser(properties.getProperty("bees.api.proxy.user", proxyUser));
        beesClientConfiguration.setProxyPassword(properties.getProperty("bees.api.proxy.password", proxyPassword));
        return beesClientConfiguration;
    }

    public String getApiUrl() {
        String apiUrl;
        if (server != null)
            apiUrl = server;
        else {
            if (endPoint != null)
                apiUrl = properties.getProperty("bees.api.url." + endPoint);
            else {
                apiUrl = properties.getProperty("bees.api.url");
                if (apiUrl == null)
                    apiUrl = properties.getProperty("bees.api.url.us");
            }
        }
        return apiUrl;
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
            properties = config.load(UserConfiguration.EMAIL_CREDENTIALS, params);
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
        add(params, "endPoint", endPoint);
        return params;
    }

    private void add(Map<String, String> params, String key, String value) {
        if (value!=null)    params.put(key,value);
    }
}
