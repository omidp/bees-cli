package com.cloudbees.sdk.commands.services;

import com.cloudbees.api.ServiceSubscriptionInfo;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class ServiceBase extends Command {
    private String account;
    private Map<String, String> settings;

    public ServiceBase() {
        super();
        settings = new HashMap<String, String>();
    }

    public String getParameterName() {
        return getParameters().size() > 0 ? getParameters().get(0) : null;
    }

    public String getAccount() throws IOException {
        if (account == null) account = getDefaultDomain();
        if (account == null) account = Helper.promptFor("Account name: ", true);
        return account;
    }

    protected String getDefaultDomain() {
        return getConfigProperties().getProperty("bees.project.app.domain");
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * This method is call before parsing the command line.
     * This is the place to add command line options
     *
     * @return true if successful, false otherwise
     */
    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "account", true, "Account Name" );
        return true;
    }

    /**
     * This method is call after parsing the command line.
     * This is the place to parse additional command arguments
     *
     * @return true if successful, false otherwise
     */
    @Override
    protected boolean postParseCommandLine() {
        List otherArgs = getCommandLine().getArgList();
        for (int i=0; i<otherArgs.size(); i++) {
            String str = (String)otherArgs.get(i);
            int idx = isSetting(str);
            if (idx > -1) {
                settings.put(str.substring(0, idx), str.substring(idx+1));
            } else {
                addParameter(str);
            }
        }
        return true;
    }

    private int isSetting(String str) {
        boolean endQuote = true;
        int length = str.length();
        for (int i=0; i<length; i++) {
            char c = str.charAt(i);
            if (c == '"') endQuote = !endQuote;
            if (c == '=' && endQuote)
                return i;
        }
        return -1;
    }

    protected void displayResult(ServiceSubscriptionInfo subscription) {
        if (isTextOutput()) {
            if (subscription.getMessage() != null)
                System.out.println(subscription.getMessage());
            System.out.println("Subscription: " + subscription.getId() + "/" + subscription.getService());
            if (subscription.getPlan() != null)
                System.out.println("plan: " + subscription.getPlan());
            Map<String, String> config = subscription.getConfig();
            if(config != null && config.size() > 0) {
                System.out.println("config:");
                for (Map.Entry<String, String> entry : config.entrySet()) {
                    System.out.println("  " + entry.getKey() + "=" + entry.getValue());
                }
            }
            Map<String, String> settings = subscription.getSettings();
            if(settings != null && settings.size() > 0) {
                System.out.println("settings:");
                for (Map.Entry<String, String> entry : settings.entrySet()) {
                    System.out.println("  " + entry.getKey() + "=" + entry.getValue());
                }
            }
        } else
            printOutput(subscription, ServiceSubscriptionInfo.class);
    }

    protected String pad(String str, int length) {
        return Helper.getPaddedString(str, length);
    }

}
