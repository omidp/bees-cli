package com.cloudbees.sdk.utils;

import com.cloudbees.api.*;
import com.cloudbees.sdk.BeesSecurityException;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;
import com.staxnet.appserver.utils.ZipHelper;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;

/**
 * @Author: Fabian Donze
 */
public class Helper {
    public static int EMAIL_CREDENTIALS = 0;
    public static int KEYS_CREDENTIALS = 1;

    public static String promptForAppId() throws IOException {
        return promptFor("Enter application ID (ex: account/appname) : ", true);
    }

    public static String promptFor(String message, boolean cannotBeNull) throws IOException {
        String input = promptFor(message);
        if (cannotBeNull && (input == null || input.trim().length() == 0))
            return promptFor(message, cannotBeNull);
        return input;
    }

    public static boolean promptMatches(String message, String successPattern) throws IOException {
        String input = promptFor(message);
        if (input == null || input.trim().length() == 0)
            return promptMatches(message, successPattern);
        return input.matches(successPattern);
    }

    public static String promptFor(String message) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        return inputReader.readLine();
    }

    public static Properties initConfigProperties(File localRepoDir, boolean force, int credentialType, Map<String, String> paramaters, boolean verbose) {
        Properties properties = new Properties();
        File userConfigFile = new File(localRepoDir, "bees.config");
        if (force || !loadProperties(userConfigFile, properties)) {
            System.out.println();
            System.out.println("You have not created a CloudBees configuration profile, let's create one now...");

            try {
                String server = paramaters.get("server");
                if (server == null)
                    server = "https://api.cloudbees.com/api";
                properties.setProperty("bees.api.url", server);
                String key = paramaters.get("key");
                String secret = paramaters.get("secret");
                String domain = paramaters.get("domain");
                if (key == null || secret == null) {
                    if (credentialType == KEYS_CREDENTIALS) {
                        System.out.println("Go to https://grandcentral.cloudbees.com/user/keys to retrieve your API key");
                        System.out.println();
                    } else if (credentialType == EMAIL_CREDENTIALS) {
                        String email = paramaters.get("email");
                        if (email == null)
                            email = Helper.promptFor("Enter your CloudBees account email address: ", true);
                        String password = paramaters.get("password");
                        if (password == null) {
                            password = PasswordHelper.prompt("Enter your CloudBees account password: ");
                        }

                        // Get the API key & secret
                        BeesClientConfiguration beesClientConfiguration = new BeesClientConfiguration(server, "1", "0", "xml", "1.0");
                        // Set proxy information
                        beesClientConfiguration.setProxyHost(paramaters.get("proxy.host"));
                        if (paramaters.get("proxy.port") != null)
                            beesClientConfiguration.setProxyPort(Integer.parseInt(paramaters.get("proxy.port")));
                        beesClientConfiguration.setProxyUser(paramaters.get("proxy.user"));
                        beesClientConfiguration.setProxyPassword(paramaters.get("proxy.password"));

                        StaxClient staxClient = new StaxClient(beesClientConfiguration);
                        staxClient.setVerbose(verbose);
                        AccountKeysResponse response = staxClient.accountKeys(domain, email, password);
                        key = response.getKey();
                        secret = response.getSecret();

                        // Get the default account name
                        beesClientConfiguration.setApiKey(key);
                        beesClientConfiguration.setSecret(secret);
                        staxClient = new StaxClient(beesClientConfiguration);
                        staxClient.setVerbose(verbose);
                        AccountListResponse listResponse = staxClient.accountList();
                        List<AccountInfo> accounts = listResponse.getAccounts();
                        if (accounts.size() == 1) {
                            domain = accounts.get(0).getName();
                        } else {
                            String accountsString = null;
                            for (AccountInfo info: accounts) {
                                if (accountsString == null)
                                    accountsString = info.getName();
                                else
                                    accountsString += "," + info.getName();
                            }
                            System.out.println("You have several accounts: " + accountsString);
                            domain = Helper.promptFor("Enter your default CloudBees account name : ", true);
                        }
                    }
                }

                if (key == null) key = Helper.promptFor("Enter your CloudBees API key: ", true);
                if (secret == null) secret = Helper.promptFor("Enter your CloudBees secret: ", true);
                if (domain == null) domain = Helper.promptFor("Enter your default CloudBees account name: ", true);

                properties.setProperty("bees.api.key", key);
                properties.setProperty("bees.api.secret", secret);
                properties.setProperty("bees.project.app.domain", domain);
                if (paramaters.get("proxy.host") != null)
                    properties.setProperty("bees.api.proxy.host", paramaters.get("proxy.host"));
                if (paramaters.get("proxy.port") != null)
                    properties.setProperty("bees.api.proxy.port", paramaters.get("proxy.port"));
                if (paramaters.get("proxy.user") != null)
                    properties.setProperty("bees.api.proxy.user", paramaters.get("proxy.user"));
                if (paramaters.get("proxy.password") != null)
                    properties.setProperty("bees.api.proxy.password", paramaters.get("proxy.password"));

                if (!userConfigFile.getParentFile().exists())
                    userConfigFile.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(userConfigFile);
                properties.store(fos, "CloudBees SDK config");
                fos.close();

            } catch (BeesClientException e) {
                String errCode = e.getError().getErrorCode();
                if (errCode != null && errCode.equals("AuthFailure"))
                    throw new BeesSecurityException("Authentication failure, please check credentials!", e);
                else
                    throw new RuntimeException(e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create configuration", e);
            }
        }
        return properties;
    }

    public static String getArchiveApplicationId() {
        String appid = null;
        File deployFile = new File("build/webapp.war");
        if (!deployFile.exists()) {
            File dir = new File("target");
            String[] files = getFiles(dir, ".war");
            if (files != null && files.length == 1) {
                deployFile = new File(dir, files[0]);
            }
        }
        if (deployFile.exists()) {
            AppConfig appConfig = null;
            try {
                appConfig = getAppConfig(deployFile, new String[0], new String[] { "deploy" });
            } catch (IOException e) {
                System.err.println("WARNING: " + e.getMessage());
            }
            appid = appConfig.getApplicationId();
        }
        return appid;
    }
    public static boolean loadProperties(File propertyFile, Properties properties) {
        if (propertyFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(propertyFile);
                properties.load(fis);
                fis.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return false;
    }

    public static void deleteDirectory(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory())
                            deleteDirectory(f);
                        else
                            f.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    public static void deleteDirectoryOnExit(File dir) {
        if (dir.exists()) {
            dir.deleteOnExit();
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory())
                            deleteDirectoryOnExit(f);
                        else
                            f.deleteOnExit();
                    }
                }
            }
        }
    }

    public static void copyFile(File from, File to) throws IOException {
        byte[] buf = new byte[1024];
        FileInputStream in = new FileInputStream(from);
        try {
            FileOutputStream out = new FileOutputStream(to);
            try {
                int numRead = in.read(buf);
                while (numRead != -1) {
                    out.write(buf, 0, numRead);
                    numRead = in.read(buf);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static void downloadFile(String url, String fileName) throws IOException {
        byte[] buf = new byte[1024];
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream in = new URL(url).openStream();
        try {
            int numRead = in.read(buf);
            while (numRead != -1) {
                bos.write(buf, 0, numRead);
                numRead = in.read(buf);
            }
        } finally {
            bos.close();
        }
    }

    public static String[] getFiles(File dir, final String extension) {
        return dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(extension));
            }
        });
    }

    public static AppConfig getAppConfig(File deployZip, final String[] environments,
            final String[] implicitEnvironments) throws IOException {
        final AppConfig appConfig = new AppConfig();

        FileInputStream fin = new FileInputStream(deployZip);
        try {
            ZipHelper.unzipFile(fin, new ZipHelper.ZipEntryHandler() {
                public void unzip(ZipEntry entry, InputStream zis)
                        throws IOException {
                    if (entry.getName().equals("META-INF/stax-application.xml")
                            || entry.getName().equals("WEB-INF/stax-web.xml")
                            || entry.getName().equals("WEB-INF/cloudbees-web.xml")) {
                        AppConfigHelper.load(appConfig, zis, null, environments, implicitEnvironments);
                    }
                }
            }, false);
        } finally {
            fin.close();
        }

        return appConfig;
    }

    public static String getPaddedString(String str, int length) {
        StringBuffer sb = new StringBuffer(str);
        int size = sb.length();
        if (size < length) {
            for (int i=0; i<length-size; i++)
                sb.append(" ");
        }
        return sb.toString();
    }
}
