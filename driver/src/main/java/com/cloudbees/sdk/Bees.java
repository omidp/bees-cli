package com.cloudbees.sdk;

import com.cloudbees.api.BeesClientException;
import com.cloudbees.sdk.annotations.CLICommandImpl;
import com.cloudbees.sdk.commands.AntTargetCommandsModule;
import com.cloudbees.sdk.utils.Helper;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.staxnet.appserver.utils.XmlHelper;
import com.staxnet.repository.LocalRepository;
import hudson.util.VersionNumber;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.VersionResolver;
import org.w3c.dom.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * @Author: Fabian Donze
 */
public class Bees {
    /**
     * Version of the bees CLI.
     */
    public static VersionNumber version = loadVersion();

    private final static String app_template_xml_url = "http://cloudbees-downloads.s3.amazonaws.com/";
    private final static String app_template_xml_name = "sdk/cloudbees-sdk-config-2.xml";
    private final static String app_template_xml_desc = "CloudBees SDK configuration";

    private static final long CHECK_INTERVAL = 1000 * 60 * 60 * 12;  // 12 hours

    @Inject
    private CommandService commandService;

    @Inject
    RepositorySystem rs;


    /**
     * Entry point to all the components.
     */
    private final Injector injector;

    public Bees() throws PlexusContainerException, ComponentLookupException {
        if (!initialize(false)) {
            throw new RuntimeException("");
        }
        
        DefaultPlexusContainer plexus = new DefaultPlexusContainer(
            new DefaultContainerConfiguration(),
            new CLICommandModule(getClass().getClassLoader()),
            new AntTargetCommandsModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    alias("getsource", "app:getsource");
                    bind(VersionResolver.class).to(VersionResolverImpl.class);
                }

                private void alias(String from, final String to) {
                    bind(ICommand.class).annotatedWith(new CLICommandImpl(from))
                        .toProvider(new Provider<ICommand>() {
                            public ICommand get() {
                                return commandService.getCommand(to);
                            }
                        });
                }
            }
        );

        injector = plexus.lookup(Injector.class);
        injector.injectMembers(this);
    }

    public int run(String[] args) throws Exception {
        // if no sub-command is given, assume help
        if (args.length==0) args = new String[]{"help"};
        
        ICommand command = commandService.getCommand(args[0]);
        if (command==null) {
            // no such command. print help
            System.err.println("No such command: "+args[0]);
            command = commandService.getCommand("help");
        }

        int r = command.run(Arrays.asList(args));
        if (r == 99) {
            initialize(true);
        }
        return r;
    }

    private boolean initialize(boolean force) {
        boolean ok = true;
        LocalRepository localRepository = new LocalRepository();
        try {
            String beesRepoPath = localRepository.getRepositoryPath();
            File lastCheckFile = new File(beesRepoPath, "sdk/check.dat");
            boolean checkVersion = true;
            Properties p = new Properties();
            if (!force && Helper.loadProperties(lastCheckFile, p)) {
                String str = p.getProperty("last");
                if (str != null) {
                    long interval = System.currentTimeMillis() - Long.parseLong(str);
                    if (interval < CHECK_INTERVAL)
                        checkVersion = false;
                }
            }

            if (checkVersion) {
                // Check SDK version
                File sdkConfig = localRepository.getURLAsFile(app_template_xml_url + app_template_xml_name,
                        app_template_xml_name, app_template_xml_desc);
                Document doc = XmlHelper.readXMLFromFile(sdkConfig.getCanonicalPath());
                Element e = doc.getDocumentElement();
                String availVersion = e.getAttribute("version");
                String minVersion = e.getAttribute("min-version");

                VersionNumber currentVersion = version;
                VersionNumber availableVersion = new VersionNumber(availVersion);
                VersionNumber minimunVersion = new VersionNumber(minVersion);

                if (currentVersion.compareTo(availableVersion) < 0) {
                    System.out.println();
                    if (currentVersion.compareTo(minimunVersion) < 0) {
                        System.err.println("Error - This version of the CloudBees SDK is no longer supported," + "" +
                                " please install the latest version (" + availVersion + ").");
                        ok = false;
                    } else if (currentVersion.compareTo(availableVersion) < 0) {
                        System.out.println("A new version of the CloudBees SDK is available, please install the latest version (" + availVersion + ").");
                    }

                    String hRef = e.getAttribute("href");

                    String homeRef = "www.cloudbees.com";
                    NodeList nodeList = e.getElementsByTagName("link");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        NamedNodeMap nodeMap = node.getAttributes();
                        Node rel = nodeMap.getNamedItem("rel");
                        Node href = nodeMap.getNamedItem("href");
                        if (rel != null && rel.getTextContent().trim().equalsIgnoreCase("alternate") && href != null) {
                            homeRef = href.getTextContent();
                        }
                    }
                    System.out.println("  SDK home:     " + homeRef);
                    System.out.println("  SDK download: " + hRef);
                    System.out.println();
                }

                if (ok) {
                    // Get libraries
                    NodeList libsNL = e.getElementsByTagName("libraries");
                    Node libsNode = null;
                    if (libsNL.getLength() > 0) {
                        libsNode = libsNL.item(0);
                    }
                    if (libsNode != null) {
                        NodeList libNL = e.getElementsByTagName("library");
                        for (int i = 0; i < libNL.getLength(); i++) {
                            Node node = libNL.item(i);
                            NamedNodeMap nodeMap = node.getAttributes();
                            Node nameNode = nodeMap.getNamedItem("name");
                            Node refNode = nodeMap.getNamedItem("href");
                            if (nameNode != null && refNode != null) {
                                String libName = nameNode.getTextContent();
                                String libUrlString = refNode.getTextContent().trim();
                                int idx = libUrlString.lastIndexOf('/');
                                String libFileName = libUrlString.substring(idx);
                                localRepository.getURLAsFile(libUrlString, "lib" + libFileName, libName);
                            }
                        }
                    }

                    // Update last check
                    p.setProperty("last", ""+System.currentTimeMillis());
                    lastCheckFile.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(lastCheckFile);
                    p.store(fos, "CloudBees SDK check");
                    fos.close();
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Cannot retrieve SDK version info: " + e.getMessage());
        }
        return ok;
    }

    private static float getVersion(String version)
    {
        String[] numbers = version.split("\\.");
        return Float.parseFloat(numbers[0]) + Float.parseFloat(numbers[1])/100 + Float.parseFloat(numbers[2])/10000;
    }

    public static void main(String[] args)  throws Exception {
        System.out.println("# CloudBees SDK version: " + version);
        try {
            System.exit(new Bees().run(args));
        } catch (BeesClientException e) {
            System.err.println();
            String errCode = e.getError().getErrorCode();
            if (errCode != null && errCode.equals("AuthFailure"))
                System.err.println("ERROR: Authentication failure, please check credentials!");
            else
                System.err.println("ERROR: " +  e.getMessage());
//            e.printStackTrace();
            System.exit(2);
        } catch (UnrecognizedOptionException e) {
            System.err.println();
            System.err.println("ERROR: " + e.getMessage());
            System.exit(2);
        } catch (IllegalArgumentException e) {
            System.err.println();
            System.err.println("ERROR: " + e.getMessage());
            System.exit(2);
        } catch (BeesSecurityException e) {
            System.err.println();
            System.err.println("ERROR: " + e.getMessage());
            System.exit(2);
        }
    }

    private static boolean isVerbose(String[] args) {
        for (String arg: args) {
            if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("--verbose"))
                return true;
        }
        return false;
    }

    /**
     * Parses the version number of SDK from the resource file that Maven produces.
     *
     * To support running this from IDE and elsewhere, work gracefully if the version
     * is not available or not filtered.
     */
    private static VersionNumber loadVersion() {
        Properties props = new Properties();
        InputStream in = Bees.class.getResourceAsStream("version.properties");
        if (in!=null) {
            try {
                props.load(in);
            } catch (IOException e) {
                throw new Error(e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        Object v = props.get("version");
        if (v!=null)
            try {
                return new VersionNumber(v.toString());
            } catch (Exception e) {
                // fall through
            }

        return new VersionNumber("0");
    }
}
