package com.cloudbees.sdk.cli;

import org.kohsuke.args4j.Option;

import javax.inject.Singleton;

/**
 * Injectable component that captures the verboseness flag.
 * 
 * This is not a binding annotation but a value holder class because
 * wiring needs to happen before arguments are parsed.
 * 
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class Verbose implements HasOptions {
    @Option(name="-v",aliases="--verbose",usage="Make the command output more verbose")
    private boolean verbose;

    public Verbose() {
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
