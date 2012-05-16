package com.staxnet.ant;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.api.HashWriteProgress;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;
import com.staxnet.appserver.utils.ZipHelper;
import com.staxnet.appserver.utils.ZipHelper.ZipEntryHandler;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

public class DeployTask extends Task {
    private File deployFile;
    private File srcFile;
    private String message;
    private String containerType;
    private String username;
    private String password;

    private String apiKey;
    private String apiSecret;
    private String environment;
    private String appId;
    private String delta;
    private boolean verbose;
    private boolean deploySource;

    @Override
    public void execute() throws BuildException {
        deployFile = getAttributeFile("deployfile", deployFile, true);
        if(srcFile != null && srcFile.length() > 0)
            srcFile = getAttributeFile("srcfile", srcFile, false);

        // By default even if srcFile is defined in the build file, the source will not be deployed
        // unless the deploySource option (-ds) is defined
        deploySource = Boolean.parseBoolean(getAttributeValue("deploySource", null, false));
        if (!deploySource)
            srcFile = null;

        appId = getAttributeValue("appid", emptyValueToNull(appId), false);
        message = getAttributeValue("message", emptyValueToNull(message), false);
        containerType = getAttributeValue("containerType", emptyValueToNull(containerType), false);
        username = emptyValueToNull(getAttributeValue("username", username,
                false));
        password = emptyValueToNull(getAttributeValue("password", password,
                false));
        if ((password != null && !password.equals(""))
                || (username != null && !username.equals(""))) {
            // log("Ignoring Stax username and password, they are no longer
            // supported for deployments");
        }

        verbose = Boolean.parseBoolean(getAttributeValue("verbose", "false", false));
        apiKey = getAttributeValue("apiKey", getProject().getProperty(
                "beesConfig.bees.api.key"), false);
        apiSecret = getAttributeValue("apiSecret", getProject().getProperty(
                "beesConfig.bees.api.secret"), false);
        String apiVersion = "1.0";
        if (apiKey == null) {
            if (username == null)
            {
                username = getProject().getProperty(
                        "beesConfig.bees.api.username");
            }
            if (password == null)
            {
                password = getProject().getProperty(
                        "beesConfig.bees.api.password");
            }
            // try to use the username/password instead
            if (username != null) {
                apiKey = username;
                apiSecret = password;
                apiVersion = "0.1";
            }
        }
        if(apiKey == null || apiKey.equals("") || apiSecret == null || apiSecret.equals(""))
        {
            throw new BuildException("CloudBees credentials not provided");
        }

        String defaultAppDomain = getProject().getProperty(
                "beesConfig.bees.project.app.domain");

        if(defaultAppDomain == null || defaultAppDomain.equals(""))
            defaultAppDomain = username;
        environment = emptyValueToNull(getAttributeValue("environment",
                emptyValueToNull(environment), false));

        String[] appIdParts = appId != null ? appId.split("@") : new String[0];
        String appId = appIdParts.length > 0 ? appIdParts[0] : null;

        String server = appIdParts.length == 2 ? appIdParts[1] : null;

        int mbFileSize = (int) (deployFile.length() / 1024f / 1024f);
        if (srcFile != null)
            mbFileSize = (int) (srcFile.length() / 1024f / 1024f);
        if (mbFileSize > 1)
            System.out.println(String.format("application package size: %dMB", mbFileSize));

        try {
            String apiUrl = null;
            if (server != null) {
                apiUrl = String.format("https://%s/api", server);
            } else {
                apiUrl = getAttributeValue("server", null, false);
                if (apiUrl == null) {
                    apiUrl = getAttributeValue("api.url", null, false);
                }
            }
            if (apiUrl == null)
                throw new IllegalArgumentException("No API end point url specified");

            BeesClientConfiguration beesClientConfiguration = new BeesClientConfiguration(apiUrl, apiKey, apiSecret, "xml", apiVersion);

            // Set proxy information
            beesClientConfiguration.setProxyHost(getAttributeValue("api.proxy.host", getProject().getProperty("beesConfig.bees.api.proxy.host"), false));
            String hostPort = getAttributeValue("api.proxy.port", getProject().getProperty("beesConfig.bees.api.proxy.port"), false);
            if (hostPort != null)
                beesClientConfiguration.setProxyPort(Integer.parseInt(hostPort));
            beesClientConfiguration.setProxyUser(getAttributeValue("api.proxy.user", getProject().getProperty("beesConfig.bees.api.proxy.user"), false));
            beesClientConfiguration.setProxyPassword(getAttributeValue("api.proxy.password", getProject().getProperty("beesConfig.bees.api.proxy.password"), false));

            BeesClient client = new BeesClient(beesClientConfiguration);
            client.setVerbose(verbose);

            AppConfig appConfig = getAppConfig(deployFile, Helper.getEnvironmentList(environment), new String[] { "deploy" });
            if (appId == null || appId.equals("")) {
                appId = appConfig.getApplicationId();

                if (appId == null || appId.equals(""))
                    throw new IllegalArgumentException("No application id specified");
            }

            appIdParts = appId.split("/");
            String domain = null;
            if (appIdParts.length > 1) {
                domain = appIdParts[0];
            } 
            else if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                domain = defaultAppDomain;
                appId = domain + "/" + appId;
            }
            else
            {
                throw new BuildException("default app domain could not be determined, appid needs to be fully-qualified ");
            }

            environment = StringHelper.join(appConfig.getAppliedEnvironments().toArray(new String[0]), ",");

            System.out.println(String.format("Deploying application: %s (environment: %s)", appId, environment));

            String archiveType = deployFile.getName().endsWith(".war") ? "war" : "ear";

            delta = getAttributeValue("delta", emptyValueToNull(delta), false);
            boolean deployDelta = (this.delta == null || this.delta.equalsIgnoreCase("true")) && archiveType.equals("war");

            Map<String, String> parameters = new HashMap<String, String>();
            if (containerType != null)
                parameters.put("containerType", containerType);

            System.out.print("Uploading application ");
            if (deployDelta)
                System.out.print("deltas ");
            if (srcFile != null)
                System.out.print("+ source ");
            System.out.println("...");

            ApplicationDeployArchiveResponse res = client.applicationDeployArchive(appId, environment, message,
                    deployFile, srcFile, archiveType, deployDelta, parameters,
                    new HashWriteProgress());
            System.out.println("Application " + res.getId() + " deployed: " + res.getUrl());
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private String getAttributeValue(String attrName, String value,
            boolean required) {
        if (value == null) {
            value = System.getProperty("bees." + attrName);
            if (value == null)
                value = System.getProperty("stax." + attrName);
                if (value == null)
                    value = getProject().getProperty("bees." + attrName);
                    if (value == null)
                        value = getProject().getProperty("stax." + attrName);
            if (required && (value == null || value.equals("")))
                throw new BuildException("missing required attribute: "
                        + attrName);
        }
        return value;
    }

    private File getAttributeFile(String attrName, File value, boolean required) {
        if (value == null) {
            String strValue = System.getProperty("bees." + attrName);
            if (strValue == null)
                strValue = System.getProperty("stax." + attrName);
                if (strValue == null) {
                    if (required)
                        throw new BuildException("missing required attribute: "
                                + attrName);
                } else
                    value = new File(strValue);
        }
        return value;
    }

    public void setDeployfile(File deployFile) {
        this.deployFile = deployFile;
    }

    public void setSrcfile(File srcFile) {
        this.srcFile = srcFile;
    }

    public void setAppid(String appId) {
        this.appId = appId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public void setEnvironments(String environments) {
        this.environment = environments;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    private String emptyValueToNull(String value) {
        if (value != null && !value.equals(""))
            return value;
        return null;
    }

    private AppConfig getAppConfig(File deployZip, final String[] environments,
            final String[] implicitEnvironments) throws IOException {
        final AppConfig appConfig = new AppConfig();

        FileInputStream fin = new FileInputStream(deployZip);
        try {
            ZipHelper.unzipFile(fin, new ZipEntryHandler() {
                public void unzip(ZipEntry entry, InputStream zis)
                        throws IOException {
                    if (entry.getName().equals("META-INF/stax-application.xml")
                            || entry.getName().equals("WEB-INF/stax-web.xml")
                            || entry.getName().equals("WEB-INF/cloudbees-web.xml")) {
                        AppConfigHelper.load(appConfig, zis, null,
                                environments, implicitEnvironments);
                    }
                }
            }, false);
        } finally {
            fin.close();
        }

        return appConfig;
    }
    
    private String prompt(String message)
    {
        System.out.print(message);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
