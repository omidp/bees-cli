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
