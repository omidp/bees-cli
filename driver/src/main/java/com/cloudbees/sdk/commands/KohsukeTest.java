package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.AbstractCommand;
import com.cloudbees.sdk.BeesClientFactory;
import com.cloudbees.sdk.CLICommand;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
@CLICommand("kohsuke")
public class KohsukeTest extends AbstractCommand {
    @Inject
    BeesClientFactory factory;

    @Override
    public int main() throws Exception {
        factory.get();
        return 0;
    }
}
