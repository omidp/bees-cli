package com.cloudbees.sdk.commands.app;

import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;
import com.cloudbees.utils.ZipHelper;
import com.staxnet.ant.ApplicationHelper;
import com.staxnet.appserver.IAppServerConfiguration;
import com.staxnet.appserver.StaxSdkAppServer;
import com.staxnet.appserver.WarBasedServerConfiguration;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @Author: Fabian Donze
 */
@CLICommand("app:run")
public class ApplicationRun extends Command {

    /**
     * Configuration environments to use.
     */
    private String environment;

    /**
     * The path to the Bees deployment descriptor. (stax-application.xml)
     */
    private File appConfig;

    /**
     */
    private String tmpDir;

    private String port;

    /**
     */
    private String descriptorDir;

    /**
     * The war file
     */
    private File warFile;


    public ApplicationRun() {
        setArgumentExpected(1);
    }

    @Override
    protected String getUsageMessage() {
        return "WAR_Filename | WAR_directory";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption("p", "port", true, "server listen port (default: 8080)");
        addOption("e", "environment", true, "Environment configurations to run");
        addOption("t", "tmpDir", true, "Local working directory where temp files can be created (default: 'temp')");
        addOption("xd", "descriptorDir", true, "Directory containing application descriptors (default: 'conf')", true);

        return true;
    }

    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setWarFile(new File(getParameters().get(0)));
            return true;
        }
        return false;
    }

    @Override
    protected void initDefaults(Properties properties) {
        super.initDefaults(properties);
        setAppConfig(new File(getDescriptorDir(), "stax-application.xml"));
    }


    protected void setAppConfig(File appConfig) {
        this.appConfig = appConfig;
    }

    protected File getWebroot() {
        return new File(getTmpDir(), "webapp");
    }

    protected File getWorkDir() {
        return new File(getTmpDir(), "workdir");
    }

    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }

    protected void setWarFile(File warFile) {
        this.warFile = warFile;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setDescriptorDir(String descriptorDir) {
        this.descriptorDir = descriptorDir;
    }

    public String getTmpDir() {
        return tmpDir == null ? "tmp" : tmpDir;
    }

    public String getDescriptorDir() {
        return descriptorDir == null ? "conf" : descriptorDir;
    }

    public int getPort() {
        return port == null ? 8080 : Integer.parseInt(port);
    }

    public void setPort(String port) {
        this.port = port;
    }

    boolean cleanWebRoot = false;
    File webRoot = null;
    StaxSdkAppServer server;

    @Override
    protected boolean execute() throws Exception {

        // run the application in a local server
        // Unpack the war file
        //deleteAll(new File(getTmpDir()));

        if (warFile.exists() && !warFile.isDirectory()) {
            cleanWebRoot = true;
            webRoot = getWebroot();
            webRoot.mkdirs();

            ZipFile zipFile = new ZipFile(warFile.getAbsolutePath());
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                ZipHelper.unzipEntryToFolder(entry, zipFile.getInputStream(entry), getWebroot());
            }
            zipFile.close();

            Helper.deleteDirectoryOnExit(webRoot);
        } else {
            webRoot = warFile;
        }

        File staxWebXml = new File(webRoot, "WEB-INF/cloudbees-web.xml");
        if (!staxWebXml.exists())
            staxWebXml = new File(webRoot, "WEB-INF/stax-web.xml");

        IAppServerConfiguration config = WarBasedServerConfiguration.load(null, webRoot, staxWebXml, getEnvironments(appConfig));

        getWorkDir().mkdirs();
        server = new StaxSdkAppServer(new File(getTmpDir()).getAbsolutePath(),
                getWorkDir().getAbsolutePath(),
                getClass().getClassLoader(), new String[0],
                getPort(), config, null);

        server.start();

        return true;
    }

    private String[] getEnvironments(File staxappxmlFile)
    {
        String envString = environment;
        if(envString == null || envString.equals("") && staxappxmlFile != null && staxappxmlFile.exists())
        {
            //load the default environment, and append the run environment
            AppConfig appConfig = new AppConfig();
            AppConfigHelper.load(appConfig, staxappxmlFile.getAbsolutePath(), new String[0], new String[0]);

            envString = appConfig.getDefaultEnvironment();
        }

        String[] environment = ApplicationHelper.getEnvironmentList(envString, "run");
        return environment;
    }

    @Override
    public void stop() {
        if (server != null) server.stop();
        if (cleanWebRoot) Helper.deleteDirectory(webRoot);
    }
}
