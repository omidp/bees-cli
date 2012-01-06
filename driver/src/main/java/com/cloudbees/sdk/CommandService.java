package com.cloudbees.sdk;

import com.cloudbees.sdk.annotations.CLICommandImpl;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
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
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
@Singleton
public class CommandService {
    static final String NL = System.getProperty("line.separator");

    DirectoryStructure structure;

    @Inject
    private Injector injector;

    @Inject
    private RepositorySystem rs;

    @Inject
    private InstalledPluginList installedPluginList;

    @Inject
    public CommandService(DirectoryStructure structure) {
        this.structure = structure;
    }
    

    public ICommand getCommand(String name) {
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
            injector = createChildModule(injector, cl);
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

    public String getHelp(URL helpTitleFile, String groupHelp, boolean all) {
        StringBuilder sb = new StringBuilder(getHelpTitle(helpTitleFile));

        Map<String,List<Binding<?>>> map = new HashMap<String, List<Binding<?>>>();
        
        for (Binding<?> b : injector.getAllBindings().values()) {
            if (ICommand.class==b.getKey().getTypeLiteral().getRawType()) {
                Class<?> cmd = b.getProvider().get().getClass();
                if (!cmd.isAnnotationPresent(CLICommand.class))
                    continue;
                CommandGroup group = cmd.getAnnotation(CommandGroup.class);

                if (cmd.isAnnotationPresent(Experimental.class) && !all)
                    continue;

                String key = group == null ? "" : group.value();
                List<Binding<?>> list = map.get(key);
                if (list == null) {
                    list = new ArrayList<Binding<?>>();
                    map.put(key, list);
                }
                list.add(b);
            }
        }
        
        for (String group: map.keySet()) {
            sb.append(NL).append(group).append(" ").append(groupHelp).append(NL);
            for (Binding<?> b : map.get(group)) {
                Class<?> cmd = b.getProvider().get().getClass();
                sb.append("    ").append(cmd.getAnnotation(CLICommand.class).value());
                CommandDescription description = cmd.getAnnotation(CommandDescription.class);
                if (description != null)
                    sb.append("      ").append(description.value()).append(NL);
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

    private static final Logger LOGGER = Logger.getLogger(CommandService.class.getName());
}
