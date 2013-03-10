package com.cloudbees.sdk.cli;

import com.cloudbees.sdk.AbortException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.ClassParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.OptionHandler;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Partial implementation of {@link ACommand} that provides more integration
 * between argument parsing and dependency injections.
 *
 * <p>
 * To encourage commonality between various commands and simplify the development of new commands,
 * this subtype handles argument processing via <a href="http://args4j.kohsuke.org/">args4j</a>.
 * Primarily this means you'll be defining your command line options as annotated fields or setter
 * methods on subtypes. See {@link Option} and {@link Argument} for more details.
 *
 * <p>
 * In addition to annotating setters and fields, the argument parsing code also looks for any
 * {@linkplain Inject injected} fields whose type has {@link HasOptions} marker interface.
 * There are several injectable option fragment components (such as {@link Verbose}) that contains
 * this marker interface, and whatever options that these components define also becomes a part of
 * the recognizable options.
 *
 * <p>
 * By sharing and reusing such option fragment components, users will see consistent user interfaces
 * and command developers are freed from lower-level option parsing.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractCommand extends ACommand {
    @Inject
    Verbose verbose;

    public abstract int main() throws Exception;

    @Override
    public int run(List<String> args) throws Exception {
        CmdLineParser p = createParser();
        try {
            p.parseArgument(args.subList(1,args.size()));
            return main();
        } catch (AbortException e) {
            System.err.println(e.getMessage());
            return 1;
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: bees "+args.get(0)+" "+p.printExample(ExampleMode.REQUIRED));
            p.printUsage(System.err);
            return 1;
        }
    }

    protected CmdLineParser createParser() {
        CmdLineParser p = new CmdLineParser(this);

        Set<Object> collected = new HashSet<Object>();
        collected.add(this);
        // if any injected component define options, include them
        collectComponentsWithOptions(p, this, collected);
        return p;
    }

    /**
     * Recursively visits object graph, finds all {@link HasOptions} components,
     * and adds them all to the parser.
     */
    private void collectComponentsWithOptions(CmdLineParser p, Object o, Set<Object> collected) {
        for (Class c = o.getClass(); c!=null; c=c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(Inject.class) && HasOptions.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object child = f.get(o);
                        if (child!=null && collected.add(child)) {
                            new ClassParser().parse(child,p);
                            collectComponentsWithOptions(p,child,collected);
                        }
                    } catch (IllegalAccessException e) {
                        throw new Error(e);
                    }
                }
            }
        }
    }

    @Override
    public void printHelp(List<String> args) {
        CmdLineParser p = createParser();
        if (getUsageMessage() != null)
            System.err.println("Usage: bees "+args.get(0)+" "+getUsageMessage());
        else {
            System.err.print("Usage: bees "+args.get(0)+p.printExample(ExampleMode.REQUIRED));
            for (OptionHandler optionHandler: p.getArguments()) {
                if (optionHandler.option.required())
                    System.err.print(" " + optionHandler.getMetaVariable(null));
                else
                    System.err.print(" [" + optionHandler.getMetaVariable(null) + "]");
            }
            System.err.println();
        }
        p.printUsage(System.err);
    }

    protected String getUsageMessage() {
        return null;
    }

}
