package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ServiceResourceInfo;
import com.cloudbees.api.ServiceResourceListResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
@CommandGroup("Application")
@CLICommand("app:resource:list")
public class ApplicationResourceList extends ApplicationResourceBase {
    private String type;

    public ApplicationResourceList() {
        super();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "t", "type", true, "resource type" );
        }
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        StaxClient client = getStaxClient();
        ServiceResourceListResponse res = client.serviceResourceList(getServiceName(), getAccount(), getType());
        List<ServiceResourceInfo> resources = new ArrayList<ServiceResourceInfo>();
        for (ServiceResourceInfo resource: res.getResources()) {
            if (resource.getResourceType() != null && !resource.getResourceType().equalsIgnoreCase("application")) {
                resources.add(resource);
            }
        }
        res.setResources(resources);
        displayResult(res);
        return true;
    }

    protected void displayResult(ServiceResourceListResponse res) {
        if (isTextOutput()) {
            List<ServiceResourceInfo> resources = res.getResources();
            System.out.println("Resources:");
            for (ServiceResourceInfo resource: resources) {
                System.out.println(resource.getService() + ":" + resource.getId());
                Map<String, String> config = resource.getConfig();
                if(config != null && config.size() > 0) {
                    System.out.println("  config:");
                    for (Map.Entry<String, String> entry : config.entrySet()) {
                        System.out.println("    " + entry.getKey() + "=" + entry.getValue());
                    }
                }
                Map<String, String> settings = resource.getSettings();
                if(settings != null && settings.size() > 0) {
                    System.out.println("  settings:");
                    for (Map.Entry<String, String> entry : settings.entrySet()) {
                        System.out.println("    " + entry.getKey() + "=" + entry.getValue());
                    }
                }
            }
        } else
            printOutput(res, ServiceResourceListResponse.class, ServiceResourceInfo.class);
    }

}
