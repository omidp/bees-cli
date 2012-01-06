package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.CLICommand;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.ant.StringHelper;

import java.io.IOException;

/**
 * @Author: Fabian Donze
 */
@CLICommand("db:info")
public class DatabaseInfo extends Command {
    private Boolean password;

    private String databaseName;

    public DatabaseInfo() {
        setArgumentExpected(1);
    }

    public void setPassword(Boolean password) {
        this.password = password;
    }

    public void setDatabaseName(String databaseName) {
/*
        String[] parts = databaseName.split("/");
        if (parts.length > 1)
            this.databaseName = parts[1];
        else
*/
            this.databaseName = databaseName;
    }

    @Override
    protected String getUsageMessage() {
        return "DATABASE_NAME";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "p", "password", false, "print the database password info" );

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

        StaxClient client = getStaxClient();
        boolean fetchPassword = password == null ? false : true;
        com.cloudbees.api.DatabaseInfo res = client.databaseInfo(databaseName, fetchPassword);

        if (isTextOutput()) {
            System.out.println( "Database name: " + res.getName());
            System.out.println( "Account:       " + res.getOwner());
            System.out.println( "Status:        " + res.getStatus());
            System.out.println( "Master:        " + res.getMaster() + ":" + res.getPort());
            if (res.getSlaves() != null && res.getSlaves().length > 0)
                System.out.println( "Slaves:        " + StringHelper.join(res.getSlaves(), ","));
            System.out.println( "Port:          " + res.getPort());
//            System.out.println( "JDBC URL:      jdbc:cloudbees://" + res.getName());
            System.out.println( "Username:      " + res.getUsername());
            if (fetchPassword)
                System.out.println( "Password:      " + res.getPassword());
        } else
            printOutput(res, com.cloudbees.api.DatabaseInfo.class);

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
