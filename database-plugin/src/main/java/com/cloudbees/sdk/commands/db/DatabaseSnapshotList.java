package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseSnapshotInfo;
import com.cloudbees.api.DatabaseSnapshotListResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Database")
@CLICommand("db:snapshot:list")
public class DatabaseSnapshotList extends DatabaseBase {

    public DatabaseSnapshotList() {
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getBeesClient();
        DatabaseSnapshotListResponse res = client.databaseSnapshotList(getDatabaseName());

        if (isTextOutput()) {
            System.out.println("Snapshot   Title                          Created");
            System.out.println();
            for (DatabaseSnapshotInfo snapshotInfo: res.getSnapshots()) {
                System.out.println(s(snapshotInfo.getId(), 10)+ " " + s(snapshotInfo.getTitle(), 30)+ " " + snapshotInfo.getCreated());
            }
        } else
            printOutput(res, DatabaseSnapshotListResponse.class, DatabaseSnapshotInfo.class);

        return true;
    }

    private String s(String str, int length) {
        return Helper.getPaddedString(str, length);
    }
}
