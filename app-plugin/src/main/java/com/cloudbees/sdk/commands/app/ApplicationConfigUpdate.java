package com.cloudbees.sdk.commands.app;


import com.cloudbees.api.ApplicationConfigUpdateResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.CommandGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@CommandGroup("Application")
@CLICommand("app:update")
public class ApplicationConfigUpdate extends ApplicationBase {
    private Map<String, String> settings;

    public ApplicationConfigUpdate() {
        super();
        setArgumentExpected(1);
        settings = new HashMap<String, String>();
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * This method is call by the help command.
     * This is the place to define the command usage.
     * No need to return the options, they will be automatically added to the help
     *
     * @return usage String
     */
    @Override
    protected String getUsageMessage() {
        return "APPLICATION_ID [parameterX=valueY]";
    }

    @Override
    protected boolean postParseCommandLine() {
        if (super.postParseCommandLine()) {
            List otherArgs = getCommandLine().getArgList();
            for (int i=0; i<otherArgs.size(); i++) {
                String str = (String)otherArgs.get(i);
                int idx = isParameter(str);
                if (idx > -1) {
                    settings.put(str.substring(0, idx), str.substring(idx+1));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appId = getAppId();
        StaxClient client = getStaxClient();
        ApplicationConfigUpdateResponse res = client.applicationConfigUpdate(appId, getSettings());
        if (isTextOutput()) {
            System.out.println("application - " + appId + " updated: " + res.getStatus());
        } else
            printOutput(res, ApplicationConfigUpdateResponse.class);

        return true;
    }
}
