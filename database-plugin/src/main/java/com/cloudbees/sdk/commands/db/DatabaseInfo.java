package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.staxnet.appserver.utils.StringHelper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Database")
@CLICommand("db:info")
public class DatabaseInfo extends DatabaseBase {
    private Boolean password;

    public DatabaseInfo() {
    }

    public void setPassword(Boolean password) {
        this.password = password;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "p", "password", false, "print the database password info" );

        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getBeesClient();
        boolean fetchPassword = password == null ? false : true;
        com.cloudbees.api.DatabaseInfo res = client.databaseInfo(getDatabaseName(), fetchPassword);

        if (isTextOutput()) {
            System.out.println( "Database name: " + res.getName());
            System.out.println( "Account:       " + res.getOwner());
            System.out.println( "Status:        " + res.getStatus());
            System.out.println( "Master:        " + res.getMaster() + ":" + res.getPort());
            if (res.getSlaves() != null && res.getSlaves().length > 0)
                System.out.println( "Slaves:        " + StringHelper.join(res.getSlaves(), ","));
            System.out.println( "Port:          " + res.getPort());
            System.out.println( "Username:      " + res.getUsername());
            if (fetchPassword)
                System.out.println( "Password:      " + res.getPassword());
        } else
            printOutput(res, com.cloudbees.api.DatabaseInfo.class);

        return true;
    }
}
