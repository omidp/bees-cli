package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseSnapshotInfo;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Database")
@CLICommand("db:snapshot:create")
public class DatabaseSnapshotCreate extends DatabaseBase {
    private String title;

    public DatabaseSnapshotCreate() {
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "t", "title", true, "the database snapshot title" );

        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getStaxClient();
        DatabaseSnapshotInfo res = client.databaseSnapshotCreate(getDatabaseName(), title);

        if (isTextOutput()) {
            System.out.println( "Snapshot ID: " + res.getId());
            if (res.getTitle() != null)
                System.out.println( "Title:       " + res.getTitle());
            System.out.println( "Created:     " + res.getCreated());
        } else
            printOutput(res, DatabaseSnapshotInfo.class);

        return true;
    }
}
