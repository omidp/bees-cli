package com.cloudbees.sdk.commands.ant;


import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Project")
@CLICommand("ant:ant")
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
