package com.cloudbees.sdk.commands.app;


import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.utils.Helper;

import java.util.*;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:info")
public class ApplicationInfo extends ApplicationBase {

    public ApplicationInfo() {
    }

    @Override
    protected boolean execute() throws Exception {
        StaxClient client = getStaxClient();
        com.cloudbees.api.ApplicationInfo res = client.applicationInfo(getAppId());
        if (isTextOutput()) {
            System.out.println( "Application     : " + res.getId());
            System.out.println( "Title           : " + res.getTitle());
            System.out.println( "Created         : " + res.getCreated());
            System.out.println( "Status          : " + res.getStatus());
            System.out.println( "URL             : " + res.getUrls()[0]);
            Map<String, String> settings = res.getSettings();
            if (settings != null) {
                List<String> list = new ArrayList<String>(settings.size());
                for (Map.Entry<String, String> entry: settings.entrySet()) {
                    list.add(Helper.getPaddedString(entry.getKey(), 16) + ": " + entry.getValue());
                }
                Collections.sort(list);
                for (String item : list)
                    System.out.println(item);
            }
        } else {
            printOutput(res, com.cloudbees.api.ApplicationInfo.class);
        }

        return true;
    }

}
