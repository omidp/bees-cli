package com.cloudbees.sdk;

import com.cloudbees.sdk.extensibility.AnnotationLiteral;
import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
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
import java.util.Arrays;
import java.util.Collection;
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
    private Verbose verbose;

    @Inject
    Provider<MavenRepositorySystemSession> sessionFactory;
    
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
            GAV gav = installedPluginList.get(tokens[0]);
            if (gav==null) {
                for (GAV candidate : mapCommandToArtifacts(tokens[0])) {
                    try {
                        resolveDependencies(toArtifact(candidate));
                        gav = candidate;
                        break;  // found it!
                    } catch (RepositoryException e) {
                        if (verbose.isVerbose())
                            e.printStackTrace();
                        // keep on trying the next candidate
                    }
                }
                if (gav==null)
                    return null;    // couldn't find it

                installedPluginList.add(tokens[0],gav);
            }
            DependencyResult r = resolveDependencies(toArtifact(gav));
    
            URLClassLoader cl = createClassLoader(r.getRoot());
            injector = createChildModule(injector, cl);
        }
        Provider<ICommand> p;
        try {
            p = injector.getProvider(Key.get(ICommand.class, AnnotationLiteral.of(CLICommand.class, name)));
        } catch (ConfigurationException e) {
            return null; // failed to find the command
        }
        return p.get();
    }

    private Injector createChildModule(Injector parent, final URLClassLoader cl) throws InstantiationException, IOException {
        final List<Module> childModules = new ArrayList<Module>();
        childModules.add(new ExtensionFinder(cl) {
            @Override
            protected <T> void bind(Class<? extends T> impl, Class<T> extensionPoint) {
                if (impl.getClassLoader()!=cl)  return; // only add newly discovered stuff

                // install CLIModules
                if (extensionPoint==CLIModule.class) {
                    try {
                        install((Module)impl.newInstance());
                    } catch (InstantiationException e) {
                        throw (Error)new InstantiationError().initCause(e);
                    } catch (IllegalAccessException e) {
                        throw (Error)new IllegalAccessError().initCause(e);
                    }
                    return;
                }
                super.bind(impl,extensionPoint);
            }
        });

        return parent.createChildInjector(childModules);
    }

    /**
     * Controls the mapping from the command set prefix to the artifact.
     *
     * @return
     *      Artifacts that we should try to locate, in the order of preference.
     */
    protected Collection<GAV> mapCommandToArtifacts(String prefix) {
        return Arrays.asList(
            new GAV("com.cloudbees.sdk.plugins", prefix + "-plugin", "LATEST"),
            new GAV("org.cloudbees.sdk.plugins", prefix + "-plugin", "LATEST")
        );
    }
    
    private static Artifact toArtifact(GAV gav) {
        return new DefaultArtifact(gav.groupId,gav.artifactId,"jar",gav.version);
    }

    private DependencyResult resolveDependencies(Artifact a) throws DependencyCollectionException, DependencyResolutionException {
        MavenRepositorySystemSession session = sessionFactory.get();
        Dependency dependency = new Dependency(a, "compile");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        configureRepositories(collectRequest);
        DependencyNode node = rs.collectDependencies(session, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest(node,new ScopeDependencyFilter("provided"));

        return rs.resolveDependencies(session, dependencyRequest);
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
