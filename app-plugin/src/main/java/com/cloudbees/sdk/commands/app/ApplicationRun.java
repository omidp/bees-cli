package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationResourceListResponse;
import com.cloudbees.api.ParameterSettingsInfo;
import com.cloudbees.api.ResourceSettingsInfo;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;
import com.cloudbees.utils.ZipHelper;
import com.staxnet.appserver.IAppServerConfiguration;
import com.staxnet.appserver.StaxSdkAppServer;
import com.staxnet.appserver.WarBasedServerConfiguration;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;
import com.staxnet.appserver.config.ResourceConfig;
import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Fabian Donze
 */
@CommandGroup("Application")
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

    private Boolean noResourceFetch;
    private String appid;
    private String account;

    public ApplicationRun() {
        setArgumentExpected(1);
    }

    protected boolean fetchResources() {
        return noResourceFetch == null || !noResourceFetch;
    }

    public void setNoResourceFetch(Boolean noResourceFetch) {
        this.noResourceFetch = noResourceFetch;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid;
    }

    private String getDefaultDomain() {
        return getConfigProperties().getProperty("bees.project.app.domain");
    }

    public String getAccount() throws IOException {
        if (account == null) account = getDefaultDomain();
        if (account == null) account = Helper.promptFor("Account name: ", true);
        return account;
    }

    @Override
    protected String getUsageMessage() {
        return "WAR_Filename | WAR_directory";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption("a", "appid", true, "Resources application ID");
        addOption(null, "port", true, "server listen port (default: 8080)");
        addOption("e", "environment", true, "Environment configurations to run");
        addOption("t", "tmpDir", true, "Local working directory where temp files can be created (default: 'temp')");
        addOption("xd", "descriptorDir", true, "Directory containing application descriptors (default: 'conf')", true);
        addOption(null, "noResourceFetch", false, "do not fetch application resources");

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
    File appserverXML;

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

            // Delete on exit
            Helper.deleteDirectoryOnExit(webRoot);
        } else {
            webRoot = warFile;
        }

        File staxWebXml = new File(webRoot, "WEB-INF/cloudbees-web.xml");
        if (!staxWebXml.exists())
            staxWebXml = new File(webRoot, "WEB-INF/stax-web.xml");

        StaxClient client = getStaxClient(StaxClient.class);

        String[] environments = getEnvironments(appConfig, environment);

        appserverXML = new File("appserver.xml");
        if (!appserverXML.exists()) {
            appserverXML = new File(webRoot, "WEB-INF/cloudbees-appserver.xml");
            if (appserverXML.exists()) appserverXML.delete();
        }

        // Create the appserver.xml
        if (fetchResources() && appid != null) {
            System.out.println("Get application resources...");
            ApplicationResourceListResponse res = null;
            try {
                res = client.applicationResourceList(getAppId(null, null), null, null, environment);

                if (res.getResources() != null && res.getResources().size() > 0) {
                    // Generate appserver.xml file
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(appserverXML);
                        XStream xstream = new XStream();
                        xstream.processAnnotations(ResourceSettingsInfo.class);
                        xstream.processAnnotations(ParameterSettingsInfo.class);
                        xstream.alias("appserver", ApplicationResourceListResponse.class);
                        xstream.addImplicitCollection(ApplicationResourceListResponse.class, "resources");
                        xstream.toXML(res, fos);
                    } finally {
                        if (fos != null)
                            fos.close();
                    }
                }
            } catch (Exception e) {
                System.err.println("WARNING: Cannot retrieving application resources: " + e.getMessage());
            }
        }

        IAppServerConfiguration config = WarBasedServerConfiguration.load(appserverXML, webRoot, staxWebXml, environments);

        if (staxWebXml.exists()) {
            // Resolve variables
            String appxml = readFile(staxWebXml);
            for (Map.Entry<String, String> entry: getSystemProperties(config).entrySet()) {
                appxml = appxml.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
            }
            saveFile(staxWebXml, appxml);

            if (appserverXML.exists()) {
                appxml = readFile(appserverXML);
                for (Map.Entry<String, String> entry: getSystemProperties(config).entrySet()) {
                    appxml = appxml.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
                }
                saveFile(appserverXML, appxml);
            }

            config = WarBasedServerConfiguration.load(appserverXML, webRoot, staxWebXml, environments);
        }

        getWorkDir().mkdirs();
        server = new StaxSdkAppServer(new File(getTmpDir()).getAbsolutePath(),
                getWorkDir().getAbsolutePath(),
                getClass().getClassLoader(), new String[0],
                getPort(), config, null);

        server.start();

        return true;
    }

    private String[] getEnvironments(File staxappxmlFile, String envString)
    {
        if(envString == null || envString.equals("") && staxappxmlFile != null && staxappxmlFile.exists())
        {
            //load the default environment, and append the run environment
            AppConfig appConfig = new AppConfig();
            AppConfigHelper.load(appConfig, staxappxmlFile.getAbsolutePath(), new String[0], new String[0]);

            envString = appConfig.getDefaultEnvironment();
        }

        String[] environment = Helper.getEnvironmentList(envString, "run");
        return environment;
    }

    protected String getAppId(File configFile, String[] environments) throws IOException
    {
        if ((appid == null || appid.equals("")) && configFile != null && configFile.exists()) {
            FileInputStream fis = new FileInputStream(configFile);
            AppConfig appConfig = new AppConfig();
            AppConfigHelper.load(appConfig, fis, null, environments, new String[] { "deploy" });
            appid = appConfig.getApplicationId();
            fis.close();
        }

        if (appid == null || appid.equals(""))
            appid = Helper.promptForAppId();

        if (appid == null || appid.equals(""))
            throw new IllegalArgumentException("No application id specified");

        String[] parts = appid.split("/");
        if (parts.length < 2)
            appid = getAccount() + "/" + appid;

        return appid;
    }

    @Override
    public void stop() {
        if (server != null) server.stop();
        if (cleanWebRoot) Helper.deleteDirectory(webRoot);
        if (appserverXML.exists())
            appserverXML.delete();
    }

    private String readFile(File file) throws IOException {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } finally {
            if (fr != null) fr.close();
        }
    }

    private void saveFile(File staxWebXml, String appxml) throws IOException {
        FileWriter fos = null;
        try {
            fos = new FileWriter(staxWebXml);
            fos.write(appxml);
        } finally {
            if (fos != null)
                fos.close();
        }
    }

    private Map<String, String> getSystemProperties(IAppServerConfiguration config) {
        Map<String, String> systemProperties = new HashMap<String, String>();

        for (ResourceConfig rs : config.getServerResources()) {
            String type = rs.getType();
            if (type == null || type.equalsIgnoreCase("system-property")) {
                systemProperties.put(rs.getName(), rs.getValue());
            }
        }
        for (ResourceConfig rs : config.getAppConfiguration().getResources()) {
            String type = rs.getType();
            if (type == null || type.equalsIgnoreCase("system-property")) {
                systemProperties.put(rs.getName(), rs.getValue());
            }
        }
        return systemProperties;
    }
}
