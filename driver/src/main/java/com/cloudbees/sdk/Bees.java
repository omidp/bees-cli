package com.cloudbees.sdk;

import com.cloudbees.api.BeesClientException;
import com.cloudbees.sdk.commands.AntTargetCommandsModule;
import com.cloudbees.sdk.extensibility.AnnotationLiteral;
import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.cloudbees.sdk.utils.Helper;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.staxnet.appserver.utils.XmlHelper;
import com.staxnet.repository.LocalRepository;
import hudson.util.VersionNumber;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.IOUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.impl.VersionResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Entry point to the Bees CLI.
 *
 * @Author: Fabian Donze
 * @author Kohsuke Kawaguchi
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
    CommandService commandService;

    /**
     * Entry point to all the components.
     */
    private final Injector injector;

    /**
     * {@link ClassLoader} that includes all the bees CLI components + extensions.
     * 
     * @see ExtensionClassLoader
     */
    private final ClassLoader extLoader;

    public Bees() throws PlexusContainerException, ComponentLookupException, IOException {
        initialize(false);

        // first, we create a small IoC container for the sole purpose of loading extensions
        DefaultPlexusContainer boot = new DefaultPlexusContainer(
            new DefaultContainerConfiguration(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(VersionResolver.class).to(VersionResolverImpl.class);
                    bind(MavenRepositorySystemSession.class).toProvider(RepositorySessionProvider.class);
                }
            }
        );
        Injector injector = boot.lookup(Injector.class);
        ArtifactClassLoaderFactory f = injector.getInstance(ArtifactClassLoaderFactory.class);

        // and we load all the extensions
        for (GAV gav : injector.getInstance(InstalledExtensionList.class).values()) {
            try {
                f.add(gav);
            } catch (RepositoryException e) {
                throw (IOException)new IOException("Failed to resolve extension: "+gav).initCause(e);
            }
        }

        extLoader = f.createClassLoader(getClass().getClassLoader());


        // then we use that to build a bigger container that includes all the things that make a bees CLI.
        injector = injector.createChildInjector(
            new ExtensionFinder(extLoader),
            new AntTargetCommandsModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(ClassLoader.class).annotatedWith(AnnotationLiteral.of(ExtensionClassLoader.class)).toInstance(extLoader);
                    alias("getsource", "app:getsource");
                }

                private void alias(String from, final String to) {
                    bind(ICommand.class).annotatedWith(AnnotationLiteral.of(CLICommand.class,from))
                        .toProvider(new Provider<ICommand>() {
                            public ICommand get() {
                                return commandService.getCommand(to);
                            }
                        });
                }
            }
        );

        this.injector = injector;
        this.injector.injectMembers(this);
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

    private void initialize(boolean force) throws IOException {
        LocalRepository localRepository = new LocalRepository();
        
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
            File sdkConfig = getURLAsFile(localRepository,app_template_xml_url + app_template_xml_name,
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
                    throw new AbortException("Error - This version of the CloudBees SDK is no longer supported," + "" +
                            " please install the latest version (" + availVersion + ").");
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
                        getURLAsFile(localRepository, libUrlString, "lib" + libFileName, libName);
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
    
    private File getURLAsFile(LocalRepository localRepository,String urlStr, String localCachePath, String description) throws IOException {
        try {
            return localRepository.getURLAsFile(urlStr,localCachePath,description);
        } catch (Exception e) {
            throw (IOException)new IOException("Failed to retrieve "+urlStr).initCause(e);
        }
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

    private static final Logger LOGGER = Logger.getLogger(Bees.class.getName());
}
