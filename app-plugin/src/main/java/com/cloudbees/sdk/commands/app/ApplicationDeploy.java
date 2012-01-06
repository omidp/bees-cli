package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.HashWriteProgress;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.ant.ApplicationHelper;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;
import com.staxnet.appserver.utils.StringHelper;
import com.staxnet.appserver.utils.ZipHelper;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: Fabian Donze
 */
@CLICommand("app:deploy")
public class ApplicationDeploy extends Command {
    /**
     * The id of the stax application.
     */
    private String appid;

    /**
     * Configuration environments to use.
     */
    private String environment;

    /**
     * Message associated with the deployment.
     */
    private String message;

    /**
     * The path to the Bees deployment descriptor.
     *
     * parameter expression="${bees.appxml}" default-value =
     *            "${basedir}/src/main/config/stax-application.xml"
     */
    private File appConfig;

    /**
     * The path to the J2EE appplication deployment descriptor.
     *
     * parameter expression="${bees.j2ee.appxml}" default-value = "${basedir}/src/main/config/application.xml"
     */
    private File appxml;

    /**
     */
    private String baseDir;

    /**
     * The war file
     *
     */
    private File warFile;

    /**
     * The packaged deployment file.
     *
     */
    private File deployFile;

    /**
     * Bees deployment type.
     *
     * parameter expression="${bees.delta}" default-value = "true"
     */
    private String delta;

    private String descriptorDir;

    private String type;

    public ApplicationDeploy() {
    }

    @Override
    protected String getUsageMessage() {
        return "WAR_file";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "appid", true, "CloudBees application ID" );
        addOption( "m", "message", true, "Message describing the deployment" );
        addOption( "e", "environment", true, "Environment configurations to deploy" );
        addOption( "d", "delta", true, "true to enable, false to disable delta upload (default: true)" );
        addOption( "b", "baseDir", true, "Base directory (default: '.')");
        addOption("xd", "descriptorDir", true, "Directory containing application descriptors (default: 'src/main/conf/')");
        addOption("t", "type", true, "deployment container type");

