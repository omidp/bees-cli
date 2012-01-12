package com.cloudbees.sdk.commands.app;


import com.cloudbees.api.ApplicationGetSourceUrlResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.cli.Experimental;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.appserver.utils.ZipHelper;

import java.io.File;
import java.io.FileInputStream;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Application")
@CLICommand("app:getsource")
@Experimental
public class ApplicationGetSource extends ApplicationBase {
    private Boolean force;
    private String dir;

    public ApplicationGetSource() {
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getDir() {
        return dir != null ? dir : ".";
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force overwrite without prompting" );
            addOption( "d", "dir", true, "target directory, default [.]" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        StaxClient client = getStaxClient();
        ApplicationGetSourceUrlResponse res = client.applicationGetSourceUrl(getAppId());

        if (res.getUrl() != null) {
            String[] parts = getAppId().split("/");
            File dirName = new File(getDir(), parts[1]);
            if (force == null || !force.booleanValue()) {
                if (dirName.exists() && dirName.list().length > 0) {
                    if (!Helper.promptMatches("WARNING: The target directory contains files that may be overwritten. \n[target directory: " + dirName.getCanonicalPath() + "]\nDo you want to continue  (y/n) ", "[yY].*")) {
                        return true;
                    }
                }
            }
            System.out.print("Downloading...");
            dirName.mkdirs();
            String fileName = dirName.getCanonicalPath() + ".zip";
            Helper.downloadFile(res.getUrl(), fileName);

            FileInputStream fin = new FileInputStream(fileName);
            ZipHelper.unzipFile(fin, new File(getDir(), parts[1]), true);

            new File(fileName).delete();
            System.out.println(" DONE");
        }
        return true;
    }


}
