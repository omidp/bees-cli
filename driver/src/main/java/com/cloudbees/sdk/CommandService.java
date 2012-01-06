package com.cloudbees.sdk;

import com.cloudbees.sdk.annotations.CLICommandImpl;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.thoughtworks.xstream.XStream;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.filter.ScopeDependencyFilter;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
public class CommandService {
    static final String NL = System.getProperty("line.separator");

    List<CommandProperties> commandProperties;
    File localRepository;
    File sdkRepository;
    String fileExtension;
    boolean localRepoLoaded;

    @Inject
    private Injector injector;

    @Inject
    private RepositorySystem rs;

    @Inject
    public CommandService(DirectoryStructure structure) {
        this.localRepository = structure.localRepository;
        this.sdkRepository = structure.sdkRepository;
        this.fileExtension = ".bees";
        localRepoLoaded = false;
        loadCommandProperties();
    }

    public void loadCommandProperties() {
        commandProperties = loadCommandFiles(sdkRepository, fileExtension);
    }

    private ArrayList<CommandProperties> loadCommandFiles(File dir, String fileExtension) {
        ArrayList<CommandProperties> commandProperties = new ArrayList<CommandProperties>();

        String[] files = Helper.getFiles(dir, fileExtension);
        if (files != null) {
            for (String file: files) {
                commandProperties.addAll(loadCommands(new File(dir, file)));
            }
        }

        return  commandProperties;
    }

    private ArrayList<CommandProperties> loadCommands(File commandFile) {
        ArrayList<CommandProperties> commandProperties = new ArrayList<CommandProperties>();

        FileReader reader = null;
        try {
            reader = new FileReader(commandFile);
            Commands commands = (Commands) createXStream().fromXML(reader);
            commandProperties.addAll(commands.getProperties());
        }
        catch (IOException ex){
            System.err.println("ERROR: Cannot find file: " + commandFile);
        }
        finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {}
        }

