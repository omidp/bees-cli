package com.cloudbees.sdk.commands;

import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;
import java.util.Map;

/**
 * @Author: Fabian Donze
 */
public class ApplicationList extends Command {
    private String account;

    public ApplicationList() {
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "account", true, "Account Name" );

        return true;
    }

    @Override
    protected boolean postParseCommandLine() {
        return true;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    protected String getAccount() throws IOException {
        return account;
    }

    @Override
    protected boolean execute() throws Exception {
        StaxClient client = getStaxClient();
        ApplicationListResponse res = client.applicationList(getAccount());

        if (isTextOutput()) {
            System.out.println("Application                Status    URL                           Instance(s)");
            System.out.println();
            for (com.cloudbees.api.ApplicationInfo applicationInfo: res.getApplications()) {
                String msg = s(applicationInfo.getId(), 26)+ " " + s(applicationInfo.getStatus(), 10) + s(applicationInfo.getUrls()[0], 38);
                Map<String, String> settings = applicationInfo.getSettings();
                if (settings != null) {
                    msg += " " + settings.get("clusterSize");
                }
                System.out.println(msg);
            }
        } else {
            printOutput(res, ApplicationInfo.class, ApplicationListResponse.class);
        }

        return true;
    }

    private String s(String str, int length) {
        return Helper.getPaddedString(str, length);
    }

}
