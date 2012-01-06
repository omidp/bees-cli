package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.DatabaseDeleteResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;

/**
 * @Author: Fabian Donze
 */
@CLICommand("db:delete")
public class DatabaseDelete extends Command {
    private Boolean force;

    private String databaseName;

    public DatabaseDelete() {
        setArgumentExpected(1);
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    protected String getUsageMessage() {
        return "DATABASE_NAME";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "f", "force", false, "force delete without prompting" );

        return true;
    }

    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setDatabaseName(getParameters().get(0));
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        initDataBaseName();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to delete this database [" + databaseName + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        StaxClient client = getStaxClient();
        DatabaseDeleteResponse res = client.databaseDelete(databaseName);

        if (isTextOutput()) {
            if(res.isDeleted())
                System.out.println("database deleted - " + databaseName);
            else
                System.out.println("database could not be deleted");
        } else
            printOutput(res, DatabaseDeleteResponse.class);

        return true;
    }

    private void initDataBaseName() throws IOException
    {
        if (databaseName == null || databaseName.equals(""))
            databaseName = Helper.promptFor("Database name: ");

        if (databaseName == null || databaseName.equals(""))
            throw new IllegalArgumentException("No database name specified");
    }


}
