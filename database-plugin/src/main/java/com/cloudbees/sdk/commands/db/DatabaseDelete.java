package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseDeleteResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@CommandGroup("Database")
@CLICommand("db:delete")
public class DatabaseDelete extends DatabaseBase {
    private Boolean force;

    public DatabaseDelete() {
    }

    public void setForce(Boolean force) {
        this.force = force;
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
            if (!Helper.promptMatches("Are you sure you want to delete this database [" + getDatabaseName() + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        BeesClient client = getBeesClient();
        DatabaseDeleteResponse res = client.databaseDelete(getDatabaseName());

        if (isTextOutput()) {
            if(res.isDeleted())
                System.out.println("database deleted - " + getDatabaseName());
            else
                System.out.println("database could not be deleted");
        } else
            printOutput(res, DatabaseDeleteResponse.class);

        return true;
    }

}
