package com.cloudbees.sdk.commands;

import org.apache.commons.cli.Option;

/**
 * @author Fabian Donze
 */
public class CommandOption {
    private Option option;
    private boolean hidden;

    public CommandOption(Option option, boolean hidden) {
        this.option = option;
        this.hidden = hidden;
    }

    public CommandOption(String opt, String longOpt, boolean hasArg, String description, boolean hidden) {
        if (longOpt != null)
            option = new Option(opt, longOpt, hasArg, description);
        else
            option = new Option(opt, hasArg, description);
        this.hidden = hidden;
    }

    public Option getOption() {
        return option;
    }

    public boolean isHidden() {
        return hidden;
    }

}
