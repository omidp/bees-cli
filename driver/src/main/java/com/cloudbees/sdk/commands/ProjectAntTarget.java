package com.cloudbees.sdk.commands;


import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.CommandGroup;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Project")
@CLICommand(value="ant")
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