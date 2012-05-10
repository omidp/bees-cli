package com.cloudbees.sdk.commands.ant;


import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@CLICommand("ant")
@BeesCommand(group="Project")
public class ProjectAntTarget extends AntTarget {
    public ProjectAntTarget() {
        super(null);
        setArgumentExpected(1);
    }

    @Override
    protected String getUsageMessage() {
        return  "ANT_TARGET";
    }

    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setTarget(getParameters().get(0));
            return true;
        }
        return false;
    }

}
