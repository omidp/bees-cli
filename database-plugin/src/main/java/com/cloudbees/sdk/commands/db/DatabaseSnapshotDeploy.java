package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseSnapshotDeployResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.utils.Helper;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Database")
@CLICommand("db:snapshot:deploy")
public class DatabaseSnapshotDeploy extends DatabaseBase {
    private Boolean force;
    private String snapshot;

    public DatabaseSnapshotDeploy() {
        setArgumentExpected(2);
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    protected String getUsageMessage() {
        return "DATABASE_NAME SNAPSHOT_ID";
    }

    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setSnapshot(getParameters().get(1));
            return true;
        }
        return false;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "f", "force", false, "force deploy without prompting" );

        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to deploy snapshot [" + getSnapshot() + "] for database [" + getDatabaseName() + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        BeesClient client = getStaxClient();
        DatabaseSnapshotDeployResponse res = client.databaseSnapshotDeploy(getDatabaseName(), getSnapshot());

        if (isTextOutput()) {
            if(res.isDeployed())
                System.out.println("database snapshot deployed - " + getSnapshot());
            else
                System.out.println("database snapshot could not be deployed");
        } else
            printOutput(res, DatabaseSnapshotDeployResponse.class);

        return true;
    }

}