        return commandProperties;
    }

    public ICommand getCommand(String name) {
        CommandProperties commandProp = getCommandProperties(name, commandProperties);

        // Look for additional command definition in the local repository
        if (commandProp == null) {
            if (!localRepoLoaded) {
                List<CommandProperties> localRepoCmds = loadCommandFiles(localRepository, fileExtension);
                localRepoLoaded = true;
                commandProperties.addAll(localRepoCmds);
                commandProp = getCommandProperties(name, localRepoCmds);
            }
        }
        Command command;

        if (commandProp != null) {
            List<String> parameters = getCommandParameters(commandProp.getClassName());
            String cmdClassName = parameters.get(0);
            if (parameters.size() > 1) {
                String[] params = new String[parameters.size()-1];
                for (int i=1; i<parameters.size(); i++) {
                    params[i-1] = parameters.get(i);
                }
                command = getCommand(cmdClassName, params);
            } else {
                command = getCommand(cmdClassName, null);
            }
            command.setCommandProperties(commandProp);
            return command;
        }

        try {
            return performModernLookup(name);
        } catch (Exception e) {
            // not sure what the exception handling policy in this CLI. Hiding it under the rug for now
            throw new RuntimeException(e);
        }
    }

    /**
     * Look up a command from Guice, if necessary by downloading it.
     *
     * TODO: eventually this should be the getCommand() implementation
     */
    private ICommand performModernLookup(String name) throws Exception {
        Injector injector = this.injector;

        String[] tokens = name.split(":");
        if (tokens.length>1) {
            // commands that are not built-in
            Artifact a = mapCommandToArtifact(tokens[0]);
            DependencyResult r = resolveDependencies(a);
    
            URLClassLoader cl = createClassLoader(r.getRoot());
            injector = createChildModule(injector,cl);
        }
        Provider<ICommand> p;
        try {
            p = injector.getProvider(Key.get(ICommand.class, new CLICommandImpl(name)));
        } catch (ConfigurationException e) {
            return null; // failed to find the command
        }
        return p.get();
    }

    private Injector createChildModule(Injector parent, final URLClassLoader cl) throws InstantiationException {
        final List<Module> childModules = new ArrayList<Module>();
        for (IndexItem<CLIModule,Module> m : Index.load(CLIModule.class, Module.class, cl)) {
            childModules.add(m.instance());
        }
        childModules.add(new CLICommandModule(cl));

        return parent.createChildInjector(childModules);
    }

    /**
     * Each sub-command maps to an artifact.
     */
    protected Artifact mapCommandToArtifact(String prefix) {
        // TODO: figure out why 'LATEST' isn't working
        // TODO: do this properly later by reading a table and
        if (prefix.equals("app"))
            return new DefaultArtifact("com.cloudbees.sdk", "sdk-plugins", "jar", "0.8-SNAPSHOT");

        return new DefaultArtifact("org.cloudbees.sdk.plugins", prefix + "-plugin", "jar", "1.0-SNAPSHOT");
    }

    private DependencyResult resolveDependencies(Artifact a) throws DependencyCollectionException, DependencyResolutionException {
        MavenRepositorySystemSession session = createSession(rs);

        Dependency dependency = new Dependency(a, "compile");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        configureRepositories(collectRequest);
        DependencyNode node = rs.collectDependencies(session, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest(node,new ScopeDependencyFilter("provided"));

        return rs.resolveDependencies(session, dependencyRequest);
    }

    private MavenRepositorySystemSession createSession(RepositorySystem rs) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        LocalRepository localRepo = new LocalRepository( new File(new File(System.getProperty("user.home")),".m2/repository"));
        session.setLocalRepositoryManager(rs.newLocalRepositoryManager(localRepo));
        return session;
    }

    /**
     * Creates a {@link ClassLoader} that loads all the resolved artifacts.
     */
    private URLClassLoader createClassLoader(DependencyNode root) throws MalformedURLException {
        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        root.accept(nlg);

        List<URL> urls = new ArrayList<URL>();
        for (File jar : nlg.getFiles()) {
            urls.add(jar.toURI().toURL());
        }
        LOGGER.fine("Resolved: "+urls);
        return new URLClassLoader(urls.toArray(new URL[urls.size()]),getClass().getClassLoader());
    }

    private void configureRepositories(CollectRequest req) {
        // TODO: we want to hit our repository, too
        req.addRepository(new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/"));
    }

    public int getCount() {
        return commandProperties.size();
    }

    public String getHelp(URL helpTitleFile, String groupHelp, boolean all) {
        StringBuffer sb = new StringBuffer(getHelpTitle(helpTitleFile));
        Map<String, List<CommandProperties>> map = new LinkedHashMap<String, List<CommandProperties>>();
        if (!localRepoLoaded) commandProperties.addAll(loadCommandFiles(localRepository, fileExtension));
        for (CommandProperties cmd: commandProperties) {
            if (cmd.getGroup() != null && (!cmd.isExperimental() || all)) {
                List<CommandProperties> list = map.get(cmd.getGroup());
                if (list == null) {
                    list = new ArrayList<CommandProperties>();
                    map.put(cmd.getGroup(), list);
                }
                list.add(cmd);
            }
        }

        for (String group: map.keySet()) {
            sb.append(NL).append(group).append(" ").append(groupHelp).append(NL);
            for (CommandProperties cmd: map.get(group)) {
                sb.append("    ").append(cmd.getName());
                if (cmd.getDescription() != null)
                    sb.append("      ").append(cmd.getDescription()).append(NL);
                else
                    sb.append(NL);
            }
        }
        return sb.toString();
    }

    private StringBuffer getHelpTitle(URL helpTitleFile) {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader =  new BufferedReader(new InputStreamReader(helpTitleFile.openStream()));
            String line;
            while (( line = reader.readLine()) != null){
                sb.append(line).append(NL);
            }
        }
        catch (IOException ex){
            System.err.println("ERROR: Cannot find help file: " + helpTitleFile);
        }
        finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {}
        }
        return sb;
    }

    private CommandProperties getCommandProperties(String commandName, List<CommandProperties> commandProperties) {
        for (CommandProperties cmd: commandProperties) {
            if (commandName.matches(cmd.getPattern()))
                return cmd;
        }

        return null;
    }

    private List<String> getCommandParameters(String str) {
        List<String> parameters = new ArrayList<String>();
        int i1 = str.indexOf('(');
        int i2 = str.indexOf(')');
        if (i1 > -1 && i2 > -1) {
            parameters.add(str.substring(0, i1));
            String p = str.substring(i1+1, i2);
            String[] ps = p.split(",");
            for (String p1 : ps) {
                parameters.add(p1.trim());
            }
        } else {
            parameters.add(str);
        }
        return parameters;
    }

    private Command getCommand(String className, String[] parameters) {
        Command command = null;
        try {
            Class cl = Class.forName(className);
            if (parameters != null) {
                Class[] types = new Class[parameters.length];
                for (int i=0; i<parameters.length; i++) {
                    types[i] = String.class;
                }
                Constructor constructor = cl.getConstructor(types);
                command = (Command) constructor.newInstance((Object[]) parameters);
            } else {
                Constructor constructor = cl.getConstructor((Class<?>[]) null);
                command = (Command) constructor.newInstance();
            }
        } catch (Exception e) {
            System.err.println("cannot initialize command: " + className);
            e.printStackTrace();
        }
        return command;
    }

    private static XStream createXStream() {
        XStream xstream = new XStream();
        xstream.processAnnotations(Commands.class);
        xstream.processAnnotations(CommandProperties.class);
        return xstream;
    }

    private static final Logger LOGGER = Logger.getLogger(CommandService.class.getName());
}
