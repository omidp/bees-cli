package com.cloudbees.sdk.annotations;

import com.cloudbees.sdk.CLICommand;

import java.lang.annotation.Annotation;

/**
 * @author Kohsuke Kawaguchi
 */
public class CLICommandImpl implements CLICommand {
    private final String value;

    public CLICommandImpl(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public Class<? extends Annotation> annotationType() {
        return CLICommand.class;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CLICommand)) return false;

        CLICommand that = (CLICommand) o;
        return value.equals(that.value());
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public String toString() {
        return "@CLICommand("+value+")";
    }
}
