package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseSnapshotDeleteResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Database")
@CLICommand("db:snapshot:delete")
public class DatabaseSnapshotDelete extends DatabaseBase {
    private Boolean force;
    private String snapshot;

    public DatabaseSnapshotDelete() {
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
        addOption( "f", "force", false, "force delete without prompting" );

        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to delete snapshot [" + getSnapshot() + "] for database [" + getDatabaseName() + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        BeesClient client = getBeesClient();
        DatabaseSnapshotDeleteResponse res = client.databaseSnapshotDelete(getDatabaseName(), getSnapshot());

        if (isTextOutput()) {
            if(res.isDeleted())
                System.out.println("database snapshot deleted - " + getSnapshot());
            else
                System.out.println("database snapshot could not be deleted");
        } else
            printOutput(res, DatabaseSnapshotDeleteResponse.class);

        return true;
    }

}
