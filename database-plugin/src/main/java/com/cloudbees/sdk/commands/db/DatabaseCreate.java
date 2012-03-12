package com.cloudbees.sdk.commands.db;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseCreateResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.utils.Helper;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Database")
@CLICommand("db:create")
public class DatabaseCreate extends DatabaseBase {
    private String username;
    private String password;
    private String account;

    public DatabaseCreate() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "u", "username", true, "Database username (must be unique)" );
        addOption( "p", "password", true, "Database password" );
        addOption( "a", "account", true, "Account Name" );

        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        getDatabaseName();
        if (username == null) username = Helper.promptFor("Database Username (must be unique): ", true);
        if (password == null) password = Helper.promptFor("Database Password: ", true);

        if (account == null)
            account = getConfigProperties().getProperty("bees.project.app.domain");
        if (account == null) account = Helper.promptFor("Account name: ", true);

        BeesClient client = getStaxClient();
        DatabaseCreateResponse res = client.databaseCreate(account, getDatabaseName(), username, password);

        if (isTextOutput()) {
            System.out.println("database created: " + res.getDatabaseId());
        } else
            printOutput(res, DatabaseCreateResponse.class);


        return true;
    }


}
