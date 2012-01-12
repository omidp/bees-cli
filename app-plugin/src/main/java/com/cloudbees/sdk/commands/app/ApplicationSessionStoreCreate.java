package com.cloudbees.sdk.commands.app;

import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

import java.util.Map;

/**
 * @author Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:session-store:create")
public class ApplicationSessionStoreCreate extends ApplicationResourceCreate {
    private String size;

    public ApplicationSessionStoreCreate() {
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( "size", true, "The session store size plan");

            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        Map<String, String> settings = getSettings();
        if (getSize() != null)
            settings.put("size", getSize());
        return super.execute();
    }
}