        return true;
    }

    @Override
    protected boolean postParseCommandLine() {
        if (super.postParseCommandLine()) {
            boolean ok = true;
            List<String> otherArgs = getParameters();
            if (otherArgs.size() > 0) {
                setWarFile(new File(getBaseDir(), otherArgs.get(0)));
            }
            if (otherArgs.size() == 0 || warFile == null)
                ok = false;

            return ok;

        }
        return false;
    }

    @Override
    protected void initDefaults(Properties properties) {
        super.initDefaults(properties);
        setAppxml(new File(getDescriptorDir(), "application.xml"));
        setAppConfig(new File(getDescriptorDir(), "stax-application.xml"));
    }


    protected void setAppConfig(File appConfig) {
        this.appConfig = appConfig;
    }

    protected void setAppxml(File appxml) {
        this.appxml = appxml;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    protected void setWarFile(File warFile) {
        this.warFile = warFile;
    }

    public String getDescriptorDir() {
        return descriptorDir == null ? "src/main/conf" : descriptorDir;
    }

    public void setDescriptorDir(String descriptorDir) {
        this.descriptorDir = descriptorDir;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected boolean execute() throws Exception {

        // create the deployment package
        if(appConfig.exists() && appxml.exists())
        {
            FileOutputStream fstream = null;
            try {
                deployFile = new File(warFile.getParent(), warFile.getName() + ".ear");
                fstream = new FileOutputStream(deployFile);
                ZipOutputStream zos = new ZipOutputStream(fstream);
                ZipHelper.addFileToZip(warFile, "webapp.war", zos);
                ZipHelper.addFileToZip(appConfig, "META-INF/stax-application.xml", zos);
                ZipHelper.addFileToZip(appxml, "META-INF/application.xml", zos);
                zos.close();
            } catch (Exception e) {
                throw new RuntimeException("Package failure: " + e.getMessage(), e);
            }
        }
        else
        {
            // Experimental procfile support
            if (warFile.exists() && warFile.isFile() && warFile.getName().equals("Procfile")) {
                type = "procfile";
                // zip the Procfile directory
                deployFile = new File(warFile.getParentFile().getParent(), "procfile.zip");
                FileOutputStream fileOutputStream = new FileOutputStream(deployFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

                ZipHelper.addDirectoryToZip(warFile.getParentFile(), null, null, zipOutputStream);

                close(zipOutputStream);
                close(fileOutputStream);

                deployFile.deleteOnExit();
            } else {
                deployFile = warFile;
            }
        }

        // deploy the application to the server
        AppConfig appConfig = getAppConfig(  deployFile,
                                                    ApplicationHelper.getEnvironmentList(environment),
                                                    new String[]{"deploy"});
        initAppId(appConfig);

        String defaultAppDomain = getConfigProperties().getProperty("bees.project.app.domain");
        String[] appIdParts = appid.split("/");
        if (appIdParts.length < 2) {
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                appid = defaultAppDomain + "/" + appid;
            } else {
                throw new RuntimeException("default app account could not be determined, appid needs to be fully-qualified ");
            }
        }

        environment = StringHelper.join(appConfig.getAppliedEnvironments().toArray(new String[0]), ",");

        System.out.println(String.format("Deploying application %s (environment: %s): %s", appid, environment, deployFile));
        StaxClient client = getStaxClient();

        boolean deployDelta = (delta == null || delta.equalsIgnoreCase("true")) ? true : false;

        Map<String, String> parameters = new HashMap<String, String>();
        if (type != null)
            parameters.put("containerType", type);

        List<String> otherArgs = getParameters();
        for (int i = 1; i<otherArgs.size(); i++) {
            String str = otherArgs.get(i);
            int idx = isParameter(str);
            if (idx > -1)
                parameters.put(str.substring(0, idx), str.substring(idx+1));
        }
        if (parameters.size() > 0)
            System.out.println("Deploy parameters: " + parameters);

        ApplicationDeployArchiveResponse res;
        if(deployFile.getName().endsWith(".war"))
        {
            res = client.applicationDeployArchive(appid, environment, message,
                    deployFile.getAbsolutePath(), null, "war", deployDelta, parameters,
                    new HashWriteProgress());
        }
        else
        {
            res = client.applicationDeployArchive(appid, environment, message,
                    deployFile.getAbsolutePath(), null, "ear", false, parameters,
                    new HashWriteProgress());
        }
        if (isTextOutput())
            System.out.println("Application " + res.getId() + " deployed: " + res.getUrl());
        else
            printOutput(res, ApplicationDeployArchiveResponse.class);

        return true;
    }

    private void initAppId(AppConfig appConfig) throws IOException
    {
        if (appid == null || appid.equals("")) {
            appid = appConfig.getApplicationId();

            if (appid == null || appid.equals(""))
                appid = Helper.promptForAppId();

            if (appid == null || appid.equals(""))
                throw new IllegalArgumentException("No application id specified");
        }
    }

    private AppConfig getAppConfig(File deployZip,
                                         final String[] environments,
                                         final String[] implicitEnvironments) throws IOException {
        final AppConfig appConfig = new AppConfig();

        FileInputStream fin = new FileInputStream(deployZip);
        try {
            ZipHelper.unzipFile(fin, new ZipHelper.ZipEntryHandler() {
                public void unzip(ZipEntry entry, InputStream zis) throws IOException {
                    if (entry.getName().equals(
                            "META-INF/stax-application.xml") ||
                            entry.getName().equals("WEB-INF/stax-web.xml") ||
                            entry.getName().equals("WEB-INF/cloudbees-web.xml")) {
                        AppConfigHelper.load(appConfig, zis, null,
                                environments,
                                implicitEnvironments);
                    }
                }
            }, false);
        } finally {
            fin.close();
        }

        return appConfig;
    }

    private static void close(Closeable closeable) {
    try {
        if (closeable != null)
            closeable.close();
    } catch (IOException ignored) { }
    }

}
