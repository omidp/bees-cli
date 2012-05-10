package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ResourceBindingInfo;
import com.cloudbees.api.ServiceResourceBindingListResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application")
@CLICommand("app:bindings")
public class ApplicationBindingList extends ApplicationBase {

    public ApplicationBindingList() {
        setArgumentExpected(0);
    }

    @Override
    protected boolean execute() throws Exception {
        StaxClient client = getStaxClient(StaxClient.class);
        ServiceResourceBindingListResponse res = client.resourceBindingList("cb-app", getAppId());
        if (isTextOutput()) {
            System.out.println("Applications bindings:");
            for (ResourceBindingInfo binding: res.getBindings()) {
                System.out.println(binding.getAlias() + " " + binding.getToService() + ":" + binding.getToResourceId());
                Map<String, String> config = binding.getConfig();
                if(config != null && config.size() > 0) {
                    System.out.println("  config:");
                    for (Map.Entry<String, String> entry : config.entrySet()) {
                        System.out.println("    " + entry.getKey() + "=" + entry.getValue());
                    }
                }
            }
        } else
            printOutput(res, ResourceBindingInfo.class, ServiceResourceBindingListResponse.class);
        return true;
    }

}
