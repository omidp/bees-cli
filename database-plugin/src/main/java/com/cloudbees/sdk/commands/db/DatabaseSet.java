package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseSetPasswordResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Database")
@CLICommand("db:set")
public class DatabaseSet extends DatabaseBase {
    private String password;

    public DatabaseSet() {
    }

    protected String getPassword() throws IOException {
        if (password == null) password = Helper.promptFor("Database password: ", true);
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "p", "password", true, "The database password" );

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
        BeesClient client = getStaxClient();
        DatabaseSetPasswordResponse res = client.databaseSetPassword(getDatabaseName(), getPassword());

        if (isTextOutput()) {
            if(res.isSuccess())
                System.out.println("database password set");
            else
                System.out.println("database could not be changed");
        } else
            printOutput(res, DatabaseSetPasswordResponse.class);

        return true;
    }

}
