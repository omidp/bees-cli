package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ServiceResourceBindResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.commands.services.ServiceBase;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application")
@CLICommand("app:bind")
public class ApplicationBind extends ServiceBase {
    /**
     * The id of the application.
     */
    private String appid;

    private String alias;

    private String serviceName;

    private String resourceId;

    public ApplicationBind() {
        super();
        setArgumentExpected(0);
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid;
    }

    public String getAlias() {
        if (alias == null) {
            String[] parts = resourceId.split("/");
            alias = parts.length < 2 ? resourceId : parts[1];
        }
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getServiceName() {
        return serviceName;
    }


    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getResourceId() throws IOException {
        if (resourceId == null) setResourceId(Helper.promptFor("Resource ID fully qualified (SERVICE:RESOURCE_ID): ", true));
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        String[] parts = resourceId.split(":");
        if (parts.length < 2) {
            setServiceName("cb-app");
        } else {
            setServiceName(parts[0]);
            resourceId = parts[1];
        }

        // Check if resourceId is fully qualified with account name
        parts = resourceId.split("/");
        if (parts.length < 2) {
            try {
                resourceId = getAccount() + "/" + resourceId;
            } catch (IOException ignored) {}
        }
        this.resourceId = resourceId;
    }

    public void setDatabase(String database) {
        setServiceName("cb-db");
        try {
            String[] parts = database.split("/");
            if (parts.length < 2)
                resourceId = getAccount() + "/" + database;
            else
                resourceId = database;
        } catch (IOException ignored) {}
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "appid", true, "CloudBees application ID");
        addOption( "as", "alias", true, "Binding alias name");
        addOption( "r", "resourceId", true, "Resource ID fully qualified (SERVICE:RESOURCE_ID)");
        addOption( "db", "database", true, "Database name. Shortcut option to bind to a database resource (instead of using -r)");

        return true;
    }

    @Override
    protected String getUsageMessage() {
        return "[settingX=valueY]";
    }

    @Override
    protected boolean execute() throws Exception {
        initAppId();

        // force resourceId input if not specified
        getResourceId();

        StaxClient client = getStaxClient(StaxClient.class);
        ServiceResourceBindResponse res = client.resourceBind("cb-app", getAppid(), getServiceName(), getResourceId(), getAlias(), getSettings());
        if (isTextOutput()) {
//            System.out.println("Message: " + res.getMessage());
            System.out.println("application - " + getAppid() + " bound to " + getServiceName() + ":" + getResourceId() + " as " + getAlias());
        } else
            printOutput(res, ServiceResourceBindResponse.class);
        return true;
    }

    private void initAppId() throws IOException
    {
        if (appid == null || appid.equals("")) {
            appid = AppHelper.getArchiveApplicationId();
        }

        if (appid == null || appid.equals(""))
            appid = Helper.promptForAppId();

        if (appid == null || appid.equals(""))
            throw new IllegalArgumentException("No application id specified");

        String[] parts = appid.split("/");
        if (parts.length < 2)
            appid = getAccount() + "/" + appid;
    }


}
